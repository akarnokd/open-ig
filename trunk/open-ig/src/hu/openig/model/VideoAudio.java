/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.model;

/** The video and audio record. */
public class VideoAudio {
	/** The video resource. */
	public String video;
	/** The audio resource. */
	public String audio;
	/**
	 * Constructor.
	 */
	public VideoAudio() {
		
	}
	/**
	 * Constructor.
	 * @param video the video
	 * @param audio the audio
	 */
	public VideoAudio(String video, String audio) {
		this.video = video;
		this.audio = audio;
	}
}
