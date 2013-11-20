package hr.webstar_consulting.fis.unit_testing;

import static org.junit.Assert.*;

import java.security.KeyStore;
import hr.apis_it.fin._2012.services.fiskalizacijaservice.FiskalizacijaPortType;
import hr.apis_it.fin._2012.services.fiskalizacijaservice.FiskalizacijaService;
import hr.apis_it.fin._2012.types.f73.PoslovniProstorOdgovor;
import hr.apis_it.fin._2012.types.f73.PoslovniProstorZahtjev;
import hr.apis_it.fin._2012.types.f73.RacunOdgovor;
import hr.apis_it.fin._2012.types.f73.RacunType;
import hr.apis_it.fin._2012.types.f73.RacunZahtjev;
import java.util.logging.Logger;
import org.junit.Test;

import hr.webstar_consulting.fis.signer.*;
import hr.webstar_consulting.fis.utils.LoadTestData;

public class SignXMLTest {
	
	private static final Logger log = Logger.getLogger(SignXMLTest.class.getName());	

	@Test
	public void testSignedXML() throws Exception {

		//Test MD5 hash
		assertEquals("Test MD5 hash code ", MD5Hex.MD5Hex("abcde"),"ab56b4d92b40713acc5af89985d4b786");
		assertEquals("Test MD5 hash code ", MD5Hex.MD5Hex("12345"),"827ccb0eea8a706c4c34a16891f84e7b");

		
		System.setProperty("javax.net.ssl.trustStrore", "./key_folder/cacerts");
		System.setProperty("javax.net.ssl.trustStorePassword", "changeit");		
		KeyStore.PrivateKeyEntry pke01=KeyManager.getPrivateKeyEntry();
		KeyStore.PrivateKeyEntry pke02=KeyManager.getPrivateKeyEntry("./key_folder/fiskaltest1.pfx","fiskaltest");

		FiskalizacijaService fs = new FiskalizacijaService();
		FiskalizacijaPortType fpt =  fs.getFiskalizacijaPortType();		
		
		//Test for fiscal hash code
		RacunZahtjev rz01 = LoadTestData.testCaseRacunZahtjevNo1();
		RacunType racun01 = rz01.getRacun();
		String hashCodeReferentValue01 = "d910c2a2d6cdea5a6d6a09fcbc8440f0";
		assertEquals("Test fiscal hash code ", hashCodeReferentValue01, FiscalHashCode.getFiscalHash(racun01,pke02));

		RacunZahtjev rz02 = LoadTestData.testCaseRacunZahtjevNo2();
		RacunType racun02 = rz02.getRacun();
		String hashCodeReferentValue02 = "7a4e98471503814cafe2b749a4ef7212";
		assertEquals("Test fiscal hash code ", hashCodeReferentValue02, FiscalHashCode.getFiscalHash(racun02,pke02));		

		RacunZahtjev rz03 = LoadTestData.testCaseRacunZahtjevNo3();
		RacunType racun03 = rz03.getRacun();
		String hashCodeReferentValue03 = "7a4e98471503814cafe2b749a4ef7212";
		assertEquals("Test fiscal hash code ", hashCodeReferentValue03, FiscalHashCode.getFiscalHash(racun03,pke02));	
		
		
		//Test for messaging
		String echoMessageReferentValue="Echo";		
		assertEquals("Test server communication with echo message", echoMessageReferentValue, fs.getFiskalizacijaPortType().echo(echoMessageReferentValue));
		
		//send unsigned message
		RacunOdgovor ro= fpt.racuni(rz01);	
		String billMessageReferentValue="Neispravan digitalni potpis.";		
		String billErrorMessage=ro.getGreske().getGreska().get(0).getPorukaGreske();		
		assertEquals("Test server communication with unsigned RacunZahtjev message", billMessageReferentValue, billErrorMessage);
		
		//send signed message with self made certificate
		rz01 = (RacunZahtjev)  SignXML.getSignedObject(rz01,pke02);
		ro = fpt.racuni(rz01);	
		billMessageReferentValue="Certifikat nije izdan od strane FINA-e.";
		billErrorMessage=ro.getGreske().getGreska().get(0).getPorukaGreske();		
		assertEquals("Test server communication with self made certificate and signed RacunZahtjev message", billMessageReferentValue, billErrorMessage);
						
		
		//send signed message with self made certificate
		PoslovniProstorZahtjev ppz = LoadTestData.testCasePoslovniProstorZahtjevNo0();     
		ppz = (PoslovniProstorZahtjev) SignXML.getSignedObject(ppz,pke02);			
		PoslovniProstorOdgovor ppo = fpt.poslovniProstor(ppz);	
		String spaceErrorMessageReferentValue="Certifikat nije izdan od strane FINA-e.";		
		String spaceErrorMessage=ppo.getGreske().getGreska().get(0).getPorukaGreske();	
		assertEquals("Test server communication with self made certificate and signed PoslovniProstorZahtjev message", spaceErrorMessageReferentValue, spaceErrorMessage);	
		
		
		//send signed message with FINA certificate
		rz01 = LoadTestData.testCaseRacunZahtjevNo1();
		rz01 = (RacunZahtjev)  SignXML.getSignedObject(rz01,pke01);
		ro = fpt.racuni(rz01);	
		billMessageReferentValue=rz01.getZaglavlje().getIdPoruke();
		String billMessage=ro.getZaglavlje().getIdPoruke();
		assertEquals("Test server communication with FINA certificate and signed RacunZahtjev message", billMessageReferentValue, billMessage);
						
		
		//send signed message with FINA certificate
		ppz = LoadTestData.testCasePoslovniProstorZahtjevNo0();     
		ppz = (PoslovniProstorZahtjev) SignXML.getSignedObject(ppz,pke01);			
		ppo = fpt.poslovniProstor(ppz);		
		String spaceMessageReferentValue=ppo.getZaglavlje().getIdPoruke();
		String spaceMessage=ppo.getZaglavlje().getIdPoruke();
		assertEquals("Test server communication with FINA certificate and signed PoslovniProstorZahtjev message", spaceMessageReferentValue, spaceMessage);
		

		log.info("Tests completed");
		
		
		
	}
}
