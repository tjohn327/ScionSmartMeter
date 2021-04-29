package org.openmuc.jdlms.transportlayer;


public final class ScionDatagramPacket {
    byte[] buf;
    int offset;
    int length;
    int bufLength;
    String address;
    int port;

    public ScionDatagramPacket() {
    }

    public ScionDatagramPacket(byte[] var1, int var2, int var3) {
        this.setData(var1, var2, var3);
        this.address = null;
        this.port = -1;
    }
    public ScionDatagramPacket(byte[] data) {
        this.setData(data);
        this.setAddress(null);
    }

    public ScionDatagramPacket(byte[] data, String clientAddr) {
        this.setData(data);
        this.setAddress(clientAddr);
    }

    public ScionDatagramPacket(byte[] data, int length) {
        this.setData(data);
    }

    public ScionDatagramPacket(byte[] var1, int var2, int var3, String var4, int var5) {
        this.setData(var1, var2, var3);
        this.setAddress(var4);
        this.setPort(var5);
    }


    public ScionDatagramPacket(byte[] var1, int var2, String var3, int var4) {
        this(var1, 0, var2, var3, var4);
    }


    public synchronized String getAddress() {
        return this.address;
    }

    public synchronized int getPort() {
        return this.port;
    }

    public synchronized byte[] getData() {
        return this.buf;
    }

    public synchronized int getOffset() {
        return this.offset;
    }

    public synchronized int getLength() {
        return this.length;
    }

    public synchronized void setData(byte[] var1, int var2, int var3) {
        if (var3 >= 0 && var2 >= 0 && var3 + var2 >= 0 && var3 + var2 <= var1.length) {
            this.buf = var1;
            this.length = var3;
            this.bufLength = var3;
            this.offset = var2;
        } else {
            throw new IllegalArgumentException("illegal length or offset");
        }
    }

    public synchronized void setAddress(String var1) {
        this.address = var1;
    }

    public synchronized void setPort(int var1) {
        if (var1 >= 0 && var1 <= 65535) {
            this.port = var1;
        } else {
            throw new IllegalArgumentException("Port out of range:" + var1);
        }
    }


    public synchronized void setData(byte[] var1) {
        if (var1 == null) {
            throw new NullPointerException("null packet buffer");
        } else {
            this.buf = var1;
            this.offset = 0;
            this.length = var1.length;
            this.bufLength = var1.length;
        }
    }

    public synchronized void setLength(int var1) {
        if (var1 + this.offset <= this.buf.length && var1 >= 0 && var1 + this.offset >= 0) {
            this.length = var1;
            this.bufLength = this.length;
        } else {
            throw new IllegalArgumentException("illegal length");
        }
    }


}
