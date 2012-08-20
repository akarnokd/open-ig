/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.core.Action0;
import hu.openig.core.Func1;
import hu.openig.model.GameDefinition;
import hu.openig.model.Screens;
import hu.openig.model.SoundType;
import hu.openig.render.RenderTools;
import hu.openig.render.TextRenderer;
import hu.openig.screen.ScreenBase;
import hu.openig.ui.UICheckBox;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIContainer;
import hu.openig.ui.UIGenericButton;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UILabel;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Modifier;
import hu.openig.ui.UIPanel;
import hu.openig.ui.UISpinner;
import hu.openig.utils.U;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
		
		galaxyRaces = new CampaignSpinBox();
		galaxyRacesLabel = createLabel("skirmish.race_template");
		
		galaxyPanel.add(galaxyRaces, galaxyRacesLabel);
		
		technologyDef = new CampaignSpinBox();
		technologyDefLabel = createLabel("skirmish.tech_template");

		technologyLevelLabel = createLabel("skirmish.tech_level");
		technologyLevel = new NumberSpinBox(1, 5, 1, 1);
		technologyLevel.value = 5;

		galaxyPanel.add(technologyDef, technologyDefLabel, technologyLevelLabel, technologyLevel);
		
		
		initialMoneyLabel = createLabel("skirmish.initial_money");
		initialMoney = new NumberSpinBox(0, 2000000000, 10000, 100000);
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
	 * Create a label.
	 * @param label the text label
	 * @return the label component
	 */
	UILabel createLabel(String label) {
		return new UILabel(get(label), 14, commons.text());
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
		// TODO Auto-generated method stub

	}

	@Override
	public void onEndGame() {
		// TODO Auto-generated method stub

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
			spin.prev.enabled(value > min);
			spin.next.enabled(value < max);
		}
		@Override
		public String onValue() {
			return String.format("%,d", value);
		}
		/**
		 * Set the size to the maximum of the campaign name.
		 */
		public void setMaxSize() {
			int w = commons.text().getTextWidth(14, String.format("%,d", min));
			w = Math.max(w, commons.text().getTextWidth(14, String.format("%,d", max)));
			width = w + spin.prev.width + spin.next.width + 20;
			spin.width = width;
			height = spin.prev.height;
		}
	}
	/**
	 * Spin box referencing the campaign.
	 * @author akarnokd, 2012.08.20.
	 */
	public class CampaignSpinBox extends SpinBox {
		/** The selected index. */
		public int index;
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
			return campaigns.get(index).title;
		}
		/** Set the tooltip. */
		@Override
		public void update() {
			GameDefinition d = get();
			
			setTooltipText(spin, d.description);
			
			spin.prev.enabled(index > 0);
			spin.next.enabled(index < campaigns.size() - 1);
		}
		/**
		 * @return Get the current indexed campaign.
		 */
		public GameDefinition get() {
			return campaigns.get(index);
		}
		/**
		 * Set the size to the maximum of the campaign name.
		 */
		public void setMaxSize() {
			int w = 0;
			for (GameDefinition d : campaigns) {
				w = Math.max(w, commons.text().getTextWidth(14, d.title));
			}
			width = w + spin.prev.width + spin.next.width + 20;
			spin.width = width;
			height = spin.prev.height;
		}
	}
}
