/*
 * Copyright (c) 2015, the IRMA Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *  Neither the name of the IRMA project nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.irmacard.credentials.info;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;

import org.w3c.dom.Document;

public class IssuerDescription extends ConfigurationParser implements Serializable {
	private static final long serialVersionUID = 1640325096236188409L;
	private TranslatedString name;
	private TranslatedString shortName;
	private String id;
	private String contactAddress;
	private String contactEMail;
	private IssuerIdentifier identifier;

	/**
	 * Full human readable name of the issuer. For example this could be "Radboud University".
	 * @return
	 */
	public TranslatedString getName() {
		return name;
	}
	/**
	 * Short human readable name of the issuer. For example this could be "RU".
	 * @return
	 */
	public TranslatedString getShortName() {
		return shortName;
	}

	/**
	 * Get issuer ID, corresponding to the directory name in the configuration structures.
	 * @return
	 */
	public String getID() {
		return id;
	}

	public IssuerIdentifier getIdentifier() {
		return identifier;
	}

	/**
	 * @return Contact address of the issuer
	 */
	public String getContactAddress() {
		return contactAddress;
	}

	/**
	 * @return Contact email of the issuer
	 */
	public String getContactEMail() {
		return contactEMail;
	}

	public IssuerDescription(URI file) throws InfoException {
		super();
		Document d = parse(file);
		init(d);
	}

	public IssuerDescription(InputStream stream) throws InfoException {
		super();
		Document d = parse(stream);
		init(d);
	}

	public IssuerDescription(String xml) throws InfoException {
		this(new ByteArrayInputStream(xml.getBytes()));
	}

	private void init(Document d) throws InfoException {
		if (getSchemaVersion() < 4)
			throw new InfoException("Cannot parse issuer definition of version " + getSchemaVersion());

		id = getFirstTagText(d, "ID");
		name = getFirstTranslatedTag(d, "Name");
		shortName = getFirstTranslatedTag(d, "ShortName");

		contactAddress = getFirstTagText(d, "ContactAddress");
		contactEMail = getFirstTagText(d, "ContactEMail");
		String schemeManager = getFirstTagText(d, "SchemeManager");
		identifier = new IssuerIdentifier(schemeManager, id);
	}

	public String toString() {
		return name + " (" + contactAddress + ", " + contactEMail + ")";
	}
}
