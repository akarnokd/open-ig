/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import hu.openig.model.Parameters;
import hu.openig.utils.Exceptions;
import hu.openig.utils.U;
import hu.openig.utils.XElement;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

/**
 * The main definition panel.
 * @author akarnokd, 2012.12.17.
 */
public class CEDefinitionPanel extends JPanel implements CEPanelPreferences {
	/** */
	private static final long serialVersionUID = 3418547993103195127L;
	/** The context. */
	protected CEContext ctx;
	/** The element names. */
	protected static final String[] ELEMENT_NAMES = {
		"intro", "image", "battle", "bridge", "buildings",
		"diplomacy", "galaxy", "planets", "players",
		"talks", "tech", "test", "walks", "scripting",
		"chats", "spies"
	};
	/** The parameter names. */
	protected static final String[] PARAM_NAMES = {
		"groundRadarUnitSize", "fleetRadarUnitSize", "researchSpeed", 
		"productionUnit", "constructionSpeed", "constructionCost",
		"repairSpeed", "repairCost", "costToHitpoints", 
		"nearbyDistance", "simulationRatio", "fleetSpeed",
		"stationLimit", "battleshipLimit", "mediumshipLimit", 
		"fighterLimit", "radarShareLimit"
	};
	/** Campaign field. */
	JComboBox<String> levelField;
	/** Campaign field. */
	JComboBox<String> techLevelField;
	/** The tabs. */
	JTabbedPane tabs;
	/** The campaign's directory. */
	JTextField directory;
	/** The indicators. */
	final Map<String, JLabel> indicators = U.newLinkedHashMap();
	/** The input fields. */
	final Map<String, JTextField> fields = U.newLinkedHashMap();
	/**
	 * Constructor. Initializes the panel.
	 * @param ctx the context
	 */
	public CEDefinitionPanel(CEContext ctx) {
		this.ctx = ctx;
		initComponents();
	}
	/** Initialize the content. */
	void initComponents() {
		
		JLabel directoryLabel = new JLabel(get("definition.Directory"));
		directory = new JTextField();
		directory.setEditable(false);
		JButton openDir = new JButton(get("definition.Directory.Open"));

		JPanel panel2 = new JPanel();
		GroupLayout gl = new GroupLayout(panel2);
		panel2.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		gl.setHorizontalGroup(
			gl.createParallelGroup()
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(directoryLabel)
				.addComponent(directory)
				.addComponent(openDir)
			)
		);
		
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(directoryLabel)
				.addComponent(directory, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(openDir)
			)
		);
		
		tabs = new JTabbedPane();
		tabs.addTab(get("definition.Texts"), createTextsPanel());
		tabs.addTab(get("definition.References"), createReferencesPanel());
		tabs.addTab(get("definition.Properties"), createPropertiesPanel());
		
		setLayout(new BorderLayout());
		add(panel2, BorderLayout.PAGE_START);
		add(tabs, BorderLayout.CENTER);
		
		// ---------------------------------
		
		openDir.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				if (Desktop.isDesktopSupported()) {
					Desktop d = Desktop.getDesktop();
					try {
						d.open(ctx.dataManager().getDefinitionDirectory().getCanonicalFile());
					} catch (IOException e1) {
						Exceptions.add(e1);
					}
				}
			}
		});
	}
	/** @return Create the textual descriptions panel. */
	JPanel createTextsPanel() {
		JPanel p = new JPanel();
		// TODO
		return p;
	}
	/** @return The referenced main data files panel. */
	JPanel createReferencesPanel() {
		JPanel p = new JPanel();
		
		GroupLayout gl = new GroupLayout(p);
		p.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		SequentialGroup rows = gl.createSequentialGroup();
		ParallelGroup col1 = gl.createParallelGroup();
		ParallelGroup col2 = gl.createParallelGroup();
		ParallelGroup col3 = gl.createParallelGroup();

		for (String a : ELEMENT_NAMES) {
			JLabel indicator = new JLabel();
			JLabel caption = new JLabel(get("definition.ref_" + a));
			JTextField textField = new JTextField();
			
			indicators.put(a, indicator);
			fields.put(a, textField);
			
			ParallelGroup pg = gl.createParallelGroup(Alignment.BASELINE);
			
			col1.addComponent(caption);
			col2.addComponent(textField);
			col3.addComponent(indicator, 20, 20, 20);
			
			pg.addComponent(caption);
			pg.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE);
			pg.addComponent(indicator, 20, 20, 20);
			
			rows.addGroup(pg);
		}
		
		gl.setHorizontalGroup(
			gl.createSequentialGroup()
			.addGroup(col1)
			.addGroup(col2)
			.addGroup(col3)
		);
		gl.setVerticalGroup(rows);
		
		return p;
	}
	/**
	 * @return the properties listing panel
	 */
	JPanel createPropertiesPanel() {
		JPanel p = new JPanel();
		
		GroupLayout gl = new GroupLayout(p);
		p.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		SequentialGroup rows = gl.createSequentialGroup();
		ParallelGroup col1 = gl.createParallelGroup();
		ParallelGroup col2 = gl.createParallelGroup();
		ParallelGroup col3 = gl.createParallelGroup();
		
		for (String a : PARAM_NAMES) {
			JLabel indicator = new JLabel();
			JLabel caption = new JLabel(get("definition.refprop_" + a));
			JTextField textField = new JTextField(10);
			textField.setHorizontalAlignment(JTextField.RIGHT);
			caption.setToolTipText(get("definition.refprop_" + a + ".tip"));
			textField.setToolTipText(get("definition.refprop_" + a + ".tip"));
			
			indicators.put(a, indicator);
			fields.put(a, textField);
			
			ParallelGroup pg = gl.createParallelGroup(Alignment.BASELINE);
			
			col1.addComponent(caption);
			col2.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE);
			col3.addComponent(indicator, 20, 20, 20);
			
			pg.addComponent(caption);
			pg.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE);
			pg.addComponent(indicator, 20, 20, 20);
			
			rows.addGroup(pg);
		}
		
		gl.setHorizontalGroup(
			gl.createSequentialGroup()
			.addGroup(col1)
			.addGroup(col2)
			.addGroup(col3)
		);
		gl.setVerticalGroup(rows);

		
		return p;
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
	public String preferencesId() {
		return "definition-panel";
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
	 * Load the fields.
	 */
	public void load() {
		CampaignData cd = ctx.campaignData();
		try {
			directory.setText(ctx.dataManager().getDefinitionDirectory().getCanonicalPath());
		} catch (IOException e1) {
			Exceptions.add(e1);
		}
		for (String a : ELEMENT_NAMES) {
			JTextField tf = fields.get(a);
			tf.setText(cd.def.childValue(a));
		}
		Parameters params = new Parameters(null);
		XElement xparam = cd.def.childElement("parameters");
		if (xparam != null) {
			for (String a : PARAM_NAMES) {
				JTextField tf = fields.get(a);
				String val = xparam.get(a, null);
				if (val == null) {
					try {
						val = Parameters.class.getField(a).get(params).toString();
					} catch (Exception ex) {
						Exceptions.add(ex);
					}
				}
				tf.setText(val);
			}
		}
	}
}
