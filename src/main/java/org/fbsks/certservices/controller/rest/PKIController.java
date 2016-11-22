package org.fbsks.certservices.controller.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import java.util.List;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.util.encoders.Base64;
import org.fbsks.certservices.controller.rest.jsonview.PKISummary;
import org.fbsks.certservices.model.IdentityContainer;
import org.fbsks.certservices.model.PKI;
import org.fbsks.certservices.services.CRLService;
import org.fbsks.certservices.services.PKCS12ConversorService;
import org.fbsks.certservices.services.PKIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

/**
 * 
 * @author fabio.resner
 *
 */
@RestController
@RequestMapping("/rest/pki")
public class PKIController {

	@Autowired
	private PKIService pkiService;
	
	@Autowired
	private PKCS12ConversorService p12Conversor;
	
	@Autowired
	private CRLService crlService;
	
	private static final String DEFAULT_PASSWORD = "123456";
	
	private static final String P12_EXTENSTION = ".p12";
	private static final String CRL_EXTENSTION = ".crl";
	private static final String CER_EXTENSTION = ".cer";
	
	private static final String CONTENT_DISPOSITION_HEADER = "content-disposition";
	private static final String CONTENT_DISPOSITION_ARGS = "attachment; filename=";
	
	@RequestMapping(path = "/new", method = RequestMethod.POST)
	public void generateNewPKI(@RequestParam String pkiName) {
		pkiService.generatePKI(pkiName);
	}
	
	@JsonView(PKISummary.class)
	@RequestMapping(path = "/list", method = RequestMethod.GET)
	public List<PKI> listPKIs() {
		return pkiService.listPKIs();
	}
	
	//TODO Review exception thrown at this point
	//TODO Response Entity returning could be revised (better standard ways to implement?)
	@RequestMapping(path="/cert/new", method = RequestMethod.POST)
	public ResponseEntity<byte[]> newPKICertificate(@RequestParam String pkiName, @RequestParam String subjectName) throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
		IdentityContainer userIdentity = this.pkiService.generateIdentity(pkiName, subjectName);
		KeyStore userPKCS12 = p12Conversor.generatePKCS12(userIdentity);
		
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		userPKCS12.store(output, DEFAULT_PASSWORD.toCharArray());
		
		return ResponseEntity.ok()
				.header(CONTENT_DISPOSITION_HEADER, CONTENT_DISPOSITION_ARGS + getCertificateName(userIdentity.getCertificate()) + P12_EXTENSTION)
				.body(Base64.encode(output.toByteArray()));
	}
	
	//TODO Review exception thrown at this point
	//TODO Response Entity returning could be revised (better standard ways to implement?)
	//TODO CRLs are being generated each time the service is invoked. (fetch from database?)
	@RequestMapping(path="/{issuerName}/crl", method=RequestMethod.GET)
	public ResponseEntity<byte[]> getCRLs(@PathVariable String issuerName) throws CRLException {
		X509CRL crl = this.crlService.generateCRL(issuerName);
		
		return ResponseEntity.ok()
				.header(CONTENT_DISPOSITION_HEADER, CONTENT_DISPOSITION_ARGS + crl.getIssuerX500Principal() + CRL_EXTENSTION)
				.body(Base64.encode(crl.getEncoded()));
	}
	
	//TODO Review exception thrown at this point
	//TODO Response Entity returning could be revised (better standard ways to implement?)
	@RequestMapping(path="/{issuerName}/cert", method=RequestMethod.GET)
	public ResponseEntity<byte[]> getAIA(@PathVariable String issuerName) throws CRLException, IOException {
		X509CertificateHolder caCertificate = this.pkiService.getCertificateChain(issuerName);
		
		return ResponseEntity.ok()
				.header(CONTENT_DISPOSITION_HEADER, CONTENT_DISPOSITION_ARGS + getCertificateName(caCertificate) + CER_EXTENSTION)
				.body(Base64.encode(caCertificate.getEncoded()));
	}
	
	private String getCertificateName(X509CertificateHolder cert) {
		String caCN = cert.getSubject().toString();
		caCN = caCN.substring(0, caCN.indexOf(","));
		caCN = caCN.replace("CN=", "");
		
		return caCN;
	}
}
