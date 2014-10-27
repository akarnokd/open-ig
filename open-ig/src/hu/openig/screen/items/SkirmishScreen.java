/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.core.Action0;
import hu.openig.core.Action1;
import hu.openig.core.Difficulty;
import hu.openig.core.Func1;
import hu.openig.core.ResourceType;
import hu.openig.model.GalaxyGenerator;
import hu.openig.model.GalaxyGenerator.PlanetCandidate;
import hu.openig.model.GalaxyModel;
import hu.openig.model.GameDefinition;
import hu.openig.model.ModelUtils;
import hu.openig.model.ResourceLocator.ResourcePlace;
import hu.openig.model.Screens;
import hu.openig.model.SkirmishAIMode;
import hu.openig.model.SkirmishDefinition;
import hu.openig.model.SkirmishDiplomaticRelation;
import hu.openig.model.SkirmishPlayer;
import hu.openig.model.SoundType;
import hu.openig.model.Traits;
import hu.openig.model.World;
import hu.openig.render.RenderTools;
import hu.openig.render.TextRenderer;
import hu.openig.screen.ScreenBase;
import hu.openig.screen.panels.ListSpinBox;
import hu.openig.screen.panels.NumberSpinBox;
import hu.openig.screen.panels.SpinBox;
import hu.openig.scripting.SkirmishScripting;
import hu.openig.ui.HorizontalAlignment;
import hu.openig.ui.UICheckBox;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIGenericButton;
import hu.openig.ui.UIImage;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UILabel;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Button;
import hu.openig.ui.UIMouse.Type;
import hu.openig.ui.UIPanel;
import hu.openig.ui.UIRadioButton;
import hu.openig.ui.UIScrollBox;
import hu.openig.ui.VerticalAlignment;
import hu.openig.utils.ImageUtils;
import hu.openig.utils.U;
import hu.openig.utils.XElement;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Semaphore;

import javax.swing.SwingUtilities;

/**
 * The skirmish configuration screen.
 * @author akarnokd, 2012.08.18.
 */
public class SkirmishScreen extends ScreenBase {
	/** The panel base rectangle. */
	final Rectangle base = new Rectangle(0, 0, 640, 480);
	/** Panel. */
	UIPanel galaxyPanel;
	/** Panel. */
	UIPanel economyPanel;
	/** Panel. */
	UIPanel playersPanel;
	/** Panel. */
	UIPanel victoryPanel;
	/** Panel. */
	UIPanel overridesPanel;
	/** Button. */
	UIGenericButton galaxyBtn;
	/** Button. */
	UIGenericButton economyBtn;
	/** Button. */
	UIGenericButton playersBtn;
	/** Back to the main screen. */
	UIGenericButton back;
	/** Load skirmish settings. */
	UIGenericButton load;
	/** Save skirmish settings. */
	UIGenericButton save;
	/** Play skirmish. */
	UIGenericButton play;
	/** Button. */
	UIGenericButton victoryBtn;
	/** Button. */
	UIGenericButton overridesBtn;
	/** The galactic background. */
	BufferedImage background;
	/** The list of campaigns. */
	final List<GameDefinition> campaigns = new ArrayList<>();
	/** The galaxy definition label. */
	UILabel galaxyDefLabel;
	/** The galaxy definition spin-box. */
	CampaignSpinBox galaxyDef;
	/** Randomize surfaces. */
	UICheckBox galaxyRandomSurface;
	/** Randomize layout. */
	UICheckBox galaxyRandomLayout;
	/** Label describing the seed value. */
	UILabel galaxyRandomSeedLabel;
	/** The random seed for generating the galaxy layout. */
	UILabel galaxyRandomSeed;
	/** Reseed with new value. */
	UIGenericButton galaxyReseed;
	/** Custom planet count. */
	UICheckBox galaxyCustomPlanets;
	/** The planet count. */
	NumberSpinBox galaxyPlanetCount;
	/** The race template. */
	CampaignSpinBox galaxyRaces;
	/** The race template label. */
	UILabel galaxyRacesLabel;
	/** The technology definition label. */
	UILabel technologyDefLabel;
	/** The technology definition. */
	CampaignSpinBox technologyDef;
	/** Technology level label. */
	UILabel technologyLevelLabel;
	/** Technology level label. */
	UILabel technologyLevelMaxLabel;
	/** Technology level start. */
	NumberSpinBox technologyLevelStart;
	/** Technology level maximum. */
	NumberSpinBox technologyLevelMax;
	/** Don't limit the number of labs per planet. */
	UICheckBox noLabLimit;
	/** Don't limit the number of labs per planet. */
	UICheckBox noFactoryLimit;
	/** Don't limit the number of economic buildings per planet. */
	UICheckBox noEconomicLimit;
	/** Allow all buildings to be built by non-human races. */
	UICheckBox allowAllBuildings;
	/** Label. */
	UILabel initialMoneyLabel;
	/** Number. */
	NumberSpinBox initialMoney;
	/** Label. */
	UILabel initialPlanetsLabel;
	/** Number. */
	NumberSpinBox initialPlanets;
	/** Label. */
	UILabel initialPopulationLabel;
	/** Number. */
	NumberSpinBox initialPopulation;
	/** Checkbox. */
	UICheckBox placeColonyHub;
	/** Checkbox. */
	UICheckBox grantColonyShip;
	/** Checkbox. */
	UICheckBox grantOrbitalFactory;
	/** Label. */
	UILabel colonyShipLabel;
	/** Number. */
	NumberSpinBox colonyShips;
	/** Label. */
	UILabel orbitalFactoryLabel;
	/** Number. */
	NumberSpinBox orbitalFactories;
	/** Label. */
	UILabel initialRelationLabel;
	/** Spin. */
	ListSpinBox<SkirmishDiplomaticRelation> initialRelation;
	/** Label. */
	UILabel initialDifficultyLabel;
	/** Spin. */
	ListSpinBox<Difficulty> initialDifficulty;
	/** Win condition. */
	UICheckBox winConquest;
	/** Win condition. */
	UICheckBox winOccupation;
	/** Win condition. */
	UICheckBox winEconomic;
	/** Win condition. */
	UICheckBox winTechnology;
	/** Win condition. */
	UICheckBox winSocial;
	/** Number. */
	NumberSpinBox winOccupationPercent;
	/** Number. */
	NumberSpinBox winOccupationTime;
	/** Number. */
	NumberSpinBox winEconomicMoney;
	/** Number. */
	NumberSpinBox winSocialMorale;
	/** Number. */
	NumberSpinBox winSocialPlanets;
	/** Number. */
	UILabel winOccupationPercentLabel;
	/** Number. */
	UILabel winOccupationTimeLabel;
	/** Number. */
	UILabel winEconomicMoneyLabel;
	/** Number. */
	UILabel winSocialMoraleLabel;
	/** Number. */
	UILabel winSocialPlanetsLabel;
	/** Players list parent panel. */
	UIPanel playersList;
	/** Players list scoll box. */
	UIScrollBox playersListScroll;
	/** The template players. */
	final List<SkirmishPlayer> templatePlayers = new ArrayList<>();
	/** The tech races. */
	final Set<String> templateTechRaces = new HashSet<>();
	/** The list of players. */
	final List<PlayerLine> playerLines = new ArrayList<>();
	/** Add player button. */
	UIGenericButton addPlayer;
	/** Clear players button. */
	UIGenericButton removePlayers;
	/** Column. */
	UICheckBox colName;
	/** Column. */
	UILabel colRace;
	/** Column. */
	UILabel colIcon;
	/** Column. */
	UILabel colAI;
	/** Column. */
	UILabel colTrait;
	/** Column. */
	UILabel colGroup;
	/** Column. */
	UILabel colPlanet;
	/** The group selection panel. */
	GroupSelectPanel groupSelectPanel;
	/** The icon selection panel. */
	IconSelectPanel iconSelectPanel;
	/** An item selection panel. */
	ItemSelectPanel itemSelectPanel;
	/** The starmap panel. */
	StarmapPanel starmapPanel;
	/** Tax base label. */
	UILabel taxBaseLabel;
	/** Base daily tax. */
	NumberSpinBox taxBase;
	/** Tax scale label. */
	UILabel taxScaleLabel;
	/** Tax scaling. */
	NumberSpinBox taxScale;
	/** The video playback completion waiter. */
	volatile Thread videoWaiter;
	/** The load waiter. */
	volatile Thread loadWaiter;
	/** The blink timer. */
	Closeable blinkTimer;
	/** The blink state. */
	boolean blink;
	/** The last galaxy seed value. */
	Long galaxySeed;
	/** Tax base label. */
	UILabel planetScaleLabel;
	/** Base daily tax. */
	NumberSpinBox planetScale;
	@Override
	public Screens screen() {
		return Screens.SKIRMISH;
	}

	@Override
	public void onInitialize() {
		galaxyPanel = new UIPanel();
		economyPanel = new UIPanel();
		playersPanel = new UIPanel();
		victoryPanel = new UIPanel();
		overridesPanel = new UIPanel();
		
		galaxyBtn = createButton("skirmish.galaxy");
		economyBtn = createButton("skirmish.economy");
		playersBtn = createButton("skirmish.players");
		victoryBtn = createButton("skirmish.victory");
		overridesBtn = createButton("skirmish.other");
		
		back = createButton("skirmish.back");
		back.onClick = new Action0() {
			@Override
			public void invoke() {
				displayPrimary(Screens.MAIN);
			}
		};
		load = createButton("skirmish.load");
		load.disabledPattern(commons.common().disabledPattern);
		load.enabled(false);
		load.visible(false);
		
		save = createButton("skirmish.save");
		save.disabledPattern(commons.common().disabledPattern);
		save.enabled(false);
		save.visible(false);
		
		play = createButton("skirmish.play");
		play.disabledPattern(commons.common().disabledPattern);
		play.onClick = new Action0() {
			@Override
			public void invoke() {
				doPlay();
			}
		};
		
		galaxyBtn.onPress = panelSwitchAction(galaxyPanel);
		economyBtn.onPress = panelSwitchAction(economyPanel);
		playersBtn.onPress = panelSwitchAction(playersPanel);
		victoryBtn.onPress = panelSwitchAction(victoryPanel);
		overridesBtn.onPress = panelSwitchAction(overridesPanel);

		galaxyDefLabel = createLabel("skirmish.galaxy_template");
		galaxyDef = new CampaignSpinBox();
		
		galaxyPanel.add(galaxyDefLabel, galaxyDef);
		
		galaxyRandomSurface = createCheckBox("skirmish.random_surface");
		galaxyRandomLayout = createCheckBox("skirmish.random_layout");
		galaxyRandomLayout.onChange = new Action0() {
			@Override
			public void invoke() {
				clearPlanetAssociations();
			}
		};
		galaxyCustomPlanets = createCheckBox("skirmish.custom_planets");
		galaxyCustomPlanets.onChange = galaxyRandomLayout.onChange;
		
		galaxyRandomSeedLabel = createLabel("skirmish.galaxy_seed");
		galaxyRandomSeed = createLabel2(String.format("%20s", ""));
		galaxyReseed = createButton("skirmish.reseed");
		galaxyReseed.onClick = new Action0() {
			@Override
			public void invoke() {
				doReseed();
			}
		};
		
		galaxyPlanetCount = new NumberSpinBox(commons, 0, 500, 1, 10);
		galaxyPlanetCount.value = 100;
		
		galaxyPanel.add(galaxyRandomSurface, galaxyRandomLayout, galaxyCustomPlanets, galaxyPlanetCount,
				galaxyRandomSeedLabel, galaxyRandomSeed, galaxyReseed);
		
		galaxyRaces = new CampaignSpinBox() {
			@Override
			public void update() {
				super.update();
				
				templatePlayers.clear();
				templatePlayers.addAll(commons.getPlayersFrom(get()));
			}
		};
		galaxyRacesLabel = createLabel("skirmish.race_template");
		
		galaxyPanel.add(galaxyRaces, galaxyRacesLabel);
		
		technologyDef = new CampaignSpinBox() {
			@Override
			public void update() {
				super.update();
				templateTechRaces.clear();
				templateTechRaces.addAll(getRacesFrom(get()));
			}
		};
		technologyDefLabel = createLabel("skirmish.tech_template");

		technologyLevelLabel = createLabel("skirmish.tech_level");
		technologyLevelMaxLabel = createLabel("skirmish.tech_level_max");
		
		technologyLevelMax = new NumberSpinBox(commons, 1, 5, 1, 1);
		technologyLevelMax.value = 5;

		technologyLevelStart = new NumberSpinBox(commons, 0, 6, 1, 1);
		technologyLevelStart.value = 0;

		galaxyPanel.add(technologyDef, technologyDefLabel, technologyLevelLabel, 
				technologyLevelStart, technologyLevelMaxLabel, technologyLevelMax);
		
		
		initialMoneyLabel = createLabel("skirmish.initial_money");
		initialMoney = new NumberSpinBox(commons, 0, 2_000_000_000, 10_000, 100_000) {
			@Override
			public String onValue() {
				return String.format("%,d cr", value);
			}
		};
		initialMoney.value = 200000;
		
		initialPlanetsLabel = createLabel("skirmish.initial_planets");
		initialPlanets = new NumberSpinBox(commons, 1, 500, 1, 10);
		initialPlanets.value = 3;
		
		economyPanel.add(
				initialMoneyLabel, initialMoney, 
				initialPlanetsLabel, initialPlanets);
		
		initialPopulationLabel = createLabel("skirmish.initial_population");
		initialPopulation = new NumberSpinBox(commons, 0, 1000000, 100, 1000);
		initialPopulation.value = 5000;
		
		placeColonyHub = createCheckBox("skirmish.place_colony_hub");
		placeColonyHub.selected(true);
		grantColonyShip = createCheckBox("skirmish.grant_colonyship");
		grantOrbitalFactory = createCheckBox("skirmish.grant_orbital_factory");
		
		economyPanel.add(initialPopulationLabel, initialPopulation, placeColonyHub,
				grantColonyShip, grantOrbitalFactory);
		
		colonyShipLabel = createLabel("skirmish.colony_ships");
		colonyShips = new NumberSpinBox(commons, 0, 1000, 1, 10);
		colonyShips.value = 1;
		
		orbitalFactoryLabel = createLabel("skirmish.orbital_factories");
		orbitalFactories = new NumberSpinBox(commons, 0, 1000, 1, 10);
		
		economyPanel.add(colonyShipLabel, colonyShips, orbitalFactoryLabel, orbitalFactories);
		
		taxBaseLabel = createLabel("skirmish.tax_base");
		taxBase = new NumberSpinBox(commons, 0, 100_000_000, 1000, 10_000) {
			@Override
			public String onValue() {
				return String.format("%,d cr", value);
			}
		};
		taxScaleLabel = createLabel("skirmish.tax_scale");
		taxScale = new NumberSpinBox(commons, 100, 1000, 5, 25) {
			@Override
			public String onValue() {
				return String.format("%d %%", value);
			}
		};
		
		economyPanel.add(taxBaseLabel, taxBase, taxScaleLabel, taxScale);
		
		
		initialRelationLabel = createLabel("skirmish.initial_relation");
		initialRelation = new ListSpinBox<>(commons, new Func1<SkirmishDiplomaticRelation, String>() { 
			@Override
			public String invoke(SkirmishDiplomaticRelation value) {
				return get("skirmish.relation." + value);
			}
		}, SkirmishDiplomaticRelation.values());
		initialRelation.index = SkirmishDiplomaticRelation.DEFAULT.ordinal();
		
		initialDifficultyLabel = createLabel("skirmish.initial_difficulty");
		initialDifficulty = new ListSpinBox<>(commons, new Func1<Difficulty, String>() { 
			@Override
			public String invoke(Difficulty value) {
				return get("difficulty." + value);
			}
		}, Difficulty.values());
		initialDifficulty.index = Difficulty.NORMAL.ordinal();
		
		galaxyPanel.add(initialRelationLabel, initialRelation, initialDifficultyLabel, initialDifficulty);

		winConquest = createCheckBox("skirmish.conquest");
		setTooltip(winConquest, "skirmish.conquest.tooltip");
		winOccupation = createCheckBox("skirmish.occupation");
		setTooltip(winOccupation, "skirmish.occupation.tooltip");
		winEconomic = createCheckBox("skirmish.economic");
		setTooltip(winEconomic, "skirmish.economic.tooltip");
		winTechnology = createCheckBox("skirmish.technology");
		setTooltip(winTechnology, "skirmish.technology.tooltip");
		winSocial = createCheckBox("skirmish.social");
		setTooltip(winSocial, "skirmish.social.tooltip");
		
		winOccupationPercent = new NumberSpinBox(commons, 0, 100, 1, 10) {
			@Override
			public String onValue() {
				return String.format("%,d %%", value);
			}
		};
		winOccupationPercent.value = 66;
		
		final String fdays = get("skirmish.days");
		winOccupationTime = new NumberSpinBox(commons, 0, 1000, 1, 10) {
			@Override
			public String onValue() {
				return String.format("%,d %s", value, fdays);
			}
		};
		winOccupationTime.value = 30;
		
		winEconomicMoney = new NumberSpinBox(commons, 0, 2000000000, 1000000, 10000000) {
			@Override
			public String onValue() {
				return String.format("%,d cr", value);
			}
		};
		winEconomicMoney.value = 10000000;
		
		winSocialMorale = new NumberSpinBox(commons, 0, 100, 1, 10) {
			@Override
			public String onValue() {
				return String.format("%,d %%", value);
			}
		};
		winSocialMorale.value = 95;
		
		winSocialPlanets = new NumberSpinBox(commons, 0, 500, 1, 10);
		winSocialPlanets.value = 30;

		victoryPanel.add(winConquest, winOccupation, winEconomic, winTechnology, winSocial);
		victoryPanel.add(winOccupationPercent, winOccupationTime, winEconomicMoney, winSocialMorale, winSocialPlanets);

		winOccupationPercentLabel = createLabel("skirmish.occupation_percent");
		winOccupationTimeLabel = createLabel("skirmish.occupation_time");
		winEconomicMoneyLabel = createLabel("skirmish.economic_money");
		winSocialMoraleLabel = createLabel("skirmish.social_morale");
		winSocialPlanetsLabel = createLabel("skirmish.social_planets");

		victoryPanel.add(winOccupationPercentLabel, winOccupationTimeLabel, winEconomicMoneyLabel,
				winSocialMoraleLabel, winSocialPlanetsLabel);

		// ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
		
		noFactoryLimit = createCheckBox("skirmish.no_factory_limit");
		noLabLimit = createCheckBox("skirmish.no_lab_limit");
		noEconomicLimit = createCheckBox("skirmish.no_economic_limit");
		allowAllBuildings = createCheckBox("skirmish.allow_all_buildings");
		allowAllBuildings.selected(true);
		
		planetScaleLabel = createLabel("skirmish.planet_scale");
		planetScale = new NumberSpinBox(commons, 1, 4, 1, 1) {
			@Override
			public String onValue() {
				return String.format("x %d", value);
			}
		};

		overridesPanel.add(noFactoryLimit, noLabLimit, noEconomicLimit, allowAllBuildings,
				planetScaleLabel, planetScale);
		
		// -----------------------------------------------------------------------
		
		playersList = new UIPanel();
		
		playersListScroll = new UIScrollBox(playersList, 30,
				new UIImageButton(commons.database().arrowUp),
				new UIImageButton(commons.database().arrowDown)
		);
		playersPanel.add(playersListScroll);
		
		addPlayer = createButton("skirmish.add_player");
		addPlayer.onClick = new Action0() {
			@Override
			public void invoke() {
				doAddPlayer();
			}
		};
		removePlayers = createButton("skirmish.remove_players");
		removePlayers.onClick = new Action0() {
			@Override
			public void invoke() {
				doRemovePlayers();
			}
		};
		removePlayers.disabledPattern(commons.common().disabledPattern);
		
		playersPanel.add(addPlayer, removePlayers);

		colName = createCheckBox("skirmish.name");
		colName.backgroundColor(0);
		colName.onChange = new Action0() {
			@Override
			public void invoke() {
				doSelectAll(colName.selected());
				doManageRemove();
			}
		};
		colRace = createLabel("skirmish.race");
		colIcon = createLabel("skirmish.icon");
		colAI = createLabel("skirmish.ai");
		colAI.horizontally(HorizontalAlignment.CENTER);
		colTrait = createLabel("skirmish.trait");
		colGroup = createLabel("skirmish.group");
		colPlanet = createLabel("skirmish.initial_planet");
		
		colName.color(TextRenderer.LIGHT_GREEN);
		colIcon.color(TextRenderer.LIGHT_GREEN);
		colRace.color(TextRenderer.LIGHT_GREEN);
		colAI.color(TextRenderer.LIGHT_GREEN);
		colTrait.color(TextRenderer.LIGHT_GREEN);
		colGroup.color(TextRenderer.LIGHT_GREEN);
		colPlanet.color(TextRenderer.LIGHT_GREEN);
		
		playersPanel.add(colName, colRace, colIcon, colAI, colTrait, colGroup, colPlanet);

		groupSelectPanel = new GroupSelectPanel();
		groupSelectPanel.visible(false);
		
		iconSelectPanel = new IconSelectPanel();
		iconSelectPanel.visible(false);
		
		itemSelectPanel = new ItemSelectPanel();
		itemSelectPanel.visible(false);
		
		starmapPanel = new StarmapPanel();
		starmapPanel.visible(false);
		
		addThis();
	}
	@Override
	public void onResize() {
		base.y = 10;
		base.height = height - 20;

		RenderTools.centerScreen(base, width, height, false);

		galaxyPanel.bounds(base.x + 5, base.y + 50, base.width - 10, base.height - 60);
		economyPanel.bounds(galaxyPanel.bounds());
		playersPanel.bounds(galaxyPanel.bounds());
		victoryPanel.bounds(galaxyPanel.bounds());
		overridesPanel.bounds(galaxyPanel.bounds());
		
		galaxyBtn.location(base.x + 5, base.y + 5);
		economyBtn.location(galaxyBtn.x + galaxyBtn.width + 5, base.y + 5);
		playersBtn.location(economyBtn.x + economyBtn.width + 5, base.y + 5);
		victoryBtn.location(playersBtn.x + playersBtn.width + 5, base.y + 5);
		overridesBtn.location(victoryBtn.x + victoryBtn.width + 5, base.y + 5);
		
		back.location(base.x + 10, base.y + base.height - 40);
		
		load.location(base.x + base.width / 2 - 5 - load.width, back.y);
		save.location(base.x + base.width / 2 + 5, back.y);
		
		play.location(base.x + base.width - 10 - play.width, back.y);
		
		int cy = 0;
		galaxyDefLabel.location(5, cy + 7);
		galaxyDef.setMaxSize();
		galaxyDef.location(galaxyPanel.width - galaxyDef.width - 5, cy);
		
		cy += 35;
		galaxyRandomSurface.location(5, cy + 7);
		
		cy += 35;
		galaxyRandomLayout.location(5, cy + 7);

		cy += 35;
		galaxyCustomPlanets.location(30, cy + 7);
		galaxyPlanetCount.setMaxSize();
		galaxyPlanetCount.location(30 + galaxyCustomPlanets.width + 20, cy);
		
		cy += 35;
		galaxyRandomSeedLabel.location(30, cy + 7);
		galaxyRandomSeed.location(25 + galaxyRandomSeedLabel.x + galaxyRandomSeedLabel.width, cy + 7);
		galaxyReseed.location(10 + galaxyRandomSeed.x + galaxyRandomSeed.width, cy);
		
		cy += 35;
		galaxyRacesLabel.location(5, cy + 7);
		galaxyRaces.setMaxSize();
		galaxyRaces.location(galaxyPanel.width - galaxyRaces.width - 5, cy);

		cy += 35;
		technologyDefLabel.location(5, cy + 7);
		technologyDef.setMaxSize();
		technologyDef.location(galaxyPanel.width - technologyDef.width - 5, cy);
		
		cy += 35;
		technologyLevelLabel.location(5, cy + 7);
		technologyLevelStart.setMaxSize();
		technologyLevelStart.location(5 + technologyLevelLabel.width + 20, cy);

		technologyLevelMaxLabel.location(technologyLevelStart.x + technologyLevelStart.width + 30, cy + 7);
		technologyLevelMax.setMaxSize();
		technologyLevelMax.location(technologyLevelMaxLabel.x + technologyLevelMaxLabel.width + 20, cy);

		cy += 35;
		initialRelationLabel.location(5, cy + 7);
		initialRelation.setMaxSize();
		initialRelation.location(5 + initialRelationLabel.width + 20, cy);
		
		cy += 35;
		initialDifficultyLabel.location(5, cy + 7);
		initialDifficulty.setMaxSize();
		initialDifficulty.location(5 + initialDifficultyLabel.width + 20, cy);

		//---------------------------------------------
		cy = 0;
		initialMoneyLabel.location(5, cy + 7);
		initialMoney.setMaxSize();
		initialMoney.location(5 + initialMoneyLabel.width + 20, cy);
		
		cy += 35;
		initialPlanetsLabel.location(5, cy + 7);
		initialPlanets.setMaxSize();
		initialPlanets.location(5 + initialPlanetsLabel.width + 20, cy);
		
		cy += 35;
		initialPopulationLabel.location(5, cy + 7);
		initialPopulation.setMaxSize();
		initialPopulation.location(5 + initialPopulationLabel.width + 20, cy);

		cy += 35;
		placeColonyHub.location(5, cy + 7);

		cy += 35;
		grantColonyShip.location(5, cy + 7);

		cy += 35;
		colonyShipLabel.location(30, cy + 7);
		colonyShips.setMaxSize();
		colonyShips.location(15 + colonyShipLabel.width + 20, cy);

		cy += 35;
		grantOrbitalFactory.location(5, cy + 7);

		cy += 35;
		orbitalFactoryLabel.location(30, cy + 7);
		orbitalFactories.setMaxSize();
		orbitalFactories.location(15 + orbitalFactoryLabel.width + 20, cy);
		
		cy += 35;
		taxBaseLabel.location(5, cy + 7);
		taxBase.setMaxSize();
		taxBase.location(15 + taxBaseLabel.width + 20, cy);

		cy += 35;
		taxScaleLabel.location(5, cy + 7);
		taxScale.setMaxSize();
		taxScale.location(15 + taxScaleLabel.width + 20, cy);

		// ---------------------------------------------------
		cy = 0;
		winConquest.location(5, cy + 7);
		
		cy += 35;
		winOccupation.location(5, cy + 7);
		
		cy += 35;
		winOccupationPercentLabel.location(30, cy + 7);
		winOccupationPercent.setMaxSize();
		winOccupationPercent.location(50 + winOccupationPercentLabel.width, cy);

		cy += 35;
		winOccupationTimeLabel.location(30, cy + 7);
		winOccupationTime.setMaxSize();
		winOccupationTime.location(50 + winOccupationTimeLabel.width, cy);
		
		cy += 35;
		winEconomic.location(5, cy + 7);

		cy += 35;
		winEconomicMoneyLabel.location(30, cy + 7);
		winEconomicMoney.setMaxSize();
		winEconomicMoney.location(50 + winEconomicMoneyLabel.width, cy);

		cy += 35;
		winTechnology.location(5, cy + 7);
		
		cy += 35;
		winSocial.location(5, cy + 7);

		cy += 35;
		winSocialMoraleLabel.location(30, cy + 7);
		winSocialMorale.setMaxSize();
		winSocialMorale.location(50 + winSocialMoraleLabel.width, cy);

		cy += 35;
		winSocialPlanetsLabel.location(30, cy + 7);
		winSocialPlanets.setMaxSize();
		winSocialPlanets.location(50 + winSocialPlanetsLabel.width, cy);

		//------------------------------------------
		
		cy = 0;
		noFactoryLimit.location(5, cy);

		cy += 35;
		noLabLimit.location(5, cy);

		cy += 35;
		noEconomicLimit.location(5, cy);

		cy += 35;
		allowAllBuildings.location(5, cy);
		
		cy += 35;
		planetScaleLabel.location(5, cy + 7);
		planetScale.setMaxSize();
		planetScale.location(25 + planetScaleLabel.width, cy);
		
		//------------------------------------------
		
		playersListScroll.bounds(5, 25, playersPanel.width - 5, playersPanel.height - 105);
		playersListScroll.scrollBy(0);
		playersListScroll.adjustButtons();

		playersList.width = playersListScroll.width - 40;

		removePlayers.location(playersPanel.width - 5 - removePlayers.width, playersPanel.height - 75);
		addPlayer.location(removePlayers.x - 10 - addPlayer.width, removePlayers.y);
		
		groupSelectPanel.bounds(base);
		groupSelectPanel.layout();
		
		iconSelectPanel.bounds(base);
		iconSelectPanel.layout();
		
		itemSelectPanel.bounds(base);
		itemSelectPanel.layout();
		
		starmapPanel.bounds(base);
		starmapPanel.layout();
		
		layoutPlayers();
	}
	/**
	 * Create a checkbox.
	 * @param label the text label
	 * @return the component
	 */
	UICheckBox createCheckBox(String label) {
		return new UICheckBox(get(label), 14, commons.common().checkmark, commons.text());
	}
	/**
	 * Create a checkbox.
	 * @param text the concrete text
	 * @return the component
	 */
	UICheckBox createCheckBox2(String text) {
		return new UICheckBox(text, 14, commons.common().checkmark, commons.text());
	}
	/**
	 * Create a label.
	 * @param label the text label
	 * @return the label component
	 */
	UILabel createLabel(String label) {
		return new UILabel(get(label), 14, commons.text());
	}
	/**
	 * Create a label.
	 * @param text the concrete text
	 * @return the label component
	 */
	UILabel createLabel2(String text) {
		return new UILabel(text, 14, commons.text());
	}
	/**
	 * Create a panel switch action that updates the controls.
	 * @param panel the panel
	 * @return the action
	 */
	Action0 panelSwitchAction(final UIPanel panel) {
		return new Action0() {
			@Override
			public void invoke() {
				doSelectPanel(panel, true);
			}
		};
	}
	/**
	 * Select the specified panel.
	 * @param panel the panel
	 * @param sound play sound?
	 */
	void doSelectPanel(UIPanel panel, boolean sound) {
		galaxyPanel.visible(galaxyPanel == panel);
		economyPanel.visible(economyPanel == panel);
		playersPanel.visible(playersPanel == panel);
		victoryPanel.visible(victoryPanel == panel);
		overridesPanel.visible(overridesPanel == panel);
		
		galaxyBtn.color(galaxyPanel.visible() ? TextRenderer.WHITE : 0xFF000000);
		economyBtn.color(economyPanel.visible() ? TextRenderer.WHITE : 0xFF000000);
		playersBtn.color(playersPanel.visible() ? TextRenderer.WHITE : 0xFF000000);
		victoryBtn.color(victoryPanel.visible() ? TextRenderer.WHITE : 0xFF000000);
		overridesBtn.color(overridesPanel.visible() ? TextRenderer.WHITE : 0xFF000000);
		if (sound) {
			buttonSound(SoundType.UI_ACKNOWLEDGE_2);
		}
	}
	/**
	 * Create a medium button.
	 * @param titleLabel the title label
	 * @return the button
	 */
	UIGenericButton createButton(String titleLabel) {
		UIGenericButton btn = new UIGenericButton(get(titleLabel), commons.control().fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
		return btn;
	}
	@Override
	public void onEnter(Screens mode) {
		background = rl.getImage("starmap/background");

		blinkTimer = commons.register(500, new Action0() {
			@Override
			public void invoke() {
				blink = !blink;
				askRepaint();
			}
		});
		
		scanCampaigns();

		onResize();
		
		List<UIComponent> all = new ArrayList<>();
		U.addAll(all, galaxyPanel.components());
		U.addAll(all, economyPanel.components()); 
		U.addAll(all, victoryPanel.components()); 
		
		for (UIComponent c : all) {
			if (c instanceof SpinBox) {
				((SpinBox)c).update();
			}
		}
		
		doSelectPanel(galaxyPanel, false);
		doManageRemove();
		doManagePlay();
		
		if (galaxySeed == null) {
			doReseed();
		}
	}
	
	/**
	 * Scan for campaign definitions.
	 */
	void scanCampaigns() {
		campaigns.clear();
		for (String name : commons.rl.listDirectories(commons.config.language, "campaign/")) {
			GameDefinition gd = GameDefinition.parse(commons.rl, "campaign/" + name);
			campaigns.add(gd);
		}
		for (String name : commons.rl.listDirectories(commons.config.language, "skirmish/")) {
			GameDefinition gd = GameDefinition.parse(commons.rl, "skirmish/" + name);
			campaigns.add(gd);
		}
		Collections.sort(campaigns, new Comparator<GameDefinition>() {
			@Override
			public int compare(GameDefinition o1, GameDefinition o2) {
				return o1.name.compareToIgnoreCase(o2.name);
			}
		});
	}

	@Override
	public void onLeave() {
		background = null;
		U.close(blinkTimer);
		blinkTimer = null;
	}

	@Override
	public void onFinish() {
		if (videoWaiter != null) {
			videoWaiter.interrupt();
			videoWaiter = null;
		}
		if (loadWaiter != null) {
			loadWaiter.interrupt();
			loadWaiter = null;
		}
		galaxySeed = null;
	}

	@Override
	public void onEndGame() {

	}
	@Override
	public void draw(Graphics2D g2) {
		if (background != null) {
			g2.drawImage(background, 0, 0, width, height, null);
			g2.setColor(new Color(0, 0, 0, 192));
			g2.fillRect(base.x, base.y, base.width, base.height);
		} else {
			g2.setColor(Color.BLACK);
			g2.fillRect(0, 0, width, height);
		}
		g2.setColor(Color.GRAY);
		g2.drawRect(base.x, base.y, base.width - 1, base.height - 1);

		g2.drawLine(base.x, galaxyBtn.y + 5 + galaxyBtn.height, base.x + base.width - 1, galaxyBtn.y + 5 + galaxyBtn.height);
		g2.drawLine(base.x, back.y - 5, base.x + base.width - 1, back.y - 5);
		
		super.draw(g2);
	}
	@Override
	public boolean keyboard(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			if (groupSelectPanel.visible()) {
				groupSelectPanel.visible(false);
				e.consume();
				commons.control().moveMouse();
				return true;
			} else 
			if (iconSelectPanel.visible()) {
				iconSelectPanel.visible(false);
				commons.control().moveMouse();
				e.consume();
				return true;
			} else
			if (itemSelectPanel.visible()) {
				itemSelectPanel.hide();
				commons.control().moveMouse();
				e.consume();
				return true;
			} else
			if (starmapPanel.visible()) {
				starmapPanel.hide();
				commons.control().moveMouse();
				e.consume();
				return true;
			}
			back.onClick.invoke();
			e.consume();
			return true;
		}
		return super.keyboard(e);
	}
	/**
	 * Spin box referencing the campaign.
	 * @author akarnokd, 2012.08.20.
	 */
	public class CampaignSpinBox extends ListSpinBox<GameDefinition> {
		/** Constructor. */
		public CampaignSpinBox() {
			super(SkirmishScreen.this.commons, campaigns, new Func1<GameDefinition, String>() {
				@Override
				public String invoke(GameDefinition value) {
					return value.getTitle(rl.language);
				}
			});
		}
		/** Set the tooltip. */
		@Override
		public void update() {
			GameDefinition d = get();
			
			setTooltipText(spin, d.getDescription(rl.language));

			super.update();
		}
		/**
		 * @return Get the current indexed campaign.
		 */
		public GameDefinition get() {
			return campaigns.get(index);
		}
	}
	/**
	 * Get the races from the tech and building definitions.
	 * @param def the definition
	 * @return the set of races
	 */
	public Set<String> getRacesFrom(GameDefinition def) {
		Set<String> result = new HashSet<>();
		
		XElement xtech = rl.getXML(def.tech);
		for (XElement xitem : xtech.childrenWithName("item")) {
			String r = xitem.get("race");
			
			result.addAll(Arrays.asList(r.split("\\s*,\\s*")));
		}
		XElement xbuild = rl.getXML(def.buildings);
		
		for (XElement xb : xbuild.childrenWithName("building")) {
			for (XElement xg : xb.childrenWithName("graphics")) {
				for (XElement xt : xg.childElement("tech").children()) {
					result.add(xt.get("id"));
				}
			}
		}
		
		return result;
	}
	/**
	 * A player definition. 
	 * @author akarnokd, 2012.08.20.
	 */
	public class PlayerLine extends UIPanel {
		/** The player definition. */
		public SkirmishPlayer player;
		/** The player name. */
		public UICheckBox name;
		/** The race. */
		public UILabel race;
		/** The image. */
		public UIImage icon;
		/** The AI. */
		public UILabel ai;
		/** Traits. */
		public UILabel traits;
		/** Group. */
		public UILabel group;
		/** The preferred starting planet. */
		public UIImage planet;
		/** Cunstructs the UI elements. */
		public PlayerLine() {
			name = createCheckBox2("");
//			name.textSize(10);
			name.onChange = new Action0() {
				@Override
				public void invoke() {
					doManageRemove();					
				}
			};
			name.backgroundColor(0);
			name.vertically(VerticalAlignment.BOTTOM);
			name.height += 7;
			race = createLabel2("");
			race.onPress = new Action0() {
				@Override
				public void invoke() {
					doShowRace(PlayerLine.this);
				}
			};
			race.textSize(10);
			
			icon = new UIImage() {
				@Override
				public void draw(Graphics2D g2) {
					if (over) {
						g2.setColor(Color.GRAY);
						g2.fillRect(0, 0, width, height);
					}
					super.draw(g2);
				}
			};
			icon.center(true);
			icon.onClick = new Action0() {
				@Override
				public void invoke() {
					showIconPanel(PlayerLine.this);
				}
			};
			ai = createLabel2("      ");
			ai.horizontally(HorizontalAlignment.CENTER);
			ai.onPress = new Action0() {
				@Override
				public void invoke() {
					doShowAI(PlayerLine.this);
				}
			};
			ai.textSize(10);
			
			traits = createLabel("skirmish.traits");
			traits.horizontally(HorizontalAlignment.CENTER);
			traits.onPress = new Action0() {
				@Override
				public void invoke() {
					doSelectTraits(PlayerLine.this);
				}
			};
			
			
			group = createLabel2("  ");
			group.horizontally(HorizontalAlignment.CENTER);
			group.onPress = new Action0() {
				@Override
				public void invoke() {
					showGroupPanel(PlayerLine.this);
				}
			};
			
			planet = new UIImage(commons.common().randomPlanet) {
				@Override
				public void draw(Graphics2D g2) {
					if (over) {
						g2.setColor(Color.GRAY);
						g2.fillRect(0, 0, width, height);
					}
					super.draw(g2);
				}
			};
			planet.size(24, 24);
			planet.scale(true);
			planet.onClick = new Action0() {
				@Override
				public void invoke() {
					doShowPlanets(PlayerLine.this);
				}
			};
			
			this.addThis();
		}
		/** Clears the associated planet. */
		void clearPlanet() {
			planet.image(commons.common().randomPlanet);
			planet.tooltip(get("skirmish.initial_planet.random"));
			player.initialPlanet = null;
		}
		/**
		 * Change control size to its contents.
		 */
		public void sizeToContent() {
			name.sizeToContent();
			race.sizeToContent();
			icon.sizeToContent();
			ai.sizeToContent();
			traits.sizeToContent();
			group.sizeToContent();
//			planet.sizeToContent();
		}
		@Override
		public void draw(Graphics2D g2) {
			super.draw(g2);
//			if (over) {
//				g2.setColor(Color.GRAY);
//				g2.drawRect(0, 0, width, height);
//			}
		}
		/**
		 * Update fields based on the player contents.
		 */
		void update() {
			name.text(player.name);
			race.text(player.race);
			race.hoverColor(0xFFFFFFFF);
			icon.image(player.icon);
			ai.text(get("skirmish.ai." + player.ai));
			ai.hoverColor(0xFFFFFFFF);
			if (player.traits.isEmpty()) {
				traits.color(TextRenderer.GREEN);
			} else {
				traits.color(TextRenderer.YELLOW);
			}
			traits.hoverColor(0xFFFFFFFF);
			group.text(Integer.toString(player.group));
			group.hoverColor(0xFFFFFFFF);
			
			name.tooltip(player.description);
			ai.tooltip(get("skirmish.ai." + player.ai + ".tooltip"));
			if (player.initialPlanet != null) {
				planet.tooltip(player.initialPlanet);
			} else {
				planet.tooltip(get("skirmish.initial_planet.random"));
			}
		}
	}
	/**
	 * Layout all player lines.
	 */
	void layoutPlayers() {
		
		colName.sizeToContent();
		colRace.sizeToContent();
		colIcon.sizeToContent();
		colAI.sizeToContent();
		colTrait.sizeToContent();
		colGroup.sizeToContent();
		
		
		int maxGroup = colGroup.width;
		int maxTraits = colTrait.width;
		int maxAI = colAI.width;
		int maxIcon = colIcon.width;
		int maxRace = colRace.width;
		int maxPlanet = colPlanet.width;
		
		for (PlayerLine pl : playerLines) {
			
			pl.sizeToContent();
			
			maxGroup = Math.max(maxGroup, pl.group.width);
			maxTraits = Math.max(maxTraits, pl.traits.width);
			maxAI = Math.max(maxAI, pl.ai.width);
			maxIcon = Math.max(maxIcon, pl.icon.width);
			maxRace = Math.max(maxRace, pl.race.width);
			maxPlanet = Math.max(maxPlanet, pl.planet.width);
		}
		
		colPlanet.x = 5 + playersList.width - maxPlanet;
		colPlanet.width = maxPlanet;

		int gap = 5;

		colGroup.x = colPlanet.x - gap - maxGroup;
		colGroup.width = maxGroup;

		colTrait.x = colGroup.x - gap - maxTraits;
		colTrait.width = maxTraits;
		
		colAI.x = colTrait.x - gap - maxAI;
		colAI.width = maxAI;
		
		colIcon.x = colAI.x - gap - maxIcon;
		colIcon.width = maxIcon;
		
		colRace.x = colIcon.x - gap - maxRace;
		colRace.width = maxRace;

		colName.x = 5;
		colName.width = colRace.x - gap;

		int py = 0;

		for (PlayerLine pl : playerLines) {
			pl.planet.x = playersList.width - maxPlanet;
			pl.planet.width = maxPlanet;
			
			pl.group.x = pl.planet.x - gap - maxGroup;
			pl.group.width = maxGroup;
			pl.group.y = 7;

			pl.traits.x = pl.group.x - gap - maxTraits;
			pl.traits.width = maxTraits;
			pl.traits.y = 7;
			
			pl.ai.x = pl.traits.x - gap - maxAI;
			pl.ai.width = maxAI;
			pl.ai.y = 7;
			
			pl.icon.x = pl.ai.x - gap - maxIcon;
			pl.icon.width = maxIcon;
			pl.icon.y = 7;
			
			pl.race.x = pl.icon.x - gap - maxRace;
			pl.race.width = maxRace;
			pl.race.y = 7;
			
			pl.name.width = pl.race.x - gap;
			pl.name.y = 0;

			pl.icon.height = pl.ai.height;

			pl.y = py;
			pl.pack();
			py += pl.height;
		}
		int pw = playersList.width;
		playersList.pack();
		playersList.width = pw;
		
		playersListScroll.adjustButtons();
		playersListScroll.scrollBy(0);
	}
	/**
	 * Add a new player.
	 */
	void doAddPlayer() {
		
		boolean userFound = false;
		int maxGroup = 0;
		for (PlayerLine pl : playerLines) {
			if (pl.player.ai == SkirmishAIMode.USER) {
				userFound = true;
			}
			maxGroup = Math.max(pl.player.group, maxGroup);
		}
		PlayerLine pl = new PlayerLine();
		if (!userFound) {
			for (SkirmishPlayer sp : templatePlayers) {
				if (sp.ai == SkirmishAIMode.USER) {
					pl.player = sp.copy();
				}
			}
			if (pl.player == null) {
				pl.player = new SkirmishPlayer();
				pl.player.name = get("skirmish.you");
				pl.player.race = templateTechRaces.iterator().next();
				pl.player.originalId = "you";
				pl.player.ai = SkirmishAIMode.USER;
			}
		} else {
			List<SkirmishPlayer> candidates = new ArrayList<>();
			for (SkirmishPlayer sp : templatePlayers) {
				if (sp.ai != SkirmishAIMode.USER) {
					candidates.add(sp);
				}
			}
			if (candidates.isEmpty()) {
				computerSound(SoundType.NOT_AVAILABLE);
				return;
			}
			// remove existing combintations
			List<SkirmishPlayer> candidates2 = U.newArrayList(candidates);
			for (int i = candidates.size() - 1; i >= 0; i--) {
				for (PlayerLine pl0 : playerLines) {
					SkirmishPlayer sp = candidates.get(i);
					if (sp.originalId.equals(pl0.player.originalId) && sp.race.equals(pl0.player.race)) {
						candidates.remove(i);
						break;
					}
				}
			}
			if (candidates.isEmpty()) {
				candidates.addAll(candidates2);
			}
			
			Collections.shuffle(candidates);
			pl.player = candidates.get(0).copy();
			if (pl.player.ai != SkirmishAIMode.TRADER && pl.player.ai != SkirmishAIMode.PIRATE) {
				if (initialDifficulty.index == Difficulty.EASY.ordinal()) {
					pl.player.ai = SkirmishAIMode.AI_EASY;
				} else
				if (initialDifficulty.index == Difficulty.NORMAL.ordinal()) {
					pl.player.ai = SkirmishAIMode.AI_NORMAL;
				} else
				if (initialDifficulty.index == Difficulty.HARD.ordinal()) {
					pl.player.ai = SkirmishAIMode.AI_HARD;
				}
			}
		}

		pl.player.group = maxGroup + 1;
		
		pl.update();
		playerLines.add(pl);
		playersList.add(pl);
		layoutPlayers();
		buttonSound(SoundType.CLICK_HIGH_2);
		doManagePlay();
	}
	/**
	 * Handle the remove all clicks.
	 */
	void doManageRemove() {
		int cnt = 0;
		for (PlayerLine pl : playerLines) {
			if (pl.name.selected()) {
				cnt++;
			}
		}
		
		if (cnt == 0) {
			colName.selected(false);
		} else
		if (cnt == playerLines.size()) {
			colName.selected(true);
		}
		removePlayers.enabled(cnt != 0);
	}
	/**
	 * Remove selected playes.
	 */
	void doRemovePlayers() {
		for (int i = playerLines.size() - 1; i >= 0; i--) {
			PlayerLine pl = playerLines.get(i);
			if (pl.name.selected()) {
				playersList.remove(playerLines.remove(i));
			}
		}
		if (playerLines.isEmpty()) {
			colName.selected(false);
		}
		layoutPlayers();
		buttonSound(SoundType.CLICK_HIGH_2);
		doManageRemove();
		doManagePlay();
	}
	/**
	 * Select or deselect all lines.
	 * @param value the value
	 */
	void doSelectAll(boolean value) {
		for (PlayerLine pl : playerLines) {
			pl.name.selected(value);
		}
	}
	/**
	 * Select the traits for the user.
	 * @param pl the player line.
	 */
	void doSelectTraits(final PlayerLine pl) {
		buttonSound(SoundType.UI_ACKNOWLEDGE_2);
		TraitScreen ts = (TraitScreen)displaySecondary(Screens.TRAITS);
		ts.updateTraits(pl.player.traits);
		ts.onComplete = new Action1<Traits>() {
			@Override
			public void invoke(Traits value) {
				if (value != null) {
					pl.player.traits.replace(value);
				}
				pl.update();
			}
		};
	}
	/** The group selection panel. */
	public class GroupSelectPanel extends UIPanel {
		/** The current player line. */
		public PlayerLine pl;
		/** The group selection. */
		UIPanel groupSelect;
		/** The group select value. */
		NumberSpinBox groupSelectValue;
		/** The group select label. */
		UILabel groupSelectLabel;
		/** Cancel button. */
		UIGenericButton groupCancel;
		/** Cancel button. */
		UIGenericButton groupOK;
		/**
		 * Constructor. Initializes the fields.
		 */
		public GroupSelectPanel() {
			backgroundColor(0x80000000);
			
			groupSelectLabel = createLabel("skirmish.edit_group");
			groupSelectValue = new NumberSpinBox(commons, 1, 500, 1, 10);
			
			groupSelect = new UIPanel();
			groupSelect.backgroundColor(0xE0000000);
			groupSelect.borderColor(TextRenderer.GRAY);
			
			groupOK = createButton("skirmish.ok");
			groupOK.onClick = new Action0() {
				@Override
				public void invoke() {
					pl.player.group = groupSelectValue.value;
					GroupSelectPanel.this.visible(false);
					pl.update();
					pl = null;
					commons.control().moveMouse();
				}
			};
			groupCancel = createButton("skirmish.cancel");
			groupCancel.onClick = new Action0() {
				@Override
				public void invoke() {
					GroupSelectPanel.this.visible(false);
					pl = null;
					commons.control().moveMouse();
				}
			};
			
			groupSelect.add(groupSelectLabel, groupSelectValue, groupCancel, groupOK);
			
			add(groupSelect);
		}
		/**
		 * Fix the layout.
		 */
		public void layout() {
			groupSelectValue.setMaxSize();
			
			int ocw = groupOK.width + 10 + groupCancel.width;
			int lvw = Math.max(groupSelectLabel.width, groupSelectValue.width);

			int mw = Math.max(ocw, lvw);
			int dlv = 0;
			int doc = 0;
			if (ocw < lvw) {
				doc = (lvw - ocw) / 2;
			} else {
				dlv = (ocw - lvw) / 2;
			}

			groupSelectLabel.location(5 + dlv + (mw - groupSelectLabel.width) / 2, 5);
			groupSelectValue.location(5 + dlv + (mw - groupSelectValue.width) / 2, groupSelectLabel.y + groupSelectLabel.height + 5);

			groupOK.location(5 + doc, groupSelectValue.y + groupSelectValue.height + 10);
			groupCancel.location(groupOK.x + groupOK.width + 10, groupOK.y);
			
			groupSelect.pack();
			groupSelect.width += 5;
			groupSelect.height += 5;
			
			groupSelect.location((width - groupSelect.width) / 2, (height - groupSelect.height) / 2);
		}
		/**
		 * Show the panel for the given player line.
		 * @param pl the player line
		 */
		public void show(PlayerLine pl) {
			this.pl = pl;
			groupSelectValue.value = pl.player.group;
			groupSelectValue.update();
			visible(true);
		}
	}
	/**
	 * Shwo the group panel.
	 * @param pl the line
	 */
	void showGroupPanel(PlayerLine pl) {
		buttonSound(SoundType.UI_ACKNOWLEDGE_2);
		groupSelectPanel.show(pl);
	}
	/**
	 * Show the icon panel.
	 * @param pl the player
	 */
	void showIconPanel(PlayerLine pl) {
		buttonSound(SoundType.UI_ACKNOWLEDGE_2);
		iconSelectPanel.show(pl);
	}	
	/**
	 * The icon selection panel.
	 * @author akarnokd, 2012.08.21.
	 *
	 */
	public class IconSelectPanel extends UIPanel {
		/** The current player line. */
		public PlayerLine pl;
		/** The inner panel. */
		UIPanel inner;
		/** The group select label. */
		UILabel label;
		/** Cancel button. */
		UIGenericButton cancel;
		/** Cancel button. */
		UIGenericButton ok;
		/** The image list. */
		final List<UIImage> images = new ArrayList<>();
		/** The resource reference. */
		final List<String> imageRefs = new ArrayList<>();
		/** The current selected. */
		int selectedIndex = -1;
		/** Construct the panel. */
		public IconSelectPanel() {
			backgroundColor(0x80000000);
			
			label = createLabel("skirmish.edit_icon");
			
			inner = new UIPanel();
			inner.backgroundColor(0xE0000000);
			inner.borderColor(TextRenderer.GRAY);
			
			ok = createButton("skirmish.ok");
			ok.onClick = new Action0() {
				@Override
				public void invoke() {
					pl.player.icon = images.get(selectedIndex).image();
					pl.player.iconRef = imageRefs.get(selectedIndex);
					
					IconSelectPanel.this.visible(false);
					pl.update();
					pl = null;
					commons.control().moveMouse();
				}
			};
			cancel = createButton("skirmish.cancel");
			cancel.onClick = new Action0() {
				@Override
				public void invoke() {
					IconSelectPanel.this.visible(false);
					pl = null;
					commons.control().moveMouse();
				}
			};
			
			inner.add(label, ok, cancel);

			int i = 0;
			for (ResourcePlace rp : rl.list(rl.language, "starmap/fleets")) {
				if (rp.type() == ResourceType.IMAGE) {
					String imgRef = rp.getName();
					UIImage img = new UIImage(rl.getImage(imgRef));

					img.tooltip(imgRef);
					img.center(true);
					img.borderColor(0xFF404040);
					
					images.add(img);
					imageRefs.add(imgRef);
					
					final int j = i;

					img.onClick = new Action0() {
						@Override
						public void invoke() {
							onSelectImage(j);
						}
					};
					
					inner.add(img);
					
					i++;
				}
			}
			
			add(inner);
		}
		/**
		 * The action to perform when an image is clicked.
		 * @param index the index
		 */
		void onSelectImage(int index) {
			for (int i = 0; i < images.size(); i++) {
				UIImage img = images.get(i);
				if (i == index) {
					img.backgroundColor(0xFF606060);
				} else {
					img.backgroundColor(0);
				}
			}
			selectedIndex = index;
		}
		/**
		 * Fix the layout.
		 */
		public void layout() {
			
			label.location(5, 5);
			
			int ry = label.y + label.height + 10;

			int imgw = 0;
			int imgh = 0;
            for (UIImage img : images) {
                img.sizeToContent();

                imgw = Math.max(imgw, img.width);
                imgh = Math.max(imgh, img.height);
            }
			int cols = 4;
			int resize = 2;
			imgw *= resize;
			imgh *= resize;
			int px = 5;
			int py = 0;
			for (int i = 0; i < images.size(); i++) {
				if (i > 0 && (i % cols == 0)) {
					px = 5;
					ry += py + 5; 
				}
				UIImage img = images.get(i);
				
				img.x = px;
				img.y = ry;
				
				img.width = imgw;
				img.height = imgh;
				
				py = Math.max(py, img.height);
				px += img.width + 5;
			}

			ry += py + 5;
			
			int okCancelWidth = ok.width + cancel.width + 10;
			
			ry += 10;
			
			ok.x = 0;
			ok.y = ry;
			cancel.x = ok.x + ok.width + 10;
			cancel.y = ry;
			

			inner.pack();
			inner.width += 5;
			inner.height += 5;
			
			ok.x = (inner.width - okCancelWidth) / 2;
			cancel.x = ok.x + ok.width + 10;

			imgw = 0;
			imgh = Integer.MAX_VALUE;
			for (UIImage img : images) {
				imgw = Math.max(img.x + img.width, imgw);
				imgh = Math.min(imgh, img.x);
			}
			
			int imgd = (inner.width - imgw + imgh) / 2;
			for (UIImage img : images) {
				img.x += imgd;
			}			
			inner.location((width - inner.width) / 2, (height - inner.height) / 2);
		}
		/**
		 * Show the panel for the given player line.
		 * @param pl the player line
		 */
		public void show(PlayerLine pl) {
			this.pl = pl;
			selectedIndex = imageRefs.indexOf(pl.player.iconRef);
			onSelectImage(selectedIndex);
			visible(true);
		}
	}
	/**
	 * The icon selection panel.
	 * @author akarnokd, 2012.08.21.
	 *
	 */
	public class ItemSelectPanel extends UIPanel {
		/** The inner panel. */
		UIPanel inner;
		/** The group select label. */
		UILabel label;
		/** Cancel button. */
		UIGenericButton cancel;
		/** Cancel button. */
		UIGenericButton ok;
		/** The radio buttons. */
		final List<UIRadioButton> radioButtons = new ArrayList<>();
		/** The scroll box for the buttons. */
		UIScrollBox scroll;
		/** The container for the radio buttons. */
		UIPanel scrollPanel;
		/** The selected option. */
		public int selectedIndex = -1;
		/** The completion action called when the user selects ok. Gets the selected index. */
		public Action1<Integer> onComplete;
		/** Construct the panel. */
		public ItemSelectPanel() {
			backgroundColor(0x80000000);
			
			label = createLabel("skirmish.edit_option");
			
			inner = new UIPanel();
			inner.backgroundColor(0xE0000000);
			inner.borderColor(TextRenderer.GRAY);
			
			ok = createButton("skirmish.ok");
			ok.onClick = new Action0() {
				@Override
				public void invoke() {
					if (onComplete != null) {
						onComplete.invoke(selectedIndex);
						onComplete = null;
					}
					ItemSelectPanel.this.visible(false);
					commons.control().moveMouse();
				}
			};
			cancel = createButton("skirmish.cancel");
			cancel.onClick = new Action0() {
				@Override
				public void invoke() {
					ItemSelectPanel.this.visible(false);
					commons.control().moveMouse();
				}
			};
			
			inner.add(label, ok, cancel);

			scrollPanel = new UIPanel();
			
			scroll = new UIScrollBox(scrollPanel, 30,
					new UIImageButton(commons.database().arrowUp),
					new UIImageButton(commons.database().arrowDown));
			
			inner.add(scroll);
			
			add(inner);
		}
		/**
		 * Fix the layout.
		 */
		public void layout() {
			
			label.location(5, 5);
			
			int ry = label.y + label.height + 15;

			scrollPanel.pack();
			
			int n = Math.min(10, radioButtons.size());
			
			scroll.bounds(5, ry, scrollPanel.width + 40, 30 * n);

			ry += scroll.height;
			
			ry += 10;


			ok.x = 0;
			ok.y = ry;
			cancel.x = ok.x + ok.width + 10;
			cancel.y = ry;
			
			inner.pack();
			inner.width += 5;
			inner.height += 5;
			
			int okCancelWidth = ok.width + cancel.width + 10;
			ok.x = (inner.width - okCancelWidth) / 2;
			cancel.x = ok.x + ok.width + 10;

			inner.location((width - inner.width) / 2, (height - inner.height) / 2);
			
			scroll.scrollBy(0);
			scroll.adjustButtons();
		}
		/**
		 * Display a list of options.
		 * @param options the options list
		 * @param select the indexth element to select
		 * @param onComplete the completion handler
		 */
		public void show(List<String> options, int select, Action1<Integer> onComplete) {
			this.onComplete = onComplete;
			Iterator<UIComponent> it = scrollPanel.components().iterator();
			while (it.hasNext()) {
				UIComponent c = it.next();
				if (c instanceof UIRadioButton) {
					it.remove();
				}
			}
			radioButtons.clear();
			
			this.selectedIndex = select;
			int py = 0;
			for (int i = 0; i < options.size(); i++) {
				UIRadioButton btn = new UIRadioButton(options.get(i), 14, commons.text());
				btn.selected(i == select);
				final int j = i;
				btn.onChange = new Action0() {
					@Override
					public void invoke() {
						onSelected(j);
					}
				};
				scrollPanel.add(btn);
				radioButtons.add(btn);
				btn.y = py;
				py += 30;
			}
			
			layout();
			visible(true);
		}
		/** Hide the panel. */
		void hide() {
			visible(false);
			onComplete = null;
		}
		/**
		 * React to option selection.
		 * @param index the index
		 */
		void onSelected(int index) {
			for (int i = 0; i < radioButtons.size(); i++) {
				if (i != index) {
					radioButtons.get(i).selected(false);
				}
			}
			selectedIndex = index;
		}
	}
	/**
	 * Display a list of race options.
	 * @param pl the line
	 */
	void doShowRace(final PlayerLine pl) {
		final List<String> list = new ArrayList<>();
		int index = -1;
		final List<SkirmishPlayer> tps = U.newArrayList(templatePlayers);
		Collections.sort(tps, new Comparator<SkirmishPlayer>() {
			@Override
			public int compare(SkirmishPlayer o1, SkirmishPlayer o2) {
				return o1.description.compareToIgnoreCase(o2.description);
			}
		});
		for (SkirmishPlayer p : tps) {
			if (Objects.equals(p.originalId, pl.player.originalId)) {
				index = list.size();
			}
			list.add(p.description + " = " + p.race);
		}
		itemSelectPanel.show(list, index, new Action1<Integer>() {
			@Override
			public void invoke(Integer value) {
				SkirmishPlayer p = tps.get(value);
				
				pl.player.name = p.name;
				pl.player.originalId = p.originalId;
				pl.player.race = p.race;
				pl.player.description = p.description;
				pl.player.diplomacyHead = p.diplomacyHead;
				
				pl.update();
				layoutPlayers();
			}
		});
	}
	/**
	 * Display a list of AI options.
	 * @param pl the line
	 */
	void doShowAI(final PlayerLine pl) {
		boolean you = false;
		for (PlayerLine pl0 : playerLines) {
			if (pl0.player.ai == SkirmishAIMode.USER && pl != pl0) {
				you = true;
			}
		}
		final List<String> list = new ArrayList<>();
		final List<SkirmishAIMode> modes = new ArrayList<>();
		int index = 0;
		for (SkirmishAIMode m : SkirmishAIMode.values()) {
			if (m != SkirmishAIMode.USER || !you) {
				if (m == pl.player.ai) {
					index = list.size();
				}
				list.add(get("skirmish.ai." + m + ".tooltip"));
				modes.add(m);
			}
		}
		itemSelectPanel.show(list, index, new Action1<Integer>() {
			@Override
			public void invoke(Integer value) {
				pl.player.ai = modes.get(value);
				pl.update();
				layoutPlayers();
			}
		});
	}
	/**
	 * Check if at least one user is set up.
	 */
	void doManagePlay() {
		play.enabled(!playerLines.isEmpty());
	}
	/**
	 * Create a skirmish definition based on the current UI settings.
	 * @return the definition
	 */
	SkirmishDefinition createDefinition() {
		SkirmishDefinition result = new SkirmishDefinition();
		
		// Galaxy tab --------------------
		
		result.galaxy = galaxyDef.get().name;
		result.galaxyRandomSurface = galaxyRandomSurface.selected();
		result.galaxyRandomLayout = galaxyRandomLayout.selected();
		result.galaxyCustomPlanets = galaxyCustomPlanets.selected();
		result.galaxyPlanetCount = galaxyPlanetCount.value;
		result.galaxyRandomSeed = galaxySeed != null ? galaxySeed : 0;
		
		result.race = galaxyRaces.get().name;
		result.tech = technologyDef.get().name;
		result.startLevel = technologyLevelStart.value;
		result.maxLevel = technologyLevelMax.value;
		result.noFactoryLimit = noFactoryLimit.selected();
		result.noEconomicLimit = noEconomicLimit.selected();
		result.noLabLimit = noLabLimit.selected();
		result.allowAllBuildings = allowAllBuildings.selected();
		result.planetScale = planetScale.value;

		result.initialDiplomaticRelation = SkirmishDiplomaticRelation.values()[initialRelation.index];
		result.initialDifficulty = Difficulty.values()[initialDifficulty.index];
		
		// Economic tab ---------------------
		
		result.initialMoney = initialMoney.value;
		result.initialPlanets = initialPlanets.value;
		result.initialPopulation = initialPopulation.value;
		result.placeColonyHubs = placeColonyHub.selected();
		result.grantColonyShip = grantColonyShip.selected();
		result.initialColonyShips = colonyShips.value;
		result.grantOrbitalFactory = grantOrbitalFactory.selected();
		result.initialOrbitalFactories = orbitalFactories.value;
		result.taxBase = taxBase.value;
		result.taxScale = taxScale.value / 100d;
		
		// Players --------------------------
		
		for (PlayerLine pl : playerLines) {
			result.players.add(pl.player.copy());
		}
		
		// Victory conditions ---------------
		
		result.victoryConquest = winConquest.selected();
		result.victoryEconomic = winEconomic.selected();
		result.victoryEconomicMoney = winEconomicMoney.value;
		result.victoryOccupation = winOccupation.selected();
		result.victoryOccupationPercent = winOccupationPercent.value;
		result.victoryOccupationTime = winOccupationTime.value;
		result.victoryTechnology = winTechnology.selected();
		result.victorySocial = winSocial.selected();
		result.victorySocialMorale = winSocialMorale.value;
		result.victorySocialPlanets = winSocialPlanets.value;
		
		return result;
	}
	/**
	 * Start the skirmish.
	 */
	void doPlay() {
		SkirmishDefinition def = createDefinition();
		
		commons.world(null);
		commons.worldLoading = true;
		// display the loading screen.
		commons.control().displaySecondary(Screens.LOADING);
		final Semaphore barrier = new Semaphore(-1);
		startVideoWaiter(barrier);
		// the asynchronous loading
		startLoadWaiter(barrier, def);
		barrier.release();
//		// the video playback
//		commons.control().playVideos(new Action0() {
//			@Override
//			public void invoke() {
//				barrier.release();
//			}
//		}, selectedDefinition.intro);

	}
	/**
	 * Start the load waiter thread.
	 * @param barrier the notification barrier
	 * @param def the skirmish definition
	 */
	void startLoadWaiter(final Semaphore barrier, final SkirmishDefinition def) {
		loadWaiter = new Thread("Start Game Loading") {
			@Override 
			public void run() {
				try {
					final World world = new World(commons);
					
					world.skirmishDefinition = def;
					world.loadSkirmish(commons.rl, new SkirmishScripting());
					
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							commons.labels().replaceWith(world.labels);
							commons.world(world);
							commons.worldLoading = false;
							commons.nongame = false;
							barrier.release();
						}
					});
				} finally {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							loadWaiter = null;	
						}
					});
				}
			}
        };
		loadWaiter.setPriority(Thread.MIN_PRIORITY);
		loadWaiter.start();
	}

	/**
	 * Start the video playback waiter.
	 * @param barrier the notification barrier
	 */
	void startVideoWaiter(final Semaphore barrier) {
		videoWaiter = new Thread("Start Game Video Waiter") {
			@Override 
			public void run() {
				try {
					barrier.acquire();
					SwingUtilities.invokeLater(new Runnable() {
						@Override 
						public void run() {
							enterGame();
						}
					});
				} catch (InterruptedException ex) {
					
				} finally {
					videoWaiter = null;
				}
			}
        };
		videoWaiter.setPriority(Thread.MIN_PRIORITY);
		videoWaiter.start();
	}
	/**
	 * Enter the game.
	 */
	void enterGame() {
		world().scripting.onNewGame();
		final boolean csw = config.computerVoiceScreen;
		config.computerVoiceScreen = false;
		commons.start(true);
		commons.control().displayPrimary(Screens.STARMAP);
		
		config.computerVoiceScreen = csw;
		commons.control().displayStatusbar();
	}
	/**
	 * Show planets for the given player line.
	 * @param pl the player line
	 */
	void doShowPlanets(PlayerLine pl) {
		// TODO
		starmapPanel.load(galaxyDef.get());
		starmapPanel.show(pl.player.initialPlanet, pl);
	}
	/**
	 * Shows a list of planets and their position on a starmap.
	 * @author akarnokd, 2014.03.19.
	 */
	public final class StarmapPanel extends UIPanel {
		/** The inner panel. */
		UIPanel innerPanel;
		/** The galaxy's own background image. */
		UIImage backgroundImage;
		/** The planet icons. */
		final Map<String, BufferedImage> planetIcons;
		/** The map of planets. */
		final Map<String, PlanetCandidate> planets;
		/** The player line. */
		PlayerLine pl;
		/** Cancel button. */
		UIGenericButton cancel;
		/** Cancel button. */
		UIGenericButton ok;
		/** The selected planet. */
		String selected;
		/** Selected image. */
		UIImage selectedImage;
		/** Selected name. */
		UILabel selectedName;
		/** Prepares the panel. */
		public StarmapPanel() {
			planetIcons = new HashMap<>();
			planets = new LinkedHashMap<>();
			backgroundColor(0x80000000);
			
			innerPanel = new UIPanel();
			innerPanel.backgroundColor(0xE0000000);
			innerPanel.borderColor(TextRenderer.GRAY);
			
			backgroundImage = new UIImage() {
				@Override
				public void draw(Graphics2D g2) {
					super.draw(g2);
					
					int w0 = width;
					int h0 = height;
					if (image() != null) {
						w0 = image().getWidth();
						h0 = image().getHeight();
					}
					g2.setColor(Color.WHITE);
					int sx = -1;
					int sy = -1;
					for (PlanetCandidate pd : planets.values()) {
						int px = pd.x * width / w0;
						int py = pd.y * height / h0;
						boolean sel = Objects.equals(selected, pd.id);
						if (!sel || !blink) {
							g2.fillRect(px - 1, py - 1, 3, 3);
						}
						if (sel) {
							sx = px;
							sy = py;
						}
					}
					if (sx >= 0) {
						g2.setColor(new Color(0x40800000));
						g2.drawLine(1, sy, sx - 3, sy);
						g2.drawLine(sx + 4, sy, width - 2, sy);
						g2.drawLine(sx, 1, sx, sy - 3);
						g2.drawLine(sx, sy + 4, sx, height - 2);
					}
				}
				@Override
				public boolean mouse(UIMouse e) {
					if (e.has(Type.DOWN)) {
						if (e.has(Button.LEFT)) {
							select(planetAt(e.x, e.y));
						} else {
							select(null);
						}
						return true;
					} else
					if (e.has(Type.MOVE) || e.has(Type.DRAG)) {
						PlanetCandidate pd = planetAt(e.x, e.y);
						if (pd != null) {
							tooltip(pd.id + " (" + pd.type + ")");
						} else {
							tooltip(null);
						}
						return true;
					}
					return super.mouse(e);
				}
				/**
				 * Returns the planet at the specified mouse coordinates.
				 * @param mx the mouse X
				 * @param my the mouse Y
				 * @return the planet or null if nothing
				 */
				public PlanetCandidate planetAt(int mx, int my) {
					int w0 = width;
					int h0 = height;
					if (image() != null) {
						w0 = image().getWidth();
						h0 = image().getHeight();
					}
					PlanetCandidate selected = null;
					double bestDistance = 0;
					for (PlanetCandidate pd : planets.values()) {
						int px = pd.x * width / w0;
						int py = pd.y * height / h0;
						double d = Math.hypot(mx - px, my - py);
						if (d <= 4) {
							if (selected == null || bestDistance > d) {
								selected = pd;
								bestDistance = d;
							}
						}
					}
					return selected;
				}
			};
			backgroundImage.stretch(true);
			backgroundImage.borderColor(TextRenderer.GRAY);

			selectedImage = new UIImage();
			selectedName = new UILabel("", 14, commons.text());
			
			ok = createButton("skirmish.ok");
			ok.onClick = new Action0() {
				@Override
				public void invoke() {
					pl.player.initialPlanet = selected;
					pl.planet.image(selectedImage.image());
					pl.update();
					hide();
				}
			};
			cancel = createButton("skirmish.cancel");
			cancel.onClick = new Action0() {
				@Override
				public void invoke() {
					hide();
				}
			};
			
			add(innerPanel, backgroundImage, ok, cancel, selectedImage, selectedName);
		}
		/**
		 * Select a planet.
		 * @param pd the planet data or null to deselect
		 */
		void select(PlanetCandidate pd) {
			if (pd != null) {
				selectedImage.image(planetIcons.get(pd.type));
				selectedName.text(pd.id + " (" + pd.type + ")", true);
				selected = pd.id;
			} else {
				selectedImage.image(commons.common().randomPlanet);
				selectedName.text(get("skirmish.initial_planet.random"), true);
				selected = null;
			}
			blink = false;
		}
		
		/**
		 * Layout the inner components.
		 */
		public void layout() {
			innerPanel.size(620, 450);
			innerPanel.location((width - innerPanel.width) / 2, (height - innerPanel.height) / 2);
			// TODO

			backgroundImage.location(innerPanel.x + 5, innerPanel.y + 5);
			backgroundImage.size(610, 400);
			
			ok.y = innerPanel.y + 412;
			cancel.y = innerPanel.y + 412;
			
			int oc = ok.width + 10 + cancel.width;
			int ooc = (innerPanel.width - oc);
			
			ok.x = ooc;
			cancel.x = ooc + 10 + ok.width;
			
			selectedImage.location(15, innerPanel.y + 410);
			selectedName.location(50, innerPanel.y + 417);
		}
		
		/**
		 * Loads the resources from the given game definition.
		 * @param def the definition
		 */
		public void load(GameDefinition def) {
			XElement galaxyXML = rl.getXML(def.galaxy);
			XElement background = galaxyXML.childElement("background");
			
			backgroundImage.image(rl.getImage(background.get("image")));

			planetIcons.clear();
			for (XElement xplanet : galaxyXML.childElement("planets").childrenWithName("planet")) {
				XElement xbody = xplanet.childElement("body");
				
				BufferedImage img = rl.getImage(xbody.content);
				img = ImageUtils.newSubimage(img, 0, 0, img.getHeight(), img.getHeight());
				planetIcons.put(xplanet.get("type"), img);
			}
			
			planets.clear();
			
			XElement xplanets = rl.getXML(def.planets);
			List<XElement> planetTemplate = new ArrayList<>();
			for (XElement xplanet : xplanets.childrenWithName("planet")) {
				planetTemplate.add(xplanet);
			}

			if (galaxyRandomLayout.selected()) {
				int minPlanets = playerLines.size() * initialPlanets.value;
				int count = galaxyPlanetCount.value;
				if (!galaxyCustomPlanets.selected()) {
					count = planetTemplate.size();
				}

				count = Math.max(count, minPlanets);
				
				GalaxyGenerator gg = new GalaxyGenerator();
				
				gg.setSeed(galaxySeed);
				gg.setPlanetCount(count);
				gg.setSize(backgroundImage.image().getWidth(), backgroundImage.image().getHeight());
				gg.setMinDensity(World.MIN_PLANET_DENSITY);
				
				List<String> singleNames = new ArrayList<>();
				List<String> multiNames = new ArrayList<>();
				
				GalaxyModel.parsePlanetNaming(galaxyXML, singleNames, multiNames, commons.labels());
				
				gg.setSingleNames(singleNames);
				gg.setMultiNames(multiNames);
				
				List<PlanetCandidate> planets = gg.generate();

				int j = 0;
				for (PlanetCandidate pc : planets) {

					XElement xplanet = planetTemplate.get(j);
					XElement xsurface = xplanet.childElement("surface");

					pc.type = xsurface.get("type");
					pc.variant = xsurface.getInt("id");
					
					j = (j + 1) % planetTemplate.size();
					
					this.planets.put(pc.id, pc);
				}
				
			} else {
				for (XElement xplanet : planetTemplate) {
					XElement xsurface = xplanet.childElement("surface");
					
					PlanetCandidate pd = new PlanetCandidate();
					pd.x = xplanet.getInt("x");
					pd.y = xplanet.getInt("y");
					
					pd.id = xplanet.get("id");
					pd.name = xplanet.get("name", pd.id);
					pd.type = xsurface.get("type");
					pd.variant = xsurface.getInt("id");
					
					this.planets.put(pd.id, pd);
				}
			}
		}
		/**
		 * Display the panel and select the given planet.
		 * @param planetId the selected planet id
		 * @param pl the player line to update
		 */
		public void show(String planetId, PlayerLine pl) {
			PlanetCandidate pd = planets.get(planetId);
			select(pd);
			this.pl = pl;
			visible(true);
		}
		/**
		 * Hide the panel.
		 */
		public void hide() {
			planetIcons.clear();
			pl = null;
			backgroundImage.image(null);
			planets.clear();
			planetIcons.clear();
			visible(false);
		}
	}
	/** Reseed the galaxy layout. */
	void doReseed() {
		galaxySeed = ModelUtils.randomLong();
		
		galaxyRandomSeed.text(galaxySeed.toString(), false);
		
		clearPlanetAssociations();
	}
	/** Clear the planet associations of the players. */
	private void clearPlanetAssociations() {
		for (PlayerLine sp : playerLines) {
			sp.clearPlanet();
		}
	}
}
