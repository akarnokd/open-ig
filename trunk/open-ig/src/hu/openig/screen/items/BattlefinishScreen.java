/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.core.Pair;
import hu.openig.core.SimulationSpeed;
import hu.openig.mechanics.AIUser;
import hu.openig.model.BattleInfo;
import hu.openig.model.Fleet;
import hu.openig.model.ModelUtils;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.Screens;
import hu.openig.model.SpacewarStructure;
import hu.openig.render.RenderTools;
import hu.openig.render.TextRenderer;
import hu.openig.screen.ScreenBase;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Type;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.Timer;



/**
 * The space/surface battle conclusion screen with the outcome statistics.
 * @author akarnokd, 2010.01.11.
 */
public class BattlefinishScreen extends ScreenBase {
	/** The panel base rectangle. */
	final Rectangle base = new Rectangle(0, 0, 640, 480);
	/** The background image. */
	BufferedImage background;
	/** The battle results. */
	BattleInfo battle;
	/** The text delay timer. */
	Timer textDelay;
	/** Automatically close the dialog if the current player is an AI. */
	Timer autoClose;
	/** Display text? */
	boolean showText;
	/** Enemy survival status after battle. */
	enum EnemyStatus {
		/** Enemy retreated, e.g., hp < hpMax / 2 or so. */
		RETREATED,
		/** Enemy slipped. */
		SLIPPED,
		/** Enemy destroyed. */
		DESTROYED
	}
	@Override
	public void onInitialize() {
		textDelay = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showText = true;
				scaleRepaint(base, base, margin());
				textDelay.stop();
			}
		});
		autoClose = new Timer(30000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				autoClose.stop();
				battle.battleFinished();
				displayPrimary(Screens.STARMAP);
				commons.speed1();
			}
		});
	}
	/** The background image. */
	void setBackground() {
		if ((battle.spacewarWinner == null && battle.groundwarWinner == player())
			|| (battle.groundwarWinner == null && battle.spacewarWinner == player()) 
		) {
			if (battle.rewardImage != null) {
				setRewardImage();
			} else {
				setWinImage();
			}
		} else {
			setLoseImage();
		}
	}
	/** Set a random win image. */
	void setWinImage() {
		background = rl.getImage("battlefinish/win_" + (1 + ModelUtils.randomInt(6)));
	}
	/** Set a random lose image. */
	void setLoseImage() {
		background = rl.getImage("battlefinish/lose_" + (1 + ModelUtils.randomInt(2)));
	}
	/** Set the reward image. */
	void setRewardImage() {
		background = rl.getImage(battle.rewardImage);
	}
	@Override
	public void onEnter(Screens mode) {
		showText = false;
		textDelay.start();
		if (!(player().ai instanceof AIUser)) {
			autoClose.start();
		}
	}

	@Override
	public void onLeave() {
		commons.restoreMainSimulationSpeedFunction();
		commons.battleMode = false;
		commons.playRegularMusic();
		commons.simulation.speed(battle.originalSpeed);
		textDelay.stop();
		autoClose.stop();
		battle = null;
	}

	@Override
	public void onFinish() {
		
	}
	@Override
	public boolean keyboard(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_SPACE) {
			displayPrimary(Screens.STARMAP);
			e.consume();
			return false;
		}
		return super.keyboard(e);
	}
	@Override
	public void onResize() {
		scaleResize(base, margin());
	}
	@Override
	public Screens screen() {
		return Screens.BATTLE_FINISH;
	}
	@Override
	public void onEndGame() {
		
	}
	@Override
	public void draw(Graphics2D g2) {
		AffineTransform savea = scaleDraw(g2, base, margin());
		
		RenderTools.darkenAround(base, width, height, g2, 0.5f, true);
		g2.drawImage(background, base.x, base.y, null);
		
		if (showText) {
			g2.setColor(new Color(0, 0, 0, 128));
			g2.fill(base);
			
			int x1 = base.x;
			int w1 = base.width;
			int x2 = base.x + base.width / 2;
			int w2 = base.width / 4;
			int x3 = base.x + base.width / 2 + w2;
			int w3 = w2;
			
			textCenter(g2, x1, base.y + 15, w1, TextRenderer.GREEN, 14, get("battlefinish.statistics"));
			
			g2.setColor(new Color(TextRenderer.GREEN));
			g2.drawLine(x1, base.y + 35, x1 + w1 - 1, base.y + 35);
			
			if (battle.retreated) {
				textCenter(g2, x1, base.y + 40, w1, TextRenderer.YELLOW, 14, get("battlefinish.spacewar_retreat"));
			} else {
				if (battle.spacewarWinner != null) {

					if (battle.spacewarWinner == player()) {
						Fleet enemy;
						if (battle.attacker.owner == player()) {
							enemy = battle.getFleet();
						} else {
							enemy = battle.attacker;
						}
						if (enemy != null) {
							EnemyStatus st = enemyStatus(enemy, battle);
							if (st != EnemyStatus.SLIPPED) {
								textCenter(g2, x1, base.y + 40, w1, TextRenderer.GREEN, 14, get("battlefinish.spacewar_won"));
							}
						}
					} else {
						textCenter(g2, x1, base.y + 40, w1, TextRenderer.RED, 14, get("battlefinish.spacewar_lost"));
					}
					
				}
				if (battle.groundwarWinner != null) {
					if (battle.groundwarWinner == player()) {
						textCenter(g2, x1, base.y + 65, w1, TextRenderer.GREEN, 14, get("battlefinish.groundwar_won"));
					} else {
						textCenter(g2, x1, base.y + 65, w1, TextRenderer.RED, 14, get("battlefinish.groundwar_lost"));
					}
				}
			}

			int y = base.y + 85;

			if (battle.messageText != null) {
				y += 5;
				y += textCenterWrap(g2, x1, y, w1, TextRenderer.YELLOW, 10, battle.messageText);
				y += 12;
			}
			
			textCenter(g2, x2, y, w2, TextRenderer.GREEN, 14, get("battlefinish.own_losses_1"));
			textCenter(g2, x3, y, w3, TextRenderer.GREEN, 14, get("battlefinish.enemy_losses_1"));
			
			y += 20;
			
			textCenter(g2, x2, y, w2, TextRenderer.GREEN, 14, get("battlefinish.own_losses_2"));
			textCenter(g2, x3, y, w3, TextRenderer.GREEN, 14, get("battlefinish.own_losses_2"));
			
			y += 20;
			
			if (battle.spacewarWinner != null) {
				y = printStatistics(g2, y, "battlefinish.fighters", 
						lossCount(true, ResearchSubCategory.SPACESHIPS_FIGHTERS), 
						lossCount(false, ResearchSubCategory.SPACESHIPS_FIGHTERS));
				y = printStatistics(g2, y, "battlefinish.cruisers", 
						lossCount(true, false), 
						lossCount(false, false));
				y = printStatistics(g2, y, "battlefinish.destroyers", 
						lossCount(true, true), 
						lossCount(false, true));
				y = printStatistics(g2, y, "battlefinish.battleships", 
						lossCount(true, ResearchSubCategory.SPACESHIPS_BATTLESHIPS), 
						lossCount(false, ResearchSubCategory.SPACESHIPS_BATTLESHIPS));
			}
			y = printStatistics(g2, y, "battlefinish.ground_units",
					battle.attacker.owner == player() ? battle.attackerGroundLosses : battle.defenderGroundLosses,
					battle.attacker.owner != player() ? battle.attackerGroundLosses : battle.defenderGroundLosses);
			if (battle.spacewarWinner != null) {
				y = printStatistics(g2, y, "battlefinish.stations", 
						lossCount(true, ResearchSubCategory.SPACESHIPS_STATIONS), 
						lossCount(false, ResearchSubCategory.SPACESHIPS_STATIONS));
				y = printStatistics(g2, y, "battlefinish.guns", lossCount(true, "Gun"), lossCount(false, "Gun"));
				y = printStatistics(g2, y, "battlefinish.shields", lossCount(true, "Shield"), lossCount(false, "Shield"));
			}
			if (battle.groundwarWinner != null) {
				y = printStatistics(g2, y, "battlefinish.fortifications", 0, battle.defenderFortificationLosses);
			}
			y += 20;
			
			if (battle.targetPlanet != null) {
				if (battle.targetPlanet.owner == null) {
					textCenter(g2, x1, y, w1, TextRenderer.GREEN, 14, format("battlefinish.planet_destroyed", battle.targetPlanet.name()));
					y += 20;
				} else
				if (battle.groundwarWinner == player() 
						&& battle.originalTargetPlanetOwner != player()
						&& battle.targetPlanet.owner == player()) {
					textCenter(g2, x1, y, w1, TextRenderer.GREEN, 14, format("battlefinish.planet_won", battle.targetPlanet.name()));
					y += 20;
				} else
				if (battle.groundwarWinner != player() 
				&& battle.originalTargetPlanetOwner == player() 
				&& battle.targetPlanet.owner != player()) {
					textCenter(g2, x1, y, w1, TextRenderer.RED, 14, format("battlefinish.planet_lost", battle.targetPlanet.name()));
					y += 20;
				}
			} else
			if (battle.helperPlanet != null) {
				if (battle.helperPlanet.owner == null) {
					textCenter(g2, x1, y, w1, TextRenderer.GREEN, 14, format("battlefinish.planet_destroyed", battle.helperPlanet.name()));
					y += 20;
				}
			}
			
			if (battle.spacewarWinner == player()) {
				Fleet enemy;
				if (battle.attacker.owner == player()) {
					enemy = battle.getFleet();
				} else {
					enemy = battle.attacker;
				}
				if (enemy != null) {
					EnemyStatus st = enemyStatus(enemy, battle);
					if (st == EnemyStatus.RETREATED) {
						textCenter(g2, x1, y, w1, TextRenderer.YELLOW, 14, format("battlefinish.spacewar_enemy_fled", enemy.name()));
						y += 20;
					} else
					if (st == EnemyStatus.SLIPPED) {
						textCenter(g2, x1, y, w1, TextRenderer.RED, 14, format("battlefinish.spacewar_enemy_slipped", enemy.name()));
						y += 20;
					}
				}
			}
			
			for (Fleet flt : new Fleet[] { battle.getFleet(), battle.attacker }) {
				if (flt != null && flt.inventory.size() == 0) {
					if (flt.owner == player()) {
						textCenter(g2, x1, y, w1, TextRenderer.RED, 14, 
								format("battlefinish.own_fleet_destroyed", flt.name()));
						y += 20;
					} else {
						textCenter(g2, x1, y, w1, TextRenderer.GREEN, 14, 
								format("battlefinish.enemy_fleet_destroyed", flt.name()));
						y += 20;
					}
				}
			}

			if (battle.rewardText != null) {
				y += 10;
				y += textCenterWrap(g2, x1, y, w1, TextRenderer.YELLOW, 14, battle.rewardText);
				y += 7;
			}
			
			
			textCenter(g2, base.x, base.y + base.height - 20, base.width, TextRenderer.GREEN, 14, get("battlefinish.click_to_exit"));
		}
		g2.setTransform(savea);
	}
	/**
	 * Check if any enemy is beyond the screen.
	 * @param enemy the enemy fleet
	 * @param battle the battle info
	 * @return the status of the enemy fleet.
	 */
	EnemyStatus enemyStatus(Fleet enemy, BattleInfo battle) {
		if (enemy.inventory.size() > 0) {
			if (battle.enemyFlee) {
				return EnemyStatus.RETREATED;
			}
			return EnemyStatus.SLIPPED;
		}
		return EnemyStatus.DESTROYED;
	}
	/**
	 * Count the destroyer losses or cruiser losses.
	 * @param own the owner should be the player?
	 * @param destroyer count destroyers (true) or cruisers (false)
	 * @return the loss count
	 */
	int lossCount(boolean own, boolean destroyer) {
		int result = 0;
		for (SpacewarStructure s : battle.spaceLosses) {
			boolean players = s.owner == player();
			boolean ally = isAlly(s);
			if (s.item != null && (own == (players || ally)) 
					&& s.item.type.category == ResearchSubCategory.SPACESHIPS_CRUISERS
					&& s.item.type.id.toLowerCase(Locale.ENGLISH).contains("destroyer") == destroyer) {
				result += s.loss;
			}
		}
		return result;
	}
	/**
	 * Count the destroyer losses or cruiser losses.
	 * @param own the owner should be the player?
	 * @param kind the structure kind
	 * @return the loss count
	 */
	int lossCount(boolean own, String kind) {
		int result = 0;
		for (SpacewarStructure s : battle.spaceLosses) {
			boolean players = s.owner == player();
			boolean ally = isAlly(s);
			if (s.building != null && (own == (players || ally)) 
					&& s.building.type.kind.equals(kind)) {
				result += s.loss;
			}
		}
		return result;
	}
	/**
	 * Count the losses of the specified category.
	 * @param own count the player's losses?
	 * @param category the category
	 * @return the loss amount
	 */
	int lossCount(boolean own, ResearchSubCategory category) {
		int result = 0;
		for (SpacewarStructure s : battle.spaceLosses) {
			boolean players = s.owner == player();
			boolean ally = isAlly(s);
			if (s.item != null 
					&& (own == (players || ally))
					&& s.item.type.category == category) {
				result += s.loss;
			}
		}
		return result;
	}
	/**
	 * Check if the owner of the given structure is an ally.
	 * @param s the structure
	 * @return true if ally
	 */
	boolean isAlly(SpacewarStructure s) {
		return battle.isAlly(s, player());
	}
	/**
	 * Draw a statistics witht the given label and values.
	 * @param g2 the graphics context
	 * @param y the top position
	 * @param text the label to print
	 * @param ownCount the own count
	 * @param enemyCount the enemy count
	 * @return the new offset
	 */
	int printStatistics(Graphics2D g2, int y, String text, int ownCount, int enemyCount) {
		int x2 = base.x + base.width / 2;
		int w2 = base.width / 4;
		int x3 = base.x + base.width / 2 + w2;
		int w3 = w2;
		commons.text().paintTo(g2, base.x + 20, y, 14, TextRenderer.GREEN, get(text));
		textCenter(g2, x2, y, w2, TextRenderer.GREEN, 14, Integer.toString(ownCount));
		textCenter(g2, x3, y, w3, TextRenderer.GREEN, 14, Integer.toString(enemyCount));
		return y + 20;
	}
	/**
	 * Center the text between x and x + width.
	 * @param g2 the graphics context
	 * @param x the left
	 * @param y the top
	 * @param width the width
	 * @param color the color
	 * @param size the text size
	 * @param text the actual text
	 */
	void textCenter(Graphics2D g2, int x, int y, int width, int color, int size, String text) {
		int tw;
		size++;
		do {
			size--;
			tw = commons.text().getTextWidth(size, text);
		} while (tw > width);
		commons.text().paintTo(g2, x + (width - tw) / 2, y, size, color, text);
	}
	/**
	 * Center the text between x and x + width.
	 * @param g2 the graphics context
	 * @param x the left
	 * @param y the top
	 * @param width the width
	 * @param color the color
	 * @param size the text size
	 * @param text the actual text
	 * @return the delta Y due wrapping
	 */
	int textCenterWrap(Graphics2D g2, int x, int y, int width, int color, int size, String text) {
		List<String> lines = new ArrayList<>();
		commons.text().wrapText(text, width, size, lines);
		int dy = 0;
		for (String line : lines) {
			int tw = commons.text().getTextWidth(size, line);
			commons.text().paintTo(g2, x + (width - tw) / 2, y + dy, size, color, line);
			dy += size + 3;
		}
	    return dy;
	}
	@Override
	public boolean mouse(UIMouse e) {
		scaleMouse(e, base, margin());
		if (e.has(Type.UP) /* && !e.within(base.x, base.y, base.width, base.height) */) {
			if (!showText) {
				showText = true;
				textDelay.stop();
				return true;
			}
			
			battle.battleFinished();
			displayPrimary(Screens.STARMAP);
			commons.speed1();
			return false;
		}
		return super.mouse(e);
	}
	/** Install an empty simulator. */
	void setPausedSimulator() {
		final SimulationSpeed cspd = commons.simulation.speed();
		commons.simulation = commons.new SimulationTimer(null, null) {
			{
				paused = true;
				speed = cspd;
			}
			@Override
			public void pause() {
			}
			@Override
			public void resume() {
			}
			@Override
			public void speed(SimulationSpeed newSpeed) {
			}
		};
	}
	/**
	 * Display battle summary.
	 * @param battle the battle information
	 */
	public void displayBattleSummary(BattleInfo battle) {
		this.battle = battle;
		setBackground();
		setPausedSimulator();
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
