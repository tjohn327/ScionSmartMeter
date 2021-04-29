/**
 * This class file was automatically generated by the AXDR compiler that is part of jDLMS (http://www.openmuc.org)
 */

package org.openmuc.jdlms.internal.asn1.cosem;

import java.io.IOException;
import java.io.InputStream;

import org.openmuc.jasn1.ber.ReverseByteArrayOutputStream;
import org.openmuc.jdlms.internal.asn1.axdr.AxdrType;

public class AccessRequestBody implements AxdrType {

    public byte[] code = null;
    public ListOfAccessRequestSpecification accessRequestSpecification = null;

    public ListOfData accessRequestListOfData = null;

    public AccessRequestBody() {
    }

    public AccessRequestBody(byte[] code) {
        this.code = code;
    }

    public AccessRequestBody(ListOfAccessRequestSpecification accessRequestSpecification,
            ListOfData accessRequestListOfData) {
        this.accessRequestSpecification = accessRequestSpecification;
        this.accessRequestListOfData = accessRequestListOfData;
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
            codeLength += accessRequestListOfData.encode(axdrOStream);

            codeLength += accessRequestSpecification.encode(axdrOStream);

        }

        return codeLength;

    }

    @Override
    public int decode(InputStream iStream) throws IOException {
        int codeLength = 0;

        accessRequestSpecification = new ListOfAccessRequestSpecification();
        codeLength += accessRequestSpecification.decode(iStream);

        accessRequestListOfData = new ListOfData();
        codeLength += accessRequestListOfData.decode(iStream);

        return codeLength;
    }

    public void encodeAndSave(int encodingSizeGuess) throws IOException {
        ReverseByteArrayOutputStream axdrOStream = new ReverseByteArrayOutputStream(encodingSizeGuess);
        encode(axdrOStream);
        code = axdrOStream.getArray();
    }

    @Override
    public String toString() {
        return "sequence: {" + "accessRequestSpecification: " + accessRequestSpecification
                + ", accessRequestListOfData: " + accessRequestListOfData + "}";
    }

}
