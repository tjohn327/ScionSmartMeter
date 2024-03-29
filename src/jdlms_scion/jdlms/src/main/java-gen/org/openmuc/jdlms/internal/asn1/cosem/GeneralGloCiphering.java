/**
 * This class file was automatically generated by the AXDR compiler that is part of jDLMS (http://www.openmuc.org)
 */

package org.openmuc.jdlms.internal.asn1.cosem;

import java.io.IOException;
import java.io.InputStream;

import org.openmuc.jasn1.ber.ReverseByteArrayOutputStream;
import org.openmuc.jdlms.internal.asn1.axdr.AxdrType;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrOctetString;

public class GeneralGloCiphering implements AxdrType {

    public byte[] code = null;
    public AxdrOctetString systemTitle = null;

    public AxdrOctetString cipheredContent = null;

    public GeneralGloCiphering() {
    }

    public GeneralGloCiphering(byte[] code) {
        this.code = code;
    }

    public GeneralGloCiphering(AxdrOctetString systemTitle, AxdrOctetString cipheredContent) {
        this.systemTitle = systemTitle;
        this.cipheredContent = cipheredContent;
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
            codeLength += cipheredContent.encode(axdrOStream);

            codeLength += systemTitle.encode(axdrOStream);

        }

        return codeLength;

    }

    @Override
    public int decode(InputStream iStream) throws IOException {
        int codeLength = 0;

        systemTitle = new AxdrOctetString();
        codeLength += systemTitle.decode(iStream);

        cipheredContent = new AxdrOctetString();
        codeLength += cipheredContent.decode(iStream);

        return codeLength;
    }

    public void encodeAndSave(int encodingSizeGuess) throws IOException {
        ReverseByteArrayOutputStream axdrOStream = new ReverseByteArrayOutputStream(encodingSizeGuess);
        encode(axdrOStream);
        code = axdrOStream.getArray();
    }

    @Override
    public String toString() {
        return "sequence: {" + "systemTitle: " + systemTitle + ", cipheredContent: " + cipheredContent + "}";
    }

}
