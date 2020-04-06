package main

import (
	"log"

	"github.com/ScionSmartMeter/meter"
)

func main() {
	meter.Init()
	freq, _ := meter.ReadFrequency()
	volt, _ := meter.ReadVoltage()
	log.Print(freq, volt)

}
