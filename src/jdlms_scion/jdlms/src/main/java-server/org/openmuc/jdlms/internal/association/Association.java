/*
 * Copyright 2012-20 Fraunhofer ISE
 *
 * This file is part of jDLMS.
 * For more information visit http://www.openmuc.org
 *
 * jDLMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jDLMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jDLMS.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openmuc.jdlms.internal.association;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import org.openmuc.jdlms.DataDirectory;
import org.openmuc.jdlms.SecuritySuite;
import org.openmuc.jdlms.ServerConnectionInfo.Status;
import org.openmuc.jdlms.ServerConnectionListener;
import org.openmuc.jdlms.internal.APdu;
import org.openmuc.jdlms.internal.DataDirectoryImpl;
import org.openmuc.jdlms.internal.DataDirectoryImpl.CosemLogicalDevice;
import org.openmuc.jdlms.internal.ReleaseReqReason;
import org.openmuc.jdlms.internal.ReleaseRespReason;
import org.openmuc.jdlms.internal.ServerConnectionData;
import org.openmuc.jdlms.internal.ServiceError;
import org.openmuc.jdlms.internal.StateError;
import org.openmuc.jdlms.internal.asn1.cosem.COSEMpdu;
import org.openmuc.jdlms.internal.asn1.cosem.COSEMpdu.Choices;
import org.openmuc.jdlms.internal.asn1.iso.acse.ACSEApdu;
import org.openmuc.jdlms.internal.asn1.iso.acse.RLREApdu;
import org.openmuc.jdlms.internal.asn1.iso.acse.RLRQApdu;
import org.openmuc.jdlms.internal.association.ln.ActionRequestProcessor;
import org.openmuc.jdlms.internal.association.ln.GetRequestProcessor;
import org.openmuc.jdlms.internal.association.ln.SetRequestProcessor;
import org.openmuc.jdlms.internal.association.sn.ReadRequestProcessor;
import org.openmuc.jdlms.internal.association.sn.WriteRequestProcessor;
import org.openmuc.jdlms.internal.transportlayer.server.ServerConnectionInformationImpl;
import org.openmuc.jdlms.sessionlayer.server.ServerSessionLayer;
import org.openmuc.jdlms.settings.client.ReferencingMethod;
import org.openmuc.jdlms.settings.server.ServerSettings;

public class Association implements Runnable {

    private final DataDirectoryImpl directory;
    private final ServerSettings settings;
    private final ServerConnectionInformationImpl serverConnectionInformation;

    protected final ServerConnectionData connectionData;
    protected final AssociationMessenger associationMessenger;

    public Association(DataDirectory directory, ServerSessionLayer sessionLayer, Long connectionId,
            ServerSettings settings, ServerConnectionInformationImpl serverConnectionInformation) {
        this.settings = settings;
        this.directory = (DataDirectoryImpl) directory;
        this.serverConnectionInformation = serverConnectionInformation;

        this.connectionData = new ServerConnectionData(sessionLayer, connectionId);

        this.directory.addConnection(connectionId, this.connectionData);

        this.associationMessenger = new AssociationMessenger(connectionData, this.directory);
    }

    @Override
    public final void run() {
        try {
            startAssociation();
        } catch (GenericAssociationException e) {
            try {
                associationMessenger.encodeAndSend(e.getErrorMessageApdu());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (EOFException e) {
            // client closed the connection..
            e.printStackTrace();
        } catch (SocketTimeoutException e) {
            // client was too slow
            e.printStackTrace();
        } catch (IOException e) {
            // ignore
        } finally {
            this.directory.removeConnection(this.connectionData.getConnectionId());
            try {
                sessionLayer().close();
            } catch (IOException e1) {
                // ignore
                e1.printStackTrace();
            }
            notifyListener(Status.CLOSED);
        }

    }

    private void startAssociation() throws IOException {
        connectionData.getSessionLayer().initialize();

        byte[] payload = connectionData.getSessionLayer().readNextMessage();

        notifyListener(Status.OPEN);

        RequestProcessorData requestProcessorData = new RequestProcessorData(sessionLayer().getLogicalDeviceId(),
                directory, connectionData);

        this.connectionData.setClientId(sessionLayer().getClientId());
        CosemLogicalDevice cosemLogicalDevice = this.directory.getLogicalDeviceFor(sessionLayer().getLogicalDeviceId());

        initSecuritySuite(cosemLogicalDevice);

        InitiateMessageProcessor initialmessageProcessor = new InitiateMessageProcessor(this.connectionData,
                cosemLogicalDevice);
        APdu aarqAPdu = initialmessageProcessor.processInitialMessage(payload);

        this.associationMessenger.encodeAndSend(aarqAPdu);

        Map<COSEMpdu.Choices, RequestProcessor> requestProcessors = setUpRequestProcessors(requestProcessorData,
                initialmessageProcessor.getContextId().getReferencingMethod());

        while (true) {
            APdu apdu = associationMessenger.readNextApdu();
            ACSEApdu acseApdu = apdu.getAcseAPdu();
            COSEMpdu cosemPdu = apdu.getCosemPdu();
            if (acseApdu != null && acseApdu.getRlrq() != null) {
                sendDisconnectMessage(acseApdu.getRlrq());
                return;
            }

            if (!connectionData.isAuthenticated() && cosemPdu.getChoiceIndex() != COSEMpdu.Choices.ACTION_REQUEST) {
                throw new AssociationException(StateError.SERVICE_NOT_ALLOWED, ServiceError.OPERATION_NOT_POSSIBLE);
            }

            RequestProcessor requestProcessor = requestProcessors.get(cosemPdu.getChoiceIndex());
            if (requestProcessor != null) {
                requestProcessor.processRequest(cosemPdu);
            }
            else {
                // TODO handle other requests..
            }

            if (!connectionData.isAuthenticated()) {
                // TODO

            }
        }
    }

    protected Map<COSEMpdu.Choices, RequestProcessor> setUpRequestProcessors(RequestProcessorData requestProcessorData,
            ReferencingMethod referencingMethod) {

        Map<COSEMpdu.Choices, RequestProcessor> requestProcessors = new EnumMap<>(COSEMpdu.Choices.class);

        if (referencingMethod == ReferencingMethod.LOGICAL) {
            requestProcessors.put(COSEMpdu.Choices.ACTION_REQUEST,
                    new ActionRequestProcessor(associationMessenger, requestProcessorData));

            requestProcessors.put(COSEMpdu.Choices.GET_REQUEST,
                    new GetRequestProcessor(associationMessenger, requestProcessorData));

            requestProcessors.put(COSEMpdu.Choices.SET_REQUEST,
                    new SetRequestProcessor(associationMessenger, requestProcessorData));
        }
        else {// SN
            requestProcessors.put(Choices.READREQUEST,
                    new ReadRequestProcessor(associationMessenger, requestProcessorData));
            requestProcessors.put(Choices.WRITEREQUEST,
                    new WriteRequestProcessor(associationMessenger, requestProcessorData));
        }
        return requestProcessors;
    }

    private void initSecuritySuite(CosemLogicalDevice cosemLogicalDevice) {
        Map<Integer, SecuritySuite> restrictions = cosemLogicalDevice.getLogicalDevice().getRestrictions();
        this.connectionData.setSecuritySuite(restrictions.get(this.connectionData.getClientId()));
    }

    private ServerSessionLayer sessionLayer() {
        return connectionData.getSessionLayer();
    }

    private void notifyListener(Status status) {
        ServerConnectionListener connectionListener = settings.getConnectionListener();
        if (connectionListener == null) {
            return;
        }

        serverConnectionInformation.setClientId(sessionLayer().getClientId());
        serverConnectionInformation.setLogicalDeviceAddress(sessionLayer().getLogicalDeviceId());
        serverConnectionInformation.setStatus(status);
        connectionListener.connectionChanged(serverConnectionInformation);
    }

    private void sendDisconnectMessage(RLRQApdu rlrq) throws IOException {
        ReleaseReqReason reqReason = ReleaseReqReason.reasonFor(rlrq.getReason().value.longValue());

        ReleaseRespReason respReason;
        switch (reqReason) {

        case URGENT:
            respReason = ReleaseRespReason.NOT_FINISHED;
            break;

        case USER_DEFINED:
            respReason = ReleaseRespReason.USER_DEFINED;
            break;

        case NORMAL:
        case UNKNOWN:
        default:
            respReason = ReleaseRespReason.NORMAL;
            break;
        }

        RLREApdu rlre = new RLREApdu();
        rlre.setReason(respReason.toDlmsReason());
        ACSEApdu reAcse = new ACSEApdu();
        reAcse.setRlre(rlre);
        APdu reponseApdu = new APdu(reAcse, null);

        byte[] buffer = new byte[6];
        int length = reponseApdu.encode(buffer, null);

        byte[] data = Arrays.copyOfRange(buffer, buffer.length - length, buffer.length);
        associationMessenger.send(data);
        sessionLayer().close();
    }

}
