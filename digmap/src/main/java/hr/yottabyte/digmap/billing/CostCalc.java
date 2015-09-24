package hr.yottabyte.digmap.billing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.unit.Unit;

import org.geotools.GML;
import org.geotools.GML.Version;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.factory.Hints;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.Name;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.Filter;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import org.geotools.data.store.ContentFeatureCollection;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.WFSDataStoreFactory;

public class CostCalc {

    private static final Logger LOGGER = LoggerFactory.getLogger(CostCalc.class);
    private static final int LOGLEVEL_QUIET = 0;
    private static final int LOGLEVEL_INFO = 1;
    private static final int LOGLEVEL_DEFAULT = 2;
    private static final int LOGLEVEL_VERBOSE = 3;
   
    public static double getDataCost(String baseURL, String[] features,
	    ReferencedEnvelope refEnvelope, double costPerPoint, double costPerLine,
	    double costPerPolygon, double costPerArea, double costPerExcerpt)
	    throws Exception {	
	Geometry geometry = JTS.toGeometry(refEnvelope);
	geometry.setSRID(CRS.lookupEpsgCode(refEnvelope.getCoordinateReferenceSystem(), false));
	return getDataCost(baseURL, features, geometry, costPerPoint,
		costPerLine, costPerPolygon, costPerArea, costPerExcerpt);
    };

    public static double getDataCost(String baseURL, String[] features,
	    Geometry geom, double costPerPoint, double costPerLine,
	    double costPerPolygon, double costPerArea, double costPerExcerpt)
	    throws Exception {
	LOGGER.info("Param baseURL: "+baseURL);
	for (String feature : features)
	    LOGGER.info("Param feature: "+feature);
	LOGGER.info("Param geometry: "+geom.toText());	
	double dataCost = 0;
	List<CostAndData> costAndDataList = getDataAndCost(baseURL, features,
		geom, costPerPoint, costPerLine, costPerPolygon, costPerArea, costPerExcerpt);
	for (CostAndData cad : costAndDataList)
	    dataCost += cad.getCost();
	return dataCost+costPerExcerpt;
    };
    
    
    
    public static List<CostAndData> getDataAndCost(String baseURL, String[] features,
	    Envelope envelope, double costPerPoint,
	    double costPerLine, double costPerPolygon, double costPerArea, double costPerExcerpt) throws Exception {	
	Geometry geometry = JTS.toGeometry(envelope);
	return getDataAndCost(baseURL, features, geometry, costPerPoint,
		costPerLine, costPerPolygon, costPerArea, costPerExcerpt);
	
    }

    public static List<CostAndData> getDataAndCost(String baseURL, String[] features,
	    Geometry geometry, double costPerPoint,
	    double costPerLine, double costPerPolygon, double costPerArea, double costPerExcerpt) throws Exception {
	
	List<CostAndData> costAndDataList = new ArrayList<CostAndData>();
	CostAndData costAndData;
	//add area of interest as wkt        
        String ewkt = "SRID="+geometry.getSRID()+";"+geometry.toText();
	InputStream wkt = new ByteArrayInputStream(ewkt.getBytes(StandardCharsets.UTF_8));
	costAndData = new CostAndData("","AreaSelection.wkt",0,wkt);
	costAndDataList.add(costAndData);	
	// check if there is a feature for calculation
	if (!((features!=null) && (features.length>0)))
	    return costAndDataList;
	features = replaceTypeNameSeparator(features);
	// Step 1 - connection params setup
	String getCapabilities = baseURL
		+ "/wfs?REQUEST=GetCapabilities&VERSION=1.1.0";
	LOGGER.debug("WFS baseURL: " + getCapabilities);
	Map<String, Serializable> connectionParameters = new HashMap<String, Serializable>();
	
	System.out.println();
	connectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL",getCapabilities);
	connectionParameters.put("WFSDataStoreFactory:OUTPUTFORMAT","text/xml; subtype=gml/3.1.1");
	//connectionParameters.put("WFSDataStoreFactory:AXIS_ORDER","East / North");
	connectionParameters.put("WFSDataStoreFactory:USEDEFAULTSRS",false);
	 
	
	
	// Step 2 - connection
	WFSDataStoreFactory dsf = new WFSDataStoreFactory();
	
	LOGGER.debug("WFS params OK: " + dsf.canProcess(connectionParameters));
	WFSDataStore dataStore = dsf.createDataStore(connectionParameters);
	
	
	
	
	LOGGER.debug("WFS version: " + dataStore.getInfo().getVersion());
	LOGGER.debug("WFS description: " + dataStore.getInfo().getDescription());
	LOGGER.debug("WFS title: " + dataStore.getInfo().getTitle());
	LOGGER.debug("WFS schema: " + dataStore.getInfo().getSchema());
	LOGGER.debug("WFS namespaceURI: " + dataStore.getNamespaceURI());
	
	if (LOGGER.isDebugEnabled())
	    for (Name name : dataStore.getNames()) {	
		
		LOGGER.debug("WFS name getLocalPart: " + name.getLocalPart());
		LOGGER.debug("WFS name getNamespaceURI: " + name.getNamespaceURI());
		LOGGER.debug("WFS name getSeparator: " + name.getSeparator());
		LOGGER.debug("WFS name getURI: " + name.getURI());
		LOGGER.debug("WFS name isGlobal: " + name.isGlobal());
		LOGGER.debug("WFS name toString: " + name.toString());
		
	    }
	
	
//	    for (Name name : dataStore.getNames()) {		
//		
//		LOGGER.info("WFS name getNamespaceURI: " + name.getNamespaceURI());
//		LOGGER.info("WFS name getLocalPart: " + name.getLocalPart());		
//		LOGGER.info("WFS name getSeparator: " + name.getSeparator());
//		LOGGER.info("WFS name getURI: " + name.getURI());
//		LOGGER.info("WFS name isGlobal: " + name.isGlobal());
//		LOGGER.info("WFS name toString: " + name.toString());
//		
//	    }	
	if (LOGGER.isDebugEnabled())
	    for (String name : dataStore.getTypeNames()) {
		LOGGER.debug("WFS name: " + name);
	    }	
	
	
	
	features = retainWhiteListedfeatures(features, dataStore.getTypeNames());
	// check again if there is a feature for calculation provided over wfs
	if (!((features!=null) && (features.length>0)))
	    return costAndDataList;	
	
	// Step 3 - read features of interest
	for (String typeName : features) {
	    //typeName = typeName.replaceAll(regex, replacement)
	    LOGGER.info("TypeName: " + typeName);
	    // SimpleFeatureType schema = dataStore.getSchema(typeName);
	    // Step 4 - target	    
	    ContentFeatureSource source = dataStore.getFeatureSource(typeName);
	    LOGGER.debug(source.getSchema().toString());
	    // Step 5 - query
	    // String geomName = schema.getGeometryDescriptor().getLocalName();
	    String geomName = source.getSchema().getGeometryDescriptor().getLocalName();
	    LOGGER.debug("Geometry name:" + geomName);
	    
	    FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
	    Hints h = GeoTools.getDefaultHints();
	    
	    //check if Geometry is in the same CRS as source data
	    CoordinateReferenceSystem dataCRS = source.getSchema().getCoordinateReferenceSystem();
            CoordinateReferenceSystem geomCRS = null;
            //if no SRID set assume same CRS as 
	    if (geometry.getSRID() == 0) {
		geomCRS = dataCRS;
		try {
		    geometry.setSRID(CRS.lookupEpsgCode(geomCRS, false));
		} catch (FactoryException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		LOGGER.warn("Geometry dosn't have a SRID, assume same CRS as sourcedata");
	    }
	    else {
		geomCRS = CRS.decode("EPSG:" + geometry.getSRID());
		LOGGER.info("Geometry orig SRID: "+geometry.getSRID());
	    }
	    //if CRS differ make geometry re-projection	    
            if (!dataCRS.getName().equals(geomCRS.getName())){
        	boolean lenient = true; // allow for some error due to different datums
        	MathTransform transform = CRS.findMathTransform(geomCRS,dataCRS, lenient);        	
        	Geometry nativeGeometry = JTS.transform(geometry, transform);
        	geomCRS=dataCRS;
        	nativeGeometry.setUserData(null);
        	nativeGeometry.setSRID(CRS.lookupEpsgCode(geomCRS, false));           	
        	geometry = nativeGeometry;
        	
            }
            LOGGER.info("Geometry SRID: "+geometry.getSRID());
            //invert geom points for WFS 1.1
            //geometry.apply(new InvertCoordinateFilter());
            //SrsSyntax.OGC_HTTP_URI;
            
            
            
	    Filter filter = ff.intersects(ff.property(geomName),ff.literal(geometry));
	    LOGGER.info("Filter: " + filter.toString());	    

	    Query query = new Query(typeName, filter, new String[] { geomName });
	    
	    for (String p : query.getPropertyNames())
		System.out.println(p.toString());
	    LOGGER.info("Query: " + query.toString());
	    
//	    for(Object o : source.getSupportedHints())
//		System.out.println("HINT "+o.toString());	    
	    
	    
	    ContentFeatureCollection featureCollection = source.getFeatures(query);
	    LOGGER.info("Num of features returned: : " + featureCollection.size());
	    
	    
	    LOGGER.debug("CoordinateReferenceSystem: " + dataCRS);
	    
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    writeDataAsGMLToStream(bos, featureCollection);
	    double dataCost = 0;
	    if ((costPerPoint != 0) || (costPerLine != 0) || (costPerPolygon != 0) || (costPerArea != 0))
		dataCost = getDataCost(featureCollection, costPerPoint, costPerLine, costPerPolygon, costPerArea, 0);
	    dataCost = Math.round(dataCost * 100.0) / 100.0;
	    costAndData = new CostAndData(baseURL,typeName+".xml",dataCost,new ByteArrayInputStream(bos.toByteArray()));
	    costAndDataList.add(costAndData);
	}
	return costAndDataList;
    }

    public static double getDataCost(SimpleFeatureCollection features,
	    double costPerPoint, double costPerLine, double costPerPolygon,
	    double costPerArea, double costPerExcerpt) throws Exception {
	double dataCost = 0;
	CoordinateReferenceSystem crs = features.getSchema()
		.getCoordinateReferenceSystem();
	LOGGER.debug("Features CRS: " + crs.getName().toString());
	Unit u = crs.getCoordinateSystem().getAxis(0).getUnit();
	LOGGER.debug("Units: " + u.toString());
	LOGGER.debug("Num of features: " + features.size());
	CoordinateReferenceSystem transverseMercator = null;
	MathTransform transform = null;

//	//consideration how to select right UTM zone
//	if (!u.isStandardUnit()) {
//	    // convert data to transverse mercator to be able to calc area in sq meters
//	    transverseMercator = CRS.decode("EPSG:3765");
//	    boolean lenient = true; // allow for some error due to different datums
//				    // datums
//	    transform = CRS.findMathTransform(crs, transverseMercator, lenient);
//	};

	FeatureIterator<SimpleFeature> iterator = features.features();
	try {
	    while (iterator.hasNext()) {
		SimpleFeature feature = (SimpleFeature) iterator.next();
		LOGGER.debug("Feature: " + feature.getIdentifier().getID());
		Geometry geom = (Geometry) feature.getDefaultGeometry();
		LOGGER.debug("Geometry type: " + geom.getGeometryType());
		LOGGER.debug("Num of points: " + geom.getNumPoints());		
		LOGGER.debug("Geometry: " + geom.toText());
//		Geometry geom2;
//		if (u.isStandardUnit())
//		    geom2 = geom;
//		else
//		    geom2 = JTS.transform(geom, transform);
//		LOGGER.debug("Area [sqm]: " + geom2.getArea());
		LOGGER.debug("Area: " + geom.getArea());
		LOGGER.debug("Length: " + geom.getLength());
		if (costPerPoint > 0)
		    dataCost += costPerPoint * geom.getNumPoints();
		if (costPerLine > 0)
		    dataCost += costPerLine * geom.getLength();
		if (costPerPolygon > 0) {
			if (geom.getGeometryType().equalsIgnoreCase("Polygon"))
			    dataCost += costPerPolygon;
			if (geom.getGeometryType().equalsIgnoreCase("MultiPolygon"))
			    dataCost += costPerPolygon;			
		};		
		if (costPerArea > 0)
		    dataCost += costPerArea * geom.getArea();
	    }

	} finally {
	    iterator.close();
	}
	return dataCost+costPerExcerpt;
    }



    public static double getDataCost(String filename,
	    CoordinateReferenceSystem crs, double costPerPoint, double costPerLine, double costPerPolygon,
	    double costPerArea, double costPerExcerpt) throws Exception {
	GML gml = new GML(Version.WFS1_1);
	gml.setCoordinateReferenceSystem(crs);
	File inFile = new File(filename);
	FileInputStream in = new FileInputStream(inFile);	
	SimpleFeatureCollection features = gml.decodeFeatureCollection(in);	
	return getDataCost(features, costPerPoint, costPerLine, costPerPolygon, costPerArea, costPerExcerpt);
    }

    public static void writeDataAsGMLToStream(OutputStream out,
	    ContentFeatureCollection features)
	    throws Exception {
	CoordinateReferenceSystem crs = features.getSchema().getCoordinateReferenceSystem();
	GML gml = new GML(Version.WFS1_0);
	gml.setCoordinateReferenceSystem(crs);	
	gml.setNamespace("xmlns:yottabyte", "http://www.yottabyte.hr");
	gml.encode(out, features);
	out.close();
    }
    
    



    public static String[] retainWhiteListedfeatures(String[] basefeatures,
	    String[] whiteListfeatures) {
	// Check configuration are requested features allowed
	List<String> baseList = new ArrayList<String>(Arrays.asList(basefeatures));
	List<String> whiteList = new ArrayList<String>(Arrays.asList(whiteListfeatures));	
	baseList.retainAll(whiteList);
	String[] retainedFeatures = new String[baseList.size()];
	retainedFeatures = baseList.toArray(retainedFeatures); 
	return retainedFeatures;
    }
    
    public static String[] replaceTypeNameSeparator(String[] features) {
	if (features==null) return null;
	String[] replycedseparator = new String[features.length];
	for (int i=0; i<features.length; i++)
	    replycedseparator[i] = features[i].replaceAll(":", "_");
	return replycedseparator;
    }
    
    
    
    private static class InvertCoordinateFilter implements CoordinateFilter {
	    public void filter(Coordinate coord) {
	        double oldX = coord.x;
	        coord.x = coord.y;
	        coord.y = oldX;
	    }
	}      
    
    

}
