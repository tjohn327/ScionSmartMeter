import org.openmuc.jdlms.CosemAttribute;
import org.openmuc.jdlms.CosemClass;
import org.openmuc.jdlms.CosemInterfaceObject;
import org.openmuc.jdlms.datatypes.DataObject;


@CosemClass(id = 3, version = 0)
public class RegisterIC extends CosemInterfaceObject {

    @CosemAttribute(id = 2, type = DataObject.Type.FLOAT32)
    private DataObject value;

    @CosemAttribute(id = 3, type = DataObject.Type.STRUCTURE)
    private final DataObject scaler_unit;

    private Object lock;

    public RegisterIC(String instanceId, int scale, int unit) {
        super(instanceId);
        value = DataObject.newFloat32Data(0);
        scaler_unit = DataObject.newStructureData(DataObject.newInteger32Data(scale),DataObject.newEnumerateData(unit));
        lock = new Object();
    }

    public DataObject getValue() {
        synchronized(lock){
        return value;
        }
    }

    public void setValue(DataObject value) {
        synchronized(lock){
        this.value = value;
        }
    }

    public DataObject getScaler_unit() {
        return scaler_unit;
    }
}
