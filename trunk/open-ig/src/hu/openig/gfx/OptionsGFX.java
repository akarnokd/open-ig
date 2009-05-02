/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.gfx;

import hu.openig.utils.PCXImage;
import hu.openig.utils.ResourceMapper;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Options screen graphics.
 * @author karnokd, 2009.02.28.
 *
 */
public class OptionsGFX {
	/** The base image for options. */
	public static class Opts {
		/** The background image. */
		public BufferedImage options;
		/** Checkbox for reequip planets. */
		public final Rectangle btnReequipPlanets = new Rectangle();
		/** Checkbox for building names. */
		public final Rectangle btnBuildingNames = new Rectangle();
		/** Checkbox for building damage. */
		public final Rectangle btnBuildingDamage = new Rectangle();
		/** Checkbox for building damage battle. */
		public final Rectangle btnBuildingDamageBattle = new Rectangle();
		/** Checkbox for auto scroll. */
		public final Rectangle btnAutoScroll = new Rectangle();
		/** Checkbox for reequip rockets. */
		public final Rectangle btnReequipRockets = new Rectangle();
		/** Checkbox for repair buildings. */
		public final Rectangle btnRepairBuildings = new Rectangle();
		/** Checkbox for tax info. */
		public final Rectangle btnTaxInfo = new Rectangle();
		/** Checkbox for computer voice. */
		public final Rectangle btnComputerVoice = new Rectangle();
		/** Checkbox for animations. */
		public final Rectangle btnAnimations = new Rectangle();
		/** Load button rectangle. */
		public final Rectangle btnLoad = new Rectangle();
		/** Save button rectangle. */
		public final Rectangle btnSave = new Rectangle();
		/**Exit button rectangle. */
		public final Rectangle btnExit = new Rectangle();
		/** Rectangle for music adjustments. */
		public final Rectangle btnMusic = new Rectangle();
		/** Rectangle for audio adjustments. */
		public final Rectangle btnAudio = new Rectangle();
		/** Rectangle for speed adjustments. */
		public final Rectangle btnSpeed = new Rectangle();
		/** The load button down state image. */
		public BufferedImage btnLoadDown;
		/** The load button disabled state image. */
		public BufferedImage btnLoadDisabled;
		/** The save button down state image. */
		public BufferedImage btnSaveDown;
		/** The save button disabled state image. */
		public BufferedImage btnSaveDisabled;
		/** The exit button donw state image. */
		public BufferedImage btnExitDown;
		/** The slider image. */
		public BufferedImage slider;
		/** The checkmark image. */
		public BufferedImage checkmark;
		/** The save list area. */
		public Rectangle listArea = new Rectangle();
		/** The background area. */
		public Rectangle background = new Rectangle();
		/** The enclosing settings rectangle. */
		public final Rectangle settingsRect = new Rectangle();
		/** The volume adjustment rectangle. */
		public final Rectangle volumeRect = new Rectangle();
	}
	/** The option screen settings. */
	public final Opts[] opts;
	/**
	 * Constructor. Loads the graphics.
	 * @param resMap the resource mapper object
	 */
	public OptionsGFX(ResourceMapper resMap) {
		opts = new Opts[2];
		for (int i = 0; i < opts.length; i++) {
			opts[i] = new Opts();
		}
		opts[0].options = PCXImage.from(resMap.get("SCREENS/OPTIONS1.PCX"), -1);
		
		BufferedImage bimg = PCXImage.from(resMap.get("SCREENS/OPT1_X.PCX"), -2);
		opts[0].btnLoadDown = bimg.getSubimage(138, 42, 120, 41);
		opts[0].btnLoadDisabled = bimg.getSubimage(138, 84, 120, 41);
		opts[0].btnSaveDown = bimg.getSubimage(259, 42, 118, 41);
		opts[0].btnSaveDisabled = bimg.getSubimage(259, 84, 118, 41);
		opts[0].btnExitDown = bimg.getSubimage(378, 42, 119, 41);
		opts[0].slider = bimg.getSubimage(548, 0, 22, 12);
		opts[0].checkmark = bimg.getSubimage(571, 0, 23, 19);
		
		opts[1].options = PCXImage.from(resMap.get("SCREENS/OPTIONS2.PCX"), -1); 
		bimg = PCXImage.from(resMap.get("SCREENS/OPT2_X.PCX"), -2);
		opts[1].btnLoadDown = bimg.getSubimage(138, 42, 120, 41);
		opts[1].btnLoadDisabled = bimg.getSubimage(138, 84, 120, 41);
		opts[1].btnSaveDown = bimg.getSubimage(259, 42, 118, 41);
		opts[1].btnSaveDisabled = bimg.getSubimage(259, 84, 118, 41);
		opts[1].btnExitDown = bimg.getSubimage(378, 42, 117, 41);
		opts[1].slider = bimg.getSubimage(548, 0, 22, 12);
		opts[1].checkmark = bimg.getSubimage(571, 0, 23, 19);
	}
	/**
	 * Set rectangle locations for options image 1 relative to x and y.
	 * @param o the options record
	 * @param x the horizontal position of the main option image
	 * @param y the vertical position of the main option image
	 */
	public void setLocations1(Opts o, int x, int y) {
		o.btnReequipPlanets.setBounds(x + 17, y + 14, 13, 13);
		o.btnBuildingNames.setBounds(x + 17, y + 32, 13, 13);
		o.btnBuildingDamage.setBounds(x + 17, y + 50, 13, 13);
		o.btnBuildingDamageBattle.setBounds(x + 17, y + 68, 13, 13);
		o.btnAutoScroll.setBounds(x + 17, y + 86, 13, 13);
		
		o.btnReequipRockets.setBounds(x + 281, y + 14, 13, 13);
		o.btnRepairBuildings.setBounds(x + 281, y + 32, 13, 13);
		o.btnTaxInfo.setBounds(x + 281, y + 50, 13, 13);
		o.btnComputerVoice.setBounds(x + 281, y + 68, 13, 13);
		o.btnAnimations.setBounds(x + 281, y + 86, 13, 13);
		
		o.btnLoad.setBounds(x + 28, y + 123, 120, 41);
		o.btnSave.setBounds(x + 156, y + 123, 118, 41);
		o.btnExit.setBounds(x + 281, y + 123, 119, 41);
		
		o.listArea.setBounds(x + 9, y + 174, 409, 261);
		
		o.btnMusic.setBounds(x + 478, y + 224, 25, 201);
		o.btnAudio.setBounds(x + 531, y + 224, 25, 201);
		o.btnSpeed.setBounds(x + 584, y + 224, 25, 201);
		
		o.background.setBounds(x, y, o.options.getWidth(), o.options.getHeight());
		o.settingsRect.setBounds(x, y, o.options.getWidth(), 110);
		o.volumeRect.setBounds(x + 478, y + 216, 129, 218);
	}
	/**
	 * Set rectangle locations for options image 2 relative to x and y.
	 * @param o the options record
	 * @param x the horizontal position of the main option image
	 * @param y the vertical position of the main option image
	 */
	public void setLocations2(Opts o, int x, int y) {
		o.btnReequipPlanets.setBounds(x + 17, y + 13, 270, 13);
		o.btnBuildingNames.setBounds(x + 17, y + 32, 270, 13);
		o.btnBuildingDamage.setBounds(x + 17, y + 50, 270, 13);
		o.btnBuildingDamageBattle.setBounds(x + 17, y + 68, 270, 13);
		o.btnAutoScroll.setBounds(x + 17, y + 86, 270, 13);
		
		o.btnReequipRockets.setBounds(x + 281, y + 13, 270, 13);
		o.btnRepairBuildings.setBounds(x + 281, y + 32, 270, 13);
		o.btnTaxInfo.setBounds(x + 281, y + 50, 270, 13);
		o.btnComputerVoice.setBounds(x + 281, y + 68, 270, 13);
		o.btnAnimations.setBounds(x + 281, y + 86, 270, 13);
		
		o.btnLoad.setBounds(x + 27, y + 123, 120, 41);
		o.btnSave.setBounds(x + 156, y + 123, 118, 41);
		o.btnExit.setBounds(x + 281, y + 123, 117, 41);
		
		o.listArea.setBounds(x + 9, y + 174, 409, 261);
		
		o.btnMusic.setBounds(x + 478, y + 223, 23, 201);
		o.btnAudio.setBounds(x + 531, y + 223, 23, 201);
		o.btnSpeed.setBounds(x + 584, y + 223, 23, 201);
		
		o.background.setBounds(x, y, o.options.getWidth(), o.options.getHeight());
		o.settingsRect.setBounds(x, y, o.options.getWidth(), 110);
		o.volumeRect.setBounds(x + 478, y + 216, 129, 218);
	}
}
