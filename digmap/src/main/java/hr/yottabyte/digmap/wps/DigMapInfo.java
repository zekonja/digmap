
package hr.yottabyte.digmap.wps;



import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.geotools.process.factory.DescribeProcess;
import org.geotools.process.factory.DescribeResult;
import org.geoserver.wps.gs.GeoServerProcess;
import org.geoserver.wps.process.StreamRawData;
import org.geoserver.wps.process.StringRawData;
import org.json.JSONException;
import org.json.JSONWriter;
import org.mapfish.print.MapPrinter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@DescribeProcess(title = "DigMapInfo", description = "DigMap - Echo Print Config")
public class DigMapInfo implements GeoServerProcess {

	private MapPrinter mapPrinter;
	private ResourceLoader resourceLoader;
	private ServletContext servletContext;

	public DigMapInfo(MapPrinter mapPrinter, ResourceLoader resourceLoader,ServletContext servletContext) {
		this.mapPrinter = mapPrinter;
		this.resourceLoader = resourceLoader;		
		this.servletContext = servletContext;
	}

	@SuppressWarnings("unchecked")
	@DescribeResult(name = "digmap", description = "Get print configuration", meta = {
			"mimeTypes=application/json"})
	public StringRawData execute() throws IOException {	
		String configFilePath = servletContext.getRealPath("/data/printing/config.yaml");        
		mapPrinter.setYamlConfigFile(new File(configFilePath));		
		StringWriter writer = new StringWriter();
		JSONWriter jsonWriter = new JSONWriter(writer);
		try {
			jsonWriter.object();
			mapPrinter.printClientConfig(jsonWriter);
			jsonWriter.endObject();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String jsonString = writer.getBuffer().toString();		
		StringRawData out = new StringRawData(jsonString,"application/json");
		return out;
	};



}
