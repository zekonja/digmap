package hr.yottabyte.digmap;

import hr.yottabyte.digmap.config.DigMapConfig;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.mapfish.print.Constants;
import org.mapfish.print.MapPrinter;
import org.mapfish.print.utils.PJsonObject;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.io.CharStreams;
import com.google.common.io.Files;

public class TestUtils {
	
    @Autowired
    private MapPrinter mapPrinter;
//    @Autowired
//    private static ConfigurationFactory configurationFactory;	
	
	/** DigMap properties used for signing. */
	public static String DIGMAP_CONFIG_FILE = "digmap_config.properties";	
	/** Map Fish Print config  */
	public static String MFP_CONFIG_FILE = "config.yaml";	
	/** Map Fish Print spec  */
	public static String MFP_SPEC_FILE = "requestData.json";		
	
	public static DigMapConfig loadDigMapConfig() throws FileNotFoundException, IOException
	{
		Properties properties = new Properties();				
		ClassLoader classLoader = TestUtils.class.getClassLoader();
	    File file = new File(classLoader.getResource(DIGMAP_CONFIG_FILE).getFile());
	    String configPath = file.getAbsolutePath();		
		properties.load(new FileInputStream(configPath));
		String key_path=properties.getProperty("KEY_PATH");
	    file = new File(classLoader.getResource(key_path).getFile());
	    key_path = file.getAbsolutePath();
	    properties.setProperty("KEY_PATH", key_path);
		String sign_image=properties.getProperty("SIGN_IMAGE");
		file = new File(classLoader.getResource(sign_image).getFile());
		sign_image = file.getAbsolutePath();
		properties.setProperty("SIGN_IMAGE", sign_image);
		DigMapConfig dc = new DigMapConfig(properties);
		return dc;		
	}
	
//	public static Configuration loadMapFishPrintConfig() 
//	{
//		ClassLoader classLoader = TestUtils.class.getClassLoader();
//	    File configFile = new File(classLoader.getResource(MFP_CONFIG_FILE).getFile());		
//	    System.out.println(configFile.getAbsolutePath());
//		Configuration configuration = null;
//		try {
//			configuration = configurationFactory.getConfig(configFile);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			System.out.println("sdf");
//			e.printStackTrace();
//		}
//		System.out.println(configuration.toString());
//		return configuration;
//	}
	
	public static PJsonObject loadMapFishPrintSpec() throws FileNotFoundException, IOException
	{	
		ClassLoader classLoader = TestUtils.class.getClassLoader();
	    File specFile = new File(classLoader.getResource(MFP_SPEC_FILE).getFile());
	    final InputStream inSpecFile = new FileInputStream(specFile);
		final String jsonConfiguration = CharStreams.toString(new InputStreamReader(inSpecFile, StandardCharsets.UTF_8));
		PJsonObject jsonSpec = MapPrinter.parseSpec(jsonConfiguration);
		return jsonSpec;
	}		
	

	
	
}
