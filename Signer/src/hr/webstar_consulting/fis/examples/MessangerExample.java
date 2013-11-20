package hr.webstar_consulting.fis.examples;

import hr.apis_it.fin._2012.services.fiskalizacijaservice.FiskalizacijaPortType;
import hr.apis_it.fin._2012.services.fiskalizacijaservice.FiskalizacijaService;
import hr.apis_it.fin._2012.types.f73.PoslovniProstorOdgovor;
import hr.apis_it.fin._2012.types.f73.PoslovniProstorZahtjev;
import hr.apis_it.fin._2012.types.f73.RacunOdgovor;
import hr.apis_it.fin._2012.types.f73.RacunZahtjev;

import java.security.KeyStore;
import java.security.cert.CertPath;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;

import hr.webstar_consulting.fis.signer.*;
import hr.webstar_consulting.fis.utils.LoadTestData;
import hr.webstar_consulting.fis.utils.SOAPLoggingHandler;

public class MessangerExample {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		
		String trustStrore = "./key_folder/cacerts";		
		String trustStorePassword  = "changeit";		
		System.setProperty("javax.net.ssl.trustStrore", trustStrore);
		System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);		
		
		//Load private key entry
		KeyStore.PrivateKeyEntry pke=KeyManager.getPrivateKeyEntry("./key_folder/fiskaltest0.pfx","fiskaltest");

		//Initiate service
		FiskalizacijaService fs = new FiskalizacijaService();
		FiskalizacijaPortType fpt =  fs.getFiskalizacijaPortType();		

		//setup JAX-WS SOAP message handler for debugging purpose - print out Out-bound and In-bound messages
		SOAPLoggingHandler slh = new SOAPLoggingHandler();
		@SuppressWarnings("rawtypes")
		List<Handler> handlerChain = new ArrayList<Handler>();
		handlerChain.add(slh);
		((BindingProvider) fpt).getBinding().setHandlerChain(handlerChain);		




		//Test echo message
		String anwserEcho= fpt.echo("Server radi");
		System.out.println("-----------------------");
		System.out.println(anwserEcho);


		//Create new bill request
		RacunZahtjev rz=LoadTestData.testCaseRacunZahtjevNo3();
		//Sign request
		rz = (RacunZahtjev) Signer.getSignedObject(rz,pke);
		//Send request and receive answer
		RacunOdgovor ro= fpt.racuni(rz);
		System.out.println("-----------------------");
		System.out.println("RacunZahtjev:");
		Signer.printOutObject(rz);
		System.out.println("-----------------------");
		System.out.println("RacunOdgovor:");		
		Signer.printOutObject(ro);


		//Create new space request
		PoslovniProstorZahtjev ppz = LoadTestData.testCasePoslovniProstorZahtjevNo0();     
		//Sign request
		ppz = (PoslovniProstorZahtjev) Signer.getSignedObject(ppz,pke);			
		//Send request and receive answer
		PoslovniProstorOdgovor ppo = fpt.poslovniProstor(ppz);	
		System.out.println("-----------------------");
		System.out.println("PoslovniProstorZahtjev:");
		Signer.printOutObject(ppz);		
		System.out.println("-----------------------");
		System.out.println("PoslovniProstorOdgovor:");
		Signer.printOutObject(ppo);	
		
		System.out.println("-----------------------");
		System.out.println("JIR:");
		System.out.println(ro.getJir());	
		System.out.println("-----------------------");				

	}
	
	

}
