/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.core.Action0;
import hu.openig.core.Action1;
import hu.openig.core.SwappableRenderer;
import hu.openig.model.Screens;
import hu.openig.render.RenderTools;
import hu.openig.render.TextRenderer;
import hu.openig.screen.MediaPlayer;
import hu.openig.screen.ScreenBase;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Type;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The movie screen used for full screen video playback with sound and subtitles.
 * @author akarnokd, 2010.01.08.
 */
public class MovieScreen extends ScreenBase implements SwappableRenderer {
    /** The center region for the movie frames. */
    Rectangle movieRect = new Rectangle();
    /** The current label to display. */
    String label;
    /** The image swap lock. */
    Lock swapLock = new ReentrantLock();
    /** The front buffer. */
    BufferedImage frontBuffer;
    /** The back buffer. */
    BufferedImage backBuffer;
    /** The media queue to play videos after each other. */
    public final Queue<String> mediaQueue = new LinkedList<>();
    /** The action to invoke when the playback has finished. */
    public Action0 playbackFinished;
    /**
     * The media player.
     */
    private MediaPlayer player;
    /** The mouse was pressed down. */
    boolean down;
    /** The fadein index. -1 if no fade is in progress */
    int fadeIndex;
    /** The max fade index. */
    static final int FADE_MAX = 20;
    /** The fade time step duration in milliseconds. */
    static final int FADE_TIME = 25;
    /** The fade timer. */
    Closeable fadeTimer;
    /** Called once the transition has finished. */
    public Action0 transitionFinished;
    /** Allow transition animation? */
    public boolean allowTransition = true;
    /**
     * Start playback.
     * @param media the media
     */
    protected void startPlayback(final String media) {
        player = new MediaPlayer(commons, media, this);
        player.onComplete = new Action0() {
            @Override
            public void invoke() {
                frontBuffer = null;
                playNext();
            }
        };
        player.onLabel = new Action1<String>() {
            @Override
            public void invoke(String text) {
                label = text;
                askRepaint();
            }
        };
        player.start();
    }
    /**
     *

     */
    protected void playNext() {
        String nextMedia = mediaQueue.poll();
        if (nextMedia != null) {
            startPlayback(nextMedia);
        } else {
            if (allowTransition) {
                fadeIndex = FADE_MAX / 2;
                close0(fadeTimer);
                fadeTimer = commons.register(FADE_TIME, new Action0() {
                    @Override
                    public void invoke() {
                        if (fadeIndex == 0) {
                            if (playbackFinished != null) {
                                playbackFinished.invoke();
                                playbackFinished = null;
                            }
                        }
                        fadeIndex--;
                        askRepaint();
                    }
                });
            } else {
                fadeIndex = -1;
                if (playbackFinished != null) {
                    playbackFinished.invoke();
                    playbackFinished = null;
                }
                askRepaint();
            }
        }
    }

    @Override
    public void onResize() {
        movieRect.setBounds((getInnerWidth() - 640) / 2, (getInnerHeight() - 480) / 2, 640, 480);
    }

    @Override
    public void onFinish() {
        mediaQueue.clear();
        stopPlayback();
        playbackFinished = null;
    }

    @Override
    public void onInitialize() {

    }

    @Override
    public boolean keyboard(KeyEvent e) {
        if (fadeIndex < 0) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_SPACE) {
                stopPlayback();
                e.consume();
                return true;
            }
        }
        return false;
    }

    /** Stop the current playback. */
    protected void stopPlayback() {
        if (player != null) {
            player.stop();
        }
        label = null;
        askRepaint();
    }
    @Override
    public boolean mouse(UIMouse e) {
        if (fadeIndex < 0) {
            if (e.has(Type.DOWN)) {
                down = true;
            } else
            if (e.has(Type.LEAVE)) {
                down = false;
            } else
            if (e.has(Type.UP) && down && config.movieClickSkip) {
                down = false;
                stopPlayback();
            }
        }
        return false;
    }

    @Override
    public void onEnter(Screens mode) {
        down = false;
        fadeIndex = 0;
        if (allowTransition) {
            fadeTimer = commons.register(FADE_TIME, new Action0() {
                @Override
                public void invoke() {
                    if (fadeIndex * 2 == FADE_MAX) {
                        playNext();
                        askRepaint();
                        if (transitionFinished != null) {
                            transitionFinished.invoke();
                            transitionFinished = null;
                        }
                    }

                    if (fadeIndex >= 0) {
                        fadeIndex++;
                        if (fadeIndex >= FADE_MAX) {
                            fadeIndex = -1;
                        }
                        askRepaint();
                    }
                }
            });
        } else {
            fadeIndex = -1;
            playNext();
            askRepaint();
        }
        label = null;
    }
    /** @return check if the screen is fully opaque. */
    public boolean opaque() {
        return fadeIndex < 0;
    }
    @Override
    public void onLeave() {
        close0(fadeTimer);
        fadeTimer = null;
        fadeIndex = -1;
        allowTransition = true;
    }

    @Override
    public void draw(Graphics2D g2) {
        onResize();
        if (fadeIndex >= 0 && fadeIndex * 2 < FADE_MAX) {
            g2.setColor(new Color(0, 0, 0, fadeIndex * 2f / FADE_MAX));
        } else {
            g2.setColor(Color.BLACK);
        }
        g2.fillRect(0, 0, getInnerWidth(), getInnerHeight());
        if (frontBuffer != null) {
            swapLock.lock();
            try {
                RenderTools.setInterpolation(g2, true);
                if (config.movieScale) {
                    double sx = getInnerWidth() / 640.0;
                    double sy = getInnerHeight() / 480.0;
                    double scalex = Math.min(sx, sy);
                    double scaley = scalex;
                    // center the image
                    AffineTransform save0 = g2.getTransform();
                    double dx = getInnerWidth() - (640 * scalex);
                    double dy = getInnerHeight() - (480 * scaley);

                    g2.translate(dx / 2,
                            dy / 2);

                    g2.drawImage(frontBuffer, 0, 0, (int)(640 * scalex), (int)(480 * scaley), null);
                    g2.setTransform(save0);
                    if (label != null && config.subtitles) {
                        paintLabel(g2, 0, 0, getInnerWidth(), getInnerHeight());
                    }
                } else {
                    g2.drawImage(frontBuffer, movieRect.x, movieRect.y,

                            movieRect.width, movieRect.height, null);
                    if (label != null && config.subtitles) {
                        paintLabel(g2, movieRect.x, movieRect.y, movieRect.width, movieRect.height);
                    }
                            }
                RenderTools.setInterpolation(g2, false);
            } finally {
                swapLock.unlock();
            }
        }
        if (fadeIndex * 2 >= FADE_MAX) {
            g2.setColor(new Color(0, 0, 0, (FADE_MAX - fadeIndex) * 2f / FADE_MAX));
            g2.fillRect(0, 0, getInnerWidth(), getInnerHeight());
        }
    }

    /**
     * Paint a word-wrapped label.
     * @param g2 the graphics context.
     * @param x0 the X coordinate
     * @param y0 the Y coordinate
     * @param width the draw width
     * @param height the draw height
     */
    public void paintLabel(Graphics2D g2, int x0, int y0, int width, int height) {
        List<String> lines = new ArrayList<>();
        int maxWidth = commons.text().wrapText(label, width, 14, lines);
        int y = height - lines.size() * 21 - 7;
        Composite cp = g2.getComposite();
        g2.setComposite(AlphaComposite.SrcOver.derive(0.8f));
        g2.fillRect(x0 + (width - maxWidth) / 2 - 3, y0 + y - 3, maxWidth + 6, lines.size() * 21 + 6);
        g2.setComposite(cp);
        for (String s : lines) {
            int tw = commons.text().getTextWidth(14, s);
            int x = (width - tw) / 2;
            commons.text().paintTo(g2, x0 + x, y0 + y, 14, TextRenderer.WHITE, s);
            y += 21;
        }
    }
    @Override
    public BufferedImage getBackbuffer() {
        return backBuffer;
    }

    @Override
    public void init(int width, int height) {
        swapLock.lock();
        try {
            backBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            frontBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        } finally {
            swapLock.unlock();
        }
    }

    @Override
    public void swap() {
        swapLock.lock();
        try {
            BufferedImage temp = backBuffer;
            backBuffer = frontBuffer;
            frontBuffer = temp;
        } finally {
            swapLock.unlock();
        }
        askRepaint();
    }
    @Override
    public Screens screen() {
        return Screens.MOVIE;
    }
    @Override
    public void onEndGame() {
        playbackFinished = null;
    }
}
