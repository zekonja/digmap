package hr.yottabyte.digmap;

import org.geoserver.catalog.Catalog;
import org.geoserver.wms.WMS;
import org.geotools.process.ProcessException;
import org.geotools.process.factory.DescribeParameter;
import org.geotools.process.factory.DescribeProcess;
import org.geotools.process.factory.DescribeResult;
import org.geotools.process.gs.GSProcess;
import org.springframework.core.io.ResourceLoader;

@DescribeProcess(title = "DigMap", description = "Digital map excerpt")
public class DigMapProcess  implements GSProcess {

	private Catalog catalog;
	private WMS wms;
	private ResourceLoader resourceLoader;

	public DigMapProcess(Catalog catalog, WMS wms, ResourceLoader resourceLoader) {
		this.catalog = catalog;
		this.wms = wms;
		this.resourceLoader = resourceLoader;
	}

	@DescribeResult(name = "digmap", description = "Return DigMap filename")
	public String execute(
			@DescribeParameter(name = "userName", description = "Your name")
			String userName,
			@DescribeParameter(name = "countryName", description = "Country name")
			String  countryName
			) throws ProcessException, Exception{
		//prepare CQL filter
		String cqlFilterString = "name like '" + countryName+ "'";
		//generate report and return report filename
		return  DigMapGenerator.generateDigMap(catalog, wms, resourceLoader, cqlFilterString, userName);
	}

}
