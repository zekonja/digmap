package hr.yottabyte.digmap.signer;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.Properties;

import javax.crypto.Cipher;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;
import com.itextpdf.text.pdf.security.PrivateKeySignature;
import com.itextpdf.text.pdf.security.MakeSignature;
 
public class PdfSigner {
 

    
    /**
     * Sign a PDF
     * @param  src - the original PDF
     * @return signed PDF
     * @throws IOException
     * @throws DocumentException
     * @throws GeneralSecurityException 
     */
    public static ByteArrayOutputStream signPdf(InputStream src, Properties properties)
        throws IOException, DocumentException, GeneralSecurityException {	
    	
		boolean unlimited = Cipher.getMaxAllowedKeyLength("RC5") >= 256;
		if (!unlimited) {
			System.out.println("Please copy Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files to:");
			String securityPath = new java.io.File(System.getProperty("java.home") + "/lib/security").getCanonicalPath();
			System.out.println(securityPath);
		} else
			System.out.println("Unlimited cryptography enabled: " + unlimited);

		Security.addProvider(new BouncyCastleProvider());    	
		
    	//load certificate
        String path = properties.getProperty("KEY_PATH");
        String keystorePassword = properties.getProperty("KEYSTORE_PASSWORD");
        String keyPassword = properties.getProperty("KEY_PASSWORD");
        String type = properties.getProperty("KEY_TYPE");
        String provider = properties.getProperty("SEC_PROVIDER");   
        //add security provider
        if (provider.equalsIgnoreCase("BC")) Security.addProvider(new BouncyCastleProvider());
        KeyStore ks = KeyStore.getInstance(type, provider);        
        ks.load(new FileInputStream(path), keystorePassword.toCharArray());
        String alias = (String)ks.aliases().nextElement();
        PrivateKey pk = (PrivateKey) ks.getKey(alias, keyPassword.toCharArray());
        Certificate[] chain = ks.getCertificateChain(alias);      
        // reader and stamper
        PdfReader reader = new PdfReader(src);   
        
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0');
        // appearance
        PdfSignatureAppearance appearance = stamper .getSignatureAppearance();
        boolean isSignVisible = "true".equalsIgnoreCase(properties.getProperty("SIGN_VISIBLE"));
	if (isSignVisible) {
	    String signImage = properties.getProperty("SIGN_IMAGE");
	    String signReason = properties.getProperty("SIGN_REASON");
	    String signContact = properties.getProperty("SIGN_CONTACT");
	    String signLocation = properties.getProperty("SIGN_LOCATION");
	    appearance.setImage(Image.getInstance(signImage));
	    appearance.setContact(signContact);
	    appearance.setReason(signReason);
	    appearance.setLocation(signLocation);
	    appearance.setVisibleSignature(new Rectangle(10, 10, 30, 30), 1,"Signature");
	}
        // digital signature
        ExternalSignature es = new PrivateKeySignature(pk, "SHA-256", "BC");
        ExternalDigest digest = new BouncyCastleDigest();
        MakeSignature.signDetached(appearance, digest, es, chain, null, null, null, 0, CryptoStandard.CMS);        
        return os;
    };
    
    
    /**
     * Sign a PDF file 
     * @param src - PDF path
     * @return signed PDF      
     * @throws IOException
     * @throws DocumentException
     * @throws GeneralSecurityException 
     */
    public static ByteArrayOutputStream signPdfFile(String srcPath, Properties properties)
        throws IOException, DocumentException, GeneralSecurityException {      	
    	FileInputStream in = new FileInputStream(srcPath);
    	return signPdf(in,properties);
    }
    
    
    

    
    
    
    

    
    /**
     * Sign src PDF file and writes to dest
     * @param src the original PDF
     * @param dest the resulting PDF
     * @throws IOException
     * @throws DocumentException
     * @throws GeneralSecurityException 
     */
    public static void signPdf(String srcPath, String destPath, Properties properties)
        throws IOException, DocumentException, GeneralSecurityException {
    	signPdfFile(srcPath,properties).writeTo(new FileOutputStream(destPath));    	
    }
    
    
    
    
    

}

