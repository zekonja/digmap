package hr.yottabyte.digmap.wfs;

import hr.yottabyte.digmap.config.WfsLayersConfig;





import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.mapfish.print.RenderingContext;
import org.mapfish.print.Transformer;
import org.mapfish.print.config.layout.MapBlock;
import org.mapfish.print.utils.PJsonArray;
import org.mapfish.print.utils.PJsonObject;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jts.io.WKTReader;

public class WfsUtils {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(WfsUtils.class);
    private static final int LOGLEVEL_QUIET = 0;
    private static final int LOGLEVEL_INFO = 1;
    private static final int LOGLEVEL_DEFAULT = 2;
    private static final int LOGLEVEL_VERBOSE = 3;	
    
    
    
    public static String getSRS(PJsonObject jsonSpec){
    	String projection = jsonSpec.optString("srs"); 
    	LOGGER.debug("Projection: \n" + projection);
    	return projection;
    	
    }
    
    
    
    
    public static CoordinateReferenceSystem getCRS(PJsonObject jsonSpec){
    	CoordinateReferenceSystem crs = null;
        String projection = getSRS(jsonSpec);
        LOGGER.debug("Projection: \n" + projection);
        boolean strictEpsg4326 = jsonSpec.optBool("strictEpsg4326", false);
        boolean longitudeFirst = strictEpsg4326;
        LOGGER.debug("LongitudeFirst: \n" + strictEpsg4326);    	
		try {				 
			crs = CRS.decode(projection,longitudeFirst);
		} catch (NoSuchAuthorityCodeException e1) {
			e1.printStackTrace();
		} catch (FactoryException e1) {
			e1.printStackTrace();
		} 
    	return crs;    	
    };

    

	public static List<WfsLayersConfig> getLayersConfig(PJsonObject jsonSpec, RenderingContext context){
	    	
		
		List<WfsLayersConfig> layersConfig = new ArrayList<WfsLayersConfig>();		
		int i,j;
		//Get SRS as string
		String srs = getSRS(jsonSpec);
		//get CRS
		CoordinateReferenceSystem crs = getCRS(jsonSpec); 
        //Get map reference
        MapBlock mb = context.getLayout().getMainPage().getMap(null);     
        ReferencedEnvelope re = null; 
        
		PJsonArray pages = jsonSpec.getJSONArray("pages");
		for (i = 0; i < pages.size(); ++i) {
			final PJsonObject cur = pages.getJSONObject(i);
			Transformer t = mb.createTransformer(context, cur);	
			Envelope envelope = new Envelope(t.getMinGeoX(),t.getMaxGeoX(), t.getMinGeoY(), t.getMaxGeoY());
			LOGGER.info("rotation: " + t.getRotation());
			if (t.getRotation() != 0) {	
				Geometry polygon = JTS.toGeometry( envelope );
				Coordinate ancorPoint = new Coordinate(t.getMinGeoX()+ t.getGeoW() / 2, t.getMinGeoY() + t.getGeoH() / 2);
				AffineTransform affineTransform = AffineTransform.getRotateInstance(t.getRotation(), ancorPoint.x,ancorPoint.y);				
				
				MathTransform mathTransform = new AffineTransform2D(affineTransform);
				try {
					envelope = JTS.transform(envelope, mathTransform);
					polygon = JTS.transform(polygon, mathTransform);
					LOGGER.info("rotated rectangle WKT: \n" + geometry2WKT(polygon));		
				} catch (TransformException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			};
			re = new ReferencedEnvelope(envelope, crs);
			String bbox = re.getMinX() + "," + re.getMinY() + ","+ re.getMaxX() + "," + re.getMaxY();
			LOGGER.info("map bbox envelope: " + re.toString());
			LOGGER.info("map bbox:"+bbox);
			Polygon polygon = JTS.toGeometry( re );
	        LOGGER.info("map bbox envelope WKT: \n" + geometry2WKT(polygon));		
			
			PJsonArray layers = jsonSpec.getJSONArray("layers");
			for (i = 0; i < layers.size(); i++) {
				PJsonObject layer = layers.getJSONObject(i);				
				//skip if not WMS layer type
				if (!layer.getString("type").equalsIgnoreCase("WMS")) continue;
				String baseURL = layer.getString("baseURL").replace("/wms","/wfs");
				LOGGER.debug("baseURL: \n" + baseURL);
				PJsonArray layerList = layer.getJSONArray("layers");
				String namedLayer = null;
				String namedLayers = "";
				for (j = 0; j < layerList.size(); j++) {
					namedLayer = layerList.getString(j);
					LOGGER.debug("Layer " + j + ": \n" + namedLayer);
					//check if layer is included in  DigMap configuration 
					//if (!context.getConfig().getConfigDM().getProperty("LAYERS").contains(namedLayer)) continue;
					namedLayers += layerList.getString(j);
					if (j < layerList.size() - 1)
						namedLayers += ",";
				}
				//skip empty layers list
				if (namedLayers.isEmpty()) continue;
				LOGGER.debug("Layers: \n" + namedLayers);
				String[] layersList = namedLayers.split(",");
				String wfsRequestString = baseURL
						+ "?request=GetFeature&version=2.0.0&typeName="
						+ namedLayers + "&BBOX=" + bbox + "," + srs;
				URL wfsRequest;
				try {
					wfsRequest = new URL(wfsRequestString);
					WfsLayersConfig lc = new WfsLayersConfig(baseURL,layersList, crs, null);
					layersConfig.add(lc);
					LOGGER.info("WFS request URL: \n" + wfsRequest.toString());					
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			};
		};		
		return layersConfig;		 
	}
	
	
	
	public static void writeGMLtoFile(String wfsRequest, String fileName){
		try {
			writeGMLtoFile(new URL(wfsRequest),new File(fileName));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void writeGMLtoFile(URL wfsRequest, File file){
    	try {
			FileUtils.copyURLToFile(wfsRequest, file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	} 
	
	public static String geometry2WKT(Geometry polygon) {
		StringWriter writer = new StringWriter();
		WKTWriter wktWriter = new WKTWriter(2);
		try {
			wktWriter.write(polygon, writer);
		} catch (IOException e) {
		}
		return writer.toString();
	}
	
	public static Geometry  WKT2geometry (String wellKnownText, CoordinateReferenceSystem dataCRS) {
		Geometry geom = null;
		WKTReader wktReader = new WKTReader();
		try {
			geom = wktReader.read(wellKnownText);
		} catch (com.vividsolutions.jts.io.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (dataCRS!=null)
		    try {
			geom.setSRID(CRS.lookupEpsgCode(dataCRS, false));
		    } catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		return geom;
	}	
}
