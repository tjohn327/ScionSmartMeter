package org.openmuc.jdlms.transportlayer;


import go.javaappnet.Javaappnet;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;


public class ScionSocket extends Socket {

    public static final int MAX_SCION_PAYLOAD_SIZE = 2500;
    public static ScionSocket uniqueInstance = null;
    private InetAddress inetAddress; //placeholder
    private ScionIStream inputStream;
    private ScionOStream outputStream;
    private String clientAddr;
    private int timeout = 0;
    private boolean isServerInit;

    public static ScionSocket getUniqueInstance(String serverAddr, int port, boolean isServerInit) throws IOException {
        if (uniqueInstance == null) {
            uniqueInstance = new ScionSocket(serverAddr, port, isServerInit);
        } else {
            uniqueInstance.setAddr(serverAddr);
            uniqueInstance.setServerInit(isServerInit);
            uniqueInstance.setStreams();
            Javaappnet.Open();
        }
        return uniqueInstance;
    }

    public static ScionSocket getUniqueInstance(String serverAddr, int port) throws IOException {
        return getUniqueInstance(serverAddr, port, false);
    }

    public static ScionSocket getUniqueInstance(int port, boolean isServerInit) throws IOException {
        return getUniqueInstance(null, port, false);
    }

    public ScionSocket() {

    }

    public ScionSocket(int port) throws IOException {
        this(null, port, false);
    }

    public ScionSocket(int port, boolean isServerInit) throws IOException {
        this(null, port, isServerInit);
    }

    public ScionSocket(String serverAddr, int port) throws IOException {
        this(serverAddr, port, false);
    }

    public ScionSocket(String serverAddr, int port, boolean isServerInit) throws IOException {
        Thread.currentThread().setPriority(1);
        Javaappnet.SetDebug(true);
        if (Javaappnet.Init(port, MAX_SCION_PAYLOAD_SIZE) != 0) {
            throw new SocketException("ScionSocket: Error initializing");
        }
        this.clientAddr = serverAddr;
        Javaappnet.Open();
        this.setStreams();
        this.inetAddress = null;
        this.isServerInit = isServerInit;
    }

    public void setSoTimeout(int timeout) {
        this.timeout = timeout;
        Javaappnet.SetTimeout(timeout);
    }

    public String getClientAddr() {
        return clientAddr;
    }

    public void setClientAddr(String clientAddr) {
        this.clientAddr = clientAddr;
    }

    public void setStreams() {
        this.inputStream = new ScionIStream();
        this.outputStream = new ScionOStream();
    }

    public void setDebug(boolean debug) {
        Javaappnet.SetDebug(debug);
    }

    public void setServerInit(boolean serverInit) {
        isServerInit = serverInit;
    }

    public ScionIStream getInputStream() {
        return inputStream;
    }

    public ScionOStream getOutputStream() {
        return outputStream;
    }

    public void setAddr(String addr) {
        this.clientAddr = addr;
    }


    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public void setInetAddress(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }

    public void send(ScionDatagramPacket spack) throws IOException {
        if (Javaappnet.Send(spack.getAddress(), spack.getData()) != 0) {
            throw new SocketException("ScionSocket: Error writing");
        }
    }

    public void receive(ScionDatagramPacket spack) throws IOException {
        synchronized (spack) {
            if (isServerInit) {
                Javaappnet.SetTimeout(0);
                read(spack);
                isServerInit = false;
                Javaappnet.SetTimeout(timeout);
            } else {
                read(spack);
            }
        }
    }

    private void read(ScionDatagramPacket spack) throws SocketException {
        byte[] addr = new byte[64];
        long res = Javaappnet.Receive(spack.getData(), addr);
        if (res == -1) {
            throw new SocketException("ScionSocket: Closed");
        } else if (res == -2) {
            throw new SocketException("ScionSocket: Timeout");
        } else if (res == 0 || spack.getData() == null) {
            throw new SocketException("ScionSocket: Error reading");
        }
        spack.setAddress(new String(addr).trim());
        spack.setLength((int) res);
    }

    public void resetForServer() {
        this.clientAddr = null;
        this.inetAddress = null;
        this.isServerInit = true;
    }

    public void resetConnection() throws IOException {
        Javaappnet.Close();
        this.inputStream.closeStream();
        this.outputStream.closeStream();
        this.clientAddr = null;
    }

    public void close() throws IOException {
        Javaappnet.CloseConnection();
        this.inputStream.closeStream();
        this.outputStream.closeStream();
        this.clientAddr = null;
    }

    public void closeConnection() throws IOException {
        Javaappnet.CloseConnection();
        this.inputStream.closeStream();
        this.outputStream.closeStream();
        this.clientAddr = null;
    }

    private class ScionOStream extends OutputStream {

        private final ByteArrayOutputStream os;

        public ScionOStream() {
            this.os = new ByteArrayOutputStream(ScionSocket.MAX_SCION_PAYLOAD_SIZE);
        }

        public void closeStream() throws IOException {
            this.os.close();
        }

        @Override
        public void close() throws IOException {
//            ScionSocket.this.resetConnection();
        }

        @Override
        public void write(int b) throws IOException {
            this.os.write(b);

            if (this.os.size() == ScionSocket.MAX_SCION_PAYLOAD_SIZE) {
                flush();
            }
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {

            int remaining = len;
            while (true) {
                int newLen = Math.min(len, ScionSocket.MAX_SCION_PAYLOAD_SIZE - this.os.size());

                if (newLen != 0) {
                    this.os.write(b, off + (len - remaining), newLen);
                    remaining -= newLen;
                }

                if (remaining == 0) {
                    break;
                }

                flush();
            }

        }

        @Override
        public void write(byte[] b) throws IOException {
            write(b, 0, b.length);
        }

        @Override
        public synchronized void flush() throws IOException {
            if (this.os.size() == 0) {
                return;
            }
            byte[] data = this.os.toByteArray();
            this.os.reset();
            ScionDatagramPacket packet = new ScionDatagramPacket(data, ScionSocket.this.getClientAddr());

            ScionSocket.this.send(packet);
        }
    }

    private class ScionIStream extends InputStream {

        private InputStream is;
        private final Object lock;

        public ScionIStream() {
            this.is = new ByteArrayInputStream(new byte[0]);
            this.lock = new Object();
        }

        public void closeStream() throws IOException {
            this.is.close();
        }

        @Override
        public void close() throws IOException {
//            ScionSocket.this.resetConnection();
        }

        @Override
        public int read() throws IOException {
            readIfEmpty();
            synchronized (lock) {
                return is.read();
            }

        }

        private void readIfEmpty() throws IOException {
            synchronized (lock) {
                if (is.available() == 0) {
                    readNextPacket();
                }
            }
        }

        @Override
        public int available() throws IOException {
            readIfEmpty();

            return is.available();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            try {
                readIfEmpty();
            } catch (IOException e) {
                return 0;
            }

            int remaining = len;
            while (true) {
                synchronized (ScionSocket.this) {
                    remaining -= is.read(b, len - remaining, Math.min(is.available(), remaining));
                }

                if (remaining == 0) {
                    return len;
                }

                try {
                    readIfEmpty();
                } catch (IOException e) {
                    return len - remaining;
                }
            }
        }

        @Override
        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }

        private synchronized void readNextPacket() throws IOException {

            byte[] buf = new byte[ScionSocket.MAX_SCION_PAYLOAD_SIZE];
            ScionDatagramPacket packet = new ScionDatagramPacket(buf, buf.length);
            ScionSocket.this.receive(packet);

            if (ScionSocket.this.getClientAddr() == null) {
                ScionSocket.this.setClientAddr(packet.getAddress());
            } else if (!ScionSocket.this.getClientAddr().equals(packet.getAddress())) {
                // TODO check this again
                readNextPacket();
                return;
            }

            int len = packet.getLength();
            byte[] data = packet.getData();

            synchronized (lock) {
                this.is = new ByteArrayInputStream(data, 0, len);
            }
        }
    }

}
