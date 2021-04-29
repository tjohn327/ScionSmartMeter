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
package org.openmuc.jdlms.transportlayer.client;

import java.io.IOException;

import org.openmuc.jdlms.transportlayer.StreamAccessor;

/**
 * Class handing the physical communication to a smart meter.
 * 
 * @see Iec21Layer
 * @see TcpLayer
 * @see UdpLayer
 */
public interface TransportLayer extends StreamAccessor {
    /**
     * Opens the physical layer.
     * 
     * @throws IOException
     *             if an error occurred opening the stream.
     */
    void open() throws IOException;

    /**
     * Status of the connection/transport layer.
     * 
     * @return <code>true</code> if the connection is closes <code>false</code> otherwhise.
     */
    boolean isClosed();
}
