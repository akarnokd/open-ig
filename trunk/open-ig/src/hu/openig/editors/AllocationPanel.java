/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors;

import hu.openig.mechanics.Allocator;
import hu.openig.model.Building;
import hu.openig.model.ResourceAllocationStrategy;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * The panel to define the worker/energy allocation strategy and compute the results.
 * @author akarnokd
 */
public class AllocationPanel extends JPanel {
	/** */
	private static final long serialVersionUID = -1202967327575505939L;
	/** The list of all buildings. */
	public final List<Building> buildings = new ArrayList<>();
	/** The available worker count. */
	public JTextField availableWorkers;
	/** Compute and apply the allocation values. */
	@Rename(to = "mapeditor.apply")
	public JButton apply;
	/** The allocation strategies. */
	public JComboBox<ResourceAllocationStrategy> strategies;
	/** Refresh the requirements from the model. */
	@Rename(to = "mapeditor.refresh")
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
	/** Available workers. */
	@Rename(to = "mapeditor.allocation_available_workers")
	JLabel availableWorkersLbl;
	/** Available energy. */
	@Rename(to = "mapeditor.allocation_available_energy")
	JLabel availableEnergyLbl;
	/** Strategies. */
	@Rename(to = "mapeditor.allocation_strategies")
	JLabel strategiesLbl;
	/** Energy demand. */
	@Rename(to = "mapeditor.allocation_energy_demand")
	JLabel energyDemandLbl;
	/** Worker demand. */
	@Rename(to = "mapeditor.allocation_worker_demand")
	JLabel workerDemandLbl;
	/** Operation count. */
	@Rename(to = "mapeditor.allocation_operation_count")
	JLabel operationCountLbl;
	/** Total efficiency. */
	@Rename(to = "mapeditor.allocation_total_efficiency")
	JLabel totalEfficiencyLbl;
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
		strategies = new JComboBox<>(ResourceAllocationStrategy.values());
		
		workerDemand = new JTextField(6);
		workerDemand.setEditable(false);
		workerDemand.setHorizontalAlignment(JTextField.RIGHT);
		energyDemand = new JTextField(6);
		energyDemand.setEditable(false);
		energyDemand.setHorizontalAlignment(JTextField.RIGHT);
		
		availableWorkersLbl = new JLabel("Available workers:");
		availableEnergyLbl = new JLabel("Produced energy:");
		strategiesLbl = new JLabel("Strategies:");
		energyDemandLbl = new JLabel("Energy demand:");
		workerDemandLbl = new JLabel("Worker demand:");
		
		operationCountLbl = new JLabel("Operational count:");
		totalEfficiencyLbl = new JLabel("Total efficiency:");
		operationCount = new JTextField(6);
		operationCount.setEditable(false);
		operationCount.setHorizontalAlignment(JTextField.RIGHT);
		totalEfficiency = new JTextField(6);
		totalEfficiency.setEditable(false);
		totalEfficiency.setHorizontalAlignment(JTextField.RIGHT);
		
		apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doCompute();
			}
		});
		refresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
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
		
		ThreadPoolExecutor exec = new ThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		exec.allowCoreThreadTimeOut(true);
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
				double eff = b.getEfficiency();
				if (e < 0) {
					totalEnergyDemand += e;
				} else {
					producedEnergy += b.getEnergy() * eff;
				}
				if (Building.isOperational(eff)) {
					operationalBuildings++;
				}
				totalEfficiency += eff;
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
		apply.setEnabled(false);
		doRefresh();

		Allocator.computeNow(buildings, ResourceAllocationStrategy.DAMAGE_AWARE, Integer.parseInt(availableWorkers.getText()));
		doRefresh();
		apply.setEnabled(true);
	}
}
