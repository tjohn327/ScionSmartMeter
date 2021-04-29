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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.internal.asn1.cosem.Data;

public class DataConverterTest {

    @Test
    public void testFloatToData() {
        float expectedV = 1000f;
        Data d = DataConverter.convertDataObjectToData(DataObject.newFloat32Data(expectedV));
        float f = DataConverter.convertDataToDataObject(d).getValue();

        assertEquals(expectedV, f, .01f);
    }

    @Test
    public void testFloat64ToData() {
        double expectedV = 1000d;
        Data d = DataConverter.convertDataObjectToData(DataObject.newFloat64Data(expectedV));
        double f = DataConverter.convertDataToDataObject(d).getValue();

        assertEquals(expectedV, f, .01f);
    }

}
