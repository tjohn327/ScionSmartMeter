
import go.javaappnet.Javaappnet;
import org.openmuc.jdlms.*;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.transportlayer.ScionSocket;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
    private final AttributeAddress messageAddr;

    private byte[] AUTHENTICATION_KEY;
    private byte[] GLOBAL_ENCRYPTION_KEY;
    private SecuritySuite AUTHENTICATION_C;
    private ScionConnectionBuilder connectionBuilder=null;
    private DlmsConnection conn = null;

    private MeterData meterData;
    private Object mutex;


    public ScionSmartMeterClient() {
        frequencyAddr = new AttributeAddress(3, "1.0.14", 2);
        voltageAddr = new AttributeAddress(3, "1.0.12", 2);
        currentAddr = new AttributeAddress(3, "1.0.11", 2);
        activePowerAddr = new AttributeAddress(3, "1.0.21", 2);
        reactivePowerAddr = new AttributeAddress(3, "1.0.23", 2);
        apparentPowerAddr = new AttributeAddress(3, "1.0.9", 2);
        powerFactorAddr = new AttributeAddress(3, "1.0.13", 2);
        activeEnergyAddr = new AttributeAddress(3, "1.0.1", 2);
        reactiveEnergyAddr = new AttributeAddress(3, "1.0.3", 2);
        params = Arrays.asList(frequencyAddr, voltageAddr, currentAddr, activePowerAddr,
                reactivePowerAddr, apparentPowerAddr, powerFactorAddr, activeEnergyAddr, reactiveEnergyAddr);

        disconnectControlAddr = new AttributeAddress(70, "0.0.96.3.10.1", 2);
        messageAddr = new AttributeAddress(1, "1.11.123.55.1.13", 2);

        meterData = new MeterData();
        mutex = new Object();

    }

    public synchronized void init(String serverAddress, int port, String authenticationKey, String globalEncryptionKey)
            throws Exception {

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
                    .setSecuritySuite(AUTHENTICATION_C).setResponseTimeout(2200);
        } catch (Exception e) {
            throw e;
        }
    }

    public synchronized void init(String serverAddress, int port, String globalEncryptionKey) throws Exception {
        String clientAddr = Javaappnet.GetLocalAddress(port);
        String authKey = Javaappnet.GetDRKey(serverAddress,clientAddr);
        init(serverAddress,port,authKey,globalEncryptionKey);
    }

    public MeterData getMeterData() {
        return meterData;
    }

    public void close(){
        if(conn != null){
            try {
                conn.disconnect();
                conn = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public String[] initConnect() throws IOException, InterruptedException {
        DlmsConnection connection = null;
        try {
            connection = connectionBuilder.build();
            String[] status= new String[2];
            if(readMeterConnectionStatus(connection))
                status[0]= "Connected";
            else
                status[0]= "Disconnected";
            status[1]= readMessage(connection);
            connection.disconnect();
            return status;
        } catch (Exception e) {
            if(connection != null){
                connection.close();
            }
            throw e;
        } finally {

        }
    }

    public boolean connect(){
        try {
            conn = connectionBuilder.build();
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public synchronized void readMeter() throws IOException, InterruptedException {
        DlmsConnection connection = null;
        try {
            connection = connectionBuilder.build();
            List<GetResult> results = connection.get(params);
            for (int i = 0; i < results.size(); i++) {
                GetResult result = results.get(i);
                if (result.getResultCode() == AccessResultCode.SUCCESS) {
                    DataObject resultData = result.getResultData();
                    setMeterData(i, Float.parseFloat(resultData.getValue().toString()));
                } else {
                    setMeterData(i, null);
                }

            }
            connection.disconnect();

        }catch (EOFException e){
            e.printStackTrace();
            System.exit(1);
        }
        catch (Exception e) {
            if(connection != null){
                connection.close();
            }
            throw e;
        }
    }

    public void readConnectedMeter() throws IOException {
        try {
            List<GetResult> results = conn.get(params);
            for (int i = 0; i < results.size(); i++) {
                GetResult result = results.get(i);
                if (result.getResultCode() == AccessResultCode.SUCCESS) {
                    DataObject resultData = result.getResultData();
                    setMeterData(i, Float.parseFloat(resultData.getValue().toString()));
                } else {
                    setMeterData(i, null);
                }

            }

        } catch (Exception e) {
            throw e;
        }
    }

    public synchronized void setMeterDisconnect(boolean disconnect) throws IOException {
        try {
            DlmsConnection connection = connectionBuilder.build();
            if (disconnect) {
                if (readMeterConnectionStatus(connection)) {
                    setMeterDisconnectIC(connection, false);
                }
            } else {
                if (!readMeterConnectionStatus(connection)) {
                    setMeterDisconnectIC(connection, true);
                }
            }
            connection.disconnect();

        } catch (Exception e) {
            throw e;
        }

    }

    public synchronized void setMessage(String message) throws IOException {
        try {
            DlmsConnection connection = connectionBuilder.build();
            DataObject data = newOctetStringData(message.getBytes());
            SetParameter setParameter = new SetParameter(messageAddr, data);
            AccessResultCode setResult = connection.set(setParameter);
            connection.disconnect();
            if (setResult == AccessResultCode.SUCCESS) {
                return;
            } else {
                System.out.println(setResult);
                throw new IOException("Error reading connection status");
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private boolean readMeterConnectionStatus(DlmsConnection connection) throws IOException {
        GetResult result = connection.get(disconnectControlAddr);
        if (result.getResultCode() == AccessResultCode.SUCCESS) {
            DataObject resultData = result.getResultData();
            if (resultData.getValue().toString().equals("1")) {
                return true;
            } else {
                return false;
            }
        } else {
            throw new IOException("Error reading connection status");
        }
    }

    private String readMessage(DlmsConnection connection) throws IOException {
        GetResult result = connection.get(messageAddr);
        if (result.getResultCode() == AccessResultCode.SUCCESS) {
            DataObject resultData = result.getResultData();
            String message = new String((byte[]) resultData.getValue(), StandardCharsets.UTF_8);
            return message;
        } else {
            throw new IOException("Error reading connection status");
        }
    }

    private void setMeterDisconnectIC(DlmsConnection connection, boolean connect) throws IOException {
        DataObject data;
        if (connect)
            data = newEnumerateData(1);
        else
            data = newEnumerateData(0);

        SetParameter setParameter = new SetParameter(disconnectControlAddr, data);
        AccessResultCode setResult = connection.set(setParameter);
        if (setResult == AccessResultCode.SUCCESS) {
            return;
        } else {
            System.out.println(setResult);
            throw new IOException("Error reading connection status");
        }
    }

    private void setMeterData(int i, Float value) {
        switch (i) {
            case 0:
                meterData.setFrequency(value);
                break;
            case 1:
                meterData.setVoltage(value);
                break;
            case 2:
                meterData.setCurrent(value);
                break;
            case 3:
                meterData.setActivePower(value);
                break;
            case 4:
                meterData.setReactivePower(value);
                break;
            case 5:
                meterData.setApparentPower(value);
                break;
            case 6:
                meterData.setPowerFactor(value);
                break;
            case 7:
                meterData.setActiveEnergy(value);
                break;
            case 8:
                meterData.setReactiveEnergy(value);
                break;
            default:
                break;
        }

    }

    public void printData() {
        meterData.print();
    }

}
