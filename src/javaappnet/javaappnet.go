package javaappnet

import "C"

import (
	"fmt"
	"github.com/netsec-ethz/scion-apps/pkg/appnet"
	"github.com/scionproto/scion/go/lib/snet"
	"github.com/tjohn327/scionsmartmeter/javadrkey"
	"net"
	"strings"
	"sync"
	"time"
)

var count int

var connection *snet.Conn
var connectionLock sync.Mutex
var isConnOpen = false

var rcvLength int
var rcvLengthChannel chan int

var addrClient string
var addrClientChannel chan string

var debug = false
var bufferSize = 1024
var timeout int

var client net.Addr
var clientChannel chan net.Addr
var clientLock sync.Mutex

var expireTime time.Time

var rcvChannel chan []byte
var resetChannel chan int

var canReceive bool
var receiveLock sync.Mutex

var canSend bool
var sendLock sync.Mutex

func SetDebug(value bool) {
	debug = value
}

//export SetBufferSize
func SetBufferSize(value int) {
	bufferSize = value
}

//export Init
func Init(port int, bufSize int) int {
	connectionLock.Lock()
	defer connectionLock.Unlock()
	var err error
	if isConnOpen {

		fmt.Println("Connection Already Open")
		CloseConnection()

		// return 0
	}
	bufferSize = bufSize
	rcvChannel = make(chan []byte, 4)
	rcvLengthChannel = make(chan int, 4)
	clientChannel = make(chan net.Addr, 4)
	addrClientChannel = make(chan string, 4)

	resetChannel = make(chan int)

	connection, err = appnet.ListenPort(uint16(port))
	if err != nil {
		fmt.Println(err)
		isConnOpen = false
		return 1
	}

	canReceive = true
	canSend = true
	expireTime = time.Now()
	go read(connection, rcvChannel)

	if debug {
		fmt.Println("Connection Started")
	}
	isConnOpen = true
	return 0
}

func Open() {
	receiveLock.Lock()
	canReceive = true
	receiveLock.Unlock()
	sendLock.Lock()
	canSend = true
	sendLock.Unlock()
}

//export SetTimeout
func SetTimeout(value int) {
	timeout = value
}

//export Send
func Send(scionAddr string, buf []byte) int {
	sendLock.Lock()
	defer sendLock.Unlock()
	if !canSend {
		fmt.Println("ScionSocket closed")
		return 1
	}

	if scionAddr == "" {
		return 1
	}

	if !checkBuffer(len(buf)) {
		fmt.Println("Buffer large")
		return 1
	}

	sendPacketBuffer := make([]byte, len(buf))
	copy(sendPacketBuffer, buf)

	clientLock.Lock()
	defer clientLock.Unlock()
	if client != nil {
		if strings.Compare(client.String(), scionAddr) == 0 {
			if timeout > 0 {
				deadline := time.Now().Add(time.Millisecond * time.Duration(timeout))
				connection.SetWriteDeadline(deadline)
			}

			_, err := connection.WriteTo(sendPacketBuffer, client)
			if err == nil {
				if debug {
					fmt.Printf("Send 'C' %s %d\n", client.String(), len(buf))
				}
				return 0
			}
		}
	}

	raddr, err := appnet.ResolveUDPAddr(scionAddr)
	if err != nil {
		fmt.Println("Address couldn't be resolved")
		fmt.Println(err)
		return 1
	}

	//if the remote is in the same AS
	if raddr.IA == appnet.DefNetwork().IA {

		if timeout > 0 {
			deadline := time.Now().Add(time.Millisecond * time.Duration(timeout))
			connection.SetWriteDeadline(deadline)
		}
		_, err = connection.WriteTo(sendPacketBuffer, raddr)
		if err != nil {
			fmt.Println(err)
			return 1
		}
		if debug {
			fmt.Printf("Send %s>%s %d\n", raddr.String(), raddr.NextHop.String(), len(buf))
		}
		return 0
	}

	paths, err := appnet.QueryPaths(raddr.IA)
	if err != nil {
		fmt.Println("Query path failed")
		fmt.Println(err)
		return 1
	}

	for i := 0; i < len(paths); i++ {
		appnet.SetPath(raddr, paths[i])

		if timeout > 0 {
			deadline := time.Now().Add(time.Millisecond * time.Duration(timeout))
			connection.SetWriteDeadline(deadline)
		}

		_, err = connection.WriteTo(sendPacketBuffer, raddr)
		if err != nil {
			continue
		} else {
			if debug {
				fmt.Printf("Send %s>%s %d\n", raddr.String(), raddr.NextHop.String(), len(buf))
			}
			return 0
		}
	}
	fmt.Println(err)
	return 1
}

func read(conn *snet.Conn, rcvChannel chan []byte) {
	for {
		receiveBuffer := make([]byte, bufferSize)
		n, addr, err := conn.ReadFrom(receiveBuffer)
		if err != nil {
			if strings.Contains(err.Error(), "use of closed network connection") {
				return
			}
			fmt.Println(err)
			continue
		}

		rcvChannel <- receiveBuffer
		clientChannel <- addr
		addrClientChannel <- addr.String()
		rcvLengthChannel <- n

		if debug {
			fmt.Printf("Receive R %s %d\n", addr.String(), n)
		}
	}
}

//export Receive
func Receive(buffer []byte, addr []byte) int {
	receiveLock.Lock()
	defer receiveLock.Unlock()
	if !canReceive {
		fmt.Println("ScionSocket closed")
		return -1
	}

	if !checkBuffer(len(buffer)) {
		return 0
	}
	receivePacketBuffer := make([]byte, bufferSize)

	if timeout == 0 {
		select {
		case receivePacketBuffer = <-rcvChannel:
			if receivePacketBuffer == nil {
				<-clientChannel
				<-addrClientChannel
				<-rcvLengthChannel
				return 0
			}

			clientLock.Lock()
			client = <-clientChannel
			timeDiff := time.Now().Sub(expireTime)
			if timeDiff > time.Duration(time.Minute*15) {
				client = nil
				expireTime = time.Now()
			}
			clientLock.Unlock()
			addrClient = <-addrClientChannel
			rcvLength = <-rcvLengthChannel
			copy(addr, addrClient)
			copy(buffer, receivePacketBuffer)
			if debug {
				fmt.Printf("Receive 0 %s %d\n", addrClient, len(receivePacketBuffer))
			}
			return rcvLength

		case <-resetChannel:
			return -1
		}
	}
	select {
	case receivePacketBuffer := <-rcvChannel:
		if receivePacketBuffer == nil {
			<-clientChannel
			<-addrClientChannel
			<-rcvLengthChannel
			return 0
		}
		clientLock.Lock()
		client = <-clientChannel

		timeDiff := time.Now().Sub(expireTime)
		if timeDiff > time.Duration(time.Minute*15) {
			client = nil
			expireTime = time.Now()
		}
		clientLock.Unlock()
		addrClient = <-addrClientChannel
		rcvLength = <-rcvLengthChannel
		copy(addr, addrClient)
		copy(buffer, receivePacketBuffer)
		if debug {
			fmt.Printf("Receive %s %d\n", addrClient, rcvLength)
		}
		return rcvLength

	case <-resetChannel:
		return -1
	case <-time.After(time.Duration(timeout) * time.Millisecond):
		clientLock.Lock()
		client = nil
		clientLock.Unlock()
		// if debug {
		fmt.Println("Receive timeout")
		// }
		return -2
	}
}

//export IsClosed
func IsClosed() bool {
	return !isConnOpen
}

//export Close
func Close() {
	sendLock.Lock()
	canSend = false
	sendLock.Unlock()
	select {
	case resetChannel <- 1:
	case <-time.After(time.Duration(250 * time.Millisecond)):
		// default:
	}
	receiveLock.Lock()
	canReceive = false
	receiveLock.Unlock()

	if debug {
		fmt.Println("Connection Reset")
	}
}

//export CloseConnection
func CloseConnection() {
	connectionLock.Lock()
	defer connectionLock.Unlock()
	if isConnOpen {
		Close()
		connection.Close()
		isConnOpen = false
		if debug {
			fmt.Println("Connection Closed")
		}
	}
}

func checkBuffer(length int) bool {
	if length > bufferSize {
		if debug {
			fmt.Printf("Buffer Size higher than %d\n", bufferSize)
		}
		return false
	}
	return true
}

//export GetDRKey
func GetDRKey(serverAddr, clientAddr string) string {
	return javadrkey.GetDRKey(serverAddr, clientAddr)
}

func GetLocalAddress(port int) string {
	connectionLock.Lock()
	defer connectionLock.Unlock()
	if connection != nil {
		return appnet.DefNetwork().IA.String() + "," + connection.LocalAddr().String()
	}
	connection, err := appnet.ListenPort(uint16(port))
	defer connection.Close()
	if err != nil {
		fmt.Println(err)
		return ""
	}
	return appnet.DefNetwork().IA.String() + "," + connection.LocalAddr().String()
}

func main() {}
