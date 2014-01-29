/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.panels;

import hu.openig.core.Action0;
import hu.openig.core.Pair;
import hu.openig.mechanics.DefaultAIControls;
import hu.openig.model.Planet;
import hu.openig.model.PlanetStatistics;
import hu.openig.model.Research;
import hu.openig.model.ResearchMainCategory;
import hu.openig.model.ResearchType;
import hu.openig.model.SoundType;
import hu.openig.render.TextRenderer;
import hu.openig.screen.CommonResources;
import hu.openig.ui.HorizontalAlignment;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIContainer;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UILabel;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Type;
import hu.openig.ui.UITextButton;
import hu.openig.ui.VerticalAlignment;
import hu.openig.utils.U;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The quick research panel.
 * @author akarnokd, 2012.06.23.
 */
public class QuickResearchPanel extends UIContainer {
	/** The current research status. */
	UILabel currentResearchName;
	/** The current research status. */
	UILabel currentResearchStatus;
	/** The current research money. */
	UILabel currentResearchMoney;
	/** Stop the current research. */
	UITextButton currentResearchStop;
	/** Adjust money. */
	UIImageButton moneyButton;
	/** The margin inside the panel. */
	static final int MARGIN = 6;
	/** The labels for the current available set of researches. */
	final List<List<QuickResearchLabel>> researches = new ArrayList<>();
	/** The top divider y. */
	int topDivider;
	/** The middle divider. */
	int middleDivider;
	/** The middle divider. */
	int bottomDivider;
	/** The last mouse event on the funding button. */
	UIMouse moneyMouseLast;
	/** Description of the currently hovered research. */
	UILabel hoverResearchDescription;
	/** The hover cost. */
	UILabel hoverCost;
	/** Description of the currently hovered research. */
	UILabel hoverResearchTitle;
	/** The current hover text title and details. */
	ResearchType currentText;
	/** The tip for the current research highlighted. */
	UILabel tip;
	/** The lab-active main label. */
	UILabel labActive;
	/** The lab-required main label. */
	UILabel labRequired;
	/** The lab titles. */
	final List<UILabel> labTitles = new ArrayList<>();
	/** The active counts. */
	final List<UILabel> labActives = new ArrayList<>();
	/** The required counts. */
	final List<UILabel> labRequireds = new ArrayList<>();
	/** The common resources. */
	final CommonResources commons;
	/** 
	 * Initialize the fields. 
	 * @param commons the common resources
	 */
	public QuickResearchPanel(final CommonResources commons) {
		this.commons = commons;
		currentResearchName = new UILabel("", 14, commons.text());
		currentResearchStatus = new UILabel("", 10, commons.text());
		currentResearchMoney = new UILabel("", 10, commons.text());
		currentResearchStop = new UITextButton(commons.get("quickresearch.stop"), 14, commons.text()) {
			boolean dragOver;
			@Override
			public boolean mouse(UIMouse e) {
				if (e.has(Type.DRAG)) {
					dragOver = true;
				}
				if (e.has(Type.LEAVE)) {
					dragOver = false;
				}
				if (e.has(Type.UP) && dragOver) {
					dragOver = false;
					if (onClick != null) {
						onClick.invoke();
					}
				}
				return super.mouse(e);
			}
		};
		currentResearchStop.onClick = new Action0() {
			@Override
			public void invoke() {
				ResearchType rrt = commons.player().runningResearch();
				if (rrt != null) {
					commons.player().stopResearch(rrt);
				}
				commons.screenSound(SoundType.STOP_RESEARCH);
				QuickResearchPanel.this.visible(false);
				askRepaint();
			}
		};
		
		moneyButton = new UIImageButton(commons.research().fund) {
			@Override
			public boolean mouse(UIMouse e) {
				moneyMouseLast = e;
				super.mouse(e);
				return true;
			}
        };
		moneyButton.onClick = new Action0() {
			@Override
			public void invoke() {
				commons.buttonSound(SoundType.CLICK_HIGH_3);
				doAdjustMoney(2.0f * (moneyMouseLast.x) / moneyButton.width - 1);
				askRepaint();
			}
		};
		moneyButton.setHoldDelay(100);
		moneyButton.setDisabledPattern(commons.common().disabledPattern);

		hoverResearchDescription = new UILabel("", 10, commons.text());
		hoverResearchDescription.wrap(true);
		
		hoverCost = new UILabel("", 10, commons.text());
		hoverCost.color(TextRenderer.YELLOW);
		
		hoverResearchTitle = new UILabel("", 10, commons.text());
		hoverResearchTitle.color(TextRenderer.RED);
		hoverResearchTitle.horizontally(HorizontalAlignment.CENTER);
		
		labActive = new UILabel(commons.get("quickresearch.lab_available"), 10, commons.text());
		labRequired = new UILabel(commons.get("quickresearch.lab_required"), 10, commons.text());
		
		for (String s : Arrays.asList("civ", "mech", "comp", "ai", "mil")) {
			UILabel l1 = new UILabel(commons.get("quickresearch." + s), 10, commons.text());
			l1.horizontally(HorizontalAlignment.CENTER);
			add(l1);
			labTitles.add(l1);
			
			UILabel l2 = new UILabel("", 10, commons.text());
			l2.horizontally(HorizontalAlignment.CENTER);
			add(l2);
			labActives.add(l2);

			UILabel l3 = new UILabel("", 10, commons.text());
			l3.horizontally(HorizontalAlignment.CENTER);
			add(l3);
			labRequireds.add(l3);

		}
		
		tip = new UILabel("", 10, commons.text());
		tip.color(0xFFE0E0E0);
		tip.wrap(true);
		
		addThis();
	}
	@Override
	public void draw(Graphics2D g2) {
		g2.setColor(new Color(0, 0, 0, 192));
		g2.fillRect(0, 0, width, height);
		g2.setColor(new Color(192, 192, 192));
		g2.drawRect(0, 0, width - 1, height - 1);
		
		g2.drawLine(0, topDivider, width - 1, topDivider);
		g2.drawLine(0, middleDivider, width - 1, middleDivider);
		g2.drawLine(0, bottomDivider, width - 1, bottomDivider);
		super.draw(g2);
	}
	/** Update the contents of the panel. */
	public void update() {
		
		ResearchType ar = commons.player().runningResearch();
		Research rs = commons.player().runningResearchProgress();
		if (rs != null) {
			currentResearchName.text(rs.type.name, true);
			currentResearchName.color(TextRenderer.YELLOW);

			switch (rs.state) {
			case RUNNING:
				currentResearchStatus.text(commons.format("researchinfo.progress.running", String.format("%.1f", rs.getPercent(commons.player().traits))), true);
				break;
			case STOPPED:
				currentResearchStatus.text(commons.format("researchinfo.progress.paused", String.format("%.1f", rs.getPercent(commons.player().traits))), true);
				break;
			case LAB:
				currentResearchStatus.text(commons.format("researchinfo.progress.lab", String.format("%.1f", rs.getPercent(commons.player().traits))), true);
				break;
			case MONEY:
				currentResearchStatus.text(commons.format("researchinfo.progress.money", String.format("%.1f", rs.getPercent(commons.player().traits))), true);
				break;
			default:
				currentResearchStatus.text("");
			}
			currentResearchMoney.text(rs.assignedMoney + "/" + rs.remainingMoney + " cr", true);
			
			currentResearchStop.visible(true);
			moneyButton.visible(true);
		} else {
			currentResearchName.text(commons.get("quickresearch.no_active"), true);
			currentResearchName.color(0xFFE0E0E0);
			currentResearchStatus.text("", true);
			currentResearchMoney.text("", true);
			currentResearchStop.visible(false);
			moneyButton.visible(false);
		}

		currentResearchStop.location(0, MARGIN);

		currentResearchName.location(MARGIN, MARGIN + (currentResearchStop.height - currentResearchName.height) / 2);
		currentResearchStatus.location(currentResearchName.x + currentResearchName.width + 3 * MARGIN, currentResearchName.y + 2);
		moneyButton.location(currentResearchStatus.x + currentResearchStatus.width + 3 * MARGIN, MARGIN + 1);
		currentResearchMoney.location(moneyButton.x + moneyButton.width + 3 * MARGIN, currentResearchName.y + 2);
		currentResearchStop.location(currentResearchMoney.x + currentResearchMoney.width + 3 * MARGIN, MARGIN);
		
		// ---------------------------------------------------------------------
		// collect startable researches
		Map<ResearchMainCategory, List<Pair<ResearchType, Integer>>> columns = new LinkedHashMap<>();
		for (ResearchMainCategory mcat : ResearchMainCategory.values()) {
			columns.put(mcat, new ArrayList<Pair<ResearchType, Integer>>());
		}
		
		PlanetStatistics ps = evaluatePlanetsForResearch();
		
		for (ResearchType rt : commons.world().researches.values()) {
			if (rt.race.contains(commons.player().race) && rt != ar) {
				if (commons.player().canResearch(rt)) {
					columns.get(rt.category.main).add(Pair.of(rt, 
							commons.world().getResearchColor(rt, ps, false)));
				}
			}
		}

		boolean newLines = false;
		boolean anyOver = false;
		// create labels for researches
		int i = 0;
		for (ResearchMainCategory mcat : ResearchMainCategory.values()) {
			List<Pair<ResearchType, Integer>> lst = columns.get(mcat);

			// reorder researches
			Collections.sort(lst, new Comparator<Pair<ResearchType, Integer>>() {
				@Override
				public int compare(Pair<ResearchType, Integer> o1,
						Pair<ResearchType, Integer> o2) {
					int c = o1.first.category.ordinal() - o2.first.category.ordinal();
					if (c == 0) {
						c = o1.first.index - o2.first.index;
					}
					
					return c;
				}
			});
			
			
			if (researches.size() == i) {
				researches.add(new ArrayList<QuickResearchLabel>());
			}
			List<QuickResearchLabel> catlist = researches.get(i);

			for (int j = 0; j < lst.size(); j++) {
				final Pair<ResearchType, Integer> ri = lst.get(j);
				QuickResearchLabel cl;
				if (j == catlist.size()) {
					cl = new QuickResearchLabel("", 10, commons.text());
					catlist.add(cl);
					add(cl);
					newLines = true;
				} else {
					cl = catlist.get(j);
				}
				
				Research rs1 = commons.player().getResearch(ri.first);
				
				if (rs1 == null) {
					cl.text(ri.first.name, true);
				} else {
					cl.text(String.format("%s - %d%%", ri.first.name, (int)rs1.getPercent(commons.player().traits)), true);
				}
				cl.color(ri.second);
				cl.hoverColor(TextRenderer.WHITE);
				cl.height = cl.textSize() + MARGIN;
				cl.vertically(VerticalAlignment.MIDDLE);
				cl.onPress = new Action0() {
					@Override
					public void invoke() {
						new DefaultAIControls(commons.player()).actionStartResearch(ri.first, commons.world().config.researchMoneyPercent / 1000d);
						QuickResearchPanel.this.visible(false);
						askRepaint();
						commons.player().currentResearch(ri.first);
						commons.researchChanged(ri.first);
						commons.computerSound(SoundType.START_RESEARCH);
					}
				};
				if (cl.over) {
					currentText = ri.first;
					anyOver |= cl.over;
				}
			}
			// remove excess lines
			for (int j = catlist.size() - 1; j >= lst.size() ; j--) {
				components.remove(catlist.remove(j));
				newLines = true;
			}
			
			i++;
		}

		if (currentText == null || !anyOver) {
			if (ar != null) {
				currentText = ar;
			} else {
				currentText = null;
			}
		}

		if (newLines) {
			commons.control().moveMouse();
		}
		
		int y0 = currentResearchStop.y + currentResearchStop.height + MARGIN;
		topDivider = y0 - MARGIN / 2;
		int x = MARGIN;
		middleDivider = 0;
		for (List<QuickResearchLabel> lst : researches) {
			int y = y0;
			int mw = 0;
			for (UILabel l : lst) {
				l.x = x;
				l.y = y;
				
				mw = Math.max(mw, l.width);
				y += l.height;
			}
			
			x += MARGIN * 3 + mw;
			middleDivider = Math.max(y, middleDivider);
		}
		middleDivider += MARGIN / 2;
		
		hoverResearchTitle.location(MARGIN, middleDivider + MARGIN / 2);
		hoverResearchTitle.width = 200;

		hoverResearchDescription.location(MARGIN, hoverResearchTitle.y + hoverResearchTitle.height + MARGIN / 2);
		hoverResearchDescription.height = 0;
		hoverCost.height = 0;
		// fill in lab counts

		setActives(labActives, 0, ps.activeLabs.civil, ps.labs.civil, TextRenderer.YELLOW);
		setActives(labActives, 1, ps.activeLabs.mech, ps.labs.mech, TextRenderer.YELLOW);
		setActives(labActives, 2, ps.activeLabs.comp, ps.labs.comp, TextRenderer.YELLOW);
		setActives(labActives, 3, ps.activeLabs.ai, ps.labs.ai, TextRenderer.YELLOW);
		setActives(labActives, 4, ps.activeLabs.mil, ps.labs.mil, TextRenderer.YELLOW);
		
		if (currentText != null) {
			setRequireds(labRequireds, 0, ps.labs.civil, currentText.civilLab, TextRenderer.RED);
			setRequireds(labRequireds, 1, ps.labs.mech, currentText.mechLab, TextRenderer.RED);
			setRequireds(labRequireds, 2, ps.labs.comp, currentText.compLab, TextRenderer.RED);
			setRequireds(labRequireds, 3, ps.labs.ai, currentText.aiLab, TextRenderer.RED);
			setRequireds(labRequireds, 4, ps.labs.mil, currentText.milLab, TextRenderer.RED);
		}
		
		// adjust size ---------------------------------------------------------------
		
		int dw0 = Math.max(labActive.width, labRequired.width) + 3 * MARGIN;
		labActive.visible(false);
		labRequired.visible(false);
		tip.visible(false);
		int dw1 = 0;
		for (UILabel lbl : U.concat(labTitles, labActives, labRequireds)) {
			lbl.visible(false);
			dw1 = Math.max(dw1, lbl.width);
		}

		int dw2 = dw0 + 5 * dw1 + 8 * MARGIN;
		hoverResearchDescription.width = Math.max(200, dw2);
		
		int mw = 0;
		int mh = 0;
		for (UIComponent comp : components) {
			if (comp.visible()) {
				mw = Math.max(mw, comp.x + comp.width);
				mh = Math.max(mh, comp.y + comp.height);
			}
		}
		
		hoverResearchDescription.width = mw - MARGIN;
		hoverResearchTitle.width = hoverResearchDescription.width;

		if (currentText != null) {
			hoverResearchDescription.text(currentText.description);
			hoverResearchTitle.text(currentText.longName);
			hoverResearchDescription.height = hoverResearchDescription.getWrappedHeight();
			hoverCost.text(commons.format("quickresearch.cost", currentText.researchCost(commons.player().traits)), true);
			hoverCost.height = hoverCost.textSize();
			hoverCost.location(hoverResearchDescription.x, 
					hoverResearchDescription.y + hoverResearchDescription.height + MARGIN / 2);
		} else {
			hoverResearchDescription.text("");
			hoverResearchDescription.height = 0;
			hoverResearchTitle.text(commons.get("quickresearch.no_active"));
			hoverCost.text("");
		}
		
		bottomDivider = hoverCost.y + hoverCost.height + MARGIN / 2;
		mh = Math.max(mh, hoverCost.y + hoverCost.height);
		
		// bottom area
		
		
		labActive.location(MARGIN, mh + labActive.height + 2 * MARGIN);
		labActive.visible(true);
		
		labRequired.location(MARGIN, labActive.y + labActive.height + MARGIN);
		labRequired.visible(currentText != null);
		
		
		int widthPart = (mw - MARGIN - Math.max(labActive.width, labRequired.width)) / labActives.size();
		int ii = 0;
		for (UILabel ul : labActives) {
			ul.location(labActive.x 
					+ Math.max(labActive.width, labRequired.width) + 3 * MARGIN + ii * widthPart, labActive.y);
			ii++;
			ul.visible(true);
		}
		
		ii = 0;
		for (UILabel ul : labRequireds) {
			ul.location(labActive.x 
					+ Math.max(labActive.width, labRequired.width) + 3 * MARGIN + ii * widthPart, labRequired.y);
			ii++;
			ul.visible(currentText != null);
		}

		ii = 0;
		for (UILabel ul : labTitles) {
			ul.location(labActive.x 
					+ Math.max(labActive.width, labRequired.width) + 3 * MARGIN + ii * widthPart, mh + MARGIN);
			ii++;
			ul.visible(true);
		}
		if (currentText != null && !currentText.hasEnoughLabs(ps)) {
			tip.location(MARGIN, labRequired.y + labRequired.height + MARGIN);
			
			int reqPlanet = currentText.labCount();
			
			if (!commons.world().noLabLimit()) {
				if (reqPlanet > ps.planetCount) {
					tip.text(commons.get("quickresearch.more_planets"));
				} else 
				if (currentText.hasEnoughLabsBuilt(ps)) {
					tip.text(commons.get("quickresearch.check_labs"));
				} else {
					tip.text(commons.get("quickresearch.reorg_labs"));
				}
			} else {
				tip.text(commons.get("quickresearch.build_more_labs"));
			}
			tip.width = mw - MARGIN;
			tip.height = tip.getWrappedHeight();
			
			tip.visible(true);
		}

		// readjust bounds again
		
		mw = 0;
		mh = 0;
		for (UIComponent comp : components) {
			if (comp.visible()) {
				mw = Math.max(mw, comp.x + comp.width);
				mh = Math.max(mh, comp.y + comp.height);
			}
		}
		
		width = mw + MARGIN;
		height = mh + MARGIN;
	}
	/**
	 * Set an active label based on the numerical values.
	 * @param list the list
	 * @param index the index
	 * @param active the active count
	 * @param total the total count
	 * @param color the color to use if active < total
	 */
	void setActives(List<UILabel> list, int index, int active, int total, int color) {
		UILabel l = list.get(index);
		if (active < total) {
			l.color(color);
			l.text(active + "/" + total, true);
		} else {
			l.color(TextRenderer.GREEN);
			l.text(Integer.toString(active), true);
		}
		
	}
	/**
	 * Set an active label based on the numerical values.
	 * @param list the list
	 * @param index the index
	 * @param active the active count
	 * @param total the total count
	 * @param color the color to use if active < total
	 */
	void setRequireds(List<UILabel> list, int index, int active, int total, int color) {
		UILabel l = list.get(index);
		l.text(Integer.toString(total), true);
		if (active < total) {
			l.color(color);
		} else {
			l.color(TextRenderer.GREEN);
		}
		
	}
	/** Clear contents. */
	@Override
	public void clear() {
		researches.clear();
		for (int i = components.size() - 1; i >= 0; i--) {
			UIComponent c = components.get(i);
			if (c instanceof QuickResearchLabel) {
				components.remove(i);
			}
		}

	}
	@Override
	public UIComponent visible(boolean state) {
		for (List<QuickResearchLabel> cs : researches) {
			for (QuickResearchLabel c : cs) {
				c.over &= state;
			}
		}
		return super.visible(state);
	}
	/**
	 * Adjust money based on the scale.
	 * @param scale the scale factor -1.0 ... +1.0
	 */
	void doAdjustMoney(float scale) {
		Research r = commons.player().runningResearchProgress();
		if (r != null) {
			r.assignedMoney += scale * r.type.researchCost(commons.player().traits) / 20;
			r.assignedMoney = Math.max(Math.min(r.assignedMoney, r.remainingMoney), r.remainingMoney / 8);
		}
	}
	/**
	 * Check player planets for statistics regarding only research matters.
	 * @return the planet statistics
	 */
	PlanetStatistics evaluatePlanetsForResearch() {
		PlanetStatistics ps = new PlanetStatistics();
		
		for (Planet p : commons.player().ownPlanets()) {
			ps.add(p.getResearchStatistics());
		}
		
		return ps;
	}
}
