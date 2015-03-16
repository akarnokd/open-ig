/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.core.Action0;
import hu.openig.core.Pair;
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
import hu.openig.ui.UIMouse.Button;
import hu.openig.ui.UIMouse.Type;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
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
	/** We are in talk mode. */
	boolean talkMode;
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
	final List<Choice> choices = new ArrayList<>();
	/** The state picture. */
	BufferedImage picture;
	/** The bar images. */
	BarImages images;
	/** Was the simulation running before entering into talk? */
	boolean simulationRunning;
	/** A talk choice. */
	class Choice {
		/** The target rendering rectangle. */
		final Rectangle rect = new Rectangle();
		/** The choice text rows. */
		final List<String> rows = new ArrayList<>();
		/**
		 * Paint the rows.
		 * @param g2 the graphics
		 * @param x0 the paint origin
		 * @param y0 the paint origin
		 * @param color the rendering color.
		 */
		public void paintTo(Graphics2D g2, int x0, int y0, int color) {
			int y = y0 + rect.y;
            for (String row : rows) {
                commons.text().paintTo(g2, x0 + rect.x, y + 3, 14, color, row);
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
			picture = rl.getImage(state.pictureName);
			int h0 = picture.getHeight();
			int w0 = picture.getWidth();
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
			picture = null;
			if (simulationRunning) {
				commons.simulation.resume();
			}
			world().scripting.onTalkCompleted();
		}
	}
	/**
	 * The bar images.
	 * @author akarnokd, Jan 18, 2012
	 */
	class BarImages {
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
		/** @return image of a person. */
		BufferedImage doctor() {
			if (talkDoctor == null) {
				talkDoctor = rl.getImage("bar/bar_doctor");
			}
			return talkDoctor;
		}
		/** @return image of a person. */
		BufferedImage kelly() {
			if (talkKelly == null) {
				talkKelly = rl.getImage("bar/bar_kelly");
			}
			return talkKelly;
		}
		/** @return image of a person. */
		BufferedImage brian() {
			if (talkBrian == null) {
				talkBrian = rl.getImage("bar/bar_brian");
			}
			return talkBrian;
			
		}
		/** @return image of a person. */
		BufferedImage phsychologist() {
			if (talkPhsychologist == null) { 
				talkPhsychologist = rl.getImage("bar/bar_phsychologist");
			}
			return talkPhsychologist;
			
		}
		/** @return image of a person. */
		BufferedImage bar2() {
			if (bar2 == null) {
				bar2 = rl.getImage("flagship/bar_level_2");
			}
			return bar2;
		}
		/** @return image of a person. */
		BufferedImage bar3() {
			if (bar3 == null) {
				bar3 = rl.getImage("flagship/bar_level_3");
			}
			return bar3;
		}
	}
	@Override
	public void onInitialize() {
		base.setBounds(0, 0, 
				640, 480 - 38);

	}

	@Override
	public void onEnter(Screens mode) {
		talkMode = false;
		choices.clear();
		images = new BarImages();
		simulationRunning = commons.simulation != null && !commons.simulation.paused();
	}

	@Override
	public void onLeave() {
		talkMode = false;
		images = null;
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onResize() {
		scaleResize(base, margin());
	}
	@Override
	public void draw(Graphics2D g2) {
		AffineTransform savea = scaleDraw(g2, base, margin());
		RenderTools.darkenAround(base, getInnerWidth(), getInnerHeight(), g2, 0.5f, true);

		if (talkMode && state != null && picture != null) {
			int dx = (base.width - picture.getWidth()) / 2;
			int dy = (base.height - picture.getHeight()) / 2;
			g2.drawImage(picture, base.x + dx, base.y + dy, null);
			int idx = 0;
			for (Choice c : choices) {
				TalkSpeech ts = state.speeches.get(idx);
				int color = ts == highlight ? TextRenderer.WHITE : (ts.spoken ? TextRenderer.GRAY : TextRenderer.YELLOW);
				c.paintTo(g2, base.x, base.y, color);
				idx++;
			}
		} else {
			if (world().currentTalk != null) {
				g2.drawImage(getTalk(), base.x, base.y, null);
				
				String clicktext = format("bar.start_talking", get(world().currentTalk));
				int tw = commons.text().getTextWidth(14, clicktext);
				
				int dh = base.y + (base.height - 14) / 2;
				int dw = base.x + (base.width - tw) / 2;
				
				commons.text().paintTo(g2, dw, dh, 14, TextRenderer.WHITE, clicktext);
				
			} else {
				if (world().level == 2) {
					g2.drawImage(images.bar2(), base.x, base.y, null);
				} else {
					g2.drawImage(images.bar3(), base.x, base.y, null);
				}
			}
		}
		if (!talkMode) {
			if (pointerTransition != null) {
				ScreenUtils.drawTransitionLabel(g2, pointerTransition, base, commons);
			}
		}
		
		super.draw(g2);
		g2.setTransform(savea);
	}
	@Override
	public boolean keyboard(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE && talkMode) {
			talkMode = false;
			if (simulationRunning) {
				commons.simulation.resume();
			}
			e.consume();
			return true;
		}
		return false;
	}

    /** @return Retrieve the talk image. */
	BufferedImage getTalk() {
		if ("phsychologist".equals(world().currentTalk)) {
			return images.phsychologist();
		} else
			if ("kelly".equals(world().currentTalk)) {
			return images.kelly();
		} else
			if ("brian".equals(world().currentTalk)) {
			return images.brian();
		} else
		if ("doctor".equals(world().currentTalk)) {
			return images.doctor();
		}
		return null;
	}
	/** @return the current talk person. */
	TalkPerson getPerson() {
		if (world().currentTalk != null) {
			return world().talks.persons.get(world().currentTalk);
		}
		return null;
	}
	@Override
	public boolean mouse(UIMouse e) {
		scaleMouse(e, base, margin());
		if (e.has(Type.DOWN)) {
			if (talkMode) {
				int idx = 0;
				Point pt = new Point(e.x, e.y);
				for (Choice c : choices) {
					if (c.test(base.x, base.y, pt)) {
						TalkSpeech ts = state.speeches.get(idx);
						next = person.states.get(ts.to);
						ts.spoken = true;
						((MovieScreen)commons.control().getScreen(Screens.MOVIE)).allowTransition = false;
						commons.control().playVideos(new Action0() {
							@Override
							public void invoke() {
								setState(next);
								commons.control().moveMouse();
								askRepaint();
							}
						}, ts.media);
						break;
					}
					idx++;
				}
				askRepaint();
			} else {
				if (world().currentTalk != null 
						&& e.within(base.x, base.y, base.width, 350)) {
					TalkPerson tp = getPerson();
					if (tp != null) {
						talkMode = true;
						person = tp;
						setState(tp.states.get(TalkState.START));
						askRepaint();
						simulationRunning = !commons.simulation.paused();
						commons.simulation.pause();
					}
				} else
				if (!base.contains(e.x, e.y)) {
					hideSecondary();
					return true;
				} else {
					WalkPosition position = ScreenUtils.getWalk("*bar", world());
					for (WalkTransition wt : position.transitions) {
						if (wt.area.contains(e.x - base.x, e.y - base.y)) {
							ScreenUtils.doTransition(position, wt, commons, e.has(Button.RIGHT));
							return true;
						}
					}
				}
			}
		} else
		if (e.has(Type.MOVE) || e.has(Type.DRAG)) {
			if (talkMode) {
				Point pt = new Point(e.x, e.y);
				int idx = 0;
				TalkSpeech last = highlight;
				highlight = null;
				for (Choice c : choices) {
					if (c.test(base.x, base.y, pt)) {
						highlight = state.speeches.get(idx);
						break;
					}
					idx++;
				}
				if (last != highlight) {
					askRepaint();
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
		}
		return super.mouse(e);
	}
	@Override
	public Screens screen() {
		return Screens.BAR;
	}
	@Override
	public void onEndGame() {
		person = null;
		state = null;
		next = null;
		highlight = null;
		pointerTransition = null;
		choices.clear();
		/** The state picture. */
		picture = null;
	}
	@Override
	protected Point scaleBase(int mx, int my) {
		UIMouse m = new UIMouse();
		m.x = mx;
		m.y = my;
		scaleMouse(m, base, margin()); 
		return new Point(m.x, m.y);
	}
	@Override
	protected Pair<Point, Double> scale() {
		Pair<Point, Double> s = scale(base, margin());
		return Pair.of(new Point(base.x, base.y), s.second);
	}
}
