package hr.webstar_consulting.fis.utils;

import java.io.StringReader;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;


import hr.apis_it.fin._2012.types.f73.AdresniPodatakType;
import hr.apis_it.fin._2012.types.f73.BrojRacunaType;
import hr.apis_it.fin._2012.types.f73.NacinPlacanjaType;
import hr.apis_it.fin._2012.types.f73.OznakaSlijednostiType;
import hr.apis_it.fin._2012.types.f73.PdvType;
import hr.apis_it.fin._2012.types.f73.PorezType;
import hr.apis_it.fin._2012.types.f73.PoslovniProstorType;
import hr.apis_it.fin._2012.types.f73.PoslovniProstorZahtjev;
import hr.apis_it.fin._2012.types.f73.RacunType;
import hr.apis_it.fin._2012.types.f73.RacunZahtjev;
import hr.apis_it.fin._2012.types.f73.ZaglavljeType;
import hr.webstar_consulting.fis.signer.Signer;

public class LoadTestData {

	/**
	 * Contains test data
	 * @param args
	 * @throws Exception 
	 */

	public static RacunZahtjev testCaseRacunZahtjevNo0() throws Exception {

		KeyStore.PrivateKeyEntry pke=KeyManager.getPrivateKeyEntry();
		
		// load test Data for RacunZahtjev
		RacunZahtjev rz = new RacunZahtjev();	
		
	

		ZaglavljeType rzZ = new ZaglavljeType();		
		rzZ.setIdPoruke(UUID.randomUUID().toString());
		rzZ.setIdPoruke("f81d4fae-7dec-11d0-a765-00a0c91e6bf6");
//		rzZ.setDatumVrijeme(new SimpleDateFormat("dd.MM.yyyy'T'HH:mm:ss").format(new Date()));
		rzZ.setDatumVrijeme("01.09.2012T21:10:34");
		rz.setZaglavlje(rzZ);


		RacunType rzRacun = new RacunType();
		rzRacun.setOib("57289294894");
		rzRacun.setUSustPdv(false);
//		rzRacun.setDatVrijeme(new SimpleDateFormat("dd.MM.yyyy'T'HH:mm:ss").format(new Date()));
		rzRacun.setDatVrijeme("01.09.2012T21:10:34");
		rzRacun.setOznSlijed(OznakaSlijednostiType.N);	
		BrojRacunaType br = new BrojRacunaType();
		br.setBrOznRac("123");
		br.setOznNapUr("1");
		br.setOznPosPr("POSL1");
		rzRacun.setBrRac(br);		
		PorezType p = new PorezType();
		p.setIznos("25.00");
		p.setOsnovica("100.00");
		p.setStopa("25.00");
		List<PorezType> porezList = new ArrayList<PorezType>();
		porezList.add(p);
		PdvType pdv = new PdvType();
		pdv.getPorez().add(p);
		rzRacun.setPdv(pdv);
		rzRacun.setIznosUkupno("125.00");		
		rzRacun.setNacinPlac(NacinPlacanjaType.G);
		rzRacun.setOibOper("57289294894");
		rzRacun.setZastKod(FiscalHashCode.getFiscalHash(rzRacun,pke));
		rzRacun.setNakDost(false);

		rz.setId("RacunZahtjev");
		rz.setRacun(rzRacun);
		
		return rz;	
	}


	public static RacunZahtjev testCaseRacunZahtjevNo1() throws Exception {

		KeyStore.PrivateKeyEntry pke=KeyManager.getPrivateKeyEntry();

		// load test Data for RacunZahtjev
		RacunZahtjev rz01 = new RacunZahtjev();		

		ZaglavljeType rzZ01 = new ZaglavljeType();
		rzZ01.setIdPoruke("410fdccc11e51c1e22f6d265c3774cee");
		//		rzZ.setDatumVrijeme(new SimpleDateFormat("dd.MM.yyyy'T'HH:mm:ss").format(new Date()));
		rzZ01.setDatumVrijeme("28.11.2012T23:53:30");
		rz01.setZaglavlje(rzZ01);	

		RacunType rzRacun01 = new RacunType();
		rzRacun01.setOib("12345678911");
		rzRacun01.setUSustPdv(true);
		//		rzRacun.setDatVrijeme(new SimpleDateFormat("dd.MM.yyyy'T'HH:mm:ss").format(new Date()));
		rzRacun01.setDatVrijeme("28.11.2012T23:53:30");
		rzRacun01.setOznSlijed(OznakaSlijednostiType.P);	
		BrojRacunaType br01 = new BrojRacunaType();
		br01.setBrOznRac("123");
		br01.setOznNapUr("1");
		br01.setOznPosPr("POSL1");
		rzRacun01.setBrRac(br01);	

		PdvType pdv01 = new PdvType();


		PorezType p01 = new PorezType();
		p01.setIznos("2.50");
		p01.setOsnovica("10.00");
		p01.setStopa("25.00");
		pdv01.getPorez().add(p01);

		p01 = new PorezType();
		p01.setIznos("1.00");
		p01.setOsnovica("10.00");
		p01.setStopa("10.00");
		pdv01.getPorez().add(p01);

		rzRacun01.setPdv(pdv01);
		rzRacun01.setIznosUkupno("145.25");		
		rzRacun01.setIznosMarza("0.00");
		rzRacun01.setNacinPlac(NacinPlacanjaType.G);
		rzRacun01.setOibOper("32165498721");
		rzRacun01.setZastKod("cd3062c7d2c53640da09a1961016e1a1");
		rzRacun01.setZastKod(FiscalHashCode.getFiscalHash(rzRacun01,pke));

		rzRacun01.setNakDost(false);
		rz01.setId("RacunZahtjev");
		rz01.setRacun(rzRacun01);

		return rz01;

	}

	public static RacunZahtjev testCaseRacunZahtjevNo2() throws Exception {

		KeyStore.PrivateKeyEntry pke=KeyManager.getPrivateKeyEntry();
		// load test Data for RacunZahtjev
		// "01030803852" "22.10.2012 18:04:26" "1" "1" "1" "12.50" 
		RacunZahtjev rz02 = new RacunZahtjev();		

		ZaglavljeType rzZ02 = new ZaglavljeType();
		rzZ02.setIdPoruke("60c06a12-ff4c-4210-bf4f-d730866b1e68");
		//		rzZ.setDatumVrijeme(new SimpleDateFormat("dd.MM.yyyy'T'HH:mm:ss").format(new Date()));
		rzZ02.setDatumVrijeme("22.10.2012T18:04:26");
		rz02.setZaglavlje(rzZ02);		

		RacunType rzRacun02 = new RacunType();
		rzRacun02.setOib("01030803852");
		rzRacun02.setUSustPdv(true);
		//		rzRacun.setDatVrijeme(new SimpleDateFormat("dd.MM.yyyy'T'HH:mm:ss").format(new Date()));
		rzRacun02.setDatVrijeme("22.10.2012T18:04:26");
		rzRacun02.setOznSlijed(OznakaSlijednostiType.P);	
		BrojRacunaType br02 = new BrojRacunaType();
		br02.setBrOznRac("1");
		br02.setOznNapUr("1");
		br02.setOznPosPr("1");
		rzRacun02.setBrRac(br02);	

		PdvType pdv02 = new PdvType();


		PorezType p02 = new PorezType();
		p02.setIznos("2.50");
		p02.setOsnovica("10.00");
		p02.setStopa("25.00");
		pdv02.getPorez().add(p02);
		

		rzRacun02.setPdv(pdv02);
		rzRacun02.setIznosUkupno("12.50");		
		rzRacun02.setNacinPlac(NacinPlacanjaType.G);
		rzRacun02.setOibOper("57289294894");
		// hash should return ("d7ec0ee48857553a9b4ada78cb2c3a7a");
		rzRacun02.setZastKod(FiscalHashCode.getFiscalHash(rzRacun02,pke));

		rzRacun02.setNakDost(false);
		rz02.setId("signXmlId");
		rz02.setRacun(rzRacun02);	

		return rz02;

	}	

	public static RacunZahtjev testCaseRacunZahtjevNo3() {

		try {

			// load test Data for RacunZahtjev		
			String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><tns:RacunZahtjev xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" Id=\"signXmlId\" xmlns:tns=\"http://www.apis-it.hr/fin/2012/types/f73\"><tns:Zaglavlje><tns:IdPoruke>60c06a12-ff4c-4210-bf4f-d730866b1e68</tns:IdPoruke><tns:DatumVrijeme>22.10.2012T18:04:26</tns:DatumVrijeme></tns:Zaglavlje><tns:Racun><tns:Oib>57289294894</tns:Oib><tns:USustPdv>true</tns:USustPdv><tns:DatVrijeme>22.10.2012T18:04:26</tns:DatVrijeme><tns:OznSlijed>P</tns:OznSlijed><tns:BrRac><tns:BrOznRac>1</tns:BrOznRac><tns:OznPosPr>1</tns:OznPosPr><tns:OznNapUr>1</tns:OznNapUr></tns:BrRac><tns:Pdv><tns:Porez><tns:Stopa>25.00</tns:Stopa><tns:Osnovica>10.00</tns:Osnovica><tns:Iznos>2.50</tns:Iznos></tns:Porez></tns:Pdv><tns:IznosUkupno>12.50</tns:IznosUkupno><tns:NacinPlac>G</tns:NacinPlac><tns:OibOper>57289294894</tns:OibOper><tns:ZastKod>d7ec0ee48857553a9b4ada78cb2c3a7a</tns:ZastKod><tns:NakDost>false</tns:NakDost></tns:Racun></tns:RacunZahtjev>";
			System.out.println (xmlString);			
			Document xmlRacunZahtjevDoc = Signer.convertXMLStringToDocument(xmlString);			
			RacunZahtjev rzs = (RacunZahtjev) Signer.convertDocumentToObject(xmlRacunZahtjevDoc,RacunZahtjev.class);
			return rzs;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}		
	
	public static RacunZahtjev testCaseRacunZahtjevNo4() throws  Exception {
		//load data for PoslovniProstor
		String inputFileName = "./misc/UnsignedBill.xml";
		RacunZahtjev rz = new RacunZahtjev();
		Document doc= Signer.readDocumentFromXMLFile(inputFileName);
		//Create new bill request
		rz= (RacunZahtjev) Signer.convertDocumentToObject(doc, rz.getClass());
		return rz;
	}	

	


	public static PoslovniProstorZahtjev testCasePoslovniProstorZahtjevNo0() {	

		//load data for PoslovniProstor
		PoslovniProstorZahtjev  ppz = new PoslovniProstorZahtjev();
		ZaglavljeType ppzZ = new ZaglavljeType();
		ppzZ.setIdPoruke("ca996cc7-fcc3-4c50-961b-40c8b875a5e8");
		ppzZ.setDatumVrijeme(new SimpleDateFormat("dd.MM.yyyy'T'HH:mm:ss").format(new Date()));
		PoslovniProstorType ppzPP = new PoslovniProstorType();
		ppzPP.setOib("57289294894");
		ppzPP.setOznPoslProstora("POSL1");
		AdresniPodatakType ppzPPap = new AdresniPodatakType();
		ppzPPap.setOstaliTipoviPP("Internet trgovina");
		ppzPP.setAdresniPodatak(ppzPPap);
		ppzPP.setRadnoVrijeme("8-16");
		ppzPP.setDatumPocetkaPrimjene(new SimpleDateFormat("dd.MM.yyyy").format(new Date()));
		ppz.setZaglavlje(ppzZ);
		ppz.setPoslovniProstor(ppzPP);		
		ppz.setId("signXmlId");
		return ppz;
	}
	
	public static PoslovniProstorZahtjev testCasePoslovniProstorZahtjevNo1() throws  Exception {
		//load data for PoslovniProstor
		String inputFileName = "./misc/UnsignedSpace.xml";
		PoslovniProstorZahtjev  ppz = new PoslovniProstorZahtjev();
		Document doc= Signer.readDocumentFromXMLFile(inputFileName);
		//Create new bill request
		ppz= (PoslovniProstorZahtjev) Signer.convertDocumentToObject(doc, ppz.getClass());
		return ppz;
	}
	


}
