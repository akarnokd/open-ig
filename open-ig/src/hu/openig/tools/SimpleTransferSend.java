/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Transfer files through a simple tcp protocol.
 * @author akarnokd, 2011.12.26.
 */
public final class SimpleTransferSend {
	/** Utility class. */
	private SimpleTransferSend() {
		
	}
	/**
	 * Main program.
	 * @param args no arguments
	 * @throws Exception on error
	 */
	public static void main(String[] args) throws Exception {
		String basePath = "c:/temp/open-ig-launcher/";
		String[] files = {
			"open-ig-videos-01-0.8.zip",	
			"open-ig-videos-02-0.8.zip",	
			"open-ig-videos-03-0.8.zip",	
			"open-ig-videos-04-0.8.zip",	
			"open-ig-videos-05-0.8.zip",	
			"open-ig-videos-06-0.8.zip",	
			"open-ig-videos-07-0.8.zip",	
			"open-ig-videos-08-0.8.zip",	
			"open-ig-videos-09-0.8.zip",	
			"open-ig-videos-10-0.8.zip",
			"open-ig-videos-en-0.8.zip",	
			"open-ig-videos-hu-0.8.zip",	
		};
		ServerSocket ss = new ServerSocket(5555);
		byte[] buffer = new byte[8192];
		while (!Thread.currentThread().isInterrupted()) {
			Socket socket = ss.accept();
			try {
				DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream(), 8192));
				try {
					for (String f : files) {
						String name = basePath + f;
						System.out.printf("Sending %s.", name);
						File fn = new File(name);
						InputStream in = new FileInputStream(fn);
						try {
							out.writeLong(fn.length());
							out.writeInt(f.length());
							out.writeChars(f);
							while (true) {
								int r = in.read(buffer);
								if (r > 0) {
									out.write(buffer, 0, r);
								} else
								if (r < 0) {
									break;
								}
							}
							out.flush();
						} finally {
							System.out.println("Done.");
							in.close();
						}
					}
				} finally {
					out.close();
				}
			} finally {
				socket.close();
			}
		}
	}

}
