/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.core.Action0;
import hu.openig.model.Screens;
import hu.openig.render.RenderTools;
import hu.openig.render.TextRenderer;
import hu.openig.screen.ScreenBase;
import hu.openig.ui.UIGenericButton;
import hu.openig.ui.UIPanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

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
	UIPanel techPanel;
	/** Panel. */
	UIPanel economyPanel;
	/** Panel. */
	UIPanel playersPanel;
	/** Button. */
	UIGenericButton galaxyBtn;
	/** Button. */
	UIGenericButton techBtn;
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
	/** The galactic background. */
	BufferedImage background;
	@Override
	public void onResize() {
		base.y = 10;
		base.height = height - 20;

		RenderTools.centerScreen(base, width, height, false);

		galaxyPanel.bounds(base.x + 5, base.y + 30, base.width - 10, base.height - 60);
		techPanel.bounds(galaxyPanel.bounds());
		economyPanel.bounds(galaxyPanel.bounds());
		playersPanel.bounds(galaxyPanel.bounds());
		
		galaxyBtn.location(base.x + 5, base.y + 5);
		techBtn.location(galaxyBtn.x + galaxyBtn.width + 5, base.y + 5);
		economyBtn.location(techBtn.x + techBtn.width + 5, base.y + 5);
		playersBtn.location(economyBtn.x + economyBtn.width + 5, base.y + 5);
		
		back.location(base.x + 10, base.y + base.height - 40);
		
		load.location(base.x + base.width / 2 - 5 - load.width, back.y);
		save.location(base.x + base.width / 2 + 5, back.y);
		
		play.location(base.x + base.width - 10 - play.width, back.y);
	}
	@Override
	public Screens screen() {
		return Screens.SKIRMISH;
	}

	@Override
	public void onInitialize() {
		// TODO Auto-generated method stub
		galaxyPanel = new UIPanel();
		techPanel = new UIPanel();
		economyPanel = new UIPanel();
		playersPanel = new UIPanel();
		
		galaxyBtn = createButton("skirmish.galaxy");
		techBtn = createButton("skirmish.tech");
		economyBtn = createButton("skirmish.economy");
		playersBtn = createButton("skirmish.players");
		
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
		techBtn.onPress = panelSwitchAction(techPanel);
		economyBtn.onPress = panelSwitchAction(economyPanel);
		playersBtn.onPress = panelSwitchAction(playersPanel);
		
		addThis();
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
				techPanel.visible(techPanel == panel);
				economyPanel.visible(economyPanel == panel);
				playersPanel.visible(playersPanel == panel);
				
				galaxyBtn.color(galaxyPanel.visible() ? TextRenderer.WHITE : 0xFF000000);
				techBtn.color(techPanel.visible() ? TextRenderer.WHITE : 0xFF000000);
				economyBtn.color(economyPanel.visible() ? TextRenderer.WHITE : 0xFF000000);
				playersBtn.color(playersPanel.visible() ? TextRenderer.WHITE : 0xFF000000);
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
		galaxyBtn.onPress.invoke();
		
		background = rl.getImage("starmap/background");
	}

	@Override
	public void onLeave() {
		background = null;
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
}
