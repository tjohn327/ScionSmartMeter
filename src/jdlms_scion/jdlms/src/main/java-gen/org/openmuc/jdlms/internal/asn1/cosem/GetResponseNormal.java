/**
 * This class file was automatically generated by the AXDR compiler that is part of jDLMS (http://www.openmuc.org)
 */

package org.openmuc.jdlms.internal.asn1.cosem;

import java.io.IOException;
import java.io.InputStream;

import org.openmuc.jasn1.ber.ReverseByteArrayOutputStream;
import org.openmuc.jdlms.internal.asn1.axdr.AxdrType;

public class GetResponseNormal implements AxdrType {

    public byte[] code = null;
    public InvokeIdAndPriority invokeIdAndPriority = null;

    public GetDataResult result = null;

    public GetResponseNormal() {
    }

    public GetResponseNormal(byte[] code) {
        this.code = code;
    }

    public GetResponseNormal(InvokeIdAndPriority invokeIdAndPriority, GetDataResult result) {
        this.invokeIdAndPriority = invokeIdAndPriority;
        this.result = result;
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
            codeLength += result.encode(axdrOStream);

            codeLength += invokeIdAndPriority.encode(axdrOStream);

        }

        return codeLength;

    }

    @Override
    public int decode(InputStream iStream) throws IOException {
        int codeLength = 0;

        invokeIdAndPriority = new InvokeIdAndPriority();
        codeLength += invokeIdAndPriority.decode(iStream);

        result = new GetDataResult();
        codeLength += result.decode(iStream);

        return codeLength;
    }

    public void encodeAndSave(int encodingSizeGuess) throws IOException {
        ReverseByteArrayOutputStream axdrOStream = new ReverseByteArrayOutputStream(encodingSizeGuess);
        encode(axdrOStream);
        code = axdrOStream.getArray();
    }

    @Override
    public String toString() {
        return "sequence: {" + "invokeIdAndPriority: " + invokeIdAndPriority + ", result: " + result + "}";
    }

}