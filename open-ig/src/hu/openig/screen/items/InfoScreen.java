/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.core.Action0;
import hu.openig.core.Action1;
import hu.openig.core.Func1;
import hu.openig.core.Pair;
import hu.openig.model.AutoBuild;
import hu.openig.model.Building;
import hu.openig.model.BuildingType;
import hu.openig.model.DiplomaticRelation;
import hu.openig.model.Fleet;
import hu.openig.model.FleetKnowledge;
import hu.openig.model.FleetMode;
import hu.openig.model.FleetStatistics;
import hu.openig.model.Named;
import hu.openig.model.Owned;
import hu.openig.model.Planet;
import hu.openig.model.PlanetKnowledge;
import hu.openig.model.PlanetListMode;
import hu.openig.model.PlanetProblems;
import hu.openig.model.PlanetStatistics;
import hu.openig.model.Player;
import hu.openig.model.Research;
import hu.openig.model.ResearchMainCategory;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.ResearchType;
import hu.openig.model.Screens;
import hu.openig.model.SelectionMode;
import hu.openig.model.SoundType;
import hu.openig.model.TaxLevel;
import hu.openig.model.TileSet;
import hu.openig.model.Trait;
import hu.openig.model.TraitKind;
import hu.openig.render.RenderTools;
import hu.openig.render.TextRenderer;
import hu.openig.screen.ScreenBase;
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
import hu.openig.ui.UITextButton;
import hu.openig.ui.VerticalAlignment;
import hu.openig.utils.Exceptions;
import hu.openig.utils.U;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.io.Closeable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;



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
			Screens.INFORMATION_MILITARY,
			Screens.INFORMATION_ALIENS 
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
			Screens.INFORMATION_ALIENS
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
			Screens.INFORMATION_ALIENS
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
			Screens.INFORMATION_ALIENS
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
			Screens.INFORMATION_ALIENS
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
			Screens.INFORMATION_ALIENS
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
			Screens.INFORMATION_ALIENS
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
			Screens.INFORMATION_ALIENS
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
			Screens.INFORMATION_ALIENS
	})
	UIImage problemsRepair;
	/** Problem indicator icon. */
	@ModeUI(mode = { 
			Screens.INFORMATION_BUILDINGS,
			Screens.INFORMATION_PLANETS,
			Screens.INFORMATION_COLONY,
			Screens.INFORMATION_MILITARY,
			Screens.INFORMATION_FINANCIAL,
			Screens.INFORMATION_BUILDINGS,
			Screens.INFORMATION_ALIENS
	})
	UIImage problemsColonyHub;
	/** Problem indicator icon. */
	@ModeUI(mode = { 
			Screens.INFORMATION_BUILDINGS,
			Screens.INFORMATION_PLANETS,
			Screens.INFORMATION_COLONY,
			Screens.INFORMATION_MILITARY,
			Screens.INFORMATION_FINANCIAL,
			Screens.INFORMATION_BUILDINGS,
			Screens.INFORMATION_ALIENS
	})
	UIImage problemsPolice;
	/** Problem indicator icon. */
	@ModeUI(mode = { 
			Screens.INFORMATION_BUILDINGS,
			Screens.INFORMATION_PLANETS,
			Screens.INFORMATION_COLONY,
			Screens.INFORMATION_MILITARY,
			Screens.INFORMATION_FINANCIAL,
			Screens.INFORMATION_BUILDINGS,
			Screens.INFORMATION_ALIENS
	})
	UIImage problemsFireBrigade;
	/** The current planet's owner. */
	@ModeUI(mode = { 
			Screens.INFORMATION_MILITARY,
			Screens.INFORMATION_FINANCIAL,
			Screens.INFORMATION_COLONY, 
			Screens.INFORMATION_PLANETS,
			Screens.INFORMATION_ALIENS 
			})
	UILabel colonyOwner;
	/** The current planet's race. */
	@ModeUI(mode = { 
			Screens.INFORMATION_MILITARY,
			Screens.INFORMATION_FINANCIAL,
			Screens.INFORMATION_COLONY, 
			Screens.INFORMATION_PLANETS,
			Screens.INFORMATION_ALIENS
			})
	UILabel colonyRace;
	/** The current planet's surface. */
	@ModeUI(mode = { 
			Screens.INFORMATION_MILITARY,
			Screens.INFORMATION_FINANCIAL,
			Screens.INFORMATION_COLONY, 
			Screens.INFORMATION_PLANETS,
			Screens.INFORMATION_ALIENS 
			})
	UILabel colonySurface;
	/** The current planet's population. */
	@ModeUI(mode = { 
			Screens.INFORMATION_MILITARY,
			Screens.INFORMATION_FINANCIAL,
			Screens.INFORMATION_COLONY, 
			Screens.INFORMATION_PLANETS,
			Screens.INFORMATION_ALIENS 
			})
	UILabel colonyPopulation;
	/** The current planet's taxation. */
	@ModeUI(mode = { 
			Screens.INFORMATION_MILITARY,
			Screens.INFORMATION_FINANCIAL,
			Screens.INFORMATION_COLONY, 
			Screens.INFORMATION_PLANETS,
			Screens.INFORMATION_ALIENS 
			})
	UILabel colonyTax;
	/** Other things with the planet. */
	@ModeUI(mode = { 
			Screens.INFORMATION_MILITARY,
			Screens.INFORMATION_FINANCIAL,
			Screens.INFORMATION_COLONY, 
			Screens.INFORMATION_PLANETS,
			Screens.INFORMATION_ALIENS
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
	final List<UILabel> researchLabs = new ArrayList<>();
	/** The available research labs. */
	@ModeUI(mode = { 
			Screens.INFORMATION_INVENTIONS
	})
	final List<UILabel> researchAvailable = new ArrayList<>();
	/** The military info panel. */
	@ModeUI(mode = { 
			Screens.INFORMATION_MILITARY
	})
	MilitaryInfoPanel militaryInfo;
	/** The planet list details. */
	@ModeUI(mode = { 
			Screens.INFORMATION_PLANETS
	})
	PlanetListDetails planetListDetails;
	/** The toggle for planet list details. */
	boolean showPlanetListDetails = true;
	/** The planet list mode. */
	PlanetListMode planetListMode = PlanetListMode.PROBLEMS;
	/** Toggle button for the planet list mode. */
	@ModeUI(mode = { 
			Screens.INFORMATION_PLANETS
	})
	UIGenericButton togglePlanetListMode;
	/** Toggle the planet list details view. */
	@ModeUI(mode = { 
			Screens.INFORMATION_PLANETS
	})
	UIGenericButton togglePlanetListDetails;
	/** The general statistics button. */
	@ModeUI(mode = { 
			Screens.INFORMATION_FINANCIAL
	})
	UIGenericButton statisticsButton;
	/** The diplomacy panel. */
	@ModeUI(mode = { 
			Screens.INFORMATION_ALIENS
	})
	DiplomacyPanel diplomacyPanel;
	/** The animation timer. */
	Closeable animation;
	/** The blink state. */
	boolean animationBlink;
	/** Fleet property. */
	@ModeUI(mode = { 
			Screens.INFORMATION_FLEETS
	})
	UILabel fleetName;
	/** Fleet property. */
	@ModeUI(mode = { 
			Screens.INFORMATION_FLEETS
	})
	UILabel fleetOwner;
	/** Fleet property. */
	@ModeUI(mode = { 
			Screens.INFORMATION_FLEETS
	})
	UILabel fleetStatus;
	/** Fleet property. */
	@ModeUI(mode = { 
			Screens.INFORMATION_FLEETS
	})
	UILabel fleetPlanetLabel;
	/** Fleet property. */
	@ModeUI(mode = { 
			Screens.INFORMATION_FLEETS
	})
	UILabel fleetPlanet;
	/** Fleet property. */
	@ModeUI(mode = { 
			Screens.INFORMATION_FLEETS
	})
	UILabel fleetSpeed;
	/** Fleet property. */
	@ModeUI(mode = { 
			Screens.INFORMATION_FLEETS
	})
	UILabel fleetFirepower;
	/** Fleet property. */
	@ModeUI(mode = { 
			Screens.INFORMATION_FLEETS
	})
	UILabel fleetBattleships;
	/** Fleet property. */
	@ModeUI(mode = { 
			Screens.INFORMATION_FLEETS
	})
	UILabel fleetCruisers;
	/** Fleet property. */
	@ModeUI(mode = { 
			Screens.INFORMATION_FLEETS
	})
	UILabel fleetFighters;
	/** Fleet property. */
	@ModeUI(mode = { 
			Screens.INFORMATION_FLEETS
	})
	UILabel fleetVehicles;
	/** The global statistics used in some screens. */
	private PlanetStatistics globalStatistics;
	/** Go to next planet. */
	@ModeUI(mode = { 
			Screens.INFORMATION_COLONY, Screens.INFORMATION_MILITARY, Screens.INFORMATION_FINANCIAL
	})
	UIImageButton prevPlanet;
	/** Go to previous planet. */
	@ModeUI(mode = { 
			Screens.INFORMATION_COLONY, Screens.INFORMATION_MILITARY, Screens.INFORMATION_FINANCIAL
	})
	UIImageButton nextPlanet;
	/** The local planet statistics. */
	PlanetStatistics localStatistics;
	@Override
	public void onInitialize() {
		base.setBounds(0, 0, 
				commons.info().baseTop.getWidth(), 
				commons.info().baseLeft.getHeight());
		
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
		colony.onClick = new Action0() {
			@Override
			public void invoke() {
				displayPrimary(Screens.COLONY);
			}
		};
		research = new UIImageButton(commons.info().research);
		research.onClick = new Action0() {
			@Override
			public void invoke() {
				displaySecondary(Screens.RESEARCH);
			}
		};
		production = new UIImageButton(commons.info().production);
		production.onClick = new Action0() {
			@Override
			public void invoke() {
				displaySecondary(Screens.PRODUCTION);
			}
		};
		starmap = new UIImageButton(commons.info().starmap);
		starmap.onClick = new Action0() {
			@Override
			public void invoke() {
				displayPrimary(Screens.STARMAP);
			}
		};
		diplomacy = new UIImageButton(commons.info().diplomacy);
		diplomacy.onClick = new Action0() {
			@Override
			public void invoke() {
				displaySecondary(Screens.DIPLOMACY);
			}
		};
		equipment = new UIImageButton(commons.info().equipment);
		equipment.onClick = new Action0() {
			@Override
			public void invoke() {
				displaySecondary(Screens.EQUIPMENT);
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
		
		colonies = new Listing<>();
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
				return value.name();
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
				buttonSound(SoundType.CLICK_MEDIUM_2);
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
		
		
		fleets = new Listing<>();
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
				if (knowledge(value, FleetKnowledge.VISIBLE) > 0) {
					return value.name;
				}
				return get("fleetinfo.alien_fleet");
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
				buttonSound(SoundType.CLICK_MEDIUM_2);
				player().currentFleet = value;
			}
		};
		fleets.onDoubleClick = new Action1<Fleet>() {
			@Override
			public void invoke(Fleet value) {
				player().currentFleet = value;
				player().selectionMode = SelectionMode.FLEET;
				displaySecondary(Screens.EQUIPMENT);
			}
		};
		
		buildings = new BuildingListing(
            new Action1<BuildingType>() {
                @Override
                public void invoke(BuildingType value) {
                    buttonSound(SoundType.CLICK_MEDIUM_2);
                    player().currentBuilding = value;
                    if (value.research != null) {
                        research(value.research);
                    }
                    displayBuildingInfo();
                }
            },
            new Action1<BuildingType>() {
                @Override
                public void invoke(BuildingType value) {
                    player().currentBuilding = value;
                    if (value.research != null) {
                        research(value.research);
                    }
                    displayBuildingInfo();
                    if (planet().canBuild(value)) {
                        PlanetScreen ps = (PlanetScreen)displayPrimary(Screens.COLONY);
                        ps.buildingsPanel.build.onPress.invoke();
                    }
                }
            }
        );
		
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
		problemsColonyHub = new UIImage(commons.common().colonyHubIcon);
		problemsPolice = new UIImage(commons.common().policeIcon);
		problemsFireBrigade = new UIImage(commons.common().fireBrigadeIcon);

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

		researchPre1.onPress = new Action0() {
			@Override
			public void invoke() {
				ResearchType rt = research();
				if (rt != null && rt.prerequisites.size() > 0) {
					world().selectResearch(rt.prerequisites.get(0));
				}
			}
		};
		researchPre2.onPress = new Action0() {
			@Override
			public void invoke() {
				ResearchType rt = research();
				if (rt != null && rt.prerequisites.size() > 1) {
					world().selectResearch(rt.prerequisites.get(1));
				}
			}
		};
		researchPre3.onPress = new Action0() {
			@Override
			public void invoke() {
				ResearchType rt = research();
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
		
		planetListDetails = new PlanetListDetails();
		togglePlanetListDetails = new UIGenericButton(get("info.hide_details"), commons.control().fontMetrics(14), commons.common().mediumButton, commons.common().mediumButtonPressed);

		togglePlanetListDetails.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_HIGH_2);
				showPlanetListDetails = !showPlanetListDetails;
				if (showPlanetListDetails) {
					togglePlanetListDetails.text(get("info.hide_details"), true);
				} else {
					togglePlanetListDetails.text(get("info.list_details"), true);
				}
				togglePlanetListMode.visible(showPlanetListDetails);
				colonies.visible(!showPlanetListDetails);
				planetListDetails.visible(showPlanetListDetails);
				adjustPlanetListView();
			}
		};
		togglePlanetListMode = new UIGenericButton(get("info.planetlist_labs"), commons.control().fontMetrics(14), commons.common().mediumButton, commons.common().mediumButtonPressed);
		togglePlanetListMode.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_HIGH_2);
				int n = PlanetListMode.values().length;
				planetListMode = PlanetListMode.values()[(planetListMode.ordinal() + 1) % n];

				switch (planetListMode) {
				case LABS:
					togglePlanetListMode.text(get("info.planetlist_problems"), true);
					break;
				default:
					togglePlanetListMode.text(get("info.planetlist_labs"), true);
				}
					
				adjustPlanetListView();
				onResize();
			}
		};
		
		statisticsButton = new UIGenericButton(get("info.statistics"), commons.control().fontMetrics(14), commons.common().mediumButton, commons.common().mediumButtonPressed);
		statisticsButton.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_HIGH_2);
				displaySecondary(Screens.STATISTICS);
			}
		};
		
		diplomacyPanel = new DiplomacyPanel();

		fleetName = new UILabel("", 14, commons.text());
		fleetOwner = new UILabel("", 10, commons.text());
		fleetStatus = new UILabel("", 10, commons.text());
		fleetPlanetLabel = new UILabel(format("fleetstatus.nearby", ""), 10, commons.text());
		fleetPlanet = new UILabel("", 10, commons.text());
		fleetSpeed = new UILabel("", 7, commons.text());
		fleetFirepower = new UILabel("", 7, commons.text());
		fleetBattleships = new UILabel("", 7, commons.text());
		fleetCruisers = new UILabel("", 7, commons.text());
		fleetFighters = new UILabel("", 7, commons.text());
		fleetVehicles = new UILabel("", 7, commons.text());
		
		prevPlanet = new UIImageButton(commons.starmap().backwards);
		prevPlanet.z = 2;
		prevPlanet.setHoldDelay(250);
		prevPlanet.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_HIGH_2);
				player().movePrevPlanet();
			}
		};
		nextPlanet = new UIImageButton(commons.starmap().forwards);
		nextPlanet.z = 2;
		nextPlanet.setHoldDelay(250);
		nextPlanet.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_HIGH_2);
				player().moveNextPlanet();
			}
		};

		addThis();
	}
	@Override
	public void onResize() {
		base.setBounds(0, 0, 
				commons.info().baseTop.getWidth(), 
				commons.info().baseLeft.getHeight());
		scaleResize(base, margin());
		
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
		
		int probcount = 0;
		problemsHouse.location(base.x + 420 + (11 * probcount++), buildingWorker.y + 62 + 17 * 4);
		problemsEnergy.location(base.x + 420 + (11 * probcount++), buildingWorker.y + 62  + 17 * 4);
		problemsWorker.location(base.x + 420 + (11 * probcount++), buildingWorker.y + 62 + 17 * 4);
		problemsFood.location(base.x + 420 + (11 * probcount++), buildingWorker.y + 62 + 17 * 4);
		problemsHospital.location(base.x + 420 + (11 * probcount++), buildingWorker.y + 62 + 17 * 4);
		problemsVirus.location(base.x + 420 + (11 * probcount++), buildingWorker.y + 62 + 17 * 4);
		problemsStadium.location(base.x + 420 + (11 * probcount++), buildingWorker.y + 62 + 17 * 4);
		problemsRepair.location(base.x + 420 + (11 * probcount++), buildingWorker.y + 62 + 17 * 4);
		problemsColonyHub.location(base.x + 420 + (11 * probcount++), buildingWorker.y + 62 + 17 * 4);
		problemsPolice.location(base.x + 420 + (11 * probcount++), buildingWorker.y + 62 + 17 * 4);
		problemsFireBrigade.location(base.x + 420 + (11 * probcount), buildingWorker.y + 62 + 17 * 4);

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
		
		planetListDetails.bounds(base.x + 10, base.y + 10, 400, 27 * 13);
		togglePlanetListDetails.location(base.x + 420, colonyOther.y + colonyOther.height + 5);
		
		togglePlanetListMode.location(base.x + 610 - togglePlanetListMode.width, colonyOther.y + colonyOther.height + 5);
		
		statisticsButton.location(togglePlanetListDetails.location());
		
		militaryInfo.bounds(base.x + 10, base.y + 10, 400, 27 * 13);
		
		diplomacyPanel.bounds(base.x + 10, base.y + 10, 400, 27 * 13);
		
		fleetName.bounds(planetTitle.bounds());
		fleetOwner.location(colonyOwner.location());
		fleetStatus.location(fleetOwner.x, fleetOwner.y + 17);
		fleetPlanetLabel.location(fleetOwner.x, fleetOwner.y + 17 * 2);
		fleetPlanet.location(fleetOwner.x + 10, fleetOwner.y + 17 * 3);
		fleetSpeed.location(fleetOwner.x, fleetOwner.y + 17 * 4);
		fleetFirepower.location(fleetOwner.x, fleetOwner.y + 17 * 4 + 10);
		fleetBattleships.location(fleetOwner.x, fleetOwner.y + 17 * 4 + 10 * 3);
		fleetCruisers.location(fleetOwner.x, fleetOwner.y + 17 * 4 + 10 * 4);
		fleetFighters.location(fleetOwner.x, fleetOwner.y + 17 * 4 + 10 * 5);
		fleetVehicles.location(fleetOwner.x, fleetOwner.y + 17 * 4 + 10 * 6);

		nextPlanet.location(base.x + 410 - nextPlanet.width, base.y + 5);
		prevPlanet.location(nextPlanet.x - prevPlanet.width - 2, base.y + 5);
		
	}
	@Override
	public void draw(Graphics2D g2) {
		AffineTransform savea = scaleDraw(g2, base, margin());
		
		RenderTools.darkenAround(base, width, height, g2, 0.5f, true);
		commons.info().drawInfoPanel(g2, base.x, base.y);

		localStatistics = planet().getStatistics();
		
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
		} else
		if (mode == Screens.INFORMATION_ALIENS) {
			displayPlanetInfo();
		} else
		if (mode == Screens.INFORMATION_FLEETS) {
			displayFleetInfo();
		}
		update();
		
		super.draw(g2);
		
		g2.setTransform(savea);
	}
	/** Update some general properties. */
	void update() {
		Pair<Planet, Planet> pn = player().prevNextPlanet();
		if (pn != null) {
			setTooltip(prevPlanet, "colony.prev.tooltip", pn.first.owner.color, pn.first.name());
			setTooltip(nextPlanet, "colony.next.tooltip", pn.second.owner.color, pn.second.name());
		} else {
			setTooltip(prevPlanet, null);
			setTooltip(nextPlanet, null);
		}
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
		button.onPress = new Action0() {
			@Override
			public void invoke() {
				switchToTab(mode);
			}

		};
		return button;
	}
	/**
	 * Switch to the given tab.
	 * @param mode the mode
	 */
	void switchToTab(final Screens mode) {
		if (mode != this.mode) {
			this.mode = mode;
			if (config.computerVoiceScreen) {
				switch (mode) {
				case INFORMATION_PLANETS:
					effectSound(SoundType.INFORMATION_PLANETS);
					break;
				case INFORMATION_COLONY:
					effectSound(SoundType.INFORMATION_COLONY);
					break;
				case INFORMATION_MILITARY:
					effectSound(SoundType.INFORMATION_MILITARY);
					break;
				case INFORMATION_FINANCIAL:
					effectSound(SoundType.INFORMATION_FINANCIAL);
					break;
				case INFORMATION_FLEETS:
					effectSound(SoundType.INFORMATION_FLEETS);
					break;
				case INFORMATION_BUILDINGS:
					effectSound(SoundType.INFORMATION_BUILDINGS);
					break;
				case INFORMATION_INVENTIONS:
					effectSound(SoundType.INFORMATION_INVENTIONS);
					break;
				case INFORMATION_ALIENS:
					effectSound(SoundType.INFORMATION_ALIENS);
					break;
				default:
				}
			}
			applyMode();
		}
	}
	@Override
	public void onEnter(Screens mode) {
		if (mode == Screens.INFORMATION_ALIENS && world().level < 4) {
			mode = Screens.INFORMATION_PLANETS;
		}
		this.mode = mode != null ? mode : Screens.INFORMATION_PLANETS;
		applyMode();
		animation = commons.register(500, new Action0() {
			@Override
			public void invoke() {
				animationBlink = !animationBlink;
				scaleRepaint(base, base, margin());
			}
		});
		adjustPlanetListView();
	}
	/** Adjust the visibility of fields and buttons. */
	void applyMode() {
		setUIVisibility();
		production.visible(production.visible() && world().level >= 2);
		research.visible(research.visible() && world().level >= 3);
		diplomacy.visible(diplomacy.visible() && world().getShip().positions.containsKey("*diplomacy"));
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
			planetListDetails.visible(showPlanetListDetails);
			colonies.visible(!showPlanetListDetails);
			adjustPlanetListView();
		}
		
		if (mode == Screens.INFORMATION_INVENTIONS) {
			ResearchType rt = research();
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
		close0(animation);
		animation = null;
	}

	@Override
	public void onFinish() {
		// nothing to do here
	}

	@Override
	public boolean mouse(UIMouse e) {
		scaleMouse(e, base, margin());
		if (e.has(Type.DOWN)) {
			if (showPlanetListDetails) {
				String s1 = get("info.planet_name");
				int w1 = commons.text().getTextWidth(10, s1);
				String s2 = get("info.population_details");
				int w2 = commons.text().getTextWidth(10, s2);
				String s3 = get("info.morale_details");
				int w3 = commons.text().getTextWidth(10, s3);
				String s4 = get("info.problem_details");
				int w4 = commons.text().getTextWidth(10, s4);
				String s5 = get("info.lab_details");
				int w5 = commons.text().getTextWidth(10, s5);
				
				if (e.within(planetListDetails.x + 10, planetListDetails.y - 13, w1 + 12, 12)) {
					if (planetListDetails.sortBy != 0 || !planetListDetails.ascending) {
						planetListDetails.ascending = true;
						planetListDetails.sortBy = 0;
					} else {
						planetListDetails.ascending = false;
					}
				} else
				if (e.within(planetListDetails.x + 105, planetListDetails.y - 13, w2 + 12, 12)) {
					if (planetListDetails.sortBy != 1 || !planetListDetails.ascending) {
						planetListDetails.ascending = true;
						planetListDetails.sortBy = 1;
					} else {
						planetListDetails.ascending = false;
					}
				} else
				if (e.within(planetListDetails.x + 240, planetListDetails.y - 13, w3 + 12, 12)) {
					if (planetListDetails.sortBy != 2 || !planetListDetails.ascending) {
						planetListDetails.ascending = true;
						planetListDetails.sortBy = 2;
					} else {
						planetListDetails.ascending = false;
					}
				} else
				if (e.within(planetListDetails.x + 310, planetListDetails.y - 13, w4 + 12, 12) && planetListMode == PlanetListMode.PROBLEMS) {
					if (planetListDetails.sortBy != 3 || !planetListDetails.ascending) {
						planetListDetails.ascending = true;
						planetListDetails.sortBy = 3;
					} else {
						planetListDetails.ascending = false;
					}
				} else
				if (e.within(planetListDetails.x + 310, planetListDetails.y - 13, w5 + 12, 12) && planetListMode == PlanetListMode.LABS) {
					if (planetListDetails.sortBy != 4 || !planetListDetails.ascending) {
						planetListDetails.ascending = true;
						planetListDetails.sortBy = 4;
					} else {
						planetListDetails.ascending = false;
					}
				} else {
					if (!base.contains(e.x, e.y)) {
						hideSecondary();
					} else {
						return super.mouse(e);
					}
				}
			} else {
				if (!base.contains(e.x, e.y)) {
					hideSecondary();
				} else {
					return super.mouse(e);
				}
			}
			return true;
		}
		return super.mouse(e);
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
							c.visible(U.contains(mode, mi.mode()));
						}
					} catch (IllegalAccessException ex) {
						Exceptions.add(ex);
					}
				} else
				if (Iterable.class.isAssignableFrom(f.getType())) {
					try {
						Iterable<?> it = Iterable.class.cast(f.get(this));
						if (it != null) {
                            for (Object o : it) {
                                if (UIComponent.class.isAssignableFrom(o.getClass())) {
                                    UIComponent c = UIComponent.class.cast(o);
                                    c.visible(U.contains(mode, mi.mode()));
                                }
                            }
						}
					} catch (IllegalAccessException ex) {
						Exceptions.add(ex);
					}
				}
			}
		}
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
		/** Label field. */
		UILabel needed;
		/** The labels. */
		List<UILabel> lines;
		/** More tax. */
		UIImageButton taxMore;
		/** Tax more for all? */
		boolean taxMoreAll;
		/** Less tax. */
		UIImageButton taxLess;
		/** Tax less for all? */
		boolean taxLessAll;
		/** Set the tax level on all of the player's planet to the current. */
		UITextButton taxAll;
		/** Change to the previous auto-build settings. */
		UITextButton autoPrev;
		/** Change to the next autobuild settings. */
		UITextButton autoNext;
		/** Change the autobuild for all planets to the current. */
		UITextButton autoAll;
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
			autobuild = new UILabel("-", textSize, commons.text()) {
				@Override
				public boolean mouse(UIMouse e) {
					if (e.has(Type.DOWN)) {
						doAutoBuild(e.has(Modifier.SHIFT));
						return true;
					}
					return super.mouse(e);
				}
            };
			other = new UILabel("-", textSize, commons.text());
			other.wrap(true);
			other.vertically(VerticalAlignment.TOP);
			other.size(397, (textSize + 2) * 3);
			
			needed = new UILabel("-", textSize, commons.text());
			needed.wrap(true);
			needed.vertically(VerticalAlignment.TOP);
			needed.size(397, (textSize + 2) * 3);
			needed.color(TextRenderer.RED);
			
			lines = Arrays.asList(
					owner, race, surface, population, housing, worker, hospital, food, energy, police,
					taxIncome, tradeIncome, taxMorale, taxLevel, allocation, autobuild, other, needed
			);

			taxMore = new UIImageButton(commons.info().taxMore) {
				@Override
				public boolean mouse(UIMouse e) {
					taxMoreAll = e.has(Modifier.SHIFT);
					return super.mouse(e);
				}
            };
			taxMore.onClick = new Action0() {
				@Override
				public void invoke() {
					buttonSound(SoundType.CLICK_HIGH_2);
					doTaxMore();
				}
			};
			taxMore.setHoldDelay(150);
			
			taxMore.z = 1;
			taxLess = new UIImageButton(commons.info().taxLess) {
				@Override
				public boolean mouse(UIMouse e) {
					taxLessAll = e.has(Modifier.SHIFT);
					return super.mouse(e);
				}
            };
			taxLess.onClick = new Action0() {
				@Override
				public void invoke() {
					buttonSound(SoundType.CLICK_HIGH_2);
					doTaxLess();
				}
			};
			taxLess.setHoldDelay(150);
			taxLess.z = 1;
			taxMore.setDisabledPattern(commons.common().disabledPattern);
			taxLess.setDisabledPattern(commons.common().disabledPattern);

			taxAll = new UITextButton(get("infoscreen.all"), 10, commons.text());
			taxAll.onClick = new Action0() {
				@Override
				public void invoke() {
					buttonSound(SoundType.CLICK_HIGH_2);
					doSetTaxAll();
				}
			};
			taxAll.z = 1;
			
			autoPrev = new UITextButton("-", 10, commons.text());
			autoPrev.onClick = new Action0() {
				@Override
				public void invoke() {
					buttonSound(SoundType.CLICK_HIGH_2);
					doAutoPrev();
				}
			};
			autoPrev.z = 1;
			
			autoNext = new UITextButton("+", 10, commons.text());
			autoNext.onClick = new Action0() {
				@Override
				public void invoke() {
					buttonSound(SoundType.CLICK_HIGH_2);
					doAutoNext();
				}
			};
			autoNext.z = 1;
			
			autoAll = new UITextButton(get("infoscreen.all"), 10, commons.text());
			autoAll.onClick = new Action0() {
				@Override
				public void invoke() {
					buttonSound(SoundType.CLICK_HIGH_2);
					doSetAutoAll();
				}
			};
			autoAll.z = 1;
			
			addThis();
		}
		/** Set tax level on all planets to the same. */
		void doSetTaxAll() {
			Planet p = planet();
			for (Planet q : player().ownPlanets()) {
				q.tax = p.tax;
			}
		}
		/** Set autobuild level on all planets to the same. */
		void doSetAutoAll() {
			Planet p = planet();
			for (Planet q : player().ownPlanets()) {
				q.autoBuild = p.autoBuild;
			}
		}
		/** Select previous auto-build settings or wrap around. */
		void doAutoPrev() {
			int a = planet().autoBuild.ordinal() - 1;
			if (a < 0) {
				a = AutoBuild.values().length - 1;
			}
			planet().autoBuild = AutoBuild.values()[a];
		}
		/** Select next autobuild settings or wrap around. */
		void doAutoNext() {
			int a = planet().autoBuild.ordinal() + 1;
			if (a >= AutoBuild.values().length) {
				a = 0;
			}
			planet().autoBuild = AutoBuild.values()[a];
		}
		/** Compute the panel size based on its visible component sizes. */
		public void computeSize() {
			int textSize = 10;
			int w = 0;
			int h = 0;
			int dy = 25;
			for (UILabel c : lines) {
				if (c.visible()) {
					c.x = 10;
					c.y = dy;
					c.size(textSize);
					w = Math.max(w, c.x + c.width);
					h = Math.max(h, c.y + c.height);
					dy += c.height + 5;
				}
			}
			w = Math.max(w, this.planet.x + this.planet.width);
			
//			taxLess.location(taxLevel.x + 240, taxLevel.y + (taxLevel.height - taxLess.height) / 2);
			taxLess.location(taxIncome.x + 240, taxIncome.y);
			taxMore.location(taxLess.x + taxLess.width + 5, taxLess.y);
			
			w = Math.max(w, taxMore.x + taxMore.width);
			
			width = w + 10;
			height = h + 5;
			
			taxAll.location(taxMore.x + taxMore.width - taxAll.width, taxLevel.y - 5);
			autoAll.location(taxAll.x, autobuild.y - 5);
			autoPrev.location(autoAll.x - 3 - autoPrev.width, autoAll.y);
			autoNext.location(autoPrev.x - 3 - autoNext.width, autoAll.y);
			other.y += 2;
			needed.y += 2;
		}
		/**
		 * Update the display values based on the current planet's settings.
		 */
		public void update() {
			Planet p = planet();
			
			if (p == null) {
				return;
			}
			
			planet.text(p.name(), true);
			
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
			String surfaceText = format("colonyinfo.surface", firstUpper(get(p.type.label)));
			if (p.owner == null && knowledge(p, PlanetKnowledge.OWNER) >= 0) {
				double g = world().galaxyModel.getGrowth(p.type.type, player().race);
				
				Trait t = player().traits.trait(TraitKind.FERTILE);
				if (t != null) {
					g *= 1 + t.value / 100;
				}
				
				surfaceText = format("colonyinfo.surface2", 
						firstUpper(get(p.type.label)), (int)(g * 100));
			} else
			if (p.owner == player()) {
				double g = world().galaxyModel.getGrowth(p.type.type, p.race) * localStatistics.populationGrowthModifier;

				Trait t = player().traits.trait(TraitKind.FERTILE);
				if (t != null) {
					g *= 1 + t.value / 100;
				}
				
				surfaceText = format("colonyinfo.surface2", 
						firstUpper(get(p.type.label)), (int)(g * 100));
			}
			surface.text(surfaceText, true);

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
			needed.visible(false);
			taxLess.visible(false);
			taxMore.visible(false);
			taxAll.visible(false);
			autoAll.visible(false);
			autoNext.visible(false);
			autoPrev.visible(false);

			if (p.isPopulated()) {
				if (knowledge(p, PlanetKnowledge.BUILDING) >= 0) {
					if (p.owner == player()) {
						population.text(format("colonyinfo.population", 
								(int)p.population(), get(p.getMoraleLabel()), withSign((int)p.population() - (int)p.lastPopulation())
						), true).visible(true);
					} else {
						population.text(format("colonyinfo.population.alien", 
								(int)p.population()
						), true).visible(true);
					}
				}
				if (p.owner == player()) {
					PlanetStatistics ps = localStatistics;
					setLabel(housing, "colonyinfo.housing", ps.houseAvailable, (int)p.population()).visible(true);
					setLabel(worker, "colonyinfo.worker", (int)p.population(), ps.workerDemand).visible(true);
					setLabel(hospital, "colonyinfo.hospital", ps.hospitalAvailable, (int)p.population()).visible(true);
					setLabel(food, "colonyinfo.food", ps.foodAvailable, (int)p.population()).visible(true);
					setLabel(energy, "colonyinfo.energy", ps.energyAvailable, ps.energyDemand).visible(true);
					setLabel(police, "colonyinfo.police", ps.policeAvailable, (int)p.population()).visible(true);
					
					taxIncome.text(format("colonyinfo.tax", 
							(int)p.taxIncome()
					), true).visible(true);
					tradeIncome.text(format("colonyinfo.trade",
							(int)p.tradeIncome()
					), true).visible(true);
					
					taxMorale.text(format("colonyinfo.tax-morale",
							(int)p.morale, withSign((int)p.morale - (int)p.lastMorale)
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
					if (p.autoBuild != AutoBuild.OFF) {
						autobuild.color(TextRenderer.YELLOW);
						autobuild.tooltip(get("autobuild.tooltip." + p.autoBuild));
					} else {
						autobuild.color(TextRenderer.GREEN);
					}
					
					doAdjustTaxButtons();
					taxLess.visible(true);
					taxMore.visible(true);
					taxAll.visible(true);
					autoAll.visible(true);
					autoNext.visible(true);
					autoPrev.visible(true);
					
					String nd = world().getNeeded(ps);
					if (!nd.isEmpty()) {
						needed.text(format("colonyinfo.needed",
								nd
						)).visible(true);
					}

				}
			}
			
			String oi = world().getOtherItems();
			other.text(format("colonyinfo.other",
					oi.isEmpty() ? "-" : oi
			));

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
	/** Increase the taxation level. */
	void doTaxMore() {
		Planet p = planet();
		if (p != null) {
			TaxLevel l = p.tax;
			if (l.ordinal() < TaxLevel.values().length - 1) {
				p.tax = TaxLevel.values()[l.ordinal() + 1];
			}
			if (colonyInfo.taxMoreAll) {
				for (Planet q : player().ownPlanets()) {
					q.tax = p.tax;
				}
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
			if (colonyInfo.taxLessAll) {
				for (Planet q : player().ownPlanets()) {
					q.tax = p.tax;
				}
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
//				if (p != planet() || (p == planet() && animationBlink)) {
					g2.fillRect(x0 - 1, y0 - 1, 3, 3);
//				}
				if (p == planet()) {
					g2.setColor(new Color(TextRenderer.GRAY));
					g2.drawRect(x0 - 3, y0 - 3, 6, 6);
				}
			}
			if (displayFleets) {
				for (Fleet f : player().fleets.keySet()) {
					if (knowledge(f, FleetKnowledge.VISIBLE) >= 0) {
						int x0 = (int)(f.x * width / commons.starmap().background.getWidth());
						int y0 = (int)(f.y * height / commons.starmap().background.getHeight());
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
					int x0 = (int)(f.x * width / commons.starmap().background.getWidth());
					int y0 = (int)(f.y * height / commons.starmap().background.getHeight());
					int w = f.owner.fleetIcon.getWidth();
					int h = f.owner.fleetIcon.getHeight();
					int x1 = x0 - w / 2;
					int y1 = y0 - h / 2;
					
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
						player().selectionMode = SelectionMode.FLEET;
						return true;
					}
				}
				Planet p = getPlanetAt(e.x, e.y);
				if (p != null) {
					player().currentPlanet = p;
					player().selectionMode = SelectionMode.PLANET;
					adjustPlanetListView();
					return true;
				}
			} else
			if (e.has(Type.DOUBLE_CLICK)) {
				if (displayFleets) {
					Fleet f = getFleetAt(e.x, e.y);
					if (f != null) {
						player().currentFleet = f;
						player().selectionMode = SelectionMode.FLEET;
						displaySecondary(Screens.EQUIPMENT);
						return true;
					}
					Planet p = getPlanetAt(e.x, e.y);
					if (p != null) {
						player().currentPlanet = p;
						player().selectionMode = SelectionMode.PLANET;
						displaySecondary(Screens.EQUIPMENT);
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
			if (planets.size() == 0) {
				commons.text().paintTo(g2, 0, 0, 14, TextRenderer.GRAY, get("info.empty-list"));
			}
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
	 * Comparator for the owner ordering but a custom inner ordering.
	 * @param <T> the named-owned object
	 * @param o1 the first object
	 * @param o2 the second object
	 * @param inner the secondary comparator
	 * @return the order
	 */
	<T extends Named & Owned> int compare2(T o1, T o2, Comparator<T> inner) {
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
		}
		int c = o1.owner().name.compareTo(o2.owner().name);
		if (c == 0) {
			c = inner.compare(o1, o2);
		}
		return c;
	}
	/**
	 * Compare two strings which may have a tailing number.
	 * @param s1 the first string
	 * @param s2 the second string
	 * @return the comparison result
	 */
	static int compareString(String s1, String s2) {
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
		List<Planet> planets = new ArrayList<>();
		for (Planet p : world().planets.values()) {
			if (knowledge(p, PlanetKnowledge.OWNER) >= 0) {
				planets.add(p);
			}
		}
		sortByOwnerAndName(planets);
		return planets;
	}
	/** @return an ordered list of planets to display. */
	List<Fleet> fleetsList() {
		List<Fleet> fleets = new ArrayList<>();
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
		final Action1<BuildingType> onSelect;
		/** The action to invoke when the user double-clicks on an item. */
		final Action1<BuildingType> onDoubleClick;
        /**
         * Constructor, initializes the selection callbacks.
         * @param onSelect callback when an item is simply selected
         * @param onDoubleClick callback when an item was double-clicked
         */
        public BuildingListing(Action1<BuildingType> onSelect, Action1<BuildingType> onDoubleClick) {
            this.onSelect = Objects.requireNonNull(onSelect);
            this.onDoubleClick = Objects.requireNonNull(onDoubleClick);
        }
		@Override
		public void draw(Graphics2D g2) {
			int rowCount = (height + rowHeight - 1) / rowHeight;
			int columnWidth = width / columns;
			
			List<BuildingType> planets = getList.invoke(null);
			Map<BuildingType, Integer> counts = player().countBuildings();
			Map<BuildingType, Integer> current = planet().countBuildings();
			boolean showCounts = planet().owner == player() 
					|| knowledge(planet(), PlanetKnowledge.BUILDING) >= 0
					|| (knowledge(planet(), PlanetKnowledge.OWNER) >= 0 && !planet().isPopulated());
			for (int j = 0; j < columns; j++) {
				int x0 = j * columnWidth;  
				for (int i = 0; i < rowCount; i++) {
					int y0 = i * rowHeight;
					int pidx = (j + columnIndex) * rowCount + i; 
					if (pidx >= 0 && pidx < planets.size()) {
						BuildingType p = planets.get(pidx);
						int c = TextRenderer.GRAY;
						if (planet().canBuild(p) && planet().owner == player()) {
							if (p.cost > player().money()) {
								c = 0xFFFF8080;
							} else {
								c = TextRenderer.YELLOW;
							}
						}
						String t = p.name;
						commons.text().paintTo(g2, x0 + 30, y0 + 2, fontSize, c, t);
						
						if (showCounts) {
							Integer c0 = current.get(p);
							Integer c1 = counts.get(p);
							
							String n = (c0 != null ? c0 : 0) + "/" + (c1 != null ? c1 : 0);
							int col = c1 != null ? TextRenderer.GREEN : TextRenderer.GRAY;
							commons.text().paintTo(g2, x0, y0 + 4, 7, col, n);
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
				return true;
			case KeyEvent.VK_PLUS:
			case KeyEvent.VK_MINUS:
				adjustPlanetListView();
				break;
			default:
			}
			if (showPlanetListDetails) {
				adjustPlanetListView();
			}
            break;
		case INFORMATION_INVENTIONS:
			switch (e.getKeyCode()) {
			case KeyEvent.VK_UP:
				navigateInvention(0, -1);
				return true;
			case KeyEvent.VK_DOWN:
				navigateInvention(0, 1);
				return true;
			case KeyEvent.VK_LEFT:
				navigateInvention(-1, 0);
				return true;
			case KeyEvent.VK_RIGHT:
				navigateInvention(1, 0);
				return true;
			default:
			}
		default:
		}
		if (!e.isControlDown()) {
			if (e.getKeyCode() == KeyEvent.VK_Q) {
				switchToTab(Screens.INFORMATION_PLANETS);
				return true;
			}
			if (e.getKeyCode() == KeyEvent.VK_W) {
				switchToTab(Screens.INFORMATION_COLONY);
				return true;
			}
			if (e.getKeyCode() == KeyEvent.VK_E) {
				switchToTab(Screens.INFORMATION_MILITARY);
				return true;
			}
			if (e.getKeyCode() == KeyEvent.VK_R) {
				switchToTab(Screens.INFORMATION_FINANCIAL);
				return true;
			}
			if (e.getKeyCode() == KeyEvent.VK_A) {
				switchToTab(Screens.INFORMATION_FLEETS);
				return true;
			}
			if (e.getKeyCode() == KeyEvent.VK_S) {
				switchToTab(Screens.INFORMATION_BUILDINGS);
				return true;
			}
			if (e.getKeyCode() == KeyEvent.VK_D) {
				switchToTab(Screens.INFORMATION_INVENTIONS);
				return true;
			}
			if (e.getKeyCode() == KeyEvent.VK_F && world().level >= 4) {
				switchToTab(Screens.INFORMATION_ALIENS);
				return true;
			}
		}
		return super.keyboard(e);
	}
	/**
	 * Navigate on the invention tab.
	 * @param dx the move delta x
	 * @param dy the move delta y
	 */
	public void navigateInvention(int dx, int dy) {
		List<List<ResearchType>> rts = inventionList();
		ResearchType rt0 = research();
		ResearchMainCategory m = rt0 != null ? rt0.category.main : ResearchMainCategory.SPACESHIPS;
		
		int c = m.ordinal();
		int idx = Math.max(0, rts.get(c).indexOf(rt0));
		
		c += dx;
		if (c < 0) {
			c = rts.size() - 1;
		} else
		if (c >= rts.size()) {
			c = 0;
		}
		
		idx += dy;
		if (dy == 0 && idx >= rts.get(c).size()) {
			idx = rts.get(c).size() - 1;
		} else
		if (idx >= rts.get(c).size()) {
			idx = 0;
		} else
		if (idx < 0) {
			idx = rts.get(c).size() - 1;
		}
		
		rt0 = rts.get(c).get(idx);
		world().selectResearch(rt0);
	}
	/**
	 * The list of list of main category inventions listed on the inventions tab.
	 * @return the list
	 */
	public List<List<ResearchType>> inventionList() {
		List<List<ResearchType>> result = new ArrayList<>();
		for (ResearchMainCategory m : ResearchMainCategory.values()) {
			List<ResearchType> res = new ArrayList<>();
			for (ResearchSubCategory sc : ResearchSubCategory.values()) {
				if (sc.main == m) {
					List<ResearchType> res0 = new ArrayList<>();
					for (ResearchType rt : world().researches.values()) {
						if (rt.category == sc && world().canDisplayResearch(rt)) {
							res0.add(rt);
						}
					}
					Collections.sort(res0, new Comparator<ResearchType>() {
						@Override
						public int compare(ResearchType o1, ResearchType o2) {
							return o1.index - o2.index;
						}
					});
					res.addAll(res0);
				}
			}
			result.add(res);
		}
		return result;
	}
	/** Adjust the highlight on the planet list view. */
	void adjustPlanetListView() {
		List<Planet> list = planetListDetails.getPlanets();
		Planet cp = colonies.getCurrent.invoke(null);
		int idx = list.indexOf(cp); 
		if (idx >= 0) {
			if (idx < planetListDetails.top) {
				planetListDetails.top = idx;
			} else
			if (idx >= planetListDetails.top + 26) {
				planetListDetails.top = idx - 26;
			}
		}
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
		UILabel yesterdayTotalIncome;
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
		/** Global yesterday value. */
		UILabel yesterdayTotalCost;
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
		/** Global today value. */
		UILabel todayTotalCost;
		/** Static field. */
		UILabel planetCurrent;
		/** Selected own planet value. */
		UILabel planetTaxIncome;
		/** Selected own planet value. */
		UILabel planetTradeIncome;
		/** Selected own planet value. */
		UILabel planetTotalIncome;
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
			yesterdayTotalIncome = new UILabel("", 10, commons.text());
			yesterdayTaxMorale = new UILabel("", 10, commons.text());

			yesterdayProductionCost = new UILabel("", 10, commons.text());
			yesterdayResearchCost = new UILabel("", 10, commons.text());
			yesterdayRepairCost = new UILabel("", 10, commons.text());
			yesterdayBuildCost = new UILabel("", 10, commons.text());
			yesterdayTotalCost = new UILabel("", 10, commons.text());
			
			todayProductionCost = new UILabel("", 10, commons.text());
			todayResearchCost = new UILabel("", 10, commons.text());
			todayRepairCost = new UILabel("", 10, commons.text());
			todayBuildCost = new UILabel("", 10, commons.text());
			todayTotalCost = new UILabel("", 10, commons.text());
			
			planetTaxIncome = new UILabel("", 10, commons.text());
			planetTradeIncome = new UILabel("", 10, commons.text());
			planetTotalIncome = new UILabel("", 10, commons.text());
			planetTaxMorale = new UILabel("", 10, commons.text());
			
			yesterday.location(0, 0);
			yesterdayTaxIncome.location(10, 25);
			yesterdayTradeIncome.location(10, 25 + 18);
			yesterdayTotalIncome.location(250, 25 + 18);
			yesterdayTaxMorale.location(10, 25 + 18 * 2);
			
			yesterdayProductionCost.location(10, 25 + 18 * 3);
			yesterdayResearchCost.location(10, 25 + 18 * 4);
			yesterdayRepairCost.location(10, 25 + 18 * 5);
			yesterdayBuildCost.location(10, 25 + 18 * 6);
			yesterdayTotalCost.location(250, 25 + 18 * 6);
			
			today.location(0, 50 + 18 * 6);
			todayProductionCost.location(10, today.y + 25);
			todayResearchCost.location(10, today.y + 25 + 18);
			todayRepairCost.location(10, today.y + 25 + 18 * 2);
			todayBuildCost.location(10, today.y + 25 + 18 * 3);
			todayTotalCost.location(250, today.y + 25 + 18 * 3);
			
			planetCurrent.location(0, today.y + 50 + 18 * 3);
			planetTaxIncome.location(10, planetCurrent.y + 25);
			planetTradeIncome.location(10, planetCurrent.y + 25 + 18);
			planetTotalIncome.location(250, planetCurrent.y + 25 + 18);
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
			yesterdayTotalIncome.text(format("colonyinfo.total", player().yesterday.getTotalIncome()), true);
			if (player().yesterday.taxMoraleCount > 0) {
				yesterdayTaxMorale.text(format("colonyinfo.tax-morale", 
						(int)(player().yesterday.taxMorale / player().yesterday.taxMoraleCount), 
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
			yesterdayTotalCost.text(format("colonyinfo.total", player().yesterday.getTotalCost()), true);
			
			todayProductionCost.text(format("financialinfo.production_cost", player().today.productionCost), true);
			todayResearchCost.text(format("financialinfo.research_cost", player().today.researchCost), true);
			todayRepairCost.text(format("financialinfo.repair_cost", player().today.repairCost), true);
			todayBuildCost.text(format("financialinfo.build_cost", player().today.buildCost), true);
			todayTotalCost.text(format("colonyinfo.total", player().today.getTotalCost()), true);
			
			planetCurrent.text(p.name(), true);
			if (p.owner == player()) {
				
				planetTaxIncome.text(format("colonyinfo.tax", (int)p.taxIncome()), true);
				planetTradeIncome.text(format("colonyinfo.trade", (int)p.tradeIncome()), true);
				planetTotalIncome.text(format("colonyinfo.total", p.getTotalIncome()), true);
				planetTaxMorale.text(format("colonyinfo.tax-morale", (int)p.morale, get(p.getMoraleLabel())), true);
				
				planetCurrent.visible(true);
				planetTaxIncome.visible(true);
				planetTradeIncome.visible(true);
				planetTaxMorale.visible(true);
				planetTotalIncome.visible(true);
			} else {
				planetCurrent.visible(false);
				planetTaxIncome.visible(false);
				planetTradeIncome.visible(false);
				planetTaxMorale.visible(false);
				planetTotalIncome.visible(false);
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
			if (bt.cost > player().money()) {
				buildingCost.color(0xFFFF8080);
			} else {
				buildingCost.color(TextRenderer.GREEN);
			}
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
			buildingPlanet.text(p.name(), true);
		} else {
			buildingPlanet.text("", true);
		}
		if (knowledge(p, PlanetKnowledge.OWNER) >= 0) {
			if (p.owner != null) {
				buildingPlanetOwner.text(p.owner.name, true);
				if (p.owner == player()) {
					buildingPlanetRace.text(format("colonyinfo.population.own", 
							(int)p.population(), get(p.getRaceLabel()), get(p.getMoraleLabel()) 
					), true);
				} else {
					if (knowledge(p, PlanetKnowledge.STATIONS) >= 0) {
						if (p.isPopulated()) {
							buildingPlanetRace.text(format("colonyinfo.population.short.alien", 
									(int)p.population()
							), true);
						} else {
							buildingPlanetRace.text("");
						}
					} else {
						buildingPlanetRace.text(p.isPopulated() ? get(p.getRaceLabel()) : "", true);
					}
				}
			} else {
				buildingPlanetOwner.text(get("planet.colonizable"), true);
				buildingPlanetRace.text("");
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
		problemsHouse.visible(false);
		problemsEnergy.visible(false);
		problemsWorker.visible(false);
		problemsFood.visible(false);
		problemsHospital.visible(false);
		problemsVirus.visible(false);
		problemsStadium.visible(false);
		problemsRepair.visible(false);
		problemsColonyHub.visible(false);
		problemsPolice.visible(false);
		problemsFireBrigade.visible(false);
		if (p.owner == player()) {
			PlanetStatistics ps = localStatistics;
			if (ps.hasProblem(PlanetProblems.HOUSING)) {
				problemsHouse.image(commons.common().houseIcon).visible(true);
				setTooltip(problemsHouse, "info.problems.house.tooltip");
			} else
			if (ps.hasWarning(PlanetProblems.HOUSING)) {
				problemsHouse.image(commons.common().houseIconDark).visible(true);
				setTooltip(problemsHouse, "info.warnings.house.tooltip");
			}
			
			if (ps.hasProblem(PlanetProblems.ENERGY)) {
				problemsEnergy.image(commons.common().energyIcon).visible(true);
				setTooltip(problemsEnergy, "info.problems.energy.tooltip");
			} else
			if (ps.hasWarning(PlanetProblems.ENERGY)) {
				problemsEnergy.image(commons.common().energyIconDark).visible(true);
				setTooltip(problemsEnergy, "info.warnings.energy.tooltip");
			}
			
			if (ps.hasProblem(PlanetProblems.WORKFORCE)) {
				problemsWorker.image(commons.common().workerIcon).visible(true);
				setTooltip(problemsWorker, "info.problems.worker.tooltip");
			} else
			if (ps.hasWarning(PlanetProblems.WORKFORCE)) {
				problemsWorker.image(commons.common().workerIconDark).visible(true);
				setTooltip(problemsWorker, "info.warnings.worker.tooltip");
			}
			
			if (ps.hasProblem(PlanetProblems.FOOD)) {
				problemsFood.image(commons.common().foodIcon).visible(true);
				setTooltip(problemsFood, "info.problems.food.tooltip");
			} else
			if (ps.hasWarning(PlanetProblems.FOOD)) {
				problemsFood.image(commons.common().foodIconDark).visible(true);
				setTooltip(problemsFood, "info.warnings.food.tooltip");
			}
			
			if (ps.hasProblem(PlanetProblems.HOSPITAL)) {
				problemsHospital.image(commons.common().hospitalIcon).visible(true);
				setTooltip(problemsHospital, "info.problems.hospital.tooltip");
			} else
			if (ps.hasWarning(PlanetProblems.HOSPITAL)) {
				problemsHospital.image(commons.common().hospitalIconDark).visible(true);
				setTooltip(problemsHospital, "info.warnings.hospital.tooltip");
			}

			if (ps.hasProblem(PlanetProblems.VIRUS)) {
				problemsVirus.image(commons.common().virusIcon).visible(true);
				setTooltip(problemsVirus, "info.problems.virus.tooltip");
			} else
			if (ps.hasWarning(PlanetProblems.VIRUS)) {
				problemsVirus.image(commons.common().virusIconDark).visible(true);
				setTooltip(problemsVirus, "info.warnings.virus.tooltip");
			}

			if (ps.hasProblem(PlanetProblems.STADIUM)) {
				problemsStadium.image(commons.common().stadiumIcon).visible(true);
				setTooltip(problemsStadium, "info.problems.stadium.tooltip");
			} else
			if (ps.hasWarning(PlanetProblems.STADIUM)) {
				problemsStadium.image(commons.common().stadiumIconDark).visible(true);
				setTooltip(problemsStadium, "info.warnings.stadium.tooltip");
			}

			if (ps.hasProblem(PlanetProblems.REPAIR)) {
				problemsRepair.image(commons.common().repairIcon).visible(true);
				setTooltip(problemsRepair, "info.problems.damage.tooltip");
			} else
			if (ps.hasWarning(PlanetProblems.REPAIR)) {
				problemsRepair.image(commons.common().repairIconDark).visible(true);
				setTooltip(problemsRepair, "info.warnings.damage.tooltip");
			}

			if (ps.hasProblem(PlanetProblems.COLONY_HUB)) {
				problemsColonyHub.image(commons.common().colonyHubIcon).visible(true);
				setTooltip(problemsColonyHub, "info.problems.hq.tooltip");
			} else
			if (ps.hasWarning(PlanetProblems.COLONY_HUB)) {
				problemsColonyHub.image(commons.common().colonyHubIconDark).visible(true);
				setTooltip(problemsColonyHub, "info.warnings.hq.tooltip");
			}
			if (ps.hasProblem(PlanetProblems.POLICE)) {
				problemsPolice.image(commons.common().policeIcon).visible(true);
				setTooltip(problemsPolice, "info.problems.police.tooltip");
			} else
			if (ps.hasWarning(PlanetProblems.POLICE)) {
				problemsPolice.image(commons.common().policeIconDark).visible(true);
				setTooltip(problemsPolice, "info.warnings.police.tooltip");
			}
			if (ps.hasProblem(PlanetProblems.FIRE_BRIGADE)) {
				problemsFireBrigade.image(commons.common().fireBrigadeIcon).visible(true);
				setTooltip(problemsFireBrigade, "info.problems.fire.tooltip");
			}
		}
	}
	/** Display the right panel's planet info on the current selected planet. */
	void displayPlanetInfo() {
		
		Planet p = planet();
		
		if (knowledge(p, PlanetKnowledge.NAME) >= 0) {
			planetTitle.text(p.name());
			int c = TextRenderer.GRAY;
			if (knowledge(p, PlanetKnowledge.OWNER) >= 0 && p.isPopulated()) {
				c = p.owner.color;
			}
			planetTitle.color(c);
			planetTitle.visible(true);
		} else {
			planetTitle.visible(false);
		}
		
		if (knowledge(p, PlanetKnowledge.OWNER) >= 0) {
			if (p.isPopulated()) {
				colonyOwner.text(p.owner != null ? p.owner.name : "", true);
				colonyRace.text(p.isPopulated() ? get(p.getRaceLabel()) : "-", true);
			} else {
				colonyOwner.text(get("planet.colonizable"), true);
				colonyRace.text("", true);
			}
			colonyOwner.visible(true);
			colonyRace.visible(true);
		} else {
			colonyOwner.visible(false);
			colonyRace.visible(false);
		}
		colonySurface.text(format("buildinginfo.planet.surface", firstUpper(get(p.type.label))), true);
		
		if (p.owner == player()) {
			colonyPopulation.text(format("colonyinfo.population.own", 
					(int)p.population(), get(p.getRaceLabel()), get(p.getMoraleLabel()) 
			), true).visible(true);
			colonyTax.text(format("colonyinfo.tax_short", get(p.getTaxLabel())), true).visible(true);
		} else {
			if (knowledge(p, PlanetKnowledge.BUILDING) >= 0) {
				if (p.isPopulated()) {
					colonyPopulation.text(format("colonyinfo.population.short.alien", 
							(int)p.population()
					), true).visible(true);
				} else {
					colonyPopulation.visible(false);
				}
			} else {
				colonyPopulation.visible(false);
			}
			colonyTax.visible(false);
		}
		colonyOther.text(world().getOtherItems());
		
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
						List<ResearchType> res = new ArrayList<>();
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
							int c = world().getResearchColor(rt, globalStatistics);
							commons.text().paintTo(g2, col * colWidth + 3, row * 12 + 2, 7, c, rt.name);
							if (rt == research()) {
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
			if (e.has(Type.WHEEL)) {
				int dx = 0;
				int dy = 0;
				if (e.has(Modifier.SHIFT)) {
					dx = e.z < 0 ? -1 : 1;
				} else {
					dy = e.z < 0 ? -1 : 1;
				}
				navigateInvention(dx, dy);
				return true;
			}
			if (e.has(Type.DOWN)) {
				int col = e.x * 4 / width;
				List<ResearchType> res = getResearchColumn(col);
				int row = e.y / 12;
				if (row < res.size()) {
					buttonSound(SoundType.CLICK_MEDIUM_2);
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
					if (player().isAvailable(rt)) {
						if (world().level >= 2) {
							displaySecondary(Screens.PRODUCTION);
						}
					} else {
						if (world().level >= 3) {
							displaySecondary(Screens.RESEARCH);
						}
					}
					return true;
				}
			}
			return super.mouse(e);
		}
    }
	/**
	 * Get the list of research items for the given column.
	 * @param column the column index
	 * @return the list of reseaches in that column
	 */
	public List<ResearchType> getResearchColumn(int column) {
		List<ResearchType> res = new ArrayList<>();
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
		ResearchType rt = research();
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

		Player p = player();
		
		if (rt != null) {
			planetTitle.text(rt.name);
			if (p.isAvailable(rt) || p.canResearch(rt)) {
				descriptionImage.image(rt.infoImage);
				descriptionText.text(rt.description);
				descriptionTitle.text(rt.longName);
				planetTitle.color(TextRenderer.ORANGE);
				
			} else {
				descriptionImage.image(rt.infoImageWired);
				descriptionText.text("");
				descriptionTitle.text(rt.longName);
				if (p.canResearch(rt)) {
					planetTitle.color(TextRenderer.GREEN);
				} else {
					planetTitle.color(TextRenderer.GRAY);
				}
			}
			Research rs = p.getResearch(rt);
			if (rs != null) {
				switch (rs.state) {
				case RUNNING:
					researchProgress.text(format("researchinfo.progress.running", (int)rs.getPercent(p.traits)), true).visible(true);
					break;
				case STOPPED:
					researchProgress.text(format("researchinfo.progress.paused", (int)rs.getPercent(p.traits)), true).visible(true);
					break;
				case LAB:
					researchProgress.text(format("researchinfo.progress.lab", (int)rs.getPercent(p.traits)), true).visible(true);
					break;
				case MONEY:
					researchProgress.text(format("researchinfo.progress.money", (int)rs.getPercent(p.traits)), true).visible(true);
					break;
				default:
					researchProgress.text("");
				}
				researchCost.text(format("researchinfo.progress.cost", rt.researchCost(p.traits)), true).visible(true);
			} else {
				if (p.isAvailable(rt)) {
					researchProgress.text(get("researchinfo.progress.done"), true).visible(true);
					researchCost.text(format("researchinfo.progress.price", rt.productionCost), true).visible(true);
					Integer cnt = p.inventory.get(rt);
					if (rt.category.main != ResearchMainCategory.BUILDINGS) {
						researchInventory.text(format("researchinfo.progress.inventory", cnt != null ? cnt : 0), true).visible(true);
					}
				} else {
					if (p.canResearch(rt)) {
						researchProgress.text(get("researchinfo.progress.can"), true).visible(true);
						researchCost.text(format("researchinfo.progress.cost", rt.researchCost(p.traits)), true).visible(true);
					} else {
						researchProgress.text(get("researchinfo.progress.cant"), true).visible(true);
						descriptionImage.image(null);
					}
				}
			}
			globalStatistics = p.getPlanetStatistics(null);

			if (rt.prerequisites.size() > 0) {
				researchPrerequisites.visible(true);
				researchPre1.text(rt.prerequisites.get(0).name, true).visible(true);
				researchPre1.color(world().getResearchColor(rt.prerequisites.get(0), globalStatistics));
			}
			if (rt.prerequisites.size() > 1) {
				researchPre2.text(rt.prerequisites.get(1).name, true).visible(true);
				researchPre2.color(world().getResearchColor(rt.prerequisites.get(1), globalStatistics));
			}
			if (rt.prerequisites.size() > 2) {
				researchPre3.text(rt.prerequisites.get(2).name, true).visible(true);
				researchPre3.color(world().getResearchColor(rt.prerequisites.get(2), globalStatistics));
			}
			
			researchLabs.get(0).text(Integer.toString(rt.civilLab));
			researchLabs.get(1).text(Integer.toString(rt.mechLab));
			researchLabs.get(2).text(Integer.toString(rt.compLab));
			researchLabs.get(3).text(Integer.toString(rt.aiLab));
			researchLabs.get(4).text(Integer.toString(rt.milLab));

			researchAvailable.get(0).text(Integer.toString(globalStatistics.activeLabs.civil));
			researchAvailable.get(0).color(
					globalStatistics.labs.civil < rt.civilLab ? TextRenderer.RED 
						: (globalStatistics.labs.civil > globalStatistics.activeLabs.civil ? TextRenderer.YELLOW : TextRenderer.GREEN)
			);
			researchAvailable.get(1).text(Integer.toString(globalStatistics.activeLabs.mech));
			researchAvailable.get(1).color(
					globalStatistics.labs.mech < rt.mechLab ? TextRenderer.RED 
						: (globalStatistics.labs.mech > globalStatistics.activeLabs.mech ? TextRenderer.YELLOW : TextRenderer.GREEN)
			);
			researchAvailable.get(2).text(Integer.toString(globalStatistics.activeLabs.comp));
			researchAvailable.get(2).color(
					globalStatistics.labs.comp < rt.compLab ? TextRenderer.RED 
						: (globalStatistics.labs.comp > globalStatistics.activeLabs.comp ? TextRenderer.YELLOW : TextRenderer.GREEN)
			);
			researchAvailable.get(3).text(Integer.toString(globalStatistics.activeLabs.ai));
			researchAvailable.get(3).color(
					globalStatistics.labs.ai < rt.aiLab ? TextRenderer.RED 
						: (globalStatistics.labs.ai > globalStatistics.activeLabs.ai ? TextRenderer.YELLOW : TextRenderer.GREEN)
			);
			researchAvailable.get(4).text(Integer.toString(globalStatistics.activeLabs.mil));
			researchAvailable.get(4).color(
					globalStatistics.labs.mil < rt.milLab ? TextRenderer.RED 
						: (globalStatistics.labs.mil > globalStatistics.activeLabs.mil ? TextRenderer.YELLOW : TextRenderer.GREEN)
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
			
			planet.text(p.name(), true);
			
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
			String surfaceText = format("colonyinfo.surface", firstUpper(get(p.type.label)));
			if (p.owner == player()) {
				double g = world().galaxyModel.getGrowth(p.type.type, p.race);
				Trait t = player().traits.trait(TraitKind.FERTILE);
				if (t != null) {
					g *= 1 + t.value / 100;
				}
				surfaceText = format("colonyinfo.surface2", 
						firstUpper(get(p.type.label)), (int)(g * 100));
			}
			surface.text(surfaceText, true);

			population.visible(false);

			if (p.isPopulated()) {
				if (knowledge(p, PlanetKnowledge.STATIONS) >= 0) {
					if (p.owner == player()) {
						population.text(format("colonyinfo.population", 
								(int)p.population(), get(p.getMoraleLabel()), withSign((int)p.population() - (int)p.lastPopulation())
						), true).visible(true);
					} else {
						population.text(format("colonyinfo.population.alien", 
								(int)p.population()
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
						|| rt.category == ResearchSubCategory.WEAPONS_VEHICLES
						|| rt.category == ResearchSubCategory.SPACESHIPS_FIGHTERS) {

					int cnt = planet().getInventoryCount(rt);
					
					if (player().isAvailable(rt) 
							|| (planet().owner != player() && cnt > 0)) {
						commons.text().paintTo(g2, 10, row * 15 + 20 + population.y, 10, TextRenderer.GREEN, rt.name);

						String cntStr;
						
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
		/** Sort by column. */
		public int sortBy;
		/** Sort ascending? */
		public boolean ascending = true;
		/** @return Get the ordered planet list. */
		List<Planet> getPlanets() {
			List<Planet> list = colonies.getList.invoke(null);
			switch (sortBy) {
			case 0:
				if (ascending) {
					Collections.sort(list, new Comparator<Planet>() {
						@Override
						public int compare(Planet o1, Planet o2) {
							return compare2(o1, o2, new Comparator<Planet>() {
								@Override
								public int compare(Planet o1, Planet o2) {
									return compareString(o1.name(), o2.name());
								}
							});
						}
					});
				} else {
					Collections.sort(list, new Comparator<Planet>() {
						@Override
						public int compare(Planet o1, Planet o2) {
							return compare2(o1, o2, new Comparator<Planet>() {
								@Override
								public int compare(Planet o1, Planet o2) {
									return compareString(o2.name(), o1.name());
								}
							});
						}
					});
				}
				break;
			case 1:
				if (ascending) {
					Collections.sort(list, new Comparator<Planet>() {
						@Override
						public int compare(Planet o1, Planet o2) {
							return compare2(o1, o2, new Comparator<Planet>() {
								@Override
								public int compare(Planet o1, Planet o2) {
									return Double.compare(o1.population(), o2.population());
								}
							});
						}
					});
					
				} else {
					
					Collections.sort(list, new Comparator<Planet>() {
						@Override
						public int compare(Planet o1, Planet o2) {
							return compare2(o1, o2, new Comparator<Planet>() {
								@Override
								public int compare(Planet o1, Planet o2) {
									return Double.compare(o2.population(), o1.population());
								}
							});
						}
					});
				}
				break;
			case 2:
				if (ascending) {
					Collections.sort(list, new Comparator<Planet>() {
						@Override
						public int compare(Planet o1, Planet o2) {
							return compare2(o1, o2, new Comparator<Planet>() {
								@Override
								public int compare(Planet o1, Planet o2) {
									return Double.compare(o1.morale, o2.morale);
								}
							});
						}
					});
				} else {
					Collections.sort(list, new Comparator<Planet>() {
						@Override
						public int compare(Planet o1, Planet o2) {
							return compare2(o1, o2, new Comparator<Planet>() {
								@Override
								public int compare(Planet o1, Planet o2) {
									return Double.compare(o2.morale, o1.morale);
								}
							});
						}
					});
				}
				break;
			case 3:
				final Map<Planet, Set<PlanetProblems>> ppc = new HashMap<>();
				if (ascending) {
					Collections.sort(list, new Comparator<Planet>() {
						@Override
						public int compare(Planet o1, Planet o2) {
							return compare2(o1, o2, new Comparator<Planet>() {
								@Override
								public int compare(Planet o1, Planet o2) {
									
									Set<PlanetProblems> pp1 = ppc.get(o1);
									if (pp1 == null) {
										pp1 = new HashSet<>();
										PlanetStatistics ps = o1.getStatistics();
										pp1.addAll(ps.problems);
										pp1.addAll(ps.warnings);
										ppc.put(o1, pp1);
									}
									Set<PlanetProblems> pp2 = ppc.get(o2);
									if (pp2 == null) {
										pp2 = new HashSet<>();
										PlanetStatistics ps = o2.getStatistics();
										pp2.addAll(ps.problems);
										pp2.addAll(ps.warnings);
										ppc.put(o2, pp2);
									}
									
									return pp1.size() - pp2.size();
								}
							});
						}
					});
				} else {
					Collections.sort(list, new Comparator<Planet>() {
						@Override
						public int compare(Planet o1, Planet o2) {
							return compare2(o1, o2, new Comparator<Planet>() {
								@Override
								public int compare(Planet o1, Planet o2) {
									
									Set<PlanetProblems> pp1 = ppc.get(o1);
									if (pp1 == null) {
										pp1 = new HashSet<>();
										PlanetStatistics ps = o1.getStatistics();
										pp1.addAll(ps.problems);
										pp1.addAll(ps.warnings);
										ppc.put(o1, pp1);
									}
									Set<PlanetProblems> pp2 = ppc.get(o2);
									if (pp2 == null) {
										pp2 = new HashSet<>();
										PlanetStatistics ps = o2.getStatistics();
										pp2.addAll(ps.problems);
										pp2.addAll(ps.warnings);
										ppc.put(o2, pp2);
									}
									
									return pp2.size() - pp1.size();
								}
							});
						}
					});
				}
				break;
			case 4:
				final Comparator<Planet> labNameComparator = new Comparator<Planet>() {
					@Override
					public int compare(Planet o1, Planet o2) {
						Collection<Building> b1 = o1.surface.buildings.findByKind("Science");
						Collection<Building> b2 = o2.surface.buildings.findByKind("Science");
						if (b1.isEmpty() && b2.isEmpty()) {
							return 0;
						} else
						if (b1.isEmpty() && !b2.isEmpty()) {
							return 1;
						} else
						if (!b1.isEmpty() && b2.isEmpty()) {
							return -1;
						}
						Building a1 = b1.iterator().next();
						Building a2 = b2.iterator().next();
						return a1.type.name.compareTo(a2.type.name);
					}
				};
				Comparator<Planet> comp;
				if (ascending) {
					comp = new Comparator<Planet>() {
						@Override
						public int compare(Planet o1, Planet o2) {
							return compare2(o1, o2, labNameComparator);
						}
					};
				} else {
					comp = new Comparator<Planet>() {
						@Override
						public int compare(Planet o1, Planet o2) {
							return compare2(o1, o2, U.reverse(labNameComparator));
						}
					};
				}
				Collections.sort(list, comp);
				break;
			default:
			}
			return list;
		}
		@Override
		public void draw(Graphics2D g2) {
			int y = 0;
			Planet cp = colonies.getCurrent.invoke(null);
			g2.setColor(Color.BLACK);
			g2.fillRect(5, - 13, 390, 13);

			int probLeft = 310;
			
			String s1 = get("info.planet_name");
			int w1 = commons.text().getTextWidth(10, s1);
			String s2 = get("info.population_details");
			int w2 = commons.text().getTextWidth(10, s2);
			String s3 = get("info.morale_details");
			int w3 = commons.text().getTextWidth(10, s3);
			String s4 = get("info.problem_details");
			int w4 = commons.text().getTextWidth(10, s4);
			String s5 = get("info.lab_details");
			int w5 = commons.text().getTextWidth(10, s5);
			
			commons.text().paintTo(g2, 10, -13, 10, TextRenderer.YELLOW, s1);
			if (sortBy == 0) {
				g2.setColor(Color.YELLOW);
				drawTriangle(g2, 12 + w1, -13, 10, ascending);
			}
			
			commons.text().paintTo(g2, 105, -13, 10, TextRenderer.YELLOW, s2);
			if (sortBy == 1) {
				g2.setColor(Color.YELLOW);
				drawTriangle(g2, 107 + w2, -13, 10, ascending);
			}
			
			commons.text().paintTo(g2, 240, -13, 10, TextRenderer.YELLOW, s3);
			if (sortBy == 2) {
				g2.setColor(Color.YELLOW);
				drawTriangle(g2, 242 + w3, -13, 10, ascending);
			}

			switch (planetListMode) {
			case LABS:
				commons.text().paintTo(g2, probLeft, -13, 10, TextRenderer.YELLOW, s5);
				if (sortBy == 4) {
					g2.setColor(Color.YELLOW);
					drawTriangle(g2, probLeft + w5 + 2, -13, 10, ascending);
				}
				break;
			default:
				commons.text().paintTo(g2, probLeft, -13, 10, TextRenderer.YELLOW, s4);
				if (sortBy == 3) {
					g2.setColor(Color.YELLOW);
					drawTriangle(g2, probLeft + w4 + 2, -13, 10, ascending);
				}
			}

			
			
			if (top < 0) {
				top = 0;
			}
			List<Planet> list = getPlanets();
			
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
					String pop = Integer.toString((int)p.population());
					int popWidth = commons.text().getTextWidth(10, pop);
					commons.text().paintTo(g2, 160 - popWidth, y + 1, 10, TextRenderer.GREEN, pop);
					int pc = TextRenderer.GREEN;
					int populationChange = (int)p.population() - (int)p.lastPopulation();
					if (populationChange < -150) {
						pc = TextRenderer.RED;
					} else
					if (populationChange < 0) {
						pc = TextRenderer.YELLOW;
					} else
					if (populationChange > 300) {
						pc = TextRenderer.LIGHT_BLUE;
					} else
					if (populationChange > 80) {
						pc = TextRenderer.ORANGE;
					}
					
					commons.text().paintTo(g2, 175, y + 1, 10, pc, withSign(populationChange));
					 
					if (p.owner == player()) {

						int mmc = TextRenderer.GREEN;
						
						if (p.morale < 10) {
							mmc = TextRenderer.RED;
						} else
						if (p.morale < 30) {
							mmc = 0xFFFF8080;
						} else
						if (p.morale < 45) {
							mmc = TextRenderer.YELLOW;
						} else
						if (p.morale >= 85) {
							mmc = TextRenderer.LIGHT_BLUE;
						} else
						if (p.morale >= 65) {
							mmc = TextRenderer.ORANGE;
						}
						
						commons.text().paintTo(g2, 240, y + 1, 10, 
								mmc, 
										((int)p.morale) + "%");

						if (p.morale - p.lastMorale != 0) {
							int mc = TextRenderer.GREEN;
							if (p.morale - p.lastMorale < -4) {
								mc = TextRenderer.RED;
							} else
							if (p.morale - p.lastMorale < 0) {
								mc = TextRenderer.YELLOW;
							} else
							if (p.morale - p.lastMorale > 3) {
								mc = TextRenderer.ORANGE;
							}
									
							commons.text().paintTo(g2, 270, y + 1, 10,
									mc,  
											 withSign((int)(p.morale) - (int)(p.lastMorale)));
						}
						
						PlanetStatistics ps = p.getStatistics();
						switch (planetListMode) {
						case LABS:
							Collection<Building> bs = p.surface.buildings.findByKind("Science");
							if (!bs.isEmpty()) {
								Building b = bs.iterator().next();
								String bn = b.type.name;
								int idx = bn.indexOf(' ');
								if (idx > 0) {
									bn = bn.substring(0, idx);
								}
								commons.text().paintTo(g2, probLeft, y + 1, 10, b.isOperational() ? TextRenderer.GREEN : 0xFFFF8080, bn);
							}
							break;
						default: {
							int j = 0;
							
							if (ps.hasProblem(PlanetProblems.HOUSING)) {
								g2.drawImage(commons.common().houseIcon, probLeft + j * 11, y + 2, null);
								j++;
							} else
							if (ps.hasWarning(PlanetProblems.HOUSING)) {
								g2.drawImage(commons.common().houseIconDark, probLeft + j * 11, y + 2, null);
								j++;
							}
							
							if (ps.hasProblem(PlanetProblems.ENERGY)) {
								g2.drawImage(commons.common().energyIcon, probLeft + j * 11, y + 2, null);
								j++;
							} else
							if (ps.hasWarning(PlanetProblems.ENERGY)) {
								g2.drawImage(commons.common().energyIconDark, probLeft + j * 11, y + 2, null);
								j++;
							}
							
							if (ps.hasProblem(PlanetProblems.WORKFORCE)) {
								g2.drawImage(commons.common().workerIcon, probLeft + j * 11, y + 2, null);
								j++;
							} else
							if (ps.hasWarning(PlanetProblems.WORKFORCE)) {
								g2.drawImage(commons.common().workerIconDark, probLeft + j * 11, y + 2, null);
								j++;
							}
							
							if (ps.hasProblem(PlanetProblems.FOOD)) {
								g2.drawImage(commons.common().foodIcon, probLeft + j * 11, y + 2, null);
								j++;
							} else
							if (ps.hasWarning(PlanetProblems.FOOD)) {
								g2.drawImage(commons.common().foodIconDark, probLeft + j * 11, y + 2, null);
								j++;
							}
							
							if (ps.hasProblem(PlanetProblems.HOSPITAL)) {
								g2.drawImage(commons.common().hospitalIcon, probLeft + j * 11, y + 2, null);
								j++;
							} else
							if (ps.hasWarning(PlanetProblems.HOSPITAL)) {
								g2.drawImage(commons.common().hospitalIconDark, probLeft + j * 11, y + 2, null);
								j++;
							}
							
							if (ps.hasProblem(PlanetProblems.VIRUS)) {
								g2.drawImage(commons.common().virusIcon, probLeft + j * 11, y + 2, null);
								j++;
							} else
							if (ps.hasWarning(PlanetProblems.VIRUS)) {
								g2.drawImage(commons.common().virusIconDark, probLeft + j * 11, y + 2, null);
								j++;
							}
							
							if (ps.hasProblem(PlanetProblems.STADIUM)) {
								g2.drawImage(commons.common().stadiumIcon, probLeft + j * 11, y + 2, null);
								j++;
							} else
							if (ps.hasWarning(PlanetProblems.STADIUM)) {
								g2.drawImage(commons.common().stadiumIconDark, probLeft + j * 11, y + 2, null);
								j++;
							}
							
							if (ps.hasProblem(PlanetProblems.REPAIR)) {
								g2.drawImage(commons.common().repairIcon, probLeft + j * 11, y + 2, null);
								j++;
							} else
							if (ps.hasWarning(PlanetProblems.REPAIR)) {
								g2.drawImage(commons.common().repairIconDark, probLeft + j * 11, y + 2, null);
								j++;
							}
							
							if (ps.hasProblem(PlanetProblems.COLONY_HUB)) {
								g2.drawImage(commons.common().colonyHubIcon, probLeft + j * 11, y + 2, null);
								j++;
							} else
							if (ps.hasWarning(PlanetProblems.COLONY_HUB)) {
								g2.drawImage(commons.common().colonyHubIconDark, probLeft + j * 11, y + 2, null);
								j++;
							}
							
							if (ps.hasProblem(PlanetProblems.POLICE)) {
								g2.drawImage(commons.common().policeIcon, probLeft + j * 11, y + 2, null);
								j++;
							} else
							if (ps.hasWarning(PlanetProblems.POLICE)) {
								g2.drawImage(commons.common().policeIconDark, probLeft + j * 11, y + 2, null);
								j++;
							}
							if (ps.hasProblem(PlanetProblems.FIRE_BRIGADE)) {
								g2.drawImage(commons.common().fireBrigadeIcon, probLeft + j * 11, y + 2, null);
							}
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
				List<Planet> list = getPlanets();
				if (list.size() > 0) {
					if (e.z < 0) {
						top = Math.max(0, top - 1);
					} else {
						top = Math.min(list.size() - 27, top + 1);
					}
					return true;
				}
			} else
			if (e.has(Type.DOWN)) {
				List<Planet> list = getPlanets();
				int idx = e.y / 13 + top;
				if (idx >= 0 && idx < list.size()) {
					buttonSound(SoundType.CLICK_MEDIUM_2);
					colonies.onSelect.invoke(list.get(idx));
					return true;
				}
			} else
			if (e.has(Type.DOUBLE_CLICK)) {
				List<Planet> list = getPlanets();
				int idx = e.y / 13 + top;
				if (idx >= 0 && idx < list.size()) {
					colonies.onDoubleClick.invoke(list.get(idx));
					return true;
				}
			}
			
				
			return false;
		}
		/**
		 * Draw a triangle.
		 * @param g2 the graphics context
		 * @param x the top left of the enclosing rectangle
		 * @param y the top left of the enclosing rectangle
		 * @param size the side size
		 * @param up points upwards or downwards
		 */
		void drawTriangle(Graphics2D g2, int x, int y, int size, boolean up) {
			int[] xs = {x, x + size, x + size / 2};
			if (up) {
				int[] ys = {y + size * 866 / 1000, y + size * 866 / 1000, y};
				g2.fillPolygon(xs, ys, 3);
			} else {
				int[] ys = {y, y, y + size * 866 / 1000};
				g2.fillPolygon(xs, ys, 3);
			}
		}
	}
	@Override
	public void onEndGame() {
		// nothing to do here
	}
	/** The diplomacy screen. */
	class DiplomacyPanel extends UIComponent {
		@Override
		public void draw(Graphics2D g2) {
			
			int textSize = 10;
			int cellSize = 18;
			
			int maxw = commons.text().getTextWidth(textSize, player().shortName);
			int hlimit = 6;
			int maxh = hlimit * 9;
			
			List<Player> pl = new ArrayList<>();
			List<Player> war = new ArrayList<>();
			List<Player> ally = new ArrayList<>();
			pl.add(player());
			for (Player p : player().knownPlayers().keySet()) {
				if (p == player() || p.noDiplomacy) {
					continue;
				}
				maxw = Math.max(maxw, commons.text().getTextWidth(textSize, p.shortName));
				pl.add(p);
				if (player().knows(p)) {
					DiplomaticRelation dr = world().getRelation(player(), p);
					if (dr.value <= player().warThreshold && dr.value <= p.warThreshold) {
						war.add(p);
					} else
					if (!dr.alliancesAgainst.isEmpty()) {
						ally.add(p);
					} else
					if (dr.value >= 90) {
						ally.add(p);
					}
				}
			}
			
			maxw += 5;
			maxh += 5;
			
			g2.setColor(new Color(0xFF087B73));
			
			for (int i = 0; i <= pl.size(); i++) {
				g2.drawLine(maxw, maxh + i * cellSize, maxw + pl.size() * cellSize, maxh + i * cellSize);
				g2.drawLine(maxw + i * cellSize, maxh, maxw + i * cellSize,  maxh + pl.size() * cellSize);

				if (i < pl.size()) {
					Player p = pl.get(i);
					commons.text().paintTo(g2, 0, maxh + i * cellSize + (cellSize - textSize) / 2, textSize, p.color, p.shortName);
					for (int j = 0; j < p.shortName.length() && j < hlimit; j++) {
						commons.text().paintTo(g2, maxw + i * cellSize + (cellSize - 7) / 2, j * 9, 7, p.color, p.shortName.substring(j, j + 1));
					}
				}
			}
			
			int stanceHeight = 7;
			for (int i = 0; i < pl.size(); i++) {
				Player row = pl.get(i);
				for (int j = 0; j < pl.size(); j++) {
					Player col = pl.get(j);
					
					String stance = "-";
					int st = -1;
					if (i != j && row.knows(col)) {
						st = row.getStance(col);
						stance = Integer.toString(st);
					}
					int stanceColor = TextRenderer.GREEN;
					if (st >= 0) {
						if (st < 30) {
							stanceColor = TextRenderer.RED;
						} else
						if (st < 40) {
							stanceColor = TextRenderer.YELLOW;
						} else
						if (st > 80) {
							stanceColor = TextRenderer.LIGHT_BLUE;
						} else
						if (st > 60) {
							stanceColor = TextRenderer.LIGHT_GREEN;
						}
					}
					
					int sw = commons.text().getTextWidth(stanceHeight, stance);
					commons.text().paintTo(g2, 
							maxw + j * cellSize + (cellSize - sw) / 2,
							maxh + i * cellSize + (cellSize - stanceHeight) / 2,
							stanceHeight,
							stanceColor,
							stance
					);
				}				
			}
			
			commons.text().paintTo(g2, 0, maxh + pl.size() * cellSize + cellSize, 10, TextRenderer.GREEN, get("relations.allies"));

			int tx = commons.text().getTextWidth(10, get("relations.allies")) + 5;
			int ty = maxh + pl.size() * cellSize + cellSize;

			// paint allies
			int sep = commons.text().getTextWidth(textSize, ", ");
			int i = 0;
			if (ally.size() > 0) {
				for (Player ally0 : ally) {
					int w = commons.text().getTextWidth(textSize, ally0.name);
					if (tx + w >= width) {
						tx = 10;
						ty += textSize + 2;
					}
					commons.text().paintTo(g2, tx, ty, textSize, ally0.color, ally0.name);
					tx += w;
					if (i < ally.size() - 1) {
						commons.text().paintTo(g2, tx, ty, textSize, TextRenderer.GREEN, ",");
						tx += sep;
					}
					i++;
				}
			} else {
				commons.text().paintTo(g2, tx, ty, textSize, TextRenderer.GREEN, "-");
				ty += textSize + 2;
			}
			ty += 20;
			tx = commons.text().getTextWidth(10, get("relations.enemies")) + 5;
			commons.text().paintTo(g2, 0, ty, 10, TextRenderer.GREEN, get("relations.enemies"));
			// paint enemies
			i = 0;
			if (war.size() > 0) {
				for (Player war0 : war) {
					int w = commons.text().getTextWidth(textSize, war0.name);
					if (tx + w >= width) {
						tx = 10;
						ty += textSize + 2;
					}
					commons.text().paintTo(g2, tx, ty, textSize, war0.color, war0.name);
					tx += w;
					if (i < war.size() - 1) {
						commons.text().paintTo(g2, tx, ty, textSize, TextRenderer.GREEN, ",");
						tx += sep;
					}
					i++;
				}
			} else {
				commons.text().paintTo(g2, tx, ty, 10, TextRenderer.GREEN, "-");
			}
		}
	}
	/** Display information about the selected fleet. */
	void displayFleetInfo() {
		Fleet f = fleet();
		if (f == null) {
			fleetName.visible(false);
			fleetOwner.visible(false);
			fleetStatus.visible(false);
			fleetPlanet.visible(false);
			fleetPlanetLabel.visible(false);
			fleetFirepower.visible(false);
			fleetSpeed.visible(false);
			fleetBattleships.visible(false);
			fleetCruisers.visible(false);
			fleetFighters.visible(false);
			fleetVehicles.visible(false);
			return;
		}
		if (knowledge(f, FleetKnowledge.VISIBLE) > 0) {
			fleetName.text(f.name).visible(true);
			fleetOwner.text(f.owner.name, true).visible(true);
			fleetOwner.color(f.owner.color);
		} else {
			fleetName.text(get("fleetinfo.alien_fleet")).visible(true);
			fleetOwner.text("");
		}
		fleetName.color(f.owner.color);
		
		if (knowledge(f, FleetKnowledge.FULL) >= 0) {
			if (f.targetFleet == null && f.targetPlanet() == null) {
				if (f.waypoints.size() > 0) {
					fleetStatus.text(format("fleetstatus.moving"), true);
				} else {
					fleetStatus.text(format("fleetstatus.stopped"), true);
				}
			} else {
				if (f.mode == FleetMode.ATTACK) {
					if (f.targetFleet != null) {
						fleetStatus.text(format("fleetstatus.attack", f.targetFleet.name), true);
					} else {
						fleetStatus.text(format("fleetstatus.attack", f.targetPlanet().name()), true);
					}
				} else {
					if (f.targetFleet != null) {
						fleetStatus.text(format("fleetstatus.moving.after", f.targetFleet.name), true);
					} else {
						fleetStatus.text(format("fleetstatus.moving.to", f.targetPlanet().name()), true);
					}
				}
			}
		} else {
			if (f.waypoints.size() > 0 || f.targetFleet != null || f.targetPlanet() != null) {
				fleetStatus.text(format("fleetstatus.moving"), true);
			} else {
				fleetStatus.text(format("fleetstatus.stopped"), true);
			}
		}
		FleetStatistics fs = f.getStatistics();
		
		if (fs.planet != null && knowledge(fs.planet, PlanetKnowledge.VISIBLE) >= 0) {
			fleetPlanet.text(fs.planet.name(), true).visible(true);
			if (knowledge(fs.planet, PlanetKnowledge.OWNER) >= 0 && fs.planet.owner != null) {
				fleetPlanet.color(fs.planet.owner.color);
			} else {
				fleetPlanet.color(TextRenderer.GRAY);
			}
		} else {
			fleetPlanet.text("----", true).color(TextRenderer.GREEN).visible(true);
		}
		if (knowledge(f, FleetKnowledge.FULL) >= 0) {
			fleetFirepower.text(format("fleetstatus.firepower", fs.firepower), true).visible(true);
			fleetBattleships.text(format("fleetinformation.battleships", zeroDash(fs.battleshipCount)), true).visible(true);
			fleetCruisers.text(format("fleetinformation.cruisers", zeroDash(fs.cruiserCount)), true).visible(true);
			fleetFighters.text(format("fleetinformation.fighters", zeroDash(fs.fighterCount)), true).visible(true);
			fleetVehicles.text(format("fleetinformation.vehicles", zeroDash(fs.vehicleCount)), true).visible(true);
		} else	
		if (knowledge(f, FleetKnowledge.COMPOSITION) >= 0) {
			fleetFirepower.visible(false);
			int fcnt = fs.battleshipCount + fs.cruiserCount;
			fleetBattleships.visible(false);
			fleetCruisers.text(format("fleetinformation.spaceships", fcnt), true).visible(true);
			fleetFighters.text(format("fleetinformation.fighters", ((fs.fighterCount / 10) * 10) + ".." + ((fs.fighterCount  / 10 + 1) * 10)), true).visible(true);
//			fleetVehicles.text(format("fleetinformation.vehicles", ((fs.vehicleCount / 10) * 10) + ".." + ((fs.vehicleCount  / 10 + 1) * 10)), true).visible(true);
			fleetVehicles.visible(false);
		} else {
			fleetFirepower.visible(false);
			fleetBattleships.visible(false);
			fleetCruisers.visible(false);
			fleetFighters.visible(false);
			fleetVehicles.visible(false);
		}
		fleetSpeed.text(format("fleetstatus.speed", fs.speed), true).visible(true);
	}
	/**
	 * Replace the value with a dash if the value is zero.
	 * @param i the value
	 * @return a dash or the value as string
	 */
	String zeroDash(int i) {
		if (i == 0) {
			return "-";
		}
		return Integer.toString(i);
	}
	/** 
	 * Toggle the auto-build states. 
	 * @param all set on all planets?
	 */
	void doAutoBuild(boolean all) {
		AutoBuild ab = planet().autoBuild;
		int idx = (ab.ordinal() + 1) % AutoBuild.values().length;
		planet().autoBuild = AutoBuild.values()[idx];
		if (all) {
			for (Planet p : player().ownPlanets()) {
				p.autoBuild = planet().autoBuild;
			}
		}
	}
	@Override
	protected int margin() {
		return 11;
	}
	@Override
	protected Point scaleBase(int mx, int my) {
		UIMouse m = new UIMouse();
		m.x = mx;
		m.y = my;
		scaleMouse(m, base, margin()); 
		return new Point(m.x, m.y);
	}
	@Override
	protected Pair<Point, Double> scale() {
		Pair<Point, Double> s = scale(base, margin());
		return Pair.of(new Point(base.x, base.y), s.second);
	}
}
