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
package org.openmuc.jdlms.internal;

import java.text.MessageFormat;

import org.openmuc.jdlms.CosemInterfaceObject;
import org.openmuc.jdlms.DlmsInvocationContext;
import org.openmuc.jdlms.SecuritySuite.SecurityPolicy;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.datatypes.DataObject.Type;

abstract class AbstarctInvocationCtx implements DlmsInvocationContext {
    private final CosemInterfaceObject target;
    private Object[] parameters;
    private final SecurityPolicy securityPolicy;
    private final int numOfParams;
    private final Type parameterType;

    public AbstarctInvocationCtx(CosemInterfaceObject target, Object[] parameters, SecurityPolicy securityPolicy,
            Type parameterType) {
        this.target = target;
        this.parameters = parameters;
        this.parameterType = parameterType;
        this.numOfParams = parameters.length;
        this.securityPolicy = securityPolicy;
    }

    @Override
    public final SecurityPolicy getSecurityPolicy() {
        return this.securityPolicy;
    }

    @Override
    public final CosemInterfaceObject getTarget() {
        return this.target;
    }

    @Override
    public final Object[] getParameters() {
        return this.parameters;
    }

    @Override
    public final void setParameters(Object[] params) {
        if (this.parameters == null) {
            throw new IllegalArgumentException("The Method does not take any arguments.");
        }
        else if (numOfParams != params.length) {
            throw new IllegalArgumentException("The num of parameters does not match the method.");
        }

        for (int i = 0; i < params.length; i++) {
            if (!parameters[i].getClass().isAssignableFrom(params[i].getClass())) {
                throw new IllegalArgumentException(
                        MessageFormat.format("Parameter a index {0} is not assignable to the parameter type.", i));
            }

            if (pramIsDObutTypesAreNotEqual(params[i])) {
                throw new IllegalArgumentException("Supplied DataObject does not match the required type.");
            }
        }

        this.parameters = params;
    }

    private boolean pramIsDObutTypesAreNotEqual(Object param) {
        return parameterType != null && parameterType != Type.DONT_CARE && param instanceof DataObject
                && ((DataObject) param).getType() != parameterType;
    }

}
