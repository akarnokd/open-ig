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
import hu.openig.render.TextRenderer;
import hu.openig.screen.ScreenBase;
import hu.openig.ui.HorizontalAlignment;
import hu.openig.ui.UIGenericButton;
import hu.openig.ui.UILabel;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Type;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import javax.swing.Timer;

/**
 * The game over screen.
 * @author akarnokd, 2012.01.14.
 */
public class GameOverScreen extends ScreenBase {
	/** The animation phase. */
	int phase = 0;
	/** The animation timer. */
	Timer anim;
	/** Show the game over image? */
	boolean showImage;
	/** The image alpha level. */
	float imageAlpha;
	/** The animation speed in milliseconds. */
	final int animationSpeed = 50;
	/** The maximum phase. */
	final int maxPhase = 40;
	/** The player won! */
	public boolean win;
	/** Continue the gameplay. */
	UIGenericButton continueButton;
	/** Return to main menu. */
	UIGenericButton mainMenuButton;
	/** The win label. */
	UILabel winLabel;
	@Override
	public void onInitialize() {
		anim = new Timer(animationSpeed, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doAnimation();
			}
		});
		continueButton = new UIGenericButton(get("win.continue_game"), fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
		continueButton.onClick = new Action0() {
			@Override
			public void invoke() {
				hideSecondary();
				commons.control().displayStatusbar();
				commons.nongame = false;
			}
		};
		mainMenuButton = new UIGenericButton(get("win.main_menu"), fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
		mainMenuButton.onClick = new Action0() {
			@Override
			public void invoke() {
				commons.control().endGame();
				displayPrimary(Screens.MAIN);
			}
		};
		winLabel = new UILabel(get("win.win"), 20, commons.text());
		addThis();
	}
	
	/** Perform an animation step. */
	void doAnimation() {
		phase++;
		StatusbarScreen sts = commons.control().getScreen(Screens.STATUSBAR);
		float len = maxPhase / 2f;
		if (phase * 2 < maxPhase) {
			sts.overlay = new Color(0f, 0f, 0f, phase / len);
		} else
		if (phase * 2 >= maxPhase) {
			sts.overlay = null;
			commons.force = false;
			commons.nongame = true;
			commons.control().hideStatusbar();
			showImage = true;
			imageAlpha = (phase - len) / len;
		}
		if (phase >= maxPhase) {
			imageAlpha = 1f;
			anim.stop();
		}
		askRepaint();
	}
	
	@Override
	public void onEnter(Screens mode) {
		commons.force = true;
		imageAlpha = 0.0f;
		phase = 0;
		showImage = false;
		anim.start();
	}

	@Override
	public boolean keyboard(KeyEvent e) {
		if (phase < maxPhase) {
			e.consume();
			return true;
		}
		if (!win) {
			commons.control().endGame();
			displayPrimary(Screens.MAIN);
		}
		e.consume();
		return true;
	}
	@Override
	public boolean mouse(UIMouse e) {
		if (phase < maxPhase) {
			return false;
		}
		if (e.has(Type.DOWN) && !win) {
			commons.control().endGame();
			displayPrimary(Screens.MAIN);
			return true;
		}
		return super.mouse(e);
	}
	
	@Override
	public void onLeave() {
		anim.stop();
		win = false;
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onResize() {
		// TODO Auto-generated method stub

	}

	@Override
	public void draw(Graphics2D g2) {
		
		continueButton.visible(false);
		mainMenuButton.visible(false);
		winLabel.visible(false);
		if (showImage) {
			g2.setColor(Color.BLACK);
			g2.fillRect(0, 0, width, height);
			BufferedImage gameover = commons.background().gameover;
			Composite save0 = g2.getComposite();
			if (imageAlpha < 1) {
				g2.setComposite(AlphaComposite.SrcOver.derive(imageAlpha));
			}
		
			int bw = gameover.getWidth();
			int bh = gameover.getHeight();
			int dx = (width - bw) / 2;
			int dy = (height - bh) / 2;
			
			g2.drawImage(gameover, dx, dy, null);
			
			if (win) {
				continueButton.visible(true);
				mainMenuButton.visible(true);
				winLabel.visible(true);
				
				winLabel.location(dx, dy + 40);
				winLabel.size(bw, 30);
				winLabel.horizontally(HorizontalAlignment.CENTER);
				winLabel.color(TextRenderer.YELLOW);
				
				int w = continueButton.width + mainMenuButton.width;
				
				int dx2 = (bw - w) / 3;
				
				continueButton.location(dx + dx2, dy + bh - 50);
				mainMenuButton.location(dx + 2 * dx2 + continueButton.width, dy + bh - 50);

				super.draw(g2);
			}
			
			g2.setComposite(save0);
		}
	}
	
	@Override
	public Screens screen() {
		return Screens.GAME_OVER;
	}

	@Override
	public void onEndGame() {
		// TODO Auto-generated method stub

	}

}
