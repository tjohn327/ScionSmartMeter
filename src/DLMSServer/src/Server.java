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
import org.openmuc.jdlms.DlmsServer;
import org.openmuc.jdlms.LogicalDevice;
import org.openmuc.jdlms.RawMessageData;
import org.openmuc.jdlms.RawMessageListener;
import org.openmuc.jdlms.settings.client.ReferencingMethod;

import java.io.IOException;

import static org.openmuc.jdlms.sessionlayer.server.ServerSessionLayerFactories.newWrapperSessionLayerFactory;

public class Server {

    public static void main(String[] args) throws IOException {

        int port = 4888;
        ReferencingMethod refMethod = ReferencingMethod.SHORT;
        String manufacturerId = "ISE";
        long deviceId = 12;
        LogicalDevice logicalDevice = new LogicalDevice(1, "ise", manufacturerId, deviceId);
        logicalDevice.registerCosemObject(new SnSampleClass());

        DlmsServer.ScionServerBuilder serverBuilder = DlmsServer.scionServerBuilder(port)
                .setReferencingMethod(refMethod)
                .registerLogicalDevice(logicalDevice)
                .setSessionLayerFactory(newWrapperSessionLayerFactory());
        serverBuilder.setInactivityTimeout(2000);

        DlmsServer dlmsServer = null;
        try  {
            dlmsServer = serverBuilder.build();
            while (true) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            if(dlmsServer !=null)
            dlmsServer.shutdown();
        }

    }

}
