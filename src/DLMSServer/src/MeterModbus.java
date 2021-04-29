import com.intelligt.modbus.jlibmodbus.Modbus;
import com.intelligt.modbus.jlibmodbus.master.ModbusMaster;
import com.intelligt.modbus.jlibmodbus.master.ModbusMasterFactory;
import com.intelligt.modbus.jlibmodbus.exception.ModbusIOException;
import com.intelligt.modbus.jlibmodbus.serial.SerialParameters;
import com.intelligt.modbus.jlibmodbus.serial.SerialPort;
import com.intelligt.modbus.jlibmodbus.serial.*;

public class MeterModbus {
    private final int frequencyRegister = 0x0130;
    private final int voltageRegister = 0x0131;
    private final int currentRegister = 0x0139;
    private final int activePowerRegister = 0x0140;
    private final int reactivePowerRegister = 0x0148;
    private final int apparentPowerRegister = 0x0150;
    private final int powerFactorRegister = 0x0158;
    private final int activeEnergyRegister = 0xA000;
    private final int reactiveEnergyRegister = 0xA01E;
    private final int meterSlaveId = 0x0001;

    private MeterData meterData;

    private SerialParameters sp;
    private ModbusMaster m;

    public MeterModbus() {
        sp = new SerialParameters();
        meterData = new MeterData();
        // Modbus.setLogLevel(Modbus.LogLevel.LEVEL_DEBUG);
    }

    public void init(String device) throws SerialPortException {
        sp.setDevice(device);
        sp.setBaudRate(SerialPort.BaudRate.BAUD_RATE_9600);
        sp.setDataBits(8);
        sp.setParity(SerialPort.Parity.EVEN);
        sp.setStopBits(1);
        SerialUtils.setSerialPortFactory(new SerialPortFactoryPJC());
        m = ModbusMasterFactory.createModbusMasterRTU(sp);
    }

    public void read() {
        if (sp == null || m == null) {
            return;
        }
        try {
            m.connect();

            int[] registerValues = m.readHoldingRegisters(meterSlaveId, frequencyRegister, 1);
            meterData.setFrequency (((float) registerValues[0]) / 100);

            registerValues = m.readHoldingRegisters(meterSlaveId, voltageRegister, 0x0001);
            meterData.setVoltage(((float) registerValues[0]) / 100); 

            registerValues = m.readHoldingRegisters(meterSlaveId, currentRegister, 0x0002);
            meterData.setCurrent((float) (registerValues[1] | (registerValues[0] << 16)) / 1000);

            registerValues = m.readHoldingRegisters(meterSlaveId, activePowerRegister, 0x0002);
            meterData.setActivePower((float) (registerValues[1] | (registerValues[0] << 16)) / 1000);

            registerValues = m.readHoldingRegisters(meterSlaveId, reactivePowerRegister, 0x0002);
            meterData.setReactiveEnergy( (float) (registerValues[1] | (registerValues[0] << 16)) / 1000);

            registerValues = m.readHoldingRegisters(meterSlaveId, apparentPowerRegister, 0x0002);
            meterData.setApparentPower((float) (registerValues[1] | (registerValues[0] << 16)) / 1000);

            registerValues = m.readHoldingRegisters(meterSlaveId, powerFactorRegister, 0x0001);
            meterData.setPowerFactor(((float) registerValues[0]) / 1000);

            registerValues = m.readHoldingRegisters(meterSlaveId, activeEnergyRegister, 0x0003);
            meterData.setActiveEnergy((float) (registerValues[1] | (registerValues[0] << 16) | (registerValues[2] << 32)) / 100);

            registerValues = m.readHoldingRegisters(meterSlaveId, reactiveEnergyRegister, 0x0003);
            meterData.setReactiveEnergy((float) (registerValues[1] | (registerValues[0] << 16) | (registerValues[2] << 32)) / 100);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                m.disconnect();
            } catch (ModbusIOException e1) {
                e1.printStackTrace();
            }
        }

    }   

    public int getMeterSlaveId() {
        return meterSlaveId;
    }

    public MeterData getMeterData(){
        return meterData;
    }
}
