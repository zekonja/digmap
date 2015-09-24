package hr.yottabyte.digmap;

import static org.geotools.referencing.crs.DefaultGeographicCRS.WGS84;
import hr.yottabyte.digmap.billing.CostAndData;
import hr.yottabyte.digmap.billing.CostCalc;
import hr.yottabyte.digmap.config.DigMapConfig;
import hr.yottabyte.digmap.signer.PdfAttacher;
import hr.yottabyte.digmap.signer.PdfSigner;
import hr.yottabyte.digmap.signer.PdfUtils;
import hr.yottabyte.digmap.wfs.WfsUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.geotools.GML;
import org.geotools.GML.Version;
import org.geotools.data.DataUtilities;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.geometry.jts.WKTReader2;
import org.geotools.gml2.GMLConfiguration;
import org.geotools.gml2.SrsSyntax;
import org.geotools.referencing.CRS;
import org.mapfish.print.ShellMapPrinter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.gml2.GMLWriter;




public final class TestDM {
    

	
	
	public static void main(String[] args) throws Exception {
		
		CoordinateReferenceSystem crs = null;
		try {
			crs = CRS.decode("EPSG:31275");
			//crs = CRS.decode("EPSG:3857");
			
		        //crs = CRS.decode("EPSG:4326");
			
			System.out.println("crs equals: "+( crs.getName().equals( crs.getName() )));
			//System.out.println("crs toString: "+crs.toString());
			//System.out.println("crs toWKT: "+crs.toWKT());
			System.out.println("crs getCode: "+crs.getName().getCode());
			System.out.println("crs getCodeSpace: "+crs.getName().getCodeSpace());
			System.out.println("crs getVersion: "+crs.getName().getVersion());
			System.out.println("crs getAuthority: "+crs.getName().getAuthority());
		} catch (NoSuchAuthorityCodeException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		};
		

		
		
		
//		
//		System.out.println("POLY SRID  : "+poly.getSRID());
//		System.out.println("POLY STRING: "+poly.toString());
//		System.out.println("POLY TXT   : "+poly.toText());
		GMLConfiguration gc = new GMLConfiguration();
		gc.setSrsSyntax(SrsSyntax.OGC_HTTP_URI);
		GMLWriter gw = new GMLWriter();
		
		
//		System.out.println("POLY GML   :\n "+gw.write(poly));
		
//		LL POINT(5538546.0 5033754.0)
//		UL POINT(5538730.0 5033896.0)
		Envelope envelope = new Envelope(5538546,  5538730, 5033754, 5033896 );
		//envelope = new Envelope( 5033754, 5033896, 5538546,  5538730);
		ReferencedEnvelope re = new ReferencedEnvelope(envelope, crs);
		
		
		
		Geometry poly = JTS.toGeometry(re);
		
		
		poly.setSRID(CRS.lookupEpsgCode(crs, false));
		System.out.println("---------------------------");
		System.out.println("POLY SRID  : "+poly.getSRID());
		System.out.println("POLY STRING: "+poly.toString());		
		System.out.println("POLY EWKT   : \n"+"SRID="+poly.getSRID()+";"+poly.toText());
		gw = new GMLWriter();		
		gw.setSrsName(crs.getName().getCode());
		for (Coordinate c : poly.getCoordinates())
		    System.out.println("POINT("+c.x+" "+c.y+")");
		System.out.println("\n\nPOLY GML   :\n "+gw.write(poly));	
		
		
		
		
		
		//if (true) return;
		
		String baseURL = "http://gis.aquaeductus.hr/geoserver/";
		//String[] layers = {"DR_db_UPU_koristenje","DR_db_Promet"};
		String[] layers = {"DR_db_UPU_koristenje"};
		

//		String baseURL = "http://dr.aquaeductus.hr/geoserver/";
//		String[] layers = {"topp_states"};
//		poly = WfsUtils.WKT2geometry("POINT(36 -105)", null);
		
		
//		String baseURL = "http://digmap-dme.fiware.yottabyte.hr/geoserver/";
//		String[] layers = {"topp_states"};
//		poly = WfsUtils.WKT2geometry("POLYGON ((-12139285.3778555 4438479.7069292, -12139010.211178498 4438479.7069292, -12139010.211178498 4438797.2069292, -12139285.3778555 4438797.2069292, -12139285.3778555 4438479.7069292))", crs);
		
		
		

		DigMapConfig dc=null;
		try {
			dc = loadDigMapConfig();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    double cost = 0;
	    
	InputStream in;
	ByteArrayOutputStream bos;

	List<CostAndData> costAndDataList = CostCalc.getDataAndCost(baseURL,
		layers, poly, dc.getCost_per_point(),
		dc.getCost_per_line(), dc.getCost_per_polygon(),
		dc.getCost_per_area(),dc.getCost_per_excerpt());
	// add attachments to PDF file --assume output from mapfish-print
	System.out.println("Adding attachments");
	
	bos= PdfUtils.createDummyPdf();
	in = new ByteArrayInputStream(bos.toByteArray());
	
	List<InputStream> attachmentList= new ArrayList<InputStream>();
	List<String> displayList = new ArrayList<String>();
	List<String> descriptionList = new ArrayList<String>();
	for (CostAndData costAndData : costAndDataList)
	{
	    cost += costAndData.getCost();
	    attachmentList.add(costAndData.getData());
	    displayList.add(costAndData.getLayer());
	    if (costAndData.getCost() > 0)
		descriptionList.add("Cost: " + costAndData.getCost());
	    else
		descriptionList.add("Data layer: "+costAndData.getLayer());
	}
	

	
	InputStream[] attachments = new InputStream[attachmentList.size()];
	attachments = attachmentList.toArray(attachments);
	
	String[] displays = new String[displayList.size()];
	displays = displayList.toArray(displays);
	
	String[] descriptions = new String[descriptionList.size()];
	descriptions = descriptionList.toArray(descriptions);	
	
 	bos = PdfAttacher.addAttachments(in, attachments, displays,descriptions);
	// convert to input stream
	in = new ByteArrayInputStream(bos.toByteArray());
	// sign PDF
	System.out.println("Sign PDF");
	bos = PdfSigner.signPdf(in, dc.getProperties());
	// print info
	System.out.println("PDF info:");
	PdfUtils.printInfo(bos, null, dc.getProperties());
	// write PDF to filesystem
	String outFileName="testDM.pdf";
	System.out.println("PDF written to file: "
		+ new java.io.File(outFileName).getCanonicalPath());
	bos.writeTo(new FileOutputStream(outFileName));
		    

		System.out.println("cost: "+cost);
		System.out.println("-------------------OVER--------------------");		
		
	
	}
	

	public static void main2(String[] args) {

		String configFilePath = "C:/Users/khrnjak/Desktop/mapfish-print-2.1.x/samples/digmap/config-DR.yaml";
		String configDMFilePath = "C:/Users/khrnjak/Desktop/mapfish-print-2.1.x/samples/digmap/digmap_config.properties";
		String specFilePath = "C:/Users/khrnjak/Desktop/mapfish-print-2.1.x/samples/digmap/spec-DR-pretty.json";
		String outFilePath = "C:/Users/khrnjak/Desktop/mapfish-print-2.1.x/samples/digmap/print-DRSDF.pdf";

		

		
		
		File configFile = new File(configFilePath);
		File configDMFile = new File(configDMFilePath);
		File v2ApiRequestFile = new File(specFilePath);
		File outputFile = new File(outFilePath);

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
		
		if (true) return;
		
		
	    File file = new File("C:/Users/khrnjak/workspace/digmap/src/test/resources/wfs.xml");
	    String wfsGML = file.getAbsolutePath();
		DigMapConfig dc=null;
		try {
			dc = loadDigMapConfig();
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
			//cost = CostCalc.calcDataCost(wfsGML,CRS.decode("EPSG:31275"),dmc);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("cost: "+cost);
		System.out.println("-------------------OVER--------------------");

	}

	
	public static DigMapConfig loadDigMapConfig() throws FileNotFoundException, IOException
	{
		Properties properties = new Properties();			
		
	    File file = new File("C:/Users/khrnjak/workspace/digmap/samples/digmap/digmap_config.properties");
	    String configPath = file.getAbsolutePath();		
		properties.load(new FileInputStream(configPath));
		String key_path=properties.getProperty("KEY_PATH");
	    file = new File("C:/Users/khrnjak/workspace/digmap/samples/digmap/yottabyte.pfx");
	    key_path = file.getAbsolutePath();
	    properties.setProperty("KEY_PATH", key_path);
		String sign_image=properties.getProperty("SIGN_IMAGE");
		file = new File("C:/Users/khrnjak/workspace/digmap/samples/digmap/certificate2_pencil.png");
		sign_image = file.getAbsolutePath();
		properties.setProperty("SIGN_IMAGE", sign_image);
		DigMapConfig dc = new DigMapConfig(properties);
		return dc;		
	}
}
