package meter

import (
	"encoding/binary"
	"time"

	"github.com/goburrow/modbus"
)

const (
	frequncyRegister      = 0x0130
	voltageRegister       = 0x0131
	currentRegister       = 0x0139
	activePowerRegister   = 0x0140
	reactivePowerRegister = 0x0148
	apparentPowerRegister = 0x0150
)

var (
	handler modbus.RTUClientHandler
	client  modbus.Client
)

type meterReading struct {
	//voltage in V
	voltage float32
	//current in A
	current float32
	//frequency in Hz
	frequency float32
	//active power in kW
	activePower float32
	//reactice power in kvar
	reactivePower float32
	//apparent power in kVA
	apparentPower float32

	powerFactor float32
	//total active energy in kWh
	activeEnergy float64
	//total reactive energy in kvarh
	reactiveEnergy float64
}

//Init initializes the meter reader
func Init() {

	handler = *modbus.NewRTUClientHandler("/dev/ttyAMA0")
	handler.BaudRate = 9600
	handler.DataBits = 8
	handler.Parity = "E"
	handler.StopBits = 1
	handler.SlaveId = 1
	handler.Timeout = 2 * time.Second

	client = modbus.NewClient(&handler)
}

//ReadFrequency reads the current frequency from meter
func ReadFrequency() (frequency float32, err error) {
	result, err := readRegisters(frequncyRegister, 0x0001)
	if err == nil {
		frequency = float32(binary.BigEndian.Uint16(result) / 100)
	}
	return
}

//ReadVoltage reads the current voltage from the meter
func ReadVoltage() (voltage float32, err error) {
	result, err := readRegisters(voltageRegister, 0x0001)
	if err == nil {
		voltage = float32(binary.BigEndian.Uint16(result) / 100)
	}
	return
}

func readRegisters(register uint16, qty uint16) (result []byte, err error) {
	err = handler.Connect()
	if err == nil {
		defer handler.Close()
		result, err = client.ReadHoldingRegisters(register, qty)
	}
	return
}
