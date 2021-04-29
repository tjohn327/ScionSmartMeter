/**
 * This class file was automatically generated by the AXDR compiler that is part of jDLMS (http://www.openmuc.org)
 */

package org.openmuc.jdlms.internal.asn1.cosem;

import java.io.IOException;
import java.io.InputStream;

import org.openmuc.jasn1.ber.ReverseByteArrayOutputStream;
import org.openmuc.jdlms.internal.asn1.axdr.AxdrType;

public class CosemAttributeDescriptor implements AxdrType {

    public byte[] code = null;
    public Unsigned16 classId = null;

    public CosemObjectInstanceId instanceId = null;

    public Integer8 attributeId = null;

    public CosemAttributeDescriptor() {
    }

    public CosemAttributeDescriptor(byte[] code) {
        this.code = code;
    }

    public CosemAttributeDescriptor(Unsigned16 classId, CosemObjectInstanceId instanceId, Integer8 attributeId) {
        this.classId = classId;
        this.instanceId = instanceId;
        this.attributeId = attributeId;
    }

    @Override
    public int encode(ReverseByteArrayOutputStream axdrOStream) throws IOException {

        int codeLength;

        if (code != null) {
            codeLength = code.length;
            for (int i = code.length - 1; i >= 0; i--) {
                axdrOStream.write(code[i]);
            }
        }
        else {
            codeLength = 0;
            codeLength += attributeId.encode(axdrOStream);

            codeLength += instanceId.encode(axdrOStream);

            codeLength += classId.encode(axdrOStream);

        }

        return codeLength;

    }

    @Override
    public int decode(InputStream iStream) throws IOException {
        int codeLength = 0;

        classId = new Unsigned16();
        codeLength += classId.decode(iStream);

        instanceId = new CosemObjectInstanceId();
        codeLength += instanceId.decode(iStream);

        attributeId = new Integer8();
        codeLength += attributeId.decode(iStream);

        return codeLength;
    }

    public void encodeAndSave(int encodingSizeGuess) throws IOException {
        ReverseByteArrayOutputStream axdrOStream = new ReverseByteArrayOutputStream(encodingSizeGuess);
        encode(axdrOStream);
        code = axdrOStream.getArray();
    }

    @Override
    public String toString() {
        return "sequence: {" + "classId: " + classId + ", instanceId: " + instanceId + ", attributeId: " + attributeId
                + "}";
    }

}
