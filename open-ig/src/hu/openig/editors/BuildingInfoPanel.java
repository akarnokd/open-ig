/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.editors;

import hu.openig.model.Resource;

import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;

/**
 * The building information panel used in the map editor.
 * @author akarnokd
 */
public class BuildingInfoPanel extends JPanel {
	/**	 */
	private static final long serialVersionUID = -2469177284141429466L;
	/** The resource table model. */
	class ResourceTableModel extends AbstractTableModel {
		/**	 */
		private static final long serialVersionUID = 6093762358692787521L;
		/** The rows. */
		final List<Resource> rows = new ArrayList<>();
		/** Column names. */
		/** Column classes. */
		Class<?>[] colClasses = { String.class, Float.class };
		@Override
		public int getColumnCount() {
			return 2;
		}
		@Override
		public int getRowCount() {
			return rows.size();
		}
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Resource r = rows.get(rowIndex);
			switch (columnIndex) {
			case 0:
				return r.type;
			case 1:
				return r.amount;
			default:
			}
			return null;
		}
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return colClasses[columnIndex];
		}
		@Override
		public String getColumnName(int column) {
			return column == 0 ? resourceColumn : amountColumn;
		}
	}
	/** The building name. */
	JTextField buildingName;
	/** The completed amount. */
	JTextField completed;
	/** The hitpoints. */
	JTextField hitpoints;
	/** The assigned workers. */
	JTextField assignedWorkers;
	/** The assigned energy. */
	JTextField assignedEnergy;
	/** The current efficiency. */
	JTextField efficiency;
	/** The total completed amount. */
	JTextField completedTotal;
	/** The total hitpoint amount. */
	JTextField hitpointsTotal;
	/** The total worker demand. */
	JTextField workerTotal;
	/** The total energy demand. */
	JTextField energyTotal;
	/** Apply the settings onto the building. */
	@Rename(to = "mapeditor.apply")
	JButton apply;
	/** The technology name. */
	JTextField tech;
	/** The cost name. */
	JTextField cost;
	/** The current location. */
	JTextField locationX;
	/** The current location. */
	JTextField locationY;
	/** Building is enabled. */
	@Rename(to = "mapeditor.enabled")
	JCheckBox buildingEnabled;
	/** Building is repairing. */
	@Rename(to = "mapeditor.repairing")
	JCheckBox buildingRepairing;
	/** Upgrade list. */
	JComboBox<String> upgradeList;
	/** The other resources table. */
	JTable resourceTable;
	/** The completion percent. */
	JLabel completionPercent;
	/** The hitpoint percent. */
	JLabel hitpointPercent;
	/** The worker percent. */
	JLabel workerPercent;
	/** The energy percent. */
	JLabel energyPercent;
	/** Refresh the current values. */
	@Rename(to = "mapeditor.refresh")
	JButton refresh;
	/** Resource column label. */
	@Rename(to = "mapeditor.resource")
	String resourceColumn;
	/** Amount column label. */
	@Rename(to = "mapeditor.amount")
	String amountColumn;
	/** The resource table model. */
	ResourceTableModel resourceTableModel;
	/** Completed. */
	@Rename(to = "mapeditor.info_completed")
	JLabel completedLbl;
	/** Hitpoints. */
	@Rename(to = "mapeditor.info_hitpoints")
	JLabel hitpointsLbl;
	/** Worker. */
	@Rename(to = "mapeditor.info_worker")
	JLabel workerLbl;
	/** Energy. */
	@Rename(to = "mapeditor.info_energy")
	JLabel energyLbl;
	/** Building name. */
	@Rename(to = "mapeditor.info_building_name")
	JLabel buildingNameLbl;
	/** Completed of. */
	@Rename(to = "mapeditor.info_completed_of")
	JLabel completedOfLbl;
	/** Hitpoints of. */
	@Rename(to = "mapeditor.info_hitpoints_of")
	JLabel hitpointsOfLbl;
	/** Worker of. */
	@Rename(to = "mapeditor.info_worker_of")
	JLabel workerOfLbl;
	/** Energy of. */
	@Rename(to = "mapeditor.info_energy_of")
	JLabel energyOfLbl;
	/** Efficency. */
	@Rename(to = "mapeditor.info_efficiency")
	JLabel efficiencyLbl;
	/** Technology. */
	@Rename(to = "mapeditor.info_technology")
	JLabel techLbl;
	/** Cost. */
	@Rename(to = "mapeditor.info_cost")
	JLabel costLbl;
	/** Location. */
	@Rename(to = "mapeditor.info_location")
	JLabel locationLbl;
	/** Upgrade. */
	@Rename(to = "mapeditor.info_upgrade")
	JLabel upgradeLbl;
	/**
	 * Constructs the panel's layout.
	 */
	public BuildingInfoPanel() {
		GroupLayout gl = new GroupLayout(this);
		setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		buildingNameLbl = new JLabel("Name:");
		buildingName = new JTextField();
		buildingName.setEditable(false);
		
		completedLbl = new JLabel("Completed:");
		hitpointsLbl = new JLabel("Hitpoints:");
		
		completed = new JTextField(6);
		completed.setHorizontalAlignment(JTextField.RIGHT);
		hitpoints = new JTextField(6);
		hitpoints.setHorizontalAlignment(JTextField.RIGHT);

		workerLbl = new JLabel("Workers:");
		energyLbl = new JLabel("Energy:");
		
		assignedWorkers = new JTextField(6);
		assignedWorkers.setHorizontalAlignment(JTextField.RIGHT);
		assignedEnergy = new JTextField(6);
		assignedEnergy.setHorizontalAlignment(JTextField.RIGHT);
	
		completedOfLbl = new JLabel(" of ");
		hitpointsOfLbl = new JLabel(" of ");
		
		completedTotal = new JTextField();
		completedTotal.setEditable(false);
		completedTotal.setHorizontalAlignment(JTextField.RIGHT);
		hitpointsTotal = new JTextField();
		hitpointsTotal.setEditable(false);
		hitpointsTotal.setHorizontalAlignment(JTextField.RIGHT);
		
		workerOfLbl = new JLabel(" of ");
		energyOfLbl = new JLabel(" of ");

		workerTotal = new JTextField();
		workerTotal.setEditable(false);
		workerTotal.setHorizontalAlignment(JTextField.RIGHT);
		energyTotal = new JTextField();
		energyTotal.setEditable(false);
		energyTotal.setHorizontalAlignment(JTextField.RIGHT);
		
		efficiencyLbl = new JLabel("Efficiency:");
		efficiency = new JTextField(6);
		efficiency.setEditable(false);
		efficiency.setHorizontalAlignment(JTextField.RIGHT);
		
		techLbl = new JLabel("Technology:");
		costLbl = new JLabel("Cost:");
		
		tech = new JTextField(15);
		tech.setEditable(false);
		cost = new JTextField(6);
		cost.setEditable(false);
		cost.setHorizontalAlignment(JTextField.RIGHT);
		
		locationLbl = new JLabel("Location:");
		
		JLabel locationXLbl = new JLabel("X =");
		JLabel locationYLbl = new JLabel("Y =");
		
		locationX = new JTextField(3);
		locationX.setEditable(false);
		locationX.setHorizontalAlignment(JTextField.RIGHT);
		locationY = new JTextField(3);
		locationY.setEditable(false);
		locationY.setHorizontalAlignment(JTextField.RIGHT);
		
		apply = new JButton("Apply changes");
		
		buildingEnabled = new JCheckBox("Enabled");
		buildingRepairing = new JCheckBox("Repairing");
		
		upgradeLbl = new JLabel("Upgrades:");
		upgradeList = new JComboBox<>();
		upgradeList.addItem("None");
		
		resourceTableModel = new ResourceTableModel();
		resourceTable = new JTable(resourceTableModel);
		resourceTable.setAutoCreateRowSorter(true);
		JScrollPane sp = new JScrollPane(resourceTable);
			
		completionPercent = new JLabel();
		completionPercent.setHorizontalAlignment(JLabel.RIGHT);
		hitpointPercent = new JLabel();
		hitpointPercent.setHorizontalAlignment(JLabel.RIGHT);
		energyPercent = new JLabel();
		energyPercent.setHorizontalAlignment(JLabel.RIGHT);
		workerPercent = new JLabel();
		workerPercent.setHorizontalAlignment(JLabel.RIGHT);
		
		refresh = new JButton("Refresh");
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addGroup(
				gl.createSequentialGroup()
				.addGroup(
					gl.createParallelGroup()
					.addComponent(buildingNameLbl)
					.addComponent(techLbl)
					.addComponent(locationLbl)
					.addComponent(costLbl)
					.addComponent(completedLbl)
					.addComponent(hitpointsLbl)
					.addComponent(workerLbl)
					.addComponent(energyLbl)
					.addComponent(efficiencyLbl)
					.addComponent(upgradeLbl)
				)
				.addGroup(
					gl.createParallelGroup()
					.addComponent(buildingName)
					.addComponent(tech, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGroup(
						gl.createSequentialGroup()
						.addComponent(locationXLbl)
						.addComponent(locationX, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(locationYLbl)
						.addComponent(locationY, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
					.addComponent(cost, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGroup(
						gl.createSequentialGroup()
						.addComponent(completed, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(completedOfLbl)
						.addComponent(completedTotal)
						.addComponent(completionPercent)
					)
					.addGroup(
						gl.createSequentialGroup()
						.addComponent(hitpoints)
						.addComponent(hitpointsOfLbl)
						.addComponent(hitpointsTotal)
						.addComponent(hitpointPercent)
					)
					.addGroup(
						gl.createSequentialGroup()
						.addComponent(assignedWorkers)
						.addComponent(workerOfLbl)
						.addComponent(workerTotal)
						.addComponent(workerPercent)
					)
					.addGroup(
						gl.createSequentialGroup()
						.addComponent(assignedEnergy)
						.addComponent(energyOfLbl)
						.addComponent(energyTotal)
						.addComponent(energyPercent)
					)
					.addComponent(efficiency, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(upgradeList, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(buildingEnabled)
				.addComponent(buildingRepairing)
			)
			.addComponent(sp)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(apply)
				.addComponent(refresh)
			)
		);
		
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(buildingNameLbl)
				.addComponent(buildingName, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(techLbl)
				.addComponent(tech, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(locationLbl)
				.addComponent(locationXLbl)
				.addComponent(locationX, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(locationYLbl)
				.addComponent(locationY, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(buildingEnabled)
				.addComponent(buildingRepairing)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(costLbl)
				.addComponent(cost, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(completedLbl)
				.addComponent(completed, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(completedOfLbl)
				.addComponent(completedTotal, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(completionPercent)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(hitpointsLbl)
				.addComponent(hitpoints, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(hitpointsOfLbl)
				.addComponent(hitpointsTotal, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(hitpointPercent)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(workerLbl)
				.addComponent(assignedWorkers, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(workerOfLbl)
				.addComponent(workerTotal, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(workerPercent)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(energyLbl)
				.addComponent(assignedEnergy, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(energyOfLbl)
				.addComponent(energyTotal, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(energyPercent)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(efficiencyLbl)
				.addComponent(efficiency, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(upgradeLbl)
				.addComponent(upgradeList, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addComponent(sp, 0, 40, Short.MAX_VALUE)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(apply)
				.addComponent(refresh)
			)
		);
		
		gl.linkSize(SwingConstants.HORIZONTAL, completed, completedTotal, hitpoints, hitpointsTotal, assignedWorkers, workerTotal, assignedEnergy, energyTotal);
		gl.linkSize(SwingConstants.HORIZONTAL, completionPercent, hitpointPercent, workerPercent, energyPercent);

	}
}
