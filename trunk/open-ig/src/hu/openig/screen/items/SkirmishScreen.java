/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.core.Action0;
import hu.openig.core.Difficulty;
import hu.openig.core.Func1;
import hu.openig.model.GameDefinition;
import hu.openig.model.Screens;
import hu.openig.model.SkirmishAIMode;
import hu.openig.model.SkirmishDefinition;
import hu.openig.model.SkirmishDiplomaticRelation;
import hu.openig.model.SkirmishPlayer;
import hu.openig.model.SoundType;
import hu.openig.render.RenderTools;
import hu.openig.render.TextRenderer;
import hu.openig.screen.ScreenBase;
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
import java.util.List;
import java.util.Set;

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
	final List<GameDefinition> campaigns = new ArrayList<GameDefinition>();
	/** The skirmish definition. */
	SkirmishDefinition definition;
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
	/** Technology level. */
	NumberSpinBox technologyLevel;
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
	final List<SkirmishPlayer> templatePlayers = U.newArrayList();
	/** The tech races. */
	final Set<String> templateTechRaces = U.newHashSet();
	/** The list of players. */
	final List<PlayerLine> playerLines = U.newArrayList();
	/** Add player button. */
	UIGenericButton addPlayer;
	/** Clear players button. */
	UIGenericButton clearPlayers;
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
		save = createButton("skirmish.save");
		play = createButton("skirmish.play");
		
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
				templatePlayers.addAll(getPlayersFrom(get()));
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
		technologyLevel = new NumberSpinBox(1, 5, 1, 1);
		technologyLevel.value = 5;

		galaxyPanel.add(technologyDef, technologyDefLabel, technologyLevelLabel, technologyLevel);
		
		
		initialMoneyLabel = createLabel("skirmish.initial_money");
		initialMoney = new NumberSpinBox(0, 2000000000, 10000, 100000) {
			@Override
			public String onValue() {
				return String.format("%,d cr", value);
			}
		};
		initialMoney.value = 200000;
		
		initialPlanetsLabel = createLabel("skirmish.initial_planets");
		initialPlanets = new NumberSpinBox(0, 500, 1, 10);
		initialPlanets.value = 3;
		
		economyPanel.add(
				initialMoneyLabel, initialMoney, 
				initialPlanetsLabel, initialPlanets);
		
		initialPopulationLabel = createLabel("skirmish.initial_population");
		initialPopulation = new NumberSpinBox(0, 1000000, 100, 1000);
		initialPopulation.value = 5000;
		
		placeColonyHub = createCheckBox("skirmish.place_colony_hub");
		grantColonyShip = createCheckBox("skirmish.grant_colonyship");
		grantOrbitalFactory = createCheckBox("skirmish.grant_orbital_factory");
		
		economyPanel.add(initialPopulationLabel, initialPopulation, placeColonyHub,
				grantColonyShip, grantOrbitalFactory);
		
		colonyShipLabel = createLabel("skirmish.colony_ships");
		colonyShips = new NumberSpinBox(0, 1000, 1, 10);
		
		orbitalFactoryLabel = createLabel("skirmish.orbital_factories");
		orbitalFactories = new NumberSpinBox(0, 1000, 1, 10);
		
		economyPanel.add(colonyShipLabel, colonyShips, orbitalFactoryLabel, orbitalFactories);
		
		initialRelationLabel = createLabel("skirmish.initial_relation");
		initialRelation = new ListSpinBox<SkirmishDiplomaticRelation>(new Func1<SkirmishDiplomaticRelation, String>() { 
			@Override
			public String invoke(SkirmishDiplomaticRelation value) {
				return get("skirmish.relation." + value);
			}
		}, SkirmishDiplomaticRelation.values());
		initialRelation.index = SkirmishDiplomaticRelation.DEFAULT.ordinal();
		
		initialDifficultyLabel = createLabel("skirmish.initial_difficulty");
		initialDifficulty = new ListSpinBox<Difficulty>(new Func1<Difficulty, String>() { 
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
		clearPlayers = createButton("skirmish.remove_players");
		clearPlayers.onClick = new Action0() {
			@Override
			public void invoke() {
				doRemovePlayers();
			}
		};
		
		playersPanel.add(addPlayer, clearPlayers);

		colName = createCheckBox("skirmish.name");
		colName.backgroundColor(0);
		colName.onChange = new Action0() {
			@Override
			public void invoke() {
				doSelectAll(colName.selected());
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
		
		//FIXME
		
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
		technologyLevel.setMaxSize();
		technologyLevel.location(5 + technologyLevelLabel.width + 20, cy);

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

		clearPlayers.location(playersPanel.width - 5 - clearPlayers.width, playersPanel.height - 75);
		addPlayer.location(clearPlayers.x - 10 - addPlayer.width, clearPlayers.y);
		
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
				galaxyPanel.visible(galaxyPanel == panel);
				economyPanel.visible(economyPanel == panel);
				playersPanel.visible(playersPanel == panel);
				victoryPanel.visible(victoryPanel == panel);
				
				galaxyBtn.color(galaxyPanel.visible() ? TextRenderer.WHITE : 0xFF000000);
				economyBtn.color(economyPanel.visible() ? TextRenderer.WHITE : 0xFF000000);
				playersBtn.color(playersPanel.visible() ? TextRenderer.WHITE : 0xFF000000);
				victoryBtn.color(victoryPanel.visible() ? TextRenderer.WHITE : 0xFF000000);
				
				buttonSound(SoundType.UI_ACKNOWLEDGE_2);
			}
		};
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

		definition = new SkirmishDefinition();
		
		onResize();
		
		List<UIComponent> all = U.newArrayList();
		U.addAll(all, galaxyPanel.components());
		U.addAll(all, economyPanel.components()); 
		U.addAll(all, victoryPanel.components()); 
		
		for (UIComponent c : all) {
			if (c instanceof SpinBox) {
				((SpinBox)c).update();
			}
		}
		
		galaxyBtn.onPress.invoke();
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
		definition = null;
	}

	@Override
	public void onFinish() {

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
		public ListSpinBox(Func1<T, String> valueFunc, T... list) {
			this.list = Arrays.asList(list);
			this.valueFunc = valueFunc;
			
		}
		@Override
		public void onNext(boolean shift) {
			int cnt = shift ? 10 : 1;
			index = Math.min(campaigns.size() - 1, index + cnt);
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
					return value.title;
				}
			});
		}
		/** Set the tooltip. */
		@Override
		public void update() {
			GameDefinition d = get();
			
			setTooltipText(spin, d.description);

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
	 * Compute the icon color of the image.
	 * @param resource the resource
	 * @return the color
	 */
	public int iconColor(String resource) {
		BufferedImage bimg = rl.getImage(resource);
		int[] pixels = bimg.getRGB(0, 0, bimg.getWidth(), bimg.getHeight(), null, 0, bimg.getWidth());
		long r = 0;
		long g = 0;
		long b = 0;
		for (int i : pixels) {
			if ((i & 0xFF000000) != 0) {
				r += ((i & 0xFF0000) >> 16);
				g += ((i & 0xFF00) >> 8);
				b += ((i & 0xFF));
			}
		}
		r /= pixels.length;
		g /= pixels.length;
		b /= pixels.length;
		
		return (int)(0xFF000000 | (r << 16) | (g << 8) | (b));
	}
	/**
	 * Get the races from the tech and building definitions.
	 * @param def the definition
	 * @return the set of races
	 */
	public Set<String> getRacesFrom(GameDefinition def) {
		Set<String> result = U.newHashSet();
		
		XElement xtech = rl.getXML(def.tech);
		for (XElement xitem : xtech.childrenWithName("item")) {
			String r = xitem.get("race");
			
			result.addAll(Arrays.asList(r.split("\\s*,\\s*")));
		}
		XElement xbuild = rl.getXML(def.buildings);
		
		for (XElement xb : xbuild.childrenWithName("building")) {
			for (XElement xg : xb.childrenWithName("graphics")) {
				for (XElement xt : xg.childElement("tech")) {
					result.add(xt.get("id"));
				}
			}
		}
		
		return result;
	}
	/**
	 * Extract the players from the given definition.
	 * @param def the definition
	 * @return the set of players
	 */
	public Set<SkirmishPlayer> getPlayersFrom(GameDefinition def) {
		Set<SkirmishPlayer> result = U.newHashSet();
		
		XElement xplayers = rl.getXML(def.players);
		
		for (XElement xplayer : xplayers.childrenWithName("player")) {
			SkirmishPlayer sp = new SkirmishPlayer();
			
			sp.originalId = xplayer.get("id");
			sp.race = xplayer.get("race");
			sp.name = get(xplayer.get("name") + ".short");
			sp.description = get(xplayer.get("name"));
			sp.iconRef = xplayer.get("icon");
			sp.icon = rl.getImage(sp.iconRef);
			sp.color = (int)Long.parseLong(xplayer.get("color"), 16);
			sp.nodatabase = xplayer.getBoolean("nodatabase", false);
			sp.nodiplomacy = xplayer.getBoolean("nodiplomacy", false);
			
			String ai = xplayer.get("ai", null);
			if (xplayer.getBoolean("user", false)) {
				sp.ai = SkirmishAIMode.USER;
			} else
			if ("TRADERS".equals(ai)) {
				sp.ai = SkirmishAIMode.TRADER;
			} else
			if ("PIRATES".equals(ai)) {
				sp.ai = SkirmishAIMode.PIRATE;
			} else {
				sp.ai = SkirmishAIMode.AI_NORMAL;
			}
			
			result.add(sp);
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
			name.backgroundColor(0);
			name.vertically(VerticalAlignment.BOTTOM);
			name.height += 5;
			race = createLabel2("");
			icon = new UIImage();
			icon.center(true);
			ai = createLabel2("      ");
			ai.horizontally(HorizontalAlignment.CENTER);
			traits = createLabel("skirmish.traits");
			traits.horizontally(HorizontalAlignment.CENTER);
			
			group = createLabel2("  ");
			group.horizontally(HorizontalAlignment.CENTER);
			
			
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
			if (over) {
				g2.setColor(Color.GRAY);
				g2.drawRect(0, 0, width, height);
			}
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

			pl.icon.height = pl.name.height;

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
			List<SkirmishPlayer> candidates = U.newArrayList();
			for (SkirmishPlayer sp : templatePlayers) {
				if (sp.ai != SkirmishAIMode.USER) {
					candidates.add(sp);
				}
			}
			if (candidates.isEmpty()) {
				computerSound(SoundType.NOT_AVAILABLE);
				return;
			}
			Collections.shuffle(candidates);
			pl.player = candidates.get(0);
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
}
