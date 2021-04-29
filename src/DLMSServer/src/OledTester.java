public class OledTester{
    public static void main(String[] args) {
        OledDisplay oledDisplay = new OledDisplay();
        oledDisplay.setServerAddr("19-ffaa:1:bf9,192.168.10.114:13555");
        oledDisplay.shutdown();
    }



}