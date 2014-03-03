/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.core.Action0;
import hu.openig.core.Action1;
import hu.openig.model.Screens;
import hu.openig.model.Trait;
import hu.openig.model.TraitKind;
import hu.openig.model.Traits;
import hu.openig.render.RenderTools;
import hu.openig.render.TextRenderer;
import hu.openig.screen.CommonResources;
import hu.openig.screen.ScreenBase;
import hu.openig.ui.HorizontalAlignment;
import hu.openig.ui.UICheckBox;
import hu.openig.ui.UIContainer;
import hu.openig.ui.UIGenericButton;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UILabel;
import hu.openig.ui.UIScrollBox;
import hu.openig.ui.VerticalAlignment;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The trait editing screen.
 * @author akarnokd, 2012.08.18.
 */
public class TraitScreen extends ScreenBase {
	/** The panel base rectangle. */
	final Rectangle base = new Rectangle(0, 0, 640, 640);
	/** 
	 * The action to invoke once the user has chosen the traits.
	 * Called with null if the user cancelled.
	 */
	public Action1<Traits> onComplete;
	/** The scroll box. */
	UIScrollBox scroll;
	/** The select traits label. */
	UILabel traitsLabel;
	/** The points label. */
	UILabel pointsLabel;
	/** The points value. */
	UILabel pointsValue;
	/** The traits list control. */
	TraitsList traitsList;
	/** Accept selection. */
	UIGenericButton ok;
	/** Accept selection. */
	UIGenericButton reset;
	/** Cancel selection. */
	UIGenericButton cancel;
	@Override
	public void onResize() {
		base.width = commons.common().infoEmptyTop.getWidth();
		base.y = 10;
		base.height = height - 20;

		RenderTools.centerScreen(base, width, height, true);

		traitsLabel.location(base.x + 10, base.y + 10);
		scroll.location(base.x + 10, base.y + traitsLabel.y + traitsLabel.height + 10);
		scroll.width = base.width - 20;
		traitsList.width = scroll.width - commons.database().arrowUp[0].getWidth() - 10;
		pointsLabel.location(base.x + 10, base.y + base.height - 70);
		
		pointsValue.location(base.x + base.width - 10 - base.width / 2, pointsLabel.y);
		pointsValue.width = base.width / 2;
		
		int gap = 30;
		int w = ok.width + cancel.width + gap * 2 + reset.width;
		ok.location(base.x + (640 - w) / 2, base.y + base.height - 40);
		reset.location(ok.x + ok.width + gap, ok.y);
		cancel.location(reset.x + reset.width + gap, ok.y);
		
		scroll.height = pointsLabel.y - scroll.y - 10;
		
		scroll.scrollBy(0);
		scroll.adjustButtons();
	}
	@Override
	public Screens screen() {
		return Screens.TRAITS;
	}

	@Override
	public void onInitialize() {
		traitsList = new TraitsList();

		UIImageButton up = new UIImageButton(commons.database().arrowUp);
		UIImageButton down = new UIImageButton(commons.database().arrowDown);
		
		scroll = new UIScrollBox(traitsList, 20, up, down);
		
		traitsLabel = new UILabel(get("traits.select_traits"), 20, commons.text());
		pointsLabel = new UILabel(get("traits.available_points"), 20, commons.text());
		pointsValue = new UILabel("", 20, commons.text());
		pointsValue.horizontally(HorizontalAlignment.RIGHT);
		pointsValue.color(TextRenderer.YELLOW);

		ok = new UIGenericButton(get("traits.ok"), commons.control().fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
		ok.onClick = new Action0() {
			@Override
			public void invoke() {
				if (onComplete != null) {
					try {
						onComplete.invoke(traitsList.selected());
					} finally {
						onComplete = null;
					}
				}
				hideSecondary();
			}
		};
		reset = new UIGenericButton(get("traits.reset"), commons.control().fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
		reset.onClick = new Action0() {
			@Override
			public void invoke() {
				traitsList.reset();
			}
		};
		cancel = new UIGenericButton(get("traits.cancel"), commons.control().fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
		cancel.onClick = new Action0() {
			@Override
			public void invoke() {
				if (onComplete != null) {
					try {
						onComplete.invoke(null);
					} finally {
						onComplete = null;
					}
				}
				hideSecondary();
			}
		};
		// TODO Auto-generated method stub

		addThis();
		components.remove(traitsList);
	}

	@Override
	public void onEnter(Screens mode) {
		traitsList.prepare(commons.traits());
		traitsList.x = 0;
		scroll.scrollBy(0);
		scroll.adjustButtons();
	}
	/**
	 * Update traits.
	 * @param traits the traits
	 */
	public void updateTraits(Traits traits) {
		traitsList.update(traits);
		scroll.scrollBy(0);
		scroll.adjustButtons();
	}

	@Override
	public void onLeave() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onEndGame() {
		// TODO Auto-generated method stub

	}
	@Override
	public void draw(Graphics2D g2) {
		RenderTools.darkenAround(base, width, height, g2, 0.5f, true);
		
		g2.setColor(Color.BLACK);
		g2.fill(base);
		g2.setColor(Color.GRAY);
		g2.draw(base);
		
		g2.drawLine(base.x, scroll.y - 5, base.x + base.width - 1, scroll.y - 5);
		g2.drawLine(base.x, scroll.y + scroll.height + 5, base.x + base.width - 1, scroll.y + scroll.height + 5);

		super.draw(g2);
	}
	/**
	 * A trait checkbox.
	 * @author akarnokd, 2012.08.18.
	 */
	public static class TraitCheckBox extends UICheckBox {
		/** The trait. */
		public final Trait trait;
		/**
		 * Creates the checkbox.
		 * @param trait the trait object
         * @param commons the common resources
		 */
		public TraitCheckBox(Trait trait, CommonResources commons) {
			super(commons.get(trait.label), 14, commons.common().checkmark, commons.text());
			this.trait = trait;
			vertically(VerticalAlignment.MIDDLE);
		}
	}
	/**
	 * The traits list container.
	 * @author akarnokd, 2012.08.18.
	 */
	public class TraitsList extends UIContainer {
		/** Negative value trait color. */
		private static final int PINK = 0xFFFF8080;
		/** The available traits. */
		Traits traits;
		/** The trait checkbox. */
		final List<TraitCheckBox> traitCheckBoxes = new ArrayList<>();
		/** The trait cost labels. */
		final List<UILabel> traitCosts = new ArrayList<>();
		/** The trait description labels. */
		final List<UILabel> traitDesc = new ArrayList<>();
		/**
		 * Prepare the trait list. 
		 * @param base the traits
		 */
		public void prepare(Traits base) {
			this.components.clear();
			traitCheckBoxes.clear();
			traitCosts.clear();
			traitDesc.clear();
			this.traits = base;
			int y = 0;
			int i = 0;
			for (final Trait tr : traits) {
				final TraitCheckBox tcb = new TraitCheckBox(tr, commons);
				UILabel tcl = new UILabel(format(tr.description, tr.value), 10, commons.text());
				UILabel tcc = new UILabel((tr.cost > 0 ? "+" : "") + tr.cost, 14, commons.text());
				
				Action0 onTraitClick = new Action0() {
					@Override
					public void invoke() {
						doSelectTrait();
					}
				};
				Action0 onTraitClick2 = new Action0() {
					@Override
					public void invoke() {
						tcb.selected(!tcb.selected());
						doSelectTrait();
					}
				};
				tcb.onChange = onTraitClick;
				tcl.onPress = onTraitClick2;
				tcc.onPress = onTraitClick2;
				
				tcb.height += 10;
				tcl.width = width - 30;
				tcl.wrap(true);
				tcl.height = tcl.getWrappedHeight();
				
				tcb.z = -1;
				
				tcb.location(0, y);
				tcc.location(width - tcc.width, y + 5);
				tcl.location(30, y + tcb.height);
				tcb.width = tcc.x;
				
				y += tcb.height + tcl.height + 5;
				
				if (tr.cost < 0) {
					tcb.color(PINK);
				} else {
					tcb.color(TextRenderer.GREEN);
				}
				tcl.color(tcb.color());
				tcc.color(tcb.color());
				
				this.add(tcl);
				this.add(tcb);
				this.add(tcc);
				
				traitCheckBoxes.add(tcb);
				traitCosts.add(tcl);
				traitDesc.add(tcc);
				
				i++;
			}
			if (i > 0) {
				height = y;
			} else {
				height = y - 5;
			}
			doSelectTrait();
		}
		
		/**
		 * @return Returns the list of selected traits.
		 */
		Traits selected() {
			Traits ts = new Traits();
			for (TraitCheckBox tcb : traitCheckBoxes) {
				if (tcb.selected()) {
					ts.add(tcb.trait);
				}
			}
			return ts;
		}
		/**
		 * Perform the selection logic.
		 */
		public void doSelectTrait() {
			int points = 0;
			loop:
			while (!Thread.currentThread().isInterrupted()) {
				Set<String> excludeIds = new HashSet<>();
				Set<TraitKind> excludeKinds = new HashSet<>();
				
				// collect exclusion settings
				for (TraitCheckBox tcb : traitCheckBoxes) {
					if (tcb.selected()) {
						excludeIds.addAll(tcb.trait.excludeIds);
						excludeKinds.addAll(tcb.trait.excludeKinds);
					}
				}
				points = commons.traits().initialPoints;
				for (TraitCheckBox tcb : traitCheckBoxes) {
					boolean enabled = !excludeIds.contains(tcb.trait.id) && !excludeKinds.contains(tcb.trait.kind);
					
					tcb.selected(tcb.selected() & enabled);
					tcb.enabled(enabled);
					
					if (tcb.selected()) {
						points -= tcb.trait.cost;
					}
				}
				if (points < 0) {
					for (TraitCheckBox tcb : traitCheckBoxes) {
						if (tcb.selected() && tcb.trait.cost > 0) {
							tcb.selected(false);
							continue loop;
						}
					}
					throw new AssertionError("Points remained negative?!");
				}
				for (TraitCheckBox tcb : traitCheckBoxes) {
					if (tcb.trait.cost > points && !tcb.selected()) {
						tcb.enabled(false);
					}
				}
				break;
			}
			
			// color components according to their state
			int i = 0;
			for (TraitCheckBox tcb : traitCheckBoxes) {
				if (tcb.selected()) {
					if (tcb.trait.cost < 0) {
						tcb.color(TextRenderer.RED);
					} else {
						tcb.color(TextRenderer.LIGHT_GREEN);
					}
					tcb.color(TextRenderer.WHITE);
				} else {
					if (tcb.trait.cost < 0) {
						tcb.color(PINK);
					} else {
						tcb.color(TextRenderer.GREEN);
					}
				}
				UILabel cost = traitCosts.get(i);
				UILabel desc = traitDesc.get(i);

				cost.color(tcb.color());
				cost.enabled(tcb.enabled());
				desc.color(tcb.color());
				desc.enabled(tcb.enabled());
				i++;
			}
			
			pointsValue.text(Integer.toString(points));
		}
		/**
		 * Reset the selections.
		 */
		void reset() {
			prepare(commons.traits());
		}
		/**
		 * Check the traits based on the given set.
		 * @param traits the traits
		 */
		public void update(Traits traits) {
			Set<String> ids = new HashSet<>();
			if (traits != null) {
				for (Trait t : traits) {
					ids.add(t.id);
				}
			}
			for (TraitCheckBox tcb : traitCheckBoxes) {
				tcb.selected(ids.contains(tcb.trait.id));
			}
			doSelectTrait();
		}
	}
	@Override
	public boolean keyboard(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			e.consume();
			cancel.onClick.invoke();
			return true;
		}
		return false;
	}
}
