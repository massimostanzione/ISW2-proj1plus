/*
 * Università degli Studi di Roma Tor Vergata
 * Faculty of Engineering
 * MSc in Computer Engineering
 * 
 * "Ingegneria del Software II" 2020/2021
 * Software Testing module
 * Project "1+"
 * 
 * Porting of the JCS "EventQueueConcurrentLoadTest" test from JUnit3 to JUnit 4,
 * implementing it with Parameterized class.
 * 
 * NOTICE: The "CacheListenerImpl" class has been separated from the Test class to allow it
 * to be invoked as new object while in a static context.
 *
 * Massimo Stanzione
 * matr. 0304936
 */

package it.uniroma2.dicii.isw2.jcs.paramTests;

import static org.junit.Assert.assertTrue;

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

import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.CacheEventQueue;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * This test case is designed to makes sure there are no deadlocks in the event
 * queue. The time to live should be set to a very short interval to make a
 * deadlock more likely.
 *
 * @author Aaron Smuts
 */
@RunWith(Parameterized.class)
public class EventQueueConcurrentLoadTest {
	private static CacheEventQueue queue = null;
	private static CacheListenerImpl listen = null;
	private static int maxFailure = 3;
	private static int waitBeforeRetry = 100;
	// very small idle time
	private static int idleTime = 2;

	// Test parameters
	private String testCaseName;
	private int end;
	private int expectedPutCount;

	/**
	 * Constructor.
	 * 
	 * @param testCaseName     name of the test to be executed
	 * @param end              test parameter
	 * @param expectedPutCount test parameter
	 */
	public EventQueueConcurrentLoadTest(String testCaseName, Integer end, Integer expectedPutCount) {
		// Test parameters initialization
		this.testCaseName = testCaseName;
		this.end = end;
		this.expectedPutCount = expectedPutCount;
	}

	/**
	 * Test setup. Create the static queue to be used by all tests
	 * 
	 * Test environment configuration. This method is annotated as @BeforeClass and
	 * not @Before because the test to be run are in concurrence, and they use only
	 * one queue, so this method has to be executed only once before all the tests,
	 * and not before each of them.
	 * 
	 * Corresponding to JUnit3 "setUp" method.
	 * 
	 * @implNote must be public and static.
	 */
	@BeforeClass
	public static void configure() {
		listen = new CacheListenerImpl();
		queue = new CacheEventQueue(listen, 1L, "testCache1", maxFailure, waitBeforeRetry);
		queue.setWaitToDieMillis(idleTime);
	}

	/**
	 * Parameters association. The three test parameters involved have been declared
	 * earlier as attributes in this class. The values assumed by each of them are
	 * here defined, as triads of values: {testCaseame, end, EPC}.
	 * 
	 * For tests that need less parameters, the dummy value "none" is set, because a
	 * null value would raise a NullPointerException.
	 * 
	 * @return array containing triads with actual values of the test parameters
	 */
	@Parameters
	public static Collection<Object[]> getTestParameters() {
		int none = -1;
		return Arrays.asList(new Object[][] { { "testRunPutTest1", 200, 200 }, { "testRunPutTest2", 1200, 1400 },
				{ "testRunRemoveTest1", 2200, none }, { "testStopProcessing1", none, none },
				{ "testRunPutTest4", 5200, 6600 }, { "testRunRemoveTest2", 5200, none },
				{ "testStopProcessing2", none, none }, { "testRunPutDelayTest1", 100, 6700 }

		});
	}

	/**
	 * The test suite.
	 * 
	 * Since there is not an exact matching between JUnit3 and JUnit4 for test
	 * suites, the purpose of this method is to be a "switcher" in selecting which
	 * test has to be executed, parsing the testCaseName string.
	 * 
	 * In detail, each testCaseName is identified by a string like "testNameTestX",
	 * with "X" an index integer.
	 * 
	 * @throws Exception when a testCaseName is not recognized
	 */
	@Test(timeout=10000)
	public void compactedSuiteSwitcherTest() throws Exception {
		String testToBeExecuted = testCaseName;
		System.out.println("Starting test: " + testCaseName);
		// Trim the last character, if integer index
		if (Character.isDigit(testCaseName.charAt(testCaseName.length() - 1))) {
			testToBeExecuted = testCaseName.substring(0, testCaseName.length() - 1);
		}
		switch (testToBeExecuted) {
		case "testRunPutTest":
			this.runPutTest(end, expectedPutCount);
			break;
		case "testRunRemoveTest":
			this.runRemoveTest(end);
			break;
		case "testStopProcessing":
			this.runStopProcessingTest();
			break;
		case "testRunPutDelayTest":
			this.runPutDelayTest(end, expectedPutCount);
			break;
		default:
			throw new Exception(
					"Test name not recognized for \"" + testCaseName + "\", parsed as \"" + testToBeExecuted + "\".");
		}
	}

	/**
	 * Test logic, not modified.
	 * 
	 * Adds put events to the queue.
	 *
	 * @param end
	 * @param expectedPutCount
	 * @throws Exception
	 */
	public void runPutTest(int end, int expectedPutCount) throws Exception {
		for (int i = 0; i <= end; i++) {
			CacheElement elem = new CacheElement("testCache1", i + ":key", i + "data");
			queue.addPutEvent(elem);
		}

		while (!queue.isEmpty()) {
			synchronized (this) {
				System.out.println("queue is still busy, waiting 250 millis");
				this.wait(250);
			}
		}
		System.out.println("queue is empty, comparing putCount");

		// this becomes less accurate with each test. It should never fail. If
		// it does things are very off.
		assertTrue("The put count [" + listen.putCount + "] is below the expected minimum threshold ["
				+ expectedPutCount + "]", listen.putCount >= (expectedPutCount - 1));

	}

	/**
	 * Test logic, not modified.
	 * 
	 * Add remove events to the event queue.
	 *
	 * @param end
	 * @throws Exception
	 */
	public void runRemoveTest(int end) throws Exception {
		for (int i = 0; i <= end; i++) {
			queue.addRemoveEvent(i + ":key");
		}

	}

	/**
	 * Test logic, not modified.
	 * 
	 * Add remove events to the event queue.
	 *
	 * @throws Exception
	 */
	public void runStopProcessingTest() throws Exception {
		queue.stopProcessing();
	}

	/**
	 * Test logic, not modified.
	 * 
	 * Test putting and a delay. Waits until queue is empty to start.
	 *
	 * @param end
	 * @param expectedPutCount
	 * @throws Exception
	 */
	public void runPutDelayTest(int end, int expectedPutCount) throws Exception {
		while (!queue.isEmpty()) {
			synchronized (this) {
				System.out.println("queue is busy, waiting 250 millis to begin");
				this.wait(250);
			}
		}
		System.out.println("queue is empty, begin");

		// get it going
		CacheElement elem = new CacheElement("testCache1", "a:key", "adata");
		queue.addPutEvent(elem);

		for (int i = 0; i <= end; i++) {
			synchronized (this) {
				if (i % 2 == 0) {
					this.wait(idleTime);
				} else {
					this.wait(idleTime / 2);
				}
			}
			CacheElement elem2 = new CacheElement("testCache1", i + ":key", i + "data");
			queue.addPutEvent(elem2);
		}

		while (!queue.isEmpty()) {
			synchronized (this) {
				System.out.println("queue is still busy, waiting 250 millis");
				this.wait(250);
			}
		}
		System.out.println("queue is empty, comparing putCount");

		Thread.sleep(1000);

		// this becomes less accurate with each test. It should never fail. If
		// it does things are very off.
		assertTrue("The put count [" + listen.putCount + "] is below the expected minimum threshold ["
				+ expectedPutCount + "]", listen.putCount >= (expectedPutCount - 1));
	}
}
