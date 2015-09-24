package hr.yottabyte.digmap.signer;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.security.CertificateInfo;
import com.itextpdf.text.pdf.security.CertificateVerification;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import com.itextpdf.text.pdf.security.VerificationException;
 
public class PdfUtils {
 

	 /**
     * Print out info about the signatures of a PDF
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public static boolean isPdfSignatureValid(byte[] pdf, Properties properties) throws GeneralSecurityException, IOException {
    	boolean isValid=true;
    	//load keystore and read certificate
        String path = properties.getProperty("KEY_PATH");
        String keystorePassword = properties.getProperty("KEYSTORE_PASSWORD");        
        String type = properties.getProperty("KEY_TYPE");
        String provider = properties.getProperty("SEC_PROVIDER");   
        //add security provider
        if (provider.equalsIgnoreCase("BC")) Security.addProvider(new BouncyCastleProvider());        
        KeyStore ks = KeyStore.getInstance(type, provider);        
        ks.load(new FileInputStream(path), keystorePassword.toCharArray());
        String alias = (String)ks.aliases().nextElement();        
        X509Certificate certificate = (X509Certificate)ks.getCertificate(alias);
        //create new trusted certificate collection
        KeyStore kst = KeyStore.getInstance(KeyStore.getDefaultType());
        kst.load(null, null);
        kst.setCertificateEntry("cacert", certificate);
        //read PDF
        PdfReader reader = new PdfReader(pdf);
        AcroFields af = reader.getAcroFields();
        //check signatures
        ArrayList<String> names = af.getSignatureNames();
        for (String name : names) {
            PdfPKCS7 pk = af.verifySignature(name);
            Calendar cal = pk.getSignDate();
            Certificate[] pkc = pk.getCertificates();
            isValid = isValid & pk.verify();
            List<VerificationException> errors = CertificateVerification.verifyCertificates(pkc, kst, cal);
            if (errors.size() > 0) return false;
        }
        return isValid;
    }
 

	
    /**
     * Print out info about the signatures of a PDF
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public static void printInfo(ByteArrayOutputStream pdf, PrintStream out, Properties properties) throws GeneralSecurityException, IOException {
    	if (out == null) out=System.out;
    	//load keystore and read certificate
        String path = properties.getProperty("KEY_PATH");
        String keystorePassword = properties.getProperty("KEYSTORE_PASSWORD");        
        String type = properties.getProperty("KEY_TYPE");
        String provider = properties.getProperty("SEC_PROVIDER");
        //add security provider
        if (provider.equalsIgnoreCase("BC")) Security.addProvider(new BouncyCastleProvider());        
        KeyStore ks = KeyStore.getInstance(type, provider);        
        ks.load(new FileInputStream(path), keystorePassword.toCharArray());
        String alias = (String)ks.aliases().nextElement();        
        X509Certificate certificate = (X509Certificate)ks.getCertificate(alias);
        //create new trusted certificate collection
        KeyStore kst = KeyStore.getInstance(KeyStore.getDefaultType());
        kst.load(null, null);
        kst.setCertificateEntry("cacert", certificate);
        //read PDF
        PdfReader reader = new PdfReader(pdf.toByteArray());
        AcroFields af = reader.getAcroFields();
        //check signatures
        ArrayList<String> names = af.getSignatureNames();
        for (String name : names) {
            out.println(">Signature name: " + name);
            out.println(">Signature covers whole document: " + af.signatureCoversWholeDocument(name));
            out.println(">Document revision: " + af.getRevision(name) + " of " + af.getTotalRevisions());
            PdfPKCS7 pk = af.verifySignature(name);
            Calendar cal = pk.getSignDate();
            Certificate[] pkc = pk.getCertificates();
            out.println(">Subject: " + CertificateInfo.getSubjectFields(pk.getSigningCertificate()));
            out.println(">Is valid: " + pk.verify());
            List<VerificationException> errors = CertificateVerification.verifyCertificates(pkc, kst, cal);
            if (errors.size() == 0)
                out.println(">Certificates verified against the KeyStore");
            else
                out.println(errors);    
        }
        out.flush();
        if (!out.equals(System.out)) out.close();
    }
    
    
    public static OutputStream getOutputStream(final String output, final String suffix) {
        String outputPath = output;
        final OutputStream outFile;
        if (outputPath != null) {
            if (!outputPath.endsWith("." + suffix)) {
                outputPath = outputPath + "." + suffix;
            }
            try {
				outFile = new FileOutputStream(outputPath);
				return outFile;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } else {
            //noinspection UseOfSystemOutOrSystemErr
            outFile = System.out;
        }
        
       return null;
        
    }

    public static InputStream getInputStream(final String spec) throws FileNotFoundException {
        final InputStream file;
        if (spec != null) {
            file = new FileInputStream(spec);
        } else {
            file = System.in;
        }
        return file;
    }	
    
    /**
     * Creates a dummy PDF document with paragraph Hello world
     * @param filename the path to the new PDF document
     * @throws DocumentException 
     * @throws IOException 
     */
    public static void createDummyPdf(String filename) throws IOException, DocumentException {
    	ByteArrayOutputStream pdf = createDummyPdf();
    	writePdfToFile(pdf, filename);
    }    
    
    /**
     * Creates a dummy PDF document with paragraph Hello world
     * @throws DocumentException 
     * @throws IOException 
     */
    public static ByteArrayOutputStream createDummyPdf() throws IOException, DocumentException {
        Document document = new Document();
        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, pdf);
        document.open();
        document.add(new Paragraph("Hello World!"));
        document.close();
        return pdf;
    } 
    
	public static ByteArrayOutputStream createEchoPdf(String message)
			throws DocumentException, IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		// step 1
		Document document = new Document();
		// step 2
		PdfWriter.getInstance(document, out);
		// step 3
		document.open();
		// step 4
		document.add(new Paragraph(message));
		// step 5
		document.close();
		// return
		return out;
	}
    
    /**
     * Creates a PDF document.
     * @param filename the path to the new PDF document
     * @throws IOException 
     */
    public static void writePdfToFile(ByteArrayOutputStream pdf, String filename) throws IOException {
        FileOutputStream out = new FileOutputStream(filename);
        pdf.writeTo(out);
    }    
    
        
       
}

