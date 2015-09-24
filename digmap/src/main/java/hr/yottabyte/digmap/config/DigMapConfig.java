package hr.yottabyte.digmap.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class DigMapConfig {

	// key config
	private String key_path;
	private String key_password;
	private String keystore_password;
	String key_type = "pkcs12";
	Boolean sec_provider = false;
	// signature appearnce config
	Boolean sign_visible=false;
	String sign_image;
	String sign_reason="";
	String sign_contact="";
	int sign_rect_llx;
	int sign_rect_lly;
	int sign_rect_urx;
	int sign_rect_ury;
	// list of wms layers avaiable
	String[] layers;
	// define excerpt and data cost
	double cost_per_excerpt=0;
	double cost_per_point=0;
	double cost_per_line=0;
	double cost_per_polygon=0;
	double cost_per_area=0;
	//properties
	Properties properties=null;
	
	
	public Properties getProperties() {
		return properties;
	}

	public DigMapConfig(Properties properties) {
		this.setConfig(properties);		
	}
	
	public DigMapConfig(String configFile) {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(configFile);
			// load a properties file
			prop.load(input);
			setConfig(prop);			
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}	

	public void setConfig(Properties prop) {
		this.properties = prop;
		//mandatory properties
		this.key_path = prop.getProperty("KEY_PATH");
		this.key_password = prop.getProperty("KEY_PASSWORD");
		this.keystore_password = prop.getProperty("KEYSTORE_PASSWORD");
		this.key_type = prop.getProperty("KEYSTORE_PASSWORD");
		if (prop.getProperty("KEY_TYPE") != null)
			this.key_type = prop.getProperty("KEY_TYPE");
		if (prop.getProperty("SEC_PROVIDER") == "true")
			this.sec_provider = true;
		this.layers = prop.getProperty("LAYERS").trim().split(",");
		//optional properties	
		if (prop.getProperty("SIGN_REASON") != null)
			this.sign_reason = prop.getProperty("SIGN_REASON");
		
		if (prop.getProperty("SIGN_IMAGE") != null)
			this.sign_image = prop.getProperty("SIGN_IMAGE");
		if (prop.getProperty("SIGN_RECT_LLX") != null)
			this.sign_contact = prop.getProperty("SIGN_CONTACT");	
		if (prop.getProperty("SIGN_RECT_LLX") != null)
			this.sign_rect_llx = Integer.parseInt(prop.getProperty("SIGN_RECT_LLX"));
		if (prop.getProperty("SIGN_RECT_LLY") != null)
			this.sign_rect_lly = Integer.parseInt(prop.getProperty("SIGN_RECT_LLY"));
		if (prop.getProperty("SIGN_RECT_URX") != null)
			this.sign_rect_urx = Integer.parseInt(prop.getProperty("SIGN_RECT_URX"));
		if (prop.getProperty("SIGN_RECT_URY") != null)
			this.sign_rect_ury = Integer.parseInt(prop.getProperty("SIGN_RECT_URY"));
		//data cost properties
		if (prop.getProperty("COST_PER_EXCERPT")!=null) 
			this.cost_per_excerpt = Double.parseDouble(prop.getProperty("COST_PER_EXCERPT"));
		if (prop.getProperty("COST_PER_POINT")!=null ) 
			this.cost_per_point = Double.parseDouble(prop.getProperty("COST_PER_POINT"));
		if (prop.getProperty("COST_PER_LINE")!=null) 
			this.cost_per_line = Double.parseDouble(prop.getProperty("COST_PER_LINE"));
		if (prop.getProperty("COST_PER_POLYGON")!=null) 
			this.cost_per_polygon = Double.parseDouble(prop.getProperty("COST_PER_POLYGON"));
		if (prop.getProperty("COST_PER_AREA")!=null) 
			this.cost_per_area = Double.parseDouble(prop.getProperty("COST_PER_AREA"));
	}



	public String getKey_path() {
		return key_path;
	}

	public String getKey_password() {
		return key_password;
	}

	public String getKeystore_password() {
		return keystore_password;
	}

	public String getKey_type() {
		return key_type;
	}

	public Boolean getSec_provider() {
		return sec_provider;
	}

	public String getSign_image() {
		return sign_image;
	}

	public String getSign_reason() {
		return sign_reason;
	}

	public String getSign_contact() {
		return sign_contact;
	}

	public int getSign_rect_llx() {
		return sign_rect_llx;
	}

	public int getSign_rect_lly() {
		return sign_rect_lly;
	}

	public int getSign_rect_urx() {
		return sign_rect_urx;
	}

	public int getSign_rect_ury() {
		return sign_rect_ury;
	}

	public String[] getLayers() {
		return layers;
	}

	public double getCost_per_excerpt() {
		return cost_per_excerpt;
	}

	public double getCost_per_point() {
		return cost_per_point;
	}

	public double getCost_per_line() {
		return cost_per_line;
	}

	public double getCost_per_polygon() {
		return cost_per_polygon;
	}

	public double getCost_per_area() {
		return cost_per_area;
	}

}
