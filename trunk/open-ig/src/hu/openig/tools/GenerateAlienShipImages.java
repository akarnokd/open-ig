/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.imageio.ImageIO;

/**
 * Takes the existing alien ship images, creates 4 variants and puts upgrade stars onto them.
 * @author akarnokd, 2014-07-29
 */
public final class GenerateAlienShipImages {
	/** Tool class. */
	private GenerateAlienShipImages() { }
	/**
	 * Entry point.
	 * @param args no arguments
	 * @throws Exception ignored
	 */
	public static void main(String[] args) throws Exception {
		BufferedImage star = ImageIO.read(new File("images/generic/colony/upgrade.png"));
		String prefix = "images/generic/inventions/spaceships";
		int nstart = 1;
		int nend = 4;
		String[] ships = {
			"fighters/dargslan_fighter", "fighters/dargslan_fighter_%d",
			"fighters/dribs_fighter", "fighters/dribs_fighter_%d",
			"fighters/ecalep_fighter", "fighters/ecalep_fighter_%d",
			"fighters/morgath_fighter", "fighters/morgath_fighter_%d",
			"fighters/sullep_fighter", "fighters/sullep_fighter_%d",
			"fighters/ychom_fighter", "fighters/ychom_fighter_%d",

			"cruisers/dargslan_destroyer", "cruisers/dargslan_destroyer_%d",
			"cruisers/dribs_destroyer", "cruisers/dribs_destroyer_%d",
			"cruisers/ecalep_destroyer", "cruisers/ecalep_destroyer_%d",
			"cruisers/morgath_destroyer", "cruisers/morgath_destroyer_%d",
			"cruisers/sullep_destroyer", "cruisers/sullep_destroyer_%d",
			"cruisers/ychom_destroyer", "cruisers/ychom_destroyer_%d",

			"battleships/dargslan_battleship", "battleships/dargslan_battleship_%d",
			"battleships/dribs_battleship", "battleships/dribs_battleship_%d",
			"battleships/ecalep_battleship", "battleships/ecalep_battleship_%d",
			"battleships/morgath_battleship", "battleships/morgath_battleship_%d",
			"battleships/sullep_battleship", "battleships/sullep_battleship_%d",
			"battleships/ychom_battleship", "battleships/ychom_battleship_%d",

		};
		
		for (int i = 0; i < ships.length; i += 2) {
			String ship = prefix + "/" + ships[i];
			for (int j = nstart; j <= nend; j++) {
				String out = String.format(prefix + "/" + ships[i + 1], j);
				
				System.out.println(ship + " -> " + out);
				Files.copy(Paths.get(ship + "_rotate.png"), Paths.get(out + "_rotate.png"), StandardCopyOption.REPLACE_EXISTING);

				addStars(ship + ".png", out + ".png", star, j, 17);
				addStars(ship + "_huge.png", out + "_huge.png", star, j, 0);
				
				if (!Paths.get(ship + "_large.png").toFile().exists()) {
					BufferedImage huge = ImageIO.read(new File(ship + "_huge.png"));
					int dx = (204 - huge.getWidth()) / 2;
					int dy = (170 - huge.getHeight()) / 2;
					BufferedImage large = new BufferedImage(204, 170, BufferedImage.TYPE_INT_ARGB);
					Graphics2D g2 = large.createGraphics();
					g2.translate(dx, dy);
					g2.drawImage(huge, 0, 0, null);
					g2.dispose();
					
					ImageIO.write(large, "png", new File(ship + "_large.png"));
				}
				addStars(ship + "_large.png", out + "_large.png", star, j, 0);
				
			}
		}
	}
	/**
	 * Take the source image, add the stars to the bottom 1/4 part and save it under the output name.
	 * @param source the source file name
	 * @param output the output file name
	 * @param star the star icon
	 * @param count the number of stars
	 * @param offset additional shifting up
	 * @throws IOException on error
	 */
	static void addStars(String source, String output, BufferedImage star, int count, int offset) throws IOException {
		BufferedImage img = ImageIO.read(new File(source));
		int starWidth = star.getWidth() + 2;
		int dx = (img.getWidth() - count * starWidth) / 2;
		int dy = img.getHeight() * 3 / 4 - offset;
		if (dy + star.getHeight() / 2 > img.getHeight()) {
			dy = img.getHeight() - star.getHeight();
		}
		BufferedImage out = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = out.createGraphics();
		g2.drawImage(img, 0, 0, null);
		g2.translate(dx, dy);
		for (int i = 0; i < count; i++) {
			g2.drawImage(star, i * starWidth + 1, 0, null);
		}
		g2.dispose();
		ImageIO.write(out, "png", new File(output));
	}
}
