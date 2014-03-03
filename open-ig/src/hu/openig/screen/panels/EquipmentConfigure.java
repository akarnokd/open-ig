/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.panels;

import hu.openig.core.Action1;
import hu.openig.core.Func1;
import hu.openig.model.InventoryItem;
import hu.openig.model.InventorySlot;
import hu.openig.model.ResearchType;
import hu.openig.model.World;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Button;
import hu.openig.ui.UIMouse.Type;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;

/**
 * The inventory item configuration component.
 * @author akarnokd, Apr 13, 2011
 */
public class EquipmentConfigure extends UIComponent {
	/** The type for the image. */
	public ResearchType type;
	/** The current inventory item. */
	public InventoryItem item;
	/** The selected slot. */
	public InventorySlot selectedSlot;
	/** The action to invoke. */
	public Action1<InventorySlot> onSelect;
	/** The associated world callback. */
	public Func1<Void, World> world;
	/**
	 * Constructor with a world callback.
	 * @param world the world callback
	 */
	public EquipmentConfigure(Func1<Void, World> world) {
		this.world = world;
	}
	@Override
	public void draw(Graphics2D g2) {
		if (type == null || type.equipmentCustomizeImage == null) {
			return;
		}
		BufferedImage image = type.equipmentCustomizeImage;
		float fx = width * 1.0f / image.getWidth();
		float fy = height * 1.0f / image.getHeight();
		float f = Math.min(fx, fy);
		int dx = (int)((width - image.getWidth() * f) / 2);
		int dy = (int)((height - image.getHeight() * f) / 2);
		g2.drawImage(image, dx, dy, (int)(image.getWidth() * f), (int)(image.getHeight() * f), null);

		if (item == null) {
			return;
		}
		
		drawSlots(g2, item, selectedSlot, world.invoke(null));
	}
	/**
	 * Draw the inventory slots.
	 * @param g2 the graphics context
	 * @param item the inventory item
	 * @param selected the current slot
	 * @param world the world object
	 */
	public static void drawSlots(Graphics2D g2, 
			InventoryItem item, InventorySlot selected, World world) {
		Color green = new Color(0x009A00);
		for (InventorySlot is : item.slots.values()) {
			if (!is.slot.fixed) {
				if (is.type != null) {
					g2.setColor(is == selected ? green : Color.BLACK);
					g2.drawRect(is.slot.x, is.slot.y, is.slot.width - 1, is.slot.height - 1);
					int ihp = is.hpMax(item.owner);
					if (is != selected && is.hp < ihp) {
						g2.setColor(interpolate(
								is.hp * 1.0f / ihp, 
								Color.RED, Color.ORANGE, Color.YELLOW));
					} else {
						g2.setColor(green);
					}
					g2.drawRect(is.slot.x + 1, is.slot.y + 1, is.slot.width - 3, is.slot.height - 3);
				} else {
					g2.setColor(is == selected ? green : Color.BLACK);
					g2.drawRect(is.slot.x + 1, is.slot.y + 1, is.slot.width - 3, is.slot.height - 3);
					g2.drawRect(is.slot.x, is.slot.y, is.slot.width - 1, is.slot.height - 1);
					g2.setColor(green);
					Stroke save0 = g2.getStroke();
					g2.setStroke(new BasicStroke(1.0f,
	                        BasicStroke.CAP_BUTT,
	                        BasicStroke.JOIN_MITER,
	                        1.0f, new float[] { 2f, 1f }, 0.0f));
					
					g2.drawRect(is.slot.x + 1, is.slot.y + 1, is.slot.width - 3, is.slot.height - 3);
					g2.drawRect(is.slot.x, is.slot.y, is.slot.width - 1, is.slot.height - 1);
					g2.setStroke(save0);
				}
			}
		}
	}
	@Override
	public boolean mouse(UIMouse e) {
		if (onSelect != null && item != null 
				&& e.has(Button.LEFT) && e.has(Type.DOWN)) {
			for (InventorySlot es : item.slots.values()) {
				if (!es.slot.fixed && e.within(es.slot.x, es.slot.y, es.slot.width, es.slot.height)) {
					onSelect.invoke(es);
					return true;
				}
			}
			onSelect.invoke(null);
			return true;
		}
		return super.mouse(e);
	}
	/**
	 * Interpolate between two colors.
	 * @param amount the amount 0..1
	 * @param colors the available color points
	 * @return the new color
	 */
	static Color interpolate(double amount, Color... colors) {
		int idx = (int)(amount * (colors.length - 1));
		if (idx == colors.length - 1) {
			return colors[colors.length - 1];
		}
		
		float f0 = idx * 1.0f / (colors.length - 1);
		float f1 = 1.0f / (colors.length - 1);
		
		int a0 = colors[idx].getAlpha();
		int r0 = colors[idx].getRed();
		int g0 = colors[idx].getGreen();
		int b0 = colors[idx].getBlue();
		
		int a1 = colors[idx + 1].getAlpha();
		int r1 = colors[idx + 1].getRed();
		int g1 = colors[idx + 1].getGreen();
		int b1 = colors[idx + 1].getBlue();
		
		return new Color(
				(int)(r0 + (amount - f0) / f1 * (r1 - r0)),
				(int)(g0 + (amount - f0) / f1 * (g1 - g0)),
				(int)(b0 + (amount - f0) / f1 * (b1 - b0)),
				(int)(a0 + (amount - f0) / f1 * (a1 - a0))
		);
	}
	/**
	 * Assign the type, item and slot values from the another instance.
	 * @param other the another instance
	 */
	public void assign(EquipmentConfigure other) {
		this.type = other.type;
		this.item = other.item;
		this.selectedSlot = other.selectedSlot;
	}
	/**
	 * Clear the configuration.
	 */
	public void clear() {
		this.type = null;
		this.item = null;
		this.selectedSlot = null;
	}
}
