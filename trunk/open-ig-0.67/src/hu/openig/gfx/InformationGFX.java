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

import java.awt.image.BufferedImage;


/**
 * Contains images for rendering the information screen.
 * @author karnokd, 2009.01.18.
 * @version $Revision 1.0$
 */
public class InformationGFX {
	/** The entire info screen. */
	public final BufferedImage infoScreen;
	/** Large colony button image. */
	public final BufferedImage btnColonyLarge;
	/** Colony large down button image. */
	public final BufferedImage btnColonyLargeDown;
	/** Starmap large button image. */
	public final BufferedImage btnStarmapLarge;
	/** Starmap large down button image. */
	public final BufferedImage btnStarmapLargeDown;
	/** Equipment large button image. */
	public final BufferedImage btnEquipmentLarge;
	/** Equipment large down button image. */
	public final BufferedImage btnEquipmentLargeDown;
	/** Research large button image. */
	public final BufferedImage btnResearchLarge;
	/** Research large down button image. */
	public final BufferedImage btnResearchLargeDown;
	/** Production large button image. */
	public final BufferedImage btnProductionLarge;
	/** Production large down button image. */
	public final BufferedImage btnProductionLargeDown;
	/** Diplomacy large button image. */
	public final BufferedImage btnDiplomacyLarge;
	/** Diplomacy large down button image. */
	public final BufferedImage btnDiplomacyLargeDown;
	/** Empty large buttom image. */
	public final BufferedImage btnEmptyLarge;
	/** Colony info image. */
	public final BufferedImage btnColonyInfo;
	/** Colony info light down button image. */
	public final BufferedImage btnColonyInfoLightDown;
	/** Colony info light button image. */
	public final BufferedImage btnColonyInfoLight;
	/** Military info button image. */
	public final BufferedImage btnMilitaryInfo;
	/** Military info light down button image. */
	public final BufferedImage btnMilitaryInfoLightDown;
	/** Military info light button image. */
	public final BufferedImage btnMilitaryInfoLight;
	/** Financial info button image. */
	public final BufferedImage btnFinancialInfo;
	/** Financial info light down button image. */
	public final BufferedImage btnFinancialInfoLightDown;
	/** Financial info light button image. */
	public final BufferedImage btnFinancialInfoLight;
	/** Buildings button image. */
	public final BufferedImage btnBuildings;
	/** Buildings light down button image. */
	public final BufferedImage btnBuildingsLightDown;
	/** Buildings light button image. */
	public final BufferedImage btnBuildingsLight;
	/** Planets button image. */
	public final BufferedImage btnPlanets;
	/** Planets light down button image. */
	public final BufferedImage btnPlanetsLightDown;
	/** Planets light button image. */
	public final BufferedImage btnPlanetsLight;
	/** Fleets button image. */
	public final BufferedImage btnFleets;
	/** Fleets light down button image. */
	public final BufferedImage btnFleetsLightDown;
	/** Fleets light button image. */
	public final BufferedImage btnFleetsLight;
	/** Inventions button image. */
	public final BufferedImage btnInventions;
	/** Inventions light down button image. */
	public final BufferedImage btnInventionsLightDown;
	/** Inventions light button image. */
	public final BufferedImage btnInventionsLight;
	/** Aliens button image. */
	public final BufferedImage btnAliens;
	/** Aliens light down button image. */
	public final BufferedImage btnAliensLightDown;
	/** Aliens light button image. */
	public final BufferedImage btnAliensLight;
	/** Empty button image. */
	public final BufferedImage btnEmpty;
	/** Tax more button image. */
	public final BufferedImage btnTaxMore;
	/** Tax more down button image. */
	public final BufferedImage btnTaxMoreDown;
	/** Tax les button image. */
	public final BufferedImage btnTaxLess;
	/** Tax less down button image. */
	public final BufferedImage btnTaxLessDown;
	/**
	 * Constructor. Loads all graphics necessary for information screen rendering.
	 * @param resMap the respource mapper
	 */
	public InformationGFX(ResourceMapper resMap) {
		// entire background
		BufferedImage info = PCXImage.from(resMap.get("SCREENS/INFO.PCX"), -1);
		// fix unnecessary right sidebar
		infoScreen = info.getSubimage(0, 0, 620, 420);
		// button states
		BufferedImage infox = PCXImage.from(resMap.get("SCREENS/INFO_X.PCX"), -1);
		
		
		
		btnColonyLarge = ImageUtils.subimage(infox, 0, 0, 102, 39);
		btnColonyLargeDown = ImageUtils.subimage(infox, 0, 39, 102, 39);
		
		btnStarmapLarge = ImageUtils.subimage(infox, 102, 0, 102, 39);
		btnStarmapLargeDown = ImageUtils.subimage(infox, 102, 39, 102, 39);

		btnEquipmentLarge = ImageUtils.subimage(infox, 204, 0, 102, 39);
		btnEquipmentLargeDown = ImageUtils.subimage(infox, 204, 39, 102, 39);

		btnResearchLarge = ImageUtils.subimage(infox, 306, 0, 102, 39);
		btnResearchLargeDown = ImageUtils.subimage(infox, 306, 39, 102, 39);

		btnProductionLarge = ImageUtils.subimage(infox, 408, 0, 102, 39);
		btnProductionLargeDown = ImageUtils.subimage(infox, 408, 39, 102, 39);

		btnDiplomacyLarge = ImageUtils.subimage(infox, 510, 0, 102, 39);
		btnDiplomacyLargeDown = ImageUtils.subimage(infox, 510, 39, 102, 39);
		
		btnEmptyLarge = ImageUtils.subimage(infox, 0, 190, 102, 39);
		// small buttons
		
		btnColonyInfo = ImageUtils.subimage(infox, 0, 78, 102, 28);
		btnColonyInfoLightDown = ImageUtils.subimage(infox, 0, 106, 102, 28);
		btnColonyInfoLight = ImageUtils.subimage(infox, 0, 134, 102, 28);
		
		btnMilitaryInfo = ImageUtils.subimage(infox, 102, 78, 102, 28);
		btnMilitaryInfoLightDown = ImageUtils.subimage(infox, 102, 106, 102, 28);
		btnMilitaryInfoLight = ImageUtils.subimage(infox, 102, 134, 102, 28);
		
		btnFinancialInfo = ImageUtils.subimage(infox, 204, 78, 102, 28);
		btnFinancialInfoLightDown = ImageUtils.subimage(infox, 204, 106, 102, 28);
		btnFinancialInfoLight = ImageUtils.subimage(infox, 204, 134, 102, 28);
		
		btnBuildings = ImageUtils.subimage(infox, 306, 78, 102, 28);
		btnBuildingsLightDown = ImageUtils.subimage(infox, 306, 106, 102, 28);
		btnBuildingsLight = ImageUtils.subimage(infox, 306, 134, 102, 28);
		
		btnPlanets = ImageUtils.subimage(infox, 408, 78, 102, 28);
		btnPlanetsLightDown = ImageUtils.subimage(infox, 408, 106, 102, 28);
		btnPlanetsLight = ImageUtils.subimage(infox, 408, 134, 102, 28);
		
		btnFleets = ImageUtils.subimage(infox, 510, 78, 102, 28);
		btnFleetsLightDown = ImageUtils.subimage(infox, 510, 106, 102, 28);
		btnFleetsLight = ImageUtils.subimage(infox, 510, 134, 102, 28);
		
		btnInventions = ImageUtils.subimage(infox, 0, 162, 102, 28);
		btnAliens = ImageUtils.subimage(infox, 102, 162, 102, 28);
		
		btnInventionsLightDown = ImageUtils.subimage(infox, 204, 162, 102, 28);
		btnAliensLightDown = ImageUtils.subimage(infox, 306, 162, 102, 28);
		
		btnInventionsLight = ImageUtils.subimage(infox, 408, 162, 102, 28);
		btnAliensLight = ImageUtils.subimage(infox, 510, 162, 102, 28);
		
		btnEmpty = ImageUtils.subimage(infox, 102, 190, 102, 28);
		
		btnTaxMore = ImageUtils.subimage(infox, 204, 190, 70, 30);
		btnTaxLess = ImageUtils.subimage(infox, 274, 190, 70, 30);
		btnTaxMoreDown = ImageUtils.subimage(infox, 344, 190, 70, 30);
		btnTaxLessDown = ImageUtils.subimage(infox, 414, 190, 70, 30);
	}
}
