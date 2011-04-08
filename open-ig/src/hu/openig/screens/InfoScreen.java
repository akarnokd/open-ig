/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screens;

import hu.openig.core.Act;
import hu.openig.core.Action1;
import hu.openig.core.Func1;
import hu.openig.model.BuildingType;
import hu.openig.model.Fleet;
import hu.openig.model.FleetKnowledge;
import hu.openig.model.Named;
import hu.openig.model.Owned;
import hu.openig.model.Planet;
import hu.openig.model.PlanetInventoryItem;
import hu.openig.model.PlanetKnowledge;
import hu.openig.model.PlanetProblems;
import hu.openig.model.PlanetStatistics;
import hu.openig.model.Research;
import hu.openig.model.ResearchMainCategory;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.ResearchType;
import hu.openig.model.TaxLevel;
import hu.openig.model.TileSet;
import hu.openig.render.RenderTools;
import hu.openig.render.TextRenderer;
import hu.openig.ui.HorizontalAlignment;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIContainer;
import hu.openig.ui.UIGenericButton;
import hu.openig.ui.UIImage;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UIImageTabButton2;
import hu.openig.ui.UILabel;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Modifier;
import hu.openig.ui.UIMouse.Type;
import hu.openig.ui.VerticalAlignment;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;



/**
 * The information screen renderer.
 * @author akarnokd, 2010.01.11.
 */
public class InfoScreen extends ScreenBase {
	/** 
	 * The annotation to indicate which UI elements should by default
	 * be visible on the screen. Mark only uncommon elements.
	 * @author akarnokd, Mar 19, 2011
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@interface ModeUI {
		/** The expected screen mode. */
		Screens[] mode();
	}
	/** The panel base rectangle. */
	final Rectangle base = new Rectangle();
	
	/** Tab button. */
	UIImageTabButton2 planetsTab;
	/** Tab button. */
	UIImageTabButton2 colonyTab;
	/** Tab button. */
	UIImageTabButton2 militaryTab;
	/** Tab button. */
	UIImageTabButton2 financialTab;
	/** Tab button. */
	UIImageTabButton2 fleetsTab;
	/** Tab button. */
	UIImageTabButton2 buildingsTab;
	/** Tab button. */
	UIImageTabButton2 inventionsTab;
	/** Tab button. */
	UIImageTabButton2 aliensTab;
	/** Empty inventions. */
	UIImage inventionsEmpty;
	/** Empty aliens. */
	UIImage aliensEmpty;
	
	/** Screen switch button. */
	@ModeUI(mode = { 
			Screens.INFORMATION_PLANETS, 
			Screens.INFORMATION_BUILDINGS, 
			Screens.INFORMATION_FINANCIAL, 
			Screens.INFORMATION_MILITARY, 
			Screens.INFORMATION_COLONY 
	})
	UIImageButton colony;
	/** Screen switch button. */
	@ModeUI(mode = { Screens.INFORMATION_INVENTIONS })
	UIImageButton research;
	/** Screen switch button. */
	@ModeUI(mode = { Screens.INFORMATION_INVENTIONS })
	UIImageButton production;
	/** Screen switch button. */
	@ModeUI(mode = { 
			Screens.INFORMATION_PLANETS, 
			Screens.INFORMATION_BUILDINGS, 
			Screens.INFORMATION_FINANCIAL, 
			Screens.INFORMATION_MILITARY, 
			Screens.INFORMATION_FLEETS, 
			Screens.INFORMATION_COLONY 
	})
	UIImageButton starmap;
	/** Screen switch button. */
	@ModeUI(mode = { Screens.INFORMATION_ALIENS })
	UIImageButton diplomacy;
	/** Screen switch button. */
	@ModeUI(mode = { Screens.INFORMATION_FLEETS })
	UIImageButton equipment;
	/** The screen switch button. */
	UIImage empty1;
	/** The empty switch button. */
	UIImage empty2;
	/** Colony info page. */
	@ModeUI(mode = { Screens.INFORMATION_COLONY })
	InfoPanel colonyInfo;
	/** The planet listing. */
	@ModeUI(mode = { Screens.INFORMATION_PLANETS })
	Listing<Planet> colonies;
	/** The fleet listing. */
	@ModeUI(mode = { Screens.INFORMATION_FLEETS })
	Listing<Fleet> fleets;
	/** The building listing. */
	@ModeUI(mode = { Screens.INFORMATION_BUILDINGS })
	BuildingListing buildings;
	/** The display mode. */
	Screens mode = Screens.INFORMATION_PLANETS;
	/** The minimap renderer. */
	@ModeUI(mode = { 
			Screens.INFORMATION_PLANETS, 
			Screens.INFORMATION_FINANCIAL, 
			Screens.INFORMATION_MILITARY, 
			Screens.INFORMATION_FLEETS, 
			Screens.INFORMATION_COLONY,
			Screens.INFORMATION_ALIENS
	})
	Minimap minimap;
	/** The building description title. */
	@ModeUI(mode = { Screens.INFORMATION_BUILDINGS, Screens.INFORMATION_INVENTIONS })
	UILabel descriptionTitle;
	/** The building description text. */
	@ModeUI(mode = { Screens.INFORMATION_BUILDINGS, Screens.INFORMATION_INVENTIONS })
	UILabel descriptionText;
	/** The building description text. */
	@ModeUI(mode = { Screens.INFORMATION_BUILDINGS, Screens.INFORMATION_INVENTIONS })
	UIImage descriptionImage;
	/** The title text to display. */
	@ModeUI(mode = { Screens.INFORMATION_BUILDINGS })
	UILabel buildingTitle;
	/** The planet name. */
	@ModeUI(mode = { 
			Screens.INFORMATION_PLANETS, 
			Screens.INFORMATION_COLONY, 
			Screens.INFORMATION_FINANCIAL, 
			Screens.INFORMATION_INVENTIONS, 
			Screens.INFORMATION_MILITARY 
		})
	UILabel planetTitle;
	/** Building cost label. */
	@ModeUI(mode = { Screens.INFORMATION_BUILDINGS })
	UILabel buildingCost;
	/** Building energy label. */
	@ModeUI(mode = { Screens.INFORMATION_BUILDINGS })
	UILabel buildingEnergy;
	/** Building worker label. */
	@ModeUI(mode = { Screens.INFORMATION_BUILDINGS })
	UILabel buildingWorker;
	/** The current planet. */
	@ModeUI(mode = { Screens.INFORMATION_BUILDINGS })
	UILabel buildingPlanet;
	/** The current planet's owner. */
	@ModeUI(mode = { Screens.INFORMATION_BUILDINGS })
	UILabel buildingPlanetOwner;
	/** The current planet's race. */
	@ModeUI(mode = { Screens.INFORMATION_BUILDINGS })
	UILabel buildingPlanetRace;
	/** The current planet's surface. */
	@ModeUI(mode = { Screens.INFORMATION_BUILDINGS })
	UILabel buildingPlanetSurface;
	/** Problem indicator icon. */
	@ModeUI(mode = { 
			Screens.INFORMATION_BUILDINGS,
			Screens.INFORMATION_PLANETS,
			Screens.INFORMATION_COLONY,
			Screens.INFORMATION_MILITARY,
			Screens.INFORMATION_FINANCIAL,
			Screens.INFORMATION_BUILDINGS,
	})
	UIImage problemsHouse;
	/** Problem indicator icon. */
	@ModeUI(mode = { 
			Screens.INFORMATION_BUILDINGS,
			Screens.INFORMATION_PLANETS,
			Screens.INFORMATION_COLONY,
			Screens.INFORMATION_MILITARY,
			Screens.INFORMATION_FINANCIAL,
			Screens.INFORMATION_BUILDINGS,
	})
	UIImage problemsEnergy;
	/** Problem indicator icon. */
	@ModeUI(mode = { 
			Screens.INFORMATION_BUILDINGS,
			Screens.INFORMATION_PLANETS,
			Screens.INFORMATION_COLONY,
			Screens.INFORMATION_MILITARY,
			Screens.INFORMATION_FINANCIAL,
			Screens.INFORMATION_BUILDINGS,
	})
	UIImage problemsFood;
	/** Problem indicator icon. */
	@ModeUI(mode = { 
			Screens.INFORMATION_BUILDINGS,
			Screens.INFORMATION_PLANETS,
			Screens.INFORMATION_COLONY,
			Screens.INFORMATION_MILITARY,
			Screens.INFORMATION_FINANCIAL,
			Screens.INFORMATION_BUILDINGS,
	})
	UIImage problemsHospital;
	/** Problem indicator icon. */
	@ModeUI(mode = { 
			Screens.INFORMATION_BUILDINGS,
			Screens.INFORMATION_PLANETS,
			Screens.INFORMATION_COLONY,
			Screens.INFORMATION_MILITARY,
			Screens.INFORMATION_FINANCIAL,
			Screens.INFORMATION_BUILDINGS,
	})
	UIImage problemsWorker;
	/** Problem indicator icon. */
	@ModeUI(mode = { 
			Screens.INFORMATION_BUILDINGS,
			Screens.INFORMATION_PLANETS,
			Screens.INFORMATION_COLONY,
			Screens.INFORMATION_MILITARY,
			Screens.INFORMATION_FINANCIAL,
			Screens.INFORMATION_BUILDINGS,
	})
	UIImage problemsVirus;
	/** Problem indicator icon. */
	@ModeUI(mode = { 
			Screens.INFORMATION_BUILDINGS,
			Screens.INFORMATION_PLANETS,
			Screens.INFORMATION_COLONY,
			Screens.INFORMATION_MILITARY,
			Screens.INFORMATION_FINANCIAL,
			Screens.INFORMATION_BUILDINGS,
	})
	UIImage problemsStadium;
	/** Problem indicator icon. */
	@ModeUI(mode = { 
			Screens.INFORMATION_BUILDINGS,
			Screens.INFORMATION_PLANETS,
			Screens.INFORMATION_COLONY,
			Screens.INFORMATION_MILITARY,
			Screens.INFORMATION_FINANCIAL,
			Screens.INFORMATION_BUILDINGS,
	})
	UIImage problemsRepair;
	/** The current planet's owner. */
	@ModeUI(mode = { 
			Screens.INFORMATION_MILITARY,
			Screens.INFORMATION_FINANCIAL,
			Screens.INFORMATION_COLONY, 
			Screens.INFORMATION_PLANETS 
			})
	UILabel colonyOwner;
	/** The current planet's race. */
	@ModeUI(mode = { 
			Screens.INFORMATION_MILITARY,
			Screens.INFORMATION_FINANCIAL,
			Screens.INFORMATION_COLONY, 
			Screens.INFORMATION_PLANETS 
			})
	UILabel colonyRace;
	/** The current planet's surface. */
	@ModeUI(mode = { 
			Screens.INFORMATION_MILITARY,
			Screens.INFORMATION_FINANCIAL,
			Screens.INFORMATION_COLONY, 
			Screens.INFORMATION_PLANETS 
			})
	UILabel colonySurface;
	/** The current planet's population. */
	@ModeUI(mode = { 
			Screens.INFORMATION_MILITARY,
			Screens.INFORMATION_FINANCIAL,
			Screens.INFORMATION_COLONY, 
			Screens.INFORMATION_PLANETS 
			})
	UILabel colonyPopulation;
	/** The current planet's taxation. */
	@ModeUI(mode = { 
			Screens.INFORMATION_MILITARY,
			Screens.INFORMATION_FINANCIAL,
			Screens.INFORMATION_COLONY, 
			Screens.INFORMATION_PLANETS 
			})
	UILabel colonyTax;
	/** Other things with the planet. */
	@ModeUI(mode = { 
			Screens.INFORMATION_MILITARY,
			Screens.INFORMATION_FINANCIAL,
			Screens.INFORMATION_COLONY, 
			Screens.INFORMATION_PLANETS 
			})
	UILabel colonyOther;
	/** The financial info panel. */
	@ModeUI(mode = { 
			Screens.INFORMATION_FINANCIAL
	})
	FinancialInfo financialInfo;
	/** The research info panel. */
	@ModeUI(mode = { 
			Screens.INFORMATION_INVENTIONS
	})
	ResearchInfo researchInfo;
	/** The research progress. */
	@ModeUI(mode = { 
			Screens.INFORMATION_INVENTIONS
	})
	UILabel researchProgress;
	/** The inventory level. */
	@ModeUI(mode = { 
			Screens.INFORMATION_INVENTIONS
	})
	UILabel researchInventory;
	/** The research/build cost. */
	@ModeUI(mode = { 
			Screens.INFORMATION_INVENTIONS
	})
	UILabel researchCost;
	/** The research requirements. */
	@ModeUI(mode = { 
			Screens.INFORMATION_INVENTIONS
	})
	UILabel researchPrerequisites;
	/** The first prerequisite. */
	@ModeUI(mode = { 
			Screens.INFORMATION_INVENTIONS
	})
	UILabel researchPre1;
	/** The second prerequisite. */
	@ModeUI(mode = { 
			Screens.INFORMATION_INVENTIONS
	})
	UILabel researchPre2;
	/** The third prerequisite. */
	@ModeUI(mode = { 
			Screens.INFORMATION_INVENTIONS
	})
	UILabel researchPre3;
	/** The required lab label. */
	@ModeUI(mode = { 
			Screens.INFORMATION_INVENTIONS
	})
	UILabel researchRequiredLabs;
	/** The required research lab. */
	@ModeUI(mode = { 
			Screens.INFORMATION_INVENTIONS
	})
	final List<UILabel> researchLabs = new ArrayList<UILabel>();
	/** The available research labs. */
	@ModeUI(mode = { 
			Screens.INFORMATION_INVENTIONS
	})
	final List<UILabel> researchAvailable = new ArrayList<UILabel>();
	/** The military info panel. */
	@ModeUI(mode = { 
			Screens.INFORMATION_MILITARY
	})
	MilitaryInfoPanel militaryInfo;
	/** The planet list details. */
	@ModeUI(mode = { 
			Screens.INFORMATION_PLANETS
	})
	PlanetListDetails planetListDetais;
	/** The toggle for planet list details. */
	boolean showPlanetListDetails;
	/** Toggle the planet list details view. */
	@ModeUI(mode = { 
			Screens.INFORMATION_PLANETS
	})
	UIGenericButton togglePlanetListDetails;
	@Override
	public void onInitialize() {
		base.setBounds(0, 0, 
				commons.info().base.getWidth(), 
				commons.info().base.getHeight());
		
		planetsTab = changeMode(new UIImageTabButton2(commons.info().planets), Screens.INFORMATION_PLANETS);
		colonyTab = changeMode(new UIImageTabButton2(commons.info().colonyInfo), Screens.INFORMATION_COLONY);
		militaryTab = changeMode(new UIImageTabButton2(commons.info().militaryInfo), Screens.INFORMATION_MILITARY);
		financialTab = changeMode(new UIImageTabButton2(commons.info().financialInfo), Screens.INFORMATION_FINANCIAL);
		fleetsTab = changeMode(new UIImageTabButton2(commons.info().fleets), Screens.INFORMATION_FLEETS);
		buildingsTab = changeMode(new UIImageTabButton2(commons.info().buildings), Screens.INFORMATION_BUILDINGS);
		inventionsTab = changeMode(new UIImageTabButton2(commons.info().inventions), Screens.INFORMATION_INVENTIONS);
		aliensTab = changeMode(new UIImageTabButton2(commons.info().aliens), Screens.INFORMATION_ALIENS);
		
		inventionsEmpty = new UIImage(commons.info().emptyButton);
		inventionsEmpty.z = -1;
		aliensEmpty = new UIImage(commons.info().emptyButton);
		aliensEmpty.z = -1;
		
		colony = new UIImageButton(commons.info().colony);
		colony.onClick = new Act() {
			@Override
			public void act() {
				displayPrimary(Screens.COLONY);
			}
		};
		research = new UIImageButton(commons.info().research);
		research.onClick = new Act() {
			@Override
			public void act() {
				displaySecondary(Screens.RESEARCH);
			}
		};
		production = new UIImageButton(commons.info().production);
		production.onClick = new Act() {
			@Override
			public void act() {
				displaySecondary(Screens.PRODUCTION);
			}
		};
		starmap = new UIImageButton(commons.info().starmap);
		starmap.onClick = new Act() {
			@Override
			public void act() {
				displayPrimary(Screens.STARMAP);
			}
		};
		diplomacy = new UIImageButton(commons.info().diplomacy);
		diplomacy.onClick = new Act() {
			@Override
			public void act() {
				displaySecondary(Screens.DIPLOMACY);
			}
		};
		equipment = new UIImageButton(commons.info().equipment);
		equipment.onClick = new Act() {
			@Override
			public void act() {
				displaySecondary(Screens.COLONY);
			}
		};
		
		empty1 = new UIImage(commons.common().emptyButton);
		empty2 = new UIImage(commons.common().emptyButton);
		
		inventionsEmpty.z = -1;
		aliensEmpty.z = -1;
		empty1.z = -1;
		empty2.z = -1;
		
		colonyInfo = new InfoPanel();
		
		minimap = new Minimap();
		
		colonies = new Listing<Planet>();
		colonies.getList = new Func1<Void, List<Planet>>() {
			@Override
			public List<Planet> invoke(Void value) {
				return planetsList();
			}
		};
		colonies.getColor = new Func1<Planet, Integer>() {
			@Override
			public Integer invoke(Planet value) {
				if (value.owner == null) {
					return TextRenderer.GRAY;
				}
				return value.owner.color;
			}
		};
		colonies.getText = new Func1<Planet, String>() {
			@Override
			public String invoke(Planet value) {
				return value.name;
			}
		};
		colonies.getCurrent = new Func1<Void, Planet>() {
			@Override
			public Planet invoke(Void value) {
				return planet();
			}
		};
		colonies.onSelect = new Action1<Planet>() {
			@Override
			public void invoke(Planet value) {
				player().currentPlanet = value;
				displayPlanetInfo();
			}
		};
		colonies.onDoubleClick = new Action1<Planet>() {
			@Override
			public void invoke(Planet value) {
				player().currentPlanet = value;
				displayPlanetInfo();
				displayPrimary(Screens.COLONY);
			}
		};
		
		
		fleets = new Listing<Fleet>();
		fleets.columns = 3;
		fleets.getList = new Func1<Void, List<Fleet>>() {
			@Override
			public List<Fleet> invoke(Void value) {
				return fleetsList();
			}
		};
		fleets.getColor = new Func1<Fleet, Integer>() {
			@Override
			public Integer invoke(Fleet value) {
				if (value.owner == null) {
					return TextRenderer.GRAY;
				}
				return value.owner.color;
			}
		};
		fleets.getText = new Func1<Fleet, String>() {
			@Override
			public String invoke(Fleet value) {
				return value.name;
			}
		};
		fleets.getCurrent = new Func1<Void, Fleet>() {
			@Override
			public Fleet invoke(Void value) {
				return fleet();
			}
		};
		fleets.onSelect = new Action1<Fleet>() {
			@Override
			public void invoke(Fleet value) {
				player().currentFleet = value;
			}
		};
		fleets.onDoubleClick = new Action1<Fleet>() {
			@Override
			public void invoke(Fleet value) {
				player().currentFleet = value;
				displayPrimary(Screens.EQUIPMENT_FLEET);
			}
		};
		
		buildings = new BuildingListing();
		buildings.onSelect = new Action1<BuildingType>() {
			@Override
			public void invoke(BuildingType value) {
				player().currentBuilding = value;
				if (value.research != null) {
					player().currentResearch = value.research;
				}
				displayBuildingInfo();
			}
		};
		buildings.onDoubleClick = new Action1<BuildingType>() {
			@Override
			public void invoke(BuildingType value) {
				player().currentBuilding = value;
				if (value.research != null) {
					player().currentResearch = value.research;
				}
				displayBuildingInfo();
				if (planet().canBuild(value)) {
					PlanetScreen ps = (PlanetScreen)displayPrimary(Screens.COLONY);
					ps.buildingsPanel.build.onPress.act();
				}
			}
		};
		
		descriptionText = new UILabel("", 7, commons.text());
		descriptionText.wrap(true);
		descriptionText.horizontally(HorizontalAlignment.LEFT);
		descriptionText.vertically(VerticalAlignment.TOP);
		
		descriptionImage = new UIImage();
		descriptionImage.center(true);
		
		descriptionTitle = new UILabel("", 10, commons.text());
		descriptionTitle.color(TextRenderer.RED);
		descriptionTitle.horizontally(HorizontalAlignment.CENTER);
		
		buildingCost = new UILabel("", 10, commons.text());
		buildingEnergy = new UILabel("", 10, commons.text());
		buildingWorker = new UILabel("", 10, commons.text());
		
		buildingTitle = new UILabel("", 10, commons.text());
		buildingTitle.color(TextRenderer.RED);
		buildingTitle.size(10);
		buildingTitle.horizontally(HorizontalAlignment.CENTER);

		planetTitle = new UILabel("", 14, commons.text());
		planetTitle.horizontally(HorizontalAlignment.CENTER);

		
		buildingPlanet = new UILabel("", 10, commons.text());
		buildingPlanet.color(TextRenderer.GRAY);
		buildingPlanetOwner = new UILabel("", 10, commons.text());
		buildingPlanetOwner.color(TextRenderer.GRAY);
		buildingPlanetRace = new UILabel("", 10, commons.text());
		buildingPlanetRace.color(TextRenderer.GRAY);
		buildingPlanetSurface = new UILabel("", 10, commons.text());
		buildingPlanetSurface.color(TextRenderer.GRAY);
		
		problemsHouse = new UIImage(commons.common().houseIcon);
		problemsEnergy = new UIImage(commons.common().energyIcon);
		problemsWorker = new UIImage(commons.common().workerIcon);
		problemsFood = new UIImage(commons.common().foodIcon);
		problemsHospital = new UIImage(commons.common().hospitalIcon);
		problemsVirus = new UIImage(commons.common().virusIcon);
		problemsStadium = new UIImage(commons.common().stadiumIcon);
		problemsRepair = new UIImage(commons.common().repairIcon);

		colonyOwner = new UILabel("", 10, commons.text());
		colonyRace = new UILabel("", 10, commons.text());
		colonySurface = new UILabel("", 10, commons.text());
		colonyPopulation = new UILabel("", 10, commons.text());
		colonyTax = new UILabel("", 10, commons.text());
		colonyOther = new UILabel("", 7, commons.text());
		colonyOther.wrap(true);
		colonyOther.vertically(VerticalAlignment.TOP);
		
		financialInfo = new FinancialInfo();
		researchInfo = new ResearchInfo();
		
		researchProgress = new UILabel("", 10, commons.text());
		researchInventory = new UILabel("", 10, commons.text());
		researchCost = new UILabel("", 10, commons.text());

		researchPrerequisites = new UILabel(get("researchinfo.progress.pre"), 10, commons.text());
		researchPre1 = new UILabel("", 10, commons.text());
		researchPre2 = new UILabel("", 10, commons.text());
		researchPre3 = new UILabel("", 10, commons.text());

		researchPre1.onPress = new Act() {
			@Override
			public void act() {
				ResearchType rt = player().currentResearch;
				if (rt != null && rt.prerequisites.size() > 0) {
					world().selectResearch(rt.prerequisites.get(0));
				}
			}
		};
		researchPre2.onPress = new Act() {
			@Override
			public void act() {
				ResearchType rt = player().currentResearch;
				if (rt != null && rt.prerequisites.size() > 1) {
					world().selectResearch(rt.prerequisites.get(1));
				}
			}
		};
		researchPre3.onPress = new Act() {
			@Override
			public void act() {
				ResearchType rt = player().currentResearch;
				if (rt != null && rt.prerequisites.size() > 2) {
					world().selectResearch(rt.prerequisites.get(2));
				}
			}
		};
		
		for (int i = 0; i < 5; i++) {
			researchLabs.add(new UILabel("", 14, commons.text()).horizontally(HorizontalAlignment.CENTER));
			researchAvailable.add(new UILabel("", 14, commons.text()).horizontally(HorizontalAlignment.CENTER));
		}
		
		researchRequiredLabs = new UILabel(get("researchinfo.required_labs"), 7, commons.text());
		
		add(researchLabs);
		add(researchAvailable);
		
		militaryInfo = new MilitaryInfoPanel();
		
		planetListDetais = new PlanetListDetails();
		togglePlanetListDetails = new UIGenericButton(get("info.list_details"), commons.control().fontMetrics(14), commons.common().mediumButton, commons.common().mediumButtonPressed);
		
		togglePlanetListDetails.onClick = new Act() {
			@Override
			public void act() {
				showPlanetListDetails = !showPlanetListDetails;
				if (showPlanetListDetails) {
					togglePlanetListDetails.text(get("info.hide_details"));
				} else {
					togglePlanetListDetails.text(get("info.list_details"));
				}
				colonies.visible(!showPlanetListDetails);
				planetListDetais.visible(showPlanetListDetails);
			}
		};
		
		addThis();
	}
	@Override
	public void onResize() {
		RenderTools.centerScreen(base, width, height, true);
		
		planetsTab.location(base.x, base.y + base.height - planetsTab.height - fleetsTab.height);
		fleetsTab.location(base.x, base.y + base.height - fleetsTab.height);
		
		colonyTab.location(planetsTab.x + planetsTab.width + 1, planetsTab.y);
		militaryTab.location(colonyTab.x + colonyTab.width + 1, colonyTab.y);
		financialTab.location(militaryTab.x + militaryTab.width + 1, militaryTab.y);
		
		buildingsTab.location(fleetsTab.x + fleetsTab.width + 1, fleetsTab.y);
		inventionsTab.location(buildingsTab.x + buildingsTab.width + 1, buildingsTab.y);
		aliensTab.location(inventionsTab.x + inventionsTab.width + 1, inventionsTab.y);
		
		inventionsEmpty.location(inventionsTab.location());
		aliensEmpty.location(aliensTab.location());
		
		empty1.location(aliensEmpty.x + aliensEmpty.width + 2, base.y + base.height - empty1.height);
		empty2.location(empty1.x + empty1.width + 1, empty1.y);
		
		colony.location(empty1.location());
		starmap.location(empty2.location());
		
		production.location(empty1.location());
		research.location(empty2.location());
		diplomacy.location(empty1.location());
		equipment.location(empty1.location());
		
		colonyInfo.location(base.x + 10, base.y + 10);
		
		minimap.bounds(base.x + 415, base.y + 211, 202, 169);
		
		colonies.bounds(base.x + 10, base.y + 10, 400, 27 * 13);
		fleets.bounds(colonies.bounds());
		buildings.bounds(base.x + 10, base.y + 10, 400, 22 * 14);
		
		descriptionTitle.bounds(base.x + 10, base.y + 22 * buildings.rowHeight + 14, 400, 10);
		descriptionText.bounds(base.x + 5, base.y + 22 * buildings.rowHeight + 26, 405, 30);
		descriptionImage.bounds(minimap.bounds());
		
		buildingTitle.bounds(base.x + 415, base.y + 2, 203, 26);
		buildingCost.location(base.x + 420, base.y + 34);
		buildingEnergy.location(base.x + 420, base.y + 34 + 17);
		buildingWorker.location(base.x + 420, base.y + 34 + 17 * 2);
		
		buildingPlanet.location(base.x + 420, buildingWorker.y + 62);
		buildingPlanetOwner.location(base.x + 420, buildingWorker.y  + 62 + 17 * 1);
		buildingPlanetRace.location(base.x + 420, buildingWorker.y + 62  + 17 * 2);
		buildingPlanetSurface.location(base.x + 420, buildingWorker.y + 62  + 17 * 3);
		
		problemsHouse.location(base.x + 420, buildingWorker.y + 62 + 17 * 4);
		problemsEnergy.location(base.x + 436, buildingWorker.y + 62  + 17 * 4);
		problemsWorker.location(base.x + 452, buildingWorker.y + 62 + 17 * 4);
		problemsFood.location(base.x + 468, buildingWorker.y + 62 + 17 * 4);
		problemsHospital.location(base.x + 484, buildingWorker.y + 62 + 17 * 4);
		problemsVirus.location(base.x + 484 + 16, buildingWorker.y + 62 + 17 * 4);
		problemsStadium.location(base.x + 484 + 16 * 2, buildingWorker.y + 62 + 17 * 4);
		problemsRepair.location(base.x + 484 + 16 * 3, buildingWorker.y + 62 + 17 * 4);

		planetTitle.bounds(buildingTitle.bounds());
		
		colonyOwner.location(base.x + 420, base.y + 34);
		colonyRace.location(base.x + 420, base.y + 34 + 17);
		colonySurface.location(base.x + 420, base.y + 34 + 17 * 2);
		colonyPopulation.location(base.x + 420, base.y + 34 + 17 * 3);
		colonyTax.location(base.x + 420, base.y + 34 + 17 *  4);
		colonyOther.bounds(base.x + 420, base.y + 34 + 17 * 5, 193, 12 * 3);
		
		financialInfo.location(base.x + 10, base.y + 10);
		researchInfo.bounds(base.x + 2, base.y + 6, 410, 22 * 14 + 5);
		
		researchProgress.location(base.x + 420, base.y + 34);
		researchInventory.location(base.x + 420, base.y + 34 + 17);
		researchCost.location(base.x + 420, base.y + 34 + 17 * 2);

		researchPrerequisites.location(base.x + 420, base.y + 60 + 17 * 5);
		researchPre1.location(base.x + 430, base.y + 60 + 17 * 6);
		researchPre2.location(base.x + 430, base.y + 60 + 17 * 7);
		researchPre3.location(base.x + 430, base.y + 60 + 17 * 8);

		for (int i = 0; i < 5; i++) {
			researchLabs.get(i).bounds(base.x + 415 + i * 40, base.y + 50 + 17 * 3, 40, 14);
			researchAvailable.get(i).bounds(base.x + 415 + i * 40, base.y + 54 + 17 * 4, 40, 14);
		}
		
		researchRequiredLabs.location(base.x + 420, base.y + 34 + 17 * 3);
		
		planetListDetais.bounds(base.x + 10, base.y + 10, 400, 27 * 13);
		togglePlanetListDetails.location(base.x + 420, colonyOther.y + colonyOther.height + 5);
		
		militaryInfo.bounds(base.x + 10, base.y + 10, 400, 27 * 13);
	}
	@Override
	public void draw(Graphics2D g2) {
		RenderTools.darkenAround(base, width, height, g2, 0.5f, true);
		g2.drawImage(commons.info().base, base.x, base.y, null);

		if (mode == Screens.INFORMATION_PLANETS) {
			displayPlanetInfo();
		} else
		if (mode == Screens.INFORMATION_COLONY) {
			colonyInfo.update();
			displayPlanetInfo();
		} else
		if (mode == Screens.INFORMATION_INVENTIONS) {
			displayInventionInfo();
			g2.setColor(new Color(0xFF4C6CB4));
			g2.drawLine(base.x + 2, descriptionTitle.y - 2, base.x + 413, descriptionTitle.y - 2);
		} else
		if (mode == Screens.INFORMATION_BUILDINGS) {
			displayBuildingInfo();
			g2.setColor(new Color(0xFF4C6CB4));
			g2.drawLine(base.x + 2, descriptionTitle.y - 2, base.x + 413, descriptionTitle.y - 2);
		} else
		if (mode == Screens.INFORMATION_MILITARY) {
			militaryInfo.update();
			displayPlanetInfo();
		} else
		if (mode == Screens.INFORMATION_FINANCIAL) {
			financialInfo.update();
		}
		
		super.draw(g2);
	}
	/** 
	 * First letter to uppercase.
	 * @param s the string
	 * @return the modified string
	 */
	String firstUpper(String s) {
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}
	/**
	 * Add the +/- sign for the given integer value.
	 * @param i the value
	 * @return the string
	 */
	String withSign(int i) {
		if (i < 0) {
			return Integer.toString(i);
		} else
		if (i > 0) {
			return "+" + i;
		}
		return "0";
	}

	/**
	 * Add an onPress handler which changes the display mode.
	 * @param button the button to append to
	 * @param mode the new mode
	 * @return the button
	 */
	UIImageTabButton2 changeMode(UIImageTabButton2 button, final Screens mode) {
		button.onPress = new Act() {
			@Override
			public void act() {
				InfoScreen.this.mode = mode;
				applyMode();
			}
		};
		return button;
	}
	@Override
	public void onEnter(Screens mode) {
		this.mode = mode != null ? mode : Screens.INFORMATION_PLANETS;
		applyMode();
	}
	/** Adjust the visibility of fields and buttons. */
	void applyMode() {
		setUIVisibility();
		production.visible(production.visible() && world().level >= 2);
		research.visible(research.visible() && world().level >= 3);

//		inventionsTab.visible(world().level >= 2);
		aliensTab.visible(world().level >= 4);
		
		planetsTab.selected = mode == Screens.INFORMATION_PLANETS;
		colonyTab.selected = mode == Screens.INFORMATION_COLONY;
		militaryTab.selected = mode == Screens.INFORMATION_MILITARY;
		financialTab.selected = mode == Screens.INFORMATION_FINANCIAL;
		fleetsTab.selected = mode == Screens.INFORMATION_FLEETS;
		buildingsTab.selected = mode == Screens.INFORMATION_BUILDINGS;
		inventionsTab.selected = mode == Screens.INFORMATION_INVENTIONS;
		aliensTab.selected = mode == Screens.INFORMATION_ALIENS;
		
		minimap.displayFleets = mode == Screens.INFORMATION_FLEETS;
		if (mode == Screens.INFORMATION_PLANETS) {
			planetListDetais.visible(showPlanetListDetails);
			colonies.visible(!showPlanetListDetails);
		}
		
		if (mode == Screens.INFORMATION_INVENTIONS) {
			ResearchType rt = player().currentResearch;
			if (rt == null) {
				List<ResearchType> rts = world().getResearch();
				if (rts.size() > 0) {
					rt = rts.get(0);
					world().selectResearch(rt);
				}
			}
		}
	}

	@Override
	public void onLeave() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean mouse(UIMouse e) {
		if (!base.contains(e.x, e.y) && e.has(Type.DOWN)) {
			hideSecondary();
			return true;
		} else {
			return super.mouse(e);
		}
	}
	@Override
	public Screens screen() {
		return mode;
	}
	/**
	 * Set the visibility of UI components based on their annotation.
	 */
	void setUIVisibility() {
		for (Field f : getClass().getDeclaredFields()) {
			ModeUI mi = f.getAnnotation(ModeUI.class);
			if (mi != null) {
				if (UIComponent.class.isAssignableFrom(f.getType())) {
					try {
						UIComponent c = UIComponent.class.cast(f.get(this));
						if (c != null) {
							c.visible(contains(mode, mi.mode()));
						}
					} catch (IllegalAccessException ex) {
						ex.printStackTrace();
					}
				} else
				if (Iterable.class.isAssignableFrom(f.getType())) {
					try {
						Iterable<?> it = Iterable.class.cast(f.get(this));
						if (it != null) {
							Iterator<?> iter = it.iterator();
							while (iter.hasNext()) {
								Object o = iter.next();
								if (UIComponent.class.isAssignableFrom(o.getClass())) {
									UIComponent c = UIComponent.class.cast(o);
									c.visible(contains(mode, mi.mode()));
								}
							}
						}
					} catch (IllegalAccessException ex) {
						ex.printStackTrace();
					}
				}
			}
		}
	}
	/**
	 * Test if the array contains the give item.
	 * @param <T> the element type
	 * @param item the item to find
	 * @param array the array of Ts
	 * @return true if the item is in the array
	 */
	static <T> boolean contains(T item, T... array) {
		for (T a : array) {
			if (item == a || (item != null && item.equals(a))) {
				return true;
			}
		}
		return false;
	}
	/**
	 * The information panel showing some details.
	 * @author akarnokd
	 */
	class InfoPanel extends UIContainer {
		/** The planet name. */
		UILabel planet;
		/** Label field. */
		UILabel owner;
		/** Label field. */
		UILabel race;
		/** Label field. */
		UILabel surface;
		/** Label field. */
		UILabel population;
		/** Label field. */
		UILabel housing;
		/** Label field. */
		UILabel worker;
		/** Label field. */
		UILabel hospital;
		/** Label field. */
		UILabel food;
		/** Label field. */
		UILabel energy;
		/** Label field. */
		UILabel police;
		/** Label field. */
		UILabel taxIncome;
		/** Label field. */
		UILabel tradeIncome;
		/** Label field. */
		UILabel taxMorale;
		/** Label field. */
		UILabel taxLevel;
		/** Label field. */
		UILabel allocation;
		/** Label field. */
		UILabel autobuild;
		/** Label field. */
		UILabel other;
		/** The labels. */
		List<UILabel> lines;
		/** More tax. */
		UIImageButton taxMore;
		/** Less tax. */
		UIImageButton taxLess;
		/** Construct the label elements. */
		public InfoPanel() {
			int textSize = 10;
			planet = new UILabel("-", 14, commons.text());
			planet.location(10, 5);
			owner = new UILabel("-", textSize, commons.text());
			race = new UILabel("-", textSize, commons.text());
			surface = new UILabel("-", textSize, commons.text());
			population = new UILabel("-", textSize, commons.text());
			housing = new UILabel("-", textSize, commons.text());
			worker = new UILabel("-", textSize, commons.text());
			hospital = new UILabel("-", textSize, commons.text());
			food = new UILabel("-", textSize, commons.text());
			energy = new UILabel("-", textSize, commons.text());
			police = new UILabel("-", textSize, commons.text());
			taxIncome = new UILabel("-", textSize, commons.text());
			tradeIncome = new UILabel("-", textSize, commons.text());
			taxMorale = new UILabel("-", textSize, commons.text());
			taxLevel = new UILabel("-", textSize, commons.text());
			allocation = new UILabel("-", textSize, commons.text());
			autobuild = new UILabel("-", textSize, commons.text());
			other = new UILabel("-", textSize, commons.text());
			other.wrap(true);
			other.vertically(VerticalAlignment.TOP);
			other.size(397, (textSize + 2) * 3);
			
			lines = Arrays.asList(
					owner, race, surface, population, housing, worker, hospital, food, energy, police,
					taxIncome, tradeIncome, taxMorale, taxLevel, allocation, autobuild, other
			);

			taxMore = new UIImageButton(commons.info().taxMore);
			taxMore.onClick = new Act() {
				@Override
				public void act() {
					doTaxMore();
				}
			};
			taxMore.setHoldDelay(150);
			
			taxMore.z = 1;
			taxLess = new UIImageButton(commons.info().taxLess);
			taxLess.onClick = new Act() {
				@Override
				public void act() {
					doTaxLess();
				}
			};
			taxLess.setHoldDelay(150);
			taxLess.z = 1;
			taxMore.setDisabledPattern(commons.common().disabledPattern);
			taxLess.setDisabledPattern(commons.common().disabledPattern);

			addThis();
		}
		/** Compute the panel size based on its visible component sizes. */
		public void computeSize() {
			int textSize = 10;
			int w = 0;
			int h = 0;
			int i = 0;
			for (UILabel c : lines) {
				if (c.visible()) {
					c.x = 10;
					c.y = 25 + (textSize + 5) * i;
					c.size(textSize);
					c.height = textSize;
					w = Math.max(w, c.x + c.width);
					h = Math.max(h, c.y + c.height);
					i++;
				}
			}
			w = Math.max(w, this.planet.x + this.planet.width);
			
//			taxLess.location(taxLevel.x + 240, taxLevel.y + (taxLevel.height - taxLess.height) / 2);
			taxLess.location(taxIncome.x + 240, taxIncome.y);
			taxMore.location(taxLess.x + taxLess.width + 5, taxLess.y);
			
			w = Math.max(w, taxMore.x + taxMore.width);
			
			width = w + 10;
			height = h + 5;
		}
		/**
		 * Update the display values based on the current planet's settings.
		 */
		public void update() {
			Planet p = planet();
			
			if (p == null) {
				return;
			}
			
			planet.text(p.name, true);
			
			if (knowledge(p, PlanetKnowledge.OWNER) >= 0) {
				String s = p.owner != null ? p.owner.name : "-";
				owner.text(format("colonyinfo.owner", s), true);
				
				if (p.owner != null) {
					planet.color(p.owner.color);
					owner.color(TextRenderer.GREEN);
				} else {
					planet.color(TextRenderer.GRAY);
					owner.color(TextRenderer.GREEN);
				}
				s = p.isPopulated() ? get(p.getRaceLabel()) : "-";
				race.text(format("colonyinfo.race", s), true);
				owner.visible(true);
				race.visible(true);
			} else {
				owner.visible(false);
				race.visible(false);
				planet.color(TextRenderer.GRAY);
			}
			surface.text(format("colonyinfo.surface", firstUpper(get(p.type.label))), true);

			population.visible(false);
			housing.visible(false);
			worker.visible(false);
			hospital.visible(false);
			food.visible(false);
			energy.visible(false);
			police.visible(false);
			taxIncome.visible(false);
			tradeIncome.visible(false);
			taxMorale.visible(false);
			taxLevel.visible(false);
			allocation.visible(false);
			autobuild.visible(false);

			if (p.isPopulated()) {
				if (knowledge(p, PlanetKnowledge.BUILDING) >= 0) {
					if (p.owner == player()) {
						population.text(format("colonyinfo.population", 
								p.population, get(p.getMoraleLabel()), withSign(p.population - p.lastPopulation)
						), true).visible(true);
					} else {
						population.text(format("colonyinfo.population.alien", 
								p.population
						), true).visible(true);
					}
				}
				if (p.owner == player()) {
					PlanetStatistics ps = p.getStatistics();
					
					setLabel(housing, "colonyinfo.housing", ps.houseAvailable, p.population).visible(true);
					setLabel(worker, "colonyinfo.worker", p.population, ps.workerDemand).visible(true);
					setLabel(hospital, "colonyinfo.hospital", ps.hospitalAvailable, p.population).visible(true);
					setLabel(food, "colonyinfo.food", ps.foodAvailable, p.population).visible(true);
					setLabel(energy, "colonyinfo.energy", ps.energyAvailable, ps.energyDemand).visible(true);
					setLabel(police, "colonyinfo.police", ps.policeAvailable, p.population).visible(true);
					
					taxIncome.text(format("colonyinfo.tax", 
							p.taxIncome
					), true).visible(true);
					tradeIncome.text(format("colonyinfo.trade",
							p.tradeIncome
					), true).visible(true);
					
					taxMorale.text(format("colonyinfo.tax-morale",
							p.morale, withSign(p.morale - p.lastMorale)
					), true).visible(true);
					taxLevel.text(format("colonyinfo.tax-level",
							get(p.getTaxLabel())
					), true).visible(true);
					
					allocation.text(format("colonyinfo.allocation",
							get(p.getAllocationLabel())
					), true).visible(true);
					
					autobuild.text(format("colonyinfo.autobuild",
							get(p.getAutoBuildLabel())
					), true).visible(true);
					
					doAdjustTaxButtons();
					taxLess.visible(true);
					taxMore.visible(true);
				} else {
					taxLess.visible(false);
					taxMore.visible(false);
				}
			} else {
				taxLess.visible(false);
				taxMore.visible(false);
			}
			other.text(format("colonyinfo.other",
					getOtherItems()
			), true);

			computeSize();
		}
		/** 
		 * Color the label according to the relation between the demand and available.
		 * @param label the target label
		 * @param format the format string to use
		 * @param demand the demand amount
		 * @param avail the available amount
		 * @return the label
		 */
		UILabel setLabel(UILabel label, String format, int avail, int demand) {
			label.text(format(format, avail, demand), true);
			if (demand <= avail) {
				label.color(TextRenderer.GREEN);
			} else
			if (demand < avail * 2) {
				label.color(TextRenderer.YELLOW);
			} else {
				label.color(TextRenderer.RED);
			}
			return label;
		}
	}
	/** @return Return the list of other important items. */
	String getOtherItems() {
		StringBuilder os = new StringBuilder();
		for (PlanetInventoryItem pii : planet().inventory) {
			if (pii.owner == player() && pii.type.category == ResearchSubCategory.SPACESHIPS_SATELLITES) {
				if (os.length() > 0) {
					os.append(", ");
				}
				os.append(pii.type.name);
			} else
			if (pii.owner == player() && pii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS) {
				if (os.length() > 0) {
					os.append(", ");
				}
				os.append(pii.type.name);
			}
		}
		return os.toString();
	}
	/** Increase the taxation level. */
	void doTaxMore() {
		Planet p = planet();
		if (p != null) {
			TaxLevel l = p.tax;
			if (l.ordinal() < TaxLevel.values().length - 1) {
				p.tax = TaxLevel.values()[l.ordinal() + 1];
			}
		}
	}
	/** Adjust the tax button based on the current taxation level. */
	void doAdjustTaxButtons() {
		Planet p = planet();
		if (p != null) {
			TaxLevel l = p.tax;
			colonyInfo.taxMore.enabled(l.ordinal() < TaxLevel.values().length - 1);
			colonyInfo.taxLess.enabled(l.ordinal() > 0);
		} else {
			colonyInfo.taxMore.enabled(false);
			colonyInfo.taxLess.enabled(false);
		}
	}
	/** Decrease the taxation level. */
	void doTaxLess() {
		Planet p = planet();
		if (p != null) {
			TaxLevel l = p.tax;
			if (l.ordinal() > 0) {
				p.tax = TaxLevel.values()[l.ordinal() - 1];
			}
		}
	}
	/**
	 * The minimap renderer.
	 * @author akarnokd, Apr 2, 2011
	 */
	class Minimap extends UIComponent {
		/** Display fleets? */
		public boolean displayFleets;
		@Override
		public void draw(Graphics2D g2) {
			g2.drawImage(world().galaxyModel.map, 0, 0, width, height, null);
			Shape save0 = g2.getClip();
			g2.clipRect(0, 0, width, height);
			RenderTools.paintGrid(g2, new Rectangle(0, 0, width, height), commons.starmap().gridColor, commons.text());
			// render planets
			for (Planet p : world().planets.values()) {
				if (knowledge(p, PlanetKnowledge.VISIBLE) < 0) {
					continue;
				}
				int x0 = (p.x * width / commons.starmap().background.getWidth());
				int y0 = (p.y * height / commons.starmap().background.getHeight());
				int labelColor = TextRenderer.GRAY;
				if (p.owner != null && knowledge(p, PlanetKnowledge.OWNER) >= 0) {
					labelColor = p.owner.color;
				}
				g2.setColor(new Color(labelColor));
				g2.fillRect(x0 - 1, y0 - 1, 3, 3);
				if (p == planet()) {
					g2.setColor(new Color(TextRenderer.GRAY));
					g2.drawRect(x0 - 3, y0 - 3, 6, 6);
				}
			}
			if (displayFleets) {
				for (Fleet f : player().fleets.keySet()) {
					if (knowledge(f, FleetKnowledge.VISIBLE) >= 0) {
						int x0 = (f.x * width / commons.starmap().background.getWidth());
						int y0 = (f.y * height / commons.starmap().background.getHeight());
						int x1 = x0 - f.owner.fleetIcon.getWidth() / 2;
						int y1 = y0 - f.owner.fleetIcon.getHeight() / 2;
						g2.drawImage(f.owner.fleetIcon, x1, y1, null);
						if (f == fleet()) {
							g2.setColor(new Color(f.owner.color));
							g2.drawRect(x1 - 2, y1 - 2, f.owner.fleetIcon.getWidth() + 4, f.owner.fleetIcon.getHeight() + 4);
						}
					}
				}
			}
			g2.setClip(save0);
		}
		/** 
		 * Locate a planet at the given coordinates.
		 * @param x the mouse X coordinate
		 * @param y the mouse Y coordinate
		 * @return the planet or null if no planet is present
		 */
		public Planet getPlanetAt(int x, int y) {
			for (Planet p : world().planets.values()) {
				if (knowledge(p, PlanetKnowledge.VISIBLE) < 0) {
					continue;
				}
				int x0 = (p.x * width / commons.starmap().background.getWidth());
				int y0 = (p.y * height / commons.starmap().background.getHeight());
				
				if (x >= x0 - 2 && x <= x0 + 2 && y >= y0 - 2 && y <= y0 + 2) {
					return p;
				}
			}		
			return null;
		}
		/** 
		 * Locate a fleet at the given coordinates.
		 * @param x the mouse X coordinate
		 * @param y the mouse Y coordinate
		 * @return the fleet or null if no planet is present
		 */
		public Fleet getFleetAt(int x, int y) {
			for (Fleet f : player().fleets.keySet()) {
				if (knowledge(f, FleetKnowledge.VISIBLE) >= 0) {
					int x0 = (f.x * width / commons.starmap().background.getWidth());
					int y0 = (f.y * height / commons.starmap().background.getHeight());
					int w = f.owner.fleetIcon.getWidth();
					int h = y0 - f.owner.fleetIcon.getHeight();
					int x1 = x0 - w / 2;
					int y1 = h / 2;
					
					if (x >= x1 - 1 && x <= x1 + w + 2 && y >= y1 - 1 && y <= y1 + h + 2) {
						return f;
					}
				}
			}		
			return null;
		}
		@Override
		public boolean mouse(UIMouse e) {
			if (e.has(Type.DOWN)) {
				if (displayFleets) {
					Fleet f = getFleetAt(e.x, e.y);
					if (f != null) {
						player().currentFleet = f;
						return true;
					}
				}
				Planet p = getPlanetAt(e.x, e.y);
				if (p != null) {
					player().currentPlanet = p;
					return true;
				}
			} else
			if (e.has(Type.DOUBLE_CLICK)) {
				if (displayFleets) {
					Fleet f = getFleetAt(e.x, e.y);
					if (f != null) {
						player().currentFleet = f;
						displaySecondary(Screens.EQUIPMENT_FLEET);
						return true;
					}
				}
				Planet p = getPlanetAt(e.x, e.y);
				if (p != null) {
					player().currentPlanet = p;
					displayPrimary(Screens.COLONY);
					return true;
				}
			}
			return super.mouse(e);
		}
	}
	/**
	 * Displays a list of names and uses callbacks to query for the list and element properties.
	 * @author akarnokd, 2011.04.04.
	 * @param <T> the element type
	 */
	class Listing<T> extends UIComponent {
		/** How many columns to display. */
		int columns = 4;
		/** The start column to display. */
		int columnIndex;
		/** The target font size. */
		int fontSize = 10;
		/** A row height. */
		int rowHeight = fontSize + 3;
		/** Retrieve the list. */
		Func1<Void, List<T>> getList;
		/** Retrieve the color of the list element. */
		Func1<T, Integer> getColor;
		/** Retrieve the text to display. */
		Func1<T, String> getText;
		/** Returns the current item. */
		Func1<Void, T> getCurrent;
		/** The action to invoke when the user selects an item. */
		Action1<T> onSelect;
		/** The action to invoke when the user double-clicks on an item. */
		Action1<T> onDoubleClick;
		@Override
		public void draw(Graphics2D g2) {
			int rowCount = (height + rowHeight - 1) / rowHeight;
			int columnWidth = width / columns;
			
			List<T> planets = getList.invoke(null);
			for (int j = 0; j < columns; j++) {
				int x0 = j * columnWidth;  
				for (int i = 0; i < rowCount; i++) {
					int y0 = i * rowHeight;
					int pidx = (j + columnIndex) * rowCount + i; 
					if (pidx >= 0 && pidx < planets.size()) {
						T p = planets.get(pidx);
						int c = getColor.invoke(p);
						String t = getText.invoke(p);
						commons.text().paintTo(g2, x0, y0 + 1, fontSize, c, t);
						if (p == getCurrent.invoke(null)) {
							g2.setColor(new Color(player().color));
							g2.drawRect(x0 - 2, y0 - 1, columnWidth, rowHeight);
						}
					}
				}
			}
		}
		/** @return the maximum number of columns to display. */
		public int maxColumns() {
			int rowCount = (height + rowHeight - 1) / rowHeight;
			return (getList.invoke(null).size() + rowCount - 1) / rowCount;
		}
		@Override
		public boolean mouse(UIMouse e) {
			if (e.has(Type.DOWN)) {
				T p = getItemAt(e.x, e.y);
				if (p != null) {
					onSelect.invoke(p);
					return true;
				}
			} else
			if (e.has(Type.DOUBLE_CLICK)) {
				T p = getItemAt(e.x, e.y);
				if (p != null) {
					onDoubleClick.invoke(p);
					return true;
				}
			} else
			if (e.has(Type.WHEEL)) {
				if (e.has(Modifier.SHIFT)) {
					if (e.z < 0) {
						columnIndex = Math.max(0, columnIndex - 1);
					} else {
						columnIndex = Math.max(0, Math.min(maxColumns() - columns, columnIndex + 1));
					}
				} else {
					select(e.z < 0 ? -1 : 1);
				}
				return true;
			}
			return super.mouse(e);
		}
		/**
		 * Move the selection by a delta value.
		 * @param delta the delta
		 */
		public void select(int delta) {
			List<T> pl = getList.invoke(null);
			if (pl.size() > 0) {
				int idx = pl.indexOf(getCurrent.invoke(null));
				if (idx < 0) {
					idx = 0;
				} else {
					if (delta < 0) {
						idx = (idx + (pl.size() + delta % pl.size())) % pl.size();
					} else {
						idx = (idx + delta) % pl.size();
					}
				}
				onSelect.invoke(pl.get(idx));
			}
		}
		/** 
		 * Find a planet at the given render coordinates.
		 * @param x the X coordinate
		 * @param y the Y coordinate
		 * @return the planet or null if there is none
		 */
		public T getItemAt(int x, int y) {
			int rowCount = (height + rowHeight - 1) / rowHeight;
			int columnWidth = width / columns;
			int col = x / columnWidth;
			int row = Math.min(y / rowHeight, rowCount - 1);
			int idx = (col + columnIndex) * rowCount + row;
			List<T> items = getList.invoke(null);
			if (idx >= 0 && idx < items.size()) {
				return items.get(idx);
			}
			return null;
		}
	}
	/**
	 * Sort the planets list by owner and name, putting the player's planets at the beginning.
	 * @param planets the list of planets
	 * @param <T> a named and owned object
	 */
	<T extends Named & Owned> void sortByOwnerAndName(List<T> planets) {
		Collections.sort(planets, new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				if (o1.owner() == null && o2.owner() != null) {
					return 1;
				} else
				if (o1.owner() != null && o2.owner() == null) {
					return -1;
				} else
				if (o1.owner() == null && o2.owner() == null) {
					return compareString(o1.name(), o2.name());
				} else
				if (o1.owner() == player() && o2.owner() != player()) {
					return -1;
				} else
				if (o1.owner() != player() && o2.owner() == player()) {
					return 1;
				} else
				if (o1.owner() == player() && o2.owner() == player()) {
					return o1.name().compareTo(o2.name());
				}
				int c = o1.owner().name.compareTo(o2.owner().name);
				if (c == 0) {
					return compareString(o1.name(), o2.name());
				}
				return c;
			}
		});
	}
	/**
	 * Compare two strings which may have a tailing number.
	 * @param s1 the first string
	 * @param s2 the second string
	 * @return the comparison result
	 */
	int compareString(String s1, String s2) {
		char c1 = s1.charAt(s1.length() - 1);
		char c2 = s2.charAt(s2.length() - 1);
		if (Character.isDigit(c1) && Character.isDigit(c2)) {
			int sp1 = s1.lastIndexOf(' ');
			int sp2 = s2.lastIndexOf(' ');
			
			if (sp1 >= 0 && sp2 >= 0) {
				int c = s1.substring(0, sp1).compareTo(s2.substring(0, sp2));
				if (c == 0) {
					c = Integer.parseInt(s1.substring(sp1 + 1)) - Integer.parseInt(s2.substring(sp2 + 1));
				}
				return c;
			}
		}
		return s1.compareTo(s2);
	}
	/** @return an ordered list of planets to display. */
	List<Planet> planetsList() {
		List<Planet> planets = new ArrayList<Planet>();
		for (Planet p : player().planets.keySet()) {
			if (knowledge(p, PlanetKnowledge.OWNER) >= 0) {
				planets.add(p);
			}
		}
		sortByOwnerAndName(planets);
		return planets;
	}
	/** @return an ordered list of planets to display. */
	List<Fleet> fleetsList() {
		List<Fleet> fleets = new ArrayList<Fleet>();
		for (Fleet f : player().fleets.keySet()) {
			if (knowledge(f, FleetKnowledge.VISIBLE) >= 0) {
				fleets.add(f);
			}
		}
		sortByOwnerAndName(fleets);
		return fleets;
	}
	/**
	 * Displays a list of names and uses callbacks to query for the list and element properties.
	 * @author akarnokd, 2011.04.04.
	 */
	class BuildingListing extends UIComponent {
		/** How many columns to display. */
		int columns = 2;
		/** The start column to display. */
		int columnIndex;
		/** The target font size. */
		int fontSize = 10;
		/** A row height. */
		int rowHeight = fontSize + 4;
		/** Retrieve the list. */
		Func1<Void, List<BuildingType>> getList = new Func1<Void, List<BuildingType>>() {
			@Override
			public List<BuildingType> invoke(Void value) {
				return world().listBuildings();
			}
		};
		/** The action to invoke when the user selects an item. */
		Action1<BuildingType> onSelect;
		/** The action to invoke when the user double-clicks on an item. */
		Action1<BuildingType> onDoubleClick;
		@Override
		public void draw(Graphics2D g2) {
			int rowCount = (height + rowHeight - 1) / rowHeight;
			int columnWidth = width / columns;
			
			List<BuildingType> planets = getList.invoke(null);
			Map<BuildingType, Integer> counts = player().countBuildings();
			Map<BuildingType, Integer> current = planet().countBuildings();
			boolean showCounts = knowledge(planet(), PlanetKnowledge.OWNER) >= 0;
			for (int j = 0; j < columns; j++) {
				int x0 = j * columnWidth;  
				for (int i = 0; i < rowCount; i++) {
					int y0 = i * rowHeight;
					int pidx = (j + columnIndex) * rowCount + i; 
					if (pidx >= 0 && pidx < planets.size()) {
						BuildingType p = planets.get(pidx);
						int c = planet().canBuild(p) && planet().owner == player() ? TextRenderer.YELLOW : TextRenderer.GRAY;
						String t = p.name;
						commons.text().paintTo(g2, x0 + 30, y0 + 2, fontSize, c, t);
						
						if (showCounts) {
							Integer c0 = current.get(p);
							Integer c1 = counts.get(p);
							
							String n = (c0 != null ? c0 : 0) + "/" + (c1 != null ? c1 : 0);
							commons.text().paintTo(g2, x0, y0 + 4, 7, c1 != null ? TextRenderer.GREEN : TextRenderer.GRAY, n);
						}
						if (p == player().currentBuilding) {
							g2.setColor(new Color(player().color));
							g2.drawRect(x0 - 2 + 30, y0, columnWidth - 30, rowHeight);
						}
					}
				}
			}
		}
		/** @return the maximum number of columns to display. */
		public int maxColumns() {
			int rowCount = (height + rowHeight - 1) / rowHeight;
			return (getList.invoke(null).size() + rowCount - 1) / rowCount;
		}
		@Override
		public boolean mouse(UIMouse e) {
			if (e.has(Type.DOWN)) {
				BuildingType p = getItemAt(e.x, e.y);
				if (p != null) {
					onSelect.invoke(p);
					return true;
				}
			} else
			if (e.has(Type.DOUBLE_CLICK)) {
				BuildingType p = getItemAt(e.x, e.y);
				if (p != null && planet().canBuild(p) && planet().owner == player()) {
					onDoubleClick.invoke(p);
					return true;
				}
			} else
			if (e.has(Type.WHEEL)) {
				if (e.has(Modifier.SHIFT)) {
					if (e.z < 0) {
						columnIndex = Math.max(0, columnIndex - 1);
					} else {
						columnIndex = Math.max(0, Math.min(maxColumns() - columns, columnIndex + 1));
					}
				} else {
					selectBuilding(e.z < 0 ? -1 : 1);
				}
				return true;
			}
			return super.mouse(e);
		}
		/** 
		 * Select a building relative to the current building.
		 * @param delta the delta
		 */
		public void selectBuilding(int delta) {
			List<BuildingType> pl = getList.invoke(null);
			if (pl.size() > 0) {
				int idx = pl.indexOf(building());
				if (idx < 0) {
					idx = 0;
				} else {
					if (delta < 0) {
						idx = (idx + (pl.size() + delta % pl.size())) % pl.size();
					} else {
						idx = (idx + delta) % pl.size();
					}
				}
				onSelect.invoke(pl.get(idx));
			}
		}
		/** 
		 * Find a planet at the given render coordinates.
		 * @param x the X coordinate
		 * @param y the Y coordinate
		 * @return the planet or null if there is none
		 */
		public BuildingType getItemAt(int x, int y) {
			int rowCount = (height + rowHeight - 1) / rowHeight;
			int columnWidth = width / columns;
			int col = x / columnWidth;
			int row = Math.min(y / rowHeight, rowCount - 1);
			int idx = (col + columnIndex) * rowCount + row;
			List<BuildingType> items = getList.invoke(null);
			if (idx >= 0 && idx < items.size()) {
				return items.get(idx);
			}
			return null;
		}
	}
	@Override
	public boolean keyboard(KeyEvent e) {
		switch (mode) {
		case INFORMATION_BUILDINGS:
			switch (e.getKeyCode()) {
			case KeyEvent.VK_UP:
				buildings.selectBuilding(-1);
				return true;
			case KeyEvent.VK_DOWN:
				buildings.selectBuilding(1);
				return true;
			case KeyEvent.VK_LEFT:
				buildings.selectBuilding(-22);
				return true;
			case KeyEvent.VK_RIGHT:
				buildings.selectBuilding(22);
				return true;
			case KeyEvent.VK_ENTER:
				BuildingType bt = building();
				if (bt != null && planet().canBuild(bt) && planet().owner == player()) {
					buildings.onDoubleClick.invoke(building());
					return true;
				}
			default:
			}
			break;
		case INFORMATION_FLEETS:
			switch (e.getKeyCode()) {
			case KeyEvent.VK_UP:
				fleets.select(-1);
				return true;
			case KeyEvent.VK_DOWN:
				fleets.select(1);
				return true;
			case KeyEvent.VK_LEFT:
				fleets.select(-27);
				return true;
			case KeyEvent.VK_RIGHT:
				fleets.select(27);
				return true;
			default:
			}
			break;
		case INFORMATION_PLANETS:
			switch (e.getKeyCode()) {
			case KeyEvent.VK_UP:
				colonies.select(-1);
				return true;
			case KeyEvent.VK_DOWN:
				colonies.select(1);
				return true;
			case KeyEvent.VK_LEFT:
				colonies.select(-27);
				return true;
			case KeyEvent.VK_RIGHT:
				colonies.select(27);
				return true;
			case KeyEvent.VK_ENTER:
				colonies.onDoubleClick.invoke(planet());
			default:
			}
			break;
		default:
		}
		return super.keyboard(e);
	}
	/**
	 * The financial info panel.
	 * @author akarnokd, Apr 4, 2011
	 */
	class FinancialInfo extends UIContainer {
		/** Static field. */
		UILabel yesterday;
		/** Global yesterday value. */
		UILabel yesterdayTaxIncome;
		/** Global yesterday value. */
		UILabel yesterdayTradeIncome;
		/** Global yesterday value. */
		UILabel yesterdayTaxMorale;
		/** Global yesterday value. */
		UILabel yesterdayProductionCost;
		/** Global yesterday value. */
		UILabel yesterdayResearchCost;
		/** Global yesterday value. */
		UILabel yesterdayRepairCost;
		/** Global yesterday value. */
		UILabel yesterdayBuildCost;
		/** Static field. */
		UILabel today;
		/** Global today value. */
		UILabel todayProductionCost;
		/** Global today value. */
		UILabel todayResearchCost;
		/** Global today value. */
		UILabel todayRepairCost;
		/** Global today value. */
		UILabel todayBuildCost;
		/** Static field. */
		UILabel planetCurrent;
		/** Selected own planet value. */
		UILabel planetTaxIncome;
		/** Selected own planet value. */
		UILabel planetTradeIncome;
		/** Selected own planet value. */
		UILabel planetTaxMorale;
		
		/** Construct the fields. */
		public FinancialInfo() {
			yesterday = new UILabel(get("financialinfo.yesterday"), 14, commons.text());
			yesterday.color(TextRenderer.RED);
			today = new UILabel(get("financialinfo.today"), 14, commons.text());
			today.color(TextRenderer.RED);
			planetCurrent = new UILabel("", 14, commons.text());
			planetCurrent.color(TextRenderer.RED);
			
			yesterdayTaxIncome = new UILabel("", 10, commons.text());
			yesterdayTradeIncome = new UILabel("", 10, commons.text());
			yesterdayTaxMorale = new UILabel("", 10, commons.text());

			yesterdayProductionCost = new UILabel("", 10, commons.text());
			yesterdayResearchCost = new UILabel("", 10, commons.text());
			yesterdayRepairCost = new UILabel("", 10, commons.text());
			yesterdayBuildCost = new UILabel("", 10, commons.text());
			
			todayProductionCost = new UILabel("", 10, commons.text());
			todayResearchCost = new UILabel("", 10, commons.text());
			todayRepairCost = new UILabel("", 10, commons.text());
			todayBuildCost = new UILabel("", 10, commons.text());
			
			planetTaxIncome = new UILabel("", 10, commons.text());
			planetTradeIncome = new UILabel("", 10, commons.text());
			planetTaxMorale = new UILabel("", 10, commons.text());
			
			yesterday.location(0, 0);
			yesterdayTaxIncome.location(10, 25);
			yesterdayTradeIncome.location(10, 25 + 18);
			yesterdayTaxMorale.location(10, 25 + 18 * 2);
			
			yesterdayProductionCost.location(10, 25 + 18 * 3);
			yesterdayResearchCost.location(10, 25 + 18 * 4);
			yesterdayRepairCost.location(10, 25 + 18 * 5);
			yesterdayBuildCost.location(10, 25 + 18 * 6);
			
			today.location(0, 50 + 18 * 6);
			todayProductionCost.location(10, today.y + 25);
			todayResearchCost.location(10, today.y + 25 + 18);
			todayRepairCost.location(10, today.y + 25 + 18 * 2);
			todayBuildCost.location(10, today.y + 25 + 18 * 3);
			
			planetCurrent.location(0, today.y + 50 + 18 * 3);
			planetTaxIncome.location(10, planetCurrent.y + 25);
			planetTradeIncome.location(10, planetCurrent.y + 25 + 18);
			planetTaxMorale.location(10, planetCurrent.y + 25 + 18 * 2);
			
			size(400, planetTaxMorale.y + 12);
			
			addThis();
		}
		/**
		 * Update display values.
		 */
		public void update() {
			Planet p = planet();
			
			yesterdayTaxIncome.text(format("colonyinfo.tax", player().yesterday.taxIncome), true);
			yesterdayTradeIncome.text(format("colonyinfo.trade", player().yesterday.tradeIncome), true);
			if (player().yesterday.taxMoraleCount > 0) {
				yesterdayTaxMorale.text(format("colonyinfo.tax-morale", 
						player().yesterday.taxMorale / player().yesterday.taxMoraleCount, 
						get(Planet.getMoraleLabel(player().yesterday.taxMorale / player().yesterday.taxMoraleCount))), true);
			} else {
				yesterdayTaxMorale.text(format("colonyinfo.tax-morale", 
						50, 
						get(Planet.getMoraleLabel(50))), true);
				
			}
			yesterdayProductionCost.text(format("financialinfo.production_cost", player().yesterday.productionCost), true);
			yesterdayResearchCost.text(format("financialinfo.research_cost", player().yesterday.researchCost), true);
			yesterdayRepairCost.text(format("financialinfo.repair_cost", player().yesterday.repairCost), true);
			yesterdayBuildCost.text(format("financialinfo.build_cost", player().yesterday.buildCost), true);
			
			todayProductionCost.text(format("financialinfo.production_cost", player().today.productionCost), true);
			todayResearchCost.text(format("financialinfo.research_cost", player().today.researchCost), true);
			todayRepairCost.text(format("financialinfo.repair_cost", player().today.repairCost), true);
			todayBuildCost.text(format("financialinfo.build_cost", player().today.buildCost), true);
			
			if (p.owner == player()) {
				planetCurrent.text(p.name, true);
				
				planetTaxIncome.text(format("colonyinfo.tax", p.taxIncome), true);
				planetTradeIncome.text(format("colonyinfo.trade", p.tradeIncome), true);
				planetTaxMorale.text(format("colonyinfo.tax-morale", p.morale, get(p.getMoraleLabel())), true);
				
				planetCurrent.visible(true);
				planetTaxIncome.visible(true);
				planetTradeIncome.visible(true);
				planetTaxMorale.visible(true);
				
			} else {
				planetCurrent.visible(false);
				planetTaxIncome.visible(false);
				planetTradeIncome.visible(false);
				planetTaxMorale.visible(false);
			}
			displayColonyProblems(p);
		}
	}
	/** Display the information about the given building type. */
	void displayBuildingInfo() {
		BuildingType bt = building();
		TileSet ts = bt != null ? bt.tileset.get(race()) : null;
		if (ts == null) {
			List<BuildingType> bs = buildings.getList.invoke(null);
			if (bs.size() > 0) {
				bt = bs.get(0);
				player().currentBuilding = bt;
				ts = bt.tileset.get(race());
			}
		}
		if (bt != null && ts != null) {
			descriptionTitle.text(bt.name);
			descriptionText.text(bt.description);
			descriptionImage.image(ts.preview);
			
			buildingTitle.text(bt.name);
			
			buildingCost.text(format("buildinginfo.building.cost", bt.cost), true);
			int e = (int)(bt.resources.get("energy").amount);
			int w = (int)(bt.resources.get("worker").amount);
			buildingEnergy.text(format("buildinginfo.building.energy", e < 0 ? -e : e), true);
			buildingWorker.text(format("buildinginfo.building.worker", w < 0 ? -w : 0), true);
		} else {
			descriptionTitle.text("", true);
			descriptionText.text("", true);
			descriptionImage.image(null);
			
			buildingTitle.text("");
			buildingCost.text("", true);
			buildingEnergy.text("", true);
			buildingWorker.text("", true);
		}
		Planet p = planet();
		if (knowledge(p, PlanetKnowledge.NAME) >= 0) {
			buildingPlanet.text(p.name, true);
		} else {
			buildingPlanet.text("", true);
		}
		if (knowledge(p, PlanetKnowledge.OWNER) >= 0 && p.owner != null) {
			buildingPlanetOwner.text(p.owner.name, true);
			if (p.owner == player()) {
				buildingPlanetRace.text(format("colonyinfo.population.own", 
						p.population, get(p.getRaceLabel()), get(p.getMoraleLabel()) 
				), true);
			} else {
				if (knowledge(p, PlanetKnowledge.BUILDING) >= 0) {
					if (p.isPopulated()) {
						buildingPlanetRace.text(format("colonyinfo.population.short.alien", 
								p.population
						), true);
					} else {
						buildingPlanetRace.text("");
					}
				} else {
					buildingPlanetRace.text(p.isPopulated() ? get(p.getRaceLabel()) : "", true);
				}
			}
		} else {
			buildingPlanetOwner.text("", true);
			buildingPlanetRace.text("", true);
		}
		
		buildingPlanetSurface.text(format("buildinginfo.planet.surface", firstUpper(get(p.type.label))), true);
		
		displayColonyProblems(p);
	}
	/**
	 * Display the colony problem icons for the own planets.
	 * @param p the planet
	 */
	void displayColonyProblems(Planet p) {
		if (p.owner == player()) {
			PlanetStatistics ps = p.getStatistics();
			problemsHouse.visible(ps.has(PlanetProblems.HOUSING));
			problemsEnergy.visible(ps.has(PlanetProblems.ENERGY));
			problemsWorker.visible(ps.has(PlanetProblems.WORKFORCE));
			problemsFood.visible(ps.has(PlanetProblems.FOOD));
			problemsHospital.visible(ps.has(PlanetProblems.HOSPITAL));
			problemsVirus.visible(ps.has(PlanetProblems.VIRUS));
			problemsStadium.visible(ps.has(PlanetProblems.STADIUM));
			problemsRepair.visible(ps.has(PlanetProblems.REPAIR));
		} else {
			problemsHouse.visible(false);
			problemsEnergy.visible(false);
			problemsWorker.visible(false);
			problemsFood.visible(false);
			problemsHospital.visible(false);
			problemsVirus.visible(false);
			problemsStadium.visible(false);
			problemsRepair.visible(false);
		}
	}
	/** Display the right panel's planet info on the current selected planet. */
	void displayPlanetInfo() {
		
		Planet p = planet();
		
		if (knowledge(p, PlanetKnowledge.NAME) >= 0) {
			planetTitle.text(p.name);
			int c = TextRenderer.GRAY;
			if (knowledge(p, PlanetKnowledge.OWNER) >= 0 && p.isPopulated()) {
				c = p.owner.color;
			}
			planetTitle.color(c);
			planetTitle.visible(true);
		} else {
			planetTitle.visible(false);
		}
		
		if (knowledge(p, PlanetKnowledge.OWNER) >= 0 && p.isPopulated()) {
			colonyOwner.text(p.owner != null ? p.owner.name : "", true);
			colonyRace.text(p.isPopulated() ? get(p.getRaceLabel()) : "-", true);
			colonyOwner.visible(true);
			colonyRace.visible(true);
		} else {
			colonyOwner.visible(false);
			colonyRace.visible(false);
		}
		colonySurface.text(format("buildinginfo.planet.surface", firstUpper(get(p.type.label))), true);
		
		if (p.owner == player()) {
			colonyPopulation.text(format("colonyinfo.population.own", 
					p.population, get(p.getRaceLabel()), get(p.getMoraleLabel()) 
			), true).visible(true);
			colonyTax.text(format("colonyinfo.tax_short", get(p.getTaxLabel())), true).visible(true);
		} else {
			if (knowledge(p, PlanetKnowledge.BUILDING) >= 0) {
				if (p.isPopulated()) {
					colonyPopulation.text(format("colonyinfo.population.short.alien", 
							p.population
					), true).visible(true);
				} else {
					colonyPopulation.visible(false);
				}
			} else {
				colonyPopulation.visible(false);
			}
			colonyTax.visible(false);
		}
		colonyOther.text(getOtherItems());
		
		displayColonyProblems(p);
	}
	/** The research listing panel. */
	class ResearchInfo extends UIComponent {
		@Override
		public void draw(Graphics2D g2) {
			int col = 0;
			int colWidth = width / 4;
			g2.setColor(new Color(0xFF4C6CB4));
			for (int i = 1; i < 4; i++) {
				g2.drawLine(i * colWidth, 0, i * colWidth, height);
			}
			for (ResearchMainCategory mc : ResearchMainCategory.values()) {
				int row = 0;
				int cat = 0;
				for (ResearchSubCategory sc : ResearchSubCategory.values()) {
					if (sc.main == mc) {
						List<ResearchType> res = new ArrayList<ResearchType>();
						for (ResearchType rt : world().researches.values()) {
							if (rt.category == sc && world().canDisplayResearch(rt)) {
								res.add(rt);
							}
						}
						cat++;
						Collections.sort(res, new Comparator<ResearchType>() {
							@Override
							public int compare(ResearchType o1, ResearchType o2) {
								return o1.index - o2.index;
							}
						});
						if (cat > 1) {
							g2.setColor(new Color(TextRenderer.GRAY));
							g2.drawLine(col * colWidth, row * 12 - 1, (col + 1) * colWidth, row * 12 - 1);
						}
						for (ResearchType rt : res) {
							int c = world().getResearchColor(rt);
							commons.text().paintTo(g2, col * colWidth + 3, row * 12 + 2, 7, c, rt.name);
							if (rt == player().currentResearch) {
								g2.setColor(new Color(TextRenderer.ORANGE));
								g2.drawRect(col * colWidth + 1, row * 12, colWidth - 2, 10);
							}
							row++;
						}
					}
				}
				col++;
			}
			super.draw(g2);
		}
		@Override
		public boolean mouse(UIMouse e) {
			if (e.has(Type.DOWN)) {
				int col = e.x * 4 / width;
				List<ResearchType> res = getResearchColumn(col);
				int row = e.y / 12;
				if (row < res.size()) {
					ResearchType rt = res.get(row);
					world().selectResearch(rt);
					return true;
				}
			} else
			if (e.has(Type.DOUBLE_CLICK)) {
				int col = e.x * 4 / width;
				List<ResearchType> res = getResearchColumn(col);
				int row = e.y / 12;
				if (row < res.size()) {
					ResearchType rt = res.get(row);
					world().selectResearch(rt);
					displaySecondary(Screens.RESEARCH);
					return true;
				}
			}
			return super.mouse(e);
		};
	}
	/**
	 * Get the list of research items for the given column.
	 * @param column the column index
	 * @return the list of reseaches in that column
	 */
	public List<ResearchType> getResearchColumn(int column) {
		List<ResearchType> res = new ArrayList<ResearchType>();
		for (ResearchSubCategory sc : ResearchSubCategory.values()) {
			if (sc.main == ResearchMainCategory.values()[column]) {
				for (ResearchType rt : world().researches.values()) {
					if (rt.category == sc && world().canDisplayResearch(rt)) {
						res.add(rt);
					}
				}
			}
		}
		Collections.sort(res, new Comparator<ResearchType>() {
			@Override
			public int compare(ResearchType o1, ResearchType o2) {
				int c = o1.category.ordinal() - o2.category.ordinal();
				if (c == 0) {
					c = o1.index - o2.index; 
				}
				return c;
			}
		});
		return res;
	}
	/**
	 * Display the details of the currently selected invention.
	 */
	void displayInventionInfo() {
		ResearchType rt = player().currentResearch;
		planetTitle.text("");
		descriptionImage.image(null);
		descriptionText.text("");
		descriptionTitle.text("");
		researchProgress.visible(false);
		researchCost.visible(false);
		researchInventory.visible(false);
		researchPrerequisites.visible(false);
		researchPre1.visible(false);
		researchPre2.visible(false);
		researchPre3.visible(false);
		
		if (rt != null) {
			planetTitle.text(rt.name);
			if (player().isAvailable(rt) || world().canResearch(rt)) {
				descriptionImage.image(rt.infoImage);
				descriptionText.text(rt.description);
				descriptionTitle.text(rt.longName);
				planetTitle.color(TextRenderer.ORANGE);
				
			} else {
				descriptionImage.image(rt.infoImageWired);
				descriptionText.text("");
				descriptionTitle.text(rt.longName);
				if (world().canResearch(rt)) {
					planetTitle.color(TextRenderer.GREEN);
				} else {
					planetTitle.color(TextRenderer.GRAY);
				}
			}
			if (player().research.containsKey(rt)) {
				Research rs = player().research.get(rt);
				switch (rs.state) {
				case RUNNING:
					researchProgress.text(format("researchinfo.progress.running", (int)rs.getPercent()), true).visible(true);
					break;
				case STOPPED:
					researchProgress.text(format("researchinfo.progress.paused", (int)rs.getPercent()), true).visible(true);
					break;
				case LAB:
					researchProgress.text(format("researchinfo.progress.lab", (int)rs.getPercent()), true).visible(true);
					break;
				case MONEY:
					researchProgress.text(format("researchinfo.progress.money", (int)rs.getPercent()), true).visible(true);
					break;
				default:
					researchProgress.text("");
				}
				researchCost.text(format("researchinfo.progress.cost", rt.researchCost), true).visible(true);
			} else {
				if (player().isAvailable(rt)) {
					researchProgress.text(get("researchinfo.progress.done"), true).visible(true);
					researchCost.text(format("researchinfo.progress.price", rt.productionCost), true).visible(true);
					Integer cnt = player().inventory.get(rt);
					if (rt.category.main != ResearchMainCategory.BUILDINS) {
						researchInventory.text(format("researchinfo.progress.inventory", cnt != null ? cnt : 0), true).visible(true);
					}
				} else {
					if (world().canResearch(rt)) {
						researchProgress.text(get("researchinfo.progress.can"), true).visible(true);
						researchCost.text(format("researchinfo.progress.cost", rt.researchCost), true).visible(true);
					} else {
						researchProgress.text(get("researchinfo.progress.cant"), true).visible(true);
						descriptionImage.image(null);
					}
				}
			}
			if (rt.prerequisites.size() > 0) {
				researchPrerequisites.visible(true);
				researchPre1.text(rt.prerequisites.get(0).name, true).visible(true);
				researchPre1.color(world().getResearchColor(rt.prerequisites.get(0)));
			}
			if (rt.prerequisites.size() > 1) {
				researchPre2.text(rt.prerequisites.get(1).name, true).visible(true);
				researchPre2.color(world().getResearchColor(rt.prerequisites.get(1)));
			}
			if (rt.prerequisites.size() > 2) {
				researchPre3.text(rt.prerequisites.get(2).name, true).visible(true);
				researchPre3.color(world().getResearchColor(rt.prerequisites.get(2)));
			}
			
			researchLabs.get(0).text(Integer.toString(rt.civilLab));
			researchLabs.get(1).text(Integer.toString(rt.mechLab));
			researchLabs.get(2).text(Integer.toString(rt.compLab));
			researchLabs.get(3).text(Integer.toString(rt.aiLab));
			researchLabs.get(4).text(Integer.toString(rt.milLab));
			
			PlanetStatistics ps = player().getPlanetStatistics();

			researchAvailable.get(0).text(Integer.toString(ps.civilLabActive));
			researchAvailable.get(0).color(
					ps.civilLab < rt.civilLab ? TextRenderer.RED 
						: (ps.civilLab > ps.civilLabActive ? TextRenderer.YELLOW : TextRenderer.GREEN)
			);
			researchAvailable.get(1).text(Integer.toString(ps.mechLabActive));
			researchAvailable.get(1).color(
					ps.mechLab < rt.mechLab ? TextRenderer.RED 
						: (ps.mechLab > ps.mechLabActive ? TextRenderer.YELLOW : TextRenderer.GREEN)
			);
			researchAvailable.get(2).text(Integer.toString(ps.compLabActive));
			researchAvailable.get(2).color(
					ps.compLab < rt.compLab ? TextRenderer.RED 
						: (ps.compLab > ps.compLabActive ? TextRenderer.YELLOW : TextRenderer.GREEN)
			);
			researchAvailable.get(3).text(Integer.toString(ps.aiLabActive));
			researchAvailable.get(3).color(
					ps.aiLab < rt.aiLab ? TextRenderer.RED 
						: (ps.aiLab > ps.aiLabActive ? TextRenderer.YELLOW : TextRenderer.GREEN)
			);
			researchAvailable.get(4).text(Integer.toString(ps.milLabActive));
			researchAvailable.get(4).color(
					ps.milLab < rt.milLab ? TextRenderer.RED 
						: (ps.milLab > ps.milLabActive ? TextRenderer.YELLOW : TextRenderer.GREEN)
			);
		}
	}
	/**
	 * The information panel showing some details.
	 * @author akarnokd
	 */
	class MilitaryInfoPanel extends UIContainer {
		/** The planet name. */
		UILabel planet;
		/** Label field. */
		UILabel owner;
		/** Label field. */
		UILabel race;
		/** Label field. */
		UILabel surface;
		/** Label field. */
		UILabel population;
		
		/** Construct the label elements. */
		public MilitaryInfoPanel() {
			int textSize = 10;
			planet = new UILabel("-", 14, commons.text());
			planet.location(10, 5);
			owner = new UILabel("-", textSize, commons.text());
			owner.location(10, 25);
			race = new UILabel("-", textSize, commons.text());
			race.location(10, 40);
			surface = new UILabel("-", textSize, commons.text());
			surface.location(10, 55);
			population = new UILabel("-", textSize, commons.text());
			population.location(10, 70);

			addThis();
		}
		/**
		 * Update the display values based on the current planet's settings.
		 */
		public void update() {
			Planet p = planet();
			
			if (p == null) {
				return;
			}
			
			planet.text(p.name, true);
			
			if (knowledge(p, PlanetKnowledge.OWNER) >= 0) {
				String s = p.owner != null ? p.owner.name : "-";
				owner.text(format("colonyinfo.owner", s), true);
				
				if (p.owner != null) {
					planet.color(p.owner.color);
					owner.color(TextRenderer.GREEN);
				} else {
					planet.color(TextRenderer.GRAY);
					owner.color(TextRenderer.GREEN);
				}
				s = p.isPopulated() ? get(p.getRaceLabel()) : "-";
				race.text(format("colonyinfo.race", s), true);
				owner.visible(true);
				race.visible(true);
			} else {
				owner.visible(false);
				race.visible(false);
				planet.color(TextRenderer.GRAY);
			}
			surface.text(format("colonyinfo.surface", firstUpper(get(p.type.label))), true);

			population.visible(false);

			if (p.isPopulated()) {
				if (knowledge(p, PlanetKnowledge.BUILDING) >= 0) {
					if (p.owner == player()) {
						population.text(format("colonyinfo.population", 
								p.population, get(p.getMoraleLabel()), withSign(p.population - p.lastPopulation)
						), true).visible(true);
					} else {
						population.text(format("colonyinfo.population.alien", 
								p.population
						), true).visible(true);
					}
				}
			}
		}
		/** 
		 * Color the label according to the relation between the demand and available.
		 * @param label the target label
		 * @param format the format string to use
		 * @param demand the demand amount
		 * @param avail the available amount
		 * @return the label
		 */
		UILabel setLabel(UILabel label, String format, int avail, int demand) {
			label.text(format(format, avail, demand), true);
			if (demand <= avail) {
				label.color(TextRenderer.GREEN);
			} else
			if (demand < avail * 2) {
				label.color(TextRenderer.YELLOW);
			} else {
				label.color(TextRenderer.RED);
			}
			return label;
		}
		@Override
		public void draw(Graphics2D g2) {
			
			List<ResearchType> res = getResearchColumn(2);
			
			int row = 0;
			for (ResearchType rt : res) {
				if (rt.category == ResearchSubCategory.WEAPONS_TANKS 
						|| rt.category == ResearchSubCategory.WEAPONS_VEHICLES) {

					int cnt = planet().getInventoryCount(rt);
					
					if (player().isAvailable(rt) 
							|| (planet().owner != player() && cnt > 0)) {
						commons.text().paintTo(g2, 10, row * 15 + 20 + population.y, 10, TextRenderer.GREEN, rt.name);

						String cntStr = "";
						
						if (planet().owner != player()) {
							if (knowledge(planet(), PlanetKnowledge.BUILDING) >= 0) {
								cntStr = ": " + ((cnt / 10) * 10) + ".." + ((cnt / 10 + 1) * 10);
							} else {
								cntStr = ": ?";
							}
						} else {
							cntStr = (cnt > 0 ? ": " + cnt : ": -");
						}
						commons.text().paintTo(g2, 150, row * 15 + 20 + population.y, 10, TextRenderer.GREEN, cntStr);
						
						row++;
					}
				}
			}
			
			super.draw(g2);
		}
	}
	/**
	 * The component lists the planets in a tabular format, showing the problems and
	 * tax/morale info. 
	 * @author akarnokd, 2011.04.07.
	 */
	class PlanetListDetails extends UIComponent {
		/** The top display offset. */
		int top;
		@Override
		public void draw(Graphics2D g2) {
			List<Planet> list = colonies.getList.invoke(null);
			int y = 0;
			Planet cp = colonies.getCurrent.invoke(null);
			int idx = list.indexOf(cp); 
			if (idx < top) {
				top = idx;
			} else
			if (idx >= top + 26) {
				top = idx - 26;
			}
			g2.setColor(Color.BLACK);
			g2.fillRect(101, - 13, 300, 13);
			
			commons.text().paintTo(g2, 105, -13, 10, TextRenderer.YELLOW, get("info.population_details"));
			commons.text().paintTo(g2, 240, -13, 10, TextRenderer.YELLOW, get("info.morale_details"));
			commons.text().paintTo(g2, 320, -13, 10, TextRenderer.YELLOW, get("info.problem_details"));

			for (int i = top; i < list.size() && i < top + 27; i++) {
				Planet p = list.get(i);
				
				String name = colonies.getText.invoke(p);
				int color = colonies.getColor.invoke(p);
				
				commons.text().paintTo(g2, 0, y + 1, 10, color, name);
				if (p == cp) {
					g2.setColor(new Color(TextRenderer.ORANGE));
					g2.drawRect(-2, y - 1, 101, 13);
				}
				
				if (p.owner == player() || knowledge(p, PlanetKnowledge.BUILDING) >= 0) {
					String pop = Integer.toString(p.population);
					int popWidth = commons.text().getTextWidth(10, pop);
					commons.text().paintTo(g2, 160 - popWidth, y + 1, 10, TextRenderer.GREEN, pop);
					commons.text().paintTo(g2, 170, y + 1, 10, TextRenderer.GREEN, "(" + withSign(p.population - p.lastPopulation) + ")");
					 
					if (p.owner == player()) {

						commons.text().paintTo(g2, 240, y + 1, 10, p.morale >= 30 ? TextRenderer.GREEN : TextRenderer.RED, p.morale + "% (" + withSign(p.morale - p.lastMorale) + ")");
						
						PlanetStatistics ps = p.getStatistics();
						if (ps.problems.size() > 0) {
							int j = 0;
							for (PlanetProblems pp : ps.problems.values()) {
								BufferedImage icon = null;
								switch (pp) {
								case HOUSING:
									icon = commons.common().houseIcon;
									break;
								case FOOD:
									icon = commons.common().foodIcon;
									break;
								case HOSPITAL:
									icon = commons.common().hospitalIcon;
									break;
								case ENERGY:
									icon = commons.common().energyIcon;
									break;
								case WORKFORCE:
									icon = commons.common().workerIcon;
									break;
								case STADIUM:
									icon = commons.common().virusIcon;
									break;
								case VIRUS:
									icon = commons.common().stadiumIcon;
									break;
								case REPAIR:
									icon = commons.common().repairIcon;
									break;
								default:
								}
								g2.drawImage(icon, 320 + j * 11, y + 2, null);
								j++;
							}
						}
					}
				}
				y += 13;
			}
		}
		@Override
		public boolean mouse(UIMouse e) {
			if (e.has(Type.WHEEL)) {
				List<Planet> list = colonies.getList.invoke(null);
				if (list.size() > 0) {
					Planet cp = colonies.getCurrent.invoke(null);
					int idx = list.indexOf(cp); 
					if (e.z < 0) {
						idx = Math.max(0, idx - 1);
					} else {
						idx = Math.min(list.size() - 1, idx + 1);
					}
					colonies.onSelect.invoke(list.get(idx));
					return true;
				}
			} else
			if (e.has(Type.DOWN)) {
				List<Planet> list = colonies.getList.invoke(null);
				int idx = e.y / 13 + top;
				if (idx >= 0 && idx < list.size()) {
					colonies.onSelect.invoke(list.get(idx));
					return true;
				}
			} else
			if (e.has(Type.DOUBLE_CLICK)) {
				List<Planet> list = colonies.getList.invoke(null);
				int idx = e.y / 13 + top;
				if (idx >= 0 && idx < list.size()) {
					colonies.onDoubleClick.invoke(list.get(idx));
					return true;
				}
			}
			
				
			return false;
		}
	}
	@Override
	public void onEndGame() {
		// TODO Auto-generated method stub
		
	}
}
