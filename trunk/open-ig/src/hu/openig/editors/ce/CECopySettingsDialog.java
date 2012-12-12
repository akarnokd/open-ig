/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import hu.openig.utils.U;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

/**
 * The dialog to list and mark existing definition files.
 * @author akarnokd, 2012.12.12.
 */
public class CECopySettingsDialog extends JDialog {
	/** */
	private static final long serialVersionUID = -5563503589598771946L;
	/** The context. */
	protected CEContext ctx;
	/** The copy operation. */
	public enum CopyOp {
		/** Reference the data file. */
		REFERENCE,
		/** Create a full copy of the datafile. */
		COPY,
		/** Have an empty datafile. */
		BLANK
	}
	/**
	 * The data files.
	 * @author akarnokd, 2012.12.12.
	 */
	public enum DataFiles {
		/** Main parameters. */
		PARAMETERS,
		/** Image. */
		IMAGE,
		/** Galaxy. */
		GALAXY,
		/** Players. */
		PLAYERS,
		/** Planets. */
		PLANETS,
		/** Technology. */
		TECHNOLOGY,
		/** Buildings. */
		BUILDINGS,
		/** Battle. */
		BATTLE,
		/** Diplomacy. */
		DIPLOMACY,
		/** Bridge. */
		BRIDGE,
		/** Bar talks. */
		TALKS,
		/** Ship walks. */
		SHIPWALK,
		/** Chat. */
		CHAT,
		/** Test. */
		TEST,
		/** Spies. */
		SPIES
	}
	/** The copy operation. */
	final List<JComboBox<String>> copyOps = U.newArrayList();
	/** The copy labels. */
	final List<JLabel> copyLabels = U.newArrayList();
	/** Was the dialog approved? */
	protected boolean approved;
	/**
	 * Constructor. Initializes the dialog.
	 * @param ctx the context
	 */
	public CECopySettingsDialog(CEContext ctx) {
		this.ctx = ctx;
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle(get("copy_settings.title"));
		setModal(true);
		setResizable(false);
		initComponents();
	}
	/**
	 * Returns an editor label.
	 * @param key the label key
	 * @return the translation
	 */
	String get(String key) {
		return ctx.get(key);
	}
	/**
	 * Initialize the components.
	 */
	void initComponents() {
		Container c = getContentPane();
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);
		
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);

		SequentialGroup vert = gl.createSequentialGroup();
		
		int columns = 4;
		SequentialGroup allCols = gl.createSequentialGroup();
		List<ParallelGroup> cols = U.newArrayList();;
		for (int i = 0; i < columns; i++) {
			ParallelGroup col = gl.createParallelGroup();
			allCols.addGroup(col);
			cols.add(col);
		}
		
		ParallelGroup row = null;

		// --------------------------------
		
		JButton ok = new JButton(get("ok"));
		JButton cancel = new JButton(get("cancel"));
		JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
		JSeparator sep1 = new JSeparator(JSeparator.HORIZONTAL);

		Vector<String> ops = new Vector<String>();
		for (CopyOp co : CopyOp.values()) {
			ops.add(get("copy_settings.op_" + co));
		}
		
		
		int j = 0;
		for (DataFiles df : DataFiles.values()) {
			JLabel label = new JLabel(get("copy_settings.data_" + df));
			JComboBox<String> box = new JComboBox<String>(ops);
			
			copyLabels.add(label);
			copyOps.add(box);
			
			if ((j * 2) % cols.size() == 0) {
				row = gl.createParallelGroup(Alignment.BASELINE);
				vert.addGroup(row);
			}
			ParallelGroup cola = cols.get((j * 2) % cols.size()); 
			ParallelGroup colb = cols.get((j * 2 + 1) % cols.size()); 
			
			cola.addComponent(label);
			colb.addComponent(box);
			
			
			row.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(label)
				.addComponent(box)
			);
			j++;
		}
		
		JLabel setAllLabel = new JLabel(get("copy_settings.set_all"));
		final JComboBox<String> setAll = new JComboBox<String>(ops);
		setAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (JComboBox<String> cb : copyOps) {
					cb.setSelectedIndex(setAll.getSelectedIndex());
				}
			}
		});
		
		// --------------------------------

		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(setAllLabel)
				.addComponent(setAll, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addComponent(sep1)
			.addGroup(allCols)
			.addComponent(sep)
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
				.addComponent(setAllLabel)
				.addComponent(setAll)
			)
			.addComponent(sep1, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGroup(vert)
			.addComponent(sep, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(ok)
				.addComponent(cancel)
			)
		);
		
		gl.linkSize(SwingConstants.HORIZONTAL, ok, cancel);
		
		pack();
		// --------------------------------
		
		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				approved = true;
				dispose();
			}
		});
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
	}
	/**
	 * @return was the dialog closed via OK button?
	 */
	public boolean isApproved() {
		return approved;
	}
}
