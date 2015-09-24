package hr.yottabyte.digmap; 

import org.geoserver.catalog.Catalog;
import org.geoserver.wms.WMS;
import org.geotools.process.ProcessException;
import org.geotools.process.factory.DescribeParameter;
import org.geotools.process.factory.DescribeProcess;
import org.geotools.process.factory.DescribeResult;
import org.geotools.process.gs.GSProcess;
import org.springframework.core.io.ResourceLoader;

import com.vividsolutions.jts.geom.Geometry;

@DescribeProcess(title = "DigMapSimple", description = "Digital map excerpt")
public class DigMapSimpleProcess   implements GSProcess {

	private Catalog catalog;
	private WMS wms;
	private ResourceLoader resourceLoader;

	public DigMapSimpleProcess(Catalog catalog, WMS wms, ResourceLoader resourceLoader) {
		this.catalog = catalog;
		this.wms = wms;
		this.resourceLoader = resourceLoader;
	}

	@DescribeResult(name = "digmap", description = "Return DigMap filename")
	public String execute(
			@DescribeParameter(name = "geometry", description = "Point")
			Geometry geom
			) throws ProcessException, Exception{

		String cqlFilterString = "CONTAINS(the_geom,"+geom.toString()+")";
		return DigMapGenerator.generateDigMap(catalog, wms, resourceLoader, cqlFilterString, "");

	}


}