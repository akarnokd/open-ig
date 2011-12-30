/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.core.Configuration;
import hu.openig.core.Labels;
import hu.openig.ui.IGButton;

import java.awt.Container;
import java.awt.Font;

import javax.swing.GroupLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * The other settings dialog.
 * @author akarnokd, 2011.12.30.
 */
public class OtherSettingsDialog extends JDialog {
	/** */
	private static final long serialVersionUID = 5092881498785688796L;
	/** Labels. */
	final Labels labels;
	/** Configuration. */
	final Configuration config;
	/**
	 * Creates the dialog. 
	 * @param owner the owner of the frame
	 * @param labels the labels
	 * @param config the configuration
	 */
	public OtherSettingsDialog(JFrame owner, Labels labels, Configuration config) {
		super(owner);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.labels = labels;
		this.config = config;
		setModal(true);
		setTitle(labels.get("othersettings.title"));

		setFont(new Font(Font.DIALOG, Font.PLAIN, 16));

		IGButton ok = new IGButton();
		ok.setText(labels.get("othersettings.ok"));

		IGButton cancel = new IGButton();
		cancel.setText(labels.get("othersettings.cancel"));

		Container c = getContentPane();
		
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		
		
		pack();
		setResizable(false);
	}

}
