/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.editors;

import java.awt.Container;

import javax.swing.GroupLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.GroupLayout.Alignment;

/** A window with indeterminate backgound progress indicator. */
public class BackgroundProgress extends JDialog {
	/** */
	private static final long serialVersionUID = -5795494140780969300L;
	/** The text label. */
	JLabel label;
	/** The progress indicator. */
	JProgressBar progress;
	/** Build the dialog. */
	public BackgroundProgress() {
		setTitle("Work in background");
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setModal(true);
		
		label = new JLabel("Working...");
		progress = new JProgressBar();
		progress.setIndeterminate(true);
		
		Container c = getContentPane();
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		gl.setHorizontalGroup(gl.createParallelGroup(Alignment.CENTER)
				.addComponent(label).addComponent(progress));
		gl.setVerticalGroup(gl.createSequentialGroup().addComponent(label).addComponent(progress));
		
		pack();
		setResizable(false);
	}
	/**
	 * Set the label text.
	 * @param text the text
	 */
	public void setLabelText(String text) {
		label.setText(text);
	}
}
