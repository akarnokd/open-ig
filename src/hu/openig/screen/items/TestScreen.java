/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.core.Action0;
import hu.openig.core.Pair;
import hu.openig.model.Screens;
import hu.openig.model.TestAnswer;
import hu.openig.model.TestQuestion;
import hu.openig.render.RenderTools;
import hu.openig.screen.ScreenBase;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIContainer;
import hu.openig.ui.UIGenericButton;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UILabel;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Type;
import hu.openig.ui.VerticalAlignment;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

/**
 * The screen displaying the Phsychologist test.
 * @author akarnokd, 2011.04.20.
 */
public class TestScreen extends ScreenBase {
    /** The base rectangle. */
    Rectangle base = new Rectangle();
    /** The questions listing. */
    QuestionScroll questionScroll;
    /** Scroll up. */
    UIImageButton scrollUp;
    /** Scroll down. */
    UIImageButton scrollDown;
    /** Done. */
    UIGenericButton done;
    /** Was the game paused? */
    boolean paused;
    @Override
    public void onInitialize() {
        base.setSize(commons.background().test.getWidth(), commons.background().test.getHeight());

        questionScroll = new QuestionScroll();

        scrollUp = new UIImageButton(commons.common().moveUp);
        scrollUp.onClick = new Action0() {
            @Override
            public void invoke() {
                questionScroll.doScrollUp();
                doRepaint();
            }
        };
        scrollUp.setHoldDelay(150);

        scrollDown = new UIImageButton(commons.common().moveDown);
        scrollDown.onClick = new Action0() {
            @Override
            public void invoke() {
                questionScroll.doScrollDown();
                doRepaint();
            }
        };
        scrollDown.setHoldDelay(150);

        done = new UIGenericButton(get("test.done"), fontMetrics(14), commons.common().mediumButton, commons.common().mediumButtonPressed);
        done.onClick = new Action0() {
            @Override
            public void invoke() {
                doDone();
            }
        };
        done.disabledPattern(commons.common().disabledPattern);
        addThis();
    }
    /** Perform the partial repaint. */
    void doRepaint() {
        scaleRepaint(base, base, margin());
    }
    @Override
    public void onEnter(Screens mode) {
        questionScroll.prepare();
        paused = commons.simulation.paused();
        commons.pause();
    }
    /** Complete the test. */
    void doDone() {
        world().testNeeded = false;
        world().testCompleted = true;

        MovieScreen ms = commons.control().getScreen(Screens.MOVIE);
        ms.transitionFinished = new Action0() {
            @Override
            public void invoke() {
                hideSecondary();
            }
        };
        commons.playVideo("test/phsychologist_test_completed", new Action0() {
            @Override
            public void invoke() {
                hideSecondary();
            }
        });
    }
    /** @return is all questions answered? */
    boolean isFilled() {
        int cnt = 0;
        for (TestQuestion tq : world().test.values()) {
            for (TestAnswer ta : tq.answers) {
                if (ta.selected) {
                    cnt++;
                    break;
                }
            }
        }
        return cnt == world().test.size();
    }
    @Override
    public void onLeave() {
        questionScroll.clear();
        if (!paused) {
            commons.simulation.resume();
        }
    }

    @Override
    public void onFinish() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onResize() {
        scaleResize(base, margin());

        scrollUp.location(base.x + base.width - 30, base.y + 5);
        done.location(base.x + base.width / 2 + (base.width / 2 - done.width) / 2, base.y + base.height - 5 - done.height);
        questionScroll.bounds(base.x + base.width / 2 + 10, base.y + 10, base.width / 2 - 45, done.y - base.y - 20);
        scrollDown.location(base.x + base.width - 30, questionScroll.y + questionScroll.height - 30);
    }

    @Override
    public Screens screen() {
        return Screens.TEST;
    }

    @Override
    public void onEndGame() {
        // TODO Auto-generated method stub

    }
    @Override
    public void draw(Graphics2D g2) {
        AffineTransform savea = scaleDraw(g2, base, margin());

        RenderTools.darkenAround(base, width, height, g2, 0.5f, true);
        g2.drawImage(commons.background().test, base.x, base.y, null);

        done.enabled(isFilled());
        scrollUp.visible(questionScroll.mayScrollUp());
        scrollDown.visible(questionScroll.mayScrollDown());

        super.draw(g2);

        g2.setTransform(savea);
    }
    /** A test choice. */
    static class TestChoice extends UIComponent {
        /** The associated question. */
        TestQuestion question;
        /** The associated answer. */
        TestAnswer answer;
        /**
         * Construct a test choice.
         * @param q the question
         * @param a the answer
         */
        public TestChoice(TestQuestion q, TestAnswer a) {
            this.question = q;
            this.answer = a;
            height = 14;
            width = 14;
            a.selected = false;
        }
        @Override
        public void draw(Graphics2D g2) {
            g2.setColor(Color.BLACK);
            g2.drawOval(0, 0, 13, 13);
            if (answer.selected) {
                g2.fillOval(3, 3, 7, 7);
            }
        }
        @Override
        public boolean mouse(UIMouse e) {
            if (e.has(Type.DOWN)) {
                question.choose(answer.id);
                return true;
            }
            return false;
        }
    }
    /** The scroll box of the questions. */
    class QuestionScroll extends UIContainer {
        /** The scroll offset. */
        int top;
        /** The total height of the questions. */
        int totalHeight;
        /** Prepare the UI components for the questions. */
        public void prepare() {
            top = 0;
            components.clear();
            int textHeight = 7;
            int y = 0;
            for (final TestQuestion tq : world().test.values()) {
                UILabel lbl = new UILabel(get(tq.label), textHeight, width - 20, commons.text());
                lbl.y = y;
                lbl.x = 15;
                lbl.height = lbl.getWrappedHeight();
                lbl.color(0xFF000000);
                add(lbl);

                y += 5 + lbl.height;
                for (final TestAnswer ta : tq.answers) {
                    UILabel answ = new UILabel(get(ta.label), textHeight, width - 40, commons.text()) {
                        @Override
                        public boolean mouse(UIMouse e) {
                            if (e.has(Type.DOWN)) {
                                tq.choose(ta.id);
                                return true;
                            }
                            return super.mouse(e);
                        }
                    };
                    answ.color(0xFF000000);
                    answ.x = 40;
                    answ.y = y;
                    answ.vertically(VerticalAlignment.MIDDLE);
                    answ.height = Math.max(answ.getWrappedHeight(), 20);
                    add(answ);

                    TestChoice tc = new TestChoice(tq, ta);
                    tc.y = y + (answ.height - 14) / 2;
                    tc.x = 20;
                    add(tc);
                    y += answ.height;
                }
                y += 5;
            }
            totalHeight = y - 5;
        }
        @Override
        public void draw(Graphics2D g2) {
            Shape save0 = g2.getClip();
            g2.clipRect(0, 0, width, height);
            g2.translate(0, -top);

            super.draw(g2);

            g2.translate(0, top);
            g2.setClip(save0);
        }
        /** Scroll up. */
        void doScrollUp() {
            top = Math.max(0, Math.min(top - 30, totalHeight - height));
        }
        /** Scroll down. */
        void doScrollDown() {
            top = Math.max(0, Math.min(top + 30, totalHeight - height));
        }
        /** @return May scroll up? */
        boolean mayScrollUp() {
            return top > 0;
        }
        /** @return May scroll down? */
        boolean mayScrollDown() {
            return top < totalHeight - height;
        }
        @Override
        public boolean mouse(UIMouse e) {
            if (e.has(Type.WHEEL)) {
                if (e.z < 0) {
                    doScrollUp();
                } else {
                    doScrollDown();
                }
                return true;
            }
            e.y += top;
            boolean resp = super.mouse(e);
            e.y -= top;
            return resp;
        }
    }
    @Override
    public boolean mouse(UIMouse e) {
        scaleMouse(e, base, margin());
        return super.mouse(e);
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
