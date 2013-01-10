/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Utility class to receive multiple files through a TCP connection
 * via custom protocol.
 * @author akarnokd, 2011.12.26.
 */
public final class SimpleTransferReceive {
	/** Utility class. */
	private SimpleTransferReceive() {
	}

	/**
	 * Main program.
	 * @param args no arguments
	 * @throws Exception ignored
	 */
	public static void main(String[] args) throws Exception {
		Socket socket = new Socket(InetAddress.getByName("10.0.2.2"), 5555);
		try {
			DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream(), 8192));
			byte[] buffer = new byte[8192];
			try {
				while (true) {
					long length = in.readLong();
					int nameLen = in.readInt();
					byte[] namebuff = new byte[nameLen * 2];
					in.read(namebuff);
					String name = new String(namebuff, "UTF-16");
					FileOutputStream fout = new FileOutputStream(name);
					System.out.printf("Receiving %s.", name);
					try {
						while (length > 0) {
							int n = (length > buffer.length ? buffer.length : (int)length);
							int r = in.read(buffer, 0, n);
							length -= r;
							if (r > 0) {
								fout.write(buffer, 0, r);
							} else
							if (r < 0) {
								break;
							}
						}
					} finally {
						System.out.println("Done.");
						fout.close();
					}
				}
			} finally {
				in.close();
			}
		} finally {
			socket.close();
		}
	}

}
