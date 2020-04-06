# ScionSmartMeter

##Install GO on the raspberry pi

```bash
cd /tmp
wget https://dl.google.com/go/go1.13.4.linux-armv6l.tar.gz

sudo tar -xvf go1.13.4.linux-armv6l.tar.gz
sudo mv go /usr/local
echo 'export GOROOT=/usr/local/go' >> ~/.profile
echo 'export GOPATH=$HOME/go' >> ~/.profile
echo 'export PATH=$GOPATH/bin:$GOROOT/bin:$PATH' >> ~/.profile
source ~/.profile
go version
```




