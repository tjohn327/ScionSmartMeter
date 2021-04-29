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
package org.openmuc.jdlms.interfaceclass.attribute;

import org.openmuc.jdlms.interfaceclass.InterfaceClass;

/**
 * This class contains the attributes defined for IC EthernetSetup.
 * 
 * @deprecated since 1.5.1. Use the none enum initializer of {@link org.openmuc.jdlms.AttributeAddress}.
 */
@Deprecated
public enum EthernetSetupAttribute implements AttributeClass {
    LOGICAL_NAME(1),
    DL_REFERENCE(2),
    IP_ADDRESS_DATA_TYPE(3),
    MULTICAST_IP_ADDRESS(4),
    IP_OPTIONS(5),
    SUBNET_MASK(6),
    GATEWAY_IP_ADDRESS(7),
    USE_DHCP_FLAG(8),
    PRIMARY_DNS_ADDRESS(9),
    SECONDARY_DNS_ADDRESS(10),;

    static final InterfaceClass INTERFACE_CLASS = InterfaceClass.IP_V4_SETUP;
    private int attributeId;

    private EthernetSetupAttribute(int attributeId) {
        this.attributeId = attributeId;
    }

    @Override
    public int attributeId() {
        return attributeId;
    }

    @Override
    public String attributeName() {
        return name();
    }

    @Override
    public InterfaceClass interfaceClass() {
        return INTERFACE_CLASS;
    }

}
