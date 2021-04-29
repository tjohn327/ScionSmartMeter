# A Secure Smart Metering Platform based on SCION

The SCION smart meter platform has three components, the SCION Smart Meter (RPi Zero (RPi 3B+) and the Head End System (Ubuntu Server).

## Preparing the devices

To set up all the three devices, the ubuntu server, the Raspberry Pi 3B+ data and the Raspberry Pi Zero, first the necessary tools should be installed. Follow the following steps to install those tools.

Install go 1.14 on all three devices.

```shell
sudo add-apt-repository ppa:gophers/archive
sudo apt update
sudo apt install golang-1.14-go
```

Clone and install scion-apps and scion repositories and dependencies.

```shell
cd ~
mkdir -p go/src/github.com/scionproto
cd go/src/github.com/scionproto
git clone https://github.com/netsec-ethz/scion.git
cd scion
./env/deps
cd ../..
git clone https://github.com/netsec-ethz/scion-apps.git
```

Clone the gojava repo on the Ubuntu server and RPi Zero.

```shell
mkdir sridharv && cd sridharv
git clone https://github.com/sridharv/gojava.git
cd gojava
go build .
```

Install SCION ASes on the Ubuntu server and the RPi 3B+ following the [tutorial](https://docs.scionlab.org/content/install/pkg.html)

Install and configure the SCION endhost on the RPi Zero following the [tutorial](https://docs.scionlab.org/content/config/setup_endhost.html)

Use the following IP as NODE_IP.

```shell
export NODE_IP=192.168.10.1
```

## Setting up the Data Concentrator

The RPi acts as a DHCP server and router for meter to connect to. The eth0 interface should be connected to the Powerline ethernet adapter. Connection to the internet can be done through WLAN or by using a USB ethernet adapter.

Setup the eth0 to use the static IP 192.168.10.1.



```shell
sudo nano /etc/dhcpcd.conf
```

Go to the end of the file and ass the following:

```shell
interface eth0
    static ip_address=192.168.10.1/24
```

Install dnsmasq

```shell
sudo apt install dnsmasq
sudo DEBIAN_FRONTEND=noninteractive apt install -y netfilter-persistent iptables-persistent
```

Add and save routing rules:

```shell
sudo iptables -t nat -A POSTROUTING -o eth1 -j MASQUERADE
sudo netfilter-persistent save
```

Configure DHCP:

Rename the default configuration file and edit a new one:

```shell
sudo mv /etc/dnsmasq.conf /etc/dnsmasq.conf.orig
sudo nano /etc/dnsmasq.conf
```

Add the following to the file and save it:

```shell
interface=eth0 
dhcp-range=192.168.10.2,192.168.10.50,255.255.255.0,24h
```

Reboot the RPi and it is ready.

## Setting up the Head End System

For development of the [SCION jDLMS library](src/jdlms_scion/jdlms/) and [DLMS server application](src/DLMSServer/) Intellij Idea IDE was used. The library uses gradle build tools for bulding it.

Clone this repository:

```shell
cd ~
git clone
mkdir -p go/src/github.com/tjohn327/scionsmartmeter
cp -rf scion-iot/scionsmartmeter/src/ go/src/github.com/tjohn327/scionsmartmeter/
```

Install Java 8

```shell
sudo apt update
sudo apt install openjdk-8-jdk
```

Install node-red

```shell
curl -sL https://deb.nodesource.com/setup_12.x | sudo -E bash -
sudo apt-get install -y nodejs
bash <(curl -sL https://raw.githubusercontent.com/node-red/linux-installers/master/deb/update-nodejs-and-nodered)
sudo systemctl enable nodered.service
```

Open Node-red, import the [flow](src/Node_red_dashboard/flow.json) and deploy it. 

GUI can accessed at http://localhost:1880/ui

## Setting up the SCION Smart Meter

Clone this repository:

```shell
cd ~
git clone
mkdir -p go/src/github.com/tjohn327/scionsmartmeter
cp -rf scion-iot/scionsmartmeter/src/ go/src/github.com/tjohn327/scionsmartmeter/
```

Install pi4j and wiring pi:

```shell
sudo apt-get install wiringpi
curl -sSL https://pi4j.com/install | sudo bash
```

To run the DLMS Server:

```shell
cd ~/go/src/github.com/tjohn327/scionsmartmeter/DLMSServer/build/
sudo java -cp .:../libs/*:/opt/pi4j/lib/*  ScionSmartMeterServer
```
