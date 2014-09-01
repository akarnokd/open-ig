/*
 * Copyright 2008-2014, David Karnok 
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
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
		
		JLabel lblID = new JLabel("Slot ID");
		JLabel lblX = new JLabel("Slot X");
		JLabel lblY = new JLabel("Slot Y");
		JLabel lblW = new JLabel("Slot Width");
		JLabel lblH = new JLabel("Slot Height");
		
		txID = new JLabel();
		txX = new JSpinner();
		txY = new JSpinner();
		txW = new JSpinner();
		txH = new JSpinner();
		
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
				.addComponent(render, 298, 298, 298)
				.addGroup(
					gl.createParallelGroup()
					.addComponent(lblID)
					.addComponent(lblX)
					.addComponent(lblY)
					.addComponent(lblW)
					.addComponent(lblH)
				)
				.addGroup(
					gl.createParallelGroup()
					.addComponent(txID, 200, 200, 200)
					.addComponent(txX, 50, 50, 50)
					.addComponent(txY, 50, 50, 50)
					.addComponent(txW, 50, 50, 50)
					.addComponent(txH, 50, 50, 50)
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
				)
			)
		);
		
		cbTech.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int idx = cbTech.getSelectedIndex();
				if (idx >= 0) {
					currentSlot = null;
					render.current = techItems.get(idx);
					render.repaint();
				}
				txID.setText("");
				txX.setValue(0);
				txY.setValue(0);
				txW.setValue(0);
				txH.setValue(0);
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
							
							dragMode = e.getButton() == MouseEvent.BUTTON1;
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
				txID.setText("");
				txX.setValue(0);
				txY.setValue(0);
				txW.setValue(0);
				txH.setValue(0);
				render.repaint();
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
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
	/** Loads the tech file. */
	void doLoad() {
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
			if (xe.childElement("slot") != null) {
				techItems.add(xe);
				cbTech.addItem(xe.get("id"));
			}
		}
		cbTech.setSelectedIndex(-1);

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
