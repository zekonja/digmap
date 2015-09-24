package hr.yottabyte.digmap.wps;

import hr.yottabyte.digmap.signer.PdfUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;
import org.geotools.process.factory.DescribeParameter;
import org.geotools.process.factory.DescribeProcess;
import org.geotools.process.factory.DescribeResult;
import org.geoserver.wps.gs.GeoServerProcess;
import org.geoserver.wps.process.FileRawData;
import org.geoserver.wps.process.RawData;
import org.geoserver.wps.process.StreamRawData;
import org.mapfish.print.MapPrinter;
import org.mapfish.print.ShellMapPrinter;
import org.springframework.core.io.ResourceLoader;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.DocumentException;

@DescribeProcess(title = "DigitalMapExcerpt", description = "DigMap - create digital map excerpt.")
public class DigitalMapExcerpt implements GeoServerProcess {

	
	private MapPrinter mapPrinter;
	private ResourceLoader resourceLoader;	
	private ServletContext servletContext;

	public DigitalMapExcerpt(MapPrinter mapPrinter, ResourceLoader resourceLoader,ServletContext servletContext) {
		this.mapPrinter = mapPrinter;
		this.resourceLoader = resourceLoader;		
		this.servletContext = servletContext;
	}	

	@SuppressWarnings("unchecked")
	@DescribeResult(name = "result", description = "DigitalMapExcerpt signed PDF file with embedded vector data", meta = { "mimeTypes=application/pdf" })
	public FileRawData execute(
			@DescribeParameter(name = "spec", description = "Input JSON MFP DigMap spec", meta = { "mimeTypes=application/json" }) final RawData input) {


		//read config DM
		
		String configFilePath = servletContext.getRealPath("/data/printing/config.yaml");
		String configDMFilePath = servletContext.getRealPath("/data/printing/digmap/digmap_config.properties");
		
		File configFile = new File(configFilePath);
		File configDMFile = new File(configDMFilePath);
		
		File v2ApiRequestFile = null;
		File outputFile = null;
		
		try {
			v2ApiRequestFile = File.createTempFile("temp-file-spec", ".tmp");
			outputFile = File.createTempFile("temp-file-out", ".pdf");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		
		InputStream inSpec = null;
		FileOutputStream outSpec = null;
		try {
			outSpec = new FileOutputStream(v2ApiRequestFile);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			inSpec = input.getInputStream();			
			IOUtils.copy(inSpec,outSpec);
			inSpec.close();
			outSpec.close();			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String[] cargs = { "--config=" + configFile.getAbsolutePath(),
				"--configDM=" + configDMFile.getAbsolutePath(),
				"--spec=" + v2ApiRequestFile.getAbsolutePath(),
				"--output=" + outputFile.getAbsolutePath() };

		for (String s : cargs)
			System.out.println(s);

		try {
			ShellMapPrinter.main(cargs);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("DigitialMapExcerpt file: "+outputFile.getAbsolutePath());
		FileRawData out = new FileRawData(outputFile,"application/pdf", "pdf");
		return out;
	}

}
