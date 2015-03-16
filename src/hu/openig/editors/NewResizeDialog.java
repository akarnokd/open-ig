/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors;

import hu.openig.model.Labels;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

/**
 * @author akarnokd
 *
 */
public class NewResizeDialog extends JDialog {
	/** */
	private static final long serialVersionUID = 499611298316001879L;
	/** The width. */
	public int width;
	/** The height. */
	public int height;
	/** Was OK pressed? */
	public boolean success;
	/** Width value. */
	JSpinner widthText;
	/** Height value. */
	JSpinner heightText;
	/**
	 * Construct the GUI.
	 * @param labels the UI labels
	 * @param isNew is the dialog the New Map?
	 */
	public NewResizeDialog(Labels labels, boolean isNew) {
		setTitle(labels.get(isNew ? "mapeditor.new_map" : "mapeditor.resize_map"));
		setModal(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		Container c = getContentPane();
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		JLabel widthLbl = new JLabel(labels.get("mapeditor.new_width"));
		JLabel heightLbl = new JLabel(labels.get("mapeditor.new_height"));
		JButton ok = new JButton(labels.get("mapeditor.ok"));
		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doOk();
			}
		});
		JButton cancel = new JButton(labels.get("mapeditor.cancel"));
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doCancel();
			}
		});
		
		widthText = new JSpinner(new SpinnerNumberModel(33, 1, 255, 1));
		heightText = new JSpinner(new SpinnerNumberModel(66, 1, 255, 1));
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addGroup(
				gl.createSequentialGroup()
				.addGroup(
					gl.createParallelGroup()
					.addComponent(widthLbl)
					.addComponent(heightLbl)
				)
				.addGroup(
					gl.createParallelGroup()
					.addComponent(widthText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(heightText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(ok)
				.addComponent(cancel)
			)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(widthLbl)
				.addComponent(widthText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(heightLbl)
				.addComponent(heightText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(ok)
				.addComponent(cancel)
			)
		);
		gl.linkSize(SwingConstants.HORIZONTAL, ok, cancel);
		pack();
	}
	/**
	 * Cancel the dialog.
	 */
	protected void doCancel() {
		success = false;
		dispose();
	}
	/**
	 * Approve the dialog.
	 */
	protected void doOk() {
		success = true;
		width = (Integer)widthText.getValue();
		height = (Integer)heightText.getValue();
		dispose();
	}
}
