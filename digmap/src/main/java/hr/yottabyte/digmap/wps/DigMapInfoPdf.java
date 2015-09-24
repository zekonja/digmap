package hr.yottabyte.digmap.wps;



import hr.yottabyte.digmap.signer.PdfUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.geotools.process.factory.DescribeParameter;
import org.geotools.process.factory.DescribeProcess;
import org.geotools.process.factory.DescribeResult;
import org.geoserver.wps.gs.GeoServerProcess;
import org.geoserver.wps.process.RawData;
import org.geoserver.wps.process.StreamRawData;
import org.mapfish.print.MapPrinter;
import org.springframework.core.io.ResourceLoader;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.DocumentException;

@DescribeProcess(title = "DigMapInfoPdf", description = "DigMap - Echo JSON to PDF file")
public class DigMapInfoPdf implements GeoServerProcess {

	private MapPrinter mapPrinter;
	private ResourceLoader resourceLoader;	
	private ServletContext servletContext;

	public DigMapInfoPdf(MapPrinter mapPrinter, ResourceLoader resourceLoader,ServletContext servletContext) {
		this.mapPrinter = mapPrinter;
		this.resourceLoader = resourceLoader;		
		this.servletContext = servletContext;
	}


	@SuppressWarnings("unchecked")
	@DescribeResult(name = "digmap", description = "Echo JSON to PDF file", meta = {
			"mimeTypes=application/pdf"})
	public StreamRawData execute(
			@DescribeParameter(
					name = "data", 
					description = "Input json spec", 
					meta = { "mimeTypes=application/json" }) 
			final RawData input
			) {
		

		String info = "";
		info +=  "\n REAL getContextPath: +"+servletContext.getContextPath();
		info +=  "\n REAL getInitParameter: +"+servletContext.getInitParameter("digmap-config");
		info +=  "\n REAL getRealPath: +"+servletContext.getRealPath("/");
		info +=  "\n REAL getRealPath digmap: +"+servletContext.getRealPath("/data/printing/digmap/");
		info +=  "\n REAL getServerInfo: +"+servletContext.getServerInfo();
		info +=  "\n REAL getServletContextName: +"+servletContext.getServletContextName();
		List<String> arrlist = new ArrayList<String>();
		arrlist = Collections.list(servletContext.getInitParameterNames());
		info +=  "\n REAL getInitParameterNames: +"+arrlist;
		info +=  "\n REAL getInitParameterNames: +"+servletContext.getInitParameterNames().toString();

		
		
		
		
		ByteArrayOutputStream pdf = null;
		try {
			pdf = PdfUtils.createEchoPdf(info);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ByteArrayInputStream in = new ByteArrayInputStream(pdf.toByteArray());
		StreamRawData out = new StreamRawData("application/pdf", in, "pdf");
		return out;
	}


}
