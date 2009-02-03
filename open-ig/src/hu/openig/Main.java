/*
 * Copyright 2008, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig;

import hu.openig.core.BtnAction;
import hu.openig.core.InfoScreen;
import hu.openig.core.SurfaceType;
import hu.openig.gfx.CommonGFX;
import hu.openig.gfx.InformationGFX;
import hu.openig.gfx.InformationRenderer;
import hu.openig.gfx.PlanetGFX;
import hu.openig.gfx.PlanetRenderer;
import hu.openig.gfx.StarmapGFX;
import hu.openig.gfx.StarmapRenderer;
import hu.openig.gfx.TextGFX;
import hu.openig.model.GMPlanet;
import hu.openig.sound.UISounds;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * The main entry point for now.
 * @author karnokd
 *
 */
public class Main extends JFrame {
	/** */
	private static final long serialVersionUID = 6922932910697940684L;
	UISounds uis;
	CommonGFX cgfx;
	StarmapRenderer smr;
	PlanetRenderer pr;
	InformationRenderer ir;
	Timer fadeTimer;
	int FADE_TIME = 50;
	JLayeredPane layers;
	protected void initialize(String root) {
		setTitle("Open Imperium Galactica");
		fadeTimer = new Timer(FADE_TIME, null);
		uis = new UISounds(root);
		cgfx = new CommonGFX(root);
		smr = new StarmapRenderer(new StarmapGFX(root), cgfx, uis);
		pr = new PlanetRenderer(new PlanetGFX(root), cgfx, uis);
		ir = new InformationRenderer(new InformationGFX(root), cgfx, uis);
		smr.setVisible(true);
		ir.setVisible(false);
		pr.setVisible(false);
		setListeners();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				uis.close();
				smr.stopAnimations();
			}
		});

		setKeyboard();
		
		layers = new JLayeredPane();
		layers.add(smr, Integer.valueOf(0));
		layers.add(pr, Integer.valueOf(1));
		layers.add(ir, Integer.valueOf(2));
		
		GroupLayout gl = new GroupLayout(layers);
		layers.setLayout(gl);
		gl.setHorizontalGroup(gl.createParallelGroup()
			.addComponent(smr, 640, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addComponent(pr, 640, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addComponent(ir, 640, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		);
		gl.setVerticalGroup(
			gl.createParallelGroup()
			.addComponent(smr, 480, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addComponent(pr, 480, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addComponent(ir, 480, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		);
		
		
		// Determine minimum client width and height
		Container c = getContentPane();
		gl = new GroupLayout(c);
		c.setLayout(gl);
		gl.setHorizontalGroup(gl.createParallelGroup()
			.addComponent(layers, 640, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		);
		gl.setVerticalGroup(
			gl.createParallelGroup()
			.addComponent(layers, 480, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		);
		pack();
		setLocationRelativeTo(null);
		final int inW = getWidth();
		final int inH = getHeight();
		setMinimumSize(new Dimension(inW, inH));
		
		initModel();
		smr.startAnimations();
		setVisible(true);
	}
	private void setKeyboard() {
		JRootPane rp = getRootPane();
		
		KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0, false);
		rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "F2");
		rp.getActionMap().put("F2", new AbstractAction() { 
			/** */
			private static final long serialVersionUID = -5381260756829107852L;
			public void actionPerformed(ActionEvent e) { onF2Action(); }});
		
		ks = KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0, false);
		rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "F3");
		rp.getActionMap().put("F3", new AbstractAction() { 
			/** */
			private static final long serialVersionUID = -5381260756829107852L;
			public void actionPerformed(ActionEvent e) { onF3Action(); }});

		ks = KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0, false);
		rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "F7");
		rp.getActionMap().put("F7", new AbstractAction() { 
			/** */
			private static final long serialVersionUID = -5381260756829107852L;
			public void actionPerformed(ActionEvent e) { onF7Action(); }});

	}
	/**
	 * Sets action listeners on the various screens.
	 */
	private void setListeners() {
		smr.setOnColonyClicked(new BtnAction() { public void invoke() { onStarmapColony(); }});
		smr.setOnInformationClicked(new BtnAction() { public void invoke() { onStarmapInfo(); }});
		pr.setOnStarmapClicked(new BtnAction() { public void invoke() { onColonyStarmap(); }});
		pr.setOnInformationClicked(new BtnAction() { public void invoke() { onColonyInfo(); }});
		ir.setOnStarmapClicked(new BtnAction() { public void invoke() { onInfoStarmap(); }});
		ir.setOnColonyClicked(new BtnAction() { public void invoke() { onInfoColony(); }});
		pr.setOnPlanetsClicked(new BtnAction() { public void invoke() { onColonyPlanets(); }});
	}
	private void onStarmapColony() {
		smr.setVisible(false);
		pr.setVisible(true);
		layers.validate();
	}
	private void onStarmapInfo() {
		ir.setScreenButtonsFor(InfoScreen.PLANETS);
		ir.setVisible(true);
		layers.validate();
	}
	private void onColonyStarmap() {
		pr.setVisible(false);
		smr.setVisible(true);
		layers.validate();
	}
	private void onColonyPlanets() {
		ir.setScreenButtonsFor(InfoScreen.PLANETS);
		ir.setVisible(true);
		layers.validate();
	}
	private void onColonyInfo() {
		ir.setScreenButtonsFor(InfoScreen.COLONY_INFORMATION);
		ir.setVisible(true);
		layers.validate();
	}
	private void onInfoStarmap() {
		smr.setVisible(true);
		pr.setVisible(false);
		ir.setVisible(false);
		layers.validate();
	}
	private void onInfoColony() {
		smr.setVisible(false);
		pr.setVisible(true);
		ir.setVisible(false);
		layers.validate();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args)  throws Exception {
		String root = ".";
		if (args.length > 0) {
			root = args[0];
		}
		File file = new File(root + "/IMPERIUM.EXE");
		if (!file.exists()) {
			JOptionPane.showMessageDialog(null, "Please place this program into the Imperium Galactica directory or specify the location via the first command line parameter.");
			return;
		}
		final String fRoot = root;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Main m = new Main();
				m.initialize(fRoot);
			}
		});
	}
	private void onF2Action() {
		if (!smr.isVisible()) {
			uis.playSound("Starmap");
			smr.setVisible(true);
			pr.setVisible(false);
			ir.setVisible(false);
			layers.validate();
		}
	}
	private void onF3Action() {
		if (!pr.isVisible()) {
			uis.playSound("Colony");
			smr.setVisible(false);
			pr.setVisible(true);
			ir.setVisible(false);
			layers.validate();
		}
	}
	private void onF7Action() {
		if (!ir.isVisible()) {
			if (smr.isVisible()) {
				uis.playSound("Planets");
				ir.setScreenButtonsFor(InfoScreen.PLANETS);
			} else
			if (pr.isVisible()) {
				uis.playSound("ColonyInformation");
				ir.setScreenButtonsFor(InfoScreen.COLONY_INFORMATION);
			}
			ir.setVisible(true);
			layers.validate();
		}
	}
	/** Initialize model to test model dependand rendering. */
	private void initModel() {
		for (SurfaceType st : SurfaceType.values()) {
			GMPlanet p = new GMPlanet();
			p.name = "Planet " + st.surfaceIndex;
			p.radarRadius = 50;
			p.showName = true;
			p.showRadar = true;
			p.surfaceType = st;
			p.surfaceVariant = 1;
			p.visible = true;
			p.x = 100 + st.surfaceIndex * 50;
			p.y = 100 + st.surfaceIndex * 50;
			p.nameColor = TextGFX.GALACTIC_EMPIRE_ST;
			p.rotationDirection = st.surfaceIndex % 2 == 0;
			smr.planets.add(p);
		}
	}
}
