package hr.yottabyte.digmap.signer;

import javax.crypto.Cipher;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
 
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfFileSpecification;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.CertificateInfo;
import com.itextpdf.text.pdf.security.CertificateVerification;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;
import com.itextpdf.text.pdf.security.PrivateKeySignature;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import com.itextpdf.text.pdf.security.VerificationException;
 
public class Signatures {
 
    /** The resulting PDF */
    public static String ORIGINAL = "res/DigMap.pdf";
    /** The resulting PDF */
    public static String ORIGINAL_ATT = "res/DigMap-A.pdf";    
    /** The resulting PDF */
    public static String SIGNED1 = "res/DigMap-signed1.pdf";
    /** The resulting PDF */
    public static String SIGNED2 = "res/DigMap-signed2.pdf";
    /** Info after verification of a signed PDF */
    public static String VERIFICATION = "res/verify.txt";
    /** The resulting PDF */
    public static String REVISION = "res/DigMap-rev.pdf";
 
    /**
     * A properties file that is PRIVATE.
     * You should make your own properties file and adapt this line.
     */
    public static String CONFIG_PATH = "res/config.properties";
    /** Some properties used when signing. */
    public static Properties properties = new Properties();

    /**
     * Creates a dummy PDF document with paragraph Hello world
     * @param filename the path to the new PDF document
     * @throws DocumentException 
     * @throws IOException 
     */
    public static void createDummyPdf(String filename) throws IOException, DocumentException {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filename));
        document.open();
        document.add(new Paragraph("Hello World!"));
        document.close();
    }
    
    
    /**
     * Sign a PDF
     * @param src the original PDF
     * @param dest 
     * @return signed PDF
     * @throws IOException
     * @throws DocumentException
     * @throws GeneralSecurityException 
     */
    public ByteArrayOutputStream signPdf(InputStream src)
        throws IOException, DocumentException, GeneralSecurityException {
    	//load certificate
        String path = properties.getProperty("KEY_PATH");
        String keystorePassword = properties.getProperty("KEYSTORE_PASSWORD");
        String keyPassword = properties.getProperty("KEY_PASSWORD");
        String type = properties.getProperty("KEY_TYPE");
        String provider = properties.getProperty("SEC_PROVIDER");        		
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
        String signImage = properties.getProperty("SIGN_IMAGE");
        String signReason = properties.getProperty("SIGN_REASON");
        String signContact = properties.getProperty("SIGN_CONTACT");
        String signLocation  = properties.getProperty("SIGN_LOCATION");        
        PdfSignatureAppearance appearance = stamper .getSignatureAppearance();
        appearance.setImage(Image.getInstance(signImage));
        appearance.setContact(signContact);                
        appearance.setReason(signReason);
        appearance.setLocation(signLocation);        
        appearance.setVisibleSignature(new Rectangle(10, 10, 30, 30), 1, "Signature");
        // digital signature
        ExternalSignature es = new PrivateKeySignature(pk, "SHA-256", "BC");
        ExternalDigest digest = new BouncyCastleDigest();
        MakeSignature.signDetached(appearance, digest, es, chain, null, null, null, 0, CryptoStandard.CMS);        
        return os;
    };
    
    
    /**
     * Manipulates a PDF file src with the file dest as result
     * @param src the original PDF
     * @param dest the resulting PDF
     * @throws IOException
     * @throws DocumentException
     * @throws GeneralSecurityException 
     */
    public ByteArrayOutputStream signPdfFile(String srcPath)
        throws IOException, DocumentException, GeneralSecurityException {   	
    	FileInputStream in = new FileInputStream(srcPath);
    	return signPdf(in);
    }
    
    /**
     * Creates a PDF document.
     * @param filename the path to the new PDF document
     * @throws IOException 
     */
    public void writePdfToFile(ByteArrayOutputStream pdf, String filename) throws IOException {
        FileOutputStream out = new FileOutputStream(filename);
        pdf.writeTo(out);
    }    
    
    
    
    /**
     * Manipulates a PDF file src with the file dest as result
     * @param src the original PDF
     * @param dest the resulting PDF
     * @throws IOException
     * @throws DocumentException
     * @throws GeneralSecurityException 
     */
    public void signPdf(String src, String dest)
        throws IOException, DocumentException, GeneralSecurityException {
    	writePdfToFile(signPdfFile(src),dest);
    }
     

    
    
 
    /**
     * Manipulates a PDF file src with the file dest as result
     * @param src the original PDF
     * @param dest the resulting PDF
     * @throws IOException
     * @throws DocumentException
     * @throws GeneralSecurityException 
     */
    public void signPdfOrig(String src, String dest)
        throws IOException, DocumentException, GeneralSecurityException {
    	
    	//load certificate
        String path = properties.getProperty("PRIVATE");
        String keystore_password = properties.getProperty("PASSWORD");
        String key_password = properties.getProperty("PASSWORD");
        KeyStore ks = KeyStore.getInstance("pkcs12", "BC");
        ks.load(new FileInputStream(path), keystore_password.toCharArray());
        String alias = (String)ks.aliases().nextElement();
        PrivateKey pk = (PrivateKey) ks.getKey(alias, key_password.toCharArray());
        Certificate[] chain = ks.getCertificateChain(alias);      
        // reader and stamper
        PdfReader reader = new PdfReader(src);        
        FileOutputStream os = new FileOutputStream(dest);
        PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0');
        // appearance
        PdfSignatureAppearance appearance = stamper .getSignatureAppearance();
        String image =  properties.getProperty("SIGN_IMAGE");
        appearance.setImage(Image.getInstance(image));
        appearance.setReason("Validity confirmation");
        //appearance.setLocation("Velika Gorica, Croatia");
        appearance.setVisibleSignature(new Rectangle(100, 100, 140, 140), 1, "signature");
        // digital signature
        ExternalSignature es = new PrivateKeySignature(pk, "SHA-256", "BC");
        ExternalDigest digest = new BouncyCastleDigest();
        MakeSignature.signDetached(appearance, digest, es, chain, null, null, null, 0, CryptoStandard.CMS);
    }
 
    /**
     * Manipulates a PDF file src with the file dest as result
     * @param src the original PDF
     * @param dest the resulting PDF
     * @throws IOException
     * @throws DocumentException
     * @throws GeneralSecurityException 
     */
    public void signPdfSecondTime(String src, String dest)
        throws IOException, DocumentException, GeneralSecurityException {
        String path = "res/fiskaltest0.pfx";
        String keystore_password = "fiskaltest";
        String key_password = "fiskaltest";
        KeyStore ks = KeyStore.getInstance("pkcs12", "BC");
        ks.load(new FileInputStream(path), keystore_password.toCharArray());
        String alias = (String)ks.aliases().nextElement();
        PrivateKey pk = (PrivateKey) ks.getKey(alias, key_password.toCharArray());
        Certificate[] chain = ks.getCertificateChain(alias);
        // reader / stamper
        PdfReader reader = new PdfReader(src);
        FileOutputStream os = new FileOutputStream(dest);
        PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0', null, true);
        // appearance
        PdfSignatureAppearance appearance = stamper
                .getSignatureAppearance();
        appearance.setReason("I'm approving this.");
        appearance.setReason("I'm approving this.");
        appearance.setLocation("Foobar");
        appearance.setVisibleSignature(new Rectangle(160, 732, 232, 780), 1, "second");
        // digital signature
        ExternalSignature es = new PrivateKeySignature(pk, "SHA-256", "BC");
        ExternalDigest digest = new BouncyCastleDigest();
        MakeSignature.signDetached(appearance, digest, es, chain, null, null, null, 0, CryptoStandard.CMS);
 
    }
 
    /**
     * Print out info about the signatures of a PDF
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public void printInfo(PrintStream out) throws GeneralSecurityException, IOException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        CertificateFactory cf = CertificateFactory.getInstance("X509");
        FileInputStream is1 = new FileInputStream(properties.getProperty("ROOTCERT"));
        X509Certificate cert1 = (X509Certificate) cf.generateCertificate(is1);
        ks.setCertificateEntry("cacert", cert1);
        PdfReader reader = new PdfReader(SIGNED1);
        AcroFields af = reader.getAcroFields();
        ArrayList<String> names = af.getSignatureNames();
        for (String name : names) {
            out.println("Signature name: " + name);
            out.println("Signature covers whole document: " + af.signatureCoversWholeDocument(name));
            out.println("Document revision: " + af.getRevision(name) + " of " + af.getTotalRevisions());
            PdfPKCS7 pk = af.verifySignature(name);
            Calendar cal = pk.getSignDate();
            Certificate[] pkc = pk.getCertificates();
            out.println("Subject: " + CertificateInfo.getSubjectFields(pk.getSigningCertificate()));
            out.println("Is valid: " + pk.verify());
            List<VerificationException> errors = CertificateVerification.verifyCertificates(pkc, ks, null, cal);
            if (errors.size() == 0)
                out.println("Certificates verified against the KeyStore");
            else
                out.println(errors);    
        }
        out.flush();
        out.close();
    }
    
    
    /**
     * Print out info about the signatures of a PDF
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public void printInfo2(PrintStream out) throws GeneralSecurityException, IOException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());        
        ks.load(new FileInputStream(System.getProperty("javax.net.ssl.trustStore")),"changeit".toCharArray());
        CertificateFactory cf = CertificateFactory.getInstance("X509");
        PdfReader reader = new PdfReader(SIGNED1);
        AcroFields af = reader.getAcroFields();
        ArrayList<String> names = af.getSignatureNames();
        for (String name : names) {
            out.println("Signature name: " + name);
            out.println("Signature covers whole document: " + af.signatureCoversWholeDocument(name));
            out.println("Document revision: " + af.getRevision(name) + " of " + af.getTotalRevisions());
            PdfPKCS7 pk = af.verifySignature(name);
            Calendar cal = pk.getSignDate();
            Certificate[] pkc = pk.getCertificates();
            out.println("Subject: " + CertificateInfo.getSubjectFields(pk.getSigningCertificate()));
            out.println("Is valid: " + pk.verify());
            List<VerificationException> errors = CertificateVerification.verifyCertificates(pkc, ks, null, cal);
            if (errors.size() == 0)
                out.println("Certificates verified against the KeyStore");
            else
                out.println(errors);    
        }
        out.flush();
        out.close();
    }
     
 
    /**
     * Extracts the first revision of a PDF we've signed twice.
     * @throws IOException
     */
    public void extractFirstRevision() throws IOException {
        PdfReader reader = new PdfReader(SIGNED2);
        AcroFields af = reader.getAcroFields();
        FileOutputStream os = new FileOutputStream(REVISION);
        byte bb[] = new byte[1028];
        InputStream ip = af.extractRevision("first");
        int n = 0;
        while ((n = ip.read(bb)) > 0)
            os.write(bb, 0, n);
        os.close();
        ip.close();
    }
    
    
	public void addAttachments(String src, String dest, String[] attachments)
			throws IOException, DocumentException {
		PdfReader reader = new PdfReader(src);
		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
		for (int i = 0; i < attachments.length; i++) {
			addAttachment(stamper.getWriter(), new File(attachments[i]));
		}
		stamper.close();
	}

	protected void addAttachment(PdfWriter writer, File src) throws IOException {
		PdfFileSpecification fs = PdfFileSpecification.fileEmbedded(writer,src.getAbsolutePath(),src.getName(), null);
		writer.addFileAttachment("Desc "+src.getName().substring(0, src.getName().indexOf('.')), fs);
	}
 
    /**
     * Main method     *
     * @param    args    no arguments needed
     * @throws DocumentException 
     * @throws IOException
     * @throws GeneralSecurityException 
     */
    public static void main(String[] args)
        throws IOException, DocumentException, GeneralSecurityException {
    	
    	System.setProperty("javax.net.ssl.trustStore","C:/Java/jdk1.7.0_21/jre/lib/security/cacerts");
    	//System.setProperty("javax.net.ssl.trustStore","C:/Program Files/Java/jre1.8.0_45/lib/security/cacerts");
    	//System.setProperty("javax.net.ssl.trustStore","res/yottabyte.pfx");
    	
    	
    	System.out.println(System.getProperty("javax.net.ssl.trustStore"));
    	
    	
    	 String[] attachments =  {"res/feature1.gml", "res/feature2.gml"};
    	
    	 boolean unlimited = Cipher.getMaxAllowedKeyLength("RC5") >= 256;
    	 if (!unlimited) {
    		System.out.println(System.getProperty("java.home"));
    		System.out.println(System.getProperty("javax.net.ssl.trustStore"));
    	 }
    	 else System.out.println("Unlimited cryptography enabled: " + unlimited);    	
    	
        Security.addProvider(new BouncyCastleProvider());
        properties.load(new FileInputStream(CONFIG_PATH));
        Signatures signatures = new Signatures();
        
        signatures.addAttachments(ORIGINAL, ORIGINAL_ATT, attachments);
        signatures.signPdf(ORIGINAL_ATT, SIGNED1);
        System.out.println("SIGNED");        
        signatures.printInfo2(System.out);
        
        signatures.signPdfSecondTime(SIGNED1, SIGNED2);
//        signatures.verifySignatures();
//        signatures.extractFirstRevision();
    }
}

