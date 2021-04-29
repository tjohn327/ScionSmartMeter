import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CBus;
import net.fauxpark.oled.SSD1306;
import net.fauxpark.oled.*;
import net.fauxpark.oled.font.CodePage1252;
import net.fauxpark.oled.transport.I2CTransport;
import net.fauxpark.oled.transport.Transport;

public class OledDisplay {
    private Transport transport;
    private SSD1306 ssd1306;
    private Graphics graphics;
    private String serverAddr;
    private String message;
    public OledDisplay(){
        transport = new I2CTransport(RaspiPin.GPIO_20, I2CBus.BUS_1, 0x3C);
        ssd1306 = new SSD1306(128, 64, transport);
        graphics = ssd1306.getGraphics();


        // false indicates no external VCC
        ssd1306.startup(false);

        // Turns the pixel in the top left corner on
        ssd1306.setPixel(0, 0, true);
        // Inverts the display
        // ssd1306.setInverted(true);

        // Flips the display upside down
        // ssd1306.setVFlipped(true);
    }
    public void setServerAddr(String addr){
        serverAddr = addr;
        showText();
    }
    public void  setMessage(String message){
        this.message = message;
        showText();
    }

    private void showText(){
        ssd1306.clear();
        graphics.text(20, 1, new CodePage1252(), "SCION DLMS Meter");
        if(serverAddr != null) {
            String[] addr = serverAddr.split(",");
            graphics.text(0, 15, new CodePage1252(), addr[0]);
            graphics.text(0, 25, new CodePage1252(), addr[1]);
        }
        if(message != null){
            graphics.text(1, 35, new CodePage1252(), "Message form Client:");
            graphics.text(1, 45, new CodePage1252(), message);
        }
        ssd1306.display();
    }

    public void shutdown(){
        ssd1306.shutdown();
    }



}
