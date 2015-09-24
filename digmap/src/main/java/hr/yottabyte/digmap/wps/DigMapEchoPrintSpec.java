
package hr.yottabyte.digmap.wps;



import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.servlet.ServletContext;

import org.geotools.process.factory.DescribeProcess;
import org.geotools.process.factory.DescribeResult;
import org.geoserver.wps.gs.GeoServerProcess;
import org.geoserver.wps.process.StreamRawData;
import org.json.JSONException;
import org.json.JSONWriter;
import org.mapfish.print.MapPrinter;
import org.springframework.core.io.ResourceLoader;

@DescribeProcess(title = "DigMapEchoPrintSpec", description = "DigMap - Echo Print Spec")
public class DigMapEchoPrintSpec implements GeoServerProcess {

	private MapPrinter mapPrinter;
	private ResourceLoader resourceLoader;
	private ServletContext servletContext;

	public DigMapEchoPrintSpec(MapPrinter mapPrinter, ResourceLoader resourceLoader,ServletContext servletContext) {
		this.mapPrinter = mapPrinter;
		this.resourceLoader = resourceLoader;		
		this.servletContext = servletContext;
	}

	@SuppressWarnings("unchecked")
	@DescribeResult(name = "digmap", description = "Get Print Config JSON", meta = {
			"mimeTypes=application/json"})
	public String execute() throws IOException {	
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
		return jsonString;
	}
}
