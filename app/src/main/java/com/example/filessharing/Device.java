package com.example.filessharing;

import java.net.InetAddress;

class Device {
    private InetAddress hostAddress;
    private int hostPort;
    private String deviceName;

    Device(String deviceName, InetAddress hostAddress, int hostPort) {
        this.hostAddress = hostAddress;
        this.hostPort = hostPort;
        this.deviceName = deviceName;
    }
    InetAddress getHostAddress() {
        return hostAddress;
    }

    int getHostPort(){
        return hostPort;
    }

    String getDeviceName() {
        return deviceName;
    }
}
