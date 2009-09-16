/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.ani;

import hu.openig.ani.SpidyAniDecoder.SpidyAniCallback;
import hu.openig.compress.ImageCompress;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Transcoder program to convert the images of the original ANI file to a more modern and smaller files.
 * @author karnokd, 2009.09.16.
 * @version $Revision 1.0$
 */
public final class Transcoder {
	/** Private constructor. */
	private Transcoder() {
		// utility program
	}
	/**
	 * @param args no arguments
	 * @throws Exception ignored
	 */
	public static void main(String[] args) throws Exception {
		final File in = new File("c:\\games\\IGHU\\message\\DIGI026.ANI");
		final AtomicLong size = new AtomicLong();
		final AtomicLong uncsize = new AtomicLong();
		SpidyAniDecoder.decodeLoop(new SpidyAniCallback() {
			int w, h;
			int[] lastImage;
			int frame;
			ByteArrayOutputStream bout = new ByteArrayOutputStream(1024);
			@Override
			public void stopped() {
				
			}
			
			@Override
			public boolean isStopped() {
				return false;
			}
			
			@Override
			public boolean isPaused() {
				return false;
			}
			
			@Override
			public void initialize(int width, int height, int frames, int languageCode,
					double fps, int audioDelay) {
				w = width;
				h = height;
				lastImage = new int[w * h];
			}
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
			@Override
			public void imageData(int[] image) {
				int[] current = image.clone();
				for (int i = 0; i < current.length; i++) {
					if (current[i] == lastImage[i]) {
						current[i] = 0; // transparent
					}
				}
				bout.reset();
				try {
					ImageCompress.compressImage(current, w, h, bout);
					size.addAndGet(bout.size());
					uncsize.addAndGet(w * h * 4 + 8);
				} catch (Throwable t) {
					t.printStackTrace();
				}
				lastImage = current;
				frame++;
				if (frame % 100 == 0) {
					System.out.printf("%s, %d -> %d (%.2f%%)%n", sdf.format(new Timestamp(System.currentTimeMillis())), uncsize.get() , size.get(), size.get() * 100f / uncsize.get());
				}
			}
			
			@Override
			public InputStream getNewInputStream() {
				try {
					return new FileInputStream(in);
				} catch (IOException ex) {
					throw new RuntimeException(in.toString(), ex);
				}
			}
			
			@Override
			public String getFileName() {
				return in.getAbsolutePath();
			}
			
			@Override
			public void finished() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void fatal(Throwable t) {
				
			}
			
			@Override
			public void audioData(byte[] data) {
				
			}
		});
		System.out.printf("%n%d -> %d -> %d%n", uncsize.get(), in.length(), size.get());
	}

}
