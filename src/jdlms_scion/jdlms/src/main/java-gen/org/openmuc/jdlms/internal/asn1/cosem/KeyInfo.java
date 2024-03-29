/**
 * This class file was automatically generated by the AXDR compiler that is part of jDLMS (http://www.openmuc.org)
 */

package org.openmuc.jdlms.internal.asn1.cosem;

import java.io.IOException;
import java.io.InputStream;

import org.openmuc.jasn1.ber.ReverseByteArrayOutputStream;
import org.openmuc.jdlms.internal.asn1.axdr.AxdrType;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrEnum;

public class KeyInfo implements AxdrType {

    public byte[] code = null;

    public static enum Choices {
        _ERR_NONE_SELECTED(-1),
        IDENTIFIED_KEY(0),
        WRAPPED_KEY(1),
        AGREED_KEY(2),;

        private int value;

        private Choices(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

        public static Choices valueOf(long tagValue) {
            Choices[] values = Choices.values();

            for (Choices c : values) {
                if (c.value == tagValue) {
                    return c;
                }
            }
            return _ERR_NONE_SELECTED;
        }
    }

    private Choices choice;

    public IdentifiedKey identifiedKey = null;

    public WrappedKey wrappedKey = null;

    public AgreedKey agreedKey = null;

    public KeyInfo() {
    }

    public KeyInfo(byte[] code) {
        this.code = code;
    }

    @Override
    public int encode(ReverseByteArrayOutputStream axdrOStream) throws IOException {
        if (code != null) {
            for (int i = code.length - 1; i >= 0; i--) {
                axdrOStream.write(code[i]);
            }
            return code.length;

        }
        if (choice == Choices._ERR_NONE_SELECTED) {
            throw new IOException("Error encoding AxdrChoice: No item in choice was selected.");
        }

        int codeLength = 0;

        if (choice == Choices.AGREED_KEY) {
            codeLength += agreedKey.encode(axdrOStream);
            AxdrEnum c = new AxdrEnum(2);
            codeLength += c.encode(axdrOStream);
            return codeLength;
        }

        if (choice == Choices.WRAPPED_KEY) {
            codeLength += wrappedKey.encode(axdrOStream);
            AxdrEnum c = new AxdrEnum(1);
            codeLength += c.encode(axdrOStream);
            return codeLength;
        }

        if (choice == Choices.IDENTIFIED_KEY) {
            codeLength += identifiedKey.encode(axdrOStream);
            AxdrEnum c = new AxdrEnum(0);
            codeLength += c.encode(axdrOStream);
            return codeLength;
        }

        // This block should be unreachable
        throw new IOException("Error encoding AxdrChoice: No item in choice was encoded.");
    }

    @Override
    public int decode(InputStream iStream) throws IOException {
        int codeLength = 0;
        AxdrEnum choosen = new AxdrEnum();

        codeLength += choosen.decode(iStream);
        resetChoices();
        this.choice = Choices.valueOf(choosen.getValue());

        if (choice == Choices.IDENTIFIED_KEY) {
            identifiedKey = new IdentifiedKey();
            codeLength += identifiedKey.decode(iStream);
            return codeLength;
        }

        if (choice == Choices.WRAPPED_KEY) {
            wrappedKey = new WrappedKey();
            codeLength += wrappedKey.decode(iStream);
            return codeLength;
        }

        if (choice == Choices.AGREED_KEY) {
            agreedKey = new AgreedKey();
            codeLength += agreedKey.decode(iStream);
            return codeLength;
        }

        throw new IOException("Error decoding AxdrChoice: Identifier matched to no item.");
    }

    public void encodeAndSave(int encodingSizeGuess) throws IOException {
        ReverseByteArrayOutputStream axdrOStream = new ReverseByteArrayOutputStream(encodingSizeGuess);
        encode(axdrOStream);
        code = axdrOStream.getArray();
    }

    public Choices getChoiceIndex() {
        return this.choice;
    }

    public void setIdentifiedKey(IdentifiedKey newVal) {
        resetChoices();
        choice = Choices.IDENTIFIED_KEY;
        identifiedKey = newVal;
    }

    public void setWrappedKey(WrappedKey newVal) {
        resetChoices();
        choice = Choices.WRAPPED_KEY;
        wrappedKey = newVal;
    }

    public void setAgreedKey(AgreedKey newVal) {
        resetChoices();
        choice = Choices.AGREED_KEY;
        agreedKey = newVal;
    }

    private void resetChoices() {
        choice = Choices._ERR_NONE_SELECTED;
        identifiedKey = null;
        wrappedKey = null;
        agreedKey = null;
    }

    @Override
    public String toString() {
        if (choice == Choices.IDENTIFIED_KEY) {
            return "choice: {identifiedKey: " + identifiedKey + "}";
        }

        if (choice == Choices.WRAPPED_KEY) {
            return "choice: {wrappedKey: " + wrappedKey + "}";
        }

        if (choice == Choices.AGREED_KEY) {
            return "choice: {agreedKey: " + agreedKey + "}";
        }

        return "unknown";
    }

}
