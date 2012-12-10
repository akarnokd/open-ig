/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import hu.openig.utils.XElement;

import java.awt.Container;
import java.util.Date;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * The startup dialog with options to create a new campaign, copy an existing
 * or load a recent one.
 * @author akarnokd, 2012.12.10.
 */
public class CEStartupDialog extends JDialog implements CEPanelPreferences {
	/** */
	private static final long serialVersionUID = 8615521265482168600L;
	/** The context. */
	CEContext ctx;
	/** Create a brand new campaign. */
	JRadioButton createNew;
	/** Copy an existing campaign. */
	JRadioButton copyExisting;
	/** Open a recent. */
	JRadioButton openRecent;
	/** The campaign name. */
	JTextField newName;
	/** The name indicator. */
	JLabel newNameIndicator;
	/** Table for copying existing campaign. */
	JTable copyTable;
	/** Table for recent campaigns. */
	JTable recentTable;
	/** The copy table model. */
	GenericTableModel<CampaignItem> copyModel;
	/** The recent table model. */
	GenericTableModel<CampaignItem> recentModel;
	/** A campaign item. */
	static class CampaignItem {
		/** The file name. */
		String file;
		/** The campaign name. */
		String name;
		/** The campaign description. */
		String description;
		/** The access date. */
		Date date;
	}
	/**
	 * Construct the GUI.
	 * @param ctx the context
	 */
	public CEStartupDialog(CEContext ctx) {
		this.ctx = ctx;
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setModal(true);

		setTitle(get("startup.title"));
		
		Container c = getContentPane();
		
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		createNew = new JRadioButton(get("startup.create_new"));
		copyExisting = new JRadioButton(get("startup.copy_existing"));
		openRecent = new JRadioButton(get("startup.open_recent"));
		
		JLabel newNameLabel = new JLabel(get("startup.create_new_desc"));
		newName = new JTextField();
		newNameIndicator = new JLabel();
		
		copyModel = new GenericTableModel<CampaignItem>() {
			/** */
			private static final long serialVersionUID = -2979363143461972883L;

			@Override
			public Object getValueFor(CampaignItem item, int rowIndex,
					int columnIndex) {
				switch (columnIndex) {
				case 0: return item.file;
				case 1: return item.name;
				case 2: return item.description;
				case 3: return item.date;
				default:
					return null;
				}
			}
		};
		copyModel.setColumnNames(get("startup.file"), get("startup.name"), get("startup.description"), get("startup.date"));
		copyModel.setColumnTypes(String.class, String.class, String.class, Date.class);
		
		recentModel = new GenericTableModel<CampaignItem>() {
			/** */
			private static final long serialVersionUID = -2979363143461972883L;

			@Override
			public Object getValueFor(CampaignItem item, int rowIndex,
					int columnIndex) {
				switch (columnIndex) {
				case 0: return item.file;
				case 1: return item.name;
				case 2: return item.description;
				case 3: return item.date;
				default:
					return null;
				}
			}
		};

		recentModel.setColumnNames(get("startup.file"), get("startup.name"), get("startup.description"), get("startup.date"));
		recentModel.setColumnTypes(String.class, String.class, String.class, Date.class);
		
		copyTable = new JTable(copyModel);
		copyTable.setAutoCreateRowSorter(true);
		
		recentTable = new JTable(recentModel);
		copyTable.setAutoCreateRowSorter(true);
		
		JScrollPane copyTableScroll = new JScrollPane(copyTable);
		JScrollPane recentTableScroll = new JScrollPane(recentTable);
		
		JButton ok = new JButton(get("startup.ok"));
		JButton cancel = new JButton(get("startup.cancel"));
		
		JButton open = new JButton(get("startup.open"));
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(createNew);
		bg.add(copyExisting);
		bg.add(openRecent);
		
		JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addGroup(
				gl.createParallelGroup()
				.addComponent(createNew)
				.addGroup(
					gl.createSequentialGroup()
					.addGap(40)
					.addComponent(newNameLabel)
					.addComponent(newName)
					.addComponent(newNameIndicator, 20, 20, 20)
				)
				.addComponent(copyExisting)
				.addComponent(copyTableScroll)
				.addComponent(openRecent)
				.addComponent(recentTableScroll)
				.addComponent(open)
			)
			.addComponent(sep)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(ok)
				.addComponent(cancel)
			)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(createNew)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(newNameLabel)
				.addComponent(newName, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(newNameIndicator)
			)
			.addComponent(copyExisting)
			.addComponent(copyTableScroll, 100, 200, Short.MAX_VALUE)
			.addComponent(openRecent)
			.addComponent(recentTableScroll, 100, 200, Short.MAX_VALUE)
			.addComponent(open)
			.addComponent(sep)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(ok)
				.addComponent(cancel)
			)
		);
		
		pack();
		setLocationRelativeTo(null);
	}
	/**
	 * Returns an editor label.
	 * @param key the label key
	 * @return the translation
	 */
	String get(String key) {
		return ctx.get(key);
	}
	@Override
	public void loadPreferences(XElement preferences) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public String preferencesId() {
		return "startup-dialog";
	}
	@Override
	public void savePreferences(XElement preferences) {
		// TODO Auto-generated method stub
		
	}
}
