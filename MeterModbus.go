package main

import (
	"time"

	"github.com/ScionSmartMeter/meter"
)

func main() {
	meter.Init()
	var m meter.Reading
	for {
		m, _ = meter.ReadMeter()
		m.Print()
		time.Sleep(time.Second)
		break
	}
}
