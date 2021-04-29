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

import static org.openmuc.jdlms.AccessResultCode.SCOPE_OF_ACCESS_VIOLATED;
import static org.openmuc.jdlms.datatypes.DataObject.newNullData;
import static org.openmuc.jdlms.internal.AttributeInvokationCtx.saveCallInterceptIntercept;
import static org.openmuc.jdlms.internal.AttributeInvokationCtx.toAttributeDesctiptor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import org.openmuc.jdlms.AccessResultCode;
import org.openmuc.jdlms.CosemAttribute;
import org.openmuc.jdlms.CosemInterfaceObject;
import org.openmuc.jdlms.CosemResourceDescriptor;
import org.openmuc.jdlms.DlmsAccessException;
import org.openmuc.jdlms.DlmsInterceptor;
import org.openmuc.jdlms.DlmsInvocationContext;
import org.openmuc.jdlms.DlmsInvocationContext.XDlmsServiceType;
import org.openmuc.jdlms.IllegalAttributeAccessException;
import org.openmuc.jdlms.ObisCode;
import org.openmuc.jdlms.SecuritySuite.SecurityPolicy;
import org.openmuc.jdlms.SelectiveAccessDescription;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.internal.DataDirectoryImpl.CosemClassInstance;

public interface AttributeAccessor extends Accessor {

    DataObject get(CosemClassInstance cosemClassInstance, SelectiveAccessDescription selectiveAccessDescription,
            Long connectionId, SecurityPolicy securityPolicy) throws IllegalAttributeAccessException;

    void set(DataObject newVal, CosemClassInstance dlmsClassInstance,
            SelectiveAccessDescription selectiveAccessDescription, Long connectionId, SecurityPolicy securityPolicy)
            throws IllegalAttributeAccessException;

    CosemAttribute getCosemAttribute();

    abstract static class BaseAttributeAccessor implements AttributeAccessor {
        private final CosemAttribute cosemAttribute;

        public BaseAttributeAccessor(CosemAttribute cosemAttribute) {
            this.cosemAttribute = cosemAttribute;
        }

        @Override
        public final AccessorType getAccessorType() {
            return AccessorType.ATTRIBUTE;
        }

        @Override
        public final CosemAttribute getCosemAttribute() {
            return this.cosemAttribute;
        }
    }

    static class LogicalNameFakeAccessor extends BaseAttributeAccessor {

        private final ObisCode instanceId;

        public LogicalNameFakeAccessor(ObisCode instanceId, CosemAttribute cosemAttribute) {
            super(cosemAttribute);
            this.instanceId = instanceId;
        }

        @Override
        public DataObject get(CosemClassInstance cosemClassInstance,
                SelectiveAccessDescription selectiveAccessDescription, Long connectionId, SecurityPolicy securityPolicy)
                throws IllegalAttributeAccessException {
            return DataObject.newOctetStringData(this.instanceId.bytes());
        }

        @Override
        public void set(DataObject newVal, CosemClassInstance dlmsClassInstanc,
                SelectiveAccessDescription selectiveAccessDescription, Long connectionId, SecurityPolicy securityPolicy)
                throws IllegalAttributeAccessException {
            throw new IllegalAttributeAccessException(AccessResultCode.READ_WRITE_DENIED);
        }

    }

    static class FieldAccessor extends BaseAttributeAccessor {

        private final Field field;

        public FieldAccessor(Field field, CosemAttribute cosemAttribute) {
            super(cosemAttribute);
            this.field = field;
        }

        @Override
        public DataObject get(final CosemClassInstance cosemClassInstance,
                SelectiveAccessDescription selectiveAccessDescription, Long connectionId, SecurityPolicy securityPolicy)
                throws IllegalAttributeAccessException {
            CosemInterfaceObject instance = cosemClassInstance.getInstance();
            DlmsInterceptor interceptor = instance.getInterceptor();

            DataObject result;
            if (interceptor != null) {
                result = callGetViaInterceptor(cosemClassInstance, securityPolicy, instance, interceptor);
            }
            else {
                result = saveGet(instance);
            }

            return result == null ? newNullData() : result;
        }

        private DataObject callGetViaInterceptor(final CosemClassInstance cosemClassInstance,
                SecurityPolicy securityPolicy, CosemInterfaceObject instance, DlmsInterceptor interceptor)
                throws IllegalAttributeAccessException {
            CosemResourceDescriptor attributeDesc = toAttributeDesctiptor(cosemClassInstance, super.getCosemAttribute(),
                    instance);

            DlmsInvocationContext ctx = new AttributeInvokationCtx(securityPolicy, XDlmsServiceType.GET, attributeDesc,
                    instance, this.field, super.getCosemAttribute().type()) {

                @Override
                public DataObject proceed() throws DlmsAccessException {
                    return saveGet(getTarget());
                }
            };
            return saveCallInterceptIntercept(interceptor, ctx);
        }

        private DataObject saveGet(CosemInterfaceObject instance) throws IllegalAttributeAccessException {
            try {
                return (DataObject) this.field.get(instance);
            } catch (IllegalAccessException e) {
                throw new IllegalAttributeAccessException(AccessResultCode.OTHER_REASON, e);
            }
        }

        @Override
        public void set(final DataObject newVal, final CosemClassInstance cosemClassInstanc,
                SelectiveAccessDescription selectiveAccessDescription, Long connectionId, SecurityPolicy securityPolicy)
                throws IllegalAttributeAccessException {
            CosemInterfaceObject instance = cosemClassInstanc.getInstance();
            DlmsInterceptor interceptor = instance.getInterceptor();
            if (interceptor != null) {
                CosemResourceDescriptor attributeDesc = toAttributeDesctiptor(cosemClassInstanc,
                        super.getCosemAttribute(), instance);

                DlmsInvocationContext ctx = new AttributeInvokationCtx(securityPolicy, XDlmsServiceType.SET,
                        attributeDesc, instance, field, super.getCosemAttribute().type(), newVal) {

                    @Override
                    public DataObject proceed() throws DlmsAccessException {
                        saveSet((DataObject) (getParameters()[0]), getTarget());
                        return null;
                    }

                };

                saveCallInterceptIntercept(interceptor, ctx);
            }
            else {
                saveSet(newVal, instance);
            }

        }

        private void saveSet(DataObject newVal, CosemInterfaceObject instance) throws IllegalAttributeAccessException {
            try {
                this.field.set(instance, newVal);
            } catch (IllegalAccessException e) {
                throw new IllegalAttributeAccessException(AccessResultCode.OTHER_REASON, e);
            }
        }
    }

    static class MethodSetFieldGetAccessor extends BaseAttributeAccessor {

        private final FieldAccessor fieldAccessor;
        private final MethodAttributeAccessor methodAccessor;

        public MethodSetFieldGetAccessor(Field field, Method setMethod, CosemAttribute cosemAttribute,
                Set<Integer> accessSelectors) {
            super(cosemAttribute);
            this.fieldAccessor = new FieldAccessor(field, cosemAttribute);
            this.methodAccessor = new MethodAttributeAccessor(null, setMethod, cosemAttribute, accessSelectors);
        }

        @Override
        public DataObject get(CosemClassInstance cosemClassInstance,
                SelectiveAccessDescription selectiveAccessDescription, Long connectionId, SecurityPolicy securityPolicy)
                throws IllegalAttributeAccessException {
            return this.fieldAccessor.get(cosemClassInstance, selectiveAccessDescription, connectionId, securityPolicy);
        }

        @Override
        public void set(DataObject newVal, CosemClassInstance dlmsClassInstance,
                SelectiveAccessDescription selectiveAccessDescription, Long connectionId, SecurityPolicy securityPolicy)
                throws IllegalAttributeAccessException {
            this.methodAccessor.set(newVal, dlmsClassInstance, selectiveAccessDescription, connectionId,
                    securityPolicy);
        }

    }

    static class FieldSetMethodGetAccessor extends BaseAttributeAccessor {

        private final FieldAccessor fieldAccessor;
        private final MethodAttributeAccessor methodAccessor;

        public FieldSetMethodGetAccessor(Field field, Method getMethod, CosemAttribute cosemAttribute,
                Set<Integer> accessSelectors) {
            super(cosemAttribute);
            this.fieldAccessor = new FieldAccessor(field, cosemAttribute);
            this.methodAccessor = new MethodAttributeAccessor(getMethod, null, cosemAttribute, accessSelectors);
        }

        @Override
        public DataObject get(CosemClassInstance cosemClassInstance,
                SelectiveAccessDescription selectiveAccessDescription, Long connectionId, SecurityPolicy securityPolicy)
                throws IllegalAttributeAccessException {
            return this.methodAccessor.get(cosemClassInstance, selectiveAccessDescription, connectionId,
                    securityPolicy);
        }

        @Override
        public void set(DataObject newVal, CosemClassInstance cosemClassInstanc,
                SelectiveAccessDescription selectiveAccessDescription, Long connectionId, SecurityPolicy securityPolicy)
                throws IllegalAttributeAccessException {
            this.fieldAccessor.set(newVal, cosemClassInstanc, selectiveAccessDescription, connectionId, securityPolicy);
        }

    }

    public static class MethodAttributeAccessor extends BaseAttributeAccessor {

        private final Method getMethod;
        private final Method setMethod;
        private final Set<Integer> accessSelectors;
        private final boolean containsGetId;
        private final boolean containsSetId;

        public MethodAttributeAccessor(Method getMethod, Method setMethod, CosemAttribute cosemAttribute,
                Set<Integer> accessSelectors) {
            super(cosemAttribute);
            this.getMethod = getMethod;
            this.setMethod = setMethod;

            this.accessSelectors = accessSelectors;

            this.containsGetId = methodHasConnectionIdParam(getMethod);
            this.containsSetId = methodHasConnectionIdParam(setMethod);
        }

        private boolean methodHasConnectionIdParam(Method method) {
            if (method == null) {
                return false;
            }

            Class<?>[] parameterTypes = method.getParameterTypes();

            if (parameterTypes.length == 0) {
                return false;
            }

            int lastIndex = parameterTypes.length - 1;

            return Long.class.isAssignableFrom(parameterTypes[lastIndex]);
        }

        @Override
        public DataObject get(CosemClassInstance classInstance, SelectiveAccessDescription selectiveAccessDescription,
                Long connectionId, SecurityPolicy securityPolicy) throws IllegalAttributeAccessException {
            Object[] parameter = buildGetParameter(selectiveAccessDescription, connectionId);
            CosemInterfaceObject instance = classInstance.getInstance();
            DataObject result;

            DlmsInterceptor interceptor = instance.getInterceptor();
            if (interceptor != null) {
                CosemResourceDescriptor address = toAttributeDesctiptor(classInstance, super.getCosemAttribute(),
                        instance);
                DlmsInvocationContext ctx = new AttributeInvokationCtx(securityPolicy, XDlmsServiceType.GET, address,
                        instance, this.getMethod, super.getCosemAttribute().type(), parameter) {
                    @Override
                    public DataObject proceed() throws DlmsAccessException {
                        return saveGet(getParameters(), getTarget());
                    }
                };
                result = saveCallInterceptIntercept(interceptor, ctx);
            }
            else {
                result = saveGet(parameter, instance);
            }

            if (result == null) {
                return DataObject.newNullData();
            }

            return result;
        }

        private DataObject saveGet(Object[] parameter, CosemInterfaceObject instance)
                throws IllegalAttributeAccessException {
            try {
                return (DataObject) this.getMethod.invoke(instance, parameter);

            } catch (IllegalAccessException e) {
                throw new IllegalAttributeAccessException(AccessResultCode.OTHER_REASON);
            } catch (InvocationTargetException e) {
                throw convert(e);
            }
        }

        private Object[] buildGetParameter(SelectiveAccessDescription selectiveAccessDescription, Long connectionId)
                throws IllegalAttributeAccessException {
            Object[] parameter = new Object[this.getMethod.getParameterTypes().length];
            setSelectiveAcccessDescription(selectiveAccessDescription, parameter, 0);

            if (this.containsGetId) {
                setConnectionId(connectionId, parameter);
            }

            return parameter;
        }

        private void setConnectionId(Long connectionId, Object[] parameter) {
            parameter[parameter.length - 1] = connectionId;
        }

        @Override
        public void set(DataObject newVal, CosemClassInstance classInstance,
                SelectiveAccessDescription selectiveAccessDescription, Long connectionId, SecurityPolicy securityPolicy)
                throws IllegalAttributeAccessException {
            Object[] parameter = buildSetParameter(newVal, selectiveAccessDescription, connectionId);

            CosemInterfaceObject instance = classInstance.getInstance();

            DlmsInterceptor interceptor = instance.getInterceptor();
            if (interceptor != null) {
                DlmsInvocationContext ctx = new AttributeInvokationCtx(securityPolicy, XDlmsServiceType.SET, null,
                        instance, this.setMethod, super.getCosemAttribute().type(), parameter) {

                    @Override
                    public DataObject proceed() throws DlmsAccessException {
                        saveSet(getTarget(), getParameters());
                        return null;
                    }
                };
                saveCallInterceptIntercept(interceptor, ctx);
            }
            else {
                saveSet(instance, parameter);
            }

        }

        private void saveSet(CosemInterfaceObject instance, Object[] parameter) throws IllegalAttributeAccessException {
            try {
                this.setMethod.invoke(instance, parameter);
            } catch (IllegalAccessException e) {
                throw new IllegalAttributeAccessException(AccessResultCode.OTHER_REASON);
            } catch (InvocationTargetException e) {
                throw convert(e);
            }
        }

        private Object[] buildSetParameter(DataObject newVal, SelectiveAccessDescription selectiveAccessDescription,
                Long connectionId) throws IllegalAttributeAccessException {
            Object[] parameter = new Object[this.setMethod.getParameterTypes().length];

            parameter[0] = newVal;
            setSelectiveAcccessDescription(selectiveAccessDescription, parameter, 1);
            if (this.containsSetId) {
                setConnectionId(connectionId, parameter);
            }
            return parameter;
        }

        private void setSelectiveAcccessDescription(SelectiveAccessDescription selectiveAccessDescription,
                Object[] parameter, int indexOfAccess) throws IllegalAttributeAccessException {

            if (selectiveAccessDescription == null) {
                return;
            }

            if (this.accessSelectors.isEmpty()) {
                throw new IllegalAttributeAccessException(SCOPE_OF_ACCESS_VIOLATED);
            }

            parameter[indexOfAccess] = selectiveAccessDescription;
        }

        private IllegalAttributeAccessException convert(InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            if (!(targetException instanceof IllegalAttributeAccessException)) {
                return new IllegalAttributeAccessException(AccessResultCode.OTHER_REASON);
            }
            return (IllegalAttributeAccessException) targetException;
        }
    }

}
