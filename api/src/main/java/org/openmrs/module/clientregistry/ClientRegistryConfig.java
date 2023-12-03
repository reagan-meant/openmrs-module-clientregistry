/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.clientregistry;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.AdministrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Contains module's config.
 */
@Component
public class ClientRegistryConfig {

	public final static String MODULE_PRIVILEGE = "Client Registry Privilege";

	@Autowired
	@Qualifier("adminService")
	AdministrationService administrationService;

	@Value("${clientregistry.serverUrl}")
	private String serverUrl;

	@Value("${clientregistry.username}")
	private String username;

	@Value("${clientregistry.password}")
	private String password;

	@Value("${clientregistry.identifierRoot}")
	private String identifierRoot;

	public boolean clientRegistryConnectionEnabled() {
		return StringUtils.isNotBlank(getClientRegistryServerUrl());
	}

	public String getClientRegistryServerUrl() {
		return serverUrl;
	}

	public String getClientRegistryUserName() {
		return username;
	}

	public String getClientRegistryPassword() {
		return password;
	}

	public String getClientRegistryIdentifierRoot() {
		return identifierRoot;
	}
}
