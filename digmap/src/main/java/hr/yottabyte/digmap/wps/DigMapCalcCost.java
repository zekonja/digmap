package hr.yottabyte.digmap.wps;




import hr.yottabyte.digmap.billing.CostCalc;
import hr.yottabyte.digmap.signer.PdfUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.geotools.process.factory.DescribeParameter;
import org.geotools.process.factory.DescribeProcess;
import org.geotools.process.factory.DescribeResult;
import org.geoserver.wps.gs.GeoServerProcess;
import org.geoserver.wps.process.RawData;
import org.geoserver.wps.process.StreamRawData;
import org.mapfish.print.MapPrinter;
import org.springframework.core.io.ResourceLoader;






import com.itextpdf.text.DocumentException;
import com.vividsolutions.jts.geom.Geometry;

@DescribeProcess(title = "DigMapCalcCost", description = "DigMap - Calculate data cost")
public class DigMapCalcCost implements GeoServerProcess {

	private MapPrinter mapPrinter;
	private ResourceLoader resourceLoader;	
	private ServletContext servletContext;

	public DigMapCalcCost(MapPrinter mapPrinter, ResourceLoader resourceLoader,ServletContext servletContext) {
		this.mapPrinter = mapPrinter;
		this.resourceLoader = resourceLoader;		
		this.servletContext = servletContext;
	}


    // 
    @SuppressWarnings("unchecked")
    @DescribeResult(name = "cost", description = "Data cost")
    public double execute(
	    @DescribeParameter(name = "baseURL", description = "WFS host URL")
	    String baseURL,
	    @DescribeParameter(name = "layers", description = "Comma separated features list")
	    String features,
	    @DescribeParameter(name = "geometry", description = "Area of interest")
	    Geometry geometry,
	    @DescribeParameter(name = "costPerPoint", description = "Cost per point")
	    double costPerPoint, 
	    @DescribeParameter(name = "costPerLine", description = "Cost per line")
	    double costPerLine, 
	    @DescribeParameter(name = "costPerPolygon", description = "Cost per polygon")
	    double costPerPolygon,
	    @DescribeParameter(name = "costPerArea", description = "Cost per area")
	    double costPerArea, 
	    @DescribeParameter(name = "costPerExcerpt", description = "Cost per excerpt")
	    double costPerExcerpt

    ) throws Exception {
	    return CostCalc.getDataCost(baseURL, features.split(","), geometry, costPerPoint, costPerLine, costPerPolygon, costPerArea, costPerExcerpt);
    }

}
