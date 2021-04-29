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

import org.openmuc.jdlms.ScionConnectionBuilder;
import org.openmuc.jdlms.transportlayer.ScionDatagramPacket;
import org.openmuc.jdlms.transportlayer.ScionSocket;

import java.io.*;

/**
 * The transport layer used to communicate via Scion UDP.
 */
public class ScionLayer implements TransportLayer {

    public void setSettings(ScionConnectionBuilder.ScionSettingsImpl settings) {
        this.settings = settings;
    }

    private ScionConnectionBuilder.ScionSettingsImpl settings;
    private ScionSocket socket;
    private boolean closed;

    private InputStream inputStream;
    private OutputStream outputStream;

    public ScionLayer(ScionConnectionBuilder.ScionSettingsImpl settings) throws IOException {
        this.settings = settings;
        this.closed = true;
    }

    @Override
    public void open() throws IOException {
        this.socket = ScionSocket.getUniqueInstance(settings.getServerAddr(), settings.port());
        this.closed = false;
        this.inputStream = this.socket.getInputStream();
        this.outputStream = this.socket.getOutputStream();
    }

    @Override
    public void setTimeout(int timeout) throws IOException {
        this.socket.setSoTimeout(timeout);
    }

    @Override
    public DataInputStream getInputStream() throws IOException {
       return new DataInputStream(this.inputStream);
    }

    @Override
    public DataOutputStream getOutpuStream() throws IOException {
       return  new DataOutputStream(this.outputStream);
    }

    @Override
    public void close() throws IOException {
//        if(this.closed){
//            return;
//        }
        try {
            this.socket.resetConnection();
            this.inputStream.close();
            this.outputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            this.closed = true;
        }
    }

    @Override
    public boolean isClosed() {
        return this.closed;
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
//        ScionLayer.this.close();
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
        ScionDatagramPacket packet = new ScionDatagramPacket(data, socket.getClientAddr());
        socket.send(packet);
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
//        ScionLayer.this.close();
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
            synchronized (socket) {
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
        socket.receive(packet);

        if (socket.getClientAddr() == null) {
            socket.setClientAddr(packet.getAddress());
        } else if (!socket.getClientAddr().equals(packet.getAddress())) {
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


