
import org.openmuc.jdlms.CosemAttribute;
import org.openmuc.jdlms.CosemClass;
import org.openmuc.jdlms.CosemInterfaceObject;
import org.openmuc.jdlms.datatypes.DataObject;
import java.nio.charset.StandardCharsets;

@CosemClass(id = 1, version = 0)
public class MessageIC extends CosemInterfaceObject {

    @CosemAttribute(id = 2, type = DataObject.Type.OCTET_STRING)
    private DataObject value;
    private OledDisplay oledDisplay;

    public MessageIC(String instanceId) {
        super(instanceId);
        value = DataObject.newOctetStringData("".getBytes());
        oledDisplay = new OledDisplay();
        oledDisplay.setServerAddr("19-ffaa:1:bf9,192.168.10.114:13555");
    }
    public void setValue(DataObject value) {
        this.value = value;
        byte[] bytes = value.getValue();
        String s = new String(bytes, StandardCharsets.UTF_8);
        oledDisplay.setMessage(s);
    }
    public void close(){
        oledDisplay.shutdown();
    }
}
