/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.model.Screens;
import hu.openig.screen.ScreenBase;
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
	@Override
	public void onInitialize() {
		anim = new Timer(animationSpeed, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doAnimation();
			}
		});
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
		showImage = false;
		anim.start();
	}

	@Override
	public boolean keyboard(KeyEvent e) {
		if (phase < maxPhase) {
			e.consume();
			return true;
		}
		commons.control().endGame();
		displayPrimary(Screens.MAIN);
		e.consume();
		return true;
	}
	@Override
	public boolean mouse(UIMouse e) {
		if (phase < maxPhase) {
			return false;
		}
		if (e.has(Type.DOWN)) {
			commons.control().endGame();
			displayPrimary(Screens.MAIN);
			return true;
		}
		return false;
	}
	
	@Override
	public void onLeave() {
		anim.stop();
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
		
		if (showImage) {
			g2.setColor(Color.BLACK);
			g2.fillRect(0, 0, width, height);
			BufferedImage gameover = commons.background().gameover;
			Composite save0 = g2.getComposite();
			if (imageAlpha < 1) {
				g2.setComposite(AlphaComposite.SrcOver.derive(imageAlpha));
			}
		
			int dx = (width - gameover.getWidth()) / 2;
			int dy = (height - gameover.getHeight()) / 2;
			
			g2.drawImage(gameover, dx, dy, null);
			
			g2.setComposite(save0);
		}
		
		super.draw(g2);
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
