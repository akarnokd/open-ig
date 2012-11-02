/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import java.awt.Dimension;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.GroupLayout.Alignment;

/**
 * A value editing box with label, editor component and validity indicator.
 * @author akarnokd, 2012.11.02.
 *
 * @param <C> the editor component
 */
public class CEValueBox<C extends JComponent> extends JPanel {
	/** */
	private static final long serialVersionUID = 5000917311399588321L;
	/** The label. */
	public final JLabel label;
	/** The edit component. */
	public final C component;
	/** The validity indicator. */
	public final JLabel valid;
	/**
	 * Construct the box with the given label and editor component.
	 * @param displayText the exact text to display
	 * @param component the component
	 */
	public CEValueBox(String displayText, C component) {
		label = new JLabel(displayText);
		this.component = component;
		valid = new JLabel();
		valid.setPreferredSize(new Dimension(20, 20));
		
		GroupLayout gl = new GroupLayout(this);
		setLayout(gl);
		
		gl.setHorizontalGroup(
			gl.createSequentialGroup()
			.addComponent(label)
			.addGap(10)
			.addComponent(component)
			.addGap(5)
			.addComponent(valid, 20, 20, 20)
			.addGap(20)
		);
		gl.setVerticalGroup(
			gl.createParallelGroup(Alignment.BASELINE)
			.addComponent(label)
			.addComponent(component, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(valid, 20, 20, 20)
		);
	
		gl.setHonorsVisibility(valid, false);
	}
	/**
	 * Clear the validity indicator.
	 */
	public void clearInvalid() {
		valid.setIcon(null);
		valid.setToolTipText("");
	}
	/**
	 * Set an invalid state and indicate the issue.
	 * @param icon the icon
	 * @param explanation the explanation text
	 */
	public void setInvalid(ImageIcon icon, String explanation) {
		valid.setIcon(icon);
		valid.setToolTipText(explanation);
	}
	/**
	 * Creates a new instance with the given text and component.
	 * @param <E> the componentt type
	 * @param displayText the text
	 * @param component the component
	 * @return the value box
	 */
	public static <E extends JComponent> CEValueBox<E> of(String displayText, E component) {
		return new CEValueBox<E>(displayText, component);
	}
}
