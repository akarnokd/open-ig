/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.xold.render;

import hu.openig.xold.core.ScreenLayerer;
import hu.openig.xold.res.GameResourceManager;
import hu.openig.xold.res.gfx.CommonGFX;
import hu.openig.xold.res.gfx.TextGFX;
import hu.openig.sound.SoundFXPlayer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Renderer to fade-in or out achievement text  messages over a lot of screens.
 * @author karnokd, 2009.05.20.
 * @version $Revision 1.0$
 */
public class AchievementRenderer {
	/** The common graphics. */
	private final CommonGFX cgfx;
	/** The text graphics. */
	private final TextGFX text;
	/** The ui sounds. */
//	private final UISounds sounds;
	/** The achievement display fade in and out. */
	private Timer achievementFader;
	/** The achievement alpha value. */
	float achievementAlpha = 0f;
	/** The time step to fully display the achievement. */
	private static final int ACHIEVEMENT_FADE_TIME = 30;
	/** The time to keep achievement at full visibility. */ 
	private static final int ACHIEVEMENT_KEEP_TIME = 60;
	/** The time for the timer to fire. */
	private static final int ACHIEVEMENT_SPEED = 50;
	/** The current achievement display step. */
	private int achievementStep;
	/** The queue to display achievements. */
	private final Queue<String> achievementQueue = new LinkedBlockingQueue<String>();
	/** The current achievement text. */
	private String achievementText;
	/** The screen layerer callback. */
	private ScreenLayerer screenLayerer;
	/**
	 * Components that are in contact with the renderer and will be repainted on
	 * the active animation.
	 */
	private final List<Component> components = new LinkedList<Component>();
	/**
	 * Constructor.
	 * @param grm the game resource manager
	 * @param sounds the ui sounds
	 */
	public AchievementRenderer(GameResourceManager grm, SoundFXPlayer sounds) {
		this.cgfx = grm.commonGFX;
		this.text = grm.commonGFX.text;
//		this.sounds = sounds;
		achievementFader = new Timer(ACHIEVEMENT_SPEED, 
				new ActionListener() { public void actionPerformed(ActionEvent e) { doAchievement(); } });
	}
	/**
	 * Perform the achievement animation.
	 */
	private void doAchievement() {
		if (achievementText == null) {
			achievementText = achievementQueue.poll();
			achievementStep = 0;
			if (achievementText == null) {
				achievementFader.stop();
			}
		} else {
			achievementStep++;
			if (achievementStep <= ACHIEVEMENT_FADE_TIME) {
				achievementAlpha = Math.min(1.0f, achievementStep * 1.0f / ACHIEVEMENT_FADE_TIME);
				repaint();
			} else
			if (achievementStep > ACHIEVEMENT_FADE_TIME + ACHIEVEMENT_KEEP_TIME) {
				achievementAlpha = Math.max(0, 1 - (achievementStep * 1.0f - ACHIEVEMENT_FADE_TIME - ACHIEVEMENT_KEEP_TIME) / ACHIEVEMENT_FADE_TIME);
				repaint();
			}
			if (achievementStep > 2 * ACHIEVEMENT_FADE_TIME + ACHIEVEMENT_KEEP_TIME) {
				achievementText = null;
			}
		}
	}
	/**
	 * Render achievement icon with its text below the top status bar.
	 * @param g2 the graphics object
	 * @param component the component that requests the achivement to render
	 */
	public void renderAchievements(Graphics2D g2, JComponent component) {
		if (screenLayerer.isTopScreen(component) && achievementText != null) {
			Composite c = g2.getComposite();
			if (achievementAlpha < 0.99f) {
				g2.setComposite(AlphaComposite.SrcOver.derive(achievementAlpha));
			}
			String msg1 = "Achievement: ";
			int len1 = text.getTextWidth(14, msg1);
			int len2 = text.getTextWidth(14, achievementText);
			g2.setColor(Color.BLACK);
			g2.fillRoundRect(15, 25, Math.max(len1, len2) + cgfx.achievement.getWidth() + 15, 
					cgfx.achievement.getHeight() + 10, 25, 25);
			g2.drawImage(cgfx.achievement, 20, 30, null);
			text.paintTo(g2, 25 + cgfx.achievement.getWidth(), 30, 14, TextGFX.WHITE, msg1);
			text.paintTo(g2, 25 + cgfx.achievement.getWidth(), 50, 14, 0xFFD700, achievementText);
			
			g2.setComposite(c);
		}
	}
	/**
	 * Enqueue an achievement message.
	 * @param message the message to display
	 */
	public void enqueueAchievement(final String message) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				achievementQueue.offer(message);
				achievementFader.start();
			}
		});
	}
	/**
	 * Issue repaint messages to all registered component.
	 */
	private void repaint() {
		for (Component c : components) {
			c.repaint();
		}
	}
	/**
	 * Add component as a repaint target.
	 * @param c the component, if null then ignored
	 */
	public void add(Component c) {
		if (c != null) {
			components.add(c);
		}
	}
	/**
	 * Remove the component from the repaint targets.
	 * @param c the component, if null then ignored
	 */
	public void remove(Component c) {
		if (c != null) {
			components.remove(c);
		}
	}
	/**
	 * Start all fade animations.
	 */
	public void startAnimations() {
		achievementFader.start();
	}
	/**
	 * Stop all fade animations.
	 */
	public void stopAnimations() {
		achievementFader.stop();
	}
	/**
	 * @param screenLayerer the screenLayerer to set
	 */
	public void setScreenLayerer(ScreenLayerer screenLayerer) {
		this.screenLayerer = screenLayerer;
	}
	/**
	 * @return the screenLayerer
	 */
	public ScreenLayerer getScreenLayerer() {
		return screenLayerer;
	}
}
