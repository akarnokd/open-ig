/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.res.gfx;

import hu.openig.utils.ImageUtils;
import hu.openig.utils.JavaUtils;
import hu.openig.utils.PACFile;
import hu.openig.utils.PCXImage;
import hu.openig.utils.ResourceMapper;
import hu.openig.utils.PACFile.PACEntry;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;

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
	/** Spaceships tab light image. */
	public final BufferedImage tabSpaceshipsLight;
	/** Equipment tab light image. */
	public final BufferedImage tabEquipmentLight;
	/** Weapons tab light image. */
	public final BufferedImage tabWeaponsLight;
	/** Buildings tab light image. */
	public final BufferedImage tabBuildingsLight;
	// research screen main regions relative
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
	/** The main options images. */
	public BufferedImage[] mainOptions;
	/** The main options light images. */
	public BufferedImage[] mainOptionsLight;
	/** The sub options light array. */
	public BufferedImage[][] subOptions;
	/** The sub options light image array. */
	public BufferedImage[][] subOptionsLight;
	/** Fix for the bridge button's rendering anomaly. */
	public Rectangle rectBridgeFix;
	/** The small research images. */
	public final Map<Integer, BufferedImage> smallImages = JavaUtils.newHashMap();
	/** The empty animation. */
	public final File emptyAnimation;
	/**
	 * Constructor. Loads the images.
	 * @param resMap the resource mapper
	 */
	public ResearchGFX(ResourceMapper resMap) {
		researchScreen = PCXImage.from(resMap.get("SCREENS/FEJLESZT.PCX"), -1);
		
		BufferedImage res = PCXImage.from(resMap.get("SCREENS/FEJL_X.PCX"), -1);
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
		
		tabSpaceshipsLight = ImageUtils.subimage(res, 438, 0, 111, 21);
		tabEquipmentLight = ImageUtils.subimage(res, 438, 22, 111, 21);
		tabWeaponsLight = ImageUtils.subimage(res, 438, 44, 111, 21);
		tabBuildingsLight = ImageUtils.subimage(res, 438, 65, 111, 18);
		
		tabSpaceships = ImageUtils.subimage(res, 438, 83, 111, 21);
		tabEquipment = ImageUtils.subimage(res, 438, 105, 111, 21);
		tabWeapons = ImageUtils.subimage(res, 438, 127, 111, 21);
		tabBuildings = ImageUtils.subimage(res, 438, 149, 111, 17);
		
		mainOptions = new BufferedImage[] {
			tabSpaceships, tabEquipment, tabWeapons, tabBuildings
		};
		mainOptionsLight = new BufferedImage[] {
			tabSpaceshipsLight, tabEquipmentLight, tabWeaponsLight, tabBuildingsLight
		};
	
//		optSpaceships = ImageUtils.subimage(res, 0, 0, 168, 78);
//		optEquipment = ImageUtils.subimage(res, 0, 78, 168, 78);
//		optWeapons = ImageUtils.subimage(res, 0, 156, 168, 78);
//		optBuildings = ImageUtils.subimage(res, 0, 234, 168, 78);
		subOptions = new BufferedImage[4][];
		subOptionsLight = new BufferedImage[4][];
		// spaceships
		subOptions[0] = new BufferedImage[] {
				ImageUtils.subimage(res, 0, 0, 168, 14),
				ImageUtils.subimage(res, 0, 16, 168, 14),
				ImageUtils.subimage(res, 0, 32, 168, 14),
				ImageUtils.subimage(res, 0, 48, 168, 14),
				ImageUtils.subimage(res, 0, 64, 168, 14)
			};
		subOptionsLight[0] = new BufferedImage[] {
			ImageUtils.subimage(res, 168, 0, 168, 14),
			ImageUtils.subimage(res, 168, 16, 168, 14),
			ImageUtils.subimage(res, 168, 32, 168, 14),
			ImageUtils.subimage(res, 168, 48, 168, 14),
			ImageUtils.subimage(res, 168, 64, 168, 14)
		};
		// equipments
		subOptions[1] = new BufferedImage[] {
			ImageUtils.subimage(res, 0, 78, 168, 14),
			ImageUtils.subimage(res, 0, 94, 168, 14),
			ImageUtils.subimage(res, 0, 110, 168, 14),
			ImageUtils.subimage(res, 0, 126, 168, 14)
		};
		subOptionsLight[1] = new BufferedImage[] {
			ImageUtils.subimage(res, 168, 78, 168, 14),
			ImageUtils.subimage(res, 168, 94, 168, 14),
			ImageUtils.subimage(res, 168, 110, 168, 14),
			ImageUtils.subimage(res, 168, 126, 168, 14)
		};
		// weapons
		subOptions[2] = new BufferedImage[] {
				ImageUtils.subimage(res, 0, 156, 168, 14), // lasers
				ImageUtils.subimage(res, 0, 172, 168, 14), // guns
				ImageUtils.subimage(res, 0, 188, 168, 14), // bombs and missiles
				ImageUtils.subimage(res, 0, 204, 168, 14), // tanks
				ImageUtils.subimage(res, 0, 220, 168, 14)	 // vehicles	
		};
		subOptionsLight[2] = new BufferedImage[] {
			ImageUtils.subimage(res, 168, 156, 168, 14), // lasers
			ImageUtils.subimage(res, 168, 172, 168, 14), // guns
			ImageUtils.subimage(res, 168, 188, 168, 14), // bombs and missiles
			ImageUtils.subimage(res, 168, 204, 168, 14), // tanks
			ImageUtils.subimage(res, 168, 220, 168, 14)	 // vehicles	
		};
		subOptions[3] = new BufferedImage[] {
			ImageUtils.subimage(res, 0, 234, 168, 14),
			ImageUtils.subimage(res, 0, 250, 168, 14),
			ImageUtils.subimage(res, 0, 266, 168, 14),
			ImageUtils.subimage(res, 0, 282, 168, 14)
		};
		subOptionsLight[3] = new BufferedImage[] {
			ImageUtils.subimage(res, 168, 234, 167, 14),
			ImageUtils.subimage(res, 168, 250, 167, 14),
			ImageUtils.subimage(res, 168, 266, 167, 14),
			ImageUtils.subimage(res, 168, 282, 167, 14)
		};
		
		// various locations on the research screen
		
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
//		btnBridgeFix = ImageUtils.subimage(res, 534, 398, 102, 2);
		
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
		for (PACEntry e : PACFile.parseFully(resMap.get("DATA/EQ_PICS.PAC"))) {
			if (e.filename.startsWith("KIS")) {
				int idx = Integer.parseInt(e.filename.substring(3, 5)) * 10;
				BufferedImage img = PCXImage.parse(e.data, -1);
				for (int i = 0; i < 6; i++) {
					BufferedImage sub = ImageUtils.subimage(img, i * 106, 0, 104, 78);
					smallImages.put(idx + i + 1, sub);
				}
			}
		}
		emptyAnimation = resMap.get("EQ_ANIMS/INV000.ANI");
	}
}
