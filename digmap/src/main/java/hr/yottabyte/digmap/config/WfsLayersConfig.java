package hr.yottabyte.digmap.config;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

public class WfsLayersConfig {
	public String baseURL;
	public String[] layers;
	public CoordinateReferenceSystem crs;
	public Geometry geometry;	
	
	
	public WfsLayersConfig(String baseURL, String[] layersList, CoordinateReferenceSystem crs, Geometry geometry) {
		this.baseURL = baseURL;
		this.layers = layersList;
		this.geometry = geometry;
		this.crs = crs;	
	}


	public String getBaseURL() {
	    return baseURL;
	}


	public String[] getLayers() {
	    return layers;
	}


	public CoordinateReferenceSystem getCrs() {
	    return crs;
	}


	public Geometry getGeometry() {
	    return geometry;
	}


	public void setBaseURL(String baseURL) {
	    this.baseURL = baseURL;
	}


	public void setLayers(String[] layers) {
	    this.layers = layers;
	}


	public void setCrs(CoordinateReferenceSystem crs) {
	    this.crs = crs;
	}


	public void setGeometry(Geometry geometry) {
	    this.geometry = geometry;
	}
	


}
