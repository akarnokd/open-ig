/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.ui;

import hu.openig.core.Action0;
import hu.openig.render.TextRenderer;
import hu.openig.ui.UIMouse.Type;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.List;

/**
 * A static label with option to set color, text, alignment
 * content and word wrapping.
 * @author akarnokd, 2011.03.04.
 */
public class UILabel extends UIComponent {
	/** The text renderer. */
	protected TextRenderer tr;
	/** The content text. */
	protected String text;
	/** The font size. */
	protected int size;
	/** The row spacing. */
	protected int spacing = 2;
	/** The ARGB color to use when the label is disabled. */
	protected int disabledColor = TextRenderer.GRAY;
	/** The ARGB color to use when drawing the label. */
	protected int textColor = TextRenderer.GREEN;
	/** The optional hover color if non-zero. */
	protected int hoverColor = 0;
	/** 
	 * The shadow ARGB color to use underneath the label text. Set it to zero
	 * to disable shadowing.
	 */
	private int shadowColor;
	/** 
	 * The shadow ARGB color to use as background color or zero
	 * to disable background.
	 */
	private int backgroundColor;
	/** Is the text wrapped. */
	private boolean wrap;
	/** The horizontal alignment. */
	private HorizontalAlignment align = HorizontalAlignment.LEFT;
	/** The vertical alignment. */
	private VerticalAlignment valign = VerticalAlignment.MIDDLE;
	/** The event handler for the mouse press. */
	public Action0 onPress;
	/** Detect double clicks. */
	public Action0 onDoubleClick;
	/**
	 * Construct a non wrapping label with the given text size.
	 * The label's dimensions are adjusted to the text width and height.
	 * @param text the initial label text
	 * @param size the font size
	 * @param tr the text renderer
	 */
	public UILabel(String text, int size, TextRenderer tr) {
		this.tr = tr;
		this.text = text;
		this.size = size;
		width = tr.getTextWidth(size, text);
		height = size;
	}
	/**
	 * Construct a label width the given size boundaries. The
	 * text is automatically wrapped to the width.
	 * You need to set the <code>height</code> of this label explicitely
	 * to a value or use the <code>getWrappedHeight()</code> value.
	 * @param text the initial text of the label
	 * @param size the font size
	 * @param width the width of the label
	 * @param tr the text renderer component
	 */
	public UILabel(String text, int size, int width, TextRenderer tr) {
		this.tr = tr;
		this.text = text;
		this.size = size;
		this.width = width;
		this.wrap = true;
	}
	@Override
	public void draw(Graphics2D g2) {
		Shape save0 = g2.getClip();
		if (!tr.isUseStandardFonts()) {
			g2.clipRect(0, 0, width, height);
		}
		if (backgroundColor != 0) {
			g2.setColor(new Color(backgroundColor, true));
			g2.fillRect(0, 0, width, height);
		}
		if (wrap) {
			List<String> lines = new ArrayList<>();
			if (shadowColor == 0) {
				tr.wrapText(text, width, size, lines);
			} else {
				tr.wrapText(text, width - 1, size, lines);
			}
			int totalHeight = lines.size() * (size + spacing) - (lines.size() > 0 ? spacing : 0);
			int py = 0;
			switch (valign) {
			case BOTTOM:
				py = height - totalHeight;
				break;
			case MIDDLE:
				py = (height - totalHeight) / 2;
				break;
			default:
			}
			for (String line : lines) {
				drawAligned(g2, py, line);
				py += size + spacing;
			}
		} else {
			int totalHeight = size;
			int py = 0;
			switch (valign) {
			case BOTTOM:
				py = height - totalHeight;
				break;
			case MIDDLE:
				py = (height - totalHeight) / 2;
				break;
			default:
			}
			drawAligned(g2, py, text);
		}
		g2.setClip(save0);
	}
	/**
	 * Draw the text with alignment.
	 * @param g2 the graphics context
	 * @param py the top position to start drawing
	 * @param line a text line
	 */
	void drawAligned(Graphics2D g2, int py, String line) {
		int c;
		if (enabled || disabledColor == 0) {
			if (over && hoverColor != 0) {
				c = hoverColor;
			} else {
				c = textColor;
			}
		} else {
			c = disabledColor;
		}
		if (align != HorizontalAlignment.JUSTIFY) {
			int px = 0;
			switch (align) {
			case LEFT:
				break;
			case CENTER:
				px = (width - tr.getTextWidth(size, line)) / 2;
				break;
			case RIGHT:
				px = width - tr.getTextWidth(size, line);
				break;
			default:
			}
			if (shadowColor != 0) {
				tr.paintTo(g2, px + 1, py + 1, size, shadowColor, line);
			}
			tr.paintTo(g2, px, py, size, c, line);
		} else {
			if (line.length() > 0) {
				String[] words = line.split("\\s+");
				int perword = 0;
				for (String s : words) {
					perword += tr.getTextWidth(size, s);
				}
				float space = 1.0f * (width - perword) / words.length;
				float px = 0;
				if (shadowColor != 0) {
					for (String w : words) {
						tr.paintTo(g2, (int)px + 1, py + 1, size, shadowColor, w);
						px += tr.getTextWidth(size, w) + space;
					}
				}
				px = 0;
				for (String w : words) {
					tr.paintTo(g2, (int)px, py, size, c, w);
					px += tr.getTextWidth(size, w) + space;
				}
			}
		}
	}
	/**
	 * Compute the height in pixels when the text content
	 * is wrapped by the current component width.
	 * Use this to compute and set the height of a multi-line label.
	 * @return the wrapped case height
	 */
	public int getWrappedHeight() {
		List<String> lines = new ArrayList<>();
		if (shadowColor == 0) {
			tr.wrapText(text, width, size, lines);
		} else {
			tr.wrapText(text, width - 1, size, lines);
		}
		return lines.size() * (size + spacing) - (lines.size() > 0 ? spacing : 0);
	}
	/**
	 * Set the label text.
	 * @param text the text to set
	 * @return this
	 */
	public UILabel text(String text) {
		return text(text, false);
	}
	/**
	 * Set the label's text and resize if necessary.
	 * @param text the text
	 * @param resize resize to the new text?
	 * @return this
	 */
	public UILabel text(String text, boolean resize) {
		this.text = text;
		if (resize) {
			width = tr.getTextWidth(size, text);
		}
		return this;
	}
	/** @return the current textual label. */
	public String text() {
		return text;
	}
	/**
	 * Set the label color.
	 * @param textColor the text color to set
	 * @return this
	 */
	public UILabel color(int textColor) {
		this.textColor = textColor;
		return this;
	}
	/**
	 * Set the shadow color. Use 0 to disable the shadow.
	 * @param shadowColor the shadow color
	 * @return this
	 */
	public UILabel shadow(int shadowColor) {
		this.shadowColor = shadowColor;
		return this;
	}
	/**
	 * Set the horizontal alignment.
	 * @param a the horizontal alignment constant
	 * @return this
	 */
	public UILabel horizontally(HorizontalAlignment a) {
		this.align = a;
		return this;
	}
	/**
	 * Set the vertical alignment.
	 * @param a the vertical alignment constant
	 * @return this
	 */
	public UILabel vertically(VerticalAlignment a) {
		this.valign = a;
		return this;
	}
	/**
	 * Set the font size in pixels.
	 * @param h the font size
	 * @return this
	 */
	public UILabel size(int h) {
		this.size = h;
//		if (!wrap) {
//			width = tr.getTextWidth(size, text);
//		}
		return this;
	}
	/**
	 * @return the unwrapped text width.
	 */
	public int getTextWidth() {
		return tr.getTextWidth(size, text) + (shadowColor != 0 ? 1 : 0);
	}
	/**
	 * Set the wrapping mode.
	 * @param state the state
	 * @return wrap
	 */
	public UILabel wrap(boolean state) {
		this.wrap = state;
		return this;
	}
	/**
	 * Set the row spacing for multiline display.
	 * @param value the spacing in pixels
	 * @return this
	 */
	public UILabel spacing(int value) {
		this.spacing = value;
		return this;
	}
	@Override
	public boolean mouse(UIMouse e) {
		if (e.has(Type.DOWN)) {
			if (onPress != null) {
				onPress.invoke();
				return true;
			}
		} else
		if (e.has(Type.DOUBLE_CLICK)) {
			if (onDoubleClick != null) {
				onDoubleClick.invoke();
				return true;
			}
		}
		return super.mouse(e);
	}
	/** @return the current background color. */
	public int backgroundColor() {
		return backgroundColor;
	}
	/**
	 * Sets the background color under the text.
	 * @param newColor the new color
	 */
	public void backgroundColor(int newColor) {
		this.backgroundColor = newColor;
	}
	/**
	 * Sets a new hover color. Use 0 to disable hover coloring.
	 * @param newColor the new hover color
	 */
	public void hoverColor(int newColor) {
		this.hoverColor = newColor;
	}
	/**
	 * @return the current hover color, 0 means no hover coloring
	 */
	public int hoverColor() {
		return this.hoverColor;
	}
	/** @return the text size. */
	public int textSize() {
		return size;
	}
	/**
	 * Size the label to its contents if not a wrapped label.
	 */
	public void sizeToContent() {
		if (!wrap) {
			text(text(), true);
		}
	}
	@Override
	public String toString() {
		return "UILabel: " + text;
	}
}
