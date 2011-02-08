/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.launcher;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * @author karnokd, 2010.10.31.
 * @version $Revision 1.0$
 */
public class Downloader extends SwingWorker<Void, Void> {
	/** The download callback. */
	DownloadCallback callback;
	/** The thing to download. */
	String url;
	/** The local file to save into. */
	String localFile;
	/** The SHA1 hash computer. */
	MessageDigest sha1;
	/**
	 * Create a downloader.
	 * @param url the URL to download
	 * @param localFile the local file to save the data
	 * @param callback the progress callback
	 */
	public Downloader(String url, String localFile, DownloadCallback callback) {
		this.url = url;
		this.localFile = localFile;
		this.callback = callback;
		try {
			sha1 = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException ex) {
			ex.printStackTrace();
		}
	}
	@Override
	protected Void doInBackground() throws Exception {
		byte[] buffer = new byte[64 * 1024];
		final long t0 = System.currentTimeMillis();
		try {
			URL u = new URL(url);
			URLConnection uc = u.openConnection();
			final int bt = uc.getContentLength();
			int readSoFar = 0;
			InputStream in = u.openStream();
			try {
				FileOutputStream fout = new FileOutputStream(localFile);
				try {
					do {
						final int read = in.read(buffer);
						if (read > 0) {
							sha1.update(buffer, 0, read);
							fout.write(buffer, 0, read);
	
							readSoFar += read;
							final int freadsofar = readSoFar;
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									DownloadProgress pg = new DownloadProgress();
									pg.bytesReceived = freadsofar;
									pg.startTimestamp = t0;
									pg.bytesTotal = bt;
									callback.progress(pg);
								}
							});
						} else
						if (read < 0) {
							break;
						}
					} while (!isCancelled());
				} finally {
					fout.close();
				}
				if (!isCancelled()) {
					final byte[] sha1h = sha1.digest();
					final int freadsofar = readSoFar;
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							DownloadProgress pg = new DownloadProgress();
							pg.bytesReceived = freadsofar;
							pg.startTimestamp = t0;
							pg.bytesTotal = bt;
							callback.success(pg, sha1h);
						}
					});
				} else {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							callback.cancelled();
						}
					});
				}
			} finally {
				in.close();
			}
		} catch (final IOException ex) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					callback.failed(ex);
				}
			});
		}
		return null;
	}

}
