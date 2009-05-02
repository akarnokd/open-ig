/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.gfx;

import hu.openig.core.Btn;
import hu.openig.core.BtnAction;
import hu.openig.gfx.OptionsGFX.Opts;
import hu.openig.sound.UISounds;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JComponent;

/**
 * Option renderer.
 * @author karnokd, 2009.02.08.
 * @version $Revision 1.0$
 */
public class OptionsRenderer extends JComponent implements MouseMotionListener, MouseListener {
	/** */
	private static final long serialVersionUID = -1930544023996086129L;
	/** The menu graphics. */
	private OptionsGFX gfx;
	/** The text graphics. */
	private TextGFX text;
	/** The last width. */
	private int lastWidth;
	/** The last height. */
	private int lastHeight;
	/** The background picture selector. */
	private int picture;
	/** Indicator that the picture changed recently. */
	private boolean pictureChanged;
	/** The load button. */
	private Btn btnLoad;
	/** The save button. */
	private Btn btnSave;
	/** The exit button. */
	private Btn btnExit;
	/** The various buttons. */
	private final List<Btn> buttons = new ArrayList<Btn>();
	/** Buttons which change state on click.*/
	private final List<Btn> toggleButtons = new ArrayList<Btn>();
	/** Reequip planets checkbox. */
	private Btn btnReequipPlanets;
	/** Building names checkbox. */
	private Btn btnBuildingNames;
	/** Building damage checkbox. */
	private Btn btnBuildingDamage;
	/** Building damage battle checkbox. */
	private Btn btnBuildingDamageBattle;
	/** Auto scroll checkbox. */
	private Btn btnAutoScroll;
	/** Reequip rockets checkbox. */
	private Btn btnReequipRockets;
	/** Repair buildings checkbox. */
	private Btn btnRepairBuildings;
	/** Tax info checkbox. */
	private Btn btnTaxInfo;
	/** Computer voice checkox. */
	private Btn btnComputerVoice;
	/** Animations checkbox. */
	private Btn btnAnimations;
	/** Adjust music action. */
	private BtnAction onAdjustMusic;
	/** Adjust sound action. */
	private BtnAction onAdjustSound;
	/** Music volume. */
	private float musicVolume;
	/** Audio volume. */
	private float audioVolume;
	/** The currently selected options. */
	private Opts currentOpts;
	/** The user interface sound. */
	private UISounds uis;
	/** Audio adjusting. */
	private boolean audioAdjusting;
	/** Music adjusting. */
	private boolean musicAdjusting;
	/**
	 * Constructor. Initializes the graphics fields.
	 * @param gfx the menu graphics
	 * @param text the text graphics
	 * @param uis the user interface sound
	 */
	public OptionsRenderer(OptionsGFX gfx, TextGFX text, UISounds uis) {
		this.gfx = gfx;
		this.text = text;
		this.uis = uis;
		addMouseListener(this);
		addMouseMotionListener(this);
		initButtons();
	}
	/** Initialize buttons. */
	private void initButtons() {
		btnLoad = new Btn();
		buttons.add(btnLoad);
		btnSave = new Btn();
		buttons.add(btnSave);
		btnExit = new Btn();
		buttons.add(btnExit);
		btnReequipPlanets = new Btn();
		toggleButtons.add(btnReequipPlanets);
		btnBuildingNames = new Btn();
		toggleButtons.add(btnBuildingNames);
		btnBuildingDamage = new Btn();
		toggleButtons.add(btnBuildingDamage);
		btnBuildingDamageBattle = new Btn();
		toggleButtons.add(btnBuildingDamageBattle);
		btnAutoScroll = new Btn();
		toggleButtons.add(btnAutoScroll);
		btnReequipRockets = new Btn();
		toggleButtons.add(btnReequipRockets);
		btnRepairBuildings = new Btn();
		toggleButtons.add(btnRepairBuildings);
		btnTaxInfo = new Btn();
		toggleButtons.add(btnTaxInfo);
		btnComputerVoice = new Btn();
		toggleButtons.add(btnComputerVoice);
		btnAnimations = new Btn();
		toggleButtons.add(btnAnimations);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		int w = getWidth();
		int h = getHeight();
		
		Composite cp = g2.getComposite();
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, w, h);
		g2.setComposite(cp);
		
		if (w != lastWidth || h != lastHeight || pictureChanged) {
			lastWidth = w;
			lastHeight = h;
			// if the render window changes, re-zoom to update scrollbars
			updateRegions();
			pictureChanged = false;
		}
		BufferedImage bimg = currentOpts.options;
		g2.drawImage(bimg, currentOpts.background.x, currentOpts.background.y, null);
		
		if (btnLoad.disabled) {
			g2.drawImage(currentOpts.btnLoadDisabled, currentOpts.btnLoad.x, currentOpts.btnLoad.y, null);
		} else
		if (btnLoad.down) {
			g2.drawImage(currentOpts.btnLoadDown, currentOpts.btnLoad.x, currentOpts.btnLoad.y, null);
		}
		
		if (btnSave.disabled) {
			g2.drawImage(currentOpts.btnSaveDisabled, currentOpts.btnSave.x, currentOpts.btnSave.y, null);
		} else
		if (btnSave.down) {
			g2.drawImage(currentOpts.btnSaveDown, currentOpts.btnSave.x, currentOpts.btnSave.y, null);
		}
		
		if (btnExit.down) {
			g2.drawImage(currentOpts.btnExitDown, currentOpts.btnExit.x, currentOpts.btnExit.y, null);
		}
		
		drawCheckMark(g2, currentOpts, btnReequipPlanets);
		drawCheckMark(g2, currentOpts, btnBuildingNames);
		drawCheckMark(g2, currentOpts, btnBuildingDamage);
		drawCheckMark(g2, currentOpts, btnBuildingDamageBattle);
		drawCheckMark(g2, currentOpts, btnAutoScroll);
		
		drawCheckMark(g2, currentOpts, btnReequipRockets);
		drawCheckMark(g2, currentOpts, btnRepairBuildings);
		drawCheckMark(g2, currentOpts, btnTaxInfo);
		drawCheckMark(g2, currentOpts, btnComputerVoice);
		drawCheckMark(g2, currentOpts, btnAnimations);
		
		// draw sliders
		
		g2.drawImage(currentOpts.slider, currentOpts.btnMusic.x, (int)(currentOpts.btnMusic.y + (1 - musicVolume) * currentOpts.btnMusic.height) - currentOpts.slider.getHeight() / 2, null);
		g2.drawImage(currentOpts.slider, currentOpts.btnAudio.x, (int)(currentOpts.btnAudio.y + (1 - audioVolume) * currentOpts.btnAudio.height) - currentOpts.slider.getHeight() / 2, null);
		
		for (int i = 0; i < 20; i++) {
			text.paintTo(g2, currentOpts.listArea.x + 1, currentOpts.listArea.y + i * 13, 10, TextGFX.GREEN, String.format("%d.", i + 1));
		}
		
	}
	/**
	 * Draw checkmark if necessary.
	 * @param g2 the graphics object
	 * @param o the options
	 * @param b button
	 */
	private void drawCheckMark(Graphics2D g2, Opts o, Btn b) {
		if (b.down) {
			g2.drawImage(o.checkmark, b.rect.x, b.rect.y + b.rect.height - o.checkmark.getHeight(), null);
		}
	}
	/**
	 * Update important rendering regions.
	 */
	private void updateRegions() {
		int w = getWidth();
		int h = getHeight();
		switch (picture) {
		case 0:
			gfx.setLocations1(currentOpts, (w - currentOpts.options.getWidth()) / 2, (h - currentOpts.options.getHeight()) / 2);
			break;
		case 1:
			gfx.setLocations2(currentOpts, (w - currentOpts.options.getWidth()) / 2, (h - currentOpts.options.getHeight()) / 2);
			break;
		default:
		}
		btnLoad.rect.setBounds(currentOpts.btnLoad);
		btnSave.rect.setBounds(currentOpts.btnSave);
		btnExit.rect.setBounds(currentOpts.btnExit);
		
		btnReequipPlanets.setBounds(currentOpts.btnReequipPlanets);
		btnBuildingNames.setBounds(currentOpts.btnBuildingNames);
		btnBuildingDamage.setBounds(currentOpts.btnBuildingDamage);
		btnBuildingDamageBattle.setBounds(currentOpts.btnBuildingDamageBattle);
		btnAutoScroll.setBounds(currentOpts.btnAutoScroll);
		btnReequipRockets.setBounds(currentOpts.btnReequipRockets);
		btnRepairBuildings.setBounds(currentOpts.btnRepairBuildings);
		btnTaxInfo.setBounds(currentOpts.btnTaxInfo);
		btnComputerVoice.setBounds(currentOpts.btnComputerVoice);
		btnAnimations.setBounds(currentOpts.btnAnimations);
	}
	/**
	 * Set the background picture based on index.
	 * @param index the picture index
	 */
	public void setPicture(int index) {
		if (index >= 0 && index < gfx.opts.length) {
			picture = index;
			pictureChanged = true;
			currentOpts = gfx.opts[picture];
			repaint();
		}
	}
	/**
	 * Returns the current background image index.
	 * @return the current background image index
	 */
	public int getPicture() {
		return picture;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		Point pt = e.getPoint();
		if (musicAdjusting) {
			musicVolume = Math.min(Math.max(0, 1 - ((pt.y - currentOpts.btnMusic.y) / (float)currentOpts.btnMusic.height)), 1);
			if (onAdjustMusic != null) {
				onAdjustMusic.invoke();
			}
			repaint(currentOpts.volumeRect);
		}
		if (audioAdjusting) {
			audioVolume = Math.min(Math.max(0, 1 - ((pt.y - currentOpts.btnAudio.y) / (float)currentOpts.btnAudio.height)), 1);
			if (onAdjustSound != null) {
				onAdjustSound.invoke();
			}
			repaint(currentOpts.volumeRect);
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		Point pt = e.getPoint();
		if (e.getButton() == MouseEvent.BUTTON1) {
			for (Btn b : buttons) {
				if (b.test(pt)) {
					b.click();
					repaint(b.rect);
				}
			}
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
		
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseExited(MouseEvent e) {
		
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		Point pt = e.getPoint();
		if (e.getButton() == MouseEvent.BUTTON1) {
			for (Btn b : buttons) {
				if (b.test(pt)) {
					b.down = true;
					repaint(b.rect);
				}
			}
			for (Btn b : toggleButtons) {
				if (b.test(pt)) {
					b.down = !b.down;
					b.click();
					//repaint(b.rect);
					repaint(currentOpts.settingsRect);
				}
			}
			if (currentOpts.btnMusic.contains(pt)) {
				musicAdjusting = true;
				musicVolume = 1 - ((pt.y - currentOpts.btnMusic.y) / (float)currentOpts.btnMusic.height);
				if (onAdjustMusic != null) {
					onAdjustMusic.invoke();
				}
				repaint(currentOpts.volumeRect);
			}
			if (currentOpts.btnAudio.contains(pt)) {
				audioAdjusting = true;
				audioVolume = 1 - ((pt.y - currentOpts.btnAudio.y) / (float)currentOpts.btnAudio.height);
				if (onAdjustSound != null) {
					onAdjustSound.invoke();
				}
				repaint(currentOpts.volumeRect);
			}
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		//Point pt = e.getPoint();
		boolean needRepaint = false;
		for (Btn b : buttons) {
			needRepaint |= b.down;
			if (b.down) {
				b.down = false;
				repaint(b.rect);
			}
		}
		if (musicAdjusting) {
			musicAdjusting = false;
			repaint(currentOpts.btnMusic);
		}
		if (audioAdjusting) {
			audioAdjusting = false;
			repaint(currentOpts.btnAudio);
			uis.playSound("Local");
		}
	}
	/**
	 * Select a random start picture.
	 */
	public void setRandomPicture() {
		setPicture(new Random().nextInt(gfx.opts.length));
	}
	/**
	 * Set the music adjustment action.
	 * @param onAdjustMusic the event handler
	 */
	public void setOnAdjustMusic(BtnAction onAdjustMusic) {
		this.onAdjustMusic = onAdjustMusic;
	}
	/**
	 * Returns the adjust music event handler.
	 * @return the adjust music event handler
	 */
	public BtnAction getOnAdjustMusic() {
		return onAdjustMusic;
	}
	/**
	 * Set the sound adjustment action.
	 * @param onAdjustSound the event handler
	 */
	public void setOnAdjustSound(BtnAction onAdjustSound) {
		this.onAdjustSound = onAdjustSound;
	}
	/**
	 * Returns the adjust sound event handler.
	 * @return the adjust sound event handler
	 */
	public BtnAction getOnAdjustSound() {
		return onAdjustSound;
	}
	/**
	 * Set the music volume on a linear scale from 0 to 1.
	 * @param musicVolume the music volume of [0, 1]
	 */
	public void setMusicVolume(float musicVolume) {
		this.musicVolume = musicVolume;
	}
	/**
	 * Returns the current linear music volume.
	 * @return the current linear music volume
	 */
	public float getMusicVolume() {
		return musicVolume;
	}
	/**
	 * Set audio effects volume on a linear scale from 0 to 1.
	 * @param audioVolume the audio volume of [0, 1]
	 */
	public void setAudioVolume(float audioVolume) {
		this.audioVolume = Math.min(Math.max(0, audioVolume), 1);
	}
	/**
	 * Returns the current linear audio volume.
	 * @return the current linear audio volume
	 */
	public float getAudioVolume() {
		return audioVolume;
	}
	/**
	 * Set the on exit action.
	 * @param action the action
	 */
	public void setOnExit(BtnAction action) {
		btnExit.onClick = action;
	}
	/**
	 * Set the on load action.
	 * @param action the action
	 */
	public void setOnLoad(BtnAction action) {
		btnLoad.onClick = action;
	}
	/**
	 * Set the on save action.
	 * @param action the action
	 */
	public void setOnSave(BtnAction action) {
		btnSave.onClick = action;
	}
}
