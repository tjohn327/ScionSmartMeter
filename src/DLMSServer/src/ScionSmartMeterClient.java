import org.openmuc.jdlms.*;
import org.openmuc.jdlms.datatypes.DataObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static javax.xml.bind.DatatypeConverter.parseHexBinary;
import static org.openmuc.jdlms.datatypes.DataObject.newEnumerateData;
import static org.openmuc.jdlms.datatypes.DataObject.newOctetStringData;

public class ScionSmartMeterClient {

    private final AttributeAddress frequencyAddr;
    private final AttributeAddress voltageAddr;
    private final AttributeAddress currentAddr;
    private final AttributeAddress activePowerAddr;
    private final AttributeAddress reactivePowerAddr;
    private final AttributeAddress apparentPowerAddr;
    private final AttributeAddress powerFactorAddr;
    private final AttributeAddress activeEnergyAddr;
    private final AttributeAddress reactiveEnergyAddr;
    private final List<AttributeAddress> params;
    private final AttributeAddress disconnectControlAddr;


    private final byte[] AUTHENTICATION_KEY;
    private final byte[] GLOBAL_ENCRYPTION_KEY;
    private final SecuritySuite AUTHENTICATION_C;
    private ScionConnectionBuilder connectionBuilder;
    private MeterData meterData;


    public ScionSmartMeterClient(String serverAddress, int port, String authenticationKey, String globalEncryptionKey) {
        frequencyAddr =        new AttributeAddress(3,"1.0.14",2);
        voltageAddr =          new AttributeAddress(3,"1.0.12",2);
        currentAddr =          new AttributeAddress(3,"1.0.11",2);
        activePowerAddr =      new AttributeAddress(3,"1.0.21",2);
        reactivePowerAddr =    new AttributeAddress(3,"1.0.23",2);
        apparentPowerAddr =    new AttributeAddress(3,"1.0.9",2);
        powerFactorAddr =      new AttributeAddress(3,"1.0.13",2);
        activeEnergyAddr =     new AttributeAddress(3,"1.0.1",2);
        reactiveEnergyAddr =   new AttributeAddress(3,"1.0.3",2);
        params = Arrays.asList(frequencyAddr,voltageAddr,currentAddr,activePowerAddr,
                reactivePowerAddr,apparentPowerAddr,powerFactorAddr,activeEnergyAddr,reactiveEnergyAddr);

        disconnectControlAddr = new AttributeAddress(70,"0.0.96.3.10.1",2);

        meterData = new MeterData();
        AUTHENTICATION_KEY = parseHexBinary(authenticationKey);
        GLOBAL_ENCRYPTION_KEY = parseHexBinary(globalEncryptionKey);
        AUTHENTICATION_C = SecuritySuite.builder()
                .setAuthenticationKey(AUTHENTICATION_KEY)
                .setGlobalUnicastEncryptionKey(GLOBAL_ENCRYPTION_KEY)
                .setAuthenticationMechanism(AuthenticationMechanism.HLS5_GMAC)
                .setEncryptionMechanism(SecuritySuite.EncryptionMechanism.AES_GCM_128)
                .build();
        try {
            connectionBuilder = new ScionConnectionBuilder(serverAddress).setPort(port)
                    .setSecuritySuite(AUTHENTICATION_C);
        } catch (Exception e) {
            throw e;
        }
    }

    public void readMeter() throws IOException {
        DlmsConnection connection = null;
        try {
            connection = connectionBuilder.build();
            List<GetResult> results = connection.get(params);
            for(int i =0;i<results.size();i++){
                GetResult result = results.get(i);
                if (result.getResultCode() == AccessResultCode.SUCCESS) {
                    DataObject resultData = result.getResultData();
                    setMeterData(i,Float.parseFloat(resultData.getValue().toString()));
                }
                else{
                    setMeterData(i,null);
                }

            }

        }
        catch (Exception e){
            throw e;
        }
        finally {
            connection.disconnect();
        }
    }

    public void disconnectMeter() throws IOException, InterruptedException {
        if(readMeterConnectionStatus()){
            Thread.sleep(1000);
            setMeterDisconnection(false);
        }
    }

    public void connectMeter() throws IOException, InterruptedException {
        if(!readMeterConnectionStatus()){
            Thread.sleep(1000);
            setMeterDisconnection(true);
        }
    }

    private boolean readMeterConnectionStatus() throws IOException {
        DlmsConnection connection = null;
        try {
            connection = connectionBuilder.build();
            GetResult result = connection.get(disconnectControlAddr);
            if (result.getResultCode() == AccessResultCode.SUCCESS) {
                DataObject resultData = result.getResultData();
                if (resultData.getValue().toString().equals("1")){
                    return true;
                }
                else {
                    return false;
                }
            }
            else{
                throw new IOException("Error reading connection status");
            }

        }
        catch (Exception e){
            throw e;
        }
        finally {
            connection.disconnect();
        }
    }

    private void setMeterDisconnection(boolean connect) throws IOException {
        DlmsConnection connection = null;
        try {
            connection = connectionBuilder.build();
            DataObject data;
            if(connect)
                data = newEnumerateData(1);
            else
                data = newEnumerateData(0);

            SetParameter setParameter = new SetParameter(disconnectControlAddr, data);
            AccessResultCode setResult = connection.set(setParameter);
            if (setResult == AccessResultCode.SUCCESS) {
                return;
            }
            else{
                System.out.println(setResult);
                throw new IOException("Error reading connection status");
            }

        }
        catch (Exception e){
            throw e;
        }
        finally {
            connection.disconnect();
        }

    }

    private void setMeterData(int i, Float value){
        switch (i){
            case 0: meterData.setFrequency(value);
            break;
            case 1: meterData.setVoltage(value);
                break;
            case 2: meterData.setCurrent(value);
                break;
            case 3: meterData.setActivePower(value);
                break;
            case 4: meterData.setReactivePower(value);
                break;
            case 5: meterData.setApparentPower(value);
                break;
            case 6: meterData.setPowerFactor(value);
                break;
            case 7: meterData.setActiveEnergy(value);
                break;
            case 8: meterData.setReactiveEnergy(value);
                break;
            default:
                break;
        }

    }

    public void printData(){
        meterData.print();
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        ScionSmartMeterClient testClient = new ScionSmartMeterClient("19-ffaa:1:c25,127.0.0.1:13555",13666,
                "5468697349734150617373776f726431","000102030405060708090a0b0c0d0e0f");
        testClient.readMeter();;
        testClient.printData();
        testClient.disconnectMeter();
    }
}
