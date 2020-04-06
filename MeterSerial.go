package main

import (
	"log"

	"github.com/tarm/serial"
)

func main1() {
	log.Println("Starting")
	c := &serial.Config{Name: "/dev/ttyAMA0", Baud: 9600, Size: 8, Parity: 'E', StopBits: 1}
	s, err := serial.OpenPort(c)
	if err != nil {
		log.Fatal(err)
	}
	log.Println("Connected")
	send := []byte{0x01, 0x03, 0x01, 0x30, 0x00, 0x01, 0x85, 0xF9}
	n, err := s.Write(send)
	if err != nil {
		log.Fatal(err)
	}
	log.Println(send)
	buf := make([]byte, 128)
	n, err = s.Read(buf)
	if err != nil {
		log.Fatal(err)
	}
	log.Print(n, buf)
}
