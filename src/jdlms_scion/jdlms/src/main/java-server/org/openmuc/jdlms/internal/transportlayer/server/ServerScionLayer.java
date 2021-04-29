package org.openmuc.jdlms.internal.transportlayer.server;

import org.openmuc.jdlms.DataDirectory;
import org.openmuc.jdlms.internal.association.Association;
import org.openmuc.jdlms.sessionlayer.server.ServerSessionLayer;
import org.openmuc.jdlms.sessionlayer.server.ServerSessionLayerFactory;
import org.openmuc.jdlms.settings.server.ScionServerSettings;
import org.openmuc.jdlms.transportlayer.ScionSocket;
import org.openmuc.jdlms.transportlayer.StreamAccessor;
import org.openmuc.jdlms.transportlayer.server.ServerTransportLayer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ServerScionLayer implements ServerTransportLayer {

    private SocketListener socketListener;
    private ExecutorService serverExec;
    private final ScionServerSettings settings;
    private final DataDirectory dataDirectory;
    private final ServerSessionLayerFactory sessionLayerFactory;

    public ServerScionLayer(ScionServerSettings settings, DataDirectory dataDirectory,
                            ServerSessionLayerFactory sessionLayerFactory) {
        this.settings = settings;
        this.dataDirectory = dataDirectory;
        this.sessionLayerFactory = sessionLayerFactory;
    }

    @Override
    public void close() throws IOException {
        this.socketListener.close();
        this.serverExec.shutdown();
    }

    @Override
    public void start() throws IOException {
        this.socketListener = new SocketListener();
        serverExec = Executors.newSingleThreadExecutor();
        serverExec.submit(socketListener);
    }

    public static class ScionServerConnectionInformation extends ServerConnectionInformationImpl {

        private final InetAddress clienInetAddress;

        public ScionServerConnectionInformation(InetAddress clienInetAddress) {
            this.clienInetAddress = clienInetAddress;
        }

        @Override
        public InetAddress getClientInetAddress() {
            return this.clienInetAddress;
        }
    }

    private class SocketListener implements Runnable, AutoCloseable {

        private ScionSocket serverSocket;
        private ScionSocketAccessor scionSocketAccessor;
        private long connections;
        private boolean run;

        public SocketListener() throws IOException {
            this.connections = 0L;
            this.run = true;
        }

        @Override
        public void run() {
            try {
                saveRun();
            } catch (IOException | InterruptedException e) {
                // ignore here, connection will be closed
                e.printStackTrace();
            }
        }

        private void saveRun() throws IOException, InterruptedException {
            this.serverSocket = new ScionSocket(settings.getScionPort(), true);
            scionSocketAccessor = new ScionSocketAccessor(serverSocket);
            while (this.run) {
                acceptCon();
            }
        }

        private void acceptCon() throws IOException, InterruptedException {

            this.serverSocket.resetForServer();

            ServerSessionLayer sessionLayer = sessionLayerFactory.newSesssionLayer(scionSocketAccessor,
                    settings);

            Long connectionId = ++connections;

            Association association = new Association(dataDirectory, sessionLayer, connectionId, settings,
                    new ScionServerConnectionInformation(serverSocket.getInetAddress()));
            try{
                association.run();
            }
            catch (Exception e){
//                e.printStackTrace();
            }
            finally {
                serverSocket.resetForServer();
            }
        }

        @Override
        public void close() throws IOException {
            this.run = false;
            this.serverSocket.closeConnection();
        }
    }

    private class ScionSocketAccessor implements StreamAccessor {

        private final ScionSocket socket;
        private final DataOutputStream os;
        private final DataInputStream is;

        public ScionSocketAccessor(ScionSocket socket) throws IOException {
            this.socket = socket;

            this.is = new DataInputStream(socket.getInputStream());
            this.os = new DataOutputStream(socket.getOutputStream());

        }

        @Override
        public void setTimeout(int timeout) throws IOException {
            this.socket.setSoTimeout(timeout);
        }

        @Override
        public DataInputStream getInputStream() throws IOException {
            return this.is;
        }

        @Override
        public DataOutputStream getOutpuStream() throws IOException {
            return this.os;
        }

        @Override
        public void close() throws IOException {
//            this.socket.close();
            this.is.close();
            this.os.close();
        }
    }
}
