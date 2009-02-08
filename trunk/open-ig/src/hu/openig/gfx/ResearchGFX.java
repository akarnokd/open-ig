/*
 * Copyright 2008, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.gfx;

import hu.openig.utils.PCXImage;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Research screen graphics.
 * @author karnokd, 2009.02.08.
 * @version $Revision 1.0$
 */
public class ResearchGFX {
	/** The research base screen. */
	public BufferedImage researchScreen;
	public BufferedImage btnEmpty;
	public BufferedImage btnStart;
	public BufferedImage btnStartDown;
	public BufferedImage btnEquipmentDown;
	public BufferedImage btnProductDown;
	public BufferedImage btnBridgeDown;
	/** The current selection indicator arrow. */ 
	public BufferedImage arrow;
	public BufferedImage btnMoneyDown;
	public BufferedImage btnViewDown;
	/** The smaller empty button region. */
	public BufferedImage btnEmptySmall;
	public BufferedImage btnStopDown;
	public BufferedImage tabSpaceships;
	public BufferedImage tabEquipment;
	public BufferedImage tabWeapons;
	public BufferedImage tabBuildings;
	/** Spaceship options. */
	public BufferedImage optSpaceships;
	public BufferedImage optEquipment;
	public BufferedImage optWeapons;
	public BufferedImage optBuildings;
	/** Spaceships - Fighters suboptions. */
	public BufferedImage optFighters;
	public BufferedImage optDestroyers;
	public BufferedImage optFlagships;
	public BufferedImage optSatellites;
	public BufferedImage optSpaceStations;
	/** Equipment - Hyperdrives. */
	public BufferedImage optHyperdrives;
	public BufferedImage optModules;
	public BufferedImage optRadars;
	public BufferedImage optShields;
	/** Weapons - Laser weapons. */
	public BufferedImage optLasers;
	public BufferedImage optGuns;
	public BufferedImage optBombsMissiles;
	public BufferedImage optTanks;
	public BufferedImage optVehicles;
	/** Buildings - Buildings. */
	public BufferedImage optColonyBuildings;
	public BufferedImage optMilitaryBuildings;
	public BufferedImage optColonyRadars;
	public BufferedImage optColonyGuns;
	// research screen main regions relative
	public Rectangle rectAnimation;
	public Rectangle[] rectResearch;
	public Rectangle rectProjectName;
	public Rectangle rectProjectStatus;
	public Rectangle rectCompleted;
	public Rectangle rectTimeRemaining;
	/** Number of civil enginering rectangle. */
	public Rectangle rectCiv;
	public Rectangle rectMech;
	public Rectangle rectComp;
	public Rectangle rectAI;
	public Rectangle rectMil;
	/** Start button location. */
	public Rectangle rectStart;
	public Rectangle rectEquipment;
	public Rectangle rectProduction;
	public Rectangle rectBridge;
	/** The current project name. */
	public Rectangle rectProject;
	/** The current project money. */
	public Rectangle rectMoney;
	/** The more money button area. */
	public Rectangle rectMoreMoney;
	/** The less money button area. */
	public Rectangle rectLessMoney;
	/** The adjust money entire button rectangle. */
	public Rectangle rectMoneyAdjust;
	/** The current percent. */
	public Rectangle rectPercent;
	/** Current number of civil enginerring rectangle. */
	public Rectangle rectCurrCiv;
	public Rectangle rectCurrMech;
	public Rectangle rectCurrComp;
	public Rectangle rectCurrAI;
	public Rectangle rectCurrMil;
	/** View current research in the tech tree. */
	public Rectangle rectView;
	/** Stop research. */
	public Rectangle rectStop;
	/** The research description area. */
	public Rectangle rectDescription;
	/** The areas for required research. */
	public Rectangle[] rectNeeded;
	/** The areas for the main options labels. */
	public Rectangle[] rectMainOptions;
	/** The areas for the sub options labels. */
	public Rectangle[] rectSubOptions;
	/** Areas for the current research selector arrow. */
	public Rectangle[] rectMainArrow;
	/** Areas for the current research selector arrow in sub options. */
	public Rectangle[] rectSubArrow;
	/** Fix for the bridge button's rendering anomaly. */
	public Rectangle rectBridgeFix;
	/** The location for the bridge button's fix. */
	public BufferedImage btnBridgeFix;
	/**
	 * Constructor. Loads the images.
	 * @param root the root directory of IG
	 */
	public ResearchGFX(String root) {
		researchScreen = PCXImage.from(root + "/SCREEN/FEJLESZT.PCX", -1);
		
		BufferedImage res = PCXImage.from(root + "/SCREEN/FEJL_X.PCX", -1);
		btnEmpty = res.getSubimage(336, 0, 102, 39);
		btnStart = res.getSubimage(336, 39, 102, 39);
		btnStartDown = res.getSubimage(336, 78, 102, 39);
		btnEquipmentDown = res.getSubimage(336, 117, 102, 39);
		// the original source is malformed
		btnProductDown = res.getSubimage(336, 156, 101, 39);
		btnBridgeDown = res.getSubimage(336, 195, 101, 39);
		arrow = res.getSubimage(568, 0, 15, 11);
		// fix arrow background to transparent
		for (int i = 0; i < arrow.getHeight(); i++) {
			for (int j = 0; j < arrow.getWidth(); j++) {
				int c = arrow.getRGB(j, i);
				if ((c & 0xFFFFFF) == 0) {
					arrow.setRGB(j, i, 0);
				}
			}
		}
		btnMoneyDown = res.getSubimage(549, 104, 57, 19);
		btnViewDown = res.getSubimage(437, 189, 115, 23);
		btnEmptySmall = res.getSubimage(437, 212, 115, 23);
		btnStopDown = res.getSubimage(437, 258, 115, 23);
		
		tabSpaceships = res.getSubimage(438, 0, 111, 21);
		tabEquipment = res.getSubimage(438, 22, 111, 21);
		tabWeapons = res.getSubimage(438, 44, 111, 21);
		tabBuildings = res.getSubimage(438, 65, 111, 18);
	
		optSpaceships = res.getSubimage(0, 0, 168, 78);
		optEquipment = res.getSubimage(0, 78, 168, 78);
		optWeapons = res.getSubimage(0, 156, 168, 78);
		optBuildings = res.getSubimage(0, 234, 168, 78);
		
		optSpaceships = res.getSubimage(168, 0, 168, 14);
		optDestroyers = res.getSubimage(168, 16, 168, 14);
		optFlagships = res.getSubimage(168, 32, 168, 14);
		optSatellites = res.getSubimage(168, 48, 168, 14);
		optSpaceStations = res.getSubimage(168, 64, 168, 14);
		
		optHyperdrives = res.getSubimage(168, 78, 168, 14);
		optModules = res.getSubimage(168, 94, 168, 14);
		optRadars = res.getSubimage(168, 110, 168, 14);
		optShields = res.getSubimage(168, 126, 168, 14);
		
		optLasers = res.getSubimage(168, 156, 168, 14);
		optGuns = res.getSubimage(168, 172, 168, 14);
		optBombsMissiles = res.getSubimage(168, 188, 168, 14);
		optTanks = res.getSubimage(168, 204, 168, 14);
		optVehicles = res.getSubimage(168, 220, 168, 14);
		
		optColonyBuildings = res.getSubimage(168, 234, 168, 14);
		optMilitaryBuildings = res.getSubimage(168, 250, 168, 14);
		optColonyRadars = res.getSubimage(168, 266, 168, 14);
		optColonyGuns = res.getSubimage(168, 282, 168, 14);
		
		// various locations on the research screen
		rectAnimation = new Rectangle(2, 2, 316, 196);
		rectResearch = new Rectangle[6];
		for (int i = 0; i < rectResearch.length; i++) {
			rectResearch[i] = new Rectangle(3 + 106 * i, 201, 104, 78);
		};
		rectProjectName = new Rectangle(128, 192, 156, 10);
		rectProjectStatus = new Rectangle(128, 311, 156, 10);
		rectProject = new Rectangle(128, 344, 156, 10);
		rectMoney = new Rectangle(128, 365, 122, 10);
		rectPercent = new Rectangle(259, 365, 25, 10);
		
		rectLessMoney = new Rectangle(9, 361, 28, 19);
		rectMoreMoney = new Rectangle(38, 361, 28, 19);
		rectMoneyAdjust = new Rectangle(9, 361, 57, 19);
		
		rectDescription = new Rectangle(9, 393, 515, 41);
		
		rectStart = new Rectangle(534, 283, 102, 39);
		rectEquipment = new Rectangle(534, 322, 102, 39);
		rectProduction = new Rectangle(534, 361, 102, 39);
		rectBridge = new Rectangle(534, 400, 102, 39);
		rectBridgeFix = new Rectangle(534, 437, 102, 2);
		btnBridgeFix = res.getSubimage(534, 398, 102, 2);
		
		rectView = new Rectangle(291, 360, 115, 23);
		rectStop = new Rectangle(410, 360, 115, 23);
		
		rectCompleted = new Rectangle(314, 290, 52, 14);
		rectTimeRemaining = new Rectangle(471, 290, 51, 14);
		
		rectCiv = new Rectangle(319, 310, 12, 15);
		rectMech = new Rectangle(373, 310, 12, 15);
		rectComp = new Rectangle(430, 310, 12, 15);
		rectAI = new Rectangle(471, 310, 12, 15);
		rectMil = new Rectangle(512, 310, 12, 15);

		rectCurrCiv = new Rectangle(319, 341, 12, 15);
		rectCurrMech = new Rectangle(373, 341, 12, 15);
		rectCurrComp = new Rectangle(430, 341, 12, 15);
		rectCurrAI = new Rectangle(471, 341, 12, 15);
		rectCurrMil = new Rectangle(512, 341, 12, 15);
		
		rectNeeded = new Rectangle[3];
		for (int i = 0; i < rectNeeded.length; i++) {
			rectNeeded[i] = new Rectangle(539, 139 + i * 18, 92, 10);
		}
		rectMainOptions = new Rectangle[4];
		rectMainArrow = new Rectangle[4];
		for (int i = 0; i < rectMainOptions.length; i++) {
			rectMainOptions[i] = new Rectangle(341, 10 + i * 22, 168, 20);
			rectMainArrow[i] = new Rectangle(325, 13 + i * 22, 15, 11);
		}
		rectSubOptions = new Rectangle[5];
		rectSubArrow = new Rectangle[5];
		for (int i = 0; i < rectSubOptions.length; i++) {
			rectSubOptions[i] = new Rectangle(342, 112 + i * 16, 168, 14);
			rectSubArrow[i] = new Rectangle(325, 112 + i * 16, 15, 11);
		}
	}
}
