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
import java.io.File;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * The map save dialog.
 * @author akarnokd
 *
 */
public class MapOpenSaveDialog extends JDialog {
	/** */
	private static final long serialVersionUID = -1815299252700684422L;
	/** The map save settings. */
	public MapSaveSettings saveSettings;
	/** Save mode? */
	private final boolean save;
	/** The chosen filename. */
	private JTextField fileName;
	/** Surface included? */
	private JCheckBox surface;
	/** Building included. */
	private JCheckBox buildings;
	/** 
	 * Create GUI. 
	 * @param save display a save dialog? 
	 * @param labels the UI labels
	 * @param initialSettings the optional initial settings
	 */
	public MapOpenSaveDialog(boolean save, Labels labels, MapSaveSettings initialSettings) {
		this.save = save;
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setModal(true);
		setTitle(labels.get(save ? "mapeditor.save_map" : "mapeditor.load_map"));
		Container c = getContentPane();
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		JLabel fileNameLbl = new JLabel(labels.get("mapeditor.filename"));
		fileName = new JTextField(40);
		surface = new JCheckBox(labels.get(save ? "mapeditor.save_surface_features" : "mapeditor.load_surface_features"), true);
		buildings = new JCheckBox(labels.get(save ? "mapeditor.save_buildings" : "mapeditor.load_buildings"), true);
		JButton browse = new JButton(labels.get("mapeditor.browse"));
		browse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doBrowse();
			}
		});
		
		if (initialSettings != null) {
			fileName.setText(initialSettings.fileName.getAbsolutePath());
			surface.setSelected(initialSettings.surface);
			buildings.setSelected(initialSettings.buildings);
		}
		
		JButton ok = new JButton(labels.get(save ? "mapeditor.save_map_save" : "mapeditor.open_map_open"));
		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveSettings = new MapSaveSettings();
				saveSettings.fileName = new File(fileName.getText());
				saveSettings.surface = surface.isSelected();
				saveSettings.buildings = buildings.isSelected();
				dispose();
			}
		});
		JButton cancel = new JButton(labels.get("mapeditor.cancel"));
		cancel.addActionListener(new ActionListener() { 
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addGroup(
				gl.createParallelGroup()
				.addGroup(
					gl.createSequentialGroup()
					.addComponent(fileNameLbl)
					.addComponent(fileName)
					.addComponent(browse)
				)
				.addComponent(surface)
				.addComponent(buildings)
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
				.addComponent(fileNameLbl)
				.addComponent(fileName)
				.addComponent(browse)
			)
			.addComponent(surface)
			.addComponent(buildings)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(ok)
				.addComponent(cancel)
			)
		);
		gl.linkSize(SwingConstants.HORIZONTAL, ok, cancel);
		
		pack();
	}
	/** Browse for file. */
	void doBrowse() {
		String fn = fileName.getText();
		File path = null;
		if (fn.length() > 0) {
			path = new File(fn).getParentFile();
		}
		if (path == null) {
			path = new File(".");
		}
		JFileChooser fc = new JFileChooser(path);
		int result;
		if (save) {
			result = fc.showSaveDialog(this);
		} else {
			result = fc.showOpenDialog(this);
		}
		if (result == JFileChooser.APPROVE_OPTION) {
			fileName.setText(fc.getSelectedFile().getAbsolutePath());
		}
	}
}
