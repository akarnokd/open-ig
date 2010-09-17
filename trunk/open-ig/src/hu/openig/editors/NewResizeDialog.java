/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors;

import hu.openig.core.Act;
import hu.openig.core.Labels;

import java.awt.Container;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.GroupLayout.Alignment;

/**
 * @author karnokd
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
	JTextField widthText;
	/** Height value. */
	JTextField heightText;
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
		ok.addActionListener(new Act() {
			@Override
			public void act() {
				doOk();
			}
		});
		JButton cancel = new JButton(labels.get("mapeditor.cancel"));
		cancel.addActionListener(new Act() {
			@Override
			public void act() {
				doCancel();
			}
		});
		
		widthText = new JTextField(6);
		widthText.setText("33");
		widthText.setHorizontalAlignment(JTextField.RIGHT);
		heightText = new JTextField(6);
		heightText.setText("66");
		heightText.setHorizontalAlignment(JTextField.RIGHT);
		
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
		width = Integer.parseInt(widthText.getText());
		height = Integer.parseInt(heightText.getText());
		dispose();
	}
}
