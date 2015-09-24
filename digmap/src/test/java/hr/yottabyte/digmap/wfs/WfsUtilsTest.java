package hr.yottabyte.digmap.wfs;

import static org.junit.Assert.*;
import hr.yottabyte.digmap.TestUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;
import org.mapfish.print.MapPrinter;
import org.springframework.beans.factory.annotation.Autowired;

public class WfsUtilsTest {
	
    @Autowired
    private MapPrinter mapPrinter;
//    @Autowired
//    private Main main;    
//    @Autowired
//    private ConfigurationFactory configurationFactory; 

//	@Test
//	public void testGetCRS() {
//		fail("Not yet implemented");
//	}

//	@Test
//	public void testGetLayersConfig()  {
//		
//		String configFilePath = "C:/Users/khrnjak/workspace/digmap/target/test-classes/config.yaml";		
//		String specFilePath = "C:/Users/khrnjak/workspace/digmap/target/test-classes/requestData.json";
//		String outFilePath = "C:/Users/khrnjak/workspace/digmap/target/test-classes/out.pdf";
//		
//
//		
//		
////        InputStream inFile;
////		try {
////			inFile = new FileInputStream(specFilePath);
////	        final String jsonConfiguration = CharStreams.toString(new InputStreamReader(inFile, Constants.DEFAULT_ENCODING));
////	        PJsonObject jsonSpec = MapPrinter.parseSpec(jsonConfiguration);		
////	        System.out.println("Request Data: \n" + jsonSpec.getInternalObj().toString(2));		
////	        //System.out.println("Bbox: \n" + jsonSpec.getJSONArray("attributes").toString());	
////		} catch (Exception e1) {
////			// TODO Auto-generated catch block
////			e1.printStackTrace();
////		}
//		
//
//        
//        File configFile = new File (configFilePath);
//        File v3ApiRequestFile = new File (specFilePath);
//        File outputFile = new File (outFilePath);
//        
//        System.out.println("sds1");
//        
//
//           
//        
//        String[] cargs = {
//                "-config", configFile.getAbsolutePath(),
//                "-spec", v3ApiRequestFile.getAbsolutePath(),
//                "-output", outputFile.getAbsolutePath()};
//        try {        	
//			Main.main(cargs);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}       
//        
//        if (true) return;
//		System.out.println("sds");
//			System.out.println("sds");
//			System.out.println("sds");
//			if (this.main==null) System.out.println("null");
//			if (this.mapPrinter==null) System.out.println("null"); 			
//			ClassLoader classLoader = this.getClass().getClassLoader();
//		    //File configFile = new File(classLoader.getResource(TestUtils.MFP_CONFIG_FILE).getFile());		
//		    System.out.println(configFile.getAbsolutePath());			
//			Configuration configuration;
//			try {
//				configuration = configurationFactory.getConfig(configFile);
//				System.out.println(configuration.getOutputFilename());
//			} catch (IOException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//			
//			
//			System.out.println("sds");
//			System.out.println("sds");
//			PJsonObject jsonSpec;
//			try {
//				jsonSpec = TestUtils.loadMapFishPrintSpec();
//				String projection = jsonSpec.getJSONObject("attributes").getJSONObject("map").getString("projection");
//				assertEquals("EPSG:4326", projection);
//				System.out.println(jsonSpec.toString());
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//		
//		
//	}

//	@Test
//	public void testWriteGMLtoFileStringString() {
//		fail("Not yet implemented");
//	}

	@Test
	public void testWriteGMLtoFileURLFile() throws Exception   {		
		String baseUrl = "http://digmap-lab.fiware.yottabyte.hr/geoserver";
		String wfsRequest = "/wfs?request=GetFeature&version=1.1.0&typeName=topp:states&BBOX=-74,40,-73.9,40.3,EPSG:4326";
		URL url = new URL(baseUrl + wfsRequest);
		File temp = File.createTempFile("temp-file-name", ".tmp");
		WfsUtils.writeGMLtoFile(url, temp);
	    BufferedReader br = new BufferedReader(new FileReader(temp));
	    String line = br.readLine();
	    assertTrue(line.contains("wfs:FeatureCollection"));
		
	}

}
