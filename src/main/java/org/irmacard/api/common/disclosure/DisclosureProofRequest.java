/*
 * DisclosureProofRequest.java
 *
 * Copyright (c) 2015, Sietse Ringers, Radboud University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the IRMA project nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.irmacard.api.common.disclosure;


import org.irmacard.api.common.AttributeDisjunction;
import org.irmacard.api.common.AttributeDisjunctionList;
import org.irmacard.api.common.SessionRequest;
import org.irmacard.api.common.SessionType;
import org.irmacard.api.common.exceptions.ApiException;
import org.irmacard.credentials.idemix.IdemixPublicKey;
import org.irmacard.credentials.idemix.IdemixSystemParameters;
import org.irmacard.credentials.idemix.info.IdemixKeyStore;
import org.irmacard.credentials.idemix.proofs.ProofList;
import org.irmacard.credentials.info.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.*;


@SuppressWarnings("unused")
public class DisclosureProofRequest extends SessionRequest {
	private static final long serialVersionUID = 1016467840623150897L;
	private static Logger logger = LoggerFactory.getLogger(DisclosureProofRequest.class);

	protected AttributeDisjunctionList content;

	public DisclosureProofRequest() {
		type = SessionType.DISCLOSING;
	}

	public DisclosureProofRequest(BigInteger nonce, BigInteger context, AttributeDisjunctionList content) {
		super(nonce, context);
		type = SessionType.DISCLOSING;
		this.content = content;
	}

	public AttributeDisjunctionList getContent() {
		return content;
	}

	@Override
	public HashSet<CredentialIdentifier> getCredentialList() {
		HashSet<CredentialIdentifier> credentials = new HashSet<>();

		for (AttributeDisjunction disjunction : content)
			for (AttributeIdentifier attr : disjunction)
				credentials.add(attr.getCredentialIdentifier());

		return credentials;
	}

	@Override
	public HashMap<IssuerIdentifier, Integer> getPublicKeyList() {
		return new HashMap<>();
	}

	public boolean attributesMatchStore() throws ApiException {
		for (AttributeDisjunction disjunction : getContent())
			if (!disjunction.attributesMatchStore())
				return false;

		return true;
	}

	@Override
	public IdemixSystemParameters getLargestParameters() {
		IdemixSystemParameters params = null;

		for (AttributeDisjunction disjunction : getContent()) {
			for (AttributeIdentifier identifier : disjunction) {

				try {
					IdemixPublicKey pk = IdemixKeyStore.getInstance()
							.getLatestPublicKey(identifier.getIssuerIdentifier());
					if (params == null || params.get_l_n() < pk.getBitsize())
						params = pk.getSystemParameters();
				} catch (KeyException e) {
					throw new RuntimeException(e);
				}
			}
		}

		return params;
	}

	@Override
	public boolean isEmpty() {
		return content == null || content.size() == 0;
	}

	public DisclosureProofResult verify(ProofList proofs) throws InfoException, KeyException {
		return verify(proofs, Calendar.getInstance().getTime(), false);
	}

	protected DisclosureProofResult verify(ProofList proofs, Date validityDate, boolean allowExpired)
	throws InfoException, KeyException {
		DisclosureProofResult result = new DisclosureProofResult(); // Our return object
		HashMap<AttributeIdentifier, String> attributes = new HashMap<>();
		result.setAttributes(attributes);

		if (!proofs.verify(getContext(), getNonce(), true)) {
			logger.info("Proofs did not verify");
			result.setStatus(DisclosureProofResult.Status.INVALID);
			return result;
		}
		if (validityDate != null && !proofs.isValidOn(validityDate)) {
			result.setStatus(DisclosureProofResult.Status.EXPIRED);
			if (!allowExpired)
				return result;
		}

		HashMap<AttributeIdentifier, String> foundAttrs;
		try {
			foundAttrs =  proofs.getAttributes();
		} catch (IllegalArgumentException e) {
			logger.info("Metadata attribute missing, or unknown credential type");
			result.setStatus(DisclosureProofResult.Status.INVALID);
			return result;
		}
		
		// For each of the disjunctions, lookup attributes satisfying them
		for (AttributeDisjunction disjunction : content) {
			for (AttributeIdentifier ai : disjunction) {
				// Is this attribute given?
				if (foundAttrs.containsKey(ai)) {
					String value = foundAttrs.get(ai);
					if (!disjunction.hasValues()) {	
						disjunction.setSatisfied(true);
						attributes.put(ai, value);
						break; // Done with disjunction
					} else {
						// If the request indicated that the attribute should have a specific value, then the containing
						// disjunction is only satisfied if the actual value of the attribute is correct.
						String requiredValue = disjunction.getValues().get(ai);
						if (requiredValue.equals(value)) {
							disjunction.setSatisfied(true);
							attributes.put(ai, value);
							break; // Done with disjunction
						}
					}
				}
			}

			// If it is optional, this disjunction is always satisfied
			if (disjunction.isOptional())
				disjunction.setSatisfied(true);
		}

		for (AttributeDisjunction disjunction : content)
			if (!disjunction.isSatisfied())
				result.setStatus(DisclosureProofResult.Status.MISSING_ATTRIBUTES);

		return result;
	}

	/**
	 * Returns true if the request contains no disjunctions, and asks for attributes of a single credential type
	 */
	public boolean isSimple() {
		Set<String> credentials = new HashSet<>();

		for (AttributeDisjunction d : content) {
			if (d.size() > 1)
				return false;
			credentials.add(d.get(0).getCredentialName());
		}

		return credentials.size() == 1;
	}
}
