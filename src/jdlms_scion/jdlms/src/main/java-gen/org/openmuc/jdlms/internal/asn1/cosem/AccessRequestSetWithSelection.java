/**
 * This class file was automatically generated by the AXDR compiler that is part of jDLMS (http://www.openmuc.org)
 */

package org.openmuc.jdlms.internal.asn1.cosem;

import java.io.IOException;
import java.io.InputStream;

import org.openmuc.jasn1.ber.ReverseByteArrayOutputStream;
import org.openmuc.jdlms.internal.asn1.axdr.AxdrType;

public class AccessRequestSetWithSelection implements AxdrType {

    public byte[] code = null;
    public CosemAttributeDescriptor cosemAttributeDescriptor = null;

    public SelectiveAccessDescriptor accessSelection = null;

    public AccessRequestSetWithSelection() {
    }

    public AccessRequestSetWithSelection(byte[] code) {
        this.code = code;
    }

    public AccessRequestSetWithSelection(CosemAttributeDescriptor cosemAttributeDescriptor,
            SelectiveAccessDescriptor accessSelection) {
        this.cosemAttributeDescriptor = cosemAttributeDescriptor;
        this.accessSelection = accessSelection;
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
            codeLength += accessSelection.encode(axdrOStream);

            codeLength += cosemAttributeDescriptor.encode(axdrOStream);

        }

        return codeLength;

    }

    @Override
    public int decode(InputStream iStream) throws IOException {
        int codeLength = 0;

        cosemAttributeDescriptor = new CosemAttributeDescriptor();
        codeLength += cosemAttributeDescriptor.decode(iStream);

        accessSelection = new SelectiveAccessDescriptor();
        codeLength += accessSelection.decode(iStream);

        return codeLength;
    }

    public void encodeAndSave(int encodingSizeGuess) throws IOException {
        ReverseByteArrayOutputStream axdrOStream = new ReverseByteArrayOutputStream(encodingSizeGuess);
        encode(axdrOStream);
        code = axdrOStream.getArray();
    }

    @Override
    public String toString() {
        return "sequence: {" + "cosemAttributeDescriptor: " + cosemAttributeDescriptor + ", accessSelection: "
                + accessSelection + "}";
    }

}
