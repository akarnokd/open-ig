/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import hu.openig.utils.Exceptions;
import hu.openig.utils.GUIUtils;
import hu.openig.utils.XElement;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
import javax.swing.JTable;
import javax.swing.SwingConstants;
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
	/** The main model. */
	GenericTableModel<XElement> technologiesModel;
	/** The options menu. */
	JPopupMenu optionsMenu;
	/** The options button. */
	JButton optionsButton;
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
		technologies.setAutoCreateRowSorter(true);
		
		JPanel top = createTopPanel();
		
		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		split.setTopComponent(top);
		
		setLayout(new BorderLayout());
		add(split, BorderLayout.CENTER);
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
				.addComponent(filter, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(filterButton)
				.addComponent(filterClearButton)
				.addGap(30)
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
				.addComponent(filter, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(filterButton)
				.addComponent(filterClearButton)
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
		int idx = technologiesModel.getRowCount();
		technologiesModel.add(new XElement("item"));
		technologies.getSelectionModel().clearSelection();
		technologies.getSelectionModel().addSelectionInterval(idx, idx);
		technologies.scrollRectToVisible(technologies.getCellRect(idx, 0, true));
	}
	/** Remove the selected entries. */
	void doRemove() {
		technologiesModel.delete(technologies.getSelectedRows());
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
}
