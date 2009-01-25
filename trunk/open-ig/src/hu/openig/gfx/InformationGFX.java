/*
 * Copyright 2008, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.gfx;

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
		
		
		
		btnColonyLarge = infox.getSubimage(0, 0, 102, 39);
		btnColonyLargeDown = infox.getSubimage(0, 39, 102, 39);
		
		btnStarmapLarge = infox.getSubimage(102, 0, 102, 39);
		btnStarmapLargeDown = infox.getSubimage(102, 39, 102, 39);

		btnEquipmentLarge = infox.getSubimage(204, 0, 102, 39);
		btnEquipmentLargeDown = infox.getSubimage(204, 39, 102, 39);

		btnResearchLarge = infox.getSubimage(306, 0, 102, 39);
		btnResearchLargeDown = infox.getSubimage(306, 39, 102, 39);

		btnProductionLarge = infox.getSubimage(408, 0, 102, 39);
		btnProductionLargeDown = infox.getSubimage(408, 39, 102, 39);

		btnDiplomacyLarge = infox.getSubimage(510, 0, 102, 39);
		btnDiplomacyLargeDown = infox.getSubimage(510, 39, 102, 39);
		
		btnEmptyLarge = infox.getSubimage(0, 190, 102, 39);
		// small buttons
		
		btnColonyInfo = infox.getSubimage(0, 78, 102, 28);
		btnColonyInfoLightDown = infox.getSubimage(0, 106, 102, 28);
		btnColonyInfoLight = infox.getSubimage(0, 134, 102, 28);
		
		btnMilitaryInfo = infox.getSubimage(102, 78, 102, 28);
		btnMilitaryInfoLightDown = infox.getSubimage(102, 106, 102, 28);
		btnMilitaryInfoLight = infox.getSubimage(102, 134, 102, 28);
		
		btnFinancialInfo = infox.getSubimage(204, 78, 102, 28);
		btnFinancialInfoLightDown = infox.getSubimage(204, 106, 102, 28);
		btnFinancialInfoLight = infox.getSubimage(204, 134, 102, 28);
		
		btnBuildings = infox.getSubimage(306, 78, 102, 28);
		btnBuildingsLightDown = infox.getSubimage(306, 106, 102, 28);
		btnBuildingsLight = infox.getSubimage(306, 134, 102, 28);
		
		btnPlanets = infox.getSubimage(408, 78, 102, 28);
		btnPlanetsLightDown = infox.getSubimage(408, 106, 102, 28);
		btnPlanetsLight = infox.getSubimage(408, 134, 102, 28);
		
		btnFleets = infox.getSubimage(510, 78, 102, 28);
		btnFleetsLightDown = infox.getSubimage(510, 106, 102, 28);
		btnFleetsLight = infox.getSubimage(510, 134, 102, 28);
		
		btnInventions = infox.getSubimage(0, 162, 102, 28);
		btnAliens = infox.getSubimage(102, 162, 102, 28);
		
		btnInventionsLightDown = infox.getSubimage(204, 162, 102, 28);
		btnAliensLightDown = infox.getSubimage(306, 162, 102, 28);
		
		btnInventionsLight = infox.getSubimage(408, 162, 102, 28);
		btnAliensLight = infox.getSubimage(510, 162, 102, 28);
		
		btnEmpty = infox.getSubimage(102, 190, 102, 28);
		
		btnTaxMore = infox.getSubimage(204, 190, 70, 30);
		btnTaxLess = infox.getSubimage(274, 190, 70, 30);
		btnTaxMoreDown = infox.getSubimage(344, 190, 70, 30);
		btnTaxLessDown = infox.getSubimage(414, 190, 70, 30);
	}
}
