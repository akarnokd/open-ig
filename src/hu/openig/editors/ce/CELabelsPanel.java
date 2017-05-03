/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import hu.openig.core.Action1;
import hu.openig.core.Pair;
import hu.openig.utils.GUIUtils;
import hu.openig.utils.U;
import hu.openig.utils.XElement;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;

/**
 * Label editor panel.
 * @author akarnokd, 2012.11.01.
 */
public class CELabelsPanel extends CEBasePanel implements CEPanelPreferences {
	/** */
	private static final long serialVersionUID = -3156259343025443390L;
	/** The vertical splitter. */
	JSplitPane verticalSplit;
	/** The main table. */
	JTable mainTable;
	/** The main model. */
	GenericTableModel<LabelEntry> mainModel;
	/** The filter box. */
	JComboBox<String> filter;
	/** The options menu. */
	JPopupMenu optionsMenu;
	/** The options button. */
	JButton optionsButton;
	/** The total row counts. */
	JLabel counts;
	/** A label entry. */
	public static class LabelEntry {
		/** The label key. */
		public String key;
		/** The label content. */
		public final Map<String, String> content = new HashMap<>();
	}
	/** The language index to code. */
	final Map<Integer, String> languageIndex = new HashMap<>();
	/** The sorter. */
	TableRowSorter<GenericTableModel<LabelEntry>> mainSorter;
	/** The key field. */
	JTextField keyField;
	/** The key label. */
	JLabel keyLabel;
	/** The bottom panel. */
	JPanel bottomPanel;
	/** The text areas. */
	final Map<String, CEValueBox<JTextArea>> textAreas = new HashMap<>();
	/**
	 * Constructs the panel.
	 * @param context the context
	 */
	public CELabelsPanel(CEContext context) {
		super(context);
		initGUI();
	}
	/** Initialize the GUI. */
	private void initGUI() {
		verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		verticalSplit.setTopComponent(createTopPanel());
		verticalSplit.setBottomComponent(createBottomPanel());
		verticalSplit.setResizeWeight(0.75);
		
		setLayout(new BorderLayout());
		add(verticalSplit, BorderLayout.CENTER);
		
		doUpdateCount();
	}
	/** @return creates the top panel. */
	private JPanel createTopPanel() {
		JLabel filterLabel = new JLabel(get("tech.filter"));
		filter = new JComboBox<>();
		filter.setEditable(true);
		
		JButton filterButton = new JButton(icon("/hu/openig/editors/res/Zoom16.gif"));
		JButton filterClearButton = new JButton(get("tech.clear"));
		
		JButton addButton = new JButton(icon("/hu/openig/gfx/plus16.png"));
		JButton removeButton = new JButton(icon("/hu/openig/gfx/minus16.png"));
		optionsButton = new JButton(icon("/hu/openig/gfx/down16.png"));
		
		optionsMenu = new JPopupMenu();
		
		counts = new JLabel();
		counts.setHorizontalTextPosition(JLabel.CENTER);
		
		JLabel filterHelp = new JLabel(icon("/hu/openig/gfx/unknown.png"));

		mainModel = new GenericTableModel<LabelEntry>() {
			/**	 */
			private static final long serialVersionUID = -3868826438588405491L;

			@Override
			public Object getValueFor(LabelEntry item, int rowIndex,
					int columnIndex) {
				if (columnIndex == 0) {
					return validate(item);
				}
				if (columnIndex == 1) {
					return item.key;
				}
				String lng = languageIndex.get(columnIndex);
				return item.content.get(lng);
			}
		};
		
		mainTable = new JTable(mainModel);

		mainSorter = new TableRowSorter<>(mainModel);
		mainTable.setRowSorter(mainSorter);

		JScrollPane mainScroll = new JScrollPane(mainTable);
		
		JPanel top = new JPanel();
		GroupLayout gl = new GroupLayout(top);
		top.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);

		// ----------------------------------------
		
		ActionListener filterAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doFilter();
			}
		};
		filter.addActionListener(filterAction);
		filterButton.addActionListener(filterAction);
		filterClearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doClearFilter();
			}
		});

		String filterHelpStr = createFilterHelp();
		
		filterLabel.setToolTipText(filterHelpStr);
		filterHelp.setToolTipText(filterHelpStr);
		filter.setToolTipText(filterHelpStr);
		
		mainTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int idx = mainTable.getSelectedRow();
					if (idx >= 0) {
						idx = mainTable.convertRowIndexToModel(idx);
						doDetails(mainModel.get(idx), idx);
					} else {
						doDetails(null, -1);
					}
				}
			}
		});
		
		
		
		// -----------------------------------------
		
		gl.setHorizontalGroup(
			gl.createParallelGroup()
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(filterLabel)
				.addComponent(filterHelp)
				.addComponent(filter, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(filterButton)
				.addComponent(filterClearButton)
				.addComponent(counts, 60, 60, 60)
				.addComponent(addButton)
				.addComponent(removeButton)
				.addComponent(optionsButton)
			)
			.addComponent(mainScroll)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(filterLabel)
				.addComponent(filterHelp)
				.addComponent(filter, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(filterButton)
				.addComponent(filterClearButton)
				.addComponent(counts)
				.addComponent(addButton)
				.addComponent(removeButton)
				.addComponent(optionsButton)
			)
			.addComponent(mainScroll)
		);
		
		gl.linkSize(SwingConstants.VERTICAL, filterLabel, filter, filterButton, filterClearButton, addButton, removeButton, optionsButton);

		return top;
	}
	/**
	 * Update the full table.
	 */
	public void update() {
		languageIndex.clear();
		mainModel.clear();
		
		CampaignData cd = context.campaignData();
		List<String> languages = U.newArrayList(cd.definition.languages());
		
		
		Class<?>[] classes = new Class<?>[languages.size() + 2];
		classes[0] = ImageIcon.class;
		classes[1] = String.class;
		
		List<String> names = new ArrayList<>();
		names.add("");
		names.add(get("label.key"));
		int k = 0;
		for (String lk : languages) {
			languageIndex.put(k + 2, lk);
			classes[k + 2] = String.class;
			names.add(lk);
			k++;
		}

		mainModel.setColumnNames(names.toArray(new String[names.size()]));
		mainModel.setColumnTypes(classes);
		
		for (String key : cd.labels.keys()) {
			LabelEntry le = new LabelEntry();
			le.key = key;
			cd.labels.putInto(key, le.content);
			mainModel.add(le);
		}
		
		
		mainModel.fireTableStructureChanged();
		mainTable.getColumnModel().getColumn(0).setMaxWidth(24);
		
		updateDetails();
		doUpdateCount();
	}
	/** Clear the filter. */
	void doClearFilter() {
		filter.setSelectedItem("");
		doFilter();
	}
	/**
	 * Update the total/filtered count.
	 */
	void doUpdateCount() {
		String filterStr = (String)filter.getSelectedItem();
		if (filterStr == null || filterStr.isEmpty()) {
			counts.setText(String.format("%d", mainModel.getRowCount()));
		} else {
			counts.setText(String.format("%d / %d", mainTable.getRowCount(), mainModel.getRowCount()));
		}
	}
	/** Apply the filter. */
	void doFilter() {
		final String filterStr = ((String)filter.getSelectedItem()).trim();
		
		if (filterStr.isEmpty()) {
			mainSorter.setRowFilter(null);
			mainModel.fireTableDataChanged();
		} else {
			GUIUtils.addFirstItem(filter, filterStr);
			final List<Pair<String, Pattern>> parsed = CETools.parseFilter(filterStr);
			CEXRowFilter<LabelEntry> rf = new CEXRowFilter<LabelEntry>() {
				@Override
				public boolean include(LabelEntry item, Object[] displayValues, int index) {
					return checkFilter(item, displayValues, parsed);
				}
			};
			mainSorter.setRowFilter(rf);
		}
		doUpdateCount();
	}
	/**
	 * Execute the filter on the item and return true if the item should be visible.
	 * @param item the item
	 * @param displayValues the values
	 * @param filter the filter definition
	 * @return true if the item should be visible in the table.
	 */
	boolean checkFilter(LabelEntry item, Object[] displayValues, List<Pair<String, Pattern>> filter) {
		for (Pair<String, Pattern> e : filter) {
			String key = e.first;
			if (key.isEmpty()) {
				boolean any = false;
				for (Object o : displayValues) {
					String s = o != null ? o.toString() : "";
					if (e.second.matcher(s).matches()) {
						any = true;
						break;
					}
				}
				if (!any) {
					return false;
				}
			} else {
				if ("key".equals(key)) {
					if (!checkFilterValue(displayValues[1], e.second)) {
						return false;
					}
				}
				int li = 2;
				for (String lang : context.dataManager().languages()) {
					if (lang.equals(key)) {
						if (!checkFilterValue(displayValues[li], e.second)) {
							return false;
						}
					}
					li++;
				}
				if ("missing".equals(key)) {
					if (e.second.matcher("true").matches()) {
						return isMissing(item);
					} else
					if (e.second.matcher("false").matches()) {
						return !isMissing(item);
					}
					for (String k : item.content.keySet()) {
						if (e.second.matcher(k).matches()) {
							return isMissing(item);
						}
					}
				}
				if ("untranslated".equals(key)) {
					if (e.second.matcher("true").matches()) {
						return isUntranslated(item);
					} else
					if (e.second.matcher("false").matches()) {
						return !isUntranslated(item);
					}
				}
				if ("valid".equals(key)) {
					String value = "ok";
					ImageIcon ico = (ImageIcon)displayValues[0];
					if (ico == warningIcon) {
						value = "warning";
					} else
					if (ico == errorIcon) {
						value = "error";
					}
					if (!checkFilterValue(value, e.second)) {
						return false;
					}
				}
			}
		}
		return true;
	}
	/**
	 * Check if the value matches the pattern.
	 * @param o the display value
	 * @param pattern the pattern
	 * @return true if filter matches or that field
	 */
	boolean checkFilterValue(Object o, Pattern pattern) {
		String s = o != null ? o.toString() : "";
		return pattern.matcher(s).matches();
	}
	/** @return construct the filter help. */
	String createFilterHelp() {
		StringBuilder b = new StringBuilder();
		
		b.append("<html>");
		b.append(get("tech.filter.intro")).append("<br>");
		b.append(get("tech.filter.format")).append("<br>");
		b.append("<table border='1'>");
		b.append("<tr><td>").append(get("tech.filter.key")).append("</td><td>").append(get("tech.filter.desc")).append("</td></tr>");
		for (String key : new String[] { "key", "language", "valid", "missing", "untranslated" }) {
			b.append("<tr><td>").append(key).append("</td><td>").append(get("tech.filter.key." + key)).append("</td></tr>");
		}
		b.append("</table>");
		
		return b.toString();
	}
	/**
	 * Validate the given line.
	 * @param e the entry
	 * @return the validity icon
	 */
	ImageIcon validate(LabelEntry e) {
		if (isMissing(e)) {
			return errorIcon;
		}
		if (isUntranslated(e)) {
			return warningIcon;
		}
		return null;
	}
	/**
	 * Are some translations missing?
	 * @param e the entry
	 * @return true if missing
	 */
	boolean isMissing(LabelEntry e) {
		return e.content.size() != context.dataManager().languages().size();
	}
	/**
	 * Are there labels with the same translation?
	 * @param e the entry
	 * @return true if untranslated present
	 */
	boolean isUntranslated(LabelEntry e) {
		Set<String> strs = new HashSet<>();
		strs.addAll(e.content.values());
		return strs.size() != context.dataManager().languages().size();
	}
	/** @return the bottom panel. */
	JComponent createBottomPanel() {
		bottomPanel = new JPanel();
		
		GroupLayout gl = new GroupLayout(bottomPanel);
		bottomPanel.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);

		keyField = new JTextField();
		keyLabel = new JLabel(get("label.key"));
		
		
		JScrollPane sp = new JScrollPane(bottomPanel);
		sp.getVerticalScrollBar().setUnitIncrement(30);
		sp.getVerticalScrollBar().setBlockIncrement(90);
		
		
		return sp;
	}
	/**
	 * Update the controls of the detail panel.
	 */
	void updateDetails() {
		bottomPanel.removeAll();
		GroupLayout gl = (GroupLayout)bottomPanel.getLayout();
		
		ParallelGroup c1 = gl.createParallelGroup();
		ParallelGroup c2 = gl.createParallelGroup();
		
		SequentialGroup r1 = gl.createSequentialGroup();
		
		c1.addComponent(keyLabel);
		c2.addComponent(keyField);
		
		r1.addGroup(
			gl.createParallelGroup(Alignment.BASELINE)
			.addComponent(keyLabel)
			.addComponent(keyField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		);
		
		textAreas.clear();
		
		for (final String lang : context.dataManager().languages()) {
			JLabel lbl = new JLabel(lang);
			JTextArea ta = new JTextArea();
			
			final CEValueBox<JTextArea> vta = CEValueBox.of("", ta);
			vta.validator = new Action1<JTextArea>() {
				@Override
				public void invoke(JTextArea value) {
					validateLabelField(vta);
				}
			};
			
			textAreas.put(lang, vta);
			
			c1.addComponent(lbl);
			c2.addComponent(vta);
			r1.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(lbl)
				.addComponent(vta, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			);
		}
		
		
		gl.setHorizontalGroup(
			gl.createSequentialGroup()
			.addGroup(c1)
			.addGroup(c2)
		);
		gl.setVerticalGroup(
			r1
		);
	}
	/**
	 * Validate the label fields.
	 * @param vta the current field
	 */
	void validateLabelField(final CEValueBox<JTextArea> vta) {
		if (vta.component.getText().isEmpty()) {
			vta.setInvalid(errorIcon, get("label.missing_translation"));
		} else {
			for (CEValueBox<JTextArea> vta2 : textAreas.values()) {
				if (vta2 != vta && vta2.component.getText().equals(vta.component.getText())) {
					vta.setInvalid(warningIcon, get("label.duplicate_translation"));
				}
			}
		}
	}
	/**
	 * Show the details of an item. If null, all fields should be emptied/disabled as necessary.
	 * @param item the item
	 * @param index the model index
	 */
	void doDetails(LabelEntry item, int index) {
		for (CEValueBox<JTextArea> ta : textAreas.values()) {
			ta.clearInvalid();
			ta.component.setText("");
		}
		if (item != null) {
			keyField.setText(item.key);
			for (Map.Entry<String, String> e : item.content.entrySet()) {
				CEValueBox<JTextArea> ceValueBox = textAreas.get(e.getKey());
				ceValueBox.component.setText(e.getValue());
			}
			for (Map.Entry<String, String> e : item.content.entrySet()) {
				CEValueBox<JTextArea> ceValueBox = textAreas.get(e.getKey());
				ceValueBox.validateComponent();
			}
		} else {
			keyField.setText("");
		}
	}
	/** Load the labels from the campaign data. */
	public void load() {
		update();
	}
	@Override
	public void loadPreferences(XElement preferences) {
		verticalSplit.setDividerLocation(preferences.getInt("vertical-split", verticalSplit.getDividerLocation()));
	}
	@Override
	public String preferencesId() {
		return "labels-panel";
	}
	@Override
	public void savePreferences(XElement preferences) {
		preferences.set("vertical-split", verticalSplit.getDividerLocation());
	}
}
