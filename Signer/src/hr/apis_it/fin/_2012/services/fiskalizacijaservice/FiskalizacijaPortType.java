
package hr.apis_it.fin._2012.services.fiskalizacijaservice;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;
import hr.apis_it.fin._2012.types.f73.PoslovniProstorOdgovor;
import hr.apis_it.fin._2012.types.f73.PoslovniProstorZahtjev;
import hr.apis_it.fin._2012.types.f73.RacunOdgovor;
import hr.apis_it.fin._2012.types.f73.RacunZahtjev;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.4-b01
 * Generated source version: 2.2
 * 
 */
@WebService(name = "FiskalizacijaPortType", targetNamespace = "http://www.apis-it.hr/fin/2012/services/FiskalizacijaService")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
@XmlSeeAlso({
    hr.apis_it.fin._2012.types.f73.ObjectFactory.class,
    org.w3._2000._09.xmldsig.ObjectFactory.class
})
public interface FiskalizacijaPortType {


    /**
     * 
     * @param request
     * @return
     *     returns hr.apis_it.fin._2012.types.f73.RacunOdgovor
     */
    @WebMethod(action = "http://e-porezna.porezna-uprava.hr/fiskalizacija/2012/services/FiskalizacijaService/racuni")
    @WebResult(name = "RacunOdgovor", targetNamespace = "http://www.apis-it.hr/fin/2012/types/f73", partName = "response")
    public RacunOdgovor racuni(
        @WebParam(name = "RacunZahtjev", targetNamespace = "http://www.apis-it.hr/fin/2012/types/f73", partName = "request")
        RacunZahtjev request);

    /**
     * 
     * @param request
     * @return
     *     returns hr.apis_it.fin._2012.types.f73.PoslovniProstorOdgovor
     */
    @WebMethod(action = "http://e-porezna.porezna-uprava.hr/fiskalizacija/2012/services/FiskalizacijaService/poslovniProstor")
    @WebResult(name = "PoslovniProstorOdgovor", targetNamespace = "http://www.apis-it.hr/fin/2012/types/f73", partName = "response")
    public PoslovniProstorOdgovor poslovniProstor(
        @WebParam(name = "PoslovniProstorZahtjev", targetNamespace = "http://www.apis-it.hr/fin/2012/types/f73", partName = "request")
        PoslovniProstorZahtjev request);

    /**
     * 
     * @param request
     * @return
     *     returns java.lang.String
     */
    @WebMethod(action = "http://e-porezna.porezna-uprava.hr/fiskalizacija/2012/services/FiskalizacijaService/echo")
    @WebResult(name = "EchoResponse", targetNamespace = "http://www.apis-it.hr/fin/2012/types/f73", partName = "response")
    public String echo(
        @WebParam(name = "EchoRequest", targetNamespace = "http://www.apis-it.hr/fin/2012/types/f73", partName = "request")
        String request);

}
