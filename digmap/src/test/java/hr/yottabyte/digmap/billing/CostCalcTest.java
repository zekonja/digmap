package hr.yottabyte.digmap.billing;

import static org.geotools.referencing.crs.DefaultGeographicCRS.WGS84;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import hr.yottabyte.digmap.TestUtils;
import hr.yottabyte.digmap.config.DigMapConfig;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.junit.Test;

import com.vividsolutions.jts.geom.Envelope;

public class CostCalcTest {
	


	@Test
	public void testCalcDataCostStringStringArrayEnvelopeDigMapConfig()  {
//		Logger.getRootLogger().setLevel(Level.DEBUG);
//		DigMapConfig dmc=null;
//		try {
//			dmc = TestUtils.loadDigMapConfig();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		String baseURL = "http://digmap-lab.fiware.yottabyte.hr:80/geoserver/wfs";
//		String layerList = "topp:states,tiger:poly_landmarks";
//		ReferencedEnvelope referencedEnvelope = 
//				new ReferencedEnvelope(-74.20194131464042, -73.76205868535958, 40.68223697055791, 40.82376215900582, WGS84);
//		double cost=0;
//		
//		System.out.println("getBaseURL"+baseURL);
//		System.out.println("List"+Arrays.toString(layerList.split(",")));
//		System.out.println("getReferencedEnvelope"+referencedEnvelope.toString());		
//		try {
//			cost = CostCalc.calcDataCost(baseURL, layerList.split(","), referencedEnvelope, dmc);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}	
//		assertEquals(3.176318819054861E12, cost, 10);
//		fail("Not yet implemented");
		
	}

	@Test
	public void testCalcDataCostSimpleFeatureCollectionCoordinateReferenceSystemDigMapConfig() {
		//fail("Not yet implemented");
	}

	@Test
	public void testCalcDataCostStringCoordinateReferenceSystemDigMapConfig()  {
		ClassLoader classLoader = this.getClass().getClassLoader();
	    File file = new File(classLoader.getResource("wfs.xml").getFile());
	    String wfsGML = file.getAbsolutePath();
		DigMapConfig dc=null;
		try {
			dc = TestUtils.loadDigMapConfig();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    double cost = 0;
		try {
			cost = CostCalc.getDataCost(wfsGML,WGS84,dc.getCost_per_point(),
				dc.getCost_per_line(), dc.getCost_per_polygon(),
				dc.getCost_per_area(),dc.getCost_per_excerpt());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals(3.176318819054861E12, cost, 10);
	    
	}

}
