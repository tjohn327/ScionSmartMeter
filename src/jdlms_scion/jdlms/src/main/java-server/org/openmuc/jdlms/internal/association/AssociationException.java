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
package org.openmuc.jdlms.internal.association;

import org.openmuc.jdlms.internal.APdu;
import org.openmuc.jdlms.internal.ServiceError;
import org.openmuc.jdlms.internal.StateError;
import org.openmuc.jdlms.internal.asn1.cosem.COSEMpdu;
import org.openmuc.jdlms.internal.asn1.cosem.Enum;
import org.openmuc.jdlms.internal.asn1.cosem.ExceptionResponse;

public class AssociationException extends GenericAssociationException {

    private final StateError stateError;
    private final ServiceError serviceError;

    public AssociationException(StateError stateError, ServiceError serviceErorr) {
        this.stateError = stateError;
        this.serviceError = serviceErorr;
    }

    @Override
    public APdu getErrorMessageApdu() {
        Enum stateErrorEnum = this.stateError != null ? this.stateError.asEnum() : null;

        Enum serviceErorrEnum = this.serviceError != null ? this.serviceError.asEnum() : null;
        ExceptionResponse ex = new ExceptionResponse(stateErrorEnum, serviceErorrEnum);

        COSEMpdu cosemPdu = new COSEMpdu();
        cosemPdu.setExceptionResponse(ex);
        return new APdu(null, cosemPdu);
    }

}
