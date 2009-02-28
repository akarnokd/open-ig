/*
 * Copyright 2008, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.gfx;

import hu.openig.utils.ImageUtils;
import hu.openig.utils.PCXImage;

import java.awt.image.BufferedImage;


/**
 * Contains images for rendering the information screen.
 * @author karnokd, 2009.01.18.
 * @version $Revision 1.0$
 */
public class InformationGFX {
	/** The entire info screen. */
	public BufferedImage infoScreen;
	
	public BufferedImage btnColonyLarge;
	public BufferedImage btnColonyLargeDown;
	public BufferedImage btnStarmapLarge;
	public BufferedImage btnStarmapLargeDown;
	public BufferedImage btnEquipmentLarge;
	public BufferedImage btnEquipmentLargeDown;
	public BufferedImage btnResearchLarge;
	public BufferedImage btnResearchLargeDown;
	public BufferedImage btnProductionLarge;
	public BufferedImage btnProductionLargeDown;
	public BufferedImage btnDiplomacyLarge;
	public BufferedImage btnDiplomacyLargeDown;
	public BufferedImage btnEmptyLarge;
	
	public BufferedImage btnColonyInfo;
	public BufferedImage btnColonyInfoLightDown;
	public BufferedImage btnColonyInfoLight;
	public BufferedImage btnMilitaryInfo;
	public BufferedImage btnMilitaryInfoLightDown;
	public BufferedImage btnMilitaryInfoLight;
	public BufferedImage btnFinancialInfo;
	public BufferedImage btnFinancialInfoLightDown;
	public BufferedImage btnFinancialInfoLight;
	public BufferedImage btnBuildings;
	public BufferedImage btnBuildingsLightDown;
	public BufferedImage btnBuildingsLight;
	public BufferedImage btnPlanets;
	public BufferedImage btnPlanetsLightDown;
	public BufferedImage btnPlanetsLight;
	public BufferedImage btnFleets;
	public BufferedImage btnFleetsLightDown;
	public BufferedImage btnFleetsLight;
	public BufferedImage btnInventions;
	public BufferedImage btnInventionsLightDown;
	public BufferedImage btnInventionsLight;
	public BufferedImage btnAliens;
	public BufferedImage btnAliensLightDown;
	public BufferedImage btnAliensLight;
	public BufferedImage btnEmpty;
	
	public BufferedImage btnTaxMore;
	public BufferedImage btnTaxMoreDown;
	public BufferedImage btnTaxLess;
	public BufferedImage btnTaxLessDown;
	/**
	 * Constructor. Loads all graphics necessary for information screen rendering.
	 * @param root the root directory of the IG files.
	 */
	public InformationGFX(String root) {
		// entire background
		BufferedImage info = PCXImage.from(root + "/SCREENS/INFO.PCX", -1);
		// fix unnecessary right sidebar
		infoScreen = info.getSubimage(0, 0, 620, 420);
		// button states
		BufferedImage infox = PCXImage.from(root + "/SCREENS/INFO_X.PCX", -1);
		
		
		
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
