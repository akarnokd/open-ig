/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.gfx;

import hu.openig.utils.ImageUtils;
import hu.openig.utils.PCXImage;
import hu.openig.utils.ResourceMapper;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Research screen graphics.
 * @author karnokd, 2009.02.08.
 * @version $Revision 1.0$
 */
public class ResearchGFX {
	/** The research base screen. */
	public final BufferedImage researchScreen;
	/** Empty button image. */
	public final BufferedImage btnEmpty;
	/** Start button image. */
	public final BufferedImage btnStart;
	/** Start down button image. */
	public final BufferedImage btnStartDown;
	/** Equipment down button image. */
	public final BufferedImage btnEquipmentDown;
	/** Product down button image. */
	public final BufferedImage btnProductDown;
	/** Bridge down button image. */
	public final BufferedImage btnBridgeDown;
	/** The current selection indicator arrow. */ 
	public final BufferedImage arrow;
	/** Money down button image. */
	public final BufferedImage btnMoneyDown;
	/** View down button image. */
	public final BufferedImage btnViewDown;
	/** The smaller empty button region. */
	public final BufferedImage btnEmptySmall;
	/** Stop down button image. */
	public final BufferedImage btnStopDown;
	/** Spaceships tab image. */
	public final BufferedImage tabSpaceships;
	/** Equipment tab image. */
	public final BufferedImage tabEquipment;
	/** Weapons tab image. */
	public final BufferedImage tabWeapons;
	/** Buildings tab image. */
	public final BufferedImage tabBuildings;
	/** Spaceship options image. */
	public final BufferedImage optSpaceships;
	/** Equipment options image. */
	public final BufferedImage optEquipment;
	/** Weapons options image. */
	public final BufferedImage optWeapons;
	/** Buildings options image. */
	public final BufferedImage optBuildings;
	/** Spaceships - Fighters suboptions. */
	public final BufferedImage optFighters;
	/** Destroyers options image. */
	public final BufferedImage optDestroyers;
	/** Flagships options image. */
	public final BufferedImage optFlagships;
	/** Satellites options image. */
	public final BufferedImage optSatellites;
	/** Space station options image. */
	public final BufferedImage optSpaceStations;
	/** Equipment - Hyperdrives. */
	public final BufferedImage optHyperdrives;
	/** Modules options image. */
	public final BufferedImage optModules;
	/** Radars options image. */
	public final BufferedImage optRadars;
	/** Shields options image. */
	public final BufferedImage optShields;
	/** Weapons - Laser weapons. */
	public final BufferedImage optLasers;
	/** Guns option image. */
	public final BufferedImage optGuns;
	/** Bombs missiles  options image. */
	public final BufferedImage optBombsMissiles;
	/** Tanks options image. */
	public final BufferedImage optTanks;
	/** Vehicles options image. */
	public final BufferedImage optVehicles;
	/** Buildings - Buildings. */
	public final BufferedImage optColonyBuildings;
	/** Military buildings image. */
	public final BufferedImage optMilitaryBuildings;
	/** Colony radars buildings image. */
	public final BufferedImage optColonyRadars;
	/** Colony guns image. */
	public final BufferedImage optColonyGuns;
	// research screen main regions relative
	/** Animation rectangle. */
	public Rectangle rectAnimation;
	/** Research rectangles. */
	public Rectangle[] rectResearch;
	/** Project name rectangle. */
	public Rectangle rectProjectName;
	/** Project status rectangle. */
	public Rectangle rectProjectStatus;
	/** Completed rectangle. */
	public Rectangle rectCompleted;
	/** Time remaining rectangle. */
	public Rectangle rectTimeRemaining;
	/** Number of civil enginering rectangle. */
	public Rectangle rectCiv;
	/** Number of mechanical research rectangle. */
	public Rectangle rectMech;
	/** Number of computer research rectangle. */
	public Rectangle rectComp;
	/** Number of AU research rectangle. */
	public Rectangle rectAI;
	/** Number of Military research rectangle. */
	public Rectangle rectMil;
	/** Start button location. */
	public Rectangle rectStart;
	/** Equipment rectangle. */
	public Rectangle rectEquipment;
	/** Production rectangle. */
	public Rectangle rectProduction;
	/** Bridge rectangle. */
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
	/** Current number of mechanical research rectangle. */
	public Rectangle rectCurrMech;
	/** Current number of computer research rectangle. */
	public Rectangle rectCurrComp;
	/** Current number of AI research rectangle. */
	public Rectangle rectCurrAI;
	/** Current number of military research rectangle. */
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
	 * @param resMap the resource mapper
	 */
	public ResearchGFX(ResourceMapper resMap) {
		researchScreen = PCXImage.from(resMap.get("SCREEN/FEJLESZT.PCX"), -1);
		
		BufferedImage res = PCXImage.from(resMap.get("SCREEN/FEJL_X.PCX"), -1);
		btnEmpty = ImageUtils.subimage(res, 336, 0, 102, 39);
		btnStart = ImageUtils.subimage(res, 336, 39, 102, 39);
		btnStartDown = ImageUtils.subimage(res, 336, 78, 102, 39);
		btnEquipmentDown = ImageUtils.subimage(res, 336, 117, 102, 39);
		// the original source is malformed
		btnProductDown = ImageUtils.subimage(res, 336, 156, 101, 39);
		btnBridgeDown = ImageUtils.subimage(res, 336, 195, 101, 39);
		arrow = ImageUtils.subimage(res, 568, 0, 15, 11);
		// fix arrow background to transparent
		for (int i = 0; i < arrow.getHeight(); i++) {
			for (int j = 0; j < arrow.getWidth(); j++) {
				int c = arrow.getRGB(j, i);
				if ((c & 0xFFFFFF) == 0) {
					arrow.setRGB(j, i, 0);
				}
			}
		}
		btnMoneyDown = ImageUtils.subimage(res, 549, 104, 57, 19);
		btnViewDown = ImageUtils.subimage(res, 437, 189, 115, 23);
		btnEmptySmall = ImageUtils.subimage(res, 437, 212, 115, 23);
		btnStopDown = ImageUtils.subimage(res, 437, 258, 115, 23);
		
		tabSpaceships = ImageUtils.subimage(res, 438, 0, 111, 21);
		tabEquipment = ImageUtils.subimage(res, 438, 22, 111, 21);
		tabWeapons = ImageUtils.subimage(res, 438, 44, 111, 21);
		tabBuildings = ImageUtils.subimage(res, 438, 65, 111, 18);
	
		optSpaceships = ImageUtils.subimage(res, 0, 0, 168, 78);
		optEquipment = ImageUtils.subimage(res, 0, 78, 168, 78);
		optWeapons = ImageUtils.subimage(res, 0, 156, 168, 78);
		optBuildings = ImageUtils.subimage(res, 0, 234, 168, 78);
		
		optFighters = ImageUtils.subimage(res, 168, 0, 168, 14);
		optDestroyers = ImageUtils.subimage(res, 168, 16, 168, 14);
		optFlagships = ImageUtils.subimage(res, 168, 32, 168, 14);
		optSatellites = ImageUtils.subimage(res, 168, 48, 168, 14);
		optSpaceStations = ImageUtils.subimage(res, 168, 64, 168, 14);
		
		optHyperdrives = ImageUtils.subimage(res, 168, 78, 168, 14);
		optModules = ImageUtils.subimage(res, 168, 94, 168, 14);
		optRadars = ImageUtils.subimage(res, 168, 110, 168, 14);
		optShields = ImageUtils.subimage(res, 168, 126, 168, 14);
		
		optLasers = ImageUtils.subimage(res, 168, 156, 168, 14);
		optGuns = ImageUtils.subimage(res, 168, 172, 168, 14);
		optBombsMissiles = ImageUtils.subimage(res, 168, 188, 168, 14);
		optTanks = ImageUtils.subimage(res, 168, 204, 168, 14);
		optVehicles = ImageUtils.subimage(res, 168, 220, 168, 14);
		
		optColonyBuildings = ImageUtils.subimage(res, 168, 234, 168, 14);
		optMilitaryBuildings = ImageUtils.subimage(res, 168, 250, 168, 14);
		optColonyRadars = ImageUtils.subimage(res, 168, 266, 168, 14);
		optColonyGuns = ImageUtils.subimage(res, 168, 282, 168, 14);
		
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
		btnBridgeFix = ImageUtils.subimage(res, 534, 398, 102, 2);
		
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
