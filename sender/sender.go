package main

import (
	"encoding/json"
	"flag"
	"time"
	"log"
	"os"
	"github.com/ScionSmartMeter/meter"
	"github.com/netsec-ethz/scion-apps/pkg/appnet"
)

func check(e error) {
	if e != nil {
		log.Fatal(e)
	}
}

func main() {
	meter.Init()
	var m meter.Reading


	serverAddrStr := flag.String("s", "", "Server address (<ISD-AS,[IP]:port> or <hostname:port>)")
	flag.Parse()

	if len(*serverAddrStr) == 0 {
		flag.Usage()
		os.Exit(2)
	}

	conn, err := appnet.Dial(*serverAddrStr)
	check(err)
	for {

		m, _ = meter.ReadMeter()

		sendPacketBuffer,_ := json.Marshal(m)

		_, err = conn.Write(sendPacketBuffer)
		check(err)
		time.Sleep(5*time.Second)
	}
}
