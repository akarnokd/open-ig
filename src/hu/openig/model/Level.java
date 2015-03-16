/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.image.BufferedImage;

/** The level specific graphics. */
public class Level {
	/** The level number. */
	public int id;
	/** The bridge base image. */
	public BufferedImage image;
	/** Message panel appears. */
	public final VideoAudio messageAppear = new VideoAudio();
	/** Message panel opens. */
	public final VideoAudio messageOpen = new VideoAudio();
	/** Message panel closes. */
	public final VideoAudio messageClose = new VideoAudio();
	/** Projector opens. */
	public final VideoAudio projectorOpen = new VideoAudio();
	/** Projector closes. */
	public final VideoAudio projectorClose = new VideoAudio();
	/** The up arrow normal, pressed, empty. */
	public final BufferedImage[] up = new BufferedImage[2];
	/** The up arrow empty. */
	public BufferedImage upEmpty;
	/** The down arrow normal, pressed, empty. */
	public final BufferedImage[] down = new BufferedImage[2];
	/** The down arrow empty. */
	public BufferedImage downEmpty;
	/** The send button normal, pressed. */
	public final BufferedImage[] send = new BufferedImage[2];
	/** The receive button normal, pressed. */
	public final BufferedImage[] receive = new BufferedImage[2];
	/** The ship for the walks. */
	public WalkShip ship;
}
