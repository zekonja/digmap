package hr.webstar_consulting.fis.utils;



import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.Enumeration;

public class KeyManager  {
	
	public static String keyFileName="key_folder/fiskaltest0.pfx";
	public static String keyPassword = "fiskaltest";	
	
	public static KeyStore.PrivateKeyEntry getPrivateKeyEntry(String keyFileName,String keyPassword)  throws Exception
	{
		//Private keys are saved in .pfx format
		KeyStore keyStore = KeyStore.getInstance("pkcs12");
		keyStore.load(new FileInputStream(keyFileName),keyPassword.toCharArray()); 
		//Get key alias
		String keyAlias = keyStore.aliases().nextElement();		
		//pkcs12 key store contains only private keys
		KeyStore.PrivateKeyEntry keyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(keyAlias, new KeyStore.PasswordProtection(keyPassword.toCharArray()));		
		return keyEntry;		
	}		
	
	
	
	public static KeyStore.PrivateKeyEntry getPrivateKeyEntry()  throws Exception
	{
		return getPrivateKeyEntry(keyFileName,keyPassword);
	}


}
