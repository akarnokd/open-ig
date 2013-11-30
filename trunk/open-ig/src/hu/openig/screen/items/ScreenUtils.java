/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.core.Action0;
import hu.openig.model.Screens;
import hu.openig.model.WalkPosition;
import hu.openig.model.WalkShip;
import hu.openig.model.WalkTransition;
import hu.openig.model.World;
import hu.openig.render.TextRenderer;
import hu.openig.screen.CommonResources;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Utility class to work with reoccurring screen related tasks.
 * @author akarnokd, 2011.08.18.
 */
public final class ScreenUtils {
	/** Utility class. */
	private ScreenUtils() {
		// utility class
	}
	/**
	 * Do the transition from the given position through the given transition.
	 * @param position the position to start from
	 * @param tr the transition to perform
	 * @param commons the common resources
	 * @param skipVideo perform the transition without by skipping the video
	 */
	public static void doTransition(final WalkPosition position, 
			final WalkTransition tr, final CommonResources commons, boolean skipVideo) {
		final String to = tr.to; 
		if (to.startsWith("*") && (tr.media == null || tr.media.isEmpty() || skipVideo)) {
			// move to the screen directly.
			commons.switchScreen(to);
		} else {
			final ShipwalkScreen sws = (ShipwalkScreen)commons.control().displayPrimary(Screens.SHIPWALK);
			sws.setPosition(position);
			
			WalkShip ship = commons.world().getShip();
			sws.next = ship.positions.get(tr.to);
			sws.nextId = tr.to;
			
			final String media = tr.media;
			sws.onCompleted = new Action0() {
				@Override
				public void invoke() {
					sws.onCompleted = null;
					if (to.startsWith("*")) {
						commons.switchScreen(to);
					}
				}
			};
			if (skipVideo) {
				sws.setNextPosition();
			} else {
				sws.startTransition(media);
			}
		}
	}
	/**
	 * Retrieve the walk position for the given location.
	 * @param location the location name, e.g., "*bar", "*diplomacy", "*bridge", etc.
	 * @param world the world object holding the walks information
	 * @return the position or null if not found
	 */
	public static WalkPosition getWalk(String location, World world) {
		WalkShip ws = world.walks.ships.get("" + world.level);
		if (ws != null) {
			WalkPosition p = ws.positions.get(location);
			return p;
		}
		return null;
	}
	/**
	 * Draw the name of the transition centered into its clickable area.
	 * @param g2 The graphics context
	 * @param pointerTransition the transition selected
	 * @param origin the screen origins
	 * @param commons the common resources
	 */
	public static void drawTransitionLabel(Graphics2D g2, WalkTransition pointerTransition,
			Rectangle origin, CommonResources commons) {
		Rectangle r = pointerTransition.area.getBounds();
		String gotoLocation = commons.labels().get(pointerTransition.label);
		int tw = commons.text().getTextWidth(14, gotoLocation) + 10;
		int th = 20;
		
		
		int tx = r.x + (r.width - tw) / 2;
		int ty = r.y + (r.height - th) / 2;
		// do not let the text slide out of the viewport
		if (tx < 0) {
			tx = 0;
		}
		if (tx + tw >= origin.width) {
			tx = origin.width - tw;
		}
		if (ty < 0) {
			ty = 0;
		}
		if (ty + th >= origin.height) {
			ty = origin.height - th;
		}
		
		Composite cp = g2.getComposite();
		g2.setComposite(AlphaComposite.SrcOver.derive(0.85f));
		g2.setColor(Color.BLACK);
		g2.fillRect(origin.x + tx, origin.y + ty, tw, th);
		g2.setComposite(cp);
		commons.text().paintTo(g2, origin.x + tx + 5, origin.y + ty + 3, 14, TextRenderer.YELLOW, gotoLocation);
		
	}

}
