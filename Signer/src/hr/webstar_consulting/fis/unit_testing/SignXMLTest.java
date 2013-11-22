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
import hr.webstar_consulting.fis.utils.FiscalHashCode;
import hr.webstar_consulting.fis.utils.KeyManager;
import hr.webstar_consulting.fis.utils.LoadTestData;
import hr.webstar_consulting.fis.utils.MD5Hex;
import hr.webstar_consulting.fis.utils.SignXML;

public class SignXMLTest {
	private static final Logger log = Logger.getLogger(SignXMLTest.class.getName());	
	@Test
	public void testSignedXML() throws Exception {

		
	}
}
