/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.panels;

import hu.openig.core.Action0;
import hu.openig.model.Message;
import hu.openig.model.Screens;
import hu.openig.model.SelectionMode;
import hu.openig.model.SoundType;
import hu.openig.render.TextRenderer;
import hu.openig.screen.CommonResources;
import hu.openig.ui.UIContainer;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Type;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/** The scrollable list of notification history. */
public class NotificationHistory extends UIContainer {
	/** The common resources. */
	final CommonResources commons;
	/** The bottom row index counted from the last element. */
	int bottom = 0;
	/** Clear history. */
	public UIImageButton clear;
	/** Scroll up. */
	UIImageButton scrollUp;
	/** Scroll down. */
	UIImageButton scrollDown;
	/** The event time format. */
	SimpleDateFormat sdf;
	/** 
	 * Construct the buttons.
	 * @param commons the common resources
	 */
	public NotificationHistory(final CommonResources commons) {
		this.commons = commons;
		scrollUp = new UIImageButton(commons.common().moveUp);
		scrollUp.onClick = new Action0() {
			@Override
			public void invoke() {
				commons.buttonSound(SoundType.CLICK_MEDIUM_2);
				doScrollUp();
			}
		};
		scrollUp.setHoldDelay(250);
		scrollDown = new UIImageButton(commons.common().moveDown);
		scrollDown.onClick = new Action0() {
			@Override
			public void invoke() {
				commons.buttonSound(SoundType.CLICK_MEDIUM_2);
				doScrollDown();
			}
		};
		scrollDown.setHoldDelay(250);
		
		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		sdf.setCalendar(new GregorianCalendar(TimeZone.getTimeZone("GMT")));
		clear = new UIImageButton(commons.common().delete) {
			@Override
			public boolean mouse(UIMouse e) {
				return super.mouse(e);
			}
		};
		clear.onClick = new Action0() {
			@Override
			public void invoke() {
				commons.buttonSound(SoundType.CLICK_MEDIUM_2);
				doClearHistory();
			}
		};
		addThis();
	}
	/**
	 * Clear the entire history.
	 */
	void doClearHistory() {
		commons.player().messageHistory.clear();
	}
	@Override
	public void draw(Graphics2D g2) {
		g2.setColor(new Color(0, 0, 0, 224));
		g2.fillRect(0, 0, width, height);
		g2.setColor(Color.GRAY);
		g2.drawRect(0, 0, width - 1, height - 1);
		
		Shape save0 = g2.getClip();
		g2.clipRect(0, 0, width - 30, height);
		
		int rowHeight = 20;
		int rows = height / rowHeight;
		bottom = Math.max(0, Math.min(bottom, commons.player().messageHistory.size() - (height / 20)));
		int start = commons.player().messageHistory.size() - bottom - 1;
		int y = height - rowHeight; 
		for (int i = start; i >= start - rows && i >= 0; i--) {
			Message msg = commons.player().messageHistory.get(i);
			
			// use an icon
			if ("message.yesterday_tax_income".equals(msg.text)
					|| "message.yesterday_trade_income".equals(msg.text)
			) {
				g2.drawImage(commons.statusbar().moneyNotification, 4, y + 2, null);
			} else
			if (msg.targetProduct != null) {
				g2.drawImage(commons.statusbar().productionNotify, 4, y + 2, null);
			} else
			if (msg.targetResearch != null) {
				g2.drawImage(commons.statusbar().researchNotify, 4, y + 2, null);
			} else
			if (msg.targetPlanet != null) {
				g2.drawImage(msg.targetPlanet.type.body[0], 4, y + 2, 16, 16, null);
			} else
			if (msg.targetFleet != null) {
				int dx = (20 - msg.targetFleet.owner.fleetIcon.getWidth()) / 2;
				int dy = (20 - msg.targetFleet.owner.fleetIcon.getHeight()) / 2;
				g2.drawImage(msg.targetFleet.owner.fleetIcon, dx, y + dy, null);
			}
			
			String msgText = commons.get(msg.text);
			int idx = msgText.indexOf("%s");
			if (idx < 0) {
				commons.text().paintTo(g2, 26, y + 5, 10, 
						TextRenderer.GREEN, msgText);
			} else {
				String pre = msgText.substring(0, idx);
				String post = msgText.substring(idx + 2);
				String param = "";
				if (msg.targetPlanet != null) {
					param = msg.targetPlanet.name();
				} else
				if (msg.targetFleet != null) {
					param = msg.targetFleet.name();
				} else
				if (msg.targetProduct != null) {
					param = msg.targetProduct.name;
				} else
				if (msg.targetResearch != null) {
					param = msg.targetResearch.name;
				} else
				if (msg.value != null) {
					param = msg.value;
				} else 
				if (msg.label != null) {
					param = commons.get(msg.label);
				}
				int w0 = commons.text().getTextWidth(10, pre);
				int w1 = commons.text().getTextWidth(10, param);
				int w2 = commons.text().getTextWidth(10, post);
				
				commons.text().paintTo(g2, 26, y + 5, 10, 
						TextRenderer.GREEN, pre);
				commons.text().paintTo(g2, 26 + w0, y + 5, 10, 
						TextRenderer.YELLOW, param);
				commons.text().paintTo(g2, 26 + w0 + w1, y + 5, 10, 
						TextRenderer.GREEN, post);
				
				commons.text().paintTo(g2, 26 + w0 + w1 + w2 + 20, y + 7, 7, TextRenderer.GRAY, sdf.format(new Date(msg.gametime)));
				
			}
			y -= rowHeight;
		}
		g2.setClip(save0);
		
		clear.location(width - 30, 1);
		scrollUp.location(width - 30, clear.y + clear.height + 5);
		scrollDown.location(width - 30, height - 30);
		
		clear.visible(!commons.player().messageHistory.isEmpty());
		scrollUp.visible(bottom + rows < commons.player().messageHistory.size());
		scrollDown.visible(bottom > 0);
		
		super.draw(g2);
	}
	/** Scroll up. */
	void doScrollUp() {
		bottom = Math.max(0, Math.min(bottom + 1, commons.player().messageHistory.size() - (height / 20)));
	}
	/** Scroll down. */
	void doScrollDown() {
		bottom = Math.max(0, Math.min(bottom - 1, commons.player().messageHistory.size() - (height / 20)));
	}
	@Override
	public boolean mouse(UIMouse e) {
		if (e.has(Type.WHEEL)) {
			if (e.z < 0) {
				doScrollUp();
			} else {
				doScrollDown();
			}
			return true;
		} else
		if (!commons.battleMode && e.has(Type.DOUBLE_CLICK) && e.x < width - 31) {
			int idx = commons.player().messageHistory.size() - 1 - bottom - ((height - e.y) / 20);
			if (idx >= 0 && idx < commons.player().messageHistory.size()) {
				Message currentMessage = commons.player().messageHistory.get(idx);
				if (currentMessage.targetPlanet != null) {
					commons.player().currentPlanet = currentMessage.targetPlanet;
					commons.player().selectionMode = SelectionMode.PLANET;
					commons.control().displayPrimary(Screens.COLONY);
					visible(false);
					return true;
				} else
				if (currentMessage.targetFleet != null) {
					commons.player().currentFleet = currentMessage.targetFleet;
					commons.player().selectionMode = SelectionMode.FLEET;
					commons.control().displayPrimary(Screens.EQUIPMENT);
					visible(false);
					return true;
				} else
				if (currentMessage.targetProduct != null) {
					commons.world().selectResearch(currentMessage.targetProduct);
					commons.control().displaySecondary(Screens.PRODUCTION);
					visible(false);
					return true;
				} else
				if (currentMessage.targetResearch != null) {
					commons.world().selectResearch(currentMessage.targetResearch);
					commons.control().displaySecondary(Screens.RESEARCH);
					visible(false);
					return true;
				}
			}
		}
		return super.mouse(e);
	}
}
