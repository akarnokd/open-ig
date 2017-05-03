/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.editors;

import hu.openig.core.Action0;

import java.awt.Container;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

/** A window with indeterminate background progress indicator. */
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
	/**
	 * Run a background process and show a dialog during this operation.
	 * @param title the title
	 * @param label the label
	 * @param activity the background activity
	 * @param onComplete the activity once the background finished
	 */
	public static void run(String title, String label,
			final Action0 activity, final Action0 onComplete) {
		final BackgroundProgress bgp = new BackgroundProgress();
		bgp.setTitle(title);
		bgp.setLabelText(label);
		bgp.pack();
		bgp.setLocationRelativeTo(null);
		SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				activity.invoke();
				return null;
			}
			@Override
			protected void done() {
				bgp.dispose();
				if (onComplete != null) {
					onComplete.invoke();
				}
			}
		};
		sw.execute();
		bgp.setVisible(true);
	}
}
