package hr.webstar_consulting.fis.examples;

import org.w3c.dom.Document;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Enumeration;

import hr.apis_it.fin._2012.services.fiskalizacijaservice.FiskalizacijaPortType;
import hr.apis_it.fin._2012.services.fiskalizacijaservice.FiskalizacijaService;
import hr.apis_it.fin._2012.types.f73.RacunOdgovor;
import hr.apis_it.fin._2012.types.f73.RacunZahtjev;
import hr.webstar_consulting.fis.signer.*;
import hr.webstar_consulting.fis.utils.KeyManager;
import hr.webstar_consulting.fis.utils.LoadTestData;

public class SignerExample {	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		
		
		//Write info about keys
		String trustStrore = "./key_folder/cacerts";
		System.out.println(trustStrore);
		String trustStorePassword  = "changeit";
		System.setProperty("javax.net.ssl.trustStrore", trustStrore);
		System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);		
		KeyStore keyStore = KeyStore.getInstance("jks");
		String keyStorePath = System.getProperty("javax.net.ssl.trustStrore");
		System.out.println("KeyStorePath: "+keyStorePath);
		String keyStorePass = System.getProperty("javax.net.ssl.trustStorePassword");
		System.out.println("KeyStorePass: "+keyStorePass);
		
		//Load keyStore
		FileInputStream fis = new FileInputStream(keyStorePath);
		keyStore.load(fis, keyStorePass.toCharArray());
		fis.close();
		listKEyAliases(keyStore);	
		
		//Initiate service
		FiskalizacijaService fs = new FiskalizacijaService();
		FiskalizacijaPortType fpt =  fs.getFiskalizacijaPortType();				

	
		//Load unsigned xmlString for bill
		String unsignedBillStringXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><tns:RacunZahtjev xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" Id=\"signXmlId\" xmlns:tns=\"http://www.apis-it.hr/fin/2012/types/f73\"><tns:Zaglavlje><tns:IdPoruke>60c06a12-ff4c-4210-bf4f-d730866b1e68</tns:IdPoruke><tns:DatumVrijeme>22.10.2012T18:04:26</tns:DatumVrijeme></tns:Zaglavlje><tns:Racun><tns:Oib>01030803852</tns:Oib><tns:USustPdv>true</tns:USustPdv><tns:DatVrijeme>22.10.2012T18:04:26</tns:DatVrijeme><tns:OznSlijed>P</tns:OznSlijed><tns:BrRac><tns:BrOznRac>1</tns:BrOznRac><tns:OznPosPr>1</tns:OznPosPr><tns:OznNapUr>1</tns:OznNapUr></tns:BrRac><tns:Pdv><tns:Porez><tns:Stopa>25.00</tns:Stopa><tns:Osnovica>10.00</tns:Osnovica><tns:Iznos>2.50</tns:Iznos></tns:Porez></tns:Pdv><tns:IznosUkupno>12.50</tns:IznosUkupno><tns:NacinPlac>G</tns:NacinPlac><tns:OibOper>57289294894</tns:OibOper><tns:ZastKod>d7ec0ee48857553a9b4ada78cb2c3a7a</tns:ZastKod><tns:NakDost>false</tns:NakDost></tns:Racun></tns:RacunZahtjev>";
		System.out.println("-----------------------");
		System.out.println("Unsigned Bill:");		
		System.out.println(unsignedBillStringXML);
		System.out.println("-----------------------");
		//Load key
		KeyStore.PrivateKeyEntry pke=KeyManager.getPrivateKeyEntry();
		//Write output
		Document signedDoc = Signer.getSignedDocument(unsignedBillStringXML,pke);
		System.out.println("-----------------------");
		System.out.println("Signed Bill:");		
		System.out.println(signedDoc);
		System.out.println("-----------------------");		
		//Create new bill request
		RacunZahtjev rz= (RacunZahtjev) Signer.convertDocumentToObject(signedDoc,RacunZahtjev.class); 
		RacunOdgovor ro= fpt.racuni(rz);
		System.out.println("-----------------------");
		System.out.println("RacunZahtjevNo4:");
		Signer.printOutObject(rz);
		System.out.println("-----------------------");
		System.out.println("RacunOdgovor:");		
		Signer.printOutObject(ro);
		System.out.println("-----------------------");
		System.out.println("JIR4:");
		System.out.println(ro.getJir());	
		System.out.println("-----------------------");		
		
		

		
		//Create new bill request
		rz=LoadTestData.testCaseRacunZahtjevNo3();
		//Sign request
		rz = (RacunZahtjev) Signer.getSignedObject(rz,pke);		
		//Sign request
		rz = (RacunZahtjev) Signer.getSignedObject(rz,pke);		
		ro= fpt.racuni(rz);
		System.out.println("-----------------------");
		System.out.println("RacunZahtjevNo4:");
		Signer.printOutObject(rz);
		System.out.println("-----------------------");
		System.out.println("RacunOdgovor:");		
		Signer.printOutObject(ro);
		System.out.println("-----------------------");
		System.out.println("JIR4:");
		System.out.println(ro.getJir());	
		System.out.println("-----------------------");
		
		}
	
	
	public static void listKEyAliases(KeyStore keyStore ) throws KeyStoreException{
		// List the aliases
		Enumeration<String> e = keyStore.aliases();
		for (; e.hasMoreElements(); ) {
			String keyAlias = (String)e.nextElement();
			System.out.println("Key alias: "+keyAlias); 
			// Does alias refer to a private key?
			boolean b = keyStore.isKeyEntry(keyAlias);
			System.out.println("isKeyEntry: "+b);
			// Does alias refer to a trusted certificate?
			b = keyStore.isCertificateEntry(keyAlias);
			System.out.println("isCertificateEntry: "+b);
		}		
		
	}
	

}
