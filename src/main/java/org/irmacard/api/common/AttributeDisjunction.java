/*
 * AttributeDisjunction.java
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

package org.irmacard.api.common;

import org.irmacard.api.common.exceptions.ApiException;
import org.irmacard.api.common.util.GsonUtil;
import org.irmacard.credentials.info.AttributeIdentifier;
import org.irmacard.credentials.info.CredentialDescription;
import org.irmacard.credentials.info.DescriptionStore;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("unused")
public class AttributeDisjunction extends ArrayList<AttributeIdentifier> {
	private static final long serialVersionUID = -2053856356082434224L;

	private String label;
	private HashMap<AttributeIdentifier, String> values = new HashMap<>();
	private boolean optional;

	private transient AttributeIdentifier selected;
	private transient boolean satisfied = false;

	public AttributeDisjunction(String label, boolean optional) {
		this.label = label;
		this.optional = optional;
	}

	public AttributeDisjunction(String label, AttributeIdentifier value) {
		this.label = label;
		add(value);
	}

	public AttributeDisjunction(String label, String value) {
		AttributeIdentifier ai = new AttributeIdentifier(value);
		add(ai);
		this.label = label;
	}

	public boolean hasValues() {
		return values.size() > 0;
	}

	public HashMap<AttributeIdentifier, String> getValues() {
		return values;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isOptional() {
		return optional;
	}

	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	public AttributeIdentifier getSelected() {
		return selected;
	}

	public void setSelected(AttributeIdentifier selected) {
		this.selected = selected;
	}

	public boolean isSatisfied() {
		return satisfied;
	}

	public void setSatisfied(boolean satisfied) {
		this.satisfied = satisfied;
	}

	public boolean contains(String s) {
		AttributeIdentifier i = new AttributeIdentifier(s);
		return this.contains(i);
	}

	public int indexOf(String s) {
		AttributeIdentifier i = new AttributeIdentifier(s);
		return indexOf(i);
	}

	public boolean attributesMatchStore() throws ApiException {
		for (AttributeIdentifier attribute : this) {
			CredentialDescription cd = DescriptionStore.getInstance()
					.getCredentialDescription(attribute.getCredentialIdentifier());
			if (cd == null)
				return false;

			String attrName = attribute.getAttributeName();
			if (attrName != null && !cd.getAttributeNames().contains(attrName))
				return false;
		}

		return true;
	}

	/**
	 * Used in unit test
	 */
	@Override
	public boolean equals(Object other) {
		if (other == null || this.getClass() != other.getClass()) {
			return false;
		}
		AttributeDisjunction aother = (AttributeDisjunction) other;
		if (!getLabel().equals(aother.getLabel()) || size() != aother.size()) {
			return false;
		}

		for (int i = 0; i < size(); i++) {
			if (! get(i).equals(aother.get(i))) {
				return false;
			}
		}
		return true;
	}
}
