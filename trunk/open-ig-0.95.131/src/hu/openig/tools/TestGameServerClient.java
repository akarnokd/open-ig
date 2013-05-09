/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import hu.openig.net.MessageClient;
import hu.openig.net.MessageListener;
import hu.openig.net.MessageSerializable;

import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Simple test program to test the server and client objects.
 * @author akarnokd, 2013.04.22.
 */
public final class TestGameServerClient {
	/** Test program. */
	private TestGameServerClient() { }

	/**
	 * Program.
	 * @param args no arguments
	 * @throws Exception on error
	 */
	public static void main(String[] args) throws Exception {
		ExecutorService exec = Executors.newCachedThreadPool();
		InetAddress addr = InetAddress.getLoopbackAddress();
		int port = 13451;
		MessageListener listener = new MessageListener(addr, port, exec);
		
		listener.connect();
		
		MessageClient client = new MessageClient(addr, port);
		
		client.connect();
		
		// ---------------------------
		
		for (int i = 0; i < 100; i++) {
			Object response = client.query("PING { }\r\n");
			if (response instanceof Throwable) {
				((Throwable)response).printStackTrace();
				break;
			} else
			if (response instanceof MessageSerializable) {
				StringBuilder b = new StringBuilder();
				((MessageSerializable)response).save(b);
				System.out.println(b);
			}
		}
		
		// ---------------------------
		client.close();
		
		listener.close();
		
		exec.shutdown();
	}

}
