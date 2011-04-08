/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig;

import hu.openig.core.Act;
import hu.openig.core.ConfigButton;
import hu.openig.core.Configuration;
import hu.openig.core.LogEntry;
import hu.openig.screens.GameControls;
import hu.openig.sound.AudioThread;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

/**
 * Setup program for the game and other utilities.
 * @author akarnokd, 2009.09.23.
 */
@SuppressWarnings("unchecked") // since 6u20 or so, most jlist models became generic, but if I put the type in, it won't compile on older versions
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
	private JRadioButton rbHungarian;
	/** English language. */
	private JRadioButton rbEnglish;
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
	@SuppressWarnings("rawtypes")
	private JList fileList;
	/** Locate. */
	private ConfigButton btnLocate;
	/** File list label. */
	private JLabel fileListLabel;
	/** Remove. */
	private ConfigButton btnRemove;
	/** The file list model. */
	@SuppressWarnings("rawtypes")
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
	private ConfigButton btnTestAudioChannels;
	/** Music volume. */
	private JLabel lblMusicVolume;
	/** Test music. */
	private ConfigButton btnTestMusic;
	/** Effect volume. */
	private JLabel lblEffectVolume;
	/** Test effect volume. */
	private ConfigButton btnTestEffects;
	/** Video volume. */
	private JLabel lblVideoVolume;
	/** Test video volume. */
	private ConfigButton btnTestVideo;
	/** Music slider. */
	private JSlider musicSlider;
	/** Effect slider. */
	private JSlider effectSlider;
	/** Video slider. */
	private JSlider videoSlider;
	/** Stop playback. */
	protected volatile boolean stopPlayback;
	/**
	 * Audio playback worker.
	 */
	private SwingWorker<Void, Void> playWorker;
	/** Effect filter label. */
	private JLabel lblEffectFilter;
	/** Effect filter value. */
	private JSpinner edEffectFilter;
	/** Video filter label. */
	private JLabel lblVideoFilter;
	/** Video filter value. */
	private JSpinner edVideoFilter;
	/** Player label. */
	private JLabel lblPlayer;
	/** List of players. */
	private JComboBox cbPlayers;
	/** Money label. */
	private JLabel lblMoney;
	/** Monery editor. */
	private JTextField edMoney;
	/** AI mode label. */
	private JLabel lblAIMode;
	/** AI mode list. */
	private JComboBox cbAIMode;
	/** Set Visual button. */
	private ConfigButton btnSetVisual;
	/** Apply player button. */
	private ConfigButton btnApplyPlayer;
	/** Planet label. */
	private JLabel lblPlanet;
	/** Planets list. */
	private JComboBox cbPlanet;
	/** Owner label. */
	private JLabel lblOwner;
	/** Owner list. */
	private JComboBox cbOwner;
	/** Population count label. */
	private JLabel lblPopCount;
	/** Population count editor. */
	private JTextField edPopCount;
	/** Apply planet button. */
	private ConfigButton btnApplyPlanet;
	/** The log model. */
	protected LogModel logModel;
	/** The log table. */
	protected JTable logTable;
	/** Log detail area. */
	private JTextArea edLogDetail;
	/** The configuration. */
	private Configuration config;
	/** Action listeners for the event of run. */
	public final Set<Act> onRun = new HashSet<Act>();
	/** The game window. */
	protected GameControls gameControls;
	/** Get the current bounds. */
	private ConfigButton btnCurrentBounds;
	/** Move to the center. */
	private ConfigButton btnCenterBounds;
	/** Automatically manage resource files? */
	private JCheckBox autoResources;
	/**
	 * Constructor. Assign an active game window.
	 * @param config the configuration
	 * @param gameControls the game window
	 */
	public Setup(Configuration config, GameControls gameControls) {
		this(config);
		this.gameControls = gameControls;
		btnSaveAndRun.setEnabled(false);
	}
	/**
	 * Constructor. Initializes the GUI elements.
	 * @param config the configuration
	 */
	public Setup(Configuration config) {
		this.config = config;
		setTitle("Open Imperium Galactica Setup/Be�ll�t�s");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		try {
			background = ImageIO.read(getClass().getResource("/hu/openig/gfx/setup.png"));
			setIconImage(ImageIO.read(getClass().getResource("/hu/openig/gfx/open-ig-logo.png")));
		} catch (IOException ex) {
			config.error(ex);
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
		config.logListener.add(new Act() { @Override public void act() { doUpdateLog(); } });
		loadConfig();
	}
	/** Update the event log. */
	protected void doUpdateLog() {
		logModel.fireTableDataChanged();
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
			tabs.get(i).addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { selectPanel(j); } });
		}
		
		btnSaveAndRun = new ConfigButton("Save & Run");
		btnSaveAndRun.addActionListener(new Act() { @Override public void act() { doSaveAndRun(); } });
		btnSave = new ConfigButton("Save");
		btnSave.addActionListener(new Act() { @Override public void act() { doSave(); } });
		btnClose = new ConfigButton("Close");
		btnClose.addActionListener(new Act() { @Override public void act() { dispose(); } });
		
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
	/** Save and run the game. */
	protected void doSaveAndRun() {
		doSave();
		dispose();
		for (Act a : onRun) {
			a.act();
		}
	}
	/** Save the configuration. */
	protected void doSave() {
		String currentLanguage = config.language;
		storeConfig();
		if (!currentLanguage.equals(config.language) && gameControls != null) {
			gameControls.switchLanguage(config.language);
		}
		config.save();
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
		
		rbHungarian = new JRadioButton("Magyar", true);
		rbHungarian.setOpaque(false);
		rbHungarian.setForeground(Color.WHITE);
		rbHungarian.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				changeToHungarian();
			}
		});
		
		
		rbEnglish = new JRadioButton("Angol / English");
		rbEnglish.setOpaque(false);
		rbEnglish.setForeground(Color.WHITE);
		rbEnglish.addActionListener(new ActionListener() {
			/* (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				changeToEnglish();
			}
		});
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(rbHungarian);
		bg.add(rbEnglish);
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.LEADING)
			.addComponent(rbHungarian)
			.addComponent(rbEnglish)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(rbHungarian)
			.addComponent(rbEnglish)
		);
	}
	/**
	 * Create language panel.
	 */
	@SuppressWarnings("rawtypes")
	void createFilesPanel() {
		filesPanel = new JPanel();
		filesPanel.setOpaque(false);
		
		GroupLayout gl = new GroupLayout(filesPanel);
		filesPanel.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		autoResources = new JCheckBox("Automatically manage game files");
		autoResources.setOpaque(false);
		
		pathLabel = new JLabel("File path:");
		path = new AlphaTextField(0.85f);
		path.setOpaque(false);
		btnOpen = new ConfigButton("Open...");
		btnOpen.addActionListener(new Act() { @Override public void act() { doOpenPath(); } });
		btnLocate = new ConfigButton("Locate");
		btnLocate.addActionListener(new Act() { @Override public void act() { doLocate(); } });
		btnAdd = new ConfigButton("Add");
		btnAdd.addActionListener(new Act() { @Override public void act() { doAddPath(); } });
		btnMoveUp = new ConfigButton("Up");
		btnMoveUp.addActionListener(new Act() { @Override public void act() { doMoveUp(); } });
		btnMoveDown = new ConfigButton("Down");
		btnMoveDown.addActionListener(new Act() { @Override public void act() { doMoveDown(); } });
		btnRemove = new ConfigButton("Remove");
		btnRemove.addActionListener(new Act() { @Override public void act() { doRemove(); } });
		
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
			.addComponent(autoResources)
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
			.addComponent(autoResources)
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
	 * Remove selected list entries.
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
		for (String s : config.getContainersAutomatically()) {
			fileListModel.addElement(s);
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
	 * @author akarnokd, 2009.09.23.
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
		edLeft.setText("0");
		
		lblTop = new JLabel("Top:");
		lblTop.setForeground(Color.WHITE);
		edTop = new AlphaTextField(0.85f, 4);
		edTop.setText("0");

		lblWidth = new JLabel("Width:");
		lblWidth.setForeground(Color.WHITE);
		edWidth = new AlphaTextField(0.85f, 4);
		edWidth.setText("640");

		lblHeight = new JLabel("Height:");
		lblHeight.setForeground(Color.WHITE);
		edHeight = new AlphaTextField(0.85f, 4);
		edHeight.setText("480");
		btnApplyBounds = new ConfigButton("Apply");
		btnApplyBounds.addActionListener(new Act() { @Override public void act() { doApplyBounds(); } });
		btnCurrentBounds = new ConfigButton("Get current");
		btnCurrentBounds.addActionListener(new Act() { @Override public void act() { doCurrentBounds(); } });
		btnCenterBounds = new ConfigButton("Center");
		btnCenterBounds.addActionListener(new Act() { @Override public void act() { doCenter(); } });
		
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
			.addGroup(
				gl.createParallelGroup()
				.addComponent(btnApplyBounds)
				.addComponent(btnCurrentBounds)
				.addComponent(btnCenterBounds)
			)
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
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(btnCurrentBounds)
				.addComponent(btnApplyBounds)
				.addComponent(btnCenterBounds)
			)
		);
		gl.linkSize(SwingConstants.HORIZONTAL, edLeft, edTop, edWidth, edHeight);
		gl.linkSize(SwingConstants.HORIZONTAL, btnCurrentBounds, btnApplyBounds, btnCenterBounds);
	}
	/** Get the current bounds. */
	void doCurrentBounds() {
	}
	/** Center the window. */
	void doCenter() {
	}
	/**
	 * Apply the size settings from the input boxes.
	 */
	void doApplyBounds() {
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
		edAudioChannels.setText("8");
		btnTestAudioChannels = new ConfigButton("Test");
		btnTestAudioChannels.addActionListener(new Act() { @Override public void act() { playTestMulti(); } });
		GroupLayout gl = new GroupLayout(channelPanel);
		channelPanel.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		gl.setHorizontalGroup(
			gl.createSequentialGroup()
			.addComponent(lblAudioChannels)
			.addComponent(edAudioChannels, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(btnTestAudioChannels)
		);
		gl.setVerticalGroup(
			gl.createParallelGroup(Alignment.BASELINE)
			.addComponent(lblAudioChannels)
			.addComponent(edAudioChannels, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(btnTestAudioChannels)
		);
		
		
		// -------------------------------------------------------
		JPanel musicPanel = getAlphaPanel(0.85f, 0x000080);
		lblMusicVolume = new JLabel("Music volume:");
		lblMusicVolume.setForeground(Color.WHITE);
		btnTestMusic = new ConfigButton("Test");
		btnTestMusic.addActionListener(new Act() { @Override public void act() { playTest(musicSlider.getValue(), btnTestMusic, 1); } });
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
			.addComponent(btnTestMusic)
		);
		gl.setVerticalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(lblMusicVolume)
				.addComponent(musicSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addComponent(btnTestMusic)
		);
		// -------------------------------------------------------
		JPanel effectPanel = getAlphaPanel(0.85f, 0x000080);
		lblEffectVolume = new JLabel("Effect volume:");
		lblEffectVolume.setForeground(Color.WHITE);
		btnTestEffects = new ConfigButton("Test");
		btnTestEffects.addActionListener(new Act() { @Override public void act() { playTest(effectSlider.getValue(), btnTestEffects, (Integer)edEffectFilter.getValue()); } });
		effectSlider = new JSlider(0, 100); 
		
		lblEffectFilter = new JLabel("Averaging filter step count: ");
		lblEffectFilter.setForeground(Color.WHITE);
		edEffectFilter = new JSpinner(new SpinnerNumberModel(1, 1, 2048, 1));
		
		
		gl = new GroupLayout(effectPanel);
		effectPanel.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		gl.setHorizontalGroup(
			gl.createParallelGroup()
			.addGroup(
				gl.createSequentialGroup()
				.addGroup(
					gl.createParallelGroup()
					.addComponent(lblEffectVolume)
					.addComponent(effectSlider)
				)
				.addComponent(btnTestEffects)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(lblEffectFilter)
				.addComponent(edEffectFilter, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addGroup(
					gl.createSequentialGroup()
					.addComponent(lblEffectVolume)
					.addComponent(effectSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				)
				.addComponent(btnTestEffects)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(lblEffectFilter)
				.addComponent(edEffectFilter, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
		);
		
		// -------------------------------------------------------
		JPanel videoPanel = getAlphaPanel(0.85f, 0x000080);
		lblVideoVolume = new JLabel("Video volume:");
		lblVideoVolume.setForeground(Color.WHITE);
		btnTestVideo = new ConfigButton("Test");
		btnTestVideo.addActionListener(new Act() { @Override public void act() { playTest(videoSlider.getValue(), btnTestVideo, (Integer)edVideoFilter.getValue()); } });
		videoSlider = new JSlider(0, 100); 
		lblVideoFilter = new JLabel("Averaging filter step count: ");
		lblVideoFilter.setForeground(Color.WHITE);
		edVideoFilter = new JSpinner(new SpinnerNumberModel(1, 1, 2048, 1));
		gl = new GroupLayout(videoPanel);
		videoPanel.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		gl.setHorizontalGroup(
				gl.createParallelGroup()
				.addGroup(
					gl.createSequentialGroup()
					.addGroup(
						gl.createParallelGroup()
						.addComponent(lblVideoVolume)
						.addComponent(videoSlider)
					)
					.addComponent(btnTestVideo)
				)
				.addGroup(
					gl.createSequentialGroup()
					.addComponent(lblVideoFilter)
					.addComponent(edVideoFilter, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				)
			);
			gl.setVerticalGroup(
				gl.createSequentialGroup()
				.addGroup(
					gl.createParallelGroup(Alignment.CENTER)
					.addGroup(
						gl.createSequentialGroup()
						.addComponent(lblVideoVolume)
						.addComponent(videoSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
					.addComponent(btnTestVideo)
				)
				.addGroup(
					gl.createParallelGroup(Alignment.CENTER)
					.addComponent(lblVideoFilter)
					.addComponent(edVideoFilter, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				)
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
		
		musicSlider.setValue(100);
		effectSlider.setValue(100);
		videoSlider.setValue(100);
	}
	/**
	 * Create language panel.
	 */
	void createCheatsPanel() {
		cheatsPanel = new JPanel();
		cheatsPanel.setOpaque(false);
		cheatsPanel.setLayout(new BoxLayout(cheatsPanel, BoxLayout.LINE_AXIS));
		
		JPanel vhelp = new JPanel();
		vhelp.setOpaque(false);
		vhelp.setLayout(new BoxLayout(vhelp, BoxLayout.PAGE_AXIS));
		
		cheatsPanel.add(Box.createHorizontalGlue());
		cheatsPanel.add(vhelp);
		cheatsPanel.add(Box.createHorizontalGlue());
		
		JPanel playerPanel = getAlphaPanel(0.85f, 0x000080);
		
		GroupLayout gl = new GroupLayout(playerPanel);
		playerPanel.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		lblPlayer = new JLabel("Player:");
		lblPlayer.setForeground(Color.WHITE);
		cbPlayers = new JComboBox(new String[] { "Player 1", "Player N" });
		
		JSeparator sep0 = new JSeparator();
		
		lblMoney = new JLabel("Money:");
		lblMoney.setForeground(Color.WHITE);
		edMoney = new AlphaTextField(0.85f, 10);
		
		lblAIMode = new JLabel("AI mode:");
		lblAIMode.setForeground(Color.WHITE);
		cbAIMode = new JComboBox(new String[] { "No AI", "Hard defensive", "Medium Defensive", "Light Defensive", 
				"Normal", "Light Offensive", "Medium Offensive", "Heavy offensive" });

		btnSetVisual = new ConfigButton("Set Visual Player");
		btnApplyPlayer = new ConfigButton("Apply changes");
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addGroup(
				gl.createSequentialGroup()
				.addGroup(
					gl.createParallelGroup()
					.addComponent(lblPlayer)
					.addComponent(lblMoney)
					.addComponent(lblAIMode)
				)
				.addGroup(
					gl.createParallelGroup()
					.addComponent(cbPlayers, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(edMoney, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(cbAIMode, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				)
			)
			.addComponent(sep0)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(btnSetVisual)
				.addComponent(btnApplyPlayer)
			)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(lblPlayer)
				.addComponent(cbPlayers)
			)
			.addComponent(sep0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(lblMoney)
				.addComponent(edMoney, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(lblAIMode)
				.addComponent(cbAIMode)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(btnSetVisual)
				.addComponent(btnApplyPlayer)
			)
		);
		
		// ---------------------------------------------------------------
		
		JPanel planetPanel = getAlphaPanel(0.85f, 0x000080);

		lblPlanet = new JLabel("Planet:");
		lblPlanet.setForeground(Color.WHITE);
		cbPlanet = new JComboBox(new String[] { "Achilles", "Centronom", "New Caroline", "San Sterling", "Earth" });
		
		JSeparator sep1 = new JSeparator();
		
		lblOwner = new JLabel("Owner:");
		lblOwner.setForeground(Color.WHITE);
		cbOwner = new JComboBox(new String[] { "", "Player 1", "Player N", "Pirates" });
		
		lblPopCount = new JLabel("Population count:");
		lblPopCount.setForeground(Color.WHITE);
		edPopCount = new AlphaTextField(0.85f, 10);
		
		btnApplyPlanet = new ConfigButton("Apply");
		
		gl = new GroupLayout(planetPanel);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		planetPanel.setLayout(gl);
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addGroup(
				gl.createSequentialGroup()
				.addGroup(
					gl.createParallelGroup()
					.addComponent(lblPlanet)
					.addComponent(lblOwner)
					.addComponent(lblPopCount)
				)
				.addGroup(
					gl.createParallelGroup()
					.addComponent(cbPlanet, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(cbOwner, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(edPopCount, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				)
			)
			.addComponent(btnApplyPlanet)
			.addComponent(sep1)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(lblPlanet)
				.addComponent(cbPlanet, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addComponent(sep1)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(lblOwner)
				.addComponent(cbOwner, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(lblPopCount)
				.addComponent(edPopCount, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addComponent(btnApplyPlanet)
		);
		
		
		vhelp.add(Box.createVerticalGlue());
		vhelp.add(playerPanel);
		vhelp.add(Box.createVerticalStrut(5));
		vhelp.add(planetPanel);
		vhelp.add(Box.createVerticalGlue());
	}
	/**
	 * Log table model.
	 * @author akarnokd, 2009.09.25.
	 */
	class LogModel extends AbstractTableModel {
		/** */
		private static final long serialVersionUID = 8455687509940724726L;
		/** The column names. */
		public String[] columnNames = { "Timestamp", "Severity", "Message", "Stacktrace" };
		/** The column classes. */
		public Class<?>[] columnClasses = { String.class, String.class, String.class, String.class };
		/** The list of rows. */
		public List<LogEntry> rows = config.logs;
		/** The date format helper. */
		protected SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		@Override
		public int getColumnCount() {
			return columnNames.length;
		}
		@Override
		public int getRowCount() {
			return rows.size();
		}
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			LogEntry e = rows.get(rowIndex);
			switch (columnIndex) {
			case 0:
				return sdf.format(new Timestamp(e.timestamp));
			case 1:
				return e.severity;
			case 2:
				return e.message;
			case 3:
				return e.stackTrace;
			default:
				return null;
			}
		}
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return columnClasses[columnIndex];
		}
		@Override
		public String getColumnName(int column) {
			return columnNames[column];
		}
	}
	/**
	 * Create language panel.
	 */
	void createLogsPanel() {
		logsPanel = new JPanel();
		logsPanel.setOpaque(false);
		
		GroupLayout gl = new GroupLayout(logsPanel);
		logsPanel.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		logModel = new LogModel();
		logTable = new JTable(logModel) {
			/** */
			private static final long serialVersionUID = -2007954494618823917L;
			@Override
			public void paint(Graphics g) {
				Graphics2D g2 = (Graphics2D)g;
				g2.setComposite(AlphaComposite.SrcOver.derive(0.85f));
				super.paint(g);
			};
		};
		logTable.setOpaque(false);
		logTable.setAutoCreateRowSorter(true);
		logTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		logTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				doLogTableSelect();
			}
		});
		logTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		logTable.getColumnModel().getColumn(0).setPreferredWidth(110);
		logTable.getColumnModel().getColumn(1).setPreferredWidth(80);
		logTable.getColumnModel().getColumn(2).setPreferredWidth(300);
		logTable.getColumnModel().getColumn(3).setPreferredWidth(90);

		logTable.getColumnModel().getColumn(0).setWidth(110);
		logTable.getColumnModel().getColumn(1).setWidth(80);
		logTable.getColumnModel().getColumn(2).setWidth(300);
		logTable.getColumnModel().getColumn(3).setWidth(90);

		JScrollPane sp = new JScrollPane(logTable);
		sp.setOpaque(false);
		
		edLogDetail = new JTextArea() {
			/** */
			private static final long serialVersionUID = -1319968378332145953L;
			@Override
			public void paint(Graphics g) {
				Graphics2D g2 = (Graphics2D)g;
				g2.setComposite(AlphaComposite.SrcOver.derive(0.85f));
				super.paint(g);
			};
		};
		edLogDetail.setEditable(false);
		JScrollPane sp2 = new JScrollPane(edLogDetail);
		sp2.setOpaque(false);
		
		gl.setHorizontalGroup(
			gl.createParallelGroup()
			.addComponent(sp)
			.addComponent(sp2)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(sp, 100, 100, Short.MAX_VALUE)
			.addComponent(sp2, 100, 100, 100)
		);
	}
	/**
	 * Display log detail in the log panel.
	 */
	void doLogTableSelect() {
		int idx = logTable.getSelectedRow();
		edLogDetail.setText("");
		if (idx >= 0) {
			idx = logTable.convertRowIndexToView(idx);
			LogEntry e = logModel.rows.get(idx);
			edLogDetail.append(logModel.sdf.format(new Timestamp(e.timestamp)));
			edLogDetail.append("\r\n");
			edLogDetail.append(e.severity);
			edLogDetail.append("\r\n");
			edLogDetail.append(e.message);
			edLogDetail.append("\r\n");
			edLogDetail.append(e.stackTrace);
			edLogDetail.append("\r\n");
		}
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
	/**
	 * Play test audio on the specified volume.
	 * @param volume the volume whole percents
	 * @param button the button to change title on
	 * @param filterSteps the number of filter steps.
	 */
	protected void playTest(final int volume, final AbstractButton button, final int filterSteps) {
		if (playWorker != null) {
			stopPlayback = true;
//			playWorker.cancel(true);
		} else {
			final String title = button.getText();
			button.setText("Stop");
			playWorker = new SwingWorker<Void, Void>() {
				@Override
				protected Void doInBackground() throws Exception {
					playTestDirectly(volume, filterSteps);
					return null;
				}
				@Override
				protected void done() {
					playWorker = null;
					button.setText(title);
					stopPlayback = false;
				}
			};
			playWorker.execute();
		}
	}
	/**
	 * Play test directly.
	 * @param volume the volume
	 * @param windowSize the averaging window size
	 */
	protected void playTestDirectly(int volume, int windowSize) {
		try {
			AudioInputStream in = AudioSystem.getAudioInputStream(getClass().getResource("/hu/openig/xold/res/welcome.wav"));
			try {
				AudioFormat streamFormat = new AudioFormat(22050, 16, 1, true, false);
				DataLine.Info clipInfo = new DataLine.Info(SourceDataLine.class, streamFormat);

				SourceDataLine clip = (SourceDataLine) AudioSystem.getLine(clipInfo);
				try {
					clip.open();
					FloatControl fc = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
					double minLinear = Math.pow(10, fc.getMinimum() / 20);
					double maxLinear = Math.pow(10, fc.getMaximum() / 20);
					fc.setValue((float)(20 * Math.log10(minLinear + volume * (maxLinear - minLinear) / 100)));
					clip.start();
					byte[] buffer = new byte[in.available()];
					in.read(buffer);
					byte[] buffer2 = AudioThread.split16To8(AudioThread.movingAverage(upscale8To16AndSignify(buffer), windowSize));
					int remaining = buffer2.length;
					int offset = 0;
					while (!Thread.currentThread().isInterrupted() && !stopPlayback && remaining > 0) {
						int count = remaining > 4096 ? 4096 : remaining;
						clip.write(buffer2, offset, count);
						offset += count;
						remaining -= count;
					}
					if (stopPlayback) {
						clip.stop();
						clip.drain();
					} else {
						clip.drain();
						clip.stop();
					}
				} finally {
					clip.close();
				}
			} finally {
				in.close();
			}
		} catch (IOException ex) {
			config.error(ex);
		} catch (UnsupportedAudioFileException ex) {
			config.error(ex);
		} catch (LineUnavailableException ex) {
			config.error(ex);
		}
	}
	/**
	 * Upscale the 8 bit signed values to 16 bit signed values.
	 * @param data the data to upscale
	 * @return the upscaled data
	 */
	public static short[] upscale8To16AndSignify(byte[] data) {
		short[] result = new short[data.length];
		for (int i = 0; i < data.length; i++) {
			result[i] = (short)(((data[i] & 0xFF) - 128) * 256);
		}
		return result;
	}

	/**
	 * Play the welcome.wav simultaneously.
	 */
	protected void playTestMulti() {
		if (playWorker != null) {
			stopPlayback = true;
//			playWorker.cancel(true);
		} else {
			final int vol = effectSlider.getValue();
			final int channels = Integer.parseInt(edAudioChannels.getText());
			final String title = btnTestAudioChannels.getText();
			final int window = (Integer)edEffectFilter.getValue();
			btnTestAudioChannels.setText("Stop");
			playWorker = new SwingWorker<Void, Void>() {
				@Override
				protected Void doInBackground() throws Exception {
					try {
						ExecutorService exec = Executors.newFixedThreadPool(channels);
						for (int i = 0; i < channels; i++) {
							final int j = i;
							exec.submit(new Runnable() {
								@Override
								public void run() {
									try {
										TimeUnit.MILLISECONDS.sleep(3000 * j / channels);
										playTestDirectly(vol, window);
									} catch (InterruptedException ex) {
										config.debug(ex);
									}
								}
							});
						}
						exec.shutdown();
						exec.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
					} catch (NumberFormatException ex) {
						config.error(ex);
					} catch (InterruptedException ex) {
						config.debug(ex);
					}
					return null;
				}
				@Override
				protected void done() {
					playWorker = null;
					btnTestAudioChannels.setText(title);
					stopPlayback = false;
				}
			};
			playWorker.execute();
		}
	}		
	/** Change to hungarian labels. */
	void changeToHungarian() {
		btnLanguage.setText("Nyelv / Language");
		btnResources.setText("F�jlok");
		btnGraphics.setText("Grafika");
		btnAudio.setText("Audi�");
		btnCheats.setText("Csal�sok");
		btnLogs.setText("Logok");
		btnSaveAndRun.setText("Ment & Futtat");
		btnSave.setText("Ment�s");
		btnClose.setText("Kil�p�s");
		
		rbHungarian.setText("Magyar");
		rbEnglish.setText("Angol / English");

		pathLabel.setText("F�jl �tvonal:");
		fileListLabel.setText("Kiv�lasztott f�jlok:");
		btnOpen.setText("Megnyit�s...");
		btnLocate.setText("Auto-keres�s");
		btnAdd.setText("Hozz�ad");
		btnMoveUp.setText("Fel");
		btnMoveDown.setText("Le");
		btnRemove.setText("Elt�vol�t");

		btnApplyBounds.setText("Alkalmaz");
		lblLeft.setText("Bal:");
		lblTop.setText("Fels�:");
		lblWidth.setText("Sz�less�g:");
		lblHeight.setText("Magass�g:");
		diagnosticBorder.setTitle(" Hibakeres�si opci�k* ");
		gameWindowBorder.setTitle(" J�t�k ablak ");
		cbDisableD3D.setText("D3D letilt�sa");
		cbDisableDDraw.setText("DirectDraw letilt�sa");
		cbDisableOpenGL.setText("OpenGL letilt�sa");
		lblRestartNeeded.setText("* ak�r az eg�sz programot �jra kell ind�tani");
		
		lblAudioChannels.setText("Audio csatorn�k sz�ma:");
		btnTestAudioChannels.setText("Teszt");
		lblMusicVolume.setText("Zene hanger�:");
		btnTestMusic.setText("Teszt");
		lblEffectVolume.setText("Effektus hanger�:");
		btnTestEffects.setText("Teszt");
		lblVideoVolume.setText("Video hanger�:");
		btnTestVideo.setText("Teszt");
		
		lblEffectFilter.setText("�tlagol� sz�r� l�p�ssz�m:");
		lblVideoFilter.setText("�tlagol� sz�r� l�p�ssz�m:");

		lblPlayer.setText("J�t�kos:");
		lblMoney.setText("P�nz:");
		lblAIMode.setText("MI m�d:");
		int idx = cbAIMode.getSelectedIndex();
		cbAIMode.setModel(new DefaultComboBoxModel(new String[] { "Nincs AI", "Neh�z v�dekez�", "K�zepes v�dekez�", 
				"K�nny� v�dekez�", "Norm�l", "K�nny� t�mad�", "K�zepes t�mad�", "Neh�z t�mad�" }));
		cbAIMode.setSelectedIndex(idx);
		
		lblPlanet.setText("Bolyg�:");
		lblOwner.setText("Tulajdonos:");
		lblPopCount.setText("Popul�ci�:");

		btnSetVisual.setText("J�t�kos mutat�sa");
		btnApplyPlayer.setText("Alkalmaz");
		btnApplyPlanet.setText("Alkalmaz");
		
		btnCenterBounds.setText("K�z�pre");
		btnCurrentBounds.setText("Jelenlegi poz�ci�");
		
		autoResources.setText("Automatikusan kezelje a j�t�k f�jljait");
		
		logModel.columnNames = new String[] { "Id�b�lyeg", "Fontoss�g", "�zenet", "Hibahely" };
		refreshLogTableColumns();		
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
		
		rbHungarian.setText("Hungarian / Magyar");
		rbEnglish.setText("English");

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

		lblAudioChannels.setText("Audio channel count:");
		btnTestAudioChannels.setText("Test");
		lblMusicVolume.setText("Music volume:");
		btnTestMusic.setText("Test");
		lblEffectVolume.setText("Effect volume:");
		btnTestEffects.setText("Test");
		lblVideoVolume.setText("Video volume:");
		btnTestVideo.setText("Test");
		
		lblEffectFilter.setText("Averaging filter step count:");
		lblVideoFilter.setText("Averaging filter step count:");
		
		lblPlayer.setText("Player:");
		lblMoney.setText("Money:");
		lblAIMode.setText("AI mode:");
		int idx = cbAIMode.getSelectedIndex();
		cbAIMode.setModel(new DefaultComboBoxModel(new String[] { "No AI", "Hard defensive", "Medium Defensive", "Light Defensive", 
				"Normal", "Light Offensive", "Medium Offensive", "Heavy offensive" }));
		cbAIMode.setSelectedIndex(idx);
		
		lblPlanet.setText("Planet:");
		lblOwner.setText("Owner:");
		lblPopCount.setText("Population count:");

		btnSetVisual.setText("Show Player");
		btnApplyPlayer.setText("Apply changes");
		btnApplyPlanet.setText("Apply changes");

		btnCenterBounds.setText("Center");
		btnCurrentBounds.setText("Current position");
		
		autoResources.setText("Automatically manage game files");
		
		logModel.columnNames = new String[] { "Timestamp", "Severity", "Message", "Stacktrace" };
		refreshLogTableColumns();
	}
	/** Refresh table column titles. */
	private void refreshLogTableColumns() {
		// save and restore column widths
		List<Integer> widths = new ArrayList<Integer>();
		for (int i = 0; i < logTable.getColumnCount(); i++) {
			widths.add(logTable.getColumnModel().getColumn(i).getWidth());
		}
		logModel.fireTableStructureChanged();
		for (int i = 0; i < logTable.getColumnCount(); i++) {
			logTable.getColumnModel().getColumn(i).setWidth(widths.get(i));
			logTable.getColumnModel().getColumn(i).setPreferredWidth(widths.get(i));
		}
	}
	/**
	 * Load configuration values into the GUI.
	 */
	protected void loadConfig() {
//		if (config.isNew) {
//			return;
//		}
		if ("hu".equals(config.language)) {
			rbHungarian.setSelected(true);
			changeToHungarian();
		} else
		if ("en".equals(config.language)) {
			rbEnglish.setSelected(true);
			changeToEnglish();
		}
		fileListModel.clear();
		for (String s : config.containers) {
			fileListModel.addElement(s);
		}
		autoResources.setSelected(config.autoResources);
		
		if (config.left != null) {
			edLeft.setText(config.left.toString());
		}
		if (config.top != null) {
			edTop.setText(config.top.toString());
		}
		if (config.width != null) {
			edWidth.setText(config.width.toString());
		}
		if (config.height != null) {
			edHeight.setText(config.height.toString());
		}

		cbDisableD3D.setSelected(config.disableD3D);
		cbDisableDDraw.setSelected(config.disableDirectDraw);
		cbDisableOpenGL.setSelected(config.disableOpenGL);
		
		edAudioChannels.setText(Integer.toString(config.audioChannels));
		musicSlider.setValue(config.musicVolume);
		effectSlider.setValue(config.effectVolume);
		videoSlider.setValue(config.videoVolume);
		
		edEffectFilter.setValue(config.effectFilter);
		edVideoFilter.setValue(config.videoFilter);
		
		repaint();
	}
	/**
	 * Store GUI settings into the configuration.
	 */
	protected void storeConfig() {
		if (rbHungarian.isSelected()) {
			config.language = "hu";
		} else
		if (rbEnglish.isSelected()) {
			config.language = "en";
		}
		config.containers.clear();
		for (int i = 0; i < fileListModel.size(); i++) {
			config.containers.add((String)fileListModel.elementAt(i));
		}
		config.autoResources = autoResources.isSelected();
		
		if (!edLeft.getText().isEmpty()) {
			config.left = Integer.valueOf(edLeft.getText());
		} else {
			config.left = null;
		}
		if (!edTop.getText().isEmpty()) {
			config.top = Integer.valueOf(edTop.getText());
		} else {
			config.top = null;
		}
		if (!edWidth.getText().isEmpty()) {
			config.width = Integer.valueOf(edWidth.getText());
		} else {
			config.width = null;
		}
		if (!edHeight.getText().isEmpty()) {
			config.height = Integer.valueOf(edHeight.getText());
		} else {
			config.height = null;
		}
		config.disableD3D = cbDisableD3D.isSelected();
		config.disableDirectDraw = cbDisableDDraw.isSelected();
		config.disableOpenGL = cbDisableOpenGL.isSelected();
		
		config.audioChannels = Integer.parseInt(edAudioChannels.getText());
		config.musicVolume = musicSlider.getValue();
		config.effectVolume = effectSlider.getValue();
		config.videoVolume = effectSlider.getValue();
		config.effectFilter = (Integer)edEffectFilter.getValue();
		config.videoFilter = (Integer)edVideoFilter.getValue();
	}
}
