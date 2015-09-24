package hr.yottabyte.digmap.wps;



import hr.yottabyte.digmap.signer.PdfUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

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

@DescribeProcess(title = "DigMapEchoPdf", description = "DigMap - Echo JSON to PDF file")
public class DigMapEchoPdf implements GeoServerProcess {

	private MapPrinter mapPrinter;
	private ResourceLoader resourceLoader;	

	public DigMapEchoPdf(MapPrinter mapPrinter, ResourceLoader resourceLoader) {
		this.mapPrinter = mapPrinter;
		this.resourceLoader = resourceLoader;		
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
		
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> jsonMap = null;
		try {
			jsonMap = mapper.readValue(input.getInputStream(),Map.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String json = jsonMap.toString();
		ByteArrayOutputStream pdf = null;
		try {
			pdf = PdfUtils.createEchoPdf(json);
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
