/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.render;

import hu.openig.gfx.CommonGFX;
import hu.openig.gfx.TextGFX;
import hu.openig.model.GameSpeed;
import hu.openig.model.GameWorld;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import javax.swing.JComponent;

/**
 * @author karnokd, 2009.05.11.
 * @version $Revision 1.0$
 */
public class InfobarRenderer {
	/** The top right information text area. */
	public final Rectangle topInfoArea = new Rectangle();
	/** The money rectangle. */
	public final Rectangle moneyRect = new Rectangle(75, 3, 66, 14);
	/** The year rectangle. */
	public final Rectangle yearRect = new Rectangle(155, 3, 34, 14);
	/** The month rectangle. */
	public final Rectangle monthRect = new Rectangle(195, 3, 74, 14);
	/** The day rectangle. */
	public final Rectangle dayRect = new Rectangle(275, 3, 18, 14);
	/** The time rectangle. */
	public final Rectangle timeRect = new Rectangle(307, 3, 42, 14);
	/** The common graphics object. */
	private CommonGFX cgfx;
	/** The text graphics object. */
	private TextGFX text;
	/** The game world. */
	private GameWorld gameWorld;
	/**
	 * Constructor. Sets the common graphics and text objects.
	 * @param cgfx the common graphics
	 */
	public InfobarRenderer(CommonGFX cgfx) {
		this.cgfx = cgfx;
		this.text = cgfx.text;
	}
	/**
	 * Renders the information top and bottom bars onto the specified component and graphics context.
	 * @param c the component to draw onto
	 * @param g2 the graphics object to use
	 */
	public void renderInfoBars(JComponent c, Graphics2D g2) {
		int w = c.getWidth();
		int h = c.getHeight();
		g2.drawImage(cgfx.top.left, 0, 0, null);
		g2.drawImage(cgfx.bottom.left, 0, h - cgfx.bottom.left.getHeight(), null);
		g2.drawImage(cgfx.top.right, w - cgfx.top.right.getWidth(), 0, null);
		g2.drawImage(cgfx.bottom.right, w - cgfx.bottom.right.getWidth(), h - cgfx.bottom.left.getHeight(), null);

		// check if the rendering width is greater than the default 640
		// if so, draw the link lines
		int lr = cgfx.top.left.getWidth() + cgfx.top.right.getWidth();
		if (w > lr) {
			AffineTransform at = g2.getTransform();
			g2.translate(cgfx.top.left.getWidth(), 0);
			g2.scale(w - lr, 1);
			g2.drawImage(cgfx.top.link, 0, 0, null);

			g2.setTransform(at);
			g2.translate(cgfx.bottom.left.getWidth(), 0);
			g2.scale(w - lr, 1);
			g2.drawImage(cgfx.bottom.link, 0, h - cgfx.bottom.link.getHeight(), null);
			g2.setTransform(at);
		}
		// draw the speed buttons as needed
		if (gameWorld.showSpeedControls) {
			g2.drawImage(gameWorld.gameSpeed == GameSpeed.PAUSED ? cgfx.timePauseSelected : cgfx.timePause, 1 + 0 * 15, 2, null);
			g2.drawImage(gameWorld.gameSpeed == GameSpeed.NORMAL ? cgfx.timeNormalSelected : cgfx.timeNormal, 1 + 1 * 15, 2, null);
			g2.drawImage(gameWorld.gameSpeed == GameSpeed.FAST ? cgfx.timeFastSelected : cgfx.timeFast, 1 + 2 * 15, 2, null);
			if (gameWorld.showUltrafast) {
				g2.drawImage(gameWorld.gameSpeed == GameSpeed.ULTRAFAST ? cgfx.timeUltrafastSelected : cgfx.timeUltrafast, 1 + 3 * 15, 2, null);
			} else {
				g2.drawImage(cgfx.timeNone, 1 + 3 * 15, 2, null);
			}
		} else {
			// hide all controls
			for (int i = 0; i < 4; i++) {
				g2.drawImage(cgfx.timeNone, 1 + i * 15, 2, null);
			}
		}
		// paint money amount
		if (gameWorld.player != null) {
			String moneyStr = String.valueOf(gameWorld.player.money);
			int moneyLength = text.getTextWidth(10, moneyStr);
			text.paintTo(g2, moneyRect.x + moneyRect.width - moneyLength - 1, 
					moneyRect.y + 2, 10, gameWorld.player.race.color, moneyStr);
		}
		// draw the current time
		if (gameWorld.showTime) {
			text.paintTo(g2, yearRect.x + 1, yearRect.y + 2, 10, 
					gameWorld.player.race.color, Integer.toString(gameWorld.getYear()));
			text.paintTo(g2, monthRect.x + 1, monthRect.y + 2, 10, 
					gameWorld.player.race.color, gameWorld.getLabel("MONTHNAME_" + gameWorld.getMonth()));
			text.paintTo(g2, dayRect.x + 1, dayRect.y + 2, 10, 
					gameWorld.player.race.color, String.format("%02d", gameWorld.getDay()));
			
			
			
			int minute = gameWorld.getMinute();
			if (gameWorld.gameSpeed != GameSpeed.PAUSED) {
				// if game is not paused, the 10s of minutes are rounded off
				minute = (minute / 60) * 60;
			}
			text.paintTo(g2, timeRect.x + 1, timeRect.y + 2, 10, 
					gameWorld.player.race.color, String.format("%02d", gameWorld.getHour()));

			String colonStr = ":";
			int colonLength = text.getTextWidth(10, colonStr);
			
			text.paintTo(g2, timeRect.x + (timeRect.width - 1 - colonLength) / 2, timeRect.y + 2, 10, 
					gameWorld.player.race.color, colonStr);
			
			String minuteStr = String.format("%02d", minute);
			int minuteLength = text.getTextWidth(10, minuteStr);
			text.paintTo(g2, timeRect.x + timeRect.width - 1 - minuteLength, timeRect.y + 2, 10, 
					gameWorld.player.race.color, minuteStr);
		}
	}
	/**
	 * Update interactable region coordinates on the information bars for the given component.
	 * @param c the parent component
	 */
	public void updateRegions(JComponent c) {
		int w = c.getWidth();
		//int h = c.getHeight();
		// location of the top info area
		topInfoArea.x = 387;
		topInfoArea.y = 2;
		topInfoArea.width = w - topInfoArea.x - 11;
		topInfoArea.height = 16;
	}
	/**
	 * @param gameWorld the gameWorld to set
	 */
	public void setGameWorld(GameWorld gameWorld) {
		this.gameWorld = gameWorld;
	}
	/**
	 * @return the gameWorld
	 */
	public GameWorld getGameWorld() {
		return gameWorld;
	}
}
