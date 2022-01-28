/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.model;

import hu.openig.core.Location;

import java.util.HashMap;
import java.util.Map;

/** The original building definition (from v0.72 and before). */
public class OriginalBuilding {
    /** The building name. */
    public String name;
    /** The location. */
    public Location location;
    /** The translation map. */
    public static final Map<String, String> TRANSLATE = new HashMap<>();
    static {
        TRANSLATE.put("ColonyHub", "ColonyHub");
        TRANSLATE.put("PrefabHousing", "PrefabHousing");
        TRANSLATE.put("ApartmentBlock", "ApartmentBlock");
        TRANSLATE.put("Arcology", "Arcology");
        TRANSLATE.put("NuclearPlant", "NuclearPlant");

        TRANSLATE.put("FusionPlant", "FusionPlant");
        TRANSLATE.put("SolarPlant", "SolarPlant");
        TRANSLATE.put("RadarTelescope", "RadarTelescope");
        TRANSLATE.put("Church", "Church");
        TRANSLATE.put("MilitarySpaceport", "MilitarySpaceport");

        TRANSLATE.put("MilitaryDevCentre", "MilitaryDevCenter");
        TRANSLATE.put("PoliceStation", "PoliceStation");
        TRANSLATE.put("FireBrigade", "FireBrigade");
        TRANSLATE.put("PhoodFactory", "PhoodFactory");
        TRANSLATE.put("HyperShield", "HyperShield");

        TRANSLATE.put("Hospital", "Hospital");
        TRANSLATE.put("AIDevCentre", "AIDevCenter");
        TRANSLATE.put("Fortress", "Fortress");
        TRANSLATE.put("MesonProjector", "MesonProjector");
        TRANSLATE.put("WeaponFactory", "WeaponFactory");

        TRANSLATE.put("TradeCentre", "TradeCenter");
        TRANSLATE.put("PhasedTelescope", "PhasedTelescope");
        TRANSLATE.put("SpaceshipFactory", "SpaceshipFactory");
        TRANSLATE.put("FieldTelescope", "FieldTelescope");
        TRANSLATE.put("EquipmentFactory", "EquipmentFactory");

        TRANSLATE.put("FusionProjector", "FusionProjector");
        TRANSLATE.put("PlasmaProjector", "PlasmaProjector");
        TRANSLATE.put("MechanicsDevCentre", "MechanicalDevCenter");
        TRANSLATE.put("Bunker", "Bunker");
        TRANSLATE.put("CivilEngDevCentre", "CivilDevCenter");

        TRANSLATE.put("InversionShield", "InversionShield");
        TRANSLATE.put("IonProjector", "IonProjector");
        TRANSLATE.put("ComputerDevCentre", "ComputerDevCenter");
        TRANSLATE.put("Stadium", "Stadium");
        TRANSLATE.put("Bar", "Bar");

        TRANSLATE.put("Bank", "Bank");
        TRANSLATE.put("TradersSpaceport", "TradersSpaceport");
        TRANSLATE.put("HydroponicFoodFarm", "HydroponicFoodFarm");
        TRANSLATE.put("Barracks", "Barracks");
        TRANSLATE.put("RecreationCentre", "RecreationCenter");

        TRANSLATE.put("Park", "Park");
        TRANSLATE.put("Stronghold", "Stronghold");
    }
    /** @return the new name from the old name. */
    public String getName() {
        return TRANSLATE.get(name);
    }
}
