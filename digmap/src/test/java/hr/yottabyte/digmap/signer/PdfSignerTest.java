package hr.yottabyte.digmap.signer;

import static org.junit.Assert.*;
import hr.yottabyte.digmap.TestUtils;
import hr.yottabyte.digmap.config.DigMapConfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.security.Security;
import java.util.Properties;

import javax.crypto.Cipher;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;
import org.springframework.core.io.Resource;

public class PdfSignerTest {
	
	

	@Test
	public void testSignPdfInputStreamProperties() throws Exception {
		//create dummy pdf
		ByteArrayInputStream pdf = new ByteArrayInputStream(PdfUtils.createDummyPdf().toByteArray());
		//load properties
		DigMapConfig dmc= TestUtils.loadDigMapConfig();
		//sign pdf file
		ByteArrayOutputStream signedPdf = PdfSigner.signPdf(pdf, dmc.getProperties());
		//test if signature valid
		boolean isPdfSignatureValid = PdfUtils.isPdfSignatureValid(signedPdf.toByteArray(), dmc.getProperties());
		//make assertion
		assertTrue(isPdfSignatureValid);		
	}
	
	@Test
	public void testUnlimitedStrengthJurisdictionPolicy() throws Exception {
		boolean unlimited = Cipher.getMaxAllowedKeyLength("RC5") >= 256;
		if (!unlimited) {
			System.out.println("Please copy Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files to:");
			String securityPath = new java.io.File(System.getProperty("java.home") + "/lib/security").getCanonicalPath();
			System.out.println(securityPath);
		};		
		assertTrue(unlimited);
	}	

	@Test
	public void testSignPdfFile() {
		//TODO
		assertTrue(true);
	}

	@Test
	public void testSignPdfStringStringProperties() {
		//TODO
		assertTrue(true);
	}

	@Test
	public void testAddAttachments() {
		//TODO
		assertTrue(true);
	}

}
