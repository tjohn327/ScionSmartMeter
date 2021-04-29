import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class ConnectionController implements  AutoCloseable{

    private final GpioController gpio;
    private final GpioPinDigitalOutput pin;

    public ConnectionController(){
        gpio = GpioFactory.getInstance();
        pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_25, "Connection", PinState.HIGH);
        pin.setShutdownOptions(true, PinState.LOW);
    }

    public void connnect(){
        pin.high();
    }

    public void disconnect(){
        pin.low();
    }

    public void close(){
        gpio.shutdown();
    }
}