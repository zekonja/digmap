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
import org.junit.Test;

import hr.webstar_consulting.fis.signer.*;
import hr.webstar_consulting.fis.utils.FiscalHashCode;
import hr.webstar_consulting.fis.utils.KeyManager;
import hr.webstar_consulting.fis.utils.LoadTestData;
import hr.webstar_consulting.fis.utils.MD5Hex;

public class FiscalServiceTest {

	@Test
	public void testSignedXML() throws Exception {

		//Test MD5 hash
		assertEquals("Test MD5 hash code ", MD5Hex.MD5Hex("abcde"),"ab56b4d92b40713acc5af89985d4b786");
		assertEquals("Test MD5 hash code ", MD5Hex.MD5Hex("12345"),"827ccb0eea8a706c4c34a16891f84e7b");

		
		System.setProperty("javax.net.ssl.trustStrore", "./key_folder/cacerts");
		System.setProperty("javax.net.ssl.trustStorePassword", "changeit");		
		//Certificate issued to Krunoslav Hrnjak
		KeyStore.PrivateKeyEntry pke00=KeyManager.getPrivateKeyEntry("./key_folder/fiskaltest0.pfx","fiskaltest");
		//Certificate issued to Web Star		
		KeyStore.PrivateKeyEntry pke01=KeyManager.getPrivateKeyEntry("./key_folder/fiskaltest1.pfx","fiskaltest");

		FiskalizacijaService fs = new FiskalizacijaService();
		FiskalizacijaPortType fpt =  fs.getFiskalizacijaPortType();		
		
		//Test for fiscal hash code
		RacunZahtjev rz01 = LoadTestData.testCaseRacunZahtjevNo1();
		RacunType racun01 = rz01.getRacun();
		String hashCodeReferentValue01 = "d910c2a2d6cdea5a6d6a09fcbc8440f0";
		assertEquals("Test fiscal hash code ", hashCodeReferentValue01, FiscalHashCode.getFiscalHash(racun01,pke01));

		RacunZahtjev rz02 = LoadTestData.testCaseRacunZahtjevNo2();
		RacunType racun02 = rz02.getRacun();
		String hashCodeReferentValue02 = "7a4e98471503814cafe2b749a4ef7212";
		assertEquals("Test fiscal hash code ", hashCodeReferentValue02, FiscalHashCode.getFiscalHash(racun02,pke01));		

		RacunZahtjev rz03 = LoadTestData.testCaseRacunZahtjevNo3();
		RacunType racun03 = rz03.getRacun();
		String hashCodeReferentValue03 = "03f8a12d33695833c1d08b71e4141b26";
		assertEquals("Test fiscal hash code ", hashCodeReferentValue03, FiscalHashCode.getFiscalHash(racun03,pke01));	
		
		
		//Test for messaging
		String echoMessageReferentValue="Echo";		
		assertEquals("Test server communication with echo message", echoMessageReferentValue, fs.getFiskalizacijaPortType().echo(echoMessageReferentValue));
		
		//send unsigned message
		RacunOdgovor ro= fpt.racuni(rz01);	
		String billMessageReferentValue="Neispravan digitalni potpis.";		
		String billErrorMessage=ro.getGreske().getGreska().get(0).getPorukaGreske();		
		assertEquals("Test server communication with unsigned RacunZahtjev message", billMessageReferentValue, billErrorMessage);
		
		//send signed message with self made certificate
		rz01 = (RacunZahtjev)  Signer.getSignedObject(rz01,pke01);
		ro = fpt.racuni(rz01);	
		billMessageReferentValue="Certifikat nije izdan od strane FINA-e.";
		billErrorMessage=ro.getGreske().getGreska().get(0).getPorukaGreske();		
		assertEquals("Test server communication with self made certificate and signed RacunZahtjev message", billMessageReferentValue, billErrorMessage);
						
		
		//send signed message with self made certificate
		PoslovniProstorZahtjev ppz = LoadTestData.testCasePoslovniProstorZahtjevNo0();     
		ppz = (PoslovniProstorZahtjev) Signer.getSignedObject(ppz,pke01);			
		PoslovniProstorOdgovor ppo = fpt.poslovniProstor(ppz);	
		String spaceErrorMessageReferentValue="Certifikat nije izdan od strane FINA-e.";		
		String spaceErrorMessage=ppo.getGreske().getGreska().get(0).getPorukaGreske();	
		assertEquals("Test server communication with self made certificate and signed PoslovniProstorZahtjev message", spaceErrorMessageReferentValue, spaceErrorMessage);	
		
	
		//send signed bill message with FINA certificate
		rz01 = LoadTestData.testCaseRacunZahtjevNo1();
		rz01 = (RacunZahtjev)  Signer.getSignedObject(rz01,pke01);
		ro = fpt.racuni(rz01);	
		String billOIBErrorMessageReferentValue="OIB iz poruke zahtjeva nije jednak OIB-u iz certifikata.";		
		String billOIBErrorMessage=ppo.getGreske().getGreska().get(0).getPorukaGreske();	
		//assertEquals("Test server communication with FINA certificate and signed RacunZahtjev message with wrong OIB", billOIBErrorMessageReferentValue, billOIBErrorMessage);

		
		//send signed bill message with FINA certificate
		rz01 = LoadTestData.testCaseRacunZahtjevNo3();
		rz01 = (RacunZahtjev)  Signer.getSignedObject(rz01,pke00);
		ro = fpt.racuni(rz01);	
		billMessageReferentValue=rz01.getZaglavlje().getIdPoruke();
		String billMessage=ro.getZaglavlje().getIdPoruke();
		assertEquals("Test server communication with FINA certificate and signed RacunZahtjev message", billMessageReferentValue, billMessage);
		assertNotNull("Returned message has non empty JIR", ro.getJir());
						
		
		//send signed space message with FINA certificate
		ppz = LoadTestData.testCasePoslovniProstorZahtjevNo0();     
		ppz = (PoslovniProstorZahtjev) Signer.getSignedObject(ppz,pke00);			
		ppo = fpt.poslovniProstor(ppz);		
		String spaceMessageReferentValue=ppo.getZaglavlje().getIdPoruke();
		String spaceMessage=ppo.getZaglavlje().getIdPoruke();
		assertEquals("Test server communication with FINA certificate and signed PoslovniProstorZahtjev message", spaceMessageReferentValue, spaceMessage);			
		
	}
}
