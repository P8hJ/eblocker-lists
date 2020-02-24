/*
 * Copyright 2020 eBlocker Open Source UG (haftungsbeschraenkt)
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be
 * approved by the European Commission - subsequent versions of the EUPL
 * (the "License"); You may not use this work except in compliance with
 * the License. You may obtain a copy of the License at:
 *
 *   https://joinup.ec.europa.eu/page/eupl-text-11-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.eblocker.lists.easylist;

import java.io.InputStream;

import org.eblocker.lists.tools.ResourceInputStream;
import org.junit.rules.ExternalResource;

/**
 * An external JUnit resource that is a file in the classpath.
 */
public class FileResource extends ExternalResource {
	private String name;

	public FileResource(String name) {
		this.name = name;
	}

	public InputStream getInputStream() {
		return ResourceInputStream.get(name);
	}

}
