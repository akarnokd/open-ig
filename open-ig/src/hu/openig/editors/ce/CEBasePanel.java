/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import hu.openig.core.Action1;
import hu.openig.utils.Exceptions;
import hu.openig.utils.XElement;

import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.net.URL;

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
	/** The error icon. */
	protected ImageIcon errorIcon;
	/** The warning icon. */
	protected ImageIcon warningIcon;
	/**
	 * Constructor. Saves the context.
	 * @param context the context object
	 */
	public CEBasePanel(CEContext context) {
		this.context = context;
		errorIcon = context.getIcon(CESeverityIndicator.ERROR);
		warningIcon = context.getIcon(CESeverityIndicator.WARNING);
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
		URL u = getClass().getResource(path);
		if (u == null) {
			System.err.printf("Missing resource: %s%n", path);
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
	 * Store the contents of the value boxes into the item as attributes.
	 * @param item the output item
	 */
	public void getValueBoxes(XElement item) {
		try {
			for (Field f : getClass().getDeclaredFields()) {
				if (CEValueBox.class.isAssignableFrom(f.getType())) {
					CEValueBox<?> v = (CEValueBox<?>)f.get(this);
					if (f.isAnnotationPresent(CETextAttribute.class)) {
						CETextAttribute ta = f.getAnnotation(CETextAttribute.class);
						if (v.component instanceof JTextField) {
							String text = ((JTextField)v.component).getText();
							item.set(ta.name(), text.isEmpty() ? null : text);
						} else {
							Exceptions.add(new AssertionError("Unsupported TextAttribute component: " + v.component));
						}
					} else
					if (f.isAnnotationPresent(CEEnumAttribute.class)) {
						CEEnumAttribute ea = f.getAnnotation(CEEnumAttribute.class);
						if (v.component instanceof JComboBox<?>) {
							int idx = ((JComboBox<?>)v.component).getSelectedIndex();
							if (idx < 0) {
								item.set(ea.name(), null);
							} else {
								item.set(ea.name(), ea.enumClass().getEnumConstants()[idx]);
							}
						} else {
							Exceptions.add(new AssertionError("Unsupported EnumAttribute component: " + v.component));
						}
					} else
					if (f.isAnnotationPresent(CETextEnumAttribute.class)) {
						CETextEnumAttribute ta = f.getAnnotation(CETextEnumAttribute.class);
						if (v.component instanceof JComboBox<?>) {
							int idx = ((JComboBox<?>)v.component).getSelectedIndex();
							if (idx < 0) {
								item.set(ta.name(), null);
							} else {
								item.set(ta.name(), ta.values()[idx]);
							}
						} else {
							Exceptions.add(new AssertionError("Unsupported TextEnumAttribute component: " + v.component));
						}
					}
				}
			}
		} catch (IllegalAccessException ex) {
			Exceptions.add(ex);
		}
	}
	/**
	 * Set the value box contents from the supplied item.
	 * @param item the item
	 */
	public void setValueBoxes(XElement item) {
		try {
			for (Field f : getClass().getDeclaredFields()) {
				if (CEValueBox.class.isAssignableFrom(f.getType())) {
					CEValueBox<?> v = (CEValueBox<?>)f.get(this);
					if (f.isAnnotationPresent(CETextAttribute.class)) {
						CETextAttribute ta = f.getAnnotation(CETextAttribute.class);
						if (v.component instanceof JTextField) {
							((JTextField)v.component).setText(item.get(ta.name(), ""));
						} else {
							Exceptions.add(new AssertionError("Unsupported TextAttribute component: " + v.component));
						}
					} else
					if (f.isAnnotationPresent(CEEnumAttribute.class)) {
						CEEnumAttribute ea = f.getAnnotation(CEEnumAttribute.class);
						if (v.component instanceof JComboBox<?>) {
							int i = 0;
							int idx = -1;
							for (Enum<?> ec : ea.enumClass().getEnumConstants()) {
								if (ec.toString().equals(item.get(ea.name(), ""))) {
									idx = i;
									break;
								}
								i++;
							}
							((JComboBox<?>)v.component).setSelectedIndex(idx);
						} else {
							Exceptions.add(new AssertionError("Unsupported EnumAttribute component: " + v.component));
						}
					} else
					if (f.isAnnotationPresent(CETextEnumAttribute.class)) {
						CETextEnumAttribute ta = f.getAnnotation(CETextEnumAttribute.class);
						if (v.component instanceof JComboBox<?>) {
							int i = 0;
							int idx = -1;
							for (String ec : ta.values()) {
								if (ec.equals(item.get(ta.name(), ""))) {
									idx = i;
									break;
								}
								i++;
							}
							((JComboBox<?>)v.component).setSelectedIndex(idx);
						} else {
							Exceptions.add(new AssertionError("Unsupported TextEnumAttribute component: " + v.component));
						}
					}
				}
			}
		} catch (IllegalAccessException ex) {
			Exceptions.add(ex);
		}
	}
	/**
	 * Clear the contents of the value boxes.
	 */
	public void clearValueBoxes() {
		for (Field f : getClass().getDeclaredFields()) {
			if (CEValueBox.class.isAssignableFrom(f.getType())) {
				try {
					CEValueBox<?> v = (CEValueBox<?>)f.get(this);
					if (v.component instanceof JTextField) {
						((JTextField)v.component).setText("");
					} else
					if (v.component instanceof JComboBox<?>) {
						((JComboBox<?>)v.component).setSelectedIndex(-1);
					}
				} catch (IllegalAccessException ex) {
					Exceptions.add(ex);
				}
			}
		}
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
		if ((current == null || current == warningIcon) && other != null) {
			return other;
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
		String label = context.label(value);
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
		BufferedImage img = context.getImage(resource);
		if (img != null) {
			return new ImageIcon(img);
		}
		return null;
	}
	/**
	 * Add a text change event handler for the given textfield.
	 * @param f the field
	 * @param action the action
	 */
	public void addTextChanged(final JTextComponent f, final Action1<? super JTextComponent> action) {
		f.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				action.invoke(f);
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				action.invoke(f);
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				action.invoke(f);
			}
		});
	}
}
