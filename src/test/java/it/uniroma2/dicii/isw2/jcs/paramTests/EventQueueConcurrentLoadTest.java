package it.uniroma2.dicii.isw2.jcs.paramTests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.CacheEventQueue;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheListener;
import org.apache.jcs.engine.control.CompositeCacheManager;
import org.apache.jcs.engine.memory.lru.LRUMemoryCache;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

//import it.uniroma2.dicii.isw2.jcs.paramTests.EventQueueConcurrentLoadTest.CacheListenerImpl;
@RunWith(Parameterized.class)
public class EventQueueConcurrentLoadTest {

	// i parametri sono attributi privati della classe di test (2)
	// "the tested object, the input provided to the tested object, and the expected
	// output."
	// ---------- PARAMETRI DEL TEST
	// sono attributi privati della classe di test [2]
	// oggetto testato
	private static CacheEventQueue queue = null;
	private static CacheListenerImpl listen = null;
	private int maxFailure = 3;
	private int waitBeforeRetry = 100;
	// very small idle time
	private int idleTime = 2;

	/**
	 * ---------- CONFIGURAZIONE oggetto da testare, che è queue, vedasi
	 * dichiarazione negli attributi della classe
	 * 
	 * lo posso invocare da costruttore, @Before, @BeforeClass...
	 * 
	 * @param region
	 * 
	 * @see queue
	 */
	@Before // TODO può coesistere con chiamate esplicite?
	private void configure(int maxFailure, int waitBeforeRetry, int idleTime) {
		// inizializzazione attributo
		this.maxFailure = maxFailure;
		this.waitBeforeRetry = waitBeforeRetry;
		this.idleTime = idleTime;

		// istanziazione dell'oggetto da testare
		listen = new CacheListenerImpl();
		queue = new CacheEventQueue(listen, 1L, "testCache1", maxFailure, waitBeforeRetry);
		queue.setWaitToDieMillis(idleTime);
	}

	/**
	 * ---------- SETTAGGIO VALORI DEI PARAMETRI PER IL TEST
	 * 
	 * @return array con i parametri, tipo Collection [3]
	 */
	@Parameters
	public static Collection<String[]> getTestParameters() {

		// ...?

		// return Arrays.asList(new Integer[][] { { 0, 0, 0 }, { 1, 1, 1 } });
		return null; // FIXME

	}

	/**
	 * ---------- COSTRUTTORE
	 * 
	 * ha gli stessi parametri del test [4]
	 * 
	 * @param region
	 */
	public EventQueueConcurrentLoadTest(int maxFailure, int waitBeforeRetry, int idleTime) {
		this.configure(maxFailure, waitBeforeRetry, idleTime);
	}

	/**
	 * Adds put events to the queue.
	 *
	 * @param end
	 * @param expectedPutCount
	 * @throws Exception
	 */
	@Test
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
	 * Add remove events to the event queue.
	 *
	 * @param end
	 * @throws Exception
	 */
	@Test // TODO lo è?
	public void runRemoveTest(int end) throws Exception {
		for (int i = 0; i <= end; i++) {
			queue.addRemoveEvent(i + ":key");
		}

	}

	/**
	 * Add remove events to the event queue.
	 *
	 * @throws Exception
	 */
	@Test // TODO lo è?
	public void runStopProcessingTest() throws Exception {
		queue.stopProcessing();
	}

	/**
	 * Test putting and a delay. Waits until queue is empty to start.
	 *
	 * @param end
	 * @param expectedPutCount
	 * @throws Exception
	 */
	@Test
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

	/**
	 * This is a dummy cache listener to use when testing the event queue.
	 */
	private class CacheListenerImpl implements ICacheListener {

		/**
		 * <code>putCount</code>
		 */
		protected int putCount = 0;

		/**
		 * <code>removeCount</code>
		 */
		protected int removeCount = 0;

		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * org.apache.jcs.engine.behavior.ICacheListener#handlePut(org.apache.jcs.engine
		 * .behavior.ICacheElement)
		 */
		public void handlePut(ICacheElement item) throws IOException {
			synchronized (this) {
				putCount++;
			}
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * org.apache.jcs.engine.behavior.ICacheListener#handleRemove(java.lang.String,
		 * java.io.Serializable)
		 */
		public void handleRemove(String cacheName, Serializable key) throws IOException {
			synchronized (this) {
				removeCount++;
			}

		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.apache.jcs.engine.behavior.ICacheListener#handleRemoveAll(java.lang.
		 * String)
		 */
		public void handleRemoveAll(String cacheName) throws IOException {
			// TODO Auto-generated method stub

		}

		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * org.apache.jcs.engine.behavior.ICacheListener#handleDispose(java.lang.String)
		 */
		public void handleDispose(String cacheName) throws IOException {
			// TODO Auto-generated method stub

		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.apache.jcs.engine.behavior.ICacheListener#setListenerId(long)
		 */
		public void setListenerId(long id) throws IOException {
			// TODO Auto-generated method stub

		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.apache.jcs.engine.behavior.ICacheListener#getListenerId()
		 */
		public long getListenerId() throws IOException {
			// TODO Auto-generated method stub
			return 0;
		}

	}
}
