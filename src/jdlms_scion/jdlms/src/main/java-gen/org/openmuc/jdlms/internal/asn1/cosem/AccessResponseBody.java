/**
 * This class file was automatically generated by the AXDR compiler that is part of jDLMS (http://www.openmuc.org)
 */

package org.openmuc.jdlms.internal.asn1.cosem;

import java.io.IOException;
import java.io.InputStream;

import org.openmuc.jasn1.ber.ReverseByteArrayOutputStream;
import org.openmuc.jdlms.internal.asn1.axdr.AxdrType;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrOptional;

public class AccessResponseBody implements AxdrType {

    public byte[] code = null;
    public AxdrOptional<ListOfAccessRequestSpecification> accessRequestSpecification = new AxdrOptional<>(
            new ListOfAccessRequestSpecification(), false);

    public ListOfData accessResponseListOfData = null;

    public ListOfAccessResponseSpecification accessResponseSpecification = null;

    public AccessResponseBody() {
    }

    public AccessResponseBody(byte[] code) {
        this.code = code;
    }

    public AccessResponseBody(ListOfAccessRequestSpecification accessRequestSpecification,
            ListOfData accessResponseListOfData, ListOfAccessResponseSpecification accessResponseSpecification) {
        this.accessRequestSpecification.setValue(accessRequestSpecification);
        this.accessResponseListOfData = accessResponseListOfData;
        this.accessResponseSpecification = accessResponseSpecification;
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
            codeLength += accessResponseSpecification.encode(axdrOStream);

            codeLength += accessResponseListOfData.encode(axdrOStream);

            codeLength += accessRequestSpecification.encode(axdrOStream);

        }

        return codeLength;

    }

    @Override
    public int decode(InputStream iStream) throws IOException {
        int codeLength = 0;

        accessRequestSpecification = new AxdrOptional<>(new ListOfAccessRequestSpecification(), false);
        codeLength += accessRequestSpecification.decode(iStream);

        accessResponseListOfData = new ListOfData();
        codeLength += accessResponseListOfData.decode(iStream);

        accessResponseSpecification = new ListOfAccessResponseSpecification();
        codeLength += accessResponseSpecification.decode(iStream);

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
                + ", accessResponseListOfData: " + accessResponseListOfData + ", accessResponseSpecification: "
                + accessResponseSpecification + "}";
    }

}