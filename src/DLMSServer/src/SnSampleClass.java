/*
 * Copyright 2012-20 Fraunhofer ISE
 *
 * This file is part of jDLMS.
 * For more information visit http://www.openmuc.org
 *
 * jDLMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jDLMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jDLMS.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

import org.openmuc.jdlms.*;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.datatypes.DataObject.Type;

import static org.openmuc.jdlms.datatypes.DataObject.newOctetStringData;

@CosemClass(id = 99, version = 12)
public class SnSampleClass extends CosemSnInterfaceObject {

    public static final ObisCode INSTANCE_ID = new ObisCode("1.11.123.55.1.13");

    @CosemAttribute(id = 2, type = Type.LONG64, snOffset = 0x08)
    private final DataObject d2;

    @CosemAttribute(id = 4, type = Type.OCTET_STRING, snOffset = 0x18)
    private DataObject d1;

    public SnSampleClass() {
        super(0xA0, INSTANCE_ID.toString());
        this.d2 = DataObject.newInteger64Data(864972689331191808L);

//        byte[] string = new byte[0xFFFF * 3];
//        string[0] = 1;
//        string[string.length - 1] = 1;
        this.d1 = DataObject.newOctetStringData("Default String".getBytes());
    }

    public void setD1(DataObject d1) {
        this.d1 = d1;
        byte[] value = d1.getValue();
        System.out.println(value.length);
        System.out.println(value[0]);
        System.out.println(value[value.length - 1]);
    }

    @CosemMethod(id = 3, snOffset = 0x40)
    public DataObject hello() {
        return newOctetStringData("Hello World".getBytes());
    }
}
