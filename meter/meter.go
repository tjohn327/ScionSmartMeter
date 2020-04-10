package meter

import (
	"encoding/binary"
	"fmt"
	"time"

	"github.com/goburrow/modbus"
)

const (
	frequncyRegister       = 0x0130
	voltageRegister        = 0x0131
	currentRegister        = 0x0139
	activePowerRegister    = 0x0140
	reactivePowerRegister  = 0x0148
	apparentPowerRegister  = 0x0150
	powerFactorRegister    = 0x0158
	activeEnergyRegister   = 0xA000
	reactiveEnergyRegister = 0xA01E
)

var (
	handler modbus.RTUClientHandler
	client  modbus.Client
)

type Reading struct {
	//voltage in V
	Voltage float32 `json:"Voltage_V"`
	//current in A
	Current float32 `json:"Current_A"`
	//frequency in Hz
	Frequency float32 `json:"Frequency_Hz"`
	//active power in kW
	ActivePower float32 `json:"Active Power_kW"`
	//reactice power in kvar
	ReactivePower float32 `json:"Reactive Power_kvar"`
	//apparent power in kVA
	ApparentPower float32 `json:"Apparent Power_kVA"`

	PowerFactor float32 `json:"Power Factor"`
	//total active energy in kWh
	ActiveEnergy float32 `json:"Active Energy_kWh"`
	//total reactive energy in kvarh
	ReactiveEnergy float32 `json:"Reactive Energy_kvarh"`
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

//ReadMeter reads the meter and return a Reading object
func ReadMeter() (m Reading, err error) {
	err = handler.Connect()
	if err == nil {
		defer handler.Close()
		result, err := client.ReadHoldingRegisters(frequncyRegister, 0x0001)
		if err == nil {
			m.Frequency = float32(binary.BigEndian.Uint16(result)) / 100.0
		}

		result, err = client.ReadHoldingRegisters(voltageRegister, 0x0001)
		if err == nil {
			m.Voltage = float32(binary.BigEndian.Uint16(result)) / 100
		}

		result, err = client.ReadHoldingRegisters(currentRegister, 0x0002)
		if err == nil {
			m.Current = float32(binary.BigEndian.Uint32(result)) / 1000
		}

		result, err = client.ReadHoldingRegisters(activePowerRegister, 0x0002)
		if err == nil {
			m.ActivePower = float32(binary.BigEndian.Uint32(result)) / 1000
		}

		result, err = client.ReadHoldingRegisters(reactivePowerRegister, 0x0002)
		if err == nil {
			m.ReactivePower = float32(binary.BigEndian.Uint32(result)) / 1000
		}

		result, err = client.ReadHoldingRegisters(apparentPowerRegister, 0x0002)
		if err == nil {
			m.ApparentPower = float32(binary.BigEndian.Uint32(result)) / 1000
		}

		result, err = client.ReadHoldingRegisters(powerFactorRegister, 0x0001)
		if err == nil {
			m.PowerFactor = float32(binary.BigEndian.Uint16(result)) / 1000
		}

		result, err = client.ReadHoldingRegisters(activeEnergyRegister, 0x000A)
		if err == nil {
			m.ActiveEnergy = float32(binary.BigEndian.Uint32(result[:4])) / 100
		}
		result, err = client.ReadHoldingRegisters(reactiveEnergyRegister, 0x000A)
		if err == nil {
			m.ReactiveEnergy = float32(binary.BigEndian.Uint32(result[:4])) / 100
		}
	}
	return
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

//Print pretty prints the meter reading
func (r Reading) Print() {
	fmt.Printf("Voltage \t: %.2f V\n", r.Voltage)
	fmt.Printf("Current \t: %.2f A\n", r.Current)
	fmt.Printf("Frequency \t: %.2f Hz\n", r.Frequency)
	fmt.Printf("Active Power \t: %.3f kW\n", r.ActivePower)
	fmt.Printf("Reactive Power \t: %.3f kvar\n", r.ReactivePower)
	fmt.Printf("Apparent Power \t: %.3f kVA\n", r.ApparentPower)
	fmt.Printf("Power Factor \t: %.3f\n", r.PowerFactor)
	fmt.Printf("Active Energy \t: %.2f kWh\n", r.ActiveEnergy)
	fmt.Printf("Reactive Energy\t: %.2f kvarh\n", r.ReactiveEnergy)
}
