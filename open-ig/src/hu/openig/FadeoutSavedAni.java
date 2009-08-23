/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;

import javax.imageio.ImageIO;

/**
 * Small utility which creates additional PNG images that fade out
 * to balance the audio/frame length discrepancy of the ANI files manally.
 * @author karnok, 2009.08.21.
 * @version $Revision 1.0$
 */
public final class FadeoutSavedAni {
	/** Private constructor. */
	private FadeoutSavedAni() {
		
	}
	/**
	 * Add more frames to the video.
	 * @param args the arguments
	 * @throws Exception in any case
	 */
	public static void main(String[] args) throws Exception {
		String name = "MEGNYER.ANI";
		File file = new File("d:\\games\\ighu\\youtube\\" + name);
		File audio = new File(file, name + ".wav");
		File[] frames = file.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().toUpperCase().endsWith(".PNG");
			}
		});
		if (frames != null) {
			Arrays.sort(frames, new Comparator<File>() {
				@Override
				public int compare(File o1, File o2) {
					return o1.getName().compareToIgnoreCase(o2.getName());
				}
			});
			File last = frames[frames.length - 1];
			System.out.printf("Last frame: %s%n", last);
			long audiolen = audio.length() - 36;
			
			double timediff = frames.length / 17.89 /* FPS */ - audiolen / 22050.0 /* Bytes/s */;
			double framediff = timediff * 17.89;
			int transitions = (int)Math.floor(framediff);
			System.out.printf("Frames: %d, Frame time: %02d:%02d.%03d, Audio length: %02d:%02d.%03d%n", frames.length,
					(int)(frames.length / 17.89 / 60), (int)(frames.length / 17.89) % 60, (int)(frames.length / 17.89) % 1000,
					(int)(audiolen / 22050.0 / 60), (int)(audiolen / 22050.0) % 60, (int)(audiolen / 22.050) % 1000);
			System.out.printf("Time difference: %.3f sec, Frame difference: %d frames%n", timediff, transitions);
			if (framediff < 0) {
				BufferedImage bimg = ImageIO.read(last);
				BufferedImage back = new BufferedImage(bimg.getWidth(), bimg.getHeight(), BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2 = back.createGraphics();
				int fd = Math.abs(transitions);
				Composite c = g2.getComposite();
				for (int i = 0; i < fd; i++) {
					g2.setComposite(c);
					g2.setColor(Color.BLACK);
					g2.fillRect(0, 0, back.getWidth(), back.getHeight());
					g2.setComposite(AlphaComposite.SrcOver.derive((fd - i) * 1.0f / fd));
					g2.drawImage(bimg, 0, 0, null);
					ImageIO.write(back, "png", new File(file, String.format("%s-%05d.png", name, frames.length + i)));
					if (i % 50 == 0 && i > 0) {
						System.out.println();
					}
					if (i % 100 == 0) {
						System.out.print("*");
					} else
					if (i % 10 == 0) {
						System.out.print("|");
					} else {
						System.out.print(".");
					}
				}
				g2.dispose();
			}
		}
	}

}
