/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import hu.openig.core.Action1;
import hu.openig.utils.GUIUtils;
import hu.openig.utils.XElement;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

/**
 * The base panel with some convenience support.
 * @author akarnokd, 2012.10.31.
 */
public class CEBasePanel extends JPanel {
	/** */
	private static final long serialVersionUID = 6893457628554154892L;
	/** The context. */
	protected final CEContext context;
	/** The okay icon. */
	protected ImageIcon okIcon;
	/** The error icon. */
	protected ImageIcon errorIcon;
	/** The warning icon. */
	protected ImageIcon warningIcon;
	/** The severity order. */
	protected final List<ImageIcon> severityOrder;
	/**
	 * Constructor. Saves the context.
	 * @param context the context object
	 */
	public CEBasePanel(CEContext context) {
		this.context = context;
		okIcon = GUIUtils.createColorImageIcon(16, 16, 0);
		errorIcon = context.getIcon(CESeverityIndicator.ERROR);
		warningIcon = context.getIcon(CESeverityIndicator.WARNING);
		
		severityOrder = Arrays.asList(okIcon, warningIcon, errorIcon);
	}
	/**
	 * Get a translation for the given key.
	 * @param key the key
	 * @return the translation
	 */
	public String get(String key) {
		return context.get(key);
	}
	/**
	 * Format a translation for the given key and parameters.
	 * @param key the key
	 * @param params the parameters
	 * @return the translation
	 */
	public String format(String key, Object... params) {
		return context.format(key, params);
	}
	/**
	 * Creates an image icon for the given resource path.
	 * @param path the path
	 * @return the image icon
	 */
	public ImageIcon icon(String path) {
		URL u = CEBasePanel.class.getResource(path);
		if (u == null) {
			System.err.printf("Missing resource: %s%n", path);
            return new ImageIcon();
		}
		return new ImageIcon(u);
	}
	/**
	 * @return Create a number field.
	 */
	public JTextField numberField() {
		JTextField f = new JTextField();
		f.setHorizontalAlignment(JTextField.RIGHT);
		return f;
	}
	/**
	 * Validate that the field contains a valid number.
	 * @param f the field
	 * @param icon the current error icon
	 * @param required if true then empty value is not allowed
	 * @return the modified error icon
	 */
	public ImageIcon validateNumberField(CEValueBox<JTextField> f, ImageIcon icon, boolean required) {
		return validateNumberField(f, icon, Integer.MIN_VALUE, Integer.MAX_VALUE, required);
	}
	/**
	 * Validate that the field contains a valid number between the given range.
	 * @param f the field
	 * @param icon the current error icon
	 * @param min the minimum value
	 * @param max the maximum value
	 * @param required if true then empty value is not allowed
	 * @return the modified error icon
	 */
	public ImageIcon validateNumberField(CEValueBox<JTextField> f, ImageIcon icon, int min, int max, boolean required) {
		String value = f.component.getText();
		if (value.isEmpty()) {
			if (required) {
				f.setInvalid(errorIcon, get("field.invalid.empty"));
				icon = max(icon, errorIcon);
			}
		} else {
			try {
				int n = Integer.parseInt(value);
				if (n < min || n > max) {
					f.setInvalid(errorIcon, format("field.invalid.range", min, max));
					icon = max(icon, errorIcon);
				}
			} catch (NumberFormatException ex) {
				f.setInvalid(errorIcon, get("field.invalid.number"));
				icon = max(icon, errorIcon);
			}
		}
		return icon;
	}
	/**
	 * Select the more severe icon.
	 * @param current the current icon
	 * @param other the new icon
	 * @return the more severe icon
	 */
	public ImageIcon max(ImageIcon current, ImageIcon other) {
		if (current == null && other != null) {
			return other;
		}
		if (other != null) {
			int ci = severityOrder.indexOf(current);
			int co = severityOrder.indexOf(other);
			if (ci < co) {
				return other;
			}
		}
		return current;
	}
	/**
	 * Validate the label reference field.
	 * @param f the field
	 * @param icon the icon
	 * @return the error indicator
	 */
	public ImageIcon validateLabelRefField(CEValueBox<JTextField> f, ImageIcon icon) {
		String value = f.component.getText();
		String label = context.dataManager().label(value);
		if (label == null || label.isEmpty()) {
			f.setInvalid(warningIcon, get("field.invalid.missing_label"));
			icon = max(icon, warningIcon);
		} else {
			f.clearInvalid();
		}
		return icon;
	}
	/**
	 * Get a resource as an image icon.
	 * @param resource the resource path with extension
	 * @return the image or null if not found
	 */
	public ImageIcon getImageIcon(String resource) {
		BufferedImage img = context.dataManager().getImage(resource);
		if (img != null) {
			return new ImageIcon(img);
		}
		return null;
	}
	/**
	 * Add a text change event handler for the given textfield.
	 * @param c the value box
	 * @param action the action
	 */
	public static void addValidator(
			final CEValueBox<? extends JTextComponent> c, 
			final Action1<? super JTextComponent> action) {
		c.validator = action;
		c.component.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				action.invoke(c.component);
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				action.invoke(c.component);
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				action.invoke(c.component);
			}
		});
	}
	/**
	 * Add a selection change handler.
	 * @param c the combo box
	 * @param action the action
	 */
	public static void addValidator2(
			final CEValueBox<? extends JComboBox<String>> c, 
			final Action1<? super JComboBox<String>> action) {
		c.validator = action;
		c.component.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				action.invoke(c.component);
			}
		});
	}
	/**
	 * Sets the text content of the value box and enables/disables it.
	 * @param c the value box
	 * @param item the item, if null, the attribute contains the exact text to set
	 * @param attribute the attribute
	 * @param enabled true if enabled
	 */
	public void setTextAndEnabled(CEValueBox<? extends JTextComponent> c, XElement item, String attribute, boolean enabled) {
		c.component.setEnabled(enabled);
		c.label.setEnabled(enabled);
		String s = item != null ? item.get(attribute, "") : attribute;
		String prev = c.component.getText();
		c.component.setText(s);
		if (!enabled) {
			c.clearInvalid();
		} else {
			if (prev.equals(s)) {
				c.validateComponent();
			}
		}
	}
	/**
	 * Set a combobox index based on the attribute value matching the set of values.
	 * @param c the combobox
	 * @param item the item, if null, the attr will be used as setSelectedItem()
	 * @param attr the attribute
	 * @param enabled enabled
	 * @param enumClass the enumeration class
	 */
	public void setChoiceAndEnabled(CEValueBox<? extends JComboBox<String>> c, 
			XElement item, String attr, boolean enabled, Class<? extends Enum<?>> enumClass) {
		Object[] ecs = enumClass.getEnumConstants();
		String[] enums = new String[ecs.length];
		for (int i = 0; i < enums.length; i++) {
			enums[i] = ecs[i].toString();
		}
		setChoiceAndEnabled(c, item, attr, enabled, enums);
	}
	/**
	 * Set a combobox index based on the attribute value matching the set of values.
	 * @param c the combobox
	 * @param item the item, if null, the attr will be used as setSelectedItem()
	 * @param attr the attribute
	 * @param enabled enabled
	 * @param enums the set of values
	 */
	public void setChoiceAndEnabled(CEValueBox<? extends JComboBox<String>> c, 
			XElement item, String attr, boolean enabled, String... enums) {
		c.component.setEnabled(enabled);
		c.label.setEnabled(enabled);
		if (item == null) {
			Object prev = c.component.getSelectedItem();
			c.component.setSelectedItem(attr);
			if (Objects.equals(prev, attr)) {
				c.validateComponent();
			}
		} else {
			if (!enabled) {
				c.clearInvalid();
			} else {
				String s = item.get(attr, "");
				int idx = -1;
				int j = 0;
				for (String e : enums) {
					if (Objects.equals(e, s)) {
						idx = j;
						break;
					}
					j++;
				}
				int oidx = c.component.getSelectedIndex();
				c.component.setSelectedIndex(idx);
				if (oidx == idx) {
					c.validateComponent();
				}
			}
		}
	}
}
