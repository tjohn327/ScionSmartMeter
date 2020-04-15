package main

import (
	"encoding/json"
	"flag"
	"fmt"
	"log"
	"time"

	"github.com/ScionSmartMeter/meter"
	"github.com/netsec-ethz/scion-apps/pkg/appnet"
)

func check(e error) {
	if e != nil {
		log.Fatal(e)
	}
}

func main() {
	var m meter.Reading

	port := flag.Uint("p", 40002, "Server Port")
	flag.Parse()

	conn, err := appnet.ListenPort(uint16(*port))
	check(err)

	receivePacketBuffer := make([]byte, 2500)
	for {
		n, clientAddress, err := conn.ReadFrom(receivePacketBuffer)
		check(err)
		json.Unmarshal(receivePacketBuffer[:n], &m)
		fmt.Printf("\nData from: %s \n", clientAddress)
		fmt.Println(time.Now())
		m.Print()
		fmt.Printf("\n-----------------------------------\n")
	}
}
