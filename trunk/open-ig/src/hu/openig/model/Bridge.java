/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The definition of the bridge screen and messages.
 * @author akarnokd, 2010.01.17.
 */
public class Bridge {
	/** The level specific graphics. */
	public static class Level {
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
		public final BufferedImage[] up = new BufferedImage[3];
		/** The down arrow normal, pressed, empty. */
		public final BufferedImage[] down = new BufferedImage[3];
		/** The send button normal, pressed. */
		public final BufferedImage[] send = new BufferedImage[2];
		/** The receive button normal, pressed. */
		public final BufferedImage[] receive = new BufferedImage[2];
		/** The associated ship walk. */
		public WalkPosition walk;
		/** The ship for the walks. */
		public WalkShip ship;
	}
	/** The message description. */
	public static class Message {
		/** The identifier. */
		public String id;
		/** The media. */
		public String media;
		/** The title. */
		public String title;
		/** The description. */
		public String description;
	}
	/** The list of level graphics. */
	public final Map<Integer, Level> levels = new HashMap<Integer, Level>();
	/** The messages to send. */
	public final List<Message> sendMessages = new ArrayList<Message>();
	/** The messages to receive. */
	public final List<Message> receiveMessages = new ArrayList<Message>();
}
