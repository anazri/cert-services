package org.fbsks.certservices.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
public class PKI extends AbstractPersistable<Long> {

	private static final long serialVersionUID = -140537791349423216L;

	@OneToMany
	private List<CertificateAuthority> cas; 
	
	private String name;
	
	protected PKI() {}
	
	public PKI(String name) {
		this.name = name;
	}

	public List<CertificateAuthority> getCas() {
		return cas;
	}

	public void setCas(List<CertificateAuthority> cas) {
		this.cas = cas;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
