
import org.openmuc.jdlms.*;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.settings.client.ReferencingMethod;

import com.intelligt.modbus.jlibmodbus.serial.*;

import java.io.IOException;
import java.util.Random;

import static javax.xml.bind.DatatypeConverter.parseHexBinary;
import static org.openmuc.jdlms.sessionlayer.server.ServerSessionLayerFactories.newWrapperSessionLayerFactory;

public class SigSmartMeterServer {

    private final RegisterIC frequency;
    private final RegisterIC voltage;
    private final RegisterIC current;
    private final RegisterIC activePower;
    private final RegisterIC reactivePower;
    private final RegisterIC apparentPower;
    private final RegisterIC powerFactor;
    private final RegisterIC activeEnergy;
    private final RegisterIC reactiveEnergy;
    private final DisconnectControlIC disconnectControlIC;
    private final MessageIC messageIC;

    private final byte[] AUTHENTICATION_KEY;
    private final byte[] GLOBAL_ENCRYPTION_KEY;
    private final byte[] MASTER_KEY;

    private MeterModbus meterModbus;

    public SigSmartMeterServer() throws SerialPortException{
        AUTHENTICATION_KEY = parseHexBinary("5468697349734150617373776f726431");
        GLOBAL_ENCRYPTION_KEY = parseHexBinary("000102030405060708090a0b0c0d0e0f");
        MASTER_KEY = parseHexBinary("aa0102030405060738090a0b0c0d0eff");

        frequency = new RegisterIC("1.0.14",0,44);
        voltage = new RegisterIC("1.0.12",0,35);
        current = new RegisterIC("1.0.11",0,33);
        activePower = new RegisterIC("1.0.21",3,27);
        reactivePower = new RegisterIC("1.0.23",3,29);
        apparentPower = new RegisterIC("1.0.9",3,28);
        powerFactor = new RegisterIC("1.0.13",0,255);
        activeEnergy = new RegisterIC("1.0.1",3,30);
        reactiveEnergy = new RegisterIC("1.0.3",3,32);
        disconnectControlIC = new DisconnectControlIC("0.0.96.3.10.1");
        messageIC = new MessageIC("1.11.123.55.1.13");

        meterModbus = new MeterModbus();
        meterModbus.init("/dev/ttyAMA0");
    }

    public static void main(String[] args) throws IOException {
        
        DlmsServer.TcpServerBuilder serverBuilder = null;
        SigSmartMeterServer s = null;
        try{
            s = new SigSmartMeterServer();
            int port = 13555;
            ReferencingMethod refMethod = ReferencingMethod.LOGICAL;
            String manufacturerId = "SCI";
            long deviceId = 12;

            final SecuritySuite AUTHENTICATION_C = SecuritySuite.builder()
                    .setAuthenticationKey(s.AUTHENTICATION_KEY)
                    .setGlobalUnicastEncryptionKey(s.GLOBAL_ENCRYPTION_KEY)
                    .setAuthenticationMechanism(AuthenticationMechanism.HLS5_GMAC)
                    .setEncryptionMechanism(SecuritySuite.EncryptionMechanism.AES_GCM_128)
                    .build();

            LogicalDevice logicalDevice = new LogicalDevice(1, "sci", manufacturerId, deviceId);
            logicalDevice.setMasterKey(s.MASTER_KEY);
            logicalDevice.addRestriction(16, AUTHENTICATION_C);
            logicalDevice.registerCosemObject(s.activeEnergy,
                    s.activePower,
                    s.apparentPower,
                    s.current,
                    s.frequency,
                    s.powerFactor,
                    s.reactiveEnergy,
                    s.reactivePower,
                    s.voltage,
                    s.disconnectControlIC,
                    s.messageIC);


            serverBuilder = DlmsServer.tcpServerBuilder(port)
                    .setReferencingMethod(refMethod)
                    .setInactivityTimeout(5000)
                    .registerLogicalDevice(logicalDevice)
                    .setSessionLayerFactory(newWrapperSessionLayerFactory());
        } catch(Exception e){
            e.printStackTrace();
            return;
        }

        DlmsServer dlmsServer = null;
        try  {
            dlmsServer = serverBuilder.build();
            Thread.sleep(5000); //Time for server to start
            while (true) {
                try{
                    s.meterModbus.read();
                    s.activeEnergy.setValue(DataObject.newFloat32Data(s.meterModbus.getMeterData().getActiveEnergy()));
                    s.activePower.setValue(DataObject.newFloat32Data(s.meterModbus.getMeterData().getActivePower()));
                    s.apparentPower.setValue(DataObject.newFloat32Data(s.meterModbus.getMeterData().getApparentPower()));
                    s.current.setValue(DataObject.newFloat32Data(s.meterModbus.getMeterData().getCurrent()));
                    s.frequency.setValue(DataObject.newFloat32Data(s.meterModbus.getMeterData().getFrequency()));
                    s.powerFactor.setValue(DataObject.newFloat32Data(s.meterModbus.getMeterData().getPowerFactor()));
                    s.reactiveEnergy.setValue(DataObject.newFloat32Data(s.meterModbus.getMeterData().getReactiveEnergy()));
                    s.reactivePower.setValue(DataObject.newFloat32Data(s.meterModbus.getMeterData().getReactivePower()));
                    s.voltage.setValue(DataObject.newFloat32Data(s.meterModbus.getMeterData().getVoltage()));
                }catch(Exception e){
                    e.printStackTrace();
                }
                Thread.sleep(2000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            if(dlmsServer !=null)
                dlmsServer.shutdown();                
            s.disconnectControlIC.close();
            s.messageIC.close();
        }

    }

}
