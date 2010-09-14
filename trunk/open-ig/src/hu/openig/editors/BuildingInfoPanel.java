/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.editors;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.GroupLayout.Alignment;

/**
 * The building information panel used in the map editor.
 * @author karnokd
 */
public class BuildingInfoPanel extends JPanel {
	/**	 */
	private static final long serialVersionUID = -2469177284141429466L;
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
	JCheckBox buildingEnabled;
	/** Building is repairing. */
	JCheckBox buildingRepairing;
	/**
	 * Constructs the panel's layout.
	 */
	public BuildingInfoPanel() {
		GroupLayout gl = new GroupLayout(this);
		setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		JLabel buildingNameLbl = new JLabel("Name:");
		buildingName = new JTextField();
		buildingName.setEditable(false);
		
		JLabel completedLbl = new JLabel("Completed:");
		JLabel hitpointsLbl = new JLabel("Hitpoints:");
		
		completed = new JTextField(6);
		completed.setHorizontalAlignment(JTextField.RIGHT);
		hitpoints = new JTextField(6);
		hitpoints.setHorizontalAlignment(JTextField.RIGHT);

		JLabel workerLbl = new JLabel("Workers:");
		JLabel energyLbl = new JLabel("Energy:");
		
		assignedWorkers = new JTextField(6);
		assignedWorkers.setHorizontalAlignment(JTextField.RIGHT);
		assignedEnergy = new JTextField(6);
		assignedEnergy.setHorizontalAlignment(JTextField.RIGHT);
	
		JLabel completedOfLbl = new JLabel(" of ");
		JLabel hitpointsOfLbl = new JLabel(" of ");
		
		completedTotal = new JTextField();
		completedTotal.setEditable(false);
		completedTotal.setHorizontalAlignment(JTextField.RIGHT);
		hitpointsTotal = new JTextField();
		hitpointsTotal.setEditable(false);
		hitpointsTotal.setHorizontalAlignment(JTextField.RIGHT);
		
		JLabel workerOfLbl = new JLabel(" of ");
		JLabel energyOfLbl = new JLabel(" of ");

		workerTotal = new JTextField();
		workerTotal.setEditable(false);
		workerTotal.setHorizontalAlignment(JTextField.RIGHT);
		energyTotal = new JTextField();
		energyTotal.setEditable(false);
		energyTotal.setHorizontalAlignment(JTextField.RIGHT);
		
		JLabel efficiencyLbl = new JLabel("Efficiency:");
		efficiency = new JTextField(6);
		efficiency.setEditable(false);
		efficiency.setHorizontalAlignment(JTextField.RIGHT);
		
		JLabel techLbl = new JLabel("Technology:");
		JLabel costLbl = new JLabel("Cost:");
		
		tech = new JTextField(15);
		tech.setEditable(false);
		cost = new JTextField(6);
		cost.setEditable(false);
		cost.setHorizontalAlignment(JTextField.RIGHT);
		
		JLabel locationLbl = new JLabel("Location:");
		
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
					)
					.addGroup(
						gl.createSequentialGroup()
						.addComponent(hitpoints)
						.addComponent(hitpointsOfLbl)
						.addComponent(hitpointsTotal)
					)
					.addGroup(
						gl.createSequentialGroup()
						.addComponent(assignedWorkers)
						.addComponent(workerOfLbl)
						.addComponent(workerTotal)
					)
					.addGroup(
						gl.createSequentialGroup()
						.addComponent(assignedEnergy)
						.addComponent(energyOfLbl)
						.addComponent(energyTotal)
					)
					.addComponent(efficiency, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(buildingEnabled)
				.addComponent(buildingRepairing)
			)
			.addComponent(apply)
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
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(hitpointsLbl)
				.addComponent(hitpoints, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(hitpointsOfLbl)
				.addComponent(hitpointsTotal, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(workerLbl)
				.addComponent(assignedWorkers, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(workerOfLbl)
				.addComponent(workerTotal, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(energyLbl)
				.addComponent(assignedEnergy, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(energyOfLbl)
				.addComponent(energyTotal, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(efficiencyLbl)
				.addComponent(efficiency, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addComponent(apply)
		);
		
		gl.linkSize(SwingConstants.HORIZONTAL, completed, completedTotal, hitpoints, hitpointsTotal, assignedWorkers, workerTotal, assignedEnergy, energyTotal);

	}
}
