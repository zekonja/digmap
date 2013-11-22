package hr.webstar_consulting.fis.signer;


import hr.apis_it.fin._2012.services.fiskalizacijaservice.FiskalizacijaPortType;
import hr.apis_it.fin._2012.services.fiskalizacijaservice.FiskalizacijaService;
import hr.apis_it.fin._2012.types.f73.PoslovniProstorOdgovor;
import hr.apis_it.fin._2012.types.f73.PoslovniProstorZahtjev;
import hr.apis_it.fin._2012.types.f73.RacunOdgovor;
import hr.apis_it.fin._2012.types.f73.RacunZahtjev;
import hr.webstar_consulting.fis.utils.KeyManager;
import hr.webstar_consulting.fis.utils.LoadTestData;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.*;
import javax.xml.crypto.dsig.spec.*;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

import java.util.logging.Logger;

public class Signer {

	/**
	 * Sign XML document or FiscalService object
	 *
	 */


	//
	private static final Logger log = Logger.getLogger(Signer.class.getName());
	
	
	/**
	 * Command line interface for signing xml file with given .pfx key
	 * @param args[0] type {bill|space}
	 * @param args[1] source xml file name
	 * @param args[2] destination for signed xml file name or null for stdout
	 * @param args[3] keystore file name
	 * @param args[4] keystore and key password
	 * @throws Exception
	 * 
	 * "bill" "./misc/UnsignedBill.xml"   "./misc/SignedBill.xml"  "./key_folder/fiskaltest0.pfx" "fiskaltest"
	 * "space" "./misc/UnsignedSpace.xml"  "./misc/SignedSpace.xml" "./key_folder/fiskaltest0.pfx" "fiskaltest"
	 */
	
	public static void main(String[] args) throws Exception {
		
		String messageType = args[0];
		String inputFileName = args[1];
		String outputFileName = args[2];
		String keystoreFileName = args[3];
		String keystorePassword = args[4];		

		
		KeyStore.PrivateKeyEntry pke = KeyManager.getPrivateKeyEntry(keystoreFileName,keystorePassword);
		//read file		
		Document doc = readDocumentFromXMLFile(inputFileName);	
		
		
		
		switch(messageType) {
		case "bill": {			
			//Create new bill request	
			RacunZahtjev rz = new RacunZahtjev();
			rz= (RacunZahtjev) Signer.convertDocumentToObject(doc, rz.getClass());
			rz=(RacunZahtjev) getSignedObject(rz, pke);
			//Send request and receive answer
			if (outputFileName.compareTo("null")==0) {
				System.out.println("-----------------------");
				System.out.println("RacunZahtijev signed: ");		
				Signer.printOutObject(rz);
				System.out.println("-----------------------");				
			}
			else {
				writeDocumentToXMLFile(Signer.convertObjectToDocument(rz), outputFileName);
			}
		} break;
		case "space": {			
			//Create new space request
			PoslovniProstorZahtjev ppz= new PoslovniProstorZahtjev();
			ppz= (PoslovniProstorZahtjev) Signer.convertDocumentToObject(doc, ppz.getClass());
			ppz = (PoslovniProstorZahtjev) getSignedObject(ppz, pke);
			if (outputFileName.compareTo("null")==0) {
				System.out.println("-----------------------");
				System.out.println("PoslovniProstorZahtijev signed: ");		
				Signer.printOutObject(ppz);
				System.out.println("-----------------------");				
			}
			else {
				Signer.writeDocumentToXMLFile(Signer.convertObjectToDocument(ppz), outputFileName);
			}			
		} break;
		default : { 
			String errMsg="Message type param can be one of the following: {bill|space}";
			System.err.println(errMsg);
			throw new Exception(errMsg);
		}
		}
		
		
		
	
		
		//Create new bill request


//		System.out.println("-----------------------");
//		System.out.println("RacunZahtjev signed:");
//		Signer.printOutObject(rz);
//		System.out.println("-----------------------");
//
//		//Initiate service
//		FiskalizacijaService fs = new FiskalizacijaService();
//		FiskalizacijaPortType fpt =  fs.getFiskalizacijaPortType();
//		//Send request and receive answer		
//		RacunOdgovor ro= fpt.racuni(rz);
//		System.out.println("-----------------------");		
//		System.out.println("RacunOdgovor:");		
//		Signer.printOutObject(ro);
//		System.out.println("-----------------------");
//		System.out.println("JIR:");
//		System.out.println(ro.getJir());	
//		System.out.println("-----------------------");			
		
		
		
	}
	

	
	public static Document readDocumentFromXMLFile (String inputFileName) throws Exception
	{
		File inputXmlFile = new File(inputFileName);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setNamespaceAware(true);		
		dbFactory.setIgnoringElementContentWhitespace(true);
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(inputXmlFile);	
		doc.getDocumentElement().normalize();
		return doc;
	}
	
	
	
	
	public static void writeDocumentToXMLFile (Document doc, String outputFileName) throws Exception
	{
	    TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        File outputXmlFile = new File(outputFileName);
        StreamResult result =  new StreamResult(outputXmlFile);
        transformer.transform(source, result);			
	}	

	public static void writeTextToTxtFile (String text, String outputFileName) throws Exception
	{
		Writer writer = null;
		try {
			File file = new File(outputFileName);
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(text);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}	

	
	
	public static Object getSignedObject(Object object, KeyStore.PrivateKeyEntry pke) throws Exception
	{
		//Convert object to document
		Document sourceDoc = convertObjectToDocument(object);
		//sign document
		Document signedDocument = Signer.getSignedDocument(sourceDoc,pke);
		//return signed object
		return convertDocumentToObject(signedDocument,object.getClass());
	}

	public static Document convertObjectToDocument(Object object) throws Exception {
		//create new document
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder docBuilder = factory.newDocumentBuilder();
		Document document = docBuilder.newDocument();
		//setup marshaller
		JAXBContext jc = null;
		jc = JAXBContext.newInstance(object.getClass());
		Marshaller marshaller = jc.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);	        
		//Marshall object into document
		marshaller.marshal(object, document);		
		return document;		
	}

	public static Object convertDocumentToObject(Document document,Class className) throws Exception {
		JAXBContext jc = JAXBContext.newInstance(className);
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		return unmarshaller.unmarshal(document.getDocumentElement());	
	}	



	public static void printOutObject(Object object) throws Exception {
		//setup marshaller
		JAXBContext jc = null;
		jc = JAXBContext.newInstance(object.getClass());
		Marshaller marshaller = jc.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);	        
		//Marshall object to System.out
		marshaller.marshal(object, System.out);        		
	}	


	public static boolean validateSignature(Object object, Key publicKey) throws Exception {		
		return validateSignature(convertObjectToDocument(object),publicKey);

	}

	public static boolean validateSignature(Document doc, Key publicKey) throws Exception {
		XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
		NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
		// Create a DOMValidateContext and specify a KeyValue KeySelector and document context
		DOMValidateContext valContext = new DOMValidateContext(publicKey, nl.item(0));
		// Create a DOM XMLSignatureFactory that will be used to unmarshal the 
		// document containing the XMLSignature 
		fac = XMLSignatureFactory.getInstance("DOM");
		// unmarshal the XMLSignature
		XMLSignature signature = fac.unmarshalXMLSignature(valContext);
		// Check core validation status	
		boolean coreValidity = signature.validate(valContext);
		// Check core validation status
		if (coreValidity == false) {
			log.warning("Signature failed core validation"); 
			boolean sv = signature.getSignatureValue().validate(valContext);
			log.warning("Signature validation status: " + sv);
			// check the validation status of each Reference
			@SuppressWarnings("rawtypes")
			Iterator i = signature.getSignedInfo().getReferences().iterator();
			for (int j=0; i.hasNext(); j++) {
				Reference ref = (Reference) i.next();
				boolean refValid = ref.validate(valContext);
				log.warning("ref["+j+"] validity status: " + refValid);				
			}
			return false;

		} else {
			log.warning("Signature passed core validation");
			return true;
		}        		
	}
	
	
	
	public static String convertDocumentToXMLString (Document doc) throws Exception {
		TransformerFactory transformerFactory = TransformerFactory.newInstance(); 
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result =  new StreamResult(new StringWriter());
		transformer.transform(source, result);
		String signedXml = result.getWriter().toString();
		return signedXml;
	}
	
	
	public static Document convertXMLStringToDocument (String xmlString) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setIgnoringComments(true);
		factory.setIgnoringElementContentWhitespace(true);		
		DocumentBuilder docBuilder;
		docBuilder = factory.newDocumentBuilder();
		Document doc = docBuilder.parse(new InputSource(new StringReader(xmlString)));
		return doc;	
	}	
	
	
	public static String getSignedXMLString(String xmlString,KeyStore.PrivateKeyEntry pke) throws Exception {
		return convertDocumentToXMLString(getSignedDocument(xmlString,pke));
		
	}
	
	public static String getSignedXMLString(Document doc,KeyStore.PrivateKeyEntry pke) throws Exception {
		return convertDocumentToXMLString(getSignedDocument(doc,pke));
		
	}
	
	
	
	
	public static Document getSignedDocument(String xmlString,KeyStore.PrivateKeyEntry pke) throws Exception {
		return getSignedDocument(convertXMLStringToDocument(xmlString),pke);	
	}
	
	

	public static Document getSignedDocument(Document doc,KeyStore.PrivateKeyEntry pke) throws Exception {

		//get Private Key, Certificate and Public Key
		PrivateKey privateKey = pke.getPrivateKey();
		X509Certificate keyX509Certificate = (X509Certificate) pke.getCertificate();
		PublicKey publicKey = keyX509Certificate.getPublicKey();
		KeyPair kp = new KeyPair(publicKey, privateKey);
		org.w3c.dom.Element xmlRequest = doc.getDocumentElement();
		String id = xmlRequest.getAttribute("Id");	
		if (id!="") id="#"+id;
		// Create a DOM XMLSignatureFactory that will be used to generate the
		// enveloped signature
		XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
		// Create a Reference to the enveloped document (in this case we are
		// signing the part of document marked by Id field
		List <Transform> list = new ArrayList<Transform>();
		list.add(fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null));		
		list.add(fac.newTransform(CanonicalizationMethod.EXCLUSIVE, (TransformParameterSpec) null));
		Reference ref = fac.newReference(id, fac.newDigestMethod(DigestMethod.SHA1, null),list,null, null);
		//Setup Canonicalization and Signature Method
		CanonicalizationMethod cm = fac.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE,(C14NMethodParameterSpec) null);
		SignatureMethod sm = fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null); 
		// Create the SignedInfo
		SignedInfo si = fac.newSignedInfo(cm,sm,Collections.singletonList(ref));
		//Keystore info
		KeyInfoFactory keyInfoFactory = fac.getKeyInfoFactory();		
		// Create a KeyValue containing the RSA PublicKey that was generated
		KeyValue kv = keyInfoFactory.newKeyValue(kp.getPublic());
		String issuerName = keyX509Certificate.getIssuerX500Principal().getName();
		String subjectName = keyX509Certificate.getSubjectDN().getName();
		//Collect some more data about key		
		BigInteger serialNumber = keyX509Certificate.getSerialNumber();
		List x509 = new ArrayList();        
		x509.add(keyX509Certificate);
		x509.add(keyInfoFactory.newX509IssuerSerial(issuerName, serialNumber));
		List items = new ArrayList();
		items.add(keyInfoFactory.newX509Data(x509));  
		//Crate keyinfo
		KeyInfo ki = keyInfoFactory.newKeyInfo(items);   
		// Create a DOMSignContext and specify the DSA PrivateKey and
		// location of the resulting XMLSignature's parent element
		DOMSignContext dsc = new DOMSignContext(kp.getPrivate(), doc.getDocumentElement());
		// Create the XMLSignature (but don't sign it yet)
		XMLSignature signature = fac.newXMLSignature(si, ki);
		// Marshal, generate (and sign) the enveloped signature
		signature.sign(dsc);
		return doc;		
	}
}
