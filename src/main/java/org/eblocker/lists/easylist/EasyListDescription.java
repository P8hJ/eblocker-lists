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

import java.util.Collections;
import java.util.Set;

public class EasyListDescription {

	private String url;
	private String filename;
	private long minimumBytesJSON;
	private int maxAgeDays;
	private int maxFilterSize;
	private Set<EasyListRuleTest> easyListRuleTests;

	public EasyListDescription(String url, String filename, long minimumBytesJSON, int maxAgeDays, int maxFilterSize, Set<EasyListRuleTest> easyListRuleTests) {
		this.url = url;
		this.filename = filename;
		this.minimumBytesJSON = minimumBytesJSON;
		this.maxAgeDays = maxAgeDays;
		this.maxFilterSize = maxFilterSize;
		this.easyListRuleTests = easyListRuleTests == null ? Collections.emptySet() : easyListRuleTests;
	}
	
	public String getURL() {
		return url;
	}

	public String getFilename() {
		return filename;
	}
	
	/**
	 * Returns the minimum number of bytes that this list should produce
	 * when serialized to JSON. This should ensure that the list still
	 * can be parsed and is not empty.
	 */
	public long getMinimumBytesJSON() {
		return minimumBytesJSON;
	}

	public int getMaxAgeDays() {
		return maxAgeDays;
	}

	public int getMaxFilterSize() {
	    return maxFilterSize;
	}

	public Set<EasyListRuleTest> getEasyListRuleTests() {
		return easyListRuleTests;
	}
}
