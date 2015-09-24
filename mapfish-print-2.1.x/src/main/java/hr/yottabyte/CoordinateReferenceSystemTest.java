package hr.yottabyte;

import static org.geotools.referencing.crs.DefaultGeographicCRS.WGS84;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


public class CoordinateReferenceSystemTest {

	public static void main(String[] args) throws Exception {		
		
		
		ReferencedEnvelope re = new ReferencedEnvelope(-74, -73, 40, 41,WGS84);
        CoordinateReferenceSystem wgs_from_epsg=CRS.decode("EPSG:4326",true);
//        System.out.println(WGS84.toString());
//        System.out.println("--------------");
//        System.out.println(wgs_from_epsg.toString());
//        System.out.println("--------------");
//        System.out.println("ReferencedEnvelope              : "+re.toString());
//        System.out.println("ReferencedEnvelope to EPSG:4326 : "+re.transform(wgs_from_epsg, true).toString());
//        System.out.println("ReferencedEnvelope to WGS84     : "+re.transform(WGS84, true).toString());
        
        //System.out.println(wgs_from_epsg.toString());
        
        System.out.println(CRS.lookupEpsgCode(WGS84, true));
        
        
        
        
	}

}
