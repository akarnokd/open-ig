/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import hu.openig.core.Action1;
import hu.openig.utils.XElement;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.text.JTextComponent;

/**
 * Represents a secondary panel which depends on a primary panel.
 * @author akarnokd, 2012.11.03.
 */
public abstract class CESlavePanel extends CEBasePanel {
	/** */
	private static final long serialVersionUID = -2273389174543868260L;
	/** The current master. */
	XElement master;
	/** The callback when a validation has changed some fields. */
	public Action1<ImageIcon> onValidate;
	/**
	 * Constructor. Initializes the context.
	 * @param context the context.
	 */
	public CESlavePanel(CEContext context) {
		super(context);
	}
	/**
	 * Set the current master item.
	 * @param item the item
	 */
	public void setMaster(XElement item) {
		master = item;
		onMasterChanged();
	}
	/**
	 * Call the onValidate callback if set.
	 * @param icon the new validity indicator icon
	 */
	public void onValidate(ImageIcon icon) {
		if (onValidate != null) {
			onValidate.invoke(icon);
		}
	}
	/** Called when the master object has changed. */
	public abstract void onMasterChanged();
	/**
	 * Validate a field that contains a label reference and retrieve the label.
	 * @param attr the attribute
	 * @param field the field
	 * @param label the label
	 */
	void validateLabelRef(String attr, CEValueBox<? extends JTextComponent> field, CEValueBox<? extends JTextComponent> label) {
		String s = field.component.getText();
		master.set(attr, s);
		if (master.isNullOrEmpty(attr) || !context.dataManager().hasLabel(s)) {
			field.setInvalid(warningIcon, get("missing_label"));
		} else {
			field.clearInvalid();
		}
		setTextAndEnabled(label, null, context.dataManager().label(s), true);
	}
	/**
	 * Validate a choice field against the enum values.
	 * @param attr the attribute
	 * @param field the field
	 * @param enums the enums
	 */
	void validateChoice(String attr, CEValueBox<? extends JComboBox<String>> field, String... enums) {
		int idx = field.component.getSelectedIndex();
		if (idx < 0) {
			master.set(attr, null);
			field.setInvalid(errorIcon, get("invalid_option"));
		} else {
			master.set(attr, enums[idx]);
			field.clearInvalid();
		}
	}
	/**
	 * Validate a choice field agains the enumeration.
	 * @param attr the target attribute
	 * @param field the field
	 * @param enumClass the enum class
	 */
	void validateChoice(String attr, CEValueBox<? extends JComboBox<String>> field, Class<? extends Enum<?>> enumClass) {
		Object[] ecs = enumClass.getEnumConstants();
		String[] enums = new String[ecs.length];
		for (int i = 0; i < enums.length; i++) {
			enums[i] = ecs[i].toString();
		}
		validateChoice(attr, field, enums);
	}
}
