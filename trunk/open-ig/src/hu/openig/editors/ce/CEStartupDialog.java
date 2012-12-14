/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import hu.openig.core.Func1;
import hu.openig.core.Pair;
import hu.openig.model.GameDefinition;
import hu.openig.utils.U;
import hu.openig.utils.XElement;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.xml.stream.XMLStreamException;

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
	/** The campaign image. */
	CEImage image;
	/** The label codes. */
	JTextField languages;
	/** The project language. */
	JComboBox<String> projectLang;
	/** Do not fire change events when loading. */
	boolean projectLangLoading;
	/**
	 * Table renderer for java.util.Date values.
	 * @author akarnokd, 2012.12.12.
	 */
	private static final class DateTimeRenderer extends
			DefaultTableCellRenderer {
		/** */
		private static final long serialVersionUID = 2590248281492761016L;

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
					row, column);
			
			label.setText(SimpleDateFormat.getDateTimeInstance().format((Date)value));
			
			return label;
		}
	}
	/**
	 * Table renderer for String values with tooltips.
	 * @author akarnokd, 2012.12.12.
	 */
	private static final class StringRenderer extends
			DefaultTableCellRenderer {
		/** */
		private static final long serialVersionUID = 3724817465755739661L;

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
					row, column);
			
			label.setToolTipText((String)value);
			
			return label;
		}
	}
	/** The okay button. */
	JButton ok;
	/** Recent table scroll. */
	JScrollPane recentTableScroll;
	/** Open specific. */
	JButton open;
	
	/** A campaign item. */
	static class CampaignItem {
		/** The file name. */
		String file;
		/** The access date. */
		Date date;
		/** The concrete definition. */
		GameDefinition definition;
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
		newNameIndicator = new JLabel(" ");
		newNameIndicator.setForeground(Color.RED);
		
		copyModel = new GenericTableModel<CampaignItem>() {
			/** */
			private static final long serialVersionUID = -2979363143461972883L;

			@Override
			public Object getValueFor(CampaignItem item, int rowIndex,
					int columnIndex) {
				switch (columnIndex) {
				case 0: return item.file;
				case 1: return item.definition.getTitle(getProjectLanguage());
				case 2: return item.definition.getDescription(getProjectLanguage());
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
				case 1: return item.definition.getTitle(getProjectLanguage());
				case 2: return item.definition.getDescription(getProjectLanguage());
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
		copyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		copyTable.setDefaultRenderer(Date.class, new DateTimeRenderer());
		copyTable.setDefaultRenderer(String.class, new StringRenderer());
		
		recentTable = new JTable(recentModel);
		recentTable.setAutoCreateRowSorter(true);
		recentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		recentTable.setDefaultRenderer(Date.class, new DateTimeRenderer());
		recentTable.setDefaultRenderer(String.class, new StringRenderer());
		
		JScrollPane copyTableScroll = new JScrollPane(copyTable);
		recentTableScroll = new JScrollPane(recentTable);
		
		ok = new JButton(get("ok"));
		ok.setEnabled(false);
		JButton cancel = new JButton(get("cancel"));
		
		open = new JButton(get("startup.open"));
		open.setIcon(new ImageIcon(getClass().getResource("../res/Open16.gif")));
		
		JButton refresh = new JButton(get("startup.refresh"));
		refresh.setIcon(new ImageIcon(getClass().getResource("../res/Refresh16.gif")));
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(createNew);
		bg.add(copyExisting);
		bg.add(openRecent);
		
		JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
		
		image = new CEImage();
		image.setModal(true);
		
		languages = new JTextField("en");
		JLabel languagesLabel = new JLabel(get("startup.labels"));
		
		JLabel projectLangLabel = new JLabel(get("startup.project_language"));
		projectLang = new JComboBox<String>();
		projectLang.addItem("en");
		
		// XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addGroup(
				gl.createParallelGroup()
				.addGroup(
					gl.createSequentialGroup()
					.addComponent(newNameLabel)
					.addComponent(newName)
				)
				.addGroup(
					gl.createSequentialGroup()
					.addGap(20)
					.addComponent(newNameIndicator)
				)
				.addGroup(
					gl.createSequentialGroup()
					.addComponent(languagesLabel)
					.addComponent(languages)
				)
				.addGroup(
					gl.createSequentialGroup()
					.addComponent(projectLangLabel)
					.addComponent(projectLang, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				)
				.addComponent(createNew)
				.addGroup(
					gl.createSequentialGroup()
					.addComponent(copyExisting, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
					.addComponent(refresh)
				)
				.addGroup(
					gl.createSequentialGroup()
					.addComponent(copyTableScroll)
					.addComponent(image, 100, 100, 100)
				)
				.addGroup(
					gl.createSequentialGroup()
					.addComponent(openRecent, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
					.addComponent(open)
				)
				.addComponent(recentTableScroll)
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
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(newNameLabel)
				.addComponent(newName, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addComponent(newNameIndicator)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(languagesLabel)
				.addComponent(languages, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(projectLangLabel)
				.addComponent(projectLang, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addComponent(createNew)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(copyExisting)
				.addComponent(refresh)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.LEADING)
				.addComponent(copyTableScroll, 100, 100, Short.MAX_VALUE)
				.addComponent(image, 100, 100, 100)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(openRecent)
				.addComponent(open)
			)
			.addComponent(recentTableScroll, 100, 100, Short.MAX_VALUE)
			.addComponent(sep, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(ok)
				.addComponent(cancel)
			)
		);
		
		gl.linkSize(SwingConstants.HORIZONTAL, ok, cancel);
		
		pack();
		setLocationRelativeTo(null);
		setMinimumSize(getSize());
		
		// oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
		
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		refresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				findCampaigns();
			}
		});
		ActionListener updateOkButtonAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateOkButton();
			}
		};
		
		ListSelectionListener updateOkButtonListener = new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				updateOkButton();
			}
		};
		
		createNew.addActionListener(updateOkButtonAction);
		copyExisting.addActionListener(updateOkButtonAction);
		openRecent.addActionListener(updateOkButtonAction);
		
		copyTable.getSelectionModel().addListSelectionListener(updateOkButtonListener);
		copyTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				int sel = copyTable.getSelectedRow();
				if (sel >= 0) {
					sel = copyTable.convertRowIndexToModel(sel);
					CampaignItem item = copyModel.get(sel);
					BufferedImage img = CEStartupDialog.this.ctx.dataManager().getImage(getProjectLanguage(), item.definition.imagePath + ".png");
					image.setIcon(img);
					copyExisting.setSelected(true);
					projectLangLoading = true;
					languages.setText(U.join(item.definition.titles.keySet(), ", "));
					projectLangLoading = false;
					updateLanguageBox();
				}
			}
		});
		copyTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					copyExisting.setSelected(true);
					if (copyTable.getSelectedRow() >= 0) {
						updateOkButton();
						ok.doClick();
					}
				}
			}
		});
		
		recentTable.getSelectionModel().addListSelectionListener(updateOkButtonListener);
		recentTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					openRecent.setSelected(true);
					if (recentTable.getSelectedRow() >= 0) {
						updateOkButton();
						ok.doClick();
					}
				}
			}
		});

		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doOk();
			}
		});
		
		newName.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				doChangeName(newName.getText());
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				doChangeName(newName.getText());
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				doChangeName(newName.getText());
			}
		});
		languages.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				changed();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				changed();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				changed();
			}
			/** Handle changes. */
			void changed() {
				if (!projectLangLoading) {
					updateLanguageBox();
				}
			}
		});
		projectLang.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				copyTable.repaint();
				recentTable.repaint();
			}
		});
		
		// -------
		doChangeName("");
		projectLang.setSelectedItem("en");
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
	/**
	 * Find the latest open-ig-upgrade zip file.
	 * @param workdir the working directory
	 * @return the data file or null if not present
	 */
	File getDataPack(File workdir) {
		List<File> baseZip = U.listFiles(workdir, new Func1<File, Boolean>() {
			@Override
			public Boolean invoke(File value) {
				return value.isFile() && value.getName().startsWith("open-ig-upgrade-")
						&& value.getName().toLowerCase().endsWith(".zip");
			} 
		});
		Collections.sort(baseZip, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return o2.getName().compareTo(o1.getName());
			}
		});
		if (baseZip.isEmpty()) {
			return null;
		}
		return baseZip.get(0);
	}
	/**
	 * Find campaigns in the main files or DLC files.
	 */
	public void findCampaigns() {
		File workdir = ctx.getWorkDir();
		
		List<File> unpackedCampaigns = U.newArrayList();
		List<Pair<File, String>> packedCampaigns = U.newArrayList();
		
		// take the contents of the DLC directory
		File dlcs = new File(workdir, "dlc");
		for (File f : U.listFiles(dlcs)) {
			if (f.isDirectory()) {
				unpackedCampaigns.addAll(U.listFiles(new File(f, "generic/campaign"), CEDataManager.IS_DIR));
				unpackedCampaigns.addAll(U.listFiles(new File(f, "generic/skirmish"), CEDataManager.IS_DIR));
			} else
			if (CEDataManager.IS_ZIP.invoke(f)) {
				for (String zf : U.zipDirEntries(f, "generic/campaign")) {
					packedCampaigns.add(Pair.of(f, zf));
				}
				for (String zf : U.zipDirEntries(f, "generic/skirmish")) {
					packedCampaigns.add(Pair.of(f, zf));
				}
			}
		}
		
		// scan an unpacked base campaign
		File baseCampaign = new File(workdir, "data/generic/campaign");
		unpackedCampaigns.addAll(U.listFiles(baseCampaign));
		
		// scan an unpacked base skirmish
		File baseSkirmish = new File(workdir, "data/generic/skirmish");
		unpackedCampaigns.addAll(U.listFiles(baseSkirmish));

		// scan a packed data zip
		File dataPack = getDataPack(workdir);
		if (dataPack != null) {
			for (String zf : U.zipDirEntries(dataPack, "generic/campaign")) {
				packedCampaigns.add(Pair.of(dataPack, zf));
			}
			for (String zf : U.zipDirEntries(dataPack, "generic/skirmish")) {
				packedCampaigns.add(Pair.of(dataPack, zf));
			}
		}
		
		copyModel.clear();
		
		for (File unpacked : unpackedCampaigns) {
			File def = new File(unpacked, "definition.xml");
			if (def.canRead()) {
				try {
					GameDefinition gdef = new GameDefinition();
					gdef.name = unpacked.getName();
					gdef.parse(XElement.parseXML(def));
					
					CampaignItem item = new CampaignItem();
					item.file = def.getPath();
					item.date = new Date(def.lastModified());
					item.definition = gdef;
					copyModel.add(item);
				} catch (IllegalArgumentException ex) {
					// ignored
				} catch (XMLStreamException ex) {
					// ignored
				}
			}
		}
		for (Pair<File, String> packed : packedCampaigns) {
			byte[] xdef = U.zipData(packed.first, packed.second + "/definition.xml");
			if (xdef != null) {
				try {
					GameDefinition gdef = new GameDefinition();
					gdef.name = packed.second;
					int idx = gdef.name.lastIndexOf('/');
					if (idx >= 0) {
						gdef.name = gdef.name.substring(idx + 1);
					}
					gdef.parse(XElement.parseXML(new ByteArrayInputStream(xdef)));
					CampaignItem item = new CampaignItem();
					item.file = packed.first.getPath() + "!/" + packed.second;
					item.date = new Date(packed.first.lastModified());
					item.definition = gdef;
					copyModel.add(item);
				} catch (IllegalArgumentException ex) {
					// ignored
				} catch (XMLStreamException ex) {
					// ignored
				}
			}
		}
	}
	/**
	 * Enable/disable ok button.
	 */
	void updateOkButton() {
		ok.setEnabled(
			(
				(
					(createNew.isSelected() 
					|| (copyExisting.isSelected() && copyTable.getSelectedRow() >= 0))
					&& !newName.getText().isEmpty() && newNameIndicator.getIcon() != ctx.getIcon(CESeverityIndicator.ERROR)
					&& languageArray().length > 0
				)
					
				|| (openRecent.isSelected() && recentTable.getSelectedRow() >= 0)
			) && projectLang.getSelectedIndex() >= 0
			);
	}
	/** The okay action. */
	void doOk() {
		if (createNew.isSelected()) {
			dispose();
		} else
		if (openRecent.isSelected()) {
			dispose();
		} else
		if (copyExisting.isSelected()) {
			CECopySettingsDialog dialog = new CECopySettingsDialog(ctx);
			dialog.setLocationRelativeTo(this);
			dialog.setVisible(true);
			if (dialog.isApproved()) {
				dispose();
				
				CEDataManager dm = ctx.dataManager();
				dm.campaignData = new CampaignData();
				int idx = copyTable.getSelectedRow();
				idx = copyTable.convertRowIndexToModel(idx);
				dm.campaignData.definition = copyModel.get(idx).definition;
				dm.campaignData.definition.name = newName.getText();
				
				String[] langs = languageArray();
				
				dm.campaignData.definition.haveLanguages(langs);
				dm.campaignData.projectLanguage = getProjectLanguage();
				
				dm.copy(dialog.getSettings());
				dm.load();
				
				ctx.load();
			}
		}
	}
	/** @return The list of specified languages. */
	private String[] languageArray() {
		return U.trim(U.split(languages.getText().trim(), ","));
	}
	/**
	 * Handle the name editing.
	 * @param text the text
	 */
	void doChangeName(String text) {
		if (text.isEmpty()) {
			newNameIndicator.setIcon(ctx.getIcon(CESeverityIndicator.ERROR));
			newNameIndicator.setText(get("startup.name_empty"));
		} else
		if (!text.matches("[a-zA-Z0-9\\-_\\.]+")) {
			newNameIndicator.setIcon(ctx.getIcon(CESeverityIndicator.ERROR));
			newNameIndicator.setText(get("startup.invalid_characters_in_name"));
		} else {
			File dlcName = new File(ctx.getWorkDir(), "dlc/" + text);
			if (dlcName.exists()) {
				newNameIndicator.setIcon(ctx.getIcon(CESeverityIndicator.WARNING));
				newNameIndicator.setText(get("startup.dlc_exists"));
			} else {
				newNameIndicator.setIcon(null);
				newNameIndicator.setText(" ");
			}
		}
		
		updateOkButton();
	}
	/**
	 * Show the recent fields?
	 * @param visible true if visible
	 */
	public void showRecent(boolean visible) {
		recentTableScroll.setVisible(visible);
		open.setVisible(visible);
		openRecent.setVisible(visible);
		openRecent.setSelected(false);
	}
	/**
	 * @return The currently selected project language.
	 */
	public String getProjectLanguage() {
		return (String)projectLang.getSelectedItem();
	}
	/**
	 * Update the language box to the current available project languages.
	 */
	void updateLanguageBox() {
		Object sel = projectLang.getSelectedItem();
		projectLang.setModel(new DefaultComboBoxModel<String>(languageArray()));
		projectLang.setSelectedItem(sel);
		
		copyTable.repaint();
		recentTable.repaint();
		updateOkButton();
	}
}
