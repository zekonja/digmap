package hr.webstar_consulting.fis.signer;

import hr.apis_it.fin._2012.types.f73.BrojRacunaType;
import hr.apis_it.fin._2012.types.f73.RacunType;
import java.security.KeyStore;
import java.security.Signature;
import java.util.logging.Logger;

/**
 * @author Tihana
 *
 */
public class FiscalHashCode {
	
	private static final Logger log = Logger.getLogger(FiscalHashCode.class.getName());	
	
	public static void main(String[] args) throws Exception{
		KeyStore.PrivateKeyEntry pke = KeyManager.getPrivateKeyEntry(args[6],args[7]);
		getFiscalHash(args[0],args[1],args[2],args[3],args[4],args[5],pke);
		
	}


	public static String getFiscalHash(String oib, String datVrijeme, String brOznRac, String oznPosPr, String oznNapUr, String iznosUkupno,KeyStore.PrivateKeyEntry pke) {
		RacunType r = new RacunType();
		r.setOib(oib);
		r.setDatVrijeme(datVrijeme);
		BrojRacunaType br = new BrojRacunaType();		
		br.setBrOznRac(brOznRac);
		br.setOznPosPr(oznPosPr);
		br.setOznNapUr(oznNapUr);
		r.setBrRac(br);
		r.setIznosUkupno(iznosUkupno);
		return getFiscalHash(r,pke);
	}



	public static String getFiscalHash(RacunType r,KeyStore.PrivateKeyEntry pke) {
		//concatenate all input data
		StringBuffer  buffer = new StringBuffer();
		buffer.append(r.getOib());
		buffer.append(r.getDatVrijeme().replace('T',' '));
		buffer.append(r.getBrRac().getBrOznRac());
		buffer.append(r.getBrRac().getOznPosPr());
		buffer.append(r.getBrRac().getOznNapUr());
		buffer.append(r.getIznosUkupno());	
		log.info("FiscalHashInput: "+buffer.toString());
		// sign using RSA-SHA1
		byte[] signedString = null;
		try {
			Signature signature = Signature.getInstance( "SHA1withRSA" );
			signature.initSign(pke.getPrivateKey());
			signature.update(buffer.toString().getBytes() );
			signedString = signature.sign();
		}
		catch ( Exception e ) {
			// Java has SHA1withRSA 
			e.printStackTrace();
		}
		log.info("Final 32-digit hash code : " + MD5Hex.MD5Hex(signedString));
		return  MD5Hex.MD5Hex(signedString);
	}
}	
