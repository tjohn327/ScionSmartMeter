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
package org.openmuc.jdlms;

import org.openmuc.jdlms.sessionlayer.client.HdlcLayer;
import org.openmuc.jdlms.sessionlayer.client.SessionLayer;
import org.openmuc.jdlms.sessionlayer.client.WrapperLayer;
import org.openmuc.jdlms.sessionlayer.hdlc.HdlcAddress;
import org.openmuc.jdlms.sessionlayer.hdlc.HdlcAddressPair;
import org.openmuc.jdlms.settings.client.HdlcSettings;
import org.openmuc.jdlms.settings.client.HdlcTcpSettings;
import org.openmuc.jdlms.settings.client.Settings;
import org.openmuc.jdlms.transportlayer.client.ScionLayer;
import org.openmuc.jdlms.transportlayer.client.TransportLayer;

import java.io.IOException;
import java.net.InetAddress;

import static org.openmuc.jdlms.internal.Constants.DEFAULT_DLMS_PORT;

/**
 * Builder class to establish a DLMS connection via Scion UDP protocol suite.
 */
public class ScionConnectionBuilder extends ConnectionBuilder<ScionConnectionBuilder> {

    private String serverAddr;
    private int port;
    private InetSessionLayerType sessionLayerType;
    private TcpConnectionBuilder.InetTransportProtocol tranportProtocol;


    public ScionConnectionBuilder(String serverAddr) {
        this.serverAddr = serverAddr;
        this.port = DEFAULT_DLMS_PORT;
        this.sessionLayerType = InetSessionLayerType.WRAPPER;
        this.tranportProtocol = TcpConnectionBuilder.InetTransportProtocol.SCION;
    }


    /**
     * build Set the Internet address of the remote meter.
     *
     * @param serverAddr the Internet address.
     * @return the builder.
     */
    public ScionConnectionBuilder setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
        return this;
    }


    /**
     * Set the port of the remote meter.
     *
     * @param port the port.
     * @return the builder.
     */
    public ScionConnectionBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * Use the HDLC protocol.
     *
     * @return the builder.
     */
    public ScionConnectionBuilder useHdlc() {
        this.sessionLayerType = InetSessionLayerType.HDLC;
        return this;
    }

    /**
     * Use the Wrapper protocol. This is the defualt.
     *
     * @return the builder.
     */
    public ScionConnectionBuilder useWrapper() {
        this.sessionLayerType = InetSessionLayerType.WRAPPER;
        return this;
    }

    @Override
    protected Settings buildSettings() {
        return new ScionSettingsImpl(this);
    }

    @Override
    protected SessionLayer buildSessionLayer(Settings settings) throws IOException {
        switch (sessionLayerType) {
            case HDLC:
                return new HdlcLayer((HdlcSettings) settings);

            case WRAPPER:
            default:
                TransportLayer tl = createNewTl((ScionSettingsImpl) settings);

                return new WrapperLayer(settings, tl);
        }
    }

    private TransportLayer createNewTl(ScionSettingsImpl settings) throws IOException {
        return  new ScionLayer(settings);
    }

    public class ScionSettingsImpl extends SettingsImpl implements HdlcTcpSettings {

        public String getServerAddr() {
            return serverAddr;
        }

        private final String serverAddr;
        private final int port;
        private final HdlcAddressPair addressPair;
        private final TcpConnectionBuilder.InetTransportProtocol tranportProtocol;

        public ScionSettingsImpl(ScionConnectionBuilder connectionBuilder) {
            super(connectionBuilder);
            this.serverAddr = connectionBuilder.serverAddr;
            this.port = connectionBuilder.port;
            this.tranportProtocol = connectionBuilder.tranportProtocol;

            HdlcAddress source = new HdlcAddress(clientId());
            HdlcAddress destination = new HdlcAddress(logicalDeviceId(), physicalDeviceId());
            this.addressPair = new HdlcAddressPair(source, destination);
        }

        public String serverAddr() {
            return this.serverAddr;
        }

        @Override
        public TcpConnectionBuilder.InetTransportProtocol tranportProtocol() {
            return null;
        }

        @Override
        public InetAddress inetAddress() {
            return null;
        }

        @Override
        public int port() {
            return this.port;
        }

        @Override
        public HdlcAddressPair addressPair() {
            return this.addressPair;
        }

    }

    private enum InetSessionLayerType {
        HDLC,
        WRAPPER
    }


}
