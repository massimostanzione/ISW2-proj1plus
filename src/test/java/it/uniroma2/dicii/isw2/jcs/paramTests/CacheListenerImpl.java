/*
 * Università degli Studi di Roma Tor Vergata
 * Faculty of Engineering
 * MSc in Computer Engineering
 * 
 * "Ingegneria del Software II" 2020/2021
 * Software Testing module
 * Project "1+"
 * 
 * Dummy cache listener for the testing of JCS "EventQueueConcurrentLoadTest" test.
 * This class has been separated from the Test class to allow it to be invoked
 * as new object while in a static context. Not modified.
 * 
 * Massimo Stanzione
 * matr. 0304936
 */
package it.uniroma2.dicii.isw2.jcs.paramTests;

import java.io.IOException;
import java.io.Serializable;

import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheListener;

/**
 * This is a dummy cache listener to use when testing the event queue.
 */
public class CacheListenerImpl implements ICacheListener {

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
