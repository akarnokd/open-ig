/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.xold.res.gfx;

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
