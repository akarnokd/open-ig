/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import hu.openig.core.Action0;
import hu.openig.core.Action1;
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
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;
import javax.swing.text.JTextComponent;
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
	/** The first selected row under edit. */
	XElement selected;
	/** The details panel. */
	JTabbedPane details;
	/** The ID. */
	@CETextAttribute(name = "id")
	CEValueBox<JTextField> idField;
	/** The category field. */
	@CEEnumAttribute(name = "category", enumClass = ResearchSubCategory.class)
	CEValueBox<JComboBox<String>> categoryField;
	/** The factory field. */
	@CETextEnumAttribute(name = "factory", values = { "spaceship", "equipment", "weapon", "building" })
	CEValueBox<JComboBox<String>> factoryField;
	/** The index field. */
	@CETextAttribute(name = "index")
	CEValueBox<JTextField> indexField;
	/** The races. */
	@CETextAttribute(name = "race")
	CEValueBox<JTextField> raceField;
	/** Lab number. */
	@CETextAttribute(name = "civil")
	CEValueBox<JTextField> civilField;
	/** Lab number. */
	@CETextAttribute(name = "mech")
	CEValueBox<JTextField> mechField;
	/** Lab number. */
	@CETextAttribute(name = "comp")
	CEValueBox<JTextField> compField;
	/** Lab number. */
	@CETextAttribute(name = "ai")
	CEValueBox<JTextField> aiField;
	/** Lab number. */
	@CETextAttribute(name = "mil")
	CEValueBox<JTextField> milField;
	/** Production cost. */
	@CETextAttribute(name = "production-cost")
	CEValueBox<JTextField> productionField;
	/** Research cost. */
	@CETextAttribute(name = "research-cost")
	CEValueBox<JTextField> researchField;
	/** Level. */
	@CETextEnumAttribute(name = "level", values = { "0", "1", "2", "3", "4", "5", "6" })
	CEValueBox<JComboBox<String>> levelField;
	/** The total lab count. */
	CEValueBox<JTextField> sumLabs;
	/** Label. */
	@CETextAttribute(name = "name")
	CEValueBox<JTextField> nameField;
	/** Label. */
	JTextArea nameLabel;
	/** Label. */
	@CETextAttribute(name = "long-name")
	CEValueBox<JTextField> longNameField;
	/** Label. */
	JTextArea longNameLabel;
	/** Label. */
	@CETextAttribute(name = "description")
	CEValueBox<JTextField> descField;
	/** Label. */
	JTextArea descLabel;
	/** Image base. */
	@CETextAttribute(name = "image")
	CEValueBox<JTextField> imageField;
	/** Image. */
	CEImageRef imageNormal;
	/** Image. */
	CEImageRef imageInfoAvail;
	/** Image. */
	CEImageRef imageInfoWired;
	/** Image. */
	CEImageRef imageSpacewar;
	/** Image. */
	CEImageRef imageEquipDetails;
	/** Image. */
	CEImageRef imageEquipFleet;
	/** The video field. */
	@CETextAttribute(name = "video")
	CEValueBox<JTextField> videoField;
	/** The normal video. */
	CEVideoRef normalVideo;
	/** The wired video. */
	CEVideoRef wiredVideo;
	/** The slot editor. */
	CESlotEdit slotEdit;
	/** The normal slot image. */
	ImageIcon slotNormal;
	/** The fixed slot image. */
	ImageIcon slotFixed;
	/** The slots model. */
	GenericTableModel<XElement> slotsModel;
	/** The slots table. */
	JTable slots;
	/** The current selected slot. */
	XElement selectedSlot;
	/** Slot property. */
	CEValueBox<JTextField> slotId;
	/** Slot property. */
	CEValueBox<JTextField> slotX;
	/** Slot property. */
	CEValueBox<JTextField> slotY;
	/** Slot property. */
	CEValueBox<JTextField> slotWidth;
	/** Slot property. */
	CEValueBox<JTextField> slotHeight;
	/** Slot property. */
	CEValueBox<JComboBox<String>> slotType;
	/** Slot property. */
	CEValueBox<JTextField> slotCount;
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
				case 8: return context.getIcon(validateItem(item));
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
				if (!e.getValueIsAdjusting()) {
					int idx = technologies.getSelectedRow();
					if (idx >= 0) {
						idx = technologies.convertRowIndexToModel(idx);
						doDetails(technologiesModel.get(idx), idx);
					} else {
						doDetails(null, -1);
					}
				}
			}
		});
		
		
		JPanel top = createTopPanel();
		
		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		split.setTopComponent(top);
		
		JPanel bottom = createBottomPanel();
		
		split.setBottomComponent(bottom);
		split.setResizeWeight(0.75);
		
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
	CESeverityIndicator validateItem(XElement item) {
		CESeverityIndicator result = CESeverityIndicator.OK;
		
		if (item.get("id", "").isEmpty()) {
			result = U.max(result, CESeverityIndicator.ERROR);
		}
		if (item.get("category", "").isEmpty()) {
			result = U.max(result, CESeverityIndicator.ERROR);
		}
		if (item.get("factory", "").isEmpty()) {
			result = U.max(result, CESeverityIndicator.ERROR);
		}
		if (item.get("level", "").isEmpty()) {
			result = U.max(result, CESeverityIndicator.ERROR);
		}
		if (item.get("production-cost", "").isEmpty()) {
			result = U.max(result, CESeverityIndicator.ERROR);
		}
		if (item.get("research-cost", "").isEmpty()) {
			result = U.max(result, CESeverityIndicator.ERROR);
		}
		if (item.get("index", "").isEmpty()) {
			result = U.max(result, CESeverityIndicator.ERROR);
		}
		String races = item.get("race", "");
		if (races.isEmpty()) {
			result = U.max(result, CESeverityIndicator.ERROR);
		}
		if (item.get("image", "").isEmpty()) {
			result = U.max(result, CESeverityIndicator.ERROR);
		}
		String video = item.get("video", "");
		if (video.isEmpty()) {
			if (races.contains(context.mainPlayerRace())) {
				result = U.max(result, CESeverityIndicator.ERROR);
			} else {
				result = U.max(result, CESeverityIndicator.WARNING);
			}
		} else {
			if (!context.exists(video + ".ani.gz")) {
				if (races.contains(context.mainPlayerRace())) {
					result = U.max(result, CESeverityIndicator.ERROR);
				} else {
					result = U.max(result, CESeverityIndicator.WARNING);
				}
			}
			if (!context.exists(video + "_wired.ani.gz")) {
				if (races.contains(context.mainPlayerRace())) {
					result = U.max(result, CESeverityIndicator.ERROR);
				} else {
					result = U.max(result, CESeverityIndicator.WARNING);
				}
			}
		}
		
		int sumLab = 0;
		try {
			sumLab += item.getInt("civil", 0);
		} catch (NumberFormatException ex) {
			result = U.max(result, CESeverityIndicator.ERROR);
		}
		try {
			sumLab += item.getInt("mech", 0);
		} catch (NumberFormatException ex) {
			result = U.max(result, CESeverityIndicator.ERROR);
		}
		try {
			sumLab += item.getInt("comp", 0);
		} catch (NumberFormatException ex) {
			result = U.max(result, CESeverityIndicator.ERROR);
		}
		try {
			sumLab += item.getInt("ai", 0);
		} catch (NumberFormatException ex) {
			result = U.max(result, CESeverityIndicator.ERROR);
		}
		try {
			sumLab += item.getInt("mil", 0);
		} catch (NumberFormatException ex) {
			result = U.max(result, CESeverityIndicator.ERROR);
		}
		if (sumLab == 0) {
			if (races.contains(context.mainPlayerRace())) {
				result = U.max(result, CESeverityIndicator.ERROR);
			} else {
				result = U.max(result, CESeverityIndicator.WARNING);
			}
		}
		
		return result; 
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
	 * @return create the properties panel
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
	 * @return creates the slots panel
	 */
	Component createSlotsPanel() {
		JPanel panel = new JPanel();
		GroupLayout gl = new GroupLayout(panel);
		panel.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);

		slotId = CEValueBox.of(get("slot.id"), new JTextField());
		slotX = CEValueBox.of(get("slot.x"), new JTextField());
		slotY = CEValueBox.of(get("slot.y"), new JTextField());
		slotWidth = CEValueBox.of(get("slot.width"), new JTextField());
		slotHeight = CEValueBox.of(get("slot.height"), new JTextField());
		JComboBox<String> slotTypeBox = new JComboBox<String>(new String[] {
				get("slot.type.normal"), get("slot.type.fixed")
		});
		slotType = CEValueBox.of(get("slot.type"), slotTypeBox);
		slotCount = CEValueBox.of(get("slot.count"), numberField());
		
		slotNormal = createColorImage(16, 16, 0xFF00FF00);
		slotFixed = createColorImage(16, 16, 0xFF808080);
		
		slotsModel = new GenericTableModel<XElement>() {
			/** */
			private static final long serialVersionUID = 2557373261832556243L;

			@Override
			public Object getValueFor(XElement item, int rowIndex, int columnIndex) {
				switch (columnIndex) {
				case 0: return item.name.equals("slot-fixed") ? slotFixed : slotNormal;
				case 1: return item.get("id", "");
				case 2: return item.name.equals("slot-fixed") ? item.get("count", "") : item.get("max", "");
				case 3: return item.name.equals("slot-fixed") ? item.get("item", "") : item.get("items", "");
				case 4: return validSlot(item);
				default:
					return null;
				}
			}
		};
		slotsModel.setColumnNames(
			"", get("slot.id"), get("slot.count_max"), get("slot.items"), ""
		);
		slotsModel.setColumnTypes(
			ImageIcon.class, String.class, Integer.class, String.class, ImageIcon.class
		);
		
		JButton addSlot = new JButton(icon("/hu/openig/gfx/plus16.png"));
		JButton removeSlot = new JButton(icon("/hu/openig/gfx/minus16.png"));

		addSlot.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doAddSlot();
			}
		});
		removeSlot.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doRemoveSlot();
			}
		});

		slots = new JTable(slotsModel);
		slots.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int idx = slots.getSelectedRow();
					if (idx >= 0) {
						idx = slots.convertRowIndexToModel(idx);
						doSelectSlot(slotsModel.get(idx));
					}
				}
			}
		});
		
		JScrollPane slotScroll = new JScrollPane(slots);
		
		slotEdit = new CESlotEdit();
		slotEdit.onSlotAdded = new Action1<XElement>() {
			@Override
			public void invoke(XElement value) {
				doAddSlot(value);
			}
		};
		slotEdit.onSlotRemoved = new Action1<XElement>() {
			@Override
			public void invoke(XElement value) {
				doRemoveSlot(value);
			}
		};
		slotEdit.onSlotSelected = new Action1<XElement>() {
			@Override
			public void invoke(XElement value) {
				doSelectSlotInTable(value);
				doSelectSlot(value);
			}
		};
		
		addTextChanged(slotId.component, new Action1<Object>() {
			@Override
			public void invoke(Object value) {
				
			}
		});
		
		doSelectSlot(null);
		
		// -----------------------------------------------------------------------------------
		
		gl.setHorizontalGroup(
			gl.createParallelGroup()
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(slotEdit, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(slotScroll)
				.addGroup(
					gl.createParallelGroup()
					.addComponent(addSlot)
					.addComponent(removeSlot)
				)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addGroup(
					gl.createParallelGroup()
					.addGroup(
						gl.createSequentialGroup()
						.addComponent(slotId)
						.addComponent(slotType)
						.addComponent(slotCount)
					)
					.addGroup(
						gl.createSequentialGroup()
						.addComponent(slotX)
						.addComponent(slotY)
						.addComponent(slotWidth)
						.addComponent(slotHeight)
					)
				)
			)
		);
		
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup()
				.addComponent(slotEdit, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(slotScroll)
				.addGroup(
					gl.createSequentialGroup()
					.addComponent(addSlot)
					.addComponent(removeSlot)
				)
			)
			.addGroup(
				gl.createParallelGroup()
				.addGroup(
					gl.createSequentialGroup()
					.addGroup(
						gl.createParallelGroup(Alignment.BASELINE)
						.addComponent(slotId)
						.addComponent(slotType)
						.addComponent(slotCount)
					)
					.addGroup(
						gl.createParallelGroup(Alignment.BASELINE)
						.addComponent(slotX)
						.addComponent(slotY)
						.addComponent(slotWidth)
						.addComponent(slotHeight)
					)
				)
			)
		);
		
		// TODO Auto-generated method stub
		return panel;
	}
	/**
	 * @return create the videos panel
	 */
	Component createVideosPanel() {
		JPanel panel = new JPanel();
		GroupLayout gl = new GroupLayout(panel);
		panel.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		videoField = CEValueBox.of(get("tech.video"), new JTextField());
		
		normalVideo = new CEVideoRef(get("tech.video.normal"));
		wiredVideo = new CEVideoRef(get("tech.video.wired"));
		
		addTextChanged(videoField.component, new Action1<Object>() {
			@Override
			public void invoke(Object value) {
				setVideos();
			}
		});
		
		// --------------------------------------------------
		
		int imageSize = 100;
		
		gl.setHorizontalGroup(
			gl.createParallelGroup()
			.addComponent(videoField)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(normalVideo.image, imageSize, imageSize, imageSize)
				.addComponent(normalVideo.valid)
				.addComponent(normalVideo.label)
				.addComponent(normalVideo.path)
				.addGap(30)
				.addComponent(wiredVideo.image, imageSize, imageSize, imageSize)
				.addComponent(wiredVideo.valid)
				.addComponent(wiredVideo.label)
				.addComponent(wiredVideo.path)
			)
		);
		
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(videoField)
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(normalVideo.image, imageSize, imageSize, imageSize)
				.addComponent(normalVideo.valid)
				.addComponent(normalVideo.label)
				.addComponent(normalVideo.path, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(wiredVideo.image, imageSize, imageSize, imageSize)
				.addComponent(wiredVideo.valid)
				.addComponent(wiredVideo.label)
				.addComponent(wiredVideo.path, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
		);
		
		return panel;
	}
	/**
	 * @return create the graphics panel
	 */
	Component createGraphicsPanel() {
		JPanel panel = new JPanel();
		GroupLayout gl = new GroupLayout(panel);
		panel.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);

		imageField = CEValueBox.of(get("tech.image"), new JTextField());
		
		imageNormal = new CEImageRef(get("tech.image.normal"));
		imageInfoAvail = new CEImageRef(get("tech.image.info_available"));
		imageInfoWired = new CEImageRef(get("tech.image.info_wired"));
		imageSpacewar = new CEImageRef(get("tech.image.spacewar"));
		imageEquipDetails = new CEImageRef(get("tech.image.equipment_details"));
		imageEquipFleet = new CEImageRef(get("tech.image.equipment_fleet"));
		
		JButton browse = new JButton(get("browse"));
		browse.setVisible(false);
		
		addTextChanged(imageField.component, new Action1<Object>() {
			@Override
			public void invoke(Object value) {
				setImages();
			}
		});
		
		// -----------------------------------------------
		
		int imageSize = 75;
		
		gl.setHorizontalGroup(
			gl.createParallelGroup()
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(imageField)
				.addComponent(browse)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addGroup(
					gl.createParallelGroup()
					.addComponent(imageNormal.image, imageSize, imageSize, imageSize)
					.addComponent(imageInfoAvail.image, imageSize, imageSize, imageSize)
					.addComponent(imageInfoWired.image, imageSize, imageSize, imageSize)
				)
				.addGroup(
					gl.createParallelGroup()
					.addComponent(imageNormal.valid, 20, 20, 20)
					.addComponent(imageInfoAvail.valid, 20, 20, 20)
					.addComponent(imageInfoWired.valid, 20, 20, 20)
				)
				.addGroup(
					gl.createParallelGroup()
					.addComponent(imageNormal.label)
					.addComponent(imageInfoAvail.label)
					.addComponent(imageInfoWired.label)
				)
				.addGroup(
					gl.createParallelGroup()
					.addComponent(imageNormal.path)
					.addComponent(imageInfoAvail.path)
					.addComponent(imageInfoWired.path)
				)
				.addGap(30)
				.addGroup(
					gl.createParallelGroup()
					.addComponent(imageSpacewar.image, imageSize, imageSize, imageSize)
					.addComponent(imageEquipDetails.image, imageSize, imageSize, imageSize)
					.addComponent(imageEquipFleet.image, imageSize, imageSize, imageSize)
				)
				.addGroup(
					gl.createParallelGroup()
					.addComponent(imageSpacewar.valid, 20, 20, 20)
					.addComponent(imageEquipDetails.valid, 20, 20, 20)
					.addComponent(imageEquipFleet.valid, 20, 20, 20)
				)
				.addGroup(
					gl.createParallelGroup()
					.addComponent(imageSpacewar.label)
					.addComponent(imageEquipDetails.label)
					.addComponent(imageEquipFleet.label)
				)
				.addGroup(
					gl.createParallelGroup()
					.addComponent(imageSpacewar.path)
					.addComponent(imageEquipDetails.path)
					.addComponent(imageEquipFleet.path)
				)
			)
		);
		
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(imageField)
				.addComponent(browse)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(imageNormal.image, imageSize, imageSize, imageSize)
				.addComponent(imageNormal.valid)
				.addComponent(imageNormal.label)
				.addComponent(imageNormal.path, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(imageSpacewar.image, imageSize, imageSize, imageSize)
				.addComponent(imageSpacewar.valid)
				.addComponent(imageSpacewar.label)
				.addComponent(imageSpacewar.path, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(imageInfoAvail.image, imageSize, imageSize, imageSize)
				.addComponent(imageInfoAvail.valid)
				.addComponent(imageInfoAvail.label)
				.addComponent(imageInfoAvail.path, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(imageEquipDetails.image, imageSize, imageSize, imageSize)
				.addComponent(imageEquipDetails.valid)
				.addComponent(imageEquipDetails.label)
				.addComponent(imageEquipDetails.path, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(imageInfoWired.image, imageSize, imageSize, imageSize)
				.addComponent(imageInfoWired.valid)
				.addComponent(imageInfoWired.label)
				.addComponent(imageInfoWired.path, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(imageEquipFleet.image, imageSize, imageSize, imageSize)
				.addComponent(imageEquipFleet.valid)
				.addComponent(imageEquipFleet.label)
				.addComponent(imageEquipFleet.path, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
		);

		JScrollPane sp = new JScrollPane(panel);
		
		sp.getVerticalScrollBar().setUnitIncrement(30);
		sp.getVerticalScrollBar().setBlockIncrement(90);
		
		return sp;
	}
	/**
	 * @return create the labels panel
	 */
	Component createLabelsPanel() {
		JPanel panel = new JPanel();
		GroupLayout gl = new GroupLayout(panel);
		panel.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		nameField = CEValueBox.of(get("tech.name"), new JTextField());
		longNameField = CEValueBox.of(get("tech.longname"), new JTextField());
		descField = CEValueBox.of(get("tech.desc"), new JTextField());

		nameLabel = new JTextArea();
		longNameLabel = new JTextArea();
		descLabel = new JTextArea();

		addTextChanged(nameField.component, new Action1<Object>() {
			@Override
			public void invoke(Object value) {
				setLabels();
			}
		});
		addTextChanged(longNameField.component, new Action1<Object>() {
			@Override
			public void invoke(Object value) {
				setLabels();
			}
		});
		addTextChanged(descField.component, new Action1<Object>() {
			@Override
			public void invoke(Object value) {
				setLabels();
			}
		});

		addTextChanged(nameLabel, new Action1<Object>() {
			@Override
			public void invoke(Object value) {
				context.setLabel(nameField.component.getText(), nameLabel.getText());
				validateLabels();
			}
		});
		addTextChanged(longNameLabel, new Action1<Object>() {
			@Override
			public void invoke(Object value) {
				context.setLabel(longNameField.component.getText(), longNameLabel.getText());
				validateLabels();
			}
		});
		addTextChanged(descLabel, new Action1<Object>() {
			@Override
			public void invoke(Object value) {
				context.setLabel(descField.component.getText(), descLabel.getText());
				validateLabels();
			}
		});

		
		// --------------------------------------------------------
		
		gl.setHorizontalGroup(
			gl.createParallelGroup()
			.addComponent(nameField)
			.addComponent(nameLabel)
			.addComponent(longNameField)
			.addComponent(longNameLabel)
			.addComponent(descField)
			.addComponent(descLabel)
		);
		
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(nameField)
			.addComponent(nameLabel)
			.addComponent(longNameField)
			.addComponent(longNameLabel)
			.addComponent(descField)
			.addComponent(descLabel)
		);
		
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
		
		idField = new CEValueBox<JTextField>(get("tech.id"), new JTextField());

		JComboBox<String> category = new JComboBox<String>();
		for (ResearchSubCategory cat : ResearchSubCategory.values()) {
			category.addItem(get(cat.toString()));
		}
		category.setSelectedIndex(-1);
		categoryField = new CEValueBox<JComboBox<String>>(get("tech.category"), category); 
		
		JComboBox<String> factory = new JComboBox<String>(new String[] {
				get("SPACESHIP"), get("EQUIPMENT"), get("WEAPON"), get("BUILDING")
		});
		factory.setSelectedIndex(-1);
		factoryField = new CEValueBox<JComboBox<String>>(get("tech.factory"), factory);

		indexField = new CEValueBox<JTextField>(get("tech.index"), numberField());
		
		productionField = CEValueBox.of(get("tech.production_cost"), numberField());
		researchField = CEValueBox.of(get("tech.research_cost"), numberField());
		
		JComboBox<String> level = new JComboBox<String>(new String[] {
				get("tech.level.0"), get("tech.level.1"), get("tech.level.2"),
				get("tech.level.3"), get("tech.level.4"), get("tech.level.5"),
				get("tech.level.6")
		});
		level.setSelectedIndex(-1);
		levelField = CEValueBox.of(get("tech.level"), level);
		
		raceField = CEValueBox.of(get("tech.race"), new JTextField());
		
		JLabel labsLabel = new JLabel(get("tech.labs"));
		
		civilField = CEValueBox.of(get("tech.lab.civil"), numberField());
		mechField = CEValueBox.of(get("tech.lab.mech"), numberField());
		compField = CEValueBox.of(get("tech.lab.comp"), numberField());
		aiField = CEValueBox.of(get("tech.lab.ai"), numberField());
		milField = CEValueBox.of(get("tech.lab.mil"), numberField());
		
		JTextField sumLabsTF = numberField();
		sumLabsTF.setEditable(false);
		sumLabs = CEValueBox.of(get("tech.lab.sum"), sumLabsTF);
		
		Action1<Object> labValid = new Action1<Object>() {
			@Override
			public void invoke(Object value) {
				setSumLab();
			}
		};
		
		addTextChanged(civilField.component, labValid);
		addTextChanged(mechField.component, labValid);
		addTextChanged(compField.component, labValid);
		addTextChanged(aiField.component, labValid);
		addTextChanged(milField.component, labValid);
		
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
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(researchField)
				.addComponent(productionField)
				.addComponent(levelField)
			)
			.addComponent(raceField)
			.addComponent(labsLabel)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(civilField)
				.addComponent(mechField)
				.addComponent(compField)
				.addComponent(aiField)
				.addComponent(milField)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(sumLabs)
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
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(researchField)
				.addComponent(productionField)
				.addComponent(levelField)
			)
			.addComponent(raceField)
			.addComponent(labsLabel)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(civilField)
				.addComponent(mechField)
				.addComponent(compField)
				.addComponent(aiField)
				.addComponent(milField)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(sumLabs)
			)
		);
		
		return panel;
	}
	/**
	 * Show the details of an item. If null, all fields should be emptied/disabled as necessary.
	 * @param item the item
	 * @param index the model index
	 */
	void doDetails(XElement item, int index) {
		if (selected != null) {
			doStoreDetails(selected);
			int idx = technologiesModel.items.indexOf(selected);
			if (idx >= 0) {
				technologiesModel.fireTableRowsUpdated(idx, idx);
			}
		}
		this.selected = item;
		doSelectSlot(null);
		if (item != null) {
			
			setValueBoxes(item);

			setSumLab();

//			setLabels();
//			setImages();

			setSlots();

			doValidate();
		} else {
			setSlots();
			doClearValidation();
			clearValueBoxes();
		}
	}
	/**
	 * Set the labels.
	 */
	void setLabels() {
		nameLabel.setText(context.label(nameField.component.getText()));
		longNameLabel.setText(context.label(longNameField.component.getText()));
		descLabel.setText(context.label(descField.component.getText()));
		doValidate();
	}
	/**
	 * Set the sum labs.
	 */
	void setSumLab() {
		String l1 = civilField.component.getText();
		String l2 = mechField.component.getText();
		String l3 = compField.component.getText();
		String l4 = aiField.component.getText();
		String l5 = milField.component.getText();
		
		int sumLabCount = 0;
		try {
			sumLabCount += l1.isEmpty() ? 0 : Integer.parseInt(l1);
			sumLabCount += l2.isEmpty() ? 0 : Integer.parseInt(l2);
			sumLabCount += l3.isEmpty() ? 0 : Integer.parseInt(l3);
			sumLabCount += l4.isEmpty() ? 0 : Integer.parseInt(l4);
			sumLabCount += l5.isEmpty() ? 0 : Integer.parseInt(l5);
			sumLabs.component.setText(String.valueOf(sumLabCount));
		} catch (NumberFormatException ex) {
			sumLabs.component.setText("");
		}
	}
	/**
	 * Validate the contents of the fields and append explanation text to the icon.
	 */
	void doValidate() {
		doClearValidation();
		
		ImageIcon generalIcon = null;
		
		String id = idField.component.getText();
		if (id.isEmpty()) {
			idField.setInvalid(errorIcon, get("tech.invalid.empty_identifier"));
			generalIcon = max(generalIcon, errorIcon);
		} else {
			for (XElement e : technologiesModel.items) {
				if (e != selected && e.get("id", "").equals(id)) {
					idField.setInvalid(warningIcon, get("tech.invalid.duplicate_identifier"));
					generalIcon = max(generalIcon, warningIcon);
				}
			}
		}
		if (categoryField.component.getSelectedIndex() < 0) {
			categoryField.setInvalid(errorIcon, get("tech.invalid.select_category"));
			generalIcon = max(generalIcon, errorIcon);
		}
		if (factoryField.component.getSelectedIndex() < 0) {
			factoryField.setInvalid(errorIcon, get("tech.invalid.select_factory"));
			generalIcon = max(generalIcon, errorIcon);
		}

		generalIcon = validateNumberField(indexField, generalIcon, 0, 6, true);

		generalIcon = validateNumberField(productionField, generalIcon, true);
		generalIcon = validateNumberField(researchField, generalIcon, true);
		if (levelField.component.getSelectedIndex() < 0) {
			levelField.setInvalid(errorIcon, get("tech.invalid.select_level"));
			generalIcon = max(generalIcon, errorIcon);
		}
		
		generalIcon = validateNumberField(civilField, generalIcon, false);
		generalIcon = validateNumberField(mechField, generalIcon, false);
		generalIcon = validateNumberField(compField, generalIcon, false);
		generalIcon = validateNumberField(aiField, generalIcon, false);
		generalIcon = validateNumberField(milField, generalIcon, false);

		generalIcon = validateNumberField(sumLabs, generalIcon, 1, Integer.MAX_VALUE, true);
		
		details.setIconAt(0, generalIcon);
		
		// --------------------------------------------------------------------
		
		validateLabels();

		// --------------------------------------------------------------------
		
		generalIcon = null;

		validateImages();
		validateVideos();
	}
	/**
	 * Validate images.
	 */
	public void validateImages() {
		ImageIcon generalIcon = null;
		String image = idField.component.getText();
		if (image.isEmpty()) {
			imageField.setInvalid(errorIcon, get("tech.invalid.empty_image"));
			generalIcon = max(generalIcon, errorIcon);
		}
		
		generalIcon = max(generalIcon, (ImageIcon)imageNormal.valid.getIcon());
		generalIcon = max(generalIcon, (ImageIcon)imageInfoAvail.valid.getIcon());
		generalIcon = max(generalIcon, (ImageIcon)imageInfoWired.valid.getIcon());
		generalIcon = max(generalIcon, (ImageIcon)imageSpacewar.valid.getIcon());
		generalIcon = max(generalIcon, (ImageIcon)imageEquipDetails.valid.getIcon());
		generalIcon = max(generalIcon, (ImageIcon)imageEquipFleet.valid.getIcon());
		
		details.setIconAt(2, generalIcon);
	}
	/**
	 * Validate images.
	 */
	public void validateVideos() {
		ImageIcon generalIcon = null;
		String image = videoField.component.getText();
		if (image.isEmpty()) {
			videoField.setInvalid(errorIcon, get("tech.invalid.empty_video"));
			generalIcon = max(generalIcon, errorIcon);
		}
		
		generalIcon = max(generalIcon, (ImageIcon)normalVideo.valid.getIcon());
		generalIcon = max(generalIcon, (ImageIcon)wiredVideo.valid.getIcon());
		
		details.setIconAt(3, generalIcon);
	}
	/**
	 * Validate the label fields.
	 */
	public void validateLabels() {
		ImageIcon generalIcon;
		generalIcon = null;
		
		generalIcon = validateLabelRefField(nameField, generalIcon);
		generalIcon = validateLabelRefField(longNameField, generalIcon);
		generalIcon = validateLabelRefField(descField, generalIcon);

		details.setIconAt(1, generalIcon);
	}
	/** Remove all validation markers from the panels. */
	void doClearValidation() {
		for (int i = 0; i < details.getTabCount(); i++) {
			details.setIconAt(i, null);
		}
		for (JComponent c : GUIUtils.allComponents(this)) {
			if (c instanceof CEValueBox<?>) {
				CEValueBox<?> ceValueBox = (CEValueBox<?>) c;
				ceValueBox.clearInvalid();
			}
		}
	}
	/**
	 * Save the details.
	 * @param item the target item to update
	 */
	void doStoreDetails(XElement item) {
		getValueBoxes(item);
	}
	/** Set the video images. */
	void setVideos() {
		String videoBase = videoField.component.getText();
		if (videoBase != null && !videoBase.isEmpty()) {
			Action0 act = new Action0() {
				@Override
				public void invoke() {
					validateVideos();
				}
			};
			
			normalVideo.setVideo(videoBase + ".ani.gz", context, act);
			wiredVideo.setVideo(videoBase + "_wired.ani.gz", context, act);
		} else {
			
			normalVideo.error(errorIcon);
			wiredVideo.error(errorIcon);
			validateVideos();
		}
	}
	/**
	 * Set the images.
	 */
	void setImages() {
		String imageBase = imageField.component.getText();
		slotEdit.setImage(null);
		if (imageBase != null && !imageBase.isEmpty()) {
			Action0 act = new Action0() {
				@Override
				public void invoke() {
					validateImages();
				}
			};
			imageNormal.setImage(imageBase + ".png", context, act);
			imageInfoAvail.setImage(imageBase + "_large.png", context, act);
			imageInfoWired.setImage(imageBase + "_wired_large.png", context, act);

			imageSpacewar.clear();
			imageEquipDetails.clear();
			imageEquipFleet.clear();

			int ci = categoryField.component.getSelectedIndex();
			Set<Integer> forSpacewar = U.newHashSet(
					ResearchSubCategory.SPACESHIPS_FIGHTERS.ordinal(),
					ResearchSubCategory.SPACESHIPS_CRUISERS.ordinal(),
					ResearchSubCategory.SPACESHIPS_BATTLESHIPS.ordinal(),
					ResearchSubCategory.SPACESHIPS_STATIONS.ordinal()
			);
			Set<Integer> forEquipment = U.newHashSet(
					ResearchSubCategory.SPACESHIPS_FIGHTERS.ordinal(),
					ResearchSubCategory.SPACESHIPS_CRUISERS.ordinal(),
					ResearchSubCategory.SPACESHIPS_BATTLESHIPS.ordinal(),
					ResearchSubCategory.SPACESHIPS_STATIONS.ordinal(),
					ResearchSubCategory.WEAPONS_TANKS.ordinal(),
					ResearchSubCategory.WEAPONS_VEHICLES.ordinal()
			);

			if (forSpacewar.contains(ci)) {
				imageSpacewar.setImage(imageBase + "_huge.png", context, act);
			}
			if (forEquipment.contains(ci)) {
				Action0 act2 = new Action0() {
					@Override
					public void invoke() {
						validateImages();
						slotEdit.setImage(imageEquipDetails.image.icon);
					}
				};
				imageEquipDetails.setImage(imageBase + "_small.png", context, act2);
				imageEquipFleet.setImage(imageBase + "_tiny.png", context, act);
			}
			
		} else {
			imageNormal.error(errorIcon);
			imageInfoAvail.error(errorIcon);
			imageInfoWired.error(errorIcon);
			imageSpacewar.error(errorIcon);
			imageEquipDetails.error(errorIcon);
			imageEquipFleet.error(errorIcon);
			validateImages();
		}
	}
	/** Set the slots panel. */
	void setSlots() {
		slotEdit.setSlotParent(selected);
		slotsModel.clear();
		if (selected != null) {
			for (XElement xe : selected.children()) {
				if (xe.name.startsWith("slot")) {
					slotsModel.add(xe);
				}
			}
			GUIUtils.autoResizeColWidth(slots, slotsModel);
		}
	}
	/**
	 * Create a colored image.
	 * @param w the width
	 * @param h the height
	 * @param c the fill color
	 * @return the image icon
	 */
	ImageIcon createColorImage(int w, int h, int c) {
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {
				img.setRGB(j, i, c);
			}
		}
		return new ImageIcon(img);
	}
	/** Add a new slot. */
	void doAddSlot() {
		if (selected != null) {
			XElement xslot = selected.add("slot");
			int idx = slotsModel.getRowCount();
			slotsModel.add(xslot);

			idx = slots.convertRowIndexToView(idx);
			slots.getSelectionModel().clearSelection();
			slots.getSelectionModel().addSelectionInterval(idx, idx);
			
			slotEdit.repaint();
		}
	}
	/**
	 * Add the existing slot to the table.
	 * @param xslot the new slot
	 */
	void doAddSlot(XElement xslot) {
		int idx = slotsModel.getRowCount();
		slotsModel.add(xslot);

		idx = slots.convertRowIndexToView(idx);
		slots.getSelectionModel().clearSelection();
		slots.getSelectionModel().addSelectionInterval(idx, idx);
	}
	/**
	 * Remove the slots.
	 */
	void doRemoveSlot() {
		if (selected != null) {
			int[] idxs = GUIUtils.convertSelectionToModel(slots);
			for (int i = 0; i < idxs.length; i++) {
				selected.remove(slotsModel.get(idxs[i]));
			}
			slotsModel.delete(idxs);
			slotEdit.repaint();
			doSelectSlot(null);
		}
	}
	/**
	 * Remove the specified slot from the table.
	 * @param slot the slot to remove
	 */
	void doRemoveSlot(XElement slot) {
		slotsModel.delete(slot);
	}
	/**
	 * Select a specific slot.
	 * @param slot the slot
	 */
	void doSelectSlot(XElement slot) {
		selectedSlot = slot;
		slotEdit.selectedSlot = slot;
		if (selectedSlot != null) {
			setTextAndEnabled(slotId, slot, "id", true);
			slotType.label.setEnabled(true);
			slotType.component.setEnabled(true);
			if ("slot".equals(slot.name)) {
				
				setTextAndEnabled(slotCount, slot, "max", true);
				setTextAndEnabled(slotX, slot, "x", true);
				setTextAndEnabled(slotY, slot, "y", true);
				setTextAndEnabled(slotWidth, slot, "width", true);
				setTextAndEnabled(slotHeight, slot, "height", true);
				
				slotType.component.setSelectedIndex(0);
			} else
			if ("slot-fixed".equals(slot.name)) {
				setTextAndEnabled(slotCount, slot, "count", true);
				setTextAndEnabled(slotX, null, "", false);
				setTextAndEnabled(slotY, null, "", false);
				setTextAndEnabled(slotWidth, null, "", false);
				setTextAndEnabled(slotHeight, null, "", false);
				slotType.component.setSelectedIndex(1);
			} else {
				setTextAndEnabled(slotCount, null, "", false);
				setTextAndEnabled(slotX, null, "", false);
				setTextAndEnabled(slotY, null, "", false);
				setTextAndEnabled(slotWidth, null, "", false);
				setTextAndEnabled(slotHeight, null, "", false);
				slotType.component.setSelectedIndex(-1);
			}
			
		} else {
			setTextAndEnabled(slotId, null, "", false);
			setTextAndEnabled(slotCount, null, "", false);
			setTextAndEnabled(slotX, null, "", false);
			setTextAndEnabled(slotY, null, "", false);
			setTextAndEnabled(slotWidth, null, "", false);
			setTextAndEnabled(slotHeight, null, "", false);
			slotType.label.setEnabled(false);
			slotType.component.setEnabled(false);
			slotType.component.setSelectedIndex(-1);
		}
		slotEdit.repaint();
	}
	/**
	 * Sets the text content of the value box and enables/disables it.
	 * @param c the value box
	 * @param item the item, if null, the attribute contains the exact text to set
	 * @param attribute the attribute
	 * @param enabled true if enabled
	 */
	void setTextAndEnabled(CEValueBox<? extends JTextComponent> c, XElement item, String attribute, boolean enabled) {
		c.component.setText(item != null ? item.get(attribute, "") : attribute);
		c.component.setEnabled(enabled);
		c.label.setEnabled(enabled);
		
	}
	/**
	 * Check if the slot is valid.
	 * @param slot the slot XML
	 * @return the error indicator
	 */
	ImageIcon validSlot(XElement slot) {
		return null;
	}
	/**
	 * Select a specific slot in the table.
	 * @param slot the slot
	 */
	void doSelectSlotInTable(XElement slot) {
		slots.getSelectionModel().clearSelection();
		int idx = slotsModel.items.indexOf(slot);
		if (idx >= 0) {
			idx = slots.convertRowIndexToView(idx);
			slots.getSelectionModel().setSelectionInterval(idx, idx);
		}
	}
	
}
