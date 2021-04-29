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
package org.openmuc.jdlms.interfaceclass.method;

import org.openmuc.jdlms.interfaceclass.InterfaceClass;

/**
 * This class contains the methods defined for IC MBusClient.
 * 
 * @deprecated since 1.5.1. Use the none enum initializer of {@link org.openmuc.jdlms.MethodParameter}.
 */
@Deprecated
public enum MBusClientMethod implements MethodClass {
    SLAVE_INSTALL(1, false),
    SLAVE_DEINSTALL(2, false),
    CAPTURE(3, false),
    RESET_ALARM(4, false),
    SYNCHRONIZE_CLOCK(5, false),
    DATA_SEND(6, false),
    SET_ENCRYPTION_KEY(7, false),
    TRANSFER_KEY(8, false);

    static final InterfaceClass INTERFACE_CLASS = InterfaceClass.MBUS_CLIENT;
    private int methodId;
    private boolean mandatory;

    private MBusClientMethod(int methodId, boolean mandatory) {
        this.methodId = methodId;
        this.mandatory = mandatory;
    }

    @Override
    public boolean isMandatory() {
        return this.mandatory;
    }

    @Override
    public int getMethodId() {
        return this.methodId;
    }

    @Override
    public InterfaceClass getInterfaceClass() {
        return INTERFACE_CLASS;
    }

    @Override
    public String getMethodName() {
        return name();
    }

}
