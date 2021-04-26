package it.uniroma2.dicii.isw2.jcs.paramTests;

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

import org.junit.*;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.control.CompositeCacheManager;
import org.apache.jcs.engine.memory.lru.LRUMemoryCache;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test which exercises the LRUMemory cache. This one uses three different
 * regions for three threads.
 *
 * * RIMOSSI:
 * 
 * - costruttore vecchio
 * 
 * - main (non serve più)
 * 
 * - suite (perché la chiamata ai test è automatica)
 * 
 * @version $Id: LRUMemoryCacheConcurrentUnitTest.java 536904 2007-05-10
 *          16:03:42Z tv $
 */

@RunWith(Parameterized.class) // [1]
public class LRUMemoryCacheConcurrentUnitTest {
	/**
	 * Number of items to cache, twice the configured maxObjects for the memory
	 * cache regions.
	 */
	private static int items = 200;
	CompositeCache cache;

	// ---------- PARAMETRI DEL TEST
	// sono attributi privati della classe di test [2]
	// oggetto testato
	private LRUMemoryCache lru;
	// input al test (passato al costruttore)
	private String region;
	// output atteso
	// (none)
	
	/**
	 * Test setup
	 * 
	 * da eseguirsi prima di ogni test
	 */
	@Before
	public void setUp() {
		// JCS.setConfigFilename( "/TestDiskCache.ccf" );
	}
	/**
	 * ---------- CONFIGURAZIONE oggetto da testare, che è lru, vedasi dichiarazione
	 * negli attributi della classe
	 * 
	 * lo posso invocare da costruttore, @Before, @BeforeClass...
	 * 
	 * @param region
	 * 
	 * @see lru
	 */
	private void configure(String region) {
		// inizializzazione attributo
		this.region = region;

		// istanziazione dell'oggetto da testare
		CompositeCacheManager cacheMgr = CompositeCacheManager.getUnconfiguredInstance();
		cacheMgr.configure("/TestDiskCache.ccf");
		cache = cacheMgr.getCache(region);
		lru = new LRUMemoryCache();
	}

	/**
	 * ---------- SETTAGGIO VALORI DEI PARAMETRI PER IL TEST
	 * 
	 * @return array con i parametri, tipo Collection [3]
	 */
	@Parameters
	public static Collection<String[]> getTestParameters() {

		// ...?

		return Arrays.asList(new String[][] { { "indexedRegion1" } });

	}

	/**
	 * ---------- COSTRUTTORE
	 * 
	 * ha tanti argomenti quanti i parametri del test [4]
	 * 
	 * @param region
	 */
	public LRUMemoryCacheConcurrentUnitTest(String region) {
		this.configure(region);
	}

	/**
	 * ---------- UNICO TEST DI QUESTA CLASSE
	 * 
	 * NOTA ho spostato l'inizializzazione di lru in quanto oggetto del test
	 * 
	 * Adds items to cache, gets them, and removes them. The item count is more than
	 * the size of the memory cache, so items should be dumped.
	 *
	 * @param region Name of the region to access
	 *
	 * @exception Exception If an error occurs
	 */
	@Test
	public void runTestForRegion() throws Exception {
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
