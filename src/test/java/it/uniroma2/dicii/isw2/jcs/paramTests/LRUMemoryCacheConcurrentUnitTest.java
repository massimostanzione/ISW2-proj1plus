/*
 * Università degli Studi di Roma Tor Vergata
 * Faculty of Engineering
 * MSc in Computer Engineering
 * 
 * "Ingegneria del Software II" 2020/2021
 * Software Testing module
 * Project "1+"
 * 
 * Porting of the JCS "LRUMemoryCacheConcurrentUnitTest" test from JUnit3 to JUnit 4,
 * implementing it with Parameterized class.
 * 
 * Massimo Stanzione
 * matr. 0304936
 */

package it.uniroma2.dicii.isw2.jcs.paramTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.Arrays;
import java.util.Collection;

import org.apache.jcs.JCS;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.control.CompositeCacheManager;
import org.apache.jcs.engine.memory.lru.LRUMemoryCache;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test which exercises the LRUMemory cache. This one uses three different
 * regions for three threads.
 *
 * @version $Id: LRUMemoryCacheConcurrentUnitTest.java 536904 2007-05-10
 *          16:03:42Z tv $
 */
@RunWith(Parameterized.class)
public class LRUMemoryCacheConcurrentUnitTest {
	/**
	 * Test parameter.
	 */
	private String region;

	/**
	 * Number of items to cache, twice the configured maxObjects for the memory
	 * cache regions.
	 */
	private static int items = 200;

	/**
	 * Constructor for the TestDiskCache object.
	 *
	 * @param region test parameter
	 */
	public LRUMemoryCacheConcurrentUnitTest(String region) {
		configure(region);
	}

	/**
	 * Link class parameter with test parameter.
	 *
	 * @param region test parameter
	 */
	private void configure(String region) {
		// Test parameters initialization
		this.region = region;
	}

	/**
	 * Test environment configuration.
	 * 
	 * Corresponding to JUnit3 "setUp" method.
	 * 
	 * @implNote must be public and static.
	 */
	@BeforeClass
	public static void setup() {
		JCS.setConfigFilename("/TestDiskCache.ccf");
	}

	/**
	 * Parameters association. The parameter involved has been declared earlier as
	 * attributes in this class.
	 * 
	 * @return array containing actual values of the test parameters
	 */
	@Parameters
	public static Collection<String[]> getTestParameters() {
		return Arrays.asList(new String[][] { { "indexedRegion1" } });
	}

	/**
	 * Test logic, not modified.
	 * 
	 * Adds items to cache, gets them, and removes them. The item count is more than
	 * the size of the memory cache, so items should be dumped.
	 *
	 * @param region Name of the region to access
	 *
	 * @exception Exception If an error occurs
	 */
	@Test
	public void runTestForRegionTest() throws Exception {
		CompositeCacheManager cacheMgr = CompositeCacheManager.getUnconfiguredInstance();
		cacheMgr.configure("/TestDiskCache.ccf");
		CompositeCache cache = cacheMgr.getCache(region);
		LRUMemoryCache lru = new LRUMemoryCache();
		lru.initialize(cache);

		// Add items to cache

		for (int i = 0; i < items; i++) {
			ICacheElement ice = new CacheElement(cache.getCacheName(), i + ":key", region + " data " + i);
			ice.setElementAttributes(cache.getElementAttributes());
			lru.update(ice);
		}

		// Test that initial items have been purged

		for (int i = 0; i < 102; i++) {
			assertNull(lru.get(i + ":key"));
		}

		// Test that last items are in cache

		for (int i = 102; i < items; i++) {
			String value = (String) lru.get(i + ":key").getVal();
			assertEquals(region + " data " + i, value);
		}

		// Remove all the items

		for (int i = 0; i < items; i++) {
			lru.remove(i + ":key");
		}

		// Verify removal

		for (int i = 0; i < items; i++) {
			assertNull("Removed key should be null: " + i + ":key", lru.get(i + ":key"));
		}
	}
}
