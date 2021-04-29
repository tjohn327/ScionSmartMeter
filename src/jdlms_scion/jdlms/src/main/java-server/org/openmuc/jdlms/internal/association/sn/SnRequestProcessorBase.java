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
package org.openmuc.jdlms.internal.association.sn;

import org.openmuc.jdlms.internal.APdu;
import org.openmuc.jdlms.internal.asn1.cosem.COSEMpdu;
import org.openmuc.jdlms.internal.asn1.iso.acse.ACSEApdu;
import org.openmuc.jdlms.internal.association.AssociationMessenger;
import org.openmuc.jdlms.internal.association.RequestProcessorBase;
import org.openmuc.jdlms.internal.association.RequestProcessorData;

abstract class SnRequestProcessorBase extends RequestProcessorBase {

    public SnRequestProcessorBase(AssociationMessenger associationMessenger,
            RequestProcessorData requestProcessorData) {
        super(associationMessenger, requestProcessorData);
    }

    protected final APdu newAPdu() {
        ACSEApdu acseAPdu = null;
        COSEMpdu cosemPdu = new COSEMpdu();

        return new APdu(acseAPdu, cosemPdu);
    }

}
