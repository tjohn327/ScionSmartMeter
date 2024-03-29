/**
 * This class file was automatically generated by the AXDR compiler that is part of jDLMS (http://www.openmuc.org)
 */

package org.openmuc.jdlms.internal.asn1.cosem;

import java.io.IOException;
import java.io.InputStream;

import org.openmuc.jasn1.ber.ReverseByteArrayOutputStream;
import org.openmuc.jdlms.internal.asn1.axdr.AxdrType;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrEnum;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrOctetString;

public class WrappedKey implements AxdrType {

    public byte[] code = null;
    public AxdrEnum kekId = null;

    public AxdrOctetString keyCipheredData = null;

    public WrappedKey() {
    }

    public WrappedKey(byte[] code) {
        this.code = code;
    }

    public WrappedKey(AxdrEnum kekId, AxdrOctetString keyCipheredData) {
        this.kekId = kekId;
        this.keyCipheredData = keyCipheredData;
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
            codeLength += keyCipheredData.encode(axdrOStream);

            codeLength += kekId.encode(axdrOStream);

        }

        return codeLength;

    }

    @Override
    public int decode(InputStream iStream) throws IOException {
        int codeLength = 0;

        kekId = new AxdrEnum();
        codeLength += kekId.decode(iStream);

        keyCipheredData = new AxdrOctetString();
        codeLength += keyCipheredData.decode(iStream);

        return codeLength;
    }

    public void encodeAndSave(int encodingSizeGuess) throws IOException {
        ReverseByteArrayOutputStream axdrOStream = new ReverseByteArrayOutputStream(encodingSizeGuess);
        encode(axdrOStream);
        code = axdrOStream.getArray();
    }

    @Override
    public String toString() {
        return "sequence: {" + "kekId: " + kekId + ", keyCipheredData: " + keyCipheredData + "}";
    }

}
