/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.GroupLayout.Alignment;
import javax.swing.border.TitledBorder;

/**
 * @author karnokd, 2009.09.23.
 * @version $Revision 1.0$
 */
public class Setup extends JFrame {
	/** */
	private static final long serialVersionUID = -2427185313578487251L;
	/** Background image. */
	private BufferedImage background;
	/** The container for the tabs. */
	private JPanel basePanel;
	/** Tab buttons. */
	private List<AbstractButton> tabs = new ArrayList<AbstractButton>();
	/** Panels. */
	private List<JPanel> panels = new ArrayList<JPanel>();
	/** Button group. */
	private ButtonGroup buttonGroup;
	/** The current shown panel. */
	private JPanel currentPanel;
	/** Language panels. */
	private JPanel languagePanel;
	/** The files panel. */
	private JPanel filesPanel;
	/** The graphics panel. */
	private JPanel graphicsPanel;
	/** The audio panel. */
	private JPanel audioPanel;
	/** The cheats. */
	private JPanel cheatsPanel;
	/** The logs panel. */
	private JPanel logsPanel;
	/** Panel group layout. */
	private GroupLayout panelGroupLayout;
	/** Hungarian language. */
	private JRadioButton hungarian;
	/** English language. */
	private JRadioButton english;
	/** Language tab. */
	private ConfigButton btnLanguage;
	/** Resources tab. */
	private ConfigButton btnResources;
	/** Graphics tab. */
	private ConfigButton btnGraphics;
	/** Audio tab. */
	private ConfigButton btnAudio;
	/** Cheats tab. */
	private ConfigButton btnCheats;
	/** Logs tab. */
	private ConfigButton btnLogs;
	/** Save and run. */
	private ConfigButton btnSaveAndRun;
	/** Save. */
	private ConfigButton btnSave;
	/** Close. */
	private ConfigButton btnClose;
	/** Path label. */
	private JLabel pathLabel;
	/** Path. */
	private JTextField path;
	/** Open. */
	private ConfigButton btnOpen;
	/** Add. */
	private ConfigButton btnAdd;
	/** Move up. */
	private ConfigButton btnMoveUp;
	/** Move down. */
	private ConfigButton btnMoveDown;
	/** File list. */
	private JList fileList;
	/** Locate. */
	private ConfigButton btnLocate;
	/** File list label. */
	private JLabel fileListLabel;
	/** Remove. */
	private ConfigButton btnRemove;
	/** The file list model. */
	private DefaultListModel fileListModel;
	/** The last open path. */
	private File lastPath;
	/** Restart needed label. */
	private JLabel lblRestartNeeded;
	/** Disable OpenGL. */
	private JCheckBox cbDisableOpenGL;
	/** Disable DirectDraw. */
	private JCheckBox cbDisableDDraw;
	/** Disable D3D. */
	private JCheckBox cbDisableD3D;
	/** Diagnostic panel. */
	private JPanel gfxDiagnostic;
	/** Left. */
	private JLabel lblLeft;
	/** Top. */
	private JLabel lblTop;
	/** Width. */
	private JLabel lblWidth;
	/** Height. */
	private JLabel lblHeight;
	/** Apply bounds. */
	private ConfigButton btnApplyBounds;
	/** Left editor. */
	private JTextField edLeft;
	/** Top editor. */
	private JTextField edTop;
	/** Width editor. */
	private JTextField edWidth;
	/** Height editor. */
	private JTextField edHeight;
	/** Diagnostic border. */
	private TitledBorder diagnosticBorder;
	/** Game window border. */
	private TitledBorder gameWindowBorder;
	/** Audio channels. */
	private JLabel lblAudioChannels;
	/** Audio channels editor. */
	private JTextField edAudioChannels;
	/** Test audio channels. */
	private ConfigButton testAudioChannels;
	/** Music volume. */
	private JLabel lblMusicVolume;
	/** Test music. */
	private ConfigButton testMusic;
	/** Effect volume. */
	private JLabel lblEffectVolume;
	/** Test effect volume. */
	private ConfigButton testEffects;
	/** Video volume. */
	private JLabel lblVideoVolume;
	/** Test video volume. */
	private ConfigButton testVideo;
	/** Music slider. */
	private JSlider musicSlider;
	/** Effect slider. */
	private JSlider effectSlider;
	/** Video slider. */
	private JSlider videoSlider;
	/**
	 * Constructor. Initializes the GUI elements.
	 */
	public Setup() {
		setTitle("Open Imperium Galactica Setup/Beállítás");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		try {
			background = ImageIO.read(getClass().getResource("/hu/openig/res/setup.png"));
			setIconImage(ImageIO.read(getClass().getResource("/hu/openig/res/open-ig-logo.png")));
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		basePanel = new JPanel() {
			/** */
			private static final long serialVersionUID = 1L;
			@Override
			public void paint(Graphics g) {
				g.drawImage(background, 0, 0, null);
				super.paint(g);
			}
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(background.getWidth(), background.getHeight());
			}
		};
		basePanel.setOpaque(false);
		Container c = getContentPane();
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);
		
		gl.setHorizontalGroup(
			gl.createSequentialGroup()
			.addComponent(basePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(basePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		);
		
	
		createTabs();
		
		setResizable(false);
		pack();
	}
	/**
	 * Create configuration tabs.
	 */
	private void createTabs() {
		panelGroupLayout = new GroupLayout(basePanel);
		basePanel.setLayout(panelGroupLayout);
		
		panelGroupLayout.setAutoCreateContainerGaps(true);
		panelGroupLayout.setAutoCreateGaps(true);
		
		buttonGroup = new ButtonGroup();
		
		btnLanguage = new ConfigButton("Language/Nyelv");
		btnResources = new ConfigButton("Files");
		btnGraphics = new ConfigButton("Graphics");
		btnAudio = new ConfigButton("Audio");
		btnCheats = new ConfigButton("Cheats");
		btnLogs = new ConfigButton("Logs");

		tabs.add(btnLanguage);
		tabs.add(btnResources);
		tabs.add(btnGraphics);
		tabs.add(btnAudio);
		tabs.add(btnCheats);
		tabs.add(btnLogs);
		
		createLanguagePanel();
		createFilesPanel();
		createGraphicsPanel();
		createAudioPanel();
		createCheatsPanel();
		createLogsPanel();
		
		panels.add(languagePanel);
		panels.add(filesPanel);
		panels.add(graphicsPanel);
		panels.add(audioPanel);
		panels.add(cheatsPanel);
		panels.add(logsPanel);
		
		for (int i = 0; i < tabs.size(); i++) {
			final int j = i;
			tabs.get(i).addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { selectPanel(j); } });
		}
		
		btnSaveAndRun = new ConfigButton("Save & Run");
		btnSave = new ConfigButton("Save");
		btnClose = new ConfigButton("Close");
		btnClose.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { dispose(); } });
		
		buttonGroup.add(btnLanguage);
		buttonGroup.add(btnResources);
		buttonGroup.add(btnGraphics);
		buttonGroup.add(btnAudio);
		buttonGroup.add(btnCheats);
		buttonGroup.add(btnLogs);
		
		buttonGroup.setSelected(btnLanguage.getModel(), true);
		
		JSeparator sep0 = new JSeparator();
		JSeparator sep1 = new JSeparator();
		
		currentPanel = languagePanel;
		
		panelGroupLayout.setHorizontalGroup(
			panelGroupLayout.createParallelGroup(Alignment.TRAILING)
			.addGroup(
				panelGroupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(
					panelGroupLayout.createSequentialGroup()
					.addComponent(btnLanguage)
					.addComponent(btnResources)
					.addComponent(btnGraphics)
					.addComponent(btnAudio)
					.addComponent(btnCheats)
					.addComponent(btnLogs)
				)
				.addComponent(sep0)
				.addComponent(currentPanel)
				.addComponent(sep1)
			)
			.addGroup(
				panelGroupLayout.createSequentialGroup()
				.addComponent(btnSaveAndRun)
				.addComponent(btnSave)
				.addComponent(btnClose)
			)
		);
		panelGroupLayout.setVerticalGroup(
			panelGroupLayout.createSequentialGroup()
			.addGroup(
				panelGroupLayout.createParallelGroup(Alignment.BASELINE)
				.addComponent(btnLanguage)
				.addComponent(btnResources)
				.addComponent(btnGraphics)
				.addComponent(btnAudio)
				.addComponent(btnCheats)
				.addComponent(btnLogs)
			)
			.addComponent(sep0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(currentPanel)
			.addComponent(sep1, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGroup(
				panelGroupLayout.createParallelGroup(Alignment.BASELINE)
				.addComponent(btnSaveAndRun)
				.addComponent(btnSave)
				.addComponent(btnClose)
			)
		);
		changeToHungarian();
	}
	/**
	 * Select the panel for the given tab index.
	 * @param i the index
	 */
	protected void selectPanel(int i) {
		buttonGroup.setSelected(tabs.get(i).getModel(), true);
		panelGroupLayout.replace(currentPanel, panels.get(i));
		currentPanel = panels.get(i);
	}
	/**
	 * Special rounded rectangle toggle button.
	 * @author karnokd, 2009.09.23.
	 * @version $Revision 1.0$
	 */
	public class ConfigButton extends JButton {
		/** */
		private static final long serialVersionUID = -2759017088425629378L;
		/**
		 * Constructor. Sets the text.
		 * @param text the title
		 */
		public ConfigButton(String text) {
			super(text);
			setOpaque(false);
		}
		@Override
		public void paint(Graphics g) {
			Graphics2D g2 = (Graphics2D)g;
			g2.setFont(getFont());
			FontMetrics fm = g2.getFontMetrics();
			
			ButtonModel mdl = getModel();
			String s = getText();
			
			g2.setComposite(AlphaComposite.SrcOver.derive(0.85f));
			if (mdl.isPressed() || mdl.isSelected()) {
				if (mdl.isRollover()) {
					g2.setColor(new Color(0xE0E0E0));
				} else {
					g2.setColor(new Color(0xFFFFFF));
				}
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
				g2.setColor(new Color(0x000000));
			} else {
				if (mdl.isRollover()) {
					g2.setColor(new Color(0x000000));
				} else {
					g2.setColor(new Color(0x202020));
				}
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
				g2.setColor(new Color(0xFFFFFFFF));
			}
			int x = (getWidth() - fm.stringWidth(s)) / 2;
			int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent() + fm.getLeading();
			g2.drawString(s, x, y);
		}
	}
	/**
	 * Create language panel.
	 */
	void createLanguagePanel() {
		languagePanel = new JPanel();
		languagePanel.setOpaque(false);
		
		JPanel a = new JPanel();
		a.setOpaque(false);
		languagePanel.setLayout(new BoxLayout(languagePanel, BoxLayout.LINE_AXIS));
		languagePanel.add(Box.createHorizontalGlue());
		languagePanel.add(a);
		languagePanel.add(Box.createHorizontalGlue());
		
		JPanel mid = getAlphaPanel(0.85f, 0x000080);
		a.setLayout(new BoxLayout(a, BoxLayout.LINE_AXIS));
		a.add(Box.createVerticalGlue());
		a.add(mid);
		a.add(Box.createVerticalGlue());
		
		GroupLayout gl = new GroupLayout(mid);
		mid.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		hungarian = new JRadioButton("Magyar", true);
		hungarian.setOpaque(false);
		hungarian.setForeground(Color.WHITE);
		hungarian.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				changeToHungarian();
			}
		});
		
		
		english = new JRadioButton("Angol / English");
		english.setOpaque(false);
		english.setForeground(Color.WHITE);
		english.addActionListener(new ActionListener() {
			/* (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				changeToEnglish();
			}
		});
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(hungarian);
		bg.add(english);
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.LEADING)
			.addComponent(hungarian)
			.addComponent(english)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(hungarian)
			.addComponent(english)
		);
	}
	/**
	 * Create language panel.
	 */
	void createFilesPanel() {
		filesPanel = new JPanel();
		filesPanel.setOpaque(false);
		
		GroupLayout gl = new GroupLayout(filesPanel);
		filesPanel.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		pathLabel = new JLabel("File path:");
		path = new AlphaTextField(0.85f);
		path.setOpaque(false);
		btnOpen = new ConfigButton("Open...");
		btnOpen.addActionListener(new Act() { public void act() { doOpenPath(); } });
		btnLocate = new ConfigButton("Locate");
		btnLocate.addActionListener(new Act() { public void act() { doLocate(); } });
		btnAdd = new ConfigButton("Add");
		btnAdd.addActionListener(new Act() { public void act() { doAddPath(); } });
		btnMoveUp = new ConfigButton("Up");
		btnMoveUp.addActionListener(new Act() { public void act() { doMoveUp(); } });
		btnMoveDown = new ConfigButton("Down");
		btnMoveDown.addActionListener(new Act() { public void act() { doMoveDown(); } });
		btnRemove = new ConfigButton("Remove");
		btnRemove.addActionListener(new Act() { public void act() { doRemove(); } });
		
		fileListLabel = new JLabel("Selected files");
		fileListModel = new DefaultListModel();
		fileList = new JList(fileListModel);
		fileList.setOpaque(false);
		JScrollPane sp = new JScrollPane(fileList) {
			/** */
			private static final long serialVersionUID = 7854292553117423248L;
			@Override
			public void paint(Graphics g) {
				Graphics2D g2 = (Graphics2D)g;
				g2.setComposite(AlphaComposite.SrcOver.derive(0.85f));
				g2.setColor(Color.white);
				super.paint(g);
			}
		};
		sp.setOpaque(false);
		
		gl.setHorizontalGroup(
			gl.createParallelGroup()
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(pathLabel)
				.addComponent(path)
				.addComponent(btnOpen)
				.addComponent(btnLocate)
				.addComponent(btnAdd)
			)
			.addComponent(fileListLabel)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(sp)
				.addGroup(
					gl.createParallelGroup()
					.addComponent(btnMoveUp)
					.addComponent(btnMoveDown)
					.addComponent(btnRemove)
				)
			)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(pathLabel)
				.addComponent(path, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(btnOpen)
				.addComponent(btnLocate)
				.addComponent(btnAdd)
			)
			.addComponent(fileListLabel)
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(sp)
				.addGroup(
					gl.createSequentialGroup()
					.addComponent(btnMoveUp)
					.addComponent(btnMoveDown)
					.addComponent(btnRemove)
				)
			)
		);
		gl.linkSize(SwingConstants.HORIZONTAL, btnMoveUp, btnMoveDown, btnRemove);
	}
	/**
	 * 
	 */
	protected void doRemove() {
		int[] idxs = fileList.getSelectedIndices();
		for (int i = idxs.length - 1; i >= 0; i--) {
			fileListModel.remove(idxs[i]);
		}
		filesPanel.repaint();
	}
	/**
	 * Move the selected items down.
	 */
	protected void doMoveDown() {
		int[] idxs = fileList.getSelectedIndices();
		for (int i = idxs.length - 1; i >= 0; i--) {
			int idx = idxs[i];
			if (idx < fileListModel.size() - 1) {
				Object o = fileListModel.remove(idx);
				fileListModel.add(idx + 1, o);
				idxs[i]++;
			} else {
				break;
			}
		}
		fileList.setSelectedIndices(idxs);
		filesPanel.repaint();
	}
	/**
	 * Move selected items up.
	 */
	protected void doMoveUp() {
		int[] idxs = fileList.getSelectedIndices();
		for (int i = 0; i < idxs.length; i++) {
			int idx = idxs[i];
			if (idx > 0) {
				Object o = fileListModel.remove(idx);
				fileListModel.add(idx - 1, o);
				idxs[i]--;
			} else {
				break;
			}
		}
		fileList.setSelectedIndices(idxs);
		filesPanel.repaint();
	}
	/**
	 * Add current path to the list.
	 */
	protected void doAddPath() {
		if (path.getText().trim().length() > 0) {
			fileListModel.addElement(path.getText().trim());
		}
		filesPanel.repaint();
	}
	/**
	 * Add the standard places for the resource files.
	 */
	protected void doLocate() {
		if (new File("audio").exists()) {
			fileListModel.addElement("audio");
		}
		if (new File("data").exists()) {
			fileListModel.addElement("data");
		}
		if (new File("images").exists()) {
			fileListModel.addElement("images");
		}
		if (new File("video").exists()) {
			fileListModel.addElement("video");
		}
		File[] files = new File(".").listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().startsWith("open-ig-") && name.toLowerCase().endsWith(".zip");
			}
		});
		if (files != null) {
			for (File f : files) {
				fileListModel.addElement(f.getName());
			}
		}
		filesPanel.repaint();
	}
	/**
	 * 
	 */
	protected void doOpenPath() {
		JFileChooser fc = new JFileChooser();
		if (lastPath == null) {
			lastPath = new File(".");
		}
		fc.setCurrentDirectory(lastPath);
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setMultiSelectionEnabled(true);
		
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File[] files = fc.getSelectedFiles();
			if (files != null) {
				for (File f : files) {
					lastPath = f.getParentFile();
					fileListModel.addElement(f.getAbsolutePath());
				}
			}
		}
		filesPanel.repaint();
	}
	/**
	 * Create language panel.
	 */
	void createGraphicsPanel() {
		graphicsPanel = new JPanel();
		graphicsPanel.setOpaque(false);
		
		JPanel a = new JPanel();
		a.setOpaque(false);
		graphicsPanel.setLayout(new BoxLayout(graphicsPanel, BoxLayout.LINE_AXIS));
		graphicsPanel.add(Box.createHorizontalGlue());
		graphicsPanel.add(a);
		graphicsPanel.add(Box.createHorizontalGlue());

		JPanel diagnosticBack = getAlphaPanel(0.85f, 0x000080);
		
		createDiagnosticPanel(diagnosticBack);

		
		JPanel window = getAlphaPanel(0.85f, 0x000080);
		
		createWindowBoundsPanel(window);
		
		a.setLayout(new BoxLayout(a, BoxLayout.PAGE_AXIS));
		a.add(Box.createVerticalGlue());
		a.add(window);
		a.add(Box.createVerticalStrut(5));
		a.add(diagnosticBack);
		a.add(Box.createVerticalGlue());

	}
	/**
	 * Create the diagnostic panel contents.
	 * @param diagnosticBack the backing panel
	 */
	private void createDiagnosticPanel(JPanel diagnosticBack) {
		GroupLayout gl = new GroupLayout(diagnosticBack);
		diagnosticBack.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		gfxDiagnostic = new JPanel();
		gfxDiagnostic.setOpaque(false);
		diagnosticBorder = BorderFactory.createTitledBorder(" Troubleshooting options* ");
		diagnosticBorder.setTitleColor(Color.WHITE);
		gfxDiagnostic.setBorder(diagnosticBorder);
		
		cbDisableD3D = new JCheckBox("Disable D3D acceleration");
		cbDisableD3D.setOpaque(false);
		cbDisableD3D.setForeground(Color.WHITE);
		cbDisableDDraw = new JCheckBox("Disable DirectDraw");
		cbDisableDDraw.setOpaque(false);
		cbDisableDDraw.setForeground(Color.WHITE);
		cbDisableOpenGL = new JCheckBox("Disable OpenGL");
		cbDisableOpenGL.setOpaque(false);
		cbDisableOpenGL.setForeground(Color.WHITE);
		lblRestartNeeded = new JLabel("* might require complete program restart");
		lblRestartNeeded.setOpaque(false);
		lblRestartNeeded.setForeground(Color.WHITE);
		
		gfxDiagnostic.setLayout(new BoxLayout(gfxDiagnostic, BoxLayout.PAGE_AXIS));
		gfxDiagnostic.add(cbDisableD3D);
		gfxDiagnostic.add(Box.createVerticalStrut(5));
		gfxDiagnostic.add(cbDisableDDraw);
		gfxDiagnostic.add(Box.createVerticalStrut(5));
		gfxDiagnostic.add(cbDisableOpenGL);
		gfxDiagnostic.add(Box.createVerticalStrut(5));
		gfxDiagnostic.add(lblRestartNeeded);
		
		gl.setHorizontalGroup(
			gl.createSequentialGroup()
			.addComponent(gfxDiagnostic)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(gfxDiagnostic)
		);
	}
	/**
	 * Alpha composited text field.
	 * @author karnok, 2009.09.23.
	 * @version $Revision 1.0$
	 */
	public class AlphaTextField extends JTextField {
		/** */
		private static final long serialVersionUID = -3843025148067427453L;
		/** The translucency value. */
		private float alpha;
		/**
		 * Constructor.
		 * @param alpha the alpha value
		 */
		public AlphaTextField(float alpha) {
			super();
			this.alpha = alpha;
			setOpaque(false);
		}
		/**
		 * Constructor.
		 * @param alpha the alpha value
		 * @param size the length
		 */
		public AlphaTextField(float alpha, int size) {
			super(size);
			this.alpha = alpha;
			setOpaque(false);
		}
		@Override
		public void paint(Graphics g) {
			Graphics2D g2 = (Graphics2D)g;
			g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
			g2.setColor(Color.white);
			g2.fillRect(0, 0, this.getWidth(), this.getHeight());
			super.paint(g);
		}
	}
	/**
	 * Create the window bounds panel.
	 * @param window the window
	 */
	private void createWindowBoundsPanel(JPanel window) {
		
		JPanel panel = new JPanel();
		
		GroupLayout gl = new GroupLayout(window);
		window.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		gl.setHorizontalGroup(gl.createSequentialGroup().addComponent(panel));
		gl.setVerticalGroup(gl.createSequentialGroup().addComponent(panel));

		panel.setOpaque(false);
		gameWindowBorder = BorderFactory.createTitledBorder(" Game window ");
		gameWindowBorder.setTitleColor(Color.WHITE);
		panel.setBorder(gameWindowBorder);

		gl = new GroupLayout(panel);
		panel.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		lblLeft = new JLabel("Left:");
		lblLeft.setForeground(Color.WHITE);
		edLeft = new AlphaTextField(0.85f, 4);
		lblTop = new JLabel("Top:");
		lblTop.setForeground(Color.WHITE);
		edTop = new AlphaTextField(0.85f, 4);
		lblWidth = new JLabel("Width:");
		lblWidth.setForeground(Color.WHITE);
		edWidth = new AlphaTextField(0.85f, 4);
		lblHeight = new JLabel("Height:");
		lblHeight.setForeground(Color.WHITE);
		edHeight = new AlphaTextField(0.85f, 4);
		btnApplyBounds = new ConfigButton("Apply");
		
		gl.setHorizontalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup()
				.addComponent(lblLeft)
				.addComponent(lblTop, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup()
				.addComponent(edLeft)
				.addComponent(edTop, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup()
				.addComponent(lblWidth)
				.addComponent(lblHeight, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup()
				.addComponent(edWidth)
				.addComponent(edHeight, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addComponent(btnApplyBounds)
		);
		gl.setVerticalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addGroup(
				gl.createSequentialGroup()
				.addGroup(
					gl.createParallelGroup(Alignment.BASELINE)
					.addComponent(lblLeft)
					.addComponent(edLeft)
					.addComponent(lblWidth)
					.addComponent(edWidth)
				)
				.addGroup(
					gl.createParallelGroup(Alignment.BASELINE)
					.addComponent(lblTop)
					.addComponent(edTop)
					.addComponent(lblHeight)
					.addComponent(edHeight)
				)
			)
			.addComponent(btnApplyBounds)
		);
		gl.linkSize(SwingConstants.HORIZONTAL, edLeft, edTop, edWidth, edHeight);
		
	}
	/**
	 * Create language panel.
	 */
	void createAudioPanel() {
		audioPanel = new JPanel();
		audioPanel.setOpaque(false);
		
		audioPanel.setLayout(new BoxLayout(audioPanel, BoxLayout.LINE_AXIS));
		
		JPanel vhelp = new JPanel();
		vhelp.setOpaque(false);
		vhelp.setLayout(new BoxLayout(vhelp, BoxLayout.PAGE_AXIS));
		
		audioPanel.add(Box.createHorizontalGlue());
		audioPanel.add(vhelp);
		audioPanel.add(Box.createHorizontalGlue());
		
		JPanel channelPanel = getAlphaPanel(0.85f, 0x000080);
		lblAudioChannels = new JLabel("Audio channel count:");
		lblAudioChannels.setForeground(Color.WHITE);
		edAudioChannels = new AlphaTextField(0.85f, 2);
		testAudioChannels = new ConfigButton("Test");
		GroupLayout gl = new GroupLayout(channelPanel);
		channelPanel.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		gl.setHorizontalGroup(
			gl.createSequentialGroup()
			.addComponent(lblAudioChannels)
			.addComponent(edAudioChannels, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(testAudioChannels)
		);
		gl.setVerticalGroup(
			gl.createParallelGroup(Alignment.BASELINE)
			.addComponent(lblAudioChannels)
			.addComponent(edAudioChannels, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(testAudioChannels)
		);
		
		
		// -------------------------------------------------------
		JPanel musicPanel = getAlphaPanel(0.85f, 0x000080);
		lblMusicVolume = new JLabel("Music volume:");
		lblMusicVolume.setForeground(Color.WHITE);
		testMusic = new ConfigButton("Test");
		musicSlider = new JSlider(0, 100); 

		gl = new GroupLayout(musicPanel);
		musicPanel.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		gl.setHorizontalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup()
				.addComponent(lblMusicVolume)
				.addComponent(musicSlider)
			)
			.addComponent(testMusic)
		);
		gl.setVerticalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(lblMusicVolume)
				.addComponent(musicSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addComponent(testMusic)
		);
		// -------------------------------------------------------
		JPanel effectPanel = getAlphaPanel(0.85f, 0x000080);
		lblEffectVolume = new JLabel("Effect volume:");
		lblEffectVolume.setForeground(Color.WHITE);
		testEffects = new ConfigButton("Test");
		effectSlider = new JSlider(0, 100); 
		gl = new GroupLayout(effectPanel);
		effectPanel.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		gl.setHorizontalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup()
				.addComponent(lblEffectVolume)
				.addComponent(effectSlider)
			)
			.addComponent(testEffects)
		);
		gl.setVerticalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(lblEffectVolume)
				.addComponent(effectSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addComponent(testEffects)
		);
		
		// -------------------------------------------------------
		JPanel videoPanel = getAlphaPanel(0.85f, 0x000080);
		lblVideoVolume = new JLabel("Video volume:");
		lblVideoVolume.setForeground(Color.WHITE);
		testVideo = new ConfigButton("Test");
		videoSlider = new JSlider(0, 100); 
		gl = new GroupLayout(videoPanel);
		videoPanel.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		gl.setHorizontalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup()
				.addComponent(lblVideoVolume)
				.addComponent(videoSlider)
			)
			.addComponent(testVideo)
		);
		gl.setVerticalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(lblVideoVolume)
				.addComponent(videoSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addComponent(testVideo)
		);

		// -------------------------------------------------------
		vhelp.add(Box.createVerticalGlue());
		vhelp.add(channelPanel);
		vhelp.add(Box.createVerticalStrut(5));
		vhelp.add(musicPanel);
		vhelp.add(Box.createVerticalStrut(5));
		vhelp.add(effectPanel);
		vhelp.add(Box.createVerticalStrut(5));
		vhelp.add(videoPanel);
		vhelp.add(Box.createVerticalGlue());
	}
	/**
	 * Create language panel.
	 */
	void createCheatsPanel() {
		cheatsPanel = new JPanel();
		cheatsPanel.setOpaque(false);
	}
	/**
	 * Create language panel.
	 */
	void createLogsPanel() {
		logsPanel = new JPanel();
		logsPanel.setOpaque(false);
	}
	/**
	 * Create an alpha composite panel.
	 * @param alpha the alpha value
	 * @param color the color
	 * @return the panel
	 */
	JPanel getAlphaPanel(final float alpha, final int color) {
		JPanel p = new JPanel() {
			/** */
			private static final long serialVersionUID = 6984957471180381601L;

			@Override
			public void paint(Graphics g) {
				Graphics2D g2 = (Graphics2D)g;
				Composite cp = g2.getComposite();
				g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
				g2.setColor(new Color(color));
				
				g2.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 20, 20);
				
				g2.setComposite(cp);
				super.paint(g);
			}
		};
		p.setOpaque(false);
		return p;
	}
	/** Change to hungarian labels. */
	void changeToHungarian() {
		btnLanguage.setText("Nyelv / Language");
		btnResources.setText("Fájlok");
		btnGraphics.setText("Grafika");
		btnAudio.setText("Audió");
		btnCheats.setText("Csalások");
		btnLogs.setText("Logok");
		btnSaveAndRun.setText("Ment & Futtat");
		btnSave.setText("Mentés");
		btnClose.setText("Kilépés");
		
		hungarian.setText("Magyar");
		english.setText("Angol / English");

		pathLabel.setText("Fájl útvonal:");
		fileListLabel.setText("Kiválasztott fájlok:");
		btnOpen.setText("Megnyitás...");
		btnLocate.setText("Auto-keresés");
		btnAdd.setText("Hozzáad");
		btnMoveUp.setText("Fel");
		btnMoveDown.setText("Le");
		btnRemove.setText("Eltávolít");

		btnApplyBounds.setText("Alkalmaz");
		lblLeft.setText("Bal:");
		lblTop.setText("Felsõ:");
		lblWidth.setText("Szélesség:");
		lblHeight.setText("Magasság:");
		diagnosticBorder.setTitle(" Hibakeresési opciók* ");
		gameWindowBorder.setTitle(" Játék ablak ");
		cbDisableD3D.setText("D3D letiltása");
		cbDisableDDraw.setText("DirectDraw letiltása");
		cbDisableOpenGL.setText("OpenGL letiltása");
		lblRestartNeeded.setText("* akár az egész programot újra kell indítani");
	}
	/** Change to english labels. */
	void changeToEnglish() {
		btnLanguage.setText("Language / Nyelv");
		btnResources.setText("Files");
		btnGraphics.setText("Graphics");
		btnAudio.setText("Audio");
		btnCheats.setText("Cheats");
		btnLogs.setText("Logs");
		btnSaveAndRun.setText("Save & Run");
		btnSave.setText("Save");
		btnClose.setText("Close");
		
		hungarian.setText("Hungarian / Magyar");
		english.setText("English");

		pathLabel.setText("File path:");
		fileListLabel.setText("Selected files:");
		btnOpen.setText("Open...");
		btnLocate.setText("Auto-locate");
		btnAdd.setText("Add");
		btnMoveUp.setText("Up");
		btnMoveDown.setText("Down");
		btnRemove.setText("Remove");
		
		btnApplyBounds.setText("Apply");
		lblLeft.setText("Left:");
		lblTop.setText("Top:");
		lblWidth.setText("Width:");
		lblHeight.setText("Height:");
		diagnosticBorder.setTitle(" Troubleshooting options* ");
		gameWindowBorder.setTitle(" Game window ");
		cbDisableD3D.setText("Disable D3D");
		cbDisableDDraw.setText("Disable DirectDraw");
		cbDisableOpenGL.setText("Disable OpenGL");
		lblRestartNeeded.setText("* might require complete program restart");
	}
	

}
