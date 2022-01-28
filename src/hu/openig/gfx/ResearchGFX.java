/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.gfx;

import hu.openig.core.Anim;
import hu.openig.core.Btn2;
import hu.openig.core.Btn3H;
import hu.openig.core.Cat;
import hu.openig.core.Img;
import hu.openig.model.ResourceLocator;

import java.awt.image.BufferedImage;

/**
 * Images for the research and production screens.
 * @author akarnokd, 2009.11.09.
 */
public class ResearchGFX {
    /** Empty large. */
    @Img(name = "research/button_empty_elevated_large")
    public BufferedImage emptyElevated;
    /** Empty 3x large. */
    @Img(name = "research/button_empty_elevated_3x_large")
    public BufferedImage emptyElevated2;
    /** Empty button small. */
    @Img(name = "research/button_empty_small")
    public BufferedImage emptySmall;
    /** Fund button. */
    @Btn2(name = "research/button_fund")
    public BufferedImage[] fund;
    /** Minus one button. */
    @Btn2(name = "research/button_minus_one")
    public BufferedImage[] minusOne;
    /** Plus one button. */
    @Btn2(name = "research/button_plus_one")
    public BufferedImage[] plusOne;
    /** Minus ten button. */
    @Btn2(name = "research/button_minus_ten")
    public BufferedImage[] minusTen;
    /** Plus ten button. */
    @Btn2(name = "research/button_plus_ten")
    public BufferedImage[] plusTen;
    /** Current research arrow. */
    @Img(name = "research/current_research_arrow")
    public BufferedImage current;
    /** Start button. */
    @Btn2(name = "research/button_start")
    public BufferedImage[] start;
    /** Stop button. */
    @Btn2(name = "research/button_stop")
    public BufferedImage[] stop;
    /** Current research panel. (The one with the stop and view buttons.) */
    @Img(name = "research/panel_active_research")
    public BufferedImage activeResearchPanel;
    /** Main class panel. */
    @Img(name = "research/panel_main_class")
    public BufferedImage mainClassPanel;
    /** Requirements panel. */
    @Img(name = "research/panel_requirements")
    public BufferedImage requirementsPanel;
    /** Research info panel. */
    @Img(name = "research/panel_research_info")
    public BufferedImage researchInfoPanel;
    /** Selected research panel. */
    @Img(name = "research/panel_selected_research")
    public BufferedImage selectedResearchPanel;
    /** Subtype panel. */
    @Img(name = "research/panel_subtype")
    public BufferedImage subtypePanel;
    /** Subtype wide panel. */
    @Img(name = "research/panel_subtype_wide")
    public BufferedImage subtypeWidePanel;
    /** Production base panel. */
    @Img(name = "research/production_base")
    public BufferedImage productionBasePanel;
    /** The common production/research base panel. */
    @Img(name = "research/research_base")
    public BufferedImage basePanel;
    /** Production line. */
    @Img(name = "research/production_line")
    public BufferedImage productionLine;
    /** Missing research lab mark. */
    @Img(name = "research/research_missing_lab")
    public BufferedImage researchMissingLab;
    /** Missing prerequisite mark. */
    @Img(name = "research/research_missing_prerequisite")
    public BufferedImage researchMissingPrerequisite;
    /** The rolling CD icon. */
    @Anim(name = "research/research_rolling", width = 19)
    public BufferedImage[] rolling;
    /** The small rolling CD icon. */
    @Anim(name = "research/research_rolling_small", width = 19)
    public BufferedImage[] rollingSmall;
    /** Unavailable. */
    @Img(name = "research/unavailable")
    public BufferedImage unavailable;
    /** Add research/production button. */
    @Btn2(name = "research/button_add")
    public BufferedImage[] add;
    /** Go to research button. */
    @Btn2(name = "research/button_research")
    public BufferedImage[] research;
    /** Go to production button. */
    @Btn2(name = "research/button_production")
    public BufferedImage[] production;
    /** Go to equipment button. */
    @Btn2(name = "research/button_equipment")
    public BufferedImage[] equipmentButton;
    /** Remove production. */
    @Btn2(name = "research/button_remove")
    public BufferedImage[] remove;
    /** Sell equipment button. */
    @Btn2(name = "research/button_sell")
    public BufferedImage[] sell;
    /** View current research button. */
    @Btn2(name = "research/button_view")
    public BufferedImage[] view;
    /** Category: battleships. */
    @Cat(name = "research/label_battleships")
    public BufferedImage[] battleships;
    /** Category: buildings. */
    @Cat(name = "research/label_buildings")
    public BufferedImage[] buildings;
    /** The cannons. */
    @Cat(name = "research/label_cannons")
    public BufferedImage[] cannons;
    /** The production line capacity. */
    @Img(name = "research/label_cap")
    public BufferedImage cap;
    /** The production line capacity percent. */
    @Img(name = "research/label_cap_percent")
    public BufferedImage capPercent;
    /** The total capacity label. */
    @Img(name = "research/label_capacity")
    public BufferedImage capacity;
    /** Civil buildings category. */
    @Cat(name = "research/label_civil_buildings")
    public BufferedImage[] civilBuildings;
    /** Civil Engineering Laboratory. */
    @Img(name = "research/label_civil_lab")
    public BufferedImage civilLab;
    /** Computer Laboratory. */
    @Img(name = "research/label_comp_lab")
    public BufferedImage compLab;
    /** Production completed percent. */
    @Img(name = "research/label_completed")
    public BufferedImage completed;
    /** Cruisers category. */
    @Cat(name = "research/label_cruisers")
    public BufferedImage[] cruisers;
    /** Equipment category. */
    @Cat(name = "research/label_equipment")
    public BufferedImage[] equipment;
    /** Fighters category. */
    @Cat(name = "research/label_fighters")
    public BufferedImage[] fighters;
    /** Hyperdrives category. */
    @Cat(name = "research/label_hyperdrives")
    public BufferedImage[] hyperdrives;
    /** Production importance. */
    @Img(name = "research/label_importance")
    public BufferedImage importance;
    /** Invention name. */
    @Img(name = "research/label_invention_name")
    public BufferedImage inventionName;
    /** Lasers category. */
    @Cat(name = "research/label_lasers")
    public BufferedImage[] lasers;
    /** Mechanical Laboratory. */
    @Img(name = "research/label_mech_lab")
    public BufferedImage mechLab;
    /** Military Laboratory. */
    @Img(name = "research/label_mil_lab")
    public BufferedImage milLab;
    /** Military buildings category. */
    @Cat(name = "research/label_military_buildings")
    public BufferedImage[] militaryBuildings;
    /** Modules category. */
    @Cat(name = "research/label_modules")
    public BufferedImage[] modules;
    /** Money. */
    @Img(name = "research/label_money")
    public BufferedImage money;
    /** Pieces. */
    @Img(name = "research/label_pieces")
    public BufferedImage pieces;
    /** Planetary guns category. */
    @Cat(name = "research/label_planetary_guns")
    public BufferedImage[] planetaryGuns;
    /** Price. */
    @Img(name = "research/label_price")
    public BufferedImage price;
    /** Project label. */
    @Img(name = "research/label_project")
    public BufferedImage project;
    /** Project completed. */
    @Img(name = "research/label_project_completed")
    public BufferedImage projectCompleted;
    /** Project name. */
    @Img(name = "research/label_project_name")
    public BufferedImage projectName;
    /** Project status. */
    @Img(name = "research/label_project_status")
    public BufferedImage projectStatus;
    /** Projectiles category. */
    @Cat(name = "research/label_projectiles")
    public BufferedImage[] projectiles;
    /** Radar buildings category. */
    @Cat(name = "research/label_radar_buildings")
    public BufferedImage[] radarBuildings;
    /** Radars category. */
    @Cat(name = "research/label_radars")
    public BufferedImage[] radars;
    /** Requirements category. */
    @Img(name = "research/label_requirements")
    public BufferedImage requirements;
    /** Satellites category. */
    @Cat(name = "research/label_satellites")
    public BufferedImage[] satellites;
    /** Shields category. */
    @Cat(name = "research/label_shields")
    public BufferedImage[] shields;
    /** Space stations category. */
    @Cat(name = "research/label_space_stations")
    public BufferedImage[] spaceStations;
    /** Spaceships category. */
    @Cat(name = "research/label_spaceships")
    public BufferedImage[] spaceships;
    /** Tanks category. */
    @Cat(name = "research/label_tanks")
    public BufferedImage[] tanks;
    /** Remaining. */
    @Img(name = "research/label_time_remaining")
    public BufferedImage remaining;
    /** Vehicles category. */
    @Cat(name = "research/label_vehicles")
    public BufferedImage[] vehicles;
    /** Weapons category. */
    @Cat(name = "research/label_weapons")
    public BufferedImage[] weapons;
    /** Artifical intelligence research label. */
    @Img(name = "research/label_ai_lab")
    public BufferedImage aiLab;
    /** The production less button. */
    @Btn3H(name = "research/button_less")
    public BufferedImage[] less;
    /** The production more button. */
    @Btn3H(name = "research/button_more")
    public BufferedImage[] more;
    /** The production 10 less button. */
    @Btn3H(name = "research/button_ten_less")
    public BufferedImage[] tenLess;
    /** The production 10 more button. */
    @Btn3H(name = "research/button_ten_more")
    public BufferedImage[] tenMore;
    /** Remove a production line. */
    @Btn3H(name = "research/button_remove_x")
    public BufferedImage[] removeX;
    /**
     * Load resources.
     * @param rl the resource locator
     * @return this;
     */
    public ResearchGFX load(ResourceLocator rl) {
        GFXLoader.loadResources(this, rl);
        return this;
    }
}
