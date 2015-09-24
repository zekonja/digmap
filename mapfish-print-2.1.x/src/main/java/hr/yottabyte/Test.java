package hr.yottabyte;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.gml2.GMLWriter;

public final class Test {
	
	
	public static void main(String[] args) throws Exception {
		
		CoordinateReferenceSystem crs = null;
		try {
			crs = CRS.decode("EPSG:31275");
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
		
		
		
		Geometry poly = WfsUtils.WKT2geometry("POLYGON ((5538988.584394058 5033347.618080426, 5539080.306617142 5033347.618080426, 5539080.306617142 5033453.451411176, 5538988.584394058 5033453.451411176, 5538988.584394058 5033347.618080426))", crs);
		
		System.out.println("POLY SRID  : "+poly.getSRID());
		System.out.println("POLY STRING: "+poly.toString());
		System.out.println("POLY TXT   : "+poly.toText());
		GMLWriter gw = new GMLWriter();		
		System.out.println("POLY GML   : "+gw.write(poly));
		
		Envelope envelope = new Envelope(5538988, 5033347, 5539080, 5033453);
		ReferencedEnvelope re = new ReferencedEnvelope(envelope, crs);
		
		poly = JTS.toGeometry(re);
		
		poly.setSRID(CRS.lookupEpsgCode(crs, false));
		System.out.println("---------------------------");
		System.out.println("POLY SRID  : "+poly.getSRID());
		System.out.println("POLY STRING: "+poly.toString());		
		System.out.println("POLY TXT   : "+poly.toText());
		gw = new GMLWriter();		
		gw.setSrsName(crs.getName().getCode());
		System.out.println("POLY GML   : "+gw.write(poly));		
		
		
		
		//if (true) return;
		
		String baseURL = "http://gis.aquaeductus.hr/geoserver/";
		String[] layers = {"DR_db_UPU_koristenje"};
		envelope = new Envelope(5538988, 5033347, 5539080, 5033453);	    
		envelope = new Envelope(5538946, 5033833, 5538986, 5033871);
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
		layers, envelope, dc.getCost_per_point(),
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
	    descriptionList.add("Cost: "+costAndData.getCost());
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
	

	public static void main1(String[] args) {

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
			//ShellMapPrinter.main(cargs);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
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
