/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import hu.openig.utils.XElement;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.stream.XMLStreamException;

/**
 * Display and edit the slots of ships.
 * @author akarnokd, 2014 szept. 1
 */
public class SlotEditor extends JFrame {
    /**
     *

     */
    private static final String TECH_XML_FILE = "data/generic/campaign/main/tech.xml";
    /** */
    private static final long serialVersionUID = 8139560177720689047L;
    /** The raw tech XML. */
    XElement techXML;
    /** The items from the combobox. */
    List<XElement> techItems = new ArrayList<>();
    /** The items. */
    JComboBox<String> cbTech;
    /** Renders the equipment image. */
    EquipmentRender render;
    /** The currently selected slot. */
    String currentSlot;
    /** The current copy. */
    XElement clipBoard;
    /** Slot ID. */
    private JLabel txID;
    /** Slot X. */
    private JSpinner txX;
    /** Slot Y. */
    private JSpinner txY;
    /** Slot Width. */
    private JSpinner txW;
    /** Slot Height. */
    private JSpinner txH;
    /** Maximum items. */
    private JSpinner txMax;
    /** Default race. */
    private JTextField txRace;
    /** Skirmish race. */
    private JTextField txSkirmishRace;
    /** Tech level. */
    private JSpinner txLevel;
    /** Tech level. */
    private JSpinner txIndex;
    /** Items. */
    private JTextField txItems;
    /** Prerequisite. */
    private JTextField txRequires;
    /** Lab. */
    private JSpinner txCivil;
    /** Lab. */
    private JSpinner txMech;
    /** Lab. */
    private JSpinner txComp;
    /** Lab. */
    private JSpinner txAI;
    /** Lab. */
    private JSpinner txMil;
    /** Lab. */
    private JSpinner txResearch;
    /** Lab. */
    private JSpinner txProduction;
    /** Indicate that the main loading is in progress. */
    boolean mainLoad;
    /**
     * Constructor, initializes the GUI.
     */
    public SlotEditor() {
        super("Slot editor");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        Container c = getContentPane();

        GroupLayout gl = new GroupLayout(c);
        c.setLayout(gl);
        gl.setAutoCreateContainerGaps(true);
        gl.setAutoCreateGaps(true);

        cbTech = new JComboBox<>();
        render = new EquipmentRender();

        JLabel lblRace = new JLabel("Race");
        JLabel lblSkirmishRace = new JLabel("SkirmishRace");
        JLabel lblLevel = new JLabel("Level");
        JLabel lblIndex = new JLabel("Index");
        JLabel lblRequires = new JLabel("Requires");

        JLabel lblID = new JLabel("Slot ID");
        JLabel lblX = new JLabel("Slot X");
        JLabel lblY = new JLabel("Slot Y");
        JLabel lblW = new JLabel("Slot Width");
        JLabel lblH = new JLabel("Slot Height");
        JLabel lblMax = new JLabel("Max");
        JLabel lblItems = new JLabel("Items");

        txRace = new JTextField();
        txSkirmishRace = new JTextField();
        txLevel = new JSpinner();
        txIndex = new JSpinner();
        txRequires = new JTextField();

        JLabel lblCivil = new JLabel("Civil");
        JLabel lblMech = new JLabel("Mech");
        JLabel lblComp = new JLabel("Comp");
        JLabel lblAI = new JLabel("AI");
        JLabel lblMil = new JLabel("Mil");

        txCivil = new JSpinner();
        txMech = new JSpinner();
        txComp = new JSpinner();
        txAI = new JSpinner();
        txMil = new JSpinner();

        JLabel lblResearch = new JLabel("Research cost");
        JLabel lblProduction = new JLabel("Production cost");

        txResearch = new JSpinner();
        txProduction = new JSpinner();

        txID = new JLabel();
        txX = new JSpinner();
        txY = new JSpinner();
        txW = new JSpinner();
        txH = new JSpinner();
        txMax = new JSpinner();
        txItems = new JTextField();

        JButton btnSave = new JButton("Save");
        JButton btnLoad = new JButton("Load");
        JButton btnCopy = new JButton(new ImageIcon(SlotEditor.class.getResource("/hu/openig/editors/res/Copy16.gif")));
        JButton btnPaste = new JButton(new ImageIcon(SlotEditor.class.getResource("/hu/openig/editors/res/Paste16.gif")));

        doLoad();

        gl.setHorizontalGroup(
            gl.createParallelGroup()
            .addGroup(
                gl.createSequentialGroup()
                .addComponent(cbTech)
                .addComponent(btnLoad)
                .addComponent(btnSave)
                .addGap(20)
                .addComponent(btnCopy)
                .addComponent(btnPaste)
            )
            .addGroup(
                gl.createSequentialGroup()
                .addGroup(
                    gl.createParallelGroup()
                    .addComponent(lblRace)
                    .addComponent(lblSkirmishRace)
                    .addComponent(lblRequires)
                )
                .addGroup(
                    gl.createParallelGroup()
                    .addComponent(txRace, 350, 350, 350)
                    .addComponent(txSkirmishRace, 350, 350, 350)
                    .addComponent(txRequires, 150, 150, 150)
                )
            )
            .addGroup(
                gl.createSequentialGroup()
                .addComponent(lblLevel)
                .addComponent(txLevel, 50, 50, 50)
                .addComponent(lblIndex)
                .addComponent(txIndex, 50, 50, 50)
            )
            .addGroup(
                gl.createSequentialGroup()
                .addComponent(lblCivil)
                .addComponent(txCivil, 50, 50, 50)
                .addComponent(lblMech)
                .addComponent(txMech, 50, 50, 50)
                .addComponent(lblComp)
                .addComponent(txComp, 50, 50, 50)
                .addComponent(lblAI)
                .addComponent(txAI, 50, 50, 50)
                .addComponent(lblMil)
                .addComponent(txMil, 50, 50, 50)
            )
            .addGroup(
                gl.createSequentialGroup()
                .addComponent(lblResearch)
                .addComponent(txResearch, 100, 100, 100)
                .addComponent(lblProduction)
                .addComponent(txProduction, 100, 100, 100)
            )
            .addGroup(
                gl.createSequentialGroup()
                .addComponent(render, 298, 298, 298)
                .addGroup(
                    gl.createParallelGroup()
                    .addComponent(lblID)
                    .addComponent(lblX)
                    .addComponent(lblY)
                    .addComponent(lblW)
                    .addComponent(lblH)
                    .addComponent(lblMax)
                    .addComponent(lblItems)
                )
                .addGroup(
                    gl.createParallelGroup()
                    .addComponent(txID, 200, 200, 200)
                    .addComponent(txX, 50, 50, 50)
                    .addComponent(txY, 50, 50, 50)
                    .addComponent(txW, 50, 50, 50)
                    .addComponent(txH, 50, 50, 50)
                    .addComponent(txMax, 50, 50, 50)
                    .addComponent(txItems, 350, 350, 350)
                )
            )
        );
        gl.setVerticalGroup(
            gl.createSequentialGroup()
            .addGroup(
                gl.createParallelGroup(Alignment.BASELINE)
                .addComponent(cbTech)
                .addComponent(btnLoad)
                .addComponent(btnSave)
                .addComponent(btnCopy)
                .addComponent(btnPaste)
            )
            .addGroup(
                gl.createParallelGroup(Alignment.BASELINE)
                .addComponent(lblRace)
                .addComponent(txRace)
            )
            .addGroup(
                gl.createParallelGroup(Alignment.BASELINE)
                .addComponent(lblSkirmishRace)
                .addComponent(txSkirmishRace)
            )
            .addGroup(
                gl.createParallelGroup(Alignment.BASELINE)
                .addComponent(lblLevel)
                .addComponent(txLevel)
                .addComponent(lblIndex)
                .addComponent(txIndex)
            )
            .addGroup(
                gl.createParallelGroup(Alignment.BASELINE)
                .addComponent(lblRequires)
                .addComponent(txRequires)
            )
            .addGroup(
                gl.createParallelGroup(Alignment.BASELINE)
                .addComponent(lblCivil)
                .addComponent(txCivil)
                .addComponent(lblMech)
                .addComponent(txMech)
                .addComponent(lblComp)
                .addComponent(txComp)
                .addComponent(lblAI)
                .addComponent(txAI)
                .addComponent(lblMil)
                .addComponent(txMil)
            )
            .addGroup(
                gl.createParallelGroup(Alignment.BASELINE)
                .addComponent(lblResearch)
                .addComponent(txResearch)
                .addComponent(lblProduction)
                .addComponent(txProduction)
            )
            .addGroup(
                gl.createParallelGroup()
                .addComponent(render, 128, 128, 128)
                .addGroup(
                    gl.createSequentialGroup()
                    .addGroup(
                        gl.createParallelGroup(Alignment.BASELINE)
                        .addComponent(lblID)
                        .addComponent(txID)
                    )
                    .addGroup(
                        gl.createParallelGroup(Alignment.BASELINE)
                        .addComponent(lblX)
                        .addComponent(txX)
                    )
                    .addGroup(
                        gl.createParallelGroup(Alignment.BASELINE)
                        .addComponent(lblY)
                        .addComponent(txY)
                    )
                    .addGroup(
                        gl.createParallelGroup(Alignment.BASELINE)
                        .addComponent(lblW)
                        .addComponent(txW)
                    )
                    .addGroup(
                        gl.createParallelGroup(Alignment.BASELINE)
                        .addComponent(lblH)
                        .addComponent(txH)
                    )
                    .addGroup(
                        gl.createParallelGroup(Alignment.BASELINE)
                        .addComponent(lblMax)
                        .addComponent(txMax)
                    )
                    .addGroup(
                        gl.createParallelGroup(Alignment.BASELINE)
                        .addComponent(lblItems)
                        .addComponent(txItems)
                    )
                )
            )
        );

        cbTech.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mainLoad) {
                    return;
                }
                int idx = cbTech.getSelectedIndex();
                if (idx >= 0) {
                    currentSlot = null;
                    render.current = techItems.get(idx);

                    txRace.setText(render.current.get("race"));
                    txSkirmishRace.setText(render.current.get("skirmish-race", ""));
                    txLevel.setValue(render.current.getInt("level"));
                    txIndex.setValue(render.current.getInt("index"));
                    txRequires.setText(render.current.get("requires", ""));

                    txCivil.setValue(render.current.getInt("civil", 0));
                    txMech.setValue(render.current.getInt("mech", 0));
                    txComp.setValue(render.current.getInt("comp", 0));
                    txAI.setValue(render.current.getInt("ai", 0));
                    txMil.setValue(render.current.getInt("mil", 0));

                    txResearch.setValue(render.current.getInt("research-cost", 0));
                    txProduction.setValue(render.current.getInt("production-cost", 0));

                    render.repaint();
                }
                clearSlotText();
            }
        });
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    techXML.set("noNamespaceSchemaLocation", null);
                    techXML.set("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
                    techXML.set("xsi:noNamespaceSchemaLocation", "../../schemas/tech.xsd");
                    techXML.save(TECH_XML_FILE);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        txX.addChangeListener(createChangeListener("x"));
        txY.addChangeListener(createChangeListener("y"));
        txW.addChangeListener(createChangeListener("width"));
        txH.addChangeListener(createChangeListener("height"));
        txMax.addChangeListener(createChangeListener("max"));
        txItems.getDocument().addDocumentListener(createTextChangeListener("items", txItems));

        txRace.getDocument().addDocumentListener(createTextChangeListener2("race", txRace, false));
        txSkirmishRace.getDocument().addDocumentListener(createTextChangeListener2("skirmish-race", txSkirmishRace, true));
        txLevel.addChangeListener(createChangeListener2("level", false));
        txIndex.addChangeListener(createChangeListener2("index", false));
        txRequires.getDocument().addDocumentListener(createTextChangeListener2("requires", txRequires, true));

        txCivil.addChangeListener(createChangeListener2("civil", true));
        txMech.addChangeListener(createChangeListener2("mech", true));
        txComp.addChangeListener(createChangeListener2("comp", true));
        txAI.addChangeListener(createChangeListener2("ai", true));
        txMil.addChangeListener(createChangeListener2("mil", true));

        txResearch.addChangeListener(createChangeListener2("research-cost", false));
        txProduction.addChangeListener(createChangeListener2("production-cost", false));

        MouseAdapter ma = new MouseAdapter() {
            boolean dragMode;
            int dragX;
            int dragY;
            @Override
            public void mousePressed(MouseEvent e) {
                if (render.current != null) {
                    for (XElement xslot : render.current.childrenWithName("slot")) {
                        String sid = xslot.get("id");
                        int sx = xslot.getInt("x");
                        int sy = xslot.getInt("y");
                        int sw = xslot.getInt("width");
                        int sh = xslot.getInt("height");

                        if (e.getX() >= sx && e.getX() < sx + sw
                                && e.getY() >= sy && e.getY() < sy + sh
                                ) {
                            currentSlot = sid;

                            txID.setText(sid);
                            txX.setValue(sx);
                            txY.setValue(sy);
                            txW.setValue(sw);
                            txH.setValue(sh);
                            txMax.setValue(xslot.getInt("max"));
                            txItems.setText(xslot.get("items"));

                            dragMode = e.getButton() == MouseEvent.BUTTON3;
                            if (dragMode) {
                                dragX = e.getX() - sx;
                                dragY = e.getY() - sy;
                            }
                            render.repaint();
                            return;
                        }
                    }
                }
                currentSlot = null;
                clearSlotText();
                render.repaint();
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    dragMode = false;
                }
            }
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragMode) {
                    if (render.current != null && currentSlot != null) {
                        for (XElement xslot : render.current.childrenWithName("slot")) {
                            String sid = xslot.get("id");
                            if (Objects.equals(sid, currentSlot)) {
                                int sx = e.getX() - dragX;
                                int sy = e.getY() - dragY;
                                xslot.set("x", sx);
                                xslot.set("y", sy);
                                txX.setValue(sx);
                                txY.setValue(sy);
                                render.repaint();
                                break;
                            }
                        }
                    }
                }
            }
        };
        render.addMouseListener(ma);
        render.addMouseMotionListener(ma);
        setResizable(false);

        btnLoad.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doLoad();
                repaint();
            }
        });
        btnCopy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (render.current != null) {
                    clipBoard = render.current.copy();
                }
            }
        });
        btnPaste.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (render.current != null && clipBoard != null) {
                    for (XElement xslot0 : clipBoard.childrenWithName("slot")) {
                        String id0 = xslot0.get("id");
                        for (XElement xslot1 : render.current.childrenWithName("slot")) {
                            String id1 = xslot1.get("id");
                            if (Objects.equals(id0, id1)) {
                                xslot1.set("x", xslot0.get("x"));
                                xslot1.set("y", xslot0.get("y"));
                                xslot1.set("width", xslot0.get("width"));
                                xslot1.set("height", xslot0.get("height"));
                            }
                        }
                    }
                    render.repaint();
                }

            }
        });
    }
    /** Clears the slot text fields. */
    void clearSlotText() {
        txID.setText("");
        txX.setValue(0);
        txY.setValue(0);
        txW.setValue(0);
        txH.setValue(0);
        txMax.setValue(0);
        txItems.setText("");
    }
    /** Loads the tech file. */
    void doLoad() {
        mainLoad = true;
        try {
            techXML = XElement.parseXML(TECH_XML_FILE);
        } catch (XMLStreamException ex) {
            ex.printStackTrace();
        }

        render.current = null;
        currentSlot = null;
        techItems.clear();
        cbTech.removeAllItems();
        for (XElement xe : techXML.childrenWithName("item")) {
            if (xe.get("category", "").startsWith("SPACESHIP")) {
                techItems.add(xe);
                cbTech.addItem(xe.get("id"));
            }
        }
        cbTech.setSelectedIndex(-1);
        render.current = null;
        currentSlot = null;
        txRace.setText("");
        txSkirmishRace.setText("");
        txLevel.setValue(0);
        txRequires.setText("");
        txIndex.setValue(0);
        txResearch.setValue(0);
        txProduction.setValue(0);
        txCivil.setValue(0);
        txMech.setValue(0);
        txComp.setValue(0);
        txAI.setValue(0);
        txMil.setValue(0);
        mainLoad = false;
    }
    /**
     * Creates a change listener which saves the value into the given field of the current tech slot.
     * @param field the field attribute name
     * @return the change listener
     */
    private ChangeListener createChangeListener(final String field) {
        return new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (render.current != null && currentSlot != null) {
                    for (XElement xslot : render.current.childrenWithName("slot")) {
                        if (currentSlot.equals(xslot.get("id"))) {
                            xslot.set(field, ((JSpinner)e.getSource()).getValue());
                            render.repaint();
                            break;
                        }
                    }

                }
            }
        };
    }
    /**
     * Creates a change listener which saves the value into the given field of the current tech slot.
     * @param field the field attribute name
     * @param tf the parent text field
     * @return the change listener
     */
    private DocumentListener createTextChangeListener(final String field, final JTextField tf) {
        return new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                stateChanged(e);
            }
            @Override
            public void insertUpdate(DocumentEvent e) {
                stateChanged(e);
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                stateChanged(e);
            }
            public void stateChanged(DocumentEvent e) {
                if (render.current != null && currentSlot != null) {
                    for (XElement xslot : render.current.childrenWithName("slot")) {
                        if (currentSlot.equals(xslot.get("id"))) {
                            xslot.set(field, tf.getText());
                            render.repaint();
                            break;
                        }
                    }

                }
            }
        };
    }
    /**
     * Creates a change listener which saves the value into the given field of the current tech slot.
     * @param field the field attribute name
     * @param zeroIsNull store zero as a null value?
     * @return the change listener
     */
    private ChangeListener createChangeListener2(final String field, final boolean zeroIsNull) {
        return new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (render.current != null) {
                    int value = (Integer)(((JSpinner)e.getSource()).getValue());
                    if (value > 0 || !zeroIsNull) {
                        render.current.set(field, value);
                    } else {
                        render.current.set(field, null);
                    }
                }
            }
        };
    }
    /**
     * Creates a change listener which saves the value into the given field of the current tech slot.
     * @param field the field attribute name
     * @param tf the parent text field
     * @param emptyIsNull store a null if the text is empty?
     * @return the change listener
     */
    private DocumentListener createTextChangeListener2(final String field, final JTextField tf, final boolean emptyIsNull) {
        return new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                stateChanged(e);
            }
            @Override
            public void insertUpdate(DocumentEvent e) {
                stateChanged(e);
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                stateChanged(e);
            }
            public void stateChanged(DocumentEvent e) {
                if (render.current != null) {
                    String text = tf.getText();
                    render.current.set(field, text.isEmpty() && emptyIsNull ? null : text);
                }
            }
        };
    }
    /** Renders the technology with the slots. */
    final class EquipmentRender extends JComponent {
        /** */
        private static final long serialVersionUID = 4569061568803989171L;
        /** The current XML element. */
        XElement current;
        /** The image to be displayed. */
        BufferedImage img;
        /** The current image file. */
        String file;
        @Override
        public void paint(Graphics g) {
            if (current != null) {
                String imgFile = current.get("image");
                if (file == null || !file.equals(imgFile)) {
                    file = imgFile;
                    File input = new File("images/generic/" + imgFile + "_small.png");
                    if (!input.canRead()) {
                        input = new File("images/generic/" + imgFile + "_huge.png");
                    }
                    try {
                        img = ImageIO.read(input);
                    } catch (IOException ex) {
                        System.out.println(input);
                        ex.printStackTrace();
                    }
                }
                if (img != null) {
                    BufferedImage image = img;
                    float fx = getWidth() * 1.0f / image.getWidth();
                    float fy = getHeight() * 1.0f / image.getHeight();
                    float f = Math.min(fx, fy);
                    int dx = (int)((getWidth() - image.getWidth() * f) / 2);
                    int dy = (int)((getHeight() - image.getHeight() * f) / 2);
                    g.drawImage(image, dx, dy, (int)(image.getWidth() * f), (int)(image.getHeight() * f), null);
                }
                for (XElement xslot : current.childrenWithName("slot")) {
                    String sid = xslot.get("id");
                    int sx = xslot.getInt("x");
                    int sy = xslot.getInt("y");
                    int sw = xslot.getInt("width");
                    int sh = xslot.getInt("height");
                    if (Objects.equals(sid, currentSlot)) {
                        g.setColor(Color.ORANGE);
                    } else {
                        g.setColor(Color.GREEN);
                    }
                    g.drawRect(sx, sy, sw, sh);
                    g.drawRect(sx + 1, sy + 1, sw - 2, sh - 2);
                }
            } else {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }
    /**
     * Main program, no arguments.
     * @param args no arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SlotEditor frame = new SlotEditor();
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }
}
