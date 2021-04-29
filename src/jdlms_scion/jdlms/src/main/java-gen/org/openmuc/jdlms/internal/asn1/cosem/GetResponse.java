/**
 * This class file was automatically generated by the AXDR compiler that is part of jDLMS (http://www.openmuc.org)
 */

package org.openmuc.jdlms.internal.asn1.cosem;

import java.io.IOException;
import java.io.InputStream;

import org.openmuc.jasn1.ber.ReverseByteArrayOutputStream;
import org.openmuc.jdlms.internal.asn1.axdr.AxdrType;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrEnum;

public class GetResponse implements AxdrType {

    public byte[] code = null;

    public static enum Choices {
        _ERR_NONE_SELECTED(-1),
        GET_RESPONSE_NORMAL(1),
        GET_RESPONSE_WITH_DATABLOCK(2),
        GET_RESPONSE_WITH_LIST(3),;

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

    public GetResponseNormal getResponseNormal = null;

    public GetResponseWithDatablock getResponseWithDatablock = null;

    public GetResponseWithList getResponseWithList = null;

    public GetResponse() {
    }

    public GetResponse(byte[] code) {
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

        if (choice == Choices.GET_RESPONSE_WITH_LIST) {
            codeLength += getResponseWithList.encode(axdrOStream);
            AxdrEnum c = new AxdrEnum(3);
            codeLength += c.encode(axdrOStream);
            return codeLength;
        }

        if (choice == Choices.GET_RESPONSE_WITH_DATABLOCK) {
            codeLength += getResponseWithDatablock.encode(axdrOStream);
            AxdrEnum c = new AxdrEnum(2);
            codeLength += c.encode(axdrOStream);
            return codeLength;
        }

        if (choice == Choices.GET_RESPONSE_NORMAL) {
            codeLength += getResponseNormal.encode(axdrOStream);
            AxdrEnum c = new AxdrEnum(1);
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

        if (choice == Choices.GET_RESPONSE_NORMAL) {
            getResponseNormal = new GetResponseNormal();
            codeLength += getResponseNormal.decode(iStream);
            return codeLength;
        }

        if (choice == Choices.GET_RESPONSE_WITH_DATABLOCK) {
            getResponseWithDatablock = new GetResponseWithDatablock();
            codeLength += getResponseWithDatablock.decode(iStream);
            return codeLength;
        }

        if (choice == Choices.GET_RESPONSE_WITH_LIST) {
            getResponseWithList = new GetResponseWithList();
            codeLength += getResponseWithList.decode(iStream);
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

    public void setGetResponseNormal(GetResponseNormal newVal) {
        resetChoices();
        choice = Choices.GET_RESPONSE_NORMAL;
        getResponseNormal = newVal;
    }

    public void setGetResponseWithDatablock(GetResponseWithDatablock newVal) {
        resetChoices();
        choice = Choices.GET_RESPONSE_WITH_DATABLOCK;
        getResponseWithDatablock = newVal;
    }

    public void setGetResponseWithList(GetResponseWithList newVal) {
        resetChoices();
        choice = Choices.GET_RESPONSE_WITH_LIST;
        getResponseWithList = newVal;
    }

    private void resetChoices() {
        choice = Choices._ERR_NONE_SELECTED;
        getResponseNormal = null;
        getResponseWithDatablock = null;
        getResponseWithList = null;
    }

    @Override
    public String toString() {
        if (choice == Choices.GET_RESPONSE_NORMAL) {
            return "choice: {getResponseNormal: " + getResponseNormal + "}";
        }

        if (choice == Choices.GET_RESPONSE_WITH_DATABLOCK) {
            return "choice: {getResponseWithDatablock: " + getResponseWithDatablock + "}";
        }

        if (choice == Choices.GET_RESPONSE_WITH_LIST) {
            return "choice: {getResponseWithList: " + getResponseWithList + "}";
        }

        return "unknown";
    }

}
