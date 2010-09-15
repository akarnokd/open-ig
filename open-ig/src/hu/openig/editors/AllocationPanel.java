/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors;

import hu.openig.core.Act;
import hu.openig.model.Building;

import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.GroupLayout.Alignment;

/**
 * The panel to define the worker/energy allocation strategy and compute the results.
 * @author karnokd
 */
public class AllocationPanel extends JPanel {
	/** */
	private static final long serialVersionUID = -1202967327575505939L;
	/** The list of all buildings. */
	public List<Building> buildings;
	/** The available worker count. */
	public JTextField availableWorkers;
	/** Compute and apply the allocation values. */
	public JButton apply;
	/** The allocation strategies. */
	public JComboBox strategies;
	/** Refresh the requirements from the model. */
	public JButton refresh;
	/** Total worker demand. */
	JTextField workerDemand;
	/** Total energy demand. */
	JTextField energyDemand;
	/** Available energy. */
	JTextField availableEnergy;
	/** Operational count. */
	JTextField operationCount;
	/** Total efficiency. */
	JTextField totalEfficiency;
	/** 
	 * Creates the GUI. 
	 */
	public AllocationPanel() {
		GroupLayout gl = new GroupLayout(this);
		setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		availableWorkers = new JTextField(6);
		availableWorkers.setText("5000");
		availableWorkers.setHorizontalAlignment(JTextField.RIGHT);
		
		availableEnergy = new JTextField(6);
		availableEnergy.setEditable(false);
		availableEnergy.setHorizontalAlignment(JTextField.RIGHT);
		
		apply = new JButton("Apply");
		refresh = new JButton("Refresh");
		strategies = new JComboBox();
		
		workerDemand = new JTextField(6);
		workerDemand.setEditable(false);
		workerDemand.setHorizontalAlignment(JTextField.RIGHT);
		energyDemand = new JTextField(6);
		energyDemand.setEditable(false);
		energyDemand.setHorizontalAlignment(JTextField.RIGHT);
		
		JLabel availableWorkersLbl = new JLabel("Available workers:");
		JLabel availableEnergyLbl = new JLabel("Produced energy:");
		JLabel strategiesLbl = new JLabel("Strategies:");
		JLabel energyDemandLbl = new JLabel("Energy demand:");
		JLabel workerDemandLbl = new JLabel("Worker demand:");
		
		
		strategies.addItem("Off (set everyone to zero)");
		strategies.addItem("Uniform (original game strategy)");
		
		JLabel operationCountLbl = new JLabel("Operational count:");
		JLabel totalEfficiencyLbl = new JLabel("Total efficiency:");
		operationCount = new JTextField(6);
		operationCount.setEditable(false);
		operationCount.setHorizontalAlignment(JTextField.RIGHT);
		totalEfficiency = new JTextField(6);
		totalEfficiency.setEditable(false);
		totalEfficiency.setHorizontalAlignment(JTextField.RIGHT);
		
		apply.addActionListener(new Act() {
			@Override
			public void act() {
				doCompute();
			}
		});
		refresh.addActionListener(new Act() {
			@Override
			public void act() {
				doRefresh();
			}
		});
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addGroup(
				gl.createParallelGroup()
				.addGroup(
					gl.createSequentialGroup()
					.addGroup(
						gl.createParallelGroup()
						.addComponent(availableWorkersLbl)
						.addComponent(availableEnergyLbl)
						.addComponent(operationCountLbl)
					)
					.addGroup(
						gl.createParallelGroup()
						.addComponent(availableWorkers, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(availableEnergy, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(operationCount, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
					.addGroup(
						gl.createParallelGroup()
						.addComponent(workerDemandLbl)
						.addComponent(energyDemandLbl)
						.addComponent(totalEfficiencyLbl)
					)
					.addGroup(
						gl.createParallelGroup()
						.addComponent(workerDemand, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(energyDemand, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(totalEfficiency, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
				)
				.addGroup(
					gl.createSequentialGroup()
					.addComponent(strategiesLbl)
					.addComponent(strategies, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				)
			)
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
				.addComponent(availableWorkersLbl)
				.addComponent(availableWorkers)
				.addComponent(workerDemandLbl)
				.addComponent(workerDemand)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(availableEnergyLbl)
				.addComponent(availableEnergy)
				.addComponent(energyDemandLbl)
				.addComponent(energyDemand)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(operationCountLbl)
				.addComponent(operationCount)
				.addComponent(totalEfficiencyLbl)
				.addComponent(totalEfficiency)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(strategiesLbl)
				.addComponent(strategies)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(apply)
				.addComponent(refresh)
			)
		);
		
		gl.linkSize(SwingConstants.HORIZONTAL, availableWorkersLbl, availableEnergyLbl, strategiesLbl);
		gl.linkSize(SwingConstants.HORIZONTAL, apply, refresh);
		
	}
	/**
	 *  Refresh the total values.
	 */
	void doRefresh() {
		int totalWorkerDemand = 0;
		int totalEnergyDemand = 0;
		int producedEnergy = 0;
		int operationalBuildings = 0;
		float totalEfficiency = 0.0f;
		for (Building b : buildings) {
			if (b.isReady()) {
				totalWorkerDemand += b.getWorkers();
				int e = b.getEnergy();
				if (e < 0) {
					totalEnergyDemand += e;
				} else {
					producedEnergy += b.getEnergy() * b.getEfficiency();
				}
				if (b.getEfficiency() >= 0.5f) {
					operationalBuildings++;
				}
				totalEfficiency += b.getEfficiency();
			}
		}
		workerDemand.setText("" + totalWorkerDemand);
		energyDemand.setText("" + totalEnergyDemand);
		availableEnergy.setText("" + producedEnergy);
		operationCount.setText("" + operationalBuildings);
		this.totalEfficiency.setText(String.format("%.3f", totalEfficiency));
	}
	/**
	 * Compute the allocation based on the current strategy.
	 */
	void doCompute() {
		doRefresh();
		
		int availableWorker = Integer.parseInt(availableWorkers.getText());
		int workerDemand = Integer.parseInt(this.workerDemand.getText());
		int energyDemand = Integer.parseInt(this.energyDemand.getText());
		
		switch (strategies.getSelectedIndex()) {
		case 0:
			doZeroStrategy();
			break;
		case 1:
			doUniformStrategy(-availableWorker, workerDemand, energyDemand);
			break;
		default:
		}
		
		doRefresh();
	}
	/** Zero all assignments. */
	void doZeroStrategy() {
		for (Building b : buildings) {
			if (b.isReady()) {
				b.assignedEnergy = 0;
				b.assignedWorker = 0;
			}
		}
	}
	/** 
	 * Do an uniform distribution strategy. 
	 * @param availableWorker The available worker amount
	 * @param demandWorker the total worker demand 
	 * @param demandEnergy the energy demand
	 */
	void doUniformStrategy(int availableWorker, int demandWorker, int demandEnergy) {
		// no need to assign workers
		if (demandWorker == 0) {
			return;
		}
		float targetEfficiency = Math.min(1.0f, availableWorker / (float)demandWorker);
		float availableEnergy = 0;
		for (Building b : buildings) {
			if (b.isReady()) {
				int toAssign = Math.round(b.getWorkers() * targetEfficiency);
				b.assignedWorker = toAssign > availableWorker ? toAssign : availableWorker;
				availableWorker -= b.assignedWorker;
				int e = b.getEnergy();
				if (e > 0) {
					availableEnergy -= e * b.getEfficiency();
				}
			}
		}
		// no need assign energy: everything is either colonyhub or power plant
		if (demandEnergy == 0) {
			return;
		}
		targetEfficiency = Math.min(1.0f, availableEnergy / (float)demandEnergy);
		
		for (Building b : buildings) {
			if (b.isReady()) {
				int e = b.getEnergy();
				if (e < 0) {
					int toAssign = (int)(b.getEnergy() * targetEfficiency);
					b.assignedEnergy = toAssign > availableEnergy ? toAssign : (int)availableEnergy;
					availableEnergy -= b.assignedEnergy;
				}		
			}
		}
	}
}
