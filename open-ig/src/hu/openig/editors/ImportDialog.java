/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.editors;

import hu.openig.core.Location;
import hu.openig.model.Labels;
import hu.openig.model.OriginalBuilding;
import hu.openig.model.OriginalPlanet;
import hu.openig.model.ResourceLocator;
import hu.openig.utils.XElement;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * The import dialog to load the original surface maps or concrete planet layout.
 * @author akarnokd
 */
public class ImportDialog extends JDialog {
	/** */
	private static final long serialVersionUID = -2498784504472296902L;
	/** The dialog was accepted. */
	public boolean success;
	/** The selected map. */
	public MapType selected;
	/** The X shift. */
	public int shiftXValue;
	/** The Y shift. */
	public int shiftYValue;
	/** Replace the current surface? */
	public boolean replaceSurface;
	/** Replace the current buildings? */
	public boolean replaceBuildings;
	/** Replace the concrete planet surface? */
	public boolean withSurface;
	/** The selected planet id. */
	public transient OriginalPlanet planet;
	/** The resource locator. */
	transient ResourceLocator rl;
	/** The orignal map's definition. */
	public static class MapType {
		/** The name only. */
		public String name;
		/** The surface name id. */
		public String surfaceType;
		/** The full path to the resource. */
		public String fullPath;
		/**
		 * Constructor.
		 * @param name the name
		 * @param surfaceType the surface type
		 * @param fullPath the full path
		 */
		public MapType(String name, String surfaceType, String fullPath) {
			this.name = name;
			this.surfaceType = surfaceType;
			this.fullPath = fullPath;
		}
	}
	/** The reference for the original map definitions. */
	final List<MapType> originalMaps = new ArrayList<>();
	/** The list of original planet ids. */
	final List<OriginalPlanet> originalPlanets = new ArrayList<>();
	/** The original map to select. */
	JComboBox<String> cbOriginalMap;
	/** The number of coordinates to shift the original map. */
	JTextField edShiftX;
	/** The number of coordinates to shift the original map. */
	JTextField edShiftY;
	/** The OK button. */
	JButton btnOk;
	/** The cancel button. */
	JButton btnCancel;
	/** Replace current surface? */
	JCheckBox cbReplaceSurface;
	/** Replace current buildings? */
	JCheckBox cbReplaceBuildings;
	/** The settings for the original planet definitions. */
	JComboBox<String> cbOriginalPlanets;
	/** Update the surface settings along with the building settings. */
	JCheckBox cbWithSurface;
	/** Original map label. */
	private JLabel originalMapLabel;
	/** Shift X label. */
	private JLabel shiftXLabel;
	/** Shift Y label. */
	private JLabel shiftYLabel;
	/** Original planets label. */
	private JLabel originalPlanetsLabel;
	/**
	 * Create the GUI.
	 * @param rl the resource locator.
	 */
	public ImportDialog(ResourceLocator rl) {
		this.rl = rl;
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle("Import a map or planet settings");
		setModal(true);
		
		buildMap(9, 'a', "desert");
		buildMap(9, 'b', "frozen");
		buildMap(9, 'c', "cratered");
		buildMap(8, 'd', "rocky");
		buildMap(8, 'e', "liquid");
		buildMap(8, 'f', "earth");
		buildMap(6, 'g', "neptoplasm");
		
		cbOriginalMap = new JComboBox<>();
		cbOriginalMap.addItem("-Don't import any-");
		for (MapType mt : originalMaps) {
			cbOriginalMap.addItem(mt.surfaceType + " " + mt.name);
		}
		
		Container c = getContentPane();
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		originalMapLabel = new JLabel("Original map:");
		shiftXLabel = new JLabel("Shift X:");
		shiftYLabel = new JLabel("Shift Y:");
		originalPlanetsLabel = new JLabel("Original planet:");
		
		edShiftX = new JTextField("-1");
		edShiftY = new JTextField("-1");
		
		cbReplaceSurface = new JCheckBox("Replace current surface", true);
		cbReplaceBuildings = new JCheckBox("Replace current buildings", true);
		
		btnOk = new JButton("Import");
		btnOk.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doOk(); } });
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doCancel(); } });
		
		cbOriginalPlanets = new JComboBox<>();
		cbOriginalPlanets.addItem("-Don't import any-");
		
		parseOriginalPlanet();
		
		for (OriginalPlanet op : originalPlanets) {
			cbOriginalPlanets.addItem(op.name + " [" + op.surfaceType + " (" + op.surfaceVariant + "): " + (op.race != null ? op.race : "-") + "]");
		}
		// sort by surface and variant then add it to the list again
		List<OriginalPlanet> ops2 = new ArrayList<>(originalPlanets);
		Collections.sort(ops2, OriginalPlanet.DEFAULT_ORDER);
		for (OriginalPlanet op : ops2) {
			cbOriginalPlanets.addItem(op.name + " [" + op.surfaceType + " (" + op.surfaceVariant + "): " + (op.race != null ? op.race : "-") + "]");
		}
		originalPlanets.addAll(ops2);
		
		cbWithSurface = new JCheckBox("Replace surface along with the buildings", true);
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(originalMapLabel)
				.addComponent(cbOriginalMap)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(originalPlanetsLabel)
				.addComponent(cbOriginalPlanets)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(shiftXLabel)
				.addComponent(edShiftX)
				.addComponent(shiftYLabel)
				.addComponent(edShiftY)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.LEADING)
				.addComponent(cbReplaceSurface)
				.addComponent(cbReplaceBuildings)
				.addComponent(cbWithSurface)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(btnOk, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(btnCancel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(originalMapLabel)
				.addComponent(cbOriginalMap)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(originalPlanetsLabel)
				.addComponent(cbOriginalPlanets)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(shiftXLabel)
				.addComponent(edShiftX)
				.addComponent(shiftYLabel)
				.addComponent(edShiftY)
			)
			.addComponent(cbReplaceSurface)
			.addComponent(cbReplaceBuildings)
			.addComponent(cbWithSurface)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(btnOk)
				.addComponent(btnCancel)
			)
		);
		gl.linkSize(SwingConstants.HORIZONTAL, btnOk, btnCancel);
		pack();
		setResizable(false);
	}
	/**
	 * Build the maps from parts.
	 * @param count number of alternatives
	 * @param type the type character
	 * @param typeName the modern type name
	 */
	private void buildMap(int count, char type, String typeName) {
		for (int i = 1; i <= count; i++) {
			originalMaps.add(new MapType("map_" + type + i, typeName, "colony/map_" + type + i));
		}
	}
	/** OK button clicked. */
	void doOk() {
		success = true;
		if (cbOriginalMap.getSelectedIndex() > 0) {
			selected = originalMaps.get(cbOriginalMap.getSelectedIndex() - 1);
		} else {
			selected = null;
		}
		shiftXValue = Integer.parseInt(edShiftX.getText());
		shiftYValue = Integer.parseInt(edShiftY.getText());
		if (cbOriginalPlanets.getSelectedIndex() > 0) {
			planet = originalPlanets.get(cbOriginalPlanets.getSelectedIndex() - 1);
		} else {
			planet = null;
		}
		replaceSurface = cbReplaceSurface.isSelected();
		replaceBuildings = cbReplaceBuildings.isSelected();
		withSurface = cbWithSurface.isSelected();
		dispose();
	}
	/** Cancel button clicked. */
	void doCancel() {
		success = false;
		dispose();
	}
	/** Parse the original planet definitions. */
	void parseOriginalPlanet() {
		XElement e = rl.getXML("campaign/main/planets_old");
		for (XElement planet : e.childrenWithName("planet")) {
			OriginalPlanet op = new OriginalPlanet();
			op.name = planet.get("id");
			op.surfaceType = planet.childValue("type");
			op.location.x = Integer.parseInt(planet.childValue("location-x"));
			op.location.y = Integer.parseInt(planet.childValue("location-y"));
			op.surfaceVariant = Integer.parseInt(planet.childValue("variant"));
			op.race = planet.childValue("race");
			for (XElement building : planet.childElement("buildings").childrenWithName("building")) {
				OriginalBuilding ob = new OriginalBuilding();
				ob.name = building.childValue("id");
				ob.location = Location.of(Integer.parseInt(building.childValue("x")), 
						Integer.parseInt(building.childValue("y")));
				op.buildings.add(ob);
			}
			originalPlanets.add(op);
		}
	}
	/**
	 * Set the labels of the UI elements.
	 * @param labels the current language labels
	 */
	public void setLabels(Labels labels) {
		setTitle(labels.get("mapeditor.import_title"));
		btnOk.setText(labels.get("mapeditor.import_import"));
		btnCancel.setText(labels.get("mapeditor.cancel"));
		cbReplaceSurface.setText(labels.get("mapeditor.import_replace_surface"));
		cbReplaceBuildings.setText(labels.get("mapeditor.import_replace_buildings"));
		cbWithSurface.setText(labels.get("mapeditor.import_replace_both"));
		originalMapLabel.setText(labels.get("mapeditor.import_original_map"));
		shiftXLabel.setText(labels.get("mapeditor.import_shift_x"));
		shiftYLabel.setText(labels.get("mapeditor.import_shift_y"));
		originalPlanetsLabel.setText(labels.get("mapeditor.import_original_planet"));
		int idx = cbOriginalMap.getSelectedIndex();
		cbOriginalMap.removeItemAt(0);
		cbOriginalMap.insertItemAt(labels.get("mapeditor.import_nothing"), 0);
		cbOriginalMap.setSelectedIndex(idx);
		
		idx = cbOriginalPlanets.getSelectedIndex();
		cbOriginalPlanets.removeItemAt(0);
		cbOriginalPlanets.insertItemAt(labels.get("mapeditor.import_nothing"), 0);
		cbOriginalPlanets.setSelectedIndex(idx);
	}
}
