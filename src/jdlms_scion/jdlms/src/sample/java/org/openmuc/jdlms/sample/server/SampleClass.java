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
package org.openmuc.jdlms.sample.server;

import org.openmuc.jdlms.CosemAttribute;
import org.openmuc.jdlms.CosemClass;
import org.openmuc.jdlms.CosemInterfaceObject;
import org.openmuc.jdlms.CosemMethod;
import org.openmuc.jdlms.DlmsInterceptor;
import org.openmuc.jdlms.IllegalMethodAccessException;
import org.openmuc.jdlms.MethodResultCode;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.datatypes.DataObject.Type;

@CosemClass(id = 99)
public class SampleClass extends CosemInterfaceObject {

    @CosemAttribute(id = 2, type = Type.LONG64)
    private final DataObject d1;

    public SampleClass(DlmsInterceptor interceptor) {
        super("0.0.0.2.1.255", interceptor);

        this.d1 = DataObject.newInteger64Data(864972689331191808L);
    }

    public DataObject getD1() {
        return d1;
    }

    @CosemMethod(id = 1)
    public void hello() throws IllegalMethodAccessException {
        System.out.println();
        System.out.println("From Cosem Class: Has been called");
        System.out.println();
        return;
    }

    @CosemMethod(id = 2, consumes = Type.OCTET_STRING)
    public DataObject hello2(DataObject datO) throws IllegalMethodAccessException {
        throw new IllegalMethodAccessException(MethodResultCode.OTHER_REASON);
    }

}
