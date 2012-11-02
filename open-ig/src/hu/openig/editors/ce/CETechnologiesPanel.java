/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import hu.openig.core.Pair;
import hu.openig.model.ResearchSubCategory;
import hu.openig.utils.Exceptions;
import hu.openig.utils.GUIUtils;
import hu.openig.utils.U;
import hu.openig.utils.XElement;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;
import javax.xml.stream.XMLStreamException;

/**
 * The Technologies panel to edit contents of the <code>tech.xml</code>. 
 * @author akarnokd, 2012.10.31.
 */
public class CETechnologiesPanel extends CEBasePanel 
implements CEPanelPreferences, CEUndoRedoSupport, CEProblemLocator {
	/** */
	private static final long serialVersionUID = 8419996018754156220L;
	/** The filter box. */
	JComboBox<String> filter;
	/** The main table. */
	JTable technologies;
	/** The table row sorter. */
	TableRowSorter<GenericTableModel<XElement>> technologiesSorter;
	/** The main model. */
	GenericTableModel<XElement> technologiesModel;
	/** The options menu. */
	JPopupMenu optionsMenu;
	/** The options button. */
	JButton optionsButton;
	/** The total row counts. */
	JLabel counts;
	/** The first selected row. */
	XElement selected;
	/** The details panel. */
	JTabbedPane details;
	/** The ID. */
	private CEValueBox<JTextField> idField;
	/** The category field. */
	private CEValueBox<JComboBox<String>> categoryField;
	/** The factory field. */
	private CEValueBox<JComboBox<String>> factoryField;
	/** The index field. */
	private CEValueBox<JTextField> indexField;
	/**
	 * Constructor. Initializes the GUI.
	 * @param context the context object.
	 */
	public CETechnologiesPanel(CEContext context) {
		super(context);
		initGUI();
	}
	/**
	 * Construct the GUI.
	 */
	private void initGUI() {
		technologiesModel = new GenericTableModel<XElement>() {
			/** */
			private static final long serialVersionUID = 2557373261832556243L;

			@Override
			public Object getValueFor(XElement item, int rowIndex,
					int columnIndex) {
				switch (columnIndex) {
				case 0: return rowIndex;
				case 1: return item.get("id", "");
				case 2: return context.label(item.get("name", null));
				case 3: return context.get(item.get("category", null));
				case 4: return item.getIntObject("level");
				case 5: return item.get("race", null);
				case 6: return item.getIntObject("production-cost");
				case 7: return item.getIntObject("research-cost");
				case 8: return context.getIcon(getValidity(item));
				default:
					return null;
				}
			}	
		};
		technologiesModel.setColumnNames(
				get("tech.#"),
				get("tech.id"), 
				get("tech.name"), 
				get("tech.category"),
				get("tech.level"),
				get("tech.race"), 
				get("tech.research_cost"),
				get("tech.production_cost"), 
				"");
		technologiesModel.setColumnTypes(
				Integer.class,
				String.class,
				String.class,
				String.class,
				Integer.class,
				String.class,
				Integer.class,
				Integer.class,
				ImageIcon.class
		);
		
		// FIXME for now
		try {
			XElement xtech = XElement.parseXML("data/generic/campaign/main/tech.xml");
			technologiesModel.add(xtech.childrenWithName("item"));
		} catch (XMLStreamException ex) {
			Exceptions.add(ex);
		}
		
		technologies = new JTable(technologiesModel);
		technologiesSorter = new TableRowSorter<GenericTableModel<XElement>>(technologiesModel);
		technologies.setRowSorter(technologiesSorter);
		
		technologies.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				int idx = technologies.getSelectedRow();
				if (idx >= 0) {
					idx = technologies.convertRowIndexToModel(idx);
					doDetails(technologiesModel.get(idx));
				} else {
					doDetails(null);
				}
			}
		});
		
		
		JPanel top = createTopPanel();
		
		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		split.setTopComponent(top);
		
		JPanel bottom = createBottomPanel();
		
		split.setBottomComponent(bottom);
		
		setLayout(new BorderLayout());
		add(split, BorderLayout.CENTER);
		
		doUpdateCount();
	}
	/** 
	 * @return Create the top panel. 
	 */
	JPanel createTopPanel() {
		JScrollPane technologiesScroll = new JScrollPane(technologies);

		JPanel top = new JPanel();
		GroupLayout gl = new GroupLayout(top);
		top.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
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
		
		String filterHelpStr = createFilterHelp();
		
		filterLabel.setToolTipText(filterHelpStr);
		filterHelp.setToolTipText(filterHelpStr);
		filter.setToolTipText(filterHelpStr);
		
		createOptions();
		
		// assign actions --------------------------------------------------------
		
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
		
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doRemove();
			}
		});
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doAdd();
			}
		});
		optionsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doOptions();
			}
		});
		
		// layout ----------------------------------------------------------------
		
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
			.addComponent(technologiesScroll)
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
			.addComponent(technologiesScroll)
		);
		
		gl.linkSize(SwingConstants.VERTICAL, filterLabel, filter, filterButton, filterClearButton, addButton, removeButton, optionsButton);
		
		GUIUtils.autoResizeColWidth(technologies, technologiesModel);
		
		return top;
	}
	/** Create the options popup menu. */
	void createOptions() {
		
	}
	/** Add a new entry. */
	void doAdd() {
		doClearFilter();
		int idx = technologiesModel.getRowCount();
		technologiesModel.add(new XElement("item"));
		idx = technologies.convertRowIndexToView(idx);
		technologies.getSelectionModel().clearSelection();
		technologies.getSelectionModel().addSelectionInterval(idx, idx);
		technologies.scrollRectToVisible(technologies.getCellRect(idx, 0, true));
	}
	/** Remove the selected entries. */
	void doRemove() {
		technologiesModel.delete(GUIUtils.convertSelectionToModel(technologies));
		doUpdateCount();
	}
	/** Show the popup menu. */
	void doOptions() {
		optionsMenu.show(optionsButton, 0, optionsButton.getHeight());
	}
	/** Clear the filter. */
	void doClearFilter() {
		filter.setSelectedItem("");
		doFilter();
	}
	/** Apply the filter. */
	void doFilter() {
		final String filterStr = ((String)filter.getSelectedItem()).trim();
		
		if (filterStr.isEmpty()) {
			technologiesSorter.setRowFilter(null);
		} else {
			GUIUtils.addFirstItem(filter, filterStr);
			final List<Pair<String, Pattern>> parsed = CETools.parseFilter(filterStr);
			CEXRowFilter rf = new CEXRowFilter() {
				@Override
				public boolean include(XElement item, Object[] displayValues, int index) {
					return checkFilter(item, displayValues, parsed);
				}
			};
			technologiesSorter.setRowFilter(rf);
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
	boolean checkFilter(XElement item, Object[] displayValues, List<Pair<String, Pattern>> filter) {
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
				if ("id".equals(key)) {
					if (!checkFilterValue(displayValues[1], e.second)) {
						return false;
					}
				} else
				if ("name".equals(key)) {
					if (!checkFilterValue(displayValues[2], e.second)) {
						return false;
					}
				} else
				if ("category".equals(key)) {
					if (!checkFilterValue(displayValues[3], e.second)) {
						return false;
					}
				} else
				if ("races".equals(key)) {
					if (!checkFilterValue(displayValues[5], e.second)) {
						return false;
					}
				} else
				if ("valid".equals(key)) {
					String value = "ok";
					ImageIcon ico = (ImageIcon)displayValues[8];
					if (ico == context.getIcon(CESeverityIndicator.WARNING)) {
						value = "warning";
					} else
					if (ico == context.getIcon(CESeverityIndicator.WARNING)) {
						value = "error";
					}
					if (!checkFilterValue(value, e.second)) {
						return false;
					}
				} else
				if (key.startsWith("@")) {
					if (!checkFilterValue(item.get(key.substring(1), null), e.second)) {
						return false;
					}
				} else {
					if (!checkFilterValue(item.get(key, null), e.second)) {
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
	@Override
	public void loadPreferences(XElement preferences) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void savePreferences(XElement preferences) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void restoreState(XElement state) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void locateProblem(XElement description) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public String preferencesId() {
		return "TechnologiesPanel";
	}
	/**
	 * Return the current validity of the given item.
	 * @param item the item
	 * @return the indicator
	 */
	CESeverityIndicator getValidity(XElement item) {
		return CESeverityIndicator.WARNING; // FIXME 
	}
	/** @return construct the filter help. */
	String createFilterHelp() {
		StringBuilder b = new StringBuilder();
		
		b.append("<html>");
		b.append(get("tech.filter.intro")).append("<br>");
		b.append(get("tech.filter.format")).append("<br>");
		b.append("<table border='1'>");
		b.append("<tr><td>").append(get("tech.filter.key")).append("</td><td>").append(get("tech.filter.desc")).append("</td></tr>");
		for (String key : new String[] { "id", "name", "category", "level", "race", "races", "production-cost", "research-cost", "valid" }) {
			b.append("<tr><td>").append(key).append("</td><td>").append(get("tech.filter.key." + key)).append("</td></tr>");
		}
		b.append("</table>");
		
		return b.toString();
	}
	/**
	 * Update the total/filtered count.
	 */
	void doUpdateCount() {
		String filterStr = (String)filter.getSelectedItem();
		if (filterStr == null || filterStr.isEmpty()) {
			counts.setText(String.format("%d", technologiesModel.getRowCount()));
		} else {
			counts.setText(String.format("%d / %d", technologies.getRowCount(), technologiesModel.getRowCount()));
		}
	}
	/** @return the bottom panel. */
	JPanel createBottomPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		details = new JTabbedPane();
		
		details.addTab(get("General"), createGeneralPanel());
		details.addTab(get("Labels"), createLabelsPanel());
		details.addTab(get("Graphics"), createGraphicsPanel());
		details.addTab(get("Videos"), createVideosPanel());
		details.addTab(get("Slots"), createSlotsPanel());
		details.addTab(get("Properties"), createPropertiesPanel());
		
		panel.add(details, BorderLayout.CENTER);
		
		return panel;
	}
	/**
	 * @return
	 */
	Component createPropertiesPanel() {
		JPanel panel = new JPanel();
		GroupLayout gl = new GroupLayout(panel);
		panel.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		// TODO Auto-generated method stub
		return panel;
	}
	/**
	 * @return
	 */
	Component createSlotsPanel() {
		JPanel panel = new JPanel();
		GroupLayout gl = new GroupLayout(panel);
		panel.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		// TODO Auto-generated method stub
		return panel;
	}
	/**
	 * @return
	 */
	Component createVideosPanel() {
		JPanel panel = new JPanel();
		GroupLayout gl = new GroupLayout(panel);
		panel.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		// TODO Auto-generated method stub
		return panel;
	}
	/**
	 * @return
	 */
	Component createGraphicsPanel() {
		JPanel panel = new JPanel();
		GroupLayout gl = new GroupLayout(panel);
		panel.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		// TODO Auto-generated method stub
		return panel;
	}
	/**
	 * @return
	 */
	Component createLabelsPanel() {
		JPanel panel = new JPanel();
		GroupLayout gl = new GroupLayout(panel);
		panel.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		// TODO Auto-generated method stub
		return panel;
	}
	/**
	 * @return the general panel
	 */
	Component createGeneralPanel() {
		JPanel panel = new JPanel();
		GroupLayout gl = new GroupLayout(panel);
		panel.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		// TODO Auto-generated method stub
		
		idField = new CEValueBox<JTextField>(get("tech.id"), new JTextField());

		JComboBox<String> category = new JComboBox<String>();
		for (ResearchSubCategory cat : ResearchSubCategory.values()) {
			category.addItem(get(cat.toString()));
		}
		categoryField = new CEValueBox<JComboBox<String>>(get("tech.category"), category); 
		
		JComboBox<String> factory = new JComboBox<String>(new String[] {
				get("SPACESHIP"), get("EQUIPMENT"), get("WEAPON"), get("BUILDING")
		});
		factoryField = new CEValueBox<JComboBox<String>>(get("tech.factory"), factory);

		JTextField index = new JTextField(2);
		index.setHorizontalAlignment(JTextField.RIGHT);
		
		indexField = new CEValueBox<JTextField>(get("tech.index"), index);
		
		
		// ------------------------------------------------------

		gl.setHorizontalGroup(
			gl.createParallelGroup()
			.addComponent(idField)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(categoryField)
				.addComponent(factoryField)
				.addComponent(indexField)
			)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(idField)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(categoryField)
				.addComponent(factoryField)
				.addComponent(indexField)
			)
		);
		
		return panel;
	}
	/**
	 * Show the details of an item. If null, all fields should be emptied/disabled as necessary.
	 * @param item the item
	 */
	void doDetails(XElement item) {
		this.selected = item;
		// TODO implement
		if (item != null) {
			
			String xid = item.get("id", "");
			idField.component.setText(xid);
			
			String xcategory = item.get("category", "");
			List<String> ecategories = U.newArrayList();
			for (ResearchSubCategory cat : ResearchSubCategory.values()) {
				ecategories.add(cat.toString());
			}
			categoryField.component.setSelectedIndex(ecategories.indexOf(xcategory));
			
			String xfactory = item.get("factory", "");
			factoryField.component.setSelectedIndex(Arrays.asList("spaceship", "equipment", "weapon", "building").indexOf(xfactory));
			
			String xindex = item.get("index", "");
			indexField.component.setText(xindex);
			
			doValidate();
		} else {
			doClearValidation();
		}
	}
	/**
	 * Validate the contents of the fields and append explanation text to the icon.
	 */
	void doValidate() {
		doClearValidation();
		
		String id = idField.component.getText();
		if (id.isEmpty()) {
			idField.setInvalid(errorIcon, get("tech.invalid.empty_identifier"));
		} else {
			for (XElement e : technologiesModel.items) {
				if (e != selected && e.get("id", "").equals(id)) {
					idField.setInvalid(warningIcon, get("tech.invalid.duplicate_identifier"));
				}
			}
		}
		if (categoryField.component.getSelectedIndex() < 0) {
			categoryField.setInvalid(errorIcon, get("tech.invalid.select_category"));
		}
		if (factoryField.component.getSelectedIndex() < 0) {
			factoryField.setInvalid(errorIcon, get("tech.invalid.select_factory"));
		}
		
	}
	/** Remove all validation markers from the panels. */
	void doClearValidation() {
		idField.clearInvalid();
	}
}
