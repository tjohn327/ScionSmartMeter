/**
 * This class file was automatically generated by the AXDR compiler that is part of jDLMS (http://www.openmuc.org)
 */

package org.openmuc.jdlms.internal.asn1.cosem;

import java.io.IOException;
import java.io.InputStream;

import org.openmuc.jasn1.ber.ReverseByteArrayOutputStream;
import org.openmuc.jdlms.internal.asn1.axdr.AxdrType;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrOctetString;

public class AccessResponse implements AxdrType {

    public byte[] code = null;
    public Unsigned32 longInvokeIdAndPriority = null;

    public AxdrOctetString dateTime = null;

    public AccessResponseBody accessResponseBody = null;

    public AccessResponse() {
    }

    public AccessResponse(byte[] code) {
        this.code = code;
    }

    public AccessResponse(Unsigned32 longInvokeIdAndPriority, AxdrOctetString dateTime,
            AccessResponseBody accessResponseBody) {
        this.longInvokeIdAndPriority = longInvokeIdAndPriority;
        this.dateTime = dateTime;
        this.accessResponseBody = accessResponseBody;
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
            codeLength += accessResponseBody.encode(axdrOStream);

            codeLength += dateTime.encode(axdrOStream);

            codeLength += longInvokeIdAndPriority.encode(axdrOStream);

        }

        return codeLength;

    }

    @Override
    public int decode(InputStream iStream) throws IOException {
        int codeLength = 0;

        longInvokeIdAndPriority = new Unsigned32();
        codeLength += longInvokeIdAndPriority.decode(iStream);

        dateTime = new AxdrOctetString();
        codeLength += dateTime.decode(iStream);

        accessResponseBody = new AccessResponseBody();
        codeLength += accessResponseBody.decode(iStream);

        return codeLength;
    }

    public void encodeAndSave(int encodingSizeGuess) throws IOException {
        ReverseByteArrayOutputStream axdrOStream = new ReverseByteArrayOutputStream(encodingSizeGuess);
        encode(axdrOStream);
        code = axdrOStream.getArray();
    }

    @Override
    public String toString() {
        return "sequence: {" + "longInvokeIdAndPriority: " + longInvokeIdAndPriority + ", dateTime: " + dateTime
                + ", accessResponseBody: " + accessResponseBody + "}";
    }

}
