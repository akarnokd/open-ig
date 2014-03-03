/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools.ani;

import java.awt.Container;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

/**
 * The progress indicator dialog.
 * @author karnokd
 */
public class ProgressFrame extends JDialog implements ActionListener {
	/** Serial version. */
	private static final long serialVersionUID = -537904934073232256L;
	/** The progress bar. */
	private JProgressBar bar;
	/** The progress label. */
	private JLabel label;
	/** The cancel button. */
	private JButton cancel;
	/** The operation was cancelled. */
	private volatile boolean cancelled;
	/**
	 * Constructor. Sets the dialog's title. 
	 * @param title the title
	 * @param owner the owner
	 */
	public ProgressFrame(String title, Window owner) {
		super(owner, title);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setModalityType(ModalityType.APPLICATION_MODAL);
		bar = new JProgressBar();
		label = new JLabel();
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		Container c = getContentPane();
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addComponent(bar)
			.addComponent(label)
			.addComponent(cancel)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(bar)
			.addComponent(label)
			.addComponent(cancel)
		);
		setSize(350, 150);
		setLocationRelativeTo(owner);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		cancelled = true;
	}
	/**
	 * Set the progress bar's maximum value.
	 * @param max the maximum value
	 */
	public void setMax(int max) {
		bar.setMaximum(max);
	}
	/**
	 * Update the progress bar's current value.
	 * @param value the current value
	 * @param text the text to display in label
	 */
	public void setCurrent(int value, String text) {
		bar.setValue(value);
		label.setText(text);
	}
	/**
	 * Was the operation cancelled by the user?
	 * @return the cancellation status
	 */
	public boolean isCancelled() {
		return cancelled;
	}
}
