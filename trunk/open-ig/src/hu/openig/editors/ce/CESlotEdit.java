/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import hu.openig.core.Action1;
import hu.openig.utils.U;
import hu.openig.utils.XElement;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.Transient;
import java.util.List;

import javax.swing.JComponent;

/**
 * The slot editor.
 * @author akarnokd, 2012.11.03.
 */
public class CESlotEdit extends JComponent {
	/** */
	private static final long serialVersionUID = 404806153833510219L;
	/** The background image. */
	protected BufferedImage image;
	/** The current drag rectangle. */
	protected Rectangle selection; 
	/** The parent element of the slots. */
	protected XElement parent;
	/** If a slot is added. */
	public Action1<XElement> onSlotAdded;
	/** If a slot is removed. */
	public Action1<XElement> onSlotRemoved;
	/** The current selected slot. */
	public XElement selectedSlot;
	/** Callback if a slot is picked. */
	public Action1<XElement> onSlotSelected;
	/** Construct the event handlers. */
	public CESlotEdit() {
		MouseActivity ma = new MouseActivity();
		addMouseListener(ma);
		addMouseMotionListener(ma);
		addMouseWheelListener(ma);
	}
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		if (image != null) {
			g2.drawImage(image, 0, 0, null);
		} else {
			g2.setColor(Color.RED);
			g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
			g2.drawLine(0, 0, getWidth() - 1, getHeight() - 1);
			g2.drawLine(getWidth() - 1, 0, 0, getHeight() - 1);
		}
		if (parent != null) {
			Stroke save0 = g2.getStroke();
			g2.setStroke(new BasicStroke(2));
			for (XElement r : parent.childrenWithName("slot")) {
				if (r == selectedSlot) {
					g2.setColor(Color.ORANGE);
				} else {
					g2.setColor(Color.GREEN);
				} 
				try {
					int x = Integer.parseInt(r.get("x", ""));
					int y = Integer.parseInt(r.get("y", ""));
					int w = Integer.parseInt(r.get("width", ""));
					int h = Integer.parseInt(r.get("height", ""));
					g2.drawRect(x, y, w - 1, h - 1);
				} catch (NumberFormatException ex) {
					// ignored
				}
			}
			g2.setColor(Color.RED);
			if (selection != null) {
				g2.drawRect(selection.x, selection.y, selection.width - 1, selection.height - 1);
			}
			
			g2.setStroke(save0);
		}
	}
	@Override
	@Transient
	public Dimension getPreferredSize() {
		if (image != null) {
			return new Dimension(image.getWidth(), image.getHeight());
		}
		return new Dimension(298, 128);
	}
	/**
	 * Set the background image.
	 * @param image the image
	 */
	public void setImage(BufferedImage image) {
		this.image = image;
		invalidate();
		repaint();
	}
	/**
	 * Add a new rectangle.
	 * @param x the X
	 * @param y the Y
	 * @param width the width
	 * @param height the height
	 * @return the created rectangle
	 */
	public XElement addSlot(int x, int y, int width, int height) {
		XElement result = new XElement("slot");
		result.set("x", x);
		result.set("y", y);
		result.set("width", width);
		result.set("height", height);

		parent.add(result);
		repaint();

		return result;
	}
	/**
	 * Add a new rectangle.
	 * @param rect he source rectangle
	 * @return the created element
	 */
	public XElement addSlot(Rectangle rect) {
		return addSlot(rect.x, rect.y, rect.width, rect.height);
	}
	/**
	 * Clear the rectangles.
	 */
	public void clearRectangles() {
		parent.removeChildrenWithName("slot");
		repaint();
	}
	/**
	 * Retrieves the list of slots at a specified coordinate.
	 * @param mx the X
	 * @param my the Y
	 * @return the list of slots
	 */
	public List<XElement> getSlotsAt(int mx, int my) {
		List<XElement> result = U.newArrayList();

		Iterable<XElement> childrenWithName = parent.childrenWithName("slot");
		for (XElement r : childrenWithName) {
			try {
				int x = Integer.parseInt(r.get("x", ""));
				int y = Integer.parseInt(r.get("y", ""));
				int w = Integer.parseInt(r.get("width", ""));
				int h = Integer.parseInt(r.get("height", ""));
				
				if (x <= mx && mx < x + w && y <= my && my < y + h) {
					result.add(r);
				}
			} catch (NumberFormatException ex) {
				// ignored
			}
		}
		return result;
	}
	/**
	 * Remove a slot ad the given location.
	 * @param mx the X
	 * @param my the Y
	 */
	public void removeSlot(int mx, int my) {
		for (XElement r : getSlotsAt(mx, my)) {
			if (onSlotRemoved != null) {
				onSlotRemoved.invoke(r);
			}
			parent.remove(r);
		}
	}
	/**
	 * The mouse activities over the slot editor.
	 * @author akarnokd, 2012.11.03.
	 */
	class MouseActivity extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			if (image == null) {
				return;
			}
			if (e.getButton() == MouseEvent.BUTTON1) {
				selection = new Rectangle(e.getX(), e.getY(), 0, 0);
				List<XElement> sel = getSlotsAt(e.getX(), e.getY());
				if (!sel.isEmpty()) {
					selectedSlot = sel.get(0);
				} else {
					selectedSlot = null;
				}
				if (onSlotSelected != null) {
					onSlotSelected.invoke(selectedSlot);
				}
			} else 
			if (e.getButton() == MouseEvent.BUTTON3) {
				removeSlot(e.getX(), e.getY());
			}
			repaint();
		}
		@Override
		public void mouseDragged(MouseEvent e) {
			if (image == null) {
				return;
			}
			if (selection != null) {
				int mx = e.getX();
				int my = e.getY();
				
				int rx = selection.x;
				int ry = selection.y;
				
				if (mx < rx) {
					selection.x = mx;
					selection.width = rx - mx + 1;
				} else {
					selection.width = mx - rx + 1;
				}
				
				if (my < ry) {
					selection.y = my;
					selection.height = ry - my + 1;
				} else {
					selection.height = my - ry + 1;
				}
			} else {
				selection = new Rectangle(e.getX(), e.getY(), 0, 0);
			}
			repaint();
		}
		@Override
		public void mouseReleased(MouseEvent e) {
			if (image == null) {
				return;
			}
			if (selection != null) {
				if (selection.width > 3 && selection.height > 3) {
					XElement slot = addSlot(selection);
					if (onSlotAdded != null) {
						onSlotAdded.invoke(slot);
					}
					selection = null;
					selectedSlot = slot;
				} else {
					List<XElement> sel = getSlotsAt(e.getX(), e.getY());
					if (!sel.isEmpty()) {
						selectedSlot = sel.get(0);
					} else {
						selectedSlot = null;
					}
				}
				if (onSlotSelected != null) {
					onSlotSelected.invoke(selectedSlot);
				}
			}
			repaint();
		}
	}
	/**
	 * Set the slot parent.
	 * @param parent the parent
	 */
	public void setSlotParent(XElement parent) {
		this.parent = parent;
		repaint();
	}
	/** @return the slot parent */
	public XElement getSlotParent() {
		return parent;
	}
}
