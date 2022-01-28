/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import hu.openig.core.Pair;
import hu.openig.model.GameDefinition;
import hu.openig.model.Parameters;
import hu.openig.utils.Exceptions;
import hu.openig.utils.XElement;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * The main definition panel.
 * @author akarnokd, 2012.12.17.
 */
public class CEDefinitionPanel extends CEBasePanel implements CEPanelPreferences {
    /** */
    private static final long serialVersionUID = 3418547993103195127L;
    /** The element names. */
    static final String[] ELEMENT_NAMES = {
        "intro", "image", "battle", "bridge", "buildings",
        "diplomacy", "galaxy", "planets", "players",
        "talks", "tech", "test", "walks", "scripting",
        "chats", "spies"
    };
    /** The parameter names. */
    static final String[] PARAM_NAMES = {
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
    final Map<String, JLabel> indicators = new LinkedHashMap<>();
    /** The input fields. */
    final Map<String, JTextField> fields = new LinkedHashMap<>();
    /** The language fields. */
    final List<JTextField> languageFields = new ArrayList<>();
    /** The language fields. */
    final List<JTextField> titleFields = new ArrayList<>();
    /** The language fields. */
    final List<JTextArea> descriptionFields = new ArrayList<>();
    /** The texts subpanel. */
    JPanel textsSubPanel;
    /**
     * Constructor. Initializes the panel.
     * @param ctx the context
     */
    public CEDefinitionPanel(CEContext ctx) {
        super(ctx);
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
                        d.open(context.dataManager().getDefinitionDirectory().getCanonicalFile());
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
        p.setLayout(new BorderLayout());

        textsSubPanel = new JPanel();
        JScrollPane comp = new JScrollPane(textsSubPanel);
        comp.getVerticalScrollBar().setBlockIncrement(90);
        comp.getVerticalScrollBar().setUnitIncrement(30);
        p.add(comp, BorderLayout.CENTER);

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
     * Update the text fields.
     */
    void updateTexts() {
        textsSubPanel.removeAll();
        GroupLayout gl0 = new GroupLayout(textsSubPanel);
        textsSubPanel.setLayout(gl0);

        languageFields.clear();
        titleFields.clear();
        descriptionFields.clear();

        ParallelGroup hg = gl0.createParallelGroup(Alignment.CENTER);
        SequentialGroup vg = gl0.createSequentialGroup();

        final GameDefinition definition = context.campaignData().definition;
        for (final String lang : definition.languages()) {
            textsSubPanel.add(new JSeparator(JSeparator.HORIZONTAL));

            JLabel langLabel = new JLabel(get("definition_language"));
            JTextField langField = new JTextField(lang, 2);
            JLabel titleLabel = new JLabel(get("definition_title"));
            JTextField titleField = new JTextField(definition.getTitle(lang));
            JLabel descLabel = new JLabel(get("definition_description"));
            JTextArea descField = new JTextArea(definition.getDescription(lang));
            JButton remove = new JButton(icon("/hu/openig/gfx/minus16.png"));
            remove.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    definition.texts.remove(lang);
                    updateTexts();
                }
            });

            languageFields.add(langField);
            titleFields.add(titleField);
            descriptionFields.add(descField);

            JPanel p = new JPanel();
            GroupLayout gl = new GroupLayout(p);
            p.setLayout(gl);
            gl.setAutoCreateContainerGaps(true);
            gl.setAutoCreateGaps(true);

            gl.setHorizontalGroup(
                gl.createSequentialGroup()
                .addGroup(
                    gl.createParallelGroup()
                    .addGroup(
                        gl.createSequentialGroup()
                        .addComponent(langLabel)
                        .addComponent(langField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(30)
                        .addComponent(titleLabel)
                        .addComponent(titleField)
                    )
                    .addGroup(
                        gl.createSequentialGroup()
                        .addComponent(descLabel)
                        .addComponent(descField)
                    )
                )
                .addComponent(remove)
            );
            gl.setVerticalGroup(
                gl.createParallelGroup(Alignment.CENTER)
                .addGroup(
                    gl.createSequentialGroup()
                    .addGroup(
                        gl.createParallelGroup(Alignment.BASELINE)
                        .addComponent(langLabel)
                        .addComponent(langField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(titleLabel)
                        .addComponent(titleField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                    )
                    .addGroup(
                        gl.createParallelGroup(Alignment.BASELINE)
                        .addComponent(descLabel)
                        .addComponent(descField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                    )
                )
                .addComponent(remove)
            );

            JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);

            hg.addComponent(p);
            vg.addComponent(p);

            hg.addComponent(sep);
            vg.addComponent(sep, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE);

        }

        JButton addNew = new JButton(icon("/hu/openig/gfx/plus16.png"));
        addNew.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doAddNewLanguage();
            }
        });

        hg.addComponent(addNew);
        vg.addComponent(addNew);

        gl0.setHorizontalGroup(hg);
        gl0.setVerticalGroup(vg);

    }
    /**
     * Add a new language.
     */
    void doAddNewLanguage() {
        GameDefinition definition = context.campaignData().definition;
        String n = Integer.toString(definition.languages().size() + 1);
        definition.texts.put(n, Pair.of("", ""));
        updateTexts();
    }
    /**
     * Load the fields.
     */
    public void load() {
        CampaignData cd = context.campaignData();
        try {
            directory.setText(context.dataManager().getDefinitionDirectory().getCanonicalPath());
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
        updateTexts();
    }
}
