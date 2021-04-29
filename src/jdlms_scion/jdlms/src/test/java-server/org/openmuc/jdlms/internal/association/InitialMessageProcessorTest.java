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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.openmuc.jdlms.AuthenticationMechanism.HLS5_GMAC;
import static org.openmuc.jdlms.AuthenticationMechanism.LOW;
import static org.openmuc.jdlms.AuthenticationMechanism.NONE;
import static org.openmuc.jdlms.ConformanceSetting.ACTION;
import static org.openmuc.jdlms.ConformanceSetting.GET;
import static org.openmuc.jdlms.ConformanceSetting.SET;
import static org.openmuc.jdlms.internal.ConformanceSettingConverter.conformanceSettingFor;
import static org.openmuc.jdlms.internal.ContextId.LOGICAL_NAME_REFERENCING_NO_CIPHERING;
import static org.openmuc.jdlms.internal.ObjectIdentifier.applicationContextNameFrom;
import static org.openmuc.jdlms.internal.ObjectIdentifier.mechanismNameFrom;
import static org.powermock.api.mockito.PowerMockito.doCallRealMethod;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmuc.jasn1.ber.types.BerOctetString;
import org.openmuc.jdlms.ConformanceSetting;
import org.openmuc.jdlms.LogicalDevice;
import org.openmuc.jdlms.RawMessageData.RawMessageDataBuilder;
import org.openmuc.jdlms.SecuritySuite;
import org.openmuc.jdlms.internal.APdu;
import org.openmuc.jdlms.internal.AssociationResult;
import org.openmuc.jdlms.internal.ConformanceSettingConverter;
import org.openmuc.jdlms.internal.DataDirectoryImpl.CosemLogicalDevice;
import org.openmuc.jdlms.internal.ServerConnectionData;
import org.openmuc.jdlms.internal.asn1.cosem.COSEMpdu;
import org.openmuc.jdlms.internal.asn1.cosem.Conformance;
import org.openmuc.jdlms.internal.asn1.cosem.InitiateRequest;
import org.openmuc.jdlms.internal.asn1.cosem.InitiateResponse;
import org.openmuc.jdlms.internal.asn1.cosem.Unsigned16;
import org.openmuc.jdlms.internal.asn1.iso.acse.AARQApdu;
import org.openmuc.jdlms.internal.asn1.iso.acse.ACSEApdu;
import org.openmuc.jdlms.internal.asn1.iso.acse.AuthenticationValue;
import org.openmuc.jdlms.internal.asn1.iso.acse.MechanismName;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ APdu.class, })
public class InitialMessageProcessorTest {

    @Test(expected = AssociatRequestException.class)
    public void test_authmechanism_mismatch() throws Exception {

        SecuritySuite secSuite = mock(SecuritySuite.class);
        when(secSuite.getAuthenticationMechanism()).thenReturn(HLS5_GMAC);

        ServerConnectionData cd = mock(ServerConnectionData.class);
        when(cd.getClientId()).thenReturn(16);

        Map<Integer, SecuritySuite> restrictions = new HashMap<>();
        restrictions.put(cd.getClientId(), secSuite);

        LogicalDevice ld = mock(LogicalDevice.class);
        when(ld.getRestrictions()).thenReturn(restrictions);

        CosemLogicalDevice cLd = mock(CosemLogicalDevice.class);
        when(cLd.getLogicalDevice()).thenReturn(ld);

        mockApduDecodeMethods(null, null);
        InitiateMessageProcessor processor = spy(new InitiateMessageProcessor(cd, cLd));
        processor.processInitialMessage(null);
    }

    @Test()
    public void test_authmechanism_none() throws Exception {

        SecuritySuite secSuite = mock(SecuritySuite.class);
        when(secSuite.getAuthenticationMechanism()).thenReturn(NONE);

        ServerConnectionData cd = mock(ServerConnectionData.class);
        when(cd.getClientId()).thenReturn(16);
        when(cd.getSecuritySuite()).thenCallRealMethod();
        doCallRealMethod().when(cd).setSecuritySuite(any(SecuritySuite.class));

        Map<Integer, SecuritySuite> restrictions = new HashMap<>();
        restrictions.put(cd.getClientId(), secSuite);

        LogicalDevice ld = mock(LogicalDevice.class);
        when(ld.getRestrictions()).thenReturn(restrictions);

        CosemLogicalDevice cLd = mock(CosemLogicalDevice.class);
        when(cLd.getLogicalDevice()).thenReturn(ld);

        mockApduDecodeMethods(null, null);
        InitiateMessageProcessor processor = spy(new InitiateMessageProcessor(cd, cLd));
        APdu res = processor.processInitialMessage(null);
        assertEquals(AssociationResult.ACCEPTED.getValue(), res.getAcseAPdu().getAare().getResult().longValue());
    }

    @Test(expected = AssociatRequestException.class)
    public void test_wrong_client_Id() throws Exception {

        SecuritySuite secSuite = mock(SecuritySuite.class);
        when(secSuite.getAuthenticationMechanism()).thenReturn(NONE);

        ServerConnectionData cd = mock(ServerConnectionData.class);
        when(cd.getClientId()).thenReturn(16);

        Map<Integer, SecuritySuite> restrictions = new HashMap<>();
        restrictions.put(17, secSuite);

        LogicalDevice ld = mock(LogicalDevice.class);
        when(ld.getRestrictions()).thenReturn(restrictions);

        CosemLogicalDevice cLd = mock(CosemLogicalDevice.class);
        when(cLd.getLogicalDevice()).thenReturn(ld);

        mockApduDecodeMethods(null, null);
        InitiateMessageProcessor processor = spy(new InitiateMessageProcessor(cd, cLd));
        processor.processInitialMessage(null);
    }

    @Test
    public void test_sec_suite_low() throws Exception {
        byte[] password = "HelloWold".getBytes();

        SecuritySuite secSuite = mock(SecuritySuite.class);
        when(secSuite.getAuthenticationMechanism()).thenReturn(LOW);
        when(secSuite.getPassword()).thenReturn(password);

        ServerConnectionData cd = mock(ServerConnectionData.class);
        when(cd.getClientId()).thenReturn(16);
        when(cd.getSecuritySuite()).thenCallRealMethod();
        doCallRealMethod().when(cd).setSecuritySuite(any(SecuritySuite.class));

        Map<Integer, SecuritySuite> restrictions = new HashMap<>();
        restrictions.put(cd.getClientId(), secSuite);

        LogicalDevice ld = mock(LogicalDevice.class);
        Set<ConformanceSetting> conformance = new HashSet<>(Arrays.asList(GET, SET, ACTION));
        when(ld.getConformance()).thenReturn(conformance);
        when(ld.getRestrictions()).thenReturn(restrictions);

        CosemLogicalDevice cLd = mock(CosemLogicalDevice.class);
        when(cLd.getLogicalDevice()).thenReturn(ld);

        mockApduDecodeMethods(mechanismNameFrom(LOW), password);
        InitiateMessageProcessor processor = spy(new InitiateMessageProcessor(cd, cLd));

        APdu result = processor.processInitialMessage(null);

        ACSEApdu acseAPdu = result.getAcseAPdu();
        COSEMpdu cosemPdu = result.getCosemPdu();
        assertNotNull(acseAPdu.getAare());

        assertEquals(AssociationResult.ACCEPTED.getValue(), acseAPdu.getAare().getResult().intValue());
        assertEquals(COSEMpdu.Choices.INITIATERESPONSE, cosemPdu.getChoiceIndex());
        InitiateResponse initiateResponse = cosemPdu.initiateResponse;
        Conformance negotiatedConformance = initiateResponse.negotiatedConformance;
        assertNotNull("Conformance is null.", negotiatedConformance);
        assertEquals(conformance, conformanceSettingFor(negotiatedConformance));
        assertEquals(6L, initiateResponse.negotiatedDlmsVersionNumber.getValue());
    }

    @Test(expected = AssociatRequestException.class)
    public void test_sec_suite_low_wrong_pw() throws Exception {
        byte[] password = "HelloWold".getBytes();

        SecuritySuite secSuite = mock(SecuritySuite.class);
        when(secSuite.getAuthenticationMechanism()).thenReturn(LOW);
        when(secSuite.getPassword()).thenReturn(password);

        ServerConnectionData cd = mock(ServerConnectionData.class);
        when(cd.getClientId()).thenReturn(16);

        Map<Integer, SecuritySuite> restrictions = new HashMap<>();
        restrictions.put(cd.getClientId(), secSuite);

        LogicalDevice ld = mock(LogicalDevice.class);
        Set<ConformanceSetting> conformance = new HashSet<>(Arrays.asList(GET, SET, ACTION));
        when(ld.getConformance()).thenReturn(conformance);
        when(ld.getRestrictions()).thenReturn(restrictions);

        CosemLogicalDevice cLd = mock(CosemLogicalDevice.class);
        when(cLd.getLogicalDevice()).thenReturn(ld);

        mockApduDecodeMethods(mechanismNameFrom(LOW), "guessedPW".getBytes());
        InitiateMessageProcessor processor = spy(new InitiateMessageProcessor(cd, cLd));

        processor.processInitialMessage(null);
    }

    private void mockApduDecodeMethods(MechanismName mechanismName, byte[] password) throws IOException {
        ACSEApdu acseAPdu = new ACSEApdu();
        AARQApdu aarq = new AARQApdu();
        aarq.setApplicationContextName(applicationContextNameFrom(LOGICAL_NAME_REFERENCING_NO_CIPHERING));
        aarq.setMechanismName(mechanismName);
        AuthenticationValue authenticationValue = new AuthenticationValue();
        authenticationValue.setCharstring(new BerOctetString(password));
        aarq.setCallingAuthenticationValue(authenticationValue);
        acseAPdu.setAarq(aarq);

        COSEMpdu cosemPdu = new COSEMpdu();
        InitiateRequest initReq = new InitiateRequest();
        initReq.clientMaxReceivePduSize = new Unsigned16(0);
        Set<ConformanceSetting> clientConformance = new HashSet<>(Arrays.asList(GET, SET, ACTION,
                ConformanceSetting.ATTRIBUTE0_SUPPORTED_WITH_GET, ConformanceSetting.ATTRIBUTE0_SUPPORTED_WITH_SET));
        initReq.proposedConformance = ConformanceSettingConverter.conformanceFor(clientConformance);

        cosemPdu.setInitiateRequest(initReq);
        APdu resultApdu = new APdu(acseAPdu, cosemPdu);
        mockStatic(APdu.class);

        when(APdu.decode(any(byte[].class), any(RawMessageDataBuilder.class))).thenReturn(resultApdu);

        when(APdu.decode(any(byte[].class), any(byte[].class), any(SecuritySuite.class),
                any(RawMessageDataBuilder.class))).thenReturn(resultApdu);
    }

}
