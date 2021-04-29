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
package org.openmuc.jdlms;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.datatypes.DataObject.Type;

/**
 * This annotation is used to define a COSEM attribute in a COSEM class. Only fields of type {@link DataObject} can be
 * annotated with this annotation.
 * 
 * <pre>
 * &#64;{@link CosemClass}(id = 99, version = 2)
 * public class DemoClass extends {@linkplain CosemInterfaceObject} {
 *      &#64;{@link CosemAttribute}(id = 2)
 *      private {@link DataObject} data;
 *      ...
 * }
 * </pre>
 * 
 * To intercept read or write accesses of the attribute, a public get/set method must be provided. The get method must
 * have <i>get</i> as a prefix and the set method respectively a <i>set</i> as a prefix.
 * 
 * <p>
 * The set and get methods may only throw an {@link IllegalAttributeAccessException}.
 * </p>
 * 
 * <pre>
 * &#64;{@link CosemClass}(id = 99, version = 2)
 * public class DemoClass extends {@linkplain CosemInterfaceObject} {
 *      &#64;{@link CosemAttribute}(id = 2, type = Type.INTEGER)
 *      private {@link DataObject} data;
 *      ...
 * 
 *      public void setData(DataObject newData) throws {@link IllegalAttributeAccessException} {
 *          Number value = newData.getValue();
 * 
 *          if (value.intValue() &lt; 10) {
 *              throw new IllegalAttributeAccessException(AccessResultCode.TYPE_UNMATCHED);
 *          }
 * 
 *          this.data = newData;
 *      }
 * }
 * </pre>
 * 
 * <p>
 * <b>NOTE:</b> Ambiguous attribute ID's in a class are not allowed. Attribute ID 1 is reserved for the system.
 * </p>
 * 
 *
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CosemAttribute {

    /**
     * The attribute ID. The value must be greater than 1. Attribute ID 1 is reserved for the logical name (OBIS code /
     * instance ID).
     * 
     * @return the attribute ID &gt; 1.
     */
    byte id();

    /**
     * The access restriction for the attribute in a COSEM class.
     * 
     * <p>
     * NOTE: This attribute is optional.
     * </p>
     * 
     * @return the access mode.
     */
    AttributeAccessMode accessMode() default AttributeAccessMode.READ_AND_WRITE;

    /**
     * The data type of the attribute. If a client may want to send a different type, the server will deny the write
     * access.
     * 
     * @return the type of the attribute.
     */
    Type type() default Type.DONT_CARE;

    /**
     * Attribute sector ID's in a value range 0 to 255.
     * 
     * <p>
     * <b>NOTE:</b> get and set methods with {@link SelectiveAccessDescription}s for the attribute must be provided.
     * </p>
     * 
     * @return the attribute selector.
     * 
     * @see SelectiveAccessDescription
     */
    int[] selector() default {};

    /**
     * The offset from the base name. E.h. 0x18. Must be a multiple of 0x08 and unique in the class.
     * 
     * @return the short name offset.
     */
    int snOffset() default -1;
}
