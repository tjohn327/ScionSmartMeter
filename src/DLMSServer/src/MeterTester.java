public class MeterTester{
    public static void main(String[] args) {
        MeterModbus meterModbus = new MeterModbus();
        try{
        meterModbus.init("/dev/ttyAMA0");
        meterModbus.read();
        meterModbus.getMeterData().print();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}