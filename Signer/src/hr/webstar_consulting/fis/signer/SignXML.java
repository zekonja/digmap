package hr.webstar_consulting.fis.signer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.*;
import javax.xml.crypto.dsig.spec.*;

import java.io.File;
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

public class SignXML {

	/**
	 * This is a simple example of generating an Enveloped XML
	 * Signature using the JSR 105 API. The resulting signature will look
	 * like (key and signature values will be different):
	 *
	 * <pre><code>
	 *<Envelope xmlns="urn:envelope">
	 * <Signature xmlns="http://www.w3.org/2000/09/xmldsig#">
	 *   <SignedInfo>
	 *     <CanonicalizationMethod Algorithm="http://www.w3.org/TR/2001/REC-xml-c14n
-20010315"/>
	 *     <SignatureMethod Algorithm="http://www.w3.org/2000/09/xmldsig#dsa-sha1"/>
	 *     <Reference URI="">
	 *       <Transforms>
	 *         <Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature"/>
	 *       </Transforms>
	 *       <DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/>
	 *       <DigestValue>K8M/lPbKnuMDsO0Uzuj75lQtzQI=<DigestValue>
	 *     </Reference>
	 *   </SignedInfo>
	 *   <SignatureValue>
	 *     DpEylhQoiUKBoKWmYfajXO7LZxiDYgVtUtCNyTgwZgoChzorA2nhkQ==
	 *   </SignatureValue>
	 *   <KeyInfo>
	 *     <KeyValue>
	 *       <DSAKeyValue>
	 *         <P>
	 *           rFto8uPQM6y34FLPmDh40BLJ1rVrC8VeRquuhPZ6jYNFkQuwxnu/wCvIAMhukPBL
	 *           FET8bJf/b2ef+oqxZajEb+88zlZoyG8g/wMfDBHTxz+CnowLahnCCTYBp5kt7G8q
	 *           UobJuvjylwj1st7V9Lsu03iXMXtbiriUjFa5gURasN8=
	 *         </P>
	 *         <Q>
	 *           kEjAFpCe4lcUOdwphpzf+tBaUds=
	 *         </Q>
	 *         <G>
	 *           oe14R2OtyKx+s+60O5BRNMOYpIg2TU/f15N3bsDErKOWtKXeNK9FS7dWStreDxo2
	 *           SSgOonqAd4FuJ/4uva7GgNL4ULIqY7E+mW5iwJ7n/WTELh98mEocsLXkNh24HcH4
	 *           BZfSCTruuzmCyjdV1KSqX/Eux04HfCWYmdxN3SQ/qqw=
	 *         </G>
	 *         <Y>
	 *           pA5NnZvcd574WRXuOA7ZfC/7Lqt4cB0MRLWtHubtJoVOao9ib5ry4rTk0r6ddnOv
	 *           AIGKktutzK3ymvKleS3DOrwZQgJ+/BDWDW8kO9R66o6rdjiSobBi/0c2V1+dkqOg
	 *           jFmKz395mvCOZGhC7fqAVhHat2EjGPMfgSZyABa7+1k=
	 *         </Y>
	 *       </DSAKeyValue>
	 *     </KeyValue>
	 *   </KeyInfo>
	 * </Signature>
	 *</Envelope>
	 * </code></pre>
	 */


	//
	// Synopsis: java GenEnveloped [document] [output]
	//
	//    where "document" is the name of a file containing the XML document
	//    to be signed, and "output" is the name of the file to store the
	//    signed document. The 2nd argument is optional - if not specified,
	//    standard output will be used.

	//
	private static final Logger log = Logger.getLogger(SignXML.class.getName());
	
	
	/**
	 * Command line interface for signing xml file with given .pfx key
	 * @param args[0] source xml file name
	 * @param args[1] destination for signed xml file name
	 * @param args[2] keystore file name
	 * @param args[3] keystore and key password
	 * @throws Exception
	 * 
	 * "./misc/UnsignedBill.xml"   "./misc/SignedBill.xml"  "./key_folder/fiskaltest1.pfx" "fiskaltest"
	 * "./misc/UnsignedSpace.xml"  "./misc/SignedSpace.xml" "./key_folder/fiskaltest1.pfx" "fiskaltest"
	 */
	
	public static void main(String[] args) throws Exception {
		KeyStore.PrivateKeyEntry pke = KeyManager.getPrivateKeyEntry(args[2],args[3]);
		//read file
		File inputXmlFile = new File(args[0]);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(inputXmlFile);	
		doc.getDocumentElement().normalize();
		//sign content
		Document signedDoc = getSignedXML(doc, pke);		
		//write file
	    TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(signedDoc);
        File outputXmlFile = new File(args[1]);
        StreamResult result =  new StreamResult(outputXmlFile);
        transformer.transform(source, result);		
		
	}
	
	public static Object getSignedObject(Object object, KeyStore.PrivateKeyEntry pke) throws Exception
	{
		//Convert object to document
		Document sourceDoc = convertObjectToDocument(object);
		//sign document
		Document signedDocument = SignXML.getSignedXML(sourceDoc,pke);
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
