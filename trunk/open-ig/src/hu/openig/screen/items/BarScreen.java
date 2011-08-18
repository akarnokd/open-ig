/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.core.Act;
import hu.openig.model.Screens;
import hu.openig.model.TalkPerson;
import hu.openig.model.TalkSpeech;
import hu.openig.model.TalkState;
import hu.openig.model.WalkPosition;
import hu.openig.model.WalkTransition;
import hu.openig.render.RenderTools;
import hu.openig.render.TextRenderer;
import hu.openig.screen.ScreenBase;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Type;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;



/**
 * The bar screen.
 * @author akarnokd, 2010.01.11.
 */
public class BarScreen extends ScreenBase {
	/** The panel base rectangle. */
	final Rectangle base = new Rectangle();
	/** Display the talk image instead of the empty bar? */
	public boolean enableTalk = true;
	/** Indicator if the level 2 talk should be the doctor. */
	public boolean doctorFirst = true;
	/** We are in talk mode. */
	boolean talkMode;
	/** The talk screen for level 4 doctor. */
	BufferedImage talkDoctor;
	/** The talk screen for level 2 kelly. */
	BufferedImage talkKelly;
	/** The talk screen for level 3 brian. */
	BufferedImage talkBrian;
	/** The talk screen for level 2 psychologist. */
	BufferedImage talkPhsychologist;
	/** The level 2 bar. */
	BufferedImage bar2;
	/** The level 3 bar. */
	BufferedImage bar3;
	/** The current talk person. */
	TalkPerson person;
	/** The current talk state. */
	TalkState state;
	/** The next talk state. */
	TalkState next;
	/** The highlighted speech. */
	TalkSpeech highlight;
	/** The transition the mouse is pointing at. */
	WalkTransition pointerTransition;
	/** The list of choices. */
	final List<Choice> choices = new ArrayList<Choice>();
	/** A talk choice. */
	class Choice {
		/** The target rendering rectangle. */
		final Rectangle rect = new Rectangle();
		/** The choice text rows. */
		final List<String> rows = new ArrayList<String>();
		/**
		 * Paint the rows.
		 * @param g2 the graphics
		 * @param x0 the paint origin
		 * @param y0 the paint origin
		 * @param color the rendering color.
		 */
		public void paintTo(Graphics2D g2, int x0, int y0, int color) {
			int y = y0 + rect.y;
			for (int i = 0; i < rows.size(); i++) {
				commons.text().paintTo(g2, x0 + rect.x, y + 3, 14, color, rows.get(i));
				y += 20;
			}
		}
		/**
		 * Test if the mouse position in the rectangle.
		 * @param x0 the paint origin
		 * @param y0 the paint origin
		 * @param mouse the mouse coordinate
		 * @return true if in the rectangle
		 */
		public boolean test(int x0, int y0, Point mouse) {
			return rect.contains(mouse.x - x0, mouse.y - y0);
		}
	}
	/**
	 * Set the next talk state. 
	 * @param state the state
	 */
	void setState(TalkState state) {
		this.state = state;
		if (state != null) {
			int h0 = state.picture.getHeight();
			int w0 = state.picture.getWidth();
			int h = 0;
			choices.clear();
			int maxTw = 0;
			for (TalkSpeech ts : state.speeches) {
				Choice c = new Choice();
				int tw = commons.text().wrapText(get(ts.text), w0, 14, c.rows);
				maxTw = Math.max(maxTw, tw);
				int dh = c.rows.size() * 20;
				c.rect.setBounds(0, h, tw, dh);
				h += dh + 20;
				choices.add(c);
			}
			// center them all
			int h1 = (h0 - h + 20) / 2;
			int w1 = (w0 - maxTw) / 2;
			for (Choice c : choices) {
				c.rect.x += w1;
				c.rect.y += h1;
			}
		} else {
			talkMode = false;
		}
	}
	@Override
	public void onInitialize() {
		base.setBounds(0, 0, 
				640, 480 - 38);

		talkDoctor = rl.getImage("bar/bar_doctor");
		talkPhsychologist = rl.getImage("bar/bar_phsychologist");
		talkBrian = rl.getImage("bar/bar_brian");
		talkKelly = rl.getImage("bar/bar_kelly");
		bar2 = rl.getImage("flagship/bar_level_2");
		bar3 = rl.getImage("flagship/bar_level_3");
	}

	@Override
	public void onEnter(Screens mode) {
		talkMode = false;
		choices.clear();
	}

	@Override
	public void onLeave() {
		talkMode = false;
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onResize() {
		RenderTools.centerScreen(base, width, height, true);
		
	}
	@Override
	public void draw(Graphics2D g2) {
		RenderTools.darkenAround(base, width, height, g2, 0.5f, true);

		if (talkMode && state != null && state.picture != null) {
			g2.drawImage(state.picture, base.x, base.y, null);
			int idx = 0;
			for (Choice c : choices) {
				TalkSpeech ts = state.speeches.get(idx);
				int color = ts == highlight ? TextRenderer.WHITE : (ts.spoken ? TextRenderer.GRAY : TextRenderer.YELLOW);
				c.paintTo(g2, base.x, base.y, color);
				idx++;
			}
		} else {
			if (enableTalk) {
				g2.drawImage(getTalk(), base.x, base.y, null);
			} else {
				if (world().level == 2) {
					g2.drawImage(bar2, base.x, base.y, null);
				} else {
					g2.drawImage(bar3, base.x, base.y, null);
				}
			}
		}
		if (!talkMode) {
			if (pointerTransition != null) {
				ScreenUtils.drawTransitionLabel(g2, pointerTransition, base, commons);
			}
		}
		
		super.draw(g2);
	}
	/** @return Retrieve the talk image. */
	BufferedImage getTalk() {
		if (doctorFirst && world().level == 2) {
			return talkPhsychologist;
		} else
		if (!doctorFirst && world().level == 2) {
			return talkKelly;
		} else
		if (world().level == 3) {
			return talkBrian;
		} else
		if (world().level == 4) {
			return talkDoctor;
		}
		return bar3;
	}
	/** @return the current talk person. */
	TalkPerson getPerson() {
		if (doctorFirst && world().level == 2) {
			return world().talks.persons.get("phsychologist");
		} else
		if (!doctorFirst && world().level == 2) {
			return world().talks.persons.get("kelly");
		} else
		if (world().level == 3) {
			return world().talks.persons.get("brian");
		} else
		if (world().level == 4) {
			return world().talks.persons.get("doctor");
		}
		return null;
	}
	@Override
	public boolean mouse(UIMouse e) {
		if (e.has(Type.CLICK)) {
			if (talkMode) {
				int idx = 0;
				Point pt = new Point(e.x, e.y);
				for (Choice c : choices) {
					if (c.test(base.x, base.y, pt)) {
						TalkSpeech ts = state.speeches.get(idx);
						next = person.states.get(ts.to);
						ts.spoken = true;
						commons.control().playVideos(new Act() {
							@Override
							public void act() {
								setState(next);
								askRepaint();
							}
						}, ts.media);
						break;
					}
					idx++;
				}
				askRepaint();
			} else
			if (enableTalk && e.within(base.x, base.y, base.width, 400)) {
				// FIXME talkmode
				TalkPerson tp = getPerson();
				if (tp != null) {
					talkMode = true;
					person = tp;
					setState(tp.states.get(TalkState.START));
					askRepaint();
				}
			}
		} else
		if (e.has(Type.MOVE) || e.has(Type.DRAG)) {
			if (talkMode) {
				Point pt = new Point(e.x, e.y);
				int idx = 0;
				highlight = null;
				for (Choice c : choices) {
					if (c.test(base.x, base.y, pt)) {
						highlight = state.speeches.get(idx);
						askRepaint();
						break;
					}
					idx++;
				}
			} else {
				
				WalkTransition prev = pointerTransition;
				pointerTransition = null;
				WalkPosition position = ScreenUtils.getWalk("*bar", world());
				for (WalkTransition wt : position.transitions) {
					if (wt.area.contains(e.x - base.x, e.y - base.y)) {
						pointerTransition = wt;
						break;
					}
				}
				if (prev != pointerTransition) {
					askRepaint();
				}
			}
		} else
		if (!base.contains(e.x, e.y) && e.has(Type.DOWN)) {
			hideSecondary();
			return true;
		} else
		if (e.has(Type.DOWN) && !talkMode) {
			WalkPosition position = ScreenUtils.getWalk("*bar", world());
			for (WalkTransition wt : position.transitions) {
				if (wt.area.contains(e.x - base.x, e.y - base.y)) {
					ScreenUtils.doTransition(position, wt, commons);
					return true;
				}
			}
		}
		return super.mouse(e);
	}
	@Override
	public Screens screen() {
		return Screens.BAR;
	}
	@Override
	public void onEndGame() {
		// TODO Auto-generated method stub
		
	}
}
