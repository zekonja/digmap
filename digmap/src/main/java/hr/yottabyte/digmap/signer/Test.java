package hr.yottabyte.digmap.signer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.Properties;

import javax.crypto.Cipher;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.mapfish.print.PDFUtils;

import com.itextpdf.text.DocumentException;


public class Test {

	/** The original PDF file */
	public static String ORIGINAL = "./samples/digmap/DigMap.pdf";
	/** The PDF with embedded attachment */
	public static String ORIGINAL_ATT = "./samples/digmap/DigMap-A.pdf";
	/** The signed PDF */
	public static String SIGNED1 = "./samples/digmap/DigMap-signed1.pdf";
	/**
	 * A properties file that is PRIVATE Contains key store and private key
	 * password
	 */
	public static String CONFIG_PATH = "samples/digmap/config.properties";
	/** Some properties used when signing. */
	public static Properties properties = new Properties();
	
    /**
     * Name of the default spring context file.
     */
    public static final String DEFAULT_SPRING_CONTEXT = "/mapfish-cli-spring-application-context.xml";

	
		

	/**
	 * Main method *
	 * 
	 * @param args
	 *            no arguments needed
	 * @throws DocumentException
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public static void main1(String[] args) {
		
				
	
		
		String configFilePath = "res/mfpcfg/config.yaml";		
		String specFilePath = "./res/mfpcfg/requestData.json";
		String outFilePath = "./res/mfpcfg/out.pdf";
		
        
        File configFile = new File (configFilePath);
        File v3ApiRequestFile = new File (specFilePath);
        File outputFile = new File (outFilePath);
        
        String[] cargs = {
                "-config", configFile.getAbsolutePath(),
                "-spec", v3ApiRequestFile.getAbsolutePath(),
                "-output", outputFile.getAbsolutePath()};
        try {
			//
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}       
        
	}
	

        
        
    	public static void main(String[] args) throws IOException,
		DocumentException, GeneralSecurityException {
    	    	System.out.println(new File(".").getAbsolutePath());
    	    	InputStream in;
		ByteArrayOutputStream os;
		String[] attachments = { "C:/Users/khrnjak/workspace/digmap/samples/digmap/feature1.gml", "C:/Users/khrnjak/workspace/digmap/samples/digmap/feature2.gml" };

		System.out.println("Loading properties");
		properties.load(new FileInputStream("C:/Users/khrnjak/workspace/digmap/samples/digmap/digmap_config.properties"));
		// add attachments to PDF file --assume output from mapfish-print
		System.out.println("Adding attachments");
		PdfUtils.createDummyPdf(ORIGINAL);
		os = PdfAttacher.addAttachments(ORIGINAL,attachments,attachments,attachments);
		//convert to input stream
		in = new ByteArrayInputStream(os.toByteArray());
		// sign PDF
		System.out.println("Sign PDF");
		os = PdfSigner.signPdf(in, properties);		
		//print info
		System.out.println("PDF info:");
		PdfUtils.printInfo(os, null, properties);
		// write PDF to filesystem
		System.out.println("PDF written to file: "+new java.io.File(SIGNED1).getCanonicalPath());
		os.writeTo(new FileOutputStream(SIGNED1));
		
	}
}
