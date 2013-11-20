package hr.webstar_consulting.fis.messanger;

import hr.apis_it.fin._2012.services.fiskalizacijaservice.FiskalizacijaPortType;
import hr.apis_it.fin._2012.services.fiskalizacijaservice.FiskalizacijaService;
import hr.apis_it.fin._2012.types.f73.PoslovniProstorOdgovor;
import hr.apis_it.fin._2012.types.f73.PoslovniProstorZahtjev;
import hr.apis_it.fin._2012.types.f73.RacunOdgovor;
import hr.apis_it.fin._2012.types.f73.RacunZahtjev;

import java.io.BufferedWriter;
import java.io.File;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;

import org.omg.CORBA.ExceptionList;
import org.w3c.dom.Document;

import hr.webstar_consulting.fis.signer.*;
import hr.webstar_consulting.fis.utils.LoadTestData;

public class Messanger {

	/**
	 * Command line interface for communication with Tax Service
	 * @param args[0] message type {echo|bill|space}
	 * @param args[1] signed xml file name or echo message
	 * @param args[2] output file or null for output on the screen
	 * @throws Exception
	 * 
	 * "echo"   "Echo Message" "./misc/EchoOutput.txt"
	 * "bill"   "./misc/SignedBill.xml"  "./misc/BillOutput.xml"
	 * "space"  "./misc/SignedSpace.xml" "./misc/SpaceOutput.xml"
	 */
	public static void main(String[] args) throws Exception {

		String messageType = args[0];
		String inputFileName = args[1];
		String outputFileName = args[2];
		System.out.println(args[0]+" "+args[1]+" "+args[2]);
		




		//Initiate service
		FiskalizacijaService fs = new FiskalizacijaService();
		FiskalizacijaPortType fpt =  fs.getFiskalizacijaPortType();	


		switch(messageType) {
		case "echo": {			
			String echoAnwser= fpt.echo(args[1]);
			if (outputFileName.compareTo("null")==0) System.out.println(echoAnwser);
			else Signer.writeTextToTxtFile(echoAnwser, outputFileName);
		}  break;
		case "bill": {			
			//read file
			Document doc= Signer.readDocumentFromXMLFile(inputFileName);				
			//Create new bill request
			RacunZahtjev rz = new RacunZahtjev();			
			rz= (RacunZahtjev) Signer.convertDocumentToObject(doc, rz.getClass());
			//Send request and receive answer
			RacunOdgovor ro= fpt.racuni(rz);
			if (outputFileName.compareTo("null")==0) {
				System.out.println("-----------------------");
				System.out.println("RacunOdgovor:");		
				Signer.printOutObject(ro);
				System.out.println("-----------------------");				
			}
			else {
				Signer.writeDocumentToXMLFile(Signer.convertObjectToDocument(ro), outputFileName);

			}



		} break;
		case "space": {			
			//read file
			Document doc= Signer.readDocumentFromXMLFile(inputFileName);
			//Create new bill request
			PoslovniProstorZahtjev ppz = new PoslovniProstorZahtjev();
			ppz= (PoslovniProstorZahtjev) Signer.convertDocumentToObject(doc, ppz.getClass());
			//Send request and receive answer
			//Send request and receive answer
			PoslovniProstorOdgovor ppo = fpt.poslovniProstor(ppz);	
			if (outputFileName.compareTo("null")==0) {
				System.out.println("-----------------------");
				System.out.println("PoslovniProstorOdgovor:");		
				Signer.printOutObject(ppo);
				System.out.println("-----------------------");				
			}
			else {
				Signer.writeDocumentToXMLFile(Signer.convertObjectToDocument(ppo), outputFileName);
			}			
		} break;
		default : { 
			String errMsg="Message type param can be one of the following: {echo|bill|space}";
			System.err.println(errMsg);
			throw new Exception(errMsg);
		}
		}

	}

}
