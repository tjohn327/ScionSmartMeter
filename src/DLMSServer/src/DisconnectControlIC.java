import org.openmuc.jdlms.CosemAttribute;
import org.openmuc.jdlms.CosemClass;
import org.openmuc.jdlms.CosemInterfaceObject;
import org.openmuc.jdlms.datatypes.DataObject;

@CosemClass(id = 70, version = 0)
public class DisconnectControlIC extends CosemInterfaceObject {
    @CosemAttribute(id = 2, type = DataObject.Type.ENUMERATE)
    private DataObject value;

    private ConnectionController connectionController;

    public DisconnectControlIC(String instanceId) {
        super(instanceId);
        value = DataObject.newEnumerateData(1);
        connectionController = new ConnectionController();
        connectionController.connnect();
    }

    public DataObject getValue() {
        return value;
    }

    public void setValue(DataObject value) {
        this.value = value;
        if(value.getValue().toString().equals("1")){
            connectionController.connnect();
        }
        else if(value.getValue().toString().equals("0")){
            connectionController.disconnect();
        }
    } 
    
    public void close(){
        connectionController.close();
    }
}
