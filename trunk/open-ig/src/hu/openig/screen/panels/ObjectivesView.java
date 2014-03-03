/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.panels;

import hu.openig.model.Objective;
import hu.openig.model.ObjectiveState;
import hu.openig.model.Screens;
import hu.openig.render.TextRenderer;
import hu.openig.screen.CommonResources;
import hu.openig.ui.UIComponent;
import hu.openig.utils.U;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * The objectives viewer.
 * @author akarnokd, Jan 12, 2012
 */
public class ObjectivesView extends UIComponent {
	/** The common resources. */
	final CommonResources commons;
	/** For the statusbar's location info. */
	final UIComponent statusbar;
	/** 
	 * Constructor. Initializes the common resources.
	 * @param commons the common resources
	 * @param statusbar the statusbar info
	 */
	public ObjectivesView(CommonResources commons, UIComponent statusbar) {
		this.commons = commons;
		this.statusbar = statusbar;
	}
	@Override
	public void draw(Graphics2D g2) {
		
		int tx = 16;
		int ty = 0;
		if (commons.control().primary() == Screens.COLONY) {
			ty = 110;
		}
		
		g2.translate(tx, ty);
		try {
			int background = 0xC0000000;
			
			List<Objective> objs = commons.world().scripting.currentObjectives();
			
			if (objs.size() == 0) {
				int w = commons.text().getTextWidth(14, commons.get("no_objectives"));
				g2.setColor(new Color(background, true));

				String hideS = commons.get("hide_objectives");
				int hideW = commons.text().getTextWidth(7, hideS);

				w = Math.max(w, hideW);
				
				g2.fillRect(0, 0, w + 10, 33);
				commons.text().paintTo(g2, 5, 3, 14, TextRenderer.GRAY, commons.get("no_objectives"));
				
				g2.setColor(new Color(0xFFC0C0C0));
				g2.drawLine(0, 20, Math.min(hideW, w), 20);
				commons.text().paintTo(g2, 2, 22, 7, 0xFFFFFF00, hideS);

				return;
			}
			
			int limit = statusbar.width - 20;
			
			int w = 0;
			int h = 0;
			
			for (Objective o : objs) {
				w = Math.max(w, objectiveWidth(o, limit));
				int oh = objectiveHeight(o, limit);
				h += 3 + oh;
			}
			h += 3;
			
			String hideS = commons.get("hide_objectives");
			int hideW = commons.text().getTextWidth(7, hideS);
			
			w = Math.min(Math.max(hideW, w), limit);
			
			g2.setColor(new Color(background, true));
			
			g2.fillRect(0, 0, w + 4, h + 13);
			g2.setColor(new Color(0xFFC0C0C0));
			g2.drawRect(0, 0, w + 4, h + 13);
			
			int y = 3;
			for (Objective o : objs) {
				y += drawObjective(g2, o, 2, y, w);
			}
			
			g2.setColor(new Color(0xFFC0C0C0));
			g2.drawLine(0, y + 2, Math.min(hideW, w), y + 2);
			commons.text().paintTo(g2, 2, y + 4, 7, 0xFFFFFF00, hideS);
			
			super.draw(g2);
		} finally {			
			g2.translate(-tx, -ty);
		}
	}
	/**
	 * Draw the objective.
	 * @param g2 the graphics context.
	 * @param o the objective
	 * @param x the left
	 * @param y the top
	 * @param w the draw width
	 * @return the y increment
	 */
	int drawObjective(Graphics2D g2, Objective o, int x, int y, int w) {
		
		g2.setColor(new Color(0xFFB0B0B0));
		g2.drawRect(x + 3, y + 3, 14, 14);
		g2.drawRect(x + 4, y + 4, 12, 12);

		int dy = 0;
		
		if (o.state == ObjectiveState.FAILURE) {
			g2.drawImage(commons.common().crossOut, x, y, null);
		} else
		if (o.state == ObjectiveState.SUCCESS) {
			g2.drawImage(commons.common().checkmarkGreen, x, y, null);
		}
		
		dy += 3;
		dy += drawText(g2, x + 25, y + dy, w - 25, 14, commons.player().color, o.title);
		dy += 3;
		if (o.description != null && !o.description.isEmpty()) {
			dy += drawText(g2, x + 25, y + dy, w - 25, 10, 0xFFC0C0FF, o.description);
		}
		
		String pt = o.progressValue != null ? o.progressValue.invoke() : null;
		Double pv = o.progress != null ? o.progress.invoke() : null;
		if (pv != null || pt != null) {
			int dx = 35;
			if (pt != null) {
				dx += commons.text().getTextWidth(7, pt) + 10;
				commons.text().paintTo(g2, x + 35, y + dy, 7, 0xFFFFCC00, pt);
			}
			
			if (pv != null) {
				int rw = w - dx;
				int rwf = (int)(rw * pv);
				
				g2.setColor(new Color(0xFFFFCC00));
				g2.drawRect(x + dx, y + dy, rw, 7);
				g2.fillRect(x + dx, y + dy, rwf, 7);
			}
			dy += 10;
		}
		
		for (Objective o2 : o.subObjectives) {
			if (o2.visible) {
				dy += drawObjective(g2, o2, x + 25, y + dy, w - 25);
			}
		}
		
		return dy;
	}
	/**
	 * Draw a multiline text by wrapping.
	 * @param g2 the graphics context
	 * @param x the left
	 * @param y the top
	 * @param w the width
	 * @param size the text size
	 * @param color the color
	 * @param text the text
	 * @return the delta y
	 */
	int drawText(Graphics2D g2, int x, int y, int w, int size, int color, String text) {
		List<String> lines = new ArrayList<>();
		commons.text().wrapText(text, w, size, lines);
		int dy = 0;
		for (String s : lines) {
			commons.text().paintTo(g2, x, y + dy, size, color, s);
			dy += size + 3;
		}
		return dy;
	}
	/**
	 * Returns the width of the objective, considering the set of sub-objectives.
	 * @param o the objective
	 * @param limit the width limit
	 * @return the width
	 */
	public int objectiveWidth(Objective o, int limit) {
		int titleWidth = commons.text().getTextWidth(14, o.title);
		int descriptionWidth = o.description != null ? commons.text().getTextWidth(10, o.description) : 0;
		int progressGauge = (o.progress != null ? 100 : 0) + (o.progressValue != null ? commons.text().getTextWidth(7, o.progressValue.invoke()) : 0);
		
		int w = U.max(titleWidth, descriptionWidth, progressGauge);

		int ws = 0;
		for (Objective o2 : o.subObjectives) {
			if (o2.visible) {
				ws = Math.max(ws, objectiveWidth(o2, limit - 25));
			}
		}
		
		return Math.min(25 + Math.max(w, ws), limit);
	}
	/**
	 * Returns the height of the objective considering any sub-objectives.
	 * @param o the objective
	 * @param limit the width limit for wrapping
	 * @return the height
	 */
	public int objectiveHeight(Objective o, int limit) {
		int w = objectiveWidth(o, limit);
		int titleWidth = commons.text().getTextWidth(14, o.title);
		int descriptionWidth = o.description != null ? commons.text().getTextWidth(10, o.description) : 0;
		int progressGauge = (o.progress != null ? 100 : 0) + (o.progressValue != null ? commons.text().getTextWidth(7, o.progressValue.invoke()) : 0);
		
		int h = 0;
		if (titleWidth > w) {
			List<String> lines = new ArrayList<>();
			commons.text().wrapText(o.title, w, 14, lines);
			h += lines.size() * 17 + 3;
		} else {
			h += 20;
		}
		
		if (descriptionWidth > 0) {
			if (descriptionWidth > w) {
				List<String> lines = new ArrayList<>();
				commons.text().wrapText(o.description, w, 10, lines);
				h += lines.size() * 13;
			} else {
				h += 13;
			}
		}

		if (progressGauge > 0) {
			h += 10;
		}
		
		for (Objective o2 : o.subObjectives) {
			if (o2.visible) {
				h += 3 + objectiveHeight(o2, limit - 25);
			}
		}
		
		return h;
	}
}
