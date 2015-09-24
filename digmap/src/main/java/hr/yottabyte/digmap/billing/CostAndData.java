package hr.yottabyte.digmap.billing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class CostAndData {
    
    public String baseURL; 
    public String layer;
    double cost =  0;;
    InputStream data = null;

    
    
    public CostAndData(String baseURL, String layer, double cost,
	    InputStream data) {
	super();
	this.baseURL = baseURL;
	this.layer = layer;
	this.cost = cost;
	this.data = data;
    }
    public String getBaseURL() {
        return baseURL;
    }
    public String getLayer() {
        return layer;
    }
    public double getCost() {
        return cost;
    }
    public InputStream getData() {
        return data;
    }
    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }
    public void setLayer(String layer) {
        this.layer = layer;
    }
    public void setCost(double cost) {
        this.cost = cost;
    }
    public void setData(InputStream data) {
        this.data = data;
    }

}
