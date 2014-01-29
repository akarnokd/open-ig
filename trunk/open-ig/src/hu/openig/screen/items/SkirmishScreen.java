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
import hu.openig.core.Difficulty;
import hu.openig.core.Func1;
import hu.openig.core.ResourceType;
import hu.openig.model.GameDefinition;
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
import hu.openig.scripting.SkirmishScripting;
import hu.openig.ui.HorizontalAlignment;
import hu.openig.ui.UICheckBox;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIContainer;
import hu.openig.ui.UIGenericButton;
import hu.openig.ui.UIImage;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UILabel;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Modifier;
import hu.openig.ui.UIPanel;
import hu.openig.ui.UIRadioButton;
import hu.openig.ui.UIScrollBox;
import hu.openig.ui.UISpinner;
import hu.openig.ui.VerticalAlignment;
import hu.openig.utils.U;
import hu.openig.utils.XElement;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
	/** The group selection panel. */
	GroupSelectPanel groupSelectPanel;
	/** The icon selection panel. */
	IconSelectPanel iconSelectPanel;
	/** An item selection panel. */
	ItemSelectPanel itemSelectPanel;
	/** The video playback completion waiter. */
	volatile Thread videoWaiter;
	/** The load waiter. */
	volatile Thread loadWaiter;
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
		
		galaxyBtn = createButton("skirmish.galaxy");
		economyBtn = createButton("skirmish.economy");
		playersBtn = createButton("skirmish.players");
		victoryBtn = createButton("skirmish.victory");
		
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
		
		save = createButton("skirmish.save");
		save.disabledPattern(commons.common().disabledPattern);
		save.enabled(false);
		
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

		galaxyDefLabel = createLabel("skirmish.galaxy_template");
		galaxyDef = new CampaignSpinBox();
		
		galaxyPanel.add(galaxyDefLabel, galaxyDef);
		
		galaxyRandomSurface = createCheckBox("skirmish.random_surface");
		galaxyRandomLayout = createCheckBox("skirmish.random_layout");
		galaxyCustomPlanets = createCheckBox("skirmish.custom_planets");
		
		galaxyPlanetCount = new NumberSpinBox(0, 500, 1, 10);
		galaxyPlanetCount.value = 100;
		
		galaxyPanel.add(galaxyRandomSurface, galaxyRandomLayout, galaxyCustomPlanets, galaxyPlanetCount);
		
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
		
		technologyLevelMax = new NumberSpinBox(1, 5, 1, 1);
		technologyLevelMax.value = 5;

		technologyLevelStart = new NumberSpinBox(0, 6, 1, 1);
		technologyLevelStart.value = 0;

		noFactoryLimit = createCheckBox("skirmish.no_factory_limit");
		noLabLimit = createCheckBox("skirmish.no_lab_limit");
		
		galaxyPanel.add(technologyDef, technologyDefLabel, technologyLevelLabel, 
				technologyLevelStart, technologyLevelMaxLabel, technologyLevelMax,
				noFactoryLimit, noLabLimit);
		
		
		initialMoneyLabel = createLabel("skirmish.initial_money");
		initialMoney = new NumberSpinBox(0, 2000000000, 10000, 100000) {
			@Override
			public String onValue() {
				return String.format("%,d cr", value);
			}
		};
		initialMoney.value = 200000;
		
		initialPlanetsLabel = createLabel("skirmish.initial_planets");
		initialPlanets = new NumberSpinBox(1, 500, 1, 10);
		initialPlanets.value = 3;
		
		economyPanel.add(
				initialMoneyLabel, initialMoney, 
				initialPlanetsLabel, initialPlanets);
		
		initialPopulationLabel = createLabel("skirmish.initial_population");
		initialPopulation = new NumberSpinBox(0, 1000000, 100, 1000);
		initialPopulation.value = 5000;
		
		placeColonyHub = createCheckBox("skirmish.place_colony_hub");
		placeColonyHub.selected(true);
		grantColonyShip = createCheckBox("skirmish.grant_colonyship");
		grantOrbitalFactory = createCheckBox("skirmish.grant_orbital_factory");
		
		economyPanel.add(initialPopulationLabel, initialPopulation, placeColonyHub,
				grantColonyShip, grantOrbitalFactory);
		
		colonyShipLabel = createLabel("skirmish.colony_ships");
		colonyShips = new NumberSpinBox(0, 1000, 1, 10);
		colonyShips.value = 1;
		
		orbitalFactoryLabel = createLabel("skirmish.orbital_factories");
		orbitalFactories = new NumberSpinBox(0, 1000, 1, 10);
		
		economyPanel.add(colonyShipLabel, colonyShips, orbitalFactoryLabel, orbitalFactories);
		
		initialRelationLabel = createLabel("skirmish.initial_relation");
		initialRelation = new ListSpinBox<>(new Func1<SkirmishDiplomaticRelation, String>() { 
			@Override
			public String invoke(SkirmishDiplomaticRelation value) {
				return get("skirmish.relation." + value);
			}
		}, SkirmishDiplomaticRelation.values());
		initialRelation.index = SkirmishDiplomaticRelation.DEFAULT.ordinal();
		
		initialDifficultyLabel = createLabel("skirmish.initial_difficulty");
		initialDifficulty = new ListSpinBox<>(new Func1<Difficulty, String>() { 
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
		
		winOccupationPercent = new NumberSpinBox(0, 100, 1, 10) {
			@Override
			public String onValue() {
				return String.format("%,d %%", value);
			}
		};
		winOccupationPercent.value = 66;
		
		final String fdays = get("skirmish.days");
		winOccupationTime = new NumberSpinBox(0, 1000, 1, 10) {
			@Override
			public String onValue() {
				return String.format("%,d %s", value, fdays);
			}
		};
		winOccupationTime.value = 30;
		
		winEconomicMoney = new NumberSpinBox(0, 2000000000, 1000000, 10000000) {
			@Override
			public String onValue() {
				return String.format("%,d cr", value);
			}
		};
		winEconomicMoney.value = 10000000;
		
		winSocialMorale = new NumberSpinBox(0, 100, 1, 10) {
			@Override
			public String onValue() {
				return String.format("%,d %%", value);
			}
		};
		winSocialMorale.value = 95;
		
		winSocialPlanets = new NumberSpinBox(0, 500, 1, 10);
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
		
		colName.color(TextRenderer.LIGHT_GREEN);
		colIcon.color(TextRenderer.LIGHT_GREEN);
		colRace.color(TextRenderer.LIGHT_GREEN);
		colAI.color(TextRenderer.LIGHT_GREEN);
		colTrait.color(TextRenderer.LIGHT_GREEN);
		colGroup.color(TextRenderer.LIGHT_GREEN);
		
		playersPanel.add(colName, colRace, colIcon, colAI, colTrait, colGroup);

		groupSelectPanel = new GroupSelectPanel();
		groupSelectPanel.visible(false);
		
		iconSelectPanel = new IconSelectPanel();
		iconSelectPanel.visible(false);
		
		itemSelectPanel = new ItemSelectPanel();
		itemSelectPanel.visible(false);
		
		//FIXME implement random layout
		
		galaxyRandomLayout.enabled(false);
		galaxyPlanetCount.enabled(false);
		galaxyCustomPlanets.enabled(false);
		
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
		
		galaxyBtn.location(base.x + 5, base.y + 5);
		economyBtn.location(galaxyBtn.x + galaxyBtn.width + 5, base.y + 5);
		playersBtn.location(economyBtn.x + economyBtn.width + 5, base.y + 5);
		victoryBtn.location(playersBtn.x + playersBtn.width + 5, base.y + 5);
		
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
		galaxyCustomPlanets.location(5, cy + 7);
		galaxyPlanetCount.setMaxSize();
		galaxyPlanetCount.location(5 + galaxyCustomPlanets.width + 20, cy);
		
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

		cy += 40;
		noFactoryLimit.location(5, cy);

		cy += 35;
		noLabLimit.location(5, cy);

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

		
		playersListScroll.bounds(5, 25, playersPanel.width - 10, playersPanel.height - 105);
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
		
		galaxyBtn.color(galaxyPanel.visible() ? TextRenderer.WHITE : 0xFF000000);
		economyBtn.color(economyPanel.visible() ? TextRenderer.WHITE : 0xFF000000);
		playersBtn.color(playersPanel.visible() ? TextRenderer.WHITE : 0xFF000000);
		victoryBtn.color(victoryPanel.visible() ? TextRenderer.WHITE : 0xFF000000);
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
				itemSelectPanel.visible(false);
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
	 * A spin box with left, right and value display.
	 * @author akarnokd, 2012.08.20.
	 */
	public abstract class SpinBox extends UIContainer {
		/** The spin control. */
		public final UISpinner spin;
		/** Shift pressed while clicking on left. */
		protected boolean shiftLeft;
		/** Shift pressed while clicking on right. */
		protected boolean shiftRight;
		/**
		 * Constructor. Initializes the fields.
		 */
		public SpinBox() {
			final UIImageButton aprev = new UIImageButton(commons.common().moveLeft) {
				@Override
				public boolean mouse(UIMouse e) {
					shiftLeft = e.has(Modifier.SHIFT);
					return super.mouse(e);
				}
			};
			aprev.setDisabledPattern(commons.common().disabledPattern);
			aprev.setHoldDelay(150);
			final UIImageButton anext = new UIImageButton(commons.common().moveRight) {
				@Override
				public boolean mouse(UIMouse e) {
					shiftRight = e.has(Modifier.SHIFT);
					return super.mouse(e);
				}
			};
			anext.setDisabledPattern(commons.common().disabledPattern);
			anext.setHoldDelay(150);
			
			aprev.onClick = new Action0() {
				@Override
				public void invoke() {
					buttonSound(SoundType.CLICK_LOW_1);
					onPrev(shiftLeft);
					askRepaint();
				}
			};
			anext.onClick = new Action0() {
				@Override
				public void invoke() {
					buttonSound(SoundType.CLICK_LOW_1);
					onNext(shiftRight);
					askRepaint();
				}
			};
			
			spin = new UISpinner(14, aprev, anext, commons.text());
			spin.getValue = new Func1<Void, String>() {
				@Override
				public String invoke(Void value) {
					return onValue();
				}
			};
			this.add(spin);
		}
		/**
		 * The action to perform when clicked on previous.
		 * @param shift the SHIFT key was pressed
		 */
		public abstract void onPrev(boolean shift);
		/**
		 * The action to perform when clicked on next.
		 * @param shift the SHIFT key was pressed
		 */
		public abstract void onNext(boolean shift);
		/**
		 * @return the value to display
		 */
		public abstract String onValue();
		/** Update state after changes. */
		public void update() {
			
		}
		@Override
		public UIComponent enabled(boolean state) {
			spin.enabled(state);
			return super.enabled(state);
		}
	}
	/**
	 * A number spin box.
	 * @author akarnokd, 2012.08.20.
	 */
	public class NumberSpinBox extends SpinBox {
		/** The current value. */
		public int value;
		/** The minimum value. */
		protected int min;
		/** The maximum value. */
		protected int max;
		/** The small step. */
		protected int step;
		/** The large step. */
		protected int largeStep;
		/**
		 * Constructor. Initializes the parameters.
		 * @param min minimum value
		 * @param max maximum value
		 * @param step small step
		 * @param largeStep large step
		 */
		public NumberSpinBox(int min, int max, int step, int largeStep) {
			this.min = min;
			this.max = max;
			this.step = step;
			this.largeStep = largeStep;
			this.value = min;
		}
		@Override
		public void onNext(boolean shift) {
			value = Math.min(max, value + (shift ? largeStep : step));
			update();
		}
		@Override
		public void onPrev(boolean shift) {
			value = Math.max(min, value - (shift ? largeStep : step));
			update();
		}
		/** Update controls. */
		@Override
		public void update() {
			spin.prev.enabled(enabled && value > min);
			spin.next.enabled(enabled && value < max);
		}
		@Override
		public String onValue() {
			return String.format("%,d", value);
		}
		/**
		 * Set the size to the maximum of the campaign name.
		 */
		public void setMaxSize() {
			int val = value;
			value = min;
			int w = commons.text().getTextWidth(14, onValue());
			value = max;
			w = Math.max(w, commons.text().getTextWidth(14, onValue()));
			width = w + spin.prev.width + spin.next.width + 20;
			spin.width = width;
			height = spin.prev.height;
			value = val;
		}
		@Override
		public UIComponent enabled(boolean state) {
			spin.enabled(state);
			if (state) {
				update();
			}
			return super.enabled(state);
		}
	}
	/**
	 * List box spin box.
	 * @author akarnokd, 2012.08.20.
	 * @param <T> the element type
	 */
	public class ListSpinBox<T> extends SpinBox {
		/** The current selection index. */ 
		public int index;
		/** The list. */
		final List<T> list;
		/** The value function. */
		final Func1<T, String> valueFunc;
		/**
		 * Constructor. Initialize the fields.
		 * @param list the backing list
		 * @param valueFunc the value function
		 */
		public ListSpinBox(List<T> list, Func1<T, String> valueFunc) {
			this.list = list;
			this.valueFunc = valueFunc;
			
		}
		/**
		 * Constructor. Initialize the fields.
		 * @param list the backing list
		 * @param valueFunc the value function
		 */
		@SafeVarargs
		public ListSpinBox(Func1<T, String> valueFunc, T... list) {
			this.list = Arrays.asList(list);
			this.valueFunc = valueFunc;
			
		}
		@Override
		public void onNext(boolean shift) {
			int cnt = shift ? 10 : 1;
			index = Math.min(list.size() - 1, index + cnt);
			update();
		}
		@Override
		public void onPrev(boolean shift) {
			int cnt = shift ? 10 : 1;
			index = Math.max(0, index - cnt);
			update();
		}
		@Override
		public String onValue() {
			return valueFunc.invoke(list.get(index));
		}
		@Override
		public void update() {
			spin.prev.enabled(enabled && index > 0);
			spin.next.enabled(enabled && index < list.size() - 1);
		}
		/**
		 * Set the size to the maximum of the campaign name.
		 */
		public void setMaxSize() {
			int w = 0;
			for (T t : list) {
				w = Math.max(w, commons.text().getTextWidth(14, valueFunc.invoke(t)));
			}
			width = w + spin.prev.width + spin.next.width + 20;
			spin.width = width;
			height = spin.prev.height;
		}
		/** @return the selected index. */
		public T selected() {
			return list.get(index);
		}
	}
	/**
	 * Spin box referencing the campaign.
	 * @author akarnokd, 2012.08.20.
	 */
	public class CampaignSpinBox extends ListSpinBox<GameDefinition> {
		/** Constructor. */
		public CampaignSpinBox() {
			super(campaigns, new Func1<GameDefinition, String>() {
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
		/** Cunstructs the UI elements. */
		public PlayerLine() {
			name = createCheckBox2("");
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
			
			
			this.addThis();
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
		
		for (PlayerLine pl : playerLines) {
			
			pl.sizeToContent();
			
			maxGroup = Math.max(maxGroup, pl.group.width);
			maxTraits = Math.max(maxTraits, pl.traits.width);
			maxAI = Math.max(maxAI, pl.ai.width);
			maxIcon = Math.max(maxIcon, pl.icon.width);
			maxRace = Math.max(maxRace, pl.race.width);
			
		}
		
		colGroup.x = 5 + playersList.width - maxGroup;
		colGroup.width = maxGroup;

		int gap = 5;
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
			pl.group.x = playersList.width - maxGroup;
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
			groupSelectValue = new NumberSpinBox(1, 500, 1, 10);
			
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
		
		result.race = galaxyRaces.get().name;
		result.tech = technologyDef.get().name;
		result.startLevel = technologyLevelStart.value;
		result.maxLevel = technologyLevelMax.value;
		result.noFactoryLimit = noFactoryLimit.selected();
		result.noLabLimit = noLabLimit.selected();

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
}
