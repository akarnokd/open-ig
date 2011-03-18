/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screens;

import hu.openig.core.Act;
import hu.openig.core.ResourceType;
import hu.openig.render.RenderTools;
import hu.openig.ui.UIImage;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UIVideoImage;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

import javax.swing.Timer;



/**
 * The combined research and production screen.
 * @author akarnokd, 2010.01.11.
 */
public class ResearchProductionScreen extends ScreenBase {
	/** The equipment slot locations. */
	final List<TechnologySlot> slots = new ArrayList<TechnologySlot>();
	/** The rolling disk animation timer. */
	Timer animation;
	/** The current animation step counter. */
	int animationStep;
	/** The panel base rectangle. */
	final Rectangle base = new Rectangle();
	/** The add production button. */
	UIImageButton addButton;
	/** The remove production button. */
	UIImageButton removeButton;
	/** The empty button. */
	UIImage emptyButton;
	/** The production button. */
	UIImageButton productionButton;
	/** The research button. */
	UIImageButton researchButton;
	/** The equipment button. */
	UIImageButton equipmentButton;
	/** The bridge button. */
	UIImageButton bridgeButton;
	/** The video output. */
	UIVideoImage video;
	/** The video renderer. */
	VideoRenderer videoRenderer;
	/** Screen mode. */
	public enum RPMode {
		/** Production. */
		PRODUCTION,
		/** Research. */
		RESEARCH,
	}
	/** The screen mode mode. */
	RPMode mode;
	@Override
	public void onInitialize() {
		base.setBounds(0, 0, 
				commons.equipment().base.getWidth(), commons.equipment().base.getHeight());
		animation = new Timer(100, new Act() {
			@Override
			public void act() {
				doAnimation();
			}
		});
		
		addButton = new UIImageButton(commons.research().add);
		removeButton = new UIImageButton(commons.research().remove);
		removeButton.visible(false);
		emptyButton = new UIImage(commons.research().emptyElevated);
		emptyButton.visible(false);
		bridgeButton = new UIImageButton(commons.common().bridgeButton);
		researchButton = new UIImageButton(commons.research().research);
		productionButton = new UIImageButton(commons.research().production);
		equipmentButton = new UIImageButton(commons.research().equipmentButton);

		video = new UIVideoImage();
		
		// TODO for testing purposes only!
		for (int i = 0; i < 6; i++) {
			TechnologySlot ts = new TechnologySlot(commons);
			ts.name = "TODO";
			ts.inventory = 1;
			ts.cost = 1000;
			ts.researching = true;
			ts.percent = 0.5f;
			ts.visible(true);
			ts.missingLab = true;
			ts.image = rl.getImage(commons.language(), "inventions/spaceships/fighters/fighter_" + (i + 1) + "");
			slots.add(ts);
		}
		slots.get(0).available = true;
		slots.get(0).researching = false;
		
		slots.get(2).missingLab = false;
		slots.get(2).missingPrerequisite = true;

		slots.get(3).visible(false);
		
		slots.get(4).notResearchable = true;
		
		slots.get(5).visible(false);

		researchButton.onClick = new Act() {
			@Override
			public void act() {
				setMode(RPMode.RESEARCH);
			}
		};
		productionButton.onClick = new Act() {
			@Override
			public void act() {
				setMode(RPMode.PRODUCTION);
			}
		};
		equipmentButton.onClick = new Act() {
			@Override
			public void act() {
				commons.control.displaySecondary(Screens.EQUIPMENT);
			}
		};
		addButton.onClick = new Act() {
			@Override
			public void act() {
				// TODO
			}
		};
		removeButton.onClick = new Act() {
			@Override
			public void act() {
				// TODO
			}
		};
		bridgeButton.onClick = new Act() {
			@Override
			public void act() {
				commons.control.displayPrimary(Screens.BRIDGE);
			}
		};
		
		addThis();
		add(slots);
	}
	/**
	 * Update animating components.
	 */
	void doAnimation() {
		if (animationStep == Integer.MAX_VALUE) {
			animationStep = 0;
		} else {
			animationStep ++;
		}
		for (TechnologySlot sl : slots) {
			sl.animationStep = animationStep;
		}
		askRepaint();
	}

	@Override
	public void onEnter(Object mode) {
		onResize();
		if (mode == null || mode == RPMode.PRODUCTION) {
			setMode(RPMode.PRODUCTION);
		} else {
			setMode(RPMode.RESEARCH);
		}
		animation.start();
		
		videoRenderer = new VideoRenderer(new CyclicBarrier(1), new CyclicBarrier(1), video, 
				rl.get(commons.language(), "technology/spaceships/fighters/fighter_1", ResourceType.VIDEO), "Research-Production-Video");
		videoRenderer.setRepeat(true);
//		videoRenderer.setFpsOverride(15d);
		videoRenderer.start();
	}

	@Override
	public void onLeave() {
		animation.stop();
		if (videoRenderer != null) {
			videoRenderer.stopPlayback();
		}
	}

	@Override
	public void onFinish() {
		onLeave();
		videoRenderer = null;
		animation = null;
	}

	@Override
	public void onResize() {
		RenderTools.centerScreen(base, width, height, true);

		addButton.location(base.x + 535, base.y + 303 - 20);
		removeButton.location(addButton.location());
		emptyButton.location(addButton.location());

		video.location(base.x + 2, base.y + 2);
		video.size(316, 196);
		
		equipmentButton.location(addButton.x, addButton.y + addButton.height);
		
		productionButton.location(equipmentButton.x, equipmentButton.y + equipmentButton.height);
		researchButton.location(productionButton.location());
		
		bridgeButton.location(researchButton.x, researchButton.y + researchButton.height);
		
		for (int i = 0; i < 6; i++) {
			slots.get(i).location(base.x + 2 + i * 106, base.y + 219 - 20);
			slots.get(i).size(106, 82);
		}

	}
	@Override
	public void draw(Graphics2D g2) {
		RenderTools.darkenAround(base, width, height, g2, 0.5f, true);
		g2.drawImage(commons.research().basePanel, base.x, base.y, null);
		super.draw(g2);
	}
	/**
	 * Change and set the visibility of components based on the mode.
	 * @param m the new mode
	 */
	void setMode(RPMode m) {
		this.mode = m;
		switch (mode) {
		case PRODUCTION:
			productionButton.visible(false);
			researchButton.visible(true);
			break;
		case RESEARCH:
			productionButton.visible(true);
			researchButton.visible(false);
			break;
		default:
		}
	}
}
