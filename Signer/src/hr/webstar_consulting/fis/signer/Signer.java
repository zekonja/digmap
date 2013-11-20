package hr.webstar_consulting.fis.signer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.*;
import javax.xml.crypto.dsig.spec.*;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
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
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
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
	 * @param args[0] source xml file name
	 * @param args[1] destination for signed xml file name
	 * @param args[2] keystore file name
	 * @param args[3] keystore and key password
	 * @throws Exception
	 * 
	 * "./misc/UnsignedBill.xml"   "./misc/SignedBill.xml"  "./key_folder/fiskaltest0.pfx" "fiskaltest"
	 * "./misc/UnsignedSpace.xml"  "./misc/SignedSpace.xml" "./key_folder/fiskaltest0.pfx" "fiskaltest"
	 */
	
	public static void main(String[] args) throws Exception {
		KeyStore.PrivateKeyEntry pke = KeyManager.getPrivateKeyEntry(args[2],args[3]);
		//read file
		Document doc = readDocumentFromXMLFile(args[0]);
		//sign content
		Document signedDoc = getSignedXML(doc, pke);		
		//write file
		writeDocumentToXMLFile(signedDoc,args[1]);
		
	}
	
	public static Document readDocumentFromXMLFile (String inputFileName) throws Exception
	{
		File inputXmlFile = new File(inputFileName);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setNamespaceAware(true);
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
		Document signedDocument = Signer.getSignedXML(sourceDoc,pke);
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
		JAXBContext jc = null;
		jc = JAXBContext.newInstance(className);
		//Unmarshall signed document into object
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		return unmarshaller.unmarshal(document);	
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
	
	
	public static String getSignedXML(String xmlString,KeyStore.PrivateKeyEntry pke) throws Exception {
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(xmlString));
		Document doc = db.parse(is);
		Document signedDoc = getSignedXML(doc,pke);		
		TransformerFactory transformerFactory = TransformerFactory.newInstance(); 
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(signedDoc);
		StreamResult result =  new StreamResult(new StringWriter());
		transformer.transform(source, result);
		String signedXml = result.getWriter().toString();
		return signedXml;
	}

	public static Document getSignedXML(Document doc,KeyStore.PrivateKeyEntry pke) throws Exception {

		//get Private Key, Certificate and Public Key
		PrivateKey privateKey = pke.getPrivateKey();
		X509Certificate keyX509Certificate = (X509Certificate) pke.getCertificate();
		PublicKey publicKey = keyX509Certificate.getPublicKey();
		KeyPair kp = new KeyPair(publicKey, privateKey);



		org.w3c.dom.Element xmlRacunZahtjev = doc.getDocumentElement();
		String id = xmlRacunZahtjev.getAttribute("Id");	
		if (id!="") id="#"+id;
		// Create a DOM XMLSignatureFactory that will be used to generate the
		// enveloped signature
		XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
		// Create a Reference to the enveloped document (in this case we are
		// signing the part of document marked by Id field
		List <Transform> list = new ArrayList<Transform>();
		list.add(fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null));
		list.add(fac.newTransform("http://www.w3.org/2001/10/xml-exc-c14n#", (TransformParameterSpec) null));
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
		//Collect some more data about key
		String issuerName = keyX509Certificate.getIssuerX500Principal().getName();
		String subjectName = keyX509Certificate.getSubjectDN().getName();
		String oib = subjectName.substring(subjectName.indexOf(" HR")+3,subjectName.indexOf(" HR")+3+11);		
		String name = subjectName.substring(subjectName.indexOf("O=")+3,subjectName.indexOf(" HR"));
//		System.out.println("SubjectDN "+subjectName);
//		System.out.println("SubjectOIB "+oib);
//		System.out.println("SubjectName "+name);
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
