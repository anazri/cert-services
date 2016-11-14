package org.fbsks.certservices.services;

import java.security.KeyPair;

import org.bouncycastle.cert.X509CertificateHolder;
import org.fbsks.certservices.Repository.CertificateAuthorityRepository;
import org.fbsks.certservices.Repository.PKIRepository;
import org.fbsks.certservices.model.CertificateAuthority;
import org.fbsks.certservices.model.CertificateKeyPairGenerator;
import org.fbsks.certservices.model.PKI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author fabio.resner
 *
 */
@Service
public class PKIGenerator {

	@Autowired
	private CertificateGenerator certificateGenerator;
	
	@Autowired
	private CertificateKeyPairGenerator keyPairGenerator;
	
	@Autowired
	private PKIRepository pkiRepository;
	
	@Autowired
	private CertificateAuthorityRepository caRepository;
	
	private static final String ROOT_CA = "ROOTCA";
	
	public PKI generatePKI(String pkiName) {
		KeyPair keyPair = keyPairGenerator.generateKeyPair();
		
		X509CertificateHolder rootCertificate = this.certificateGenerator.generateSelfSignedCertificate(pkiName + ROOT_CA, keyPair);
		
		CertificateAuthority rootCa = new CertificateAuthority(pkiName + ROOT_CA, rootCertificate, keyPair.getPrivate());
		caRepository.save(rootCa);
		
		PKI pki = new PKI(pkiName, rootCa);
		rootCa.setPki(pki);
		
		pkiRepository.save(pki);
	
		return pki;
	}
}
