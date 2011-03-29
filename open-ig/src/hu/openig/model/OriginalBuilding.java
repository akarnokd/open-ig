/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.model;

import hu.openig.core.Location;

import java.util.HashMap;
import java.util.Map;

/** The origian building definition (from v0.72 and before). */
public class OriginalBuilding {
	/** The building name. */
	public String name;
	/** The location. */
	public Location location;
	/** @return the new name from the old name. */
	public String getName() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("ColonyHub", "ColonyHub");
		map.put("PrefabHousing", "PrefabHousing");
		map.put("ApartmentBlock", "ApartmentBlock");
		map.put("Arcology", "Arcology");
		map.put("NuclearPlant", "NuclearPlant");
		
		map.put("FusionPlant", "FusionPlant");
		map.put("SolarPlant", "SolarPlant");
		map.put("RadarTelescope", "RadarTelescope");
		map.put("Church", "Church");
		map.put("MilitarySpaceport", "MilitarySpaceport");
		
		map.put("MilitaryDevCentre", "MilitaryDevCenter");
		map.put("PoliceStation", "PoliceStation");
		map.put("FireBrigade", "FireBrigade");
		map.put("PhoodFactory", "PhoodFactory");
		map.put("HyperShield", "HyperShield");
		
		map.put("Hospital", "Hospital");
		map.put("AIDevCentre", "AIDevCenter");
		map.put("Fortress", "Fortress");
		map.put("MesonProjector", "MesonProjector");
		map.put("WeaponFactory", "WeaponFactory");
		
		map.put("TradeCentre", "TradeCenter");
		map.put("PhasedTelescope", "PhasedTelescope");
		map.put("SpaceshipFactory", "SpaceshipFactory");
		map.put("FieldTelescope", "FieldTelescope");
		map.put("EquipmentFactory", "EquipmentFactory");
		
		map.put("FusionProjector", "FusionProjector");
		map.put("PlasmaProjector", "PlasmaProjector");
		map.put("MechanicsDevCentre", "MechanicalDevCenter");
		map.put("Bunker", "Bunker");
		map.put("CivilEngDevCentre", "CivilDevCenter");
		
		map.put("InversionShield", "InversionShield");
		map.put("IonProjector", "IonProjector");
		map.put("ComputerDevCentre", "ComputerDevCenter");
		map.put("Stadium", "Stadium");
		map.put("Bar", "Bar");
		
		map.put("Bank", "Bank");
		map.put("TradersSpaceport", "TradersSpaceport");
		map.put("HydroponicFoodFarm", "HydroponicFoodFarm");
		map.put("Barracks", "Barracks");
		map.put("RecreationCentre", "RecreationCenter");
		
		map.put("Park", "Park");
		map.put("Stronghold", "Stronghold");
		
		return map.get(name);
	}
}
