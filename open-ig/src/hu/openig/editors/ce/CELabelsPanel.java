/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import hu.openig.core.Pair;
import hu.openig.utils.Exceptions;
import hu.openig.utils.GUIUtils;
import hu.openig.utils.U;
import hu.openig.utils.XElement;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.table.TableRowSorter;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.xml.stream.XMLStreamException;

/**
 * Label editor panel.
 * @author akarnokd, 2012.11.01.
 */
public class CELabelsPanel extends CEBasePanel {
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
		public final Map<String, XElement> content = U.newHashMap();
	}
	/** The language index to code. */
	final Map<Integer, String> languageIndex = U.newHashMap();
	/** The back reference to the list of languages and entries. */
	Map<String, List<XElement>> languages;
	/** The sorter. */
	private TableRowSorter<GenericTableModel<LabelEntry>> mainSorter;
	/**
	 * Constructs the panel.
	 * @param context the context
	 */
	public CELabelsPanel(CEContext context) {
		super(context);
		initGUI();
		
		loadTestData();
	}
	/** Initialize the GUI. */
	private void initGUI() {
		verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		verticalSplit.setTopComponent(createTopPanel());
		verticalSplit.setResizeWeight(0.75);
		
		setLayout(new BorderLayout());
		add(verticalSplit, BorderLayout.CENTER);
		
		doUpdateCount();
	}
	/** @return creates the top panel. */
	private JPanel createTopPanel() {
		JLabel filterLabel = new JLabel(get("tech.filter"));
		filter = new JComboBox<String>();
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
				XElement xe = item.content.get(lng);
				return xe != null ? xe.content : null;
			}
		};
		
		mainTable = new JTable(mainModel);

		mainSorter = new TableRowSorter<GenericTableModel<LabelEntry>>(mainModel);
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
	 * @param languages the languages
	 */
	public void update(Map<String, List<XElement>> languages) {
		this.languages = languages;
		languageIndex.clear();
		mainModel.clear();

		Set<String> keys = new LinkedHashSet<String>();
		for (List<XElement> e : languages.values()) {
			for (XElement k : e) {
				keys.add(k.get("key", ""));
			}
		}
		
		Class<?>[] classes = new Class<?>[languages.size() + 2];
		classes[0] = ImageIcon.class;
		classes[1] = String.class;
		
		List<String> names = U.newArrayList();
		names.add("");
		names.add(get("label.key"));
		int k = 0;
		for (String lk : languages.keySet()) {
			languageIndex.put(k + 2, lk);
			classes[k + 2] = String.class;
			names.add(lk);
			k++;
		}
		
		Map<String, LabelEntry> map = U.newHashMap();
		for (String s : keys) {
			LabelEntry le = new LabelEntry();
			le.key = s;
			mainModel.add(le);
			map.put(s, le);
		}

		for (Map.Entry<String, List<XElement>> e : languages.entrySet()) {
			for (XElement item : e.getValue()) {
				LabelEntry le = map.get(item.get("key", ""));
				le.content.put(e.getKey(), item);
			}
		}
		mainModel.setColumnNames(names.toArray(new String[0]));
		mainModel.setColumnTypes(classes);
		
		mainModel.fireTableStructureChanged();
		mainTable.getColumnModel().getColumn(0).setMaxWidth(24);
	}
	/** Load the test labels. */
	void loadTestData() {
		Map<String, List<XElement>> langs = U.newHashMap();
		for (String lang : context.languages()) {
			byte[] langData = context.getData(lang, "labels.xml");
			if (langData != null) {
				try {
					XElement xlabels = XElement.parseXML(new ByteArrayInputStream(langData));
					langs.put(lang, xlabels.children());
				} catch (XMLStreamException ex) {
					Exceptions.add(ex);
				}
			}
		}
		update(langs);
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
				for (String lang : context.languages()) {
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
		return e.content.size() != context.languages().size();
	}
	/**
	 * Are there labels with the same translation?
	 * @param e the entry
	 * @return true if untranslated present
	 */
	boolean isUntranslated(LabelEntry e) {
		Set<String> strs = U.newHashSet();
		for (XElement xe : e.content.values()) {
			strs.add(xe.content);
		}
		return strs.size() != context.languages().size();
	}
}
