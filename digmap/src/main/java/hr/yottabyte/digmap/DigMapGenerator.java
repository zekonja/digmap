package hr.yottabyte.digmap;

import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.wms.DefaultWebMapService;
import org.geoserver.wms.GetMap;
import org.geoserver.wms.GetMapRequest;
import org.geoserver.wms.MapLayerInfo;
import org.geoserver.wms.WMS;
import org.geoserver.wms.WebMap;
import org.geoserver.wms.map.PNGMapResponse;
import org.geoserver.wms.map.RenderedImageMap;
import org.geotools.data.DataAccess;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.text.cql2.CQL;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.geometry.BoundingBox;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.vividsolutions.jts.geom.Envelope;

public class DigMapGenerator {

	public static String generateDigMap(Catalog catalog, WMS wms, ResourceLoader resourceLoader, String cqlFilterString, String userName) throws Exception
	{
		String countryName = null;
		Name fName=null;;
		BoundingBox bb=null;
		JRMapCollectionDataSource dataSource=null;
		//create CQL filter based on input string
		Filter cqlFilter = CQL.toFilter(cqlFilterString);
		//open datastore
		DataStoreInfo  dsinfo = catalog.getDataStoreByName("opengeo","countries");
		DataAccess da = dsinfo.getDataStore(null);
		//get feature name
		fName = (Name) da.getNames().get(0);
		//grab feature source
		FeatureSource fs = da.getFeatureSource(fName);
		//apply filter on features
		FeatureCollection fc = fs.getFeatures(cqlFilter);
		//cast feature to simple feature
		SimpleFeature feature = (SimpleFeature)fc.features().next();
		//get feature bbox
		bb = feature.getBounds();
		//prepare datasource for Jasper report - grab data from feature
		List<Map<String,?>> maps = new ArrayList<Map<String, ?>> ();
		Map<String,Object> map = new HashMap<String, Object>();
		countryName=feature.getAttribute("name_long").toString();
		map.put("opengeo:name_long",countryName);
		map.put("opengeo:postal",feature.getAttribute("postal").toString());
		map.put("opengeo:pop_est",feature.getAttribute("pop_est").toString());
		map.put("opengeo:continent",feature.getAttribute("continent").toString());
		map.put("gml:lowerCorner",bb.getMinY()+" "+bb.getMinX());
		map.put("gml:upperCorner",bb.getMaxY()+" "+bb.getMaxX());
		maps.add(map);
		//create new JasperReport source
		dataSource = new JRMapCollectionDataSource(maps);
		//get layer
		LayerInfo layer = catalog.getLayerByName("countries" );
		MapLayerInfo mli = new MapLayerInfo(layer);
		List <MapLayerInfo> mlil = new ArrayList<MapLayerInfo>();
		mlil.add(mli);
		//apply CQL filter to layer also
		List<Filter> filterList = new ArrayList<Filter>();     
		filterList.add(cqlFilter);
		GetMapRequest request = new GetMapRequest();   
		request.setFilter(filterList);
		request.setTransparent(true);     
		request.setLayers(mlil);
		//set request envelope
		Envelope e = new Envelope(bb.getMinX(), bb.getMaxX(), bb.getMinY(), bb.getMaxY());
		request.setBbox(e);
		//create new wms
		DefaultWebMapService d = new DefaultWebMapService(wms);
		//better create new GetMap and set it to wms or except exceptions
		GetMap gm = new GetMap(wms);  
		d.setGetMap(gm);
		//we'll take the shortcut and use reflect
		WebMap wm = d.reflect(request);
		//some more steps
		RenderedImageMap rim = (RenderedImageMap) wm;
		RenderedImage ri = rim.getImage();
		//and finaly the image
		PNGMapResponse image = new PNGMapResponse(wms);
		//some steps to return image as InputStream for jasper report
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		image.formatImageOutputStream(ri, os, rim.getMapContext());
		InputStream wmsInputStream = new ByteArrayInputStream(((ByteArrayOutputStream) os).toByteArray());
		//reference compiled report file
		Resource resource = resourceLoader.getResource("classpath:PersonalizedHelloWorld4WPS.jasper"); 
		InputStream jrIS = resource.getInputStream();
		JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jrIS);
		//set report params
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userName",userName);
		params.put("countryName",countryName);
		params.put("wmsInputStream",wmsInputStream);
		//crerate report
		byte[] pdfBytes = JasperRunManager.runReportToPdf(jasperReport,params, dataSource);  
		String date = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.SSS").format(new Date());
		String fileName=date+"-DigMap.pdf";
		String directory="/var/www/html/digmap/";
		//write report to filesystem
		Files.write(Paths.get(directory+fileName), pdfBytes);
		//return report path 
		return fileName;
	}
}
