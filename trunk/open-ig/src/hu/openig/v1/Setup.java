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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.GroupLayout.Alignment;

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
		
		ConfigButton btnLanguage = new ConfigButton("Language/Nyelv");
		ConfigButton btnResources = new ConfigButton("Files");
		ConfigButton btnGraphics = new ConfigButton("Graphics");
		ConfigButton btnAudio = new ConfigButton("Audio");
		ConfigButton btnCheats = new ConfigButton("Cheats");
		ConfigButton btnLogs = new ConfigButton("Logs");

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
		
		ConfigButton btnSaveAndRun = new ConfigButton("Save & Run");
		ConfigButton btnSave = new ConfigButton("Save");
		ConfigButton btnClose = new ConfigButton("Close");
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
		
		JPanel mid = getAlphaPanel(0.85f, Color.BLUE.getRGB());
		a.setLayout(new BoxLayout(a, BoxLayout.LINE_AXIS));
		a.add(Box.createVerticalGlue());
		a.add(mid);
		a.add(Box.createVerticalGlue());
		
		GroupLayout gl = new GroupLayout(mid);
		mid.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		hungarian = new JRadioButton("Hungarian / Magyar", true);
		hungarian.setOpaque(false);
		hungarian.setForeground(Color.WHITE);
		
		
		english = new JRadioButton("English");
		english.setOpaque(false);
		english.setForeground(Color.WHITE);
		
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
	}
	/**
	 * Create language panel.
	 */
	void createGraphicsPanel() {
		graphicsPanel = new JPanel();
		graphicsPanel.setOpaque(false);
	}
	/**
	 * Create language panel.
	 */
	void createAudioPanel() {
		audioPanel = new JPanel();
		audioPanel.setOpaque(false);
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
}
