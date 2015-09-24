package hr.yottabyte.digmap.signer;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfDate;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfFileSpecification;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfString;
import com.itextpdf.text.pdf.PdfWriter;

 
public class PdfAttacher {
 


   
    

    /**
     * Write PDF to file
     * @param pdf byte array
     * @param filename the path to the new PDF document
     * @throws IOException 
     */
    public static void writePdfToFile(ByteArrayOutputStream pdf, String filename) throws IOException {
        FileOutputStream out = new FileOutputStream(filename);
        pdf.writeTo(out);
    }    

    public static ByteArrayOutputStream addAttachments(InputStream src,
	    InputStream[] attachments, String[] displays, String[] descriptions)
	    throws IOException, DocumentException {
	PdfReader reader = new PdfReader(src);
	ByteArrayOutputStream os = new ByteArrayOutputStream();
	PdfStamper stamper = new PdfStamper(reader, os);
	PdfWriter writer = stamper.getWriter();
	for (int i = 0; i < attachments.length; i++) {
	    String display = "";
	    if (displays != null)
		if (displays[i] != null)
		    display = displays[i];
	    String description = "";
	    if (descriptions != null)
		if (descriptions[i] != null)
		    description = descriptions[i];

	    PdfDictionary pdfDictionary = new PdfDictionary();
	    Calendar cal = Calendar.getInstance();
	    pdfDictionary.put(PdfName.CREATIONDATE, new PdfDate(cal));
	    pdfDictionary.put(PdfName.AUTHOR, new PdfString("yottabyte"));
	    pdfDictionary.put(PdfName.CREATOR, new PdfString("yottabyte"));
	    PdfFileSpecification fs = PdfFileSpecification.fileEmbedded(writer,
		    null, display, getBytesFromInputStream(attachments[i]),true,
		    "text/xml", pdfDictionary);

	    writer.addFileAttachment(description, fs);

	}
	stamper.close();
	return os;
    }
	
	
	
	public static ByteArrayOutputStream addAttachments(String src, String[] attachments, String[] displays, String[] descriptions)
			throws IOException, DocumentException {
		InputStream in = new FileInputStream(src);
		ArrayList<InputStream> att = new ArrayList<InputStream>();
		for (int i = 0; i < attachments.length; i++) {
			att.add(new FileInputStream(attachments[i]));
		}
		InputStream[] fileAttachments = new InputStream[attachments.length];
		att.toArray(fileAttachments);
		ByteArrayOutputStream os= addAttachments(in,fileAttachments,displays,descriptions);
		return os;
	}	
	
	
    
	public static void addAttachments(String src, String dest, String[] attachments, String[] displays, String[] descriptions)
			throws IOException, DocumentException {
		ByteArrayOutputStream os= addAttachments(src,attachments,displays,descriptions);
		writePdfToFile(os, dest);
	}
	
    
	
	public static byte[] getBytesFromInputStream(InputStream is) throws IOException
	{
	    try (ByteArrayOutputStream os = new ByteArrayOutputStream();)
	    {
	        byte[] buffer = new byte[0xFFFF];

	        for (int len; (len = is.read(buffer)) != -1;)
	            os.write(buffer, 0, len);

	        os.flush();

	        return os.toByteArray();
	    }
	}	
     

}

