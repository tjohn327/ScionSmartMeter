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

import static org.openmuc.jdlms.AuthenticationMechanism.NONE;
import static org.openmuc.jdlms.internal.security.RandomSequenceGenerator.generateNewChallenge;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openmuc.jdlms.AuthenticationMechanism;
import org.openmuc.jdlms.ConformanceSetting;
import org.openmuc.jdlms.LogicalDevice;
import org.openmuc.jdlms.SecuritySuite;
import org.openmuc.jdlms.SecuritySuite.EncryptionMechanism;
import org.openmuc.jdlms.internal.APdu;
import org.openmuc.jdlms.internal.AssociateSourceDiagnostic.AcseServiceUser;
import org.openmuc.jdlms.internal.ConformanceSettingConverter;
import org.openmuc.jdlms.internal.ContextId;
import org.openmuc.jdlms.internal.DataDirectoryImpl.CosemLogicalDevice;
import org.openmuc.jdlms.internal.ObjectIdentifier;
import org.openmuc.jdlms.internal.ServerConnectionData;
import org.openmuc.jdlms.internal.asn1.cosem.COSEMpdu;
import org.openmuc.jdlms.internal.asn1.cosem.Conformance;
import org.openmuc.jdlms.internal.asn1.cosem.InitiateRequest;
import org.openmuc.jdlms.internal.asn1.iso.acse.AARQApdu;
import org.openmuc.jdlms.internal.asn1.iso.acse.ACSEApdu;
import org.openmuc.jdlms.internal.asn1.iso.acse.MechanismName;

class InitiateMessageProcessor {

    private final ServerConnectionData connectionData;
    private final CosemLogicalDevice cosemLogicalDevice;
    private final LogicalDevice logicalDevice;
    private ContextId contextId;

    public InitiateMessageProcessor(ServerConnectionData connectionData, CosemLogicalDevice cosemLogicalDevice) {
        this.connectionData = connectionData;
        this.cosemLogicalDevice = cosemLogicalDevice;
        this.logicalDevice = cosemLogicalDevice.getLogicalDevice();

        Map<Integer, SecuritySuite> restrictions = cosemLogicalDevice.getLogicalDevice().getRestrictions();
        this.connectionData.setSecuritySuite(restrictions.get(this.connectionData.getClientId()));
    }

    public ContextId getContextId() {
        return contextId;
    }

    public APdu processInitialMessage(byte[] messageData) throws IOException {
        SecuritySuite securitySuite = this.connectionData.getSecuritySuite();

        if (cosemLogicalDevice == null) {
            throw new AssociatRequestException(AcseServiceUser.NO_REASON_GIVEN);
        }

        Map<Integer, SecuritySuite> restrictions = logicalDevice.getRestrictions();
        APdu aPdu = APdu.decode(messageData, null);

        this.contextId = ObjectIdentifier
                .applicationContextIdFrom(aPdu.getAcseAPdu().getAarq().getApplicationContextName());
        if (restrictions.isEmpty()) {

            this.connectionData.setAuthenticated();
            this.connectionData.setSecuritySuite(SecuritySuite.builder().build());

            return new InitiateResponseBuilder(
                    ConformanceSettingConverter.conformanceFor(this.logicalDevice.getConformance()))
                            .setContextId(contextId)
                            .build();
        }

        if (securitySuite == null) {
            // unknown client ID
            throw new AssociatRequestException(AcseServiceUser.NO_REASON_GIVEN);
        }

        if (securitySuite.getEncryptionMechanism() != EncryptionMechanism.NONE) {
            this.connectionData.setClientSystemTitle(systemTitle());
            aPdu = APdu.decode(messageData, this.connectionData.getClientSystemTitle(), securitySuite, null);
        }

        if (aPdu.getCosemPdu() == null) {
            throw new AssociatRequestException(AcseServiceUser.NO_REASON_GIVEN);
        }

        COSEMpdu cosemPdu = aPdu.getCosemPdu();

        if (cosemPdu.getChoiceIndex() != COSEMpdu.Choices.INITIATEREQUEST) {
            throw new AssociatRequestException(AcseServiceUser.NO_REASON_GIVEN);
        }
        InitiateRequest initiateRequest = cosemPdu.initiateRequest;
        this.connectionData.setClientMaxReceivePduSize(initiateRequest.clientMaxReceivePduSize.getValue() & 0xFFFF);

        Conformance negotiatedConformance = negotiateConformance(initiateRequest);

        ACSEApdu acseAPdu = aPdu.getAcseAPdu();

        if (acseAPdu == null) {
            throw new AssociatRequestException(AcseServiceUser.NO_REASON_GIVEN);
        }
        AARQApdu aarq = acseAPdu.getAarq();
        return tryToAuthenticate(aarq, securitySuite, negotiatedConformance);
    }

    private Conformance negotiateConformance(InitiateRequest initiateRequest) {
        Set<ConformanceSetting> proposedConformance = ConformanceSettingConverter
                .conformanceSettingFor(initiateRequest.proposedConformance);
        Set<ConformanceSetting> negotiatedConformance = new HashSet<>(this.logicalDevice.getConformance());
        negotiatedConformance.retainAll(proposedConformance);

        return ConformanceSettingConverter.conformanceFor(negotiatedConformance);
    }

    private static void checkChallangeLength(int challengeLength) throws AssociatRequestException {
        if (challengeLength < 8 || challengeLength > 64) {
            throw new AssociatRequestException(AcseServiceUser.AUTHENTICATION_FAILURE);
        }
    }

    private APdu tryToAuthenticate(AARQApdu aarq, SecuritySuite securitySuite, Conformance negotiatedConformance)
            throws IOException {
        InitiateResponseBuilder responseBuilder = new InitiateResponseBuilder(negotiatedConformance);

        MechanismName mechanismName = aarq.getMechanismName();

        AuthenticationMechanism authLevel = AuthenticationMechanism.NONE;
        if (mechanismName != null) {
            authLevel = ObjectIdentifier.mechanismIdFrom(mechanismName);
        }

        if (authLevel != securitySuite.getAuthenticationMechanism()) {
            throw new AssociatRequestException(AcseServiceUser.AUTHENTICATION_FAILURE);
        }

        if (authLevel == NONE && securitySuite.getAuthenticationMechanism() == NONE) {
            this.connectionData.setAuthenticated();
            return responseBuilder.setContextId(contextId).build();
        }

        this.connectionData.setClientToServerChallenge(aarq.getCallingAuthenticationValue().getCharstring().value);

        switch (authLevel) {
        case LOW:
            return processLowAuthentciationRequest(responseBuilder, aarq, securitySuite.getPassword());

        case HLS5_GMAC:
            return processHls5GmacAuthentciationRequest(responseBuilder, aarq);
        default:
            throw new AssociatRequestException(AcseServiceUser.APPLICATION_CONTEXT_NAME_NOT_SUPPORTED);
        }
    }

    private APdu processHls5GmacAuthentciationRequest(InitiateResponseBuilder responseBuilder, AARQApdu aarq)
            throws IOException {
        byte[] clientToServerChallenge = this.connectionData.getClientToServerChallenge();

        this.connectionData.setClientSystemTitle(aarq.getCallingAPTitle().getApTitleForm2().value);

        int challengeLength = clientToServerChallenge.length;

        checkChallangeLength(challengeLength);

        byte[] serverToClientChallenge = generateNewChallenge(challengeLength);

        this.connectionData.setServerToClientChallenge(serverToClientChallenge);

        return responseBuilder.setContextId(contextId)
                .setAuthenticationValue(serverToClientChallenge)
                .setSystemTitle(systemTitle())
                .build();

    }

    private APdu processLowAuthentciationRequest(InitiateResponseBuilder responseBuilder, AARQApdu aarq,
            byte[] authenticationKey) throws AssociatRequestException {
        byte[] clientAuthenticaionValue = aarq.getCallingAuthenticationValue().getCharstring().value;

        if (Arrays.equals(clientAuthenticaionValue, authenticationKey)) {
            this.connectionData.setAuthenticated();
            return responseBuilder.build();
        }

        throw new AssociatRequestException(AcseServiceUser.AUTHENTICATION_FAILURE);
    }

    private byte[] systemTitle() {
        return this.logicalDevice.getSystemTitle();
    }

}
