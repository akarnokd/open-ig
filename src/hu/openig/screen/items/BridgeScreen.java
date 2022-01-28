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
import hu.openig.core.Pair;
import hu.openig.core.ResourceType;
import hu.openig.core.SimulationSpeed;
import hu.openig.core.SwappableRenderer;
import hu.openig.model.Level;
import hu.openig.model.Profile;
import hu.openig.model.ResourceLocator.ResourcePlace;
import hu.openig.model.Screens;
import hu.openig.model.SoundType;
import hu.openig.model.VideoMessage;
import hu.openig.model.WalkPosition;
import hu.openig.render.TextRenderer;
import hu.openig.screen.MediaPlayer;
import hu.openig.screen.VideoRenderer;
import hu.openig.screen.WalkableScreen;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UIImageToggleButton;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Button;
import hu.openig.ui.UIMouse.Type;
import hu.openig.utils.Exceptions;
import hu.openig.utils.Parallels;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.SwingWorker;
import javax.swing.Timer;

/**
 * The bridge rendering screen.
 * @author akarnokd, 2010.01.11.
 */
public class BridgeScreen extends WalkableScreen {
    /** The message panel open rectangle. */
    final Rectangle messageOpenRect = new Rectangle();
    /** The message list rectangle. */
    final Rectangle messageListRect = new Rectangle();
    /** The projector rectangle. */
    final Rectangle projectorRect = new Rectangle();
    /** The video rectangle. */
    final Rectangle videoRect = new Rectangle();
    /** The message rectangle. */
    final Rectangle messageRect = new Rectangle();
    /** The message front buffer. */
    BufferedImage messageFront;
    /** The message back buffer. */
    BufferedImage messageBack;
    /** The message lock. */
    final Lock messageLock = new ReentrantLock();
    /** The projector front buffer. */
    BufferedImage projectorFront;
    /** The projector back buffer. */
    BufferedImage projectorBack;
    /** The projector lock. */
    final Lock projectorLock = new ReentrantLock();
    /** The video front buffer. */
    BufferedImage videoFront;
    /** The video back buffer. */
    BufferedImage videoBack;
    /** The video lock. */
    final Lock videoLock = new ReentrantLock();
    /** The video subtitle. */
    String videoSubtitle;
    /** The message panel video animator. */
    volatile MediaPlayer messageAnim;
    /** The projector animator. */
    volatile MediaPlayer projectorAnim;
    /** The video animator. */
    volatile MediaPlayer videoAnim;
    /** Is the message panel open? */
    boolean messageOpen;
    /** The message is closing. */
    boolean messageClosing;
    /** Is the projector open? */
    boolean projectorOpen;
    /** The projector is closing. */
    boolean projectorClosing;
    /** The opening/closing animation is in progress. */
    boolean openCloseAnimating;
    /** Prevent starting a video playback from clicks to the statusbar indicator. */
    boolean noStatusbarPlayback;
    /** The video appear animation (the first frame). */
    BufferedImage videoAppear;
    /** The video appearance timer. */
    Timer videoAppearAnim;
    /** The video appearance percentage. */
    public int videoAppearPercent;
    /** The video appearance increment. */
    public int videoAppearIncrement = 10;
    /** The video to play back. */
    public VideoMessage video;
    /** The action to invoke when the projector reached its end of animation. */
    Action0 onProjectorComplete;
    /** The action to invoke when the projector reached its end of animation. */
    Action0 onMessageComplete;
    /** Is a video running? */
    boolean videoRunning;
    /** The list up button. */
    UIImageButton listUp;
    /** The list down button. */
    UIImageButton listDown;
    /** The list up button. */
    UIImageToggleButton send;
    /** The list down button. */
    UIImageToggleButton receive;
    /** The current graphical list level. */
    int listOffset;
    /** The list of videos. */
    final List<VideoMessageEntry> videos = new ArrayList<>();
    /** The currently selected video. */
    VideoMessage selectedVideoId;
    /** The message list row height. */
    static final int ROW_HEIGHT = 25;
    /** If the video playback completed and the panel is retracted. */
    Action0 onVideoComplete;
    /** Action to invoke when a force-view was issued. */
    public Action0 onSeen;
    /** The last display level. */
    int lastLevel;
    /** The last frame of the projector appear. */
    BufferedImage messageAppearLast;
    /** The last image of the projector. */
    BufferedImage projectorLast;
    /** Resume after the video playback? */
    public boolean resumeAfterVideo;
    /** Indicate if going to test. */
    public boolean goingToTest;
    /** Is the receive mode on? */
    public boolean receiveSelected = true;
    /** Is the send mode on? */
    public boolean sendSelected;

    public BridgeScreen() {
        super(new Rectangle(0, 0, 640, 442));
    }

    /**
     * A video message entry.
     * @author akarnokd, 2012.01.12.
     */
    public class VideoMessageEntry {
        /** The video message. */
        public final VideoMessage videoMessage;
        /**
         * Constructor, sets up the video message instance.
         * @param videoMessage the video message
         */
        public VideoMessageEntry(VideoMessage videoMessage) {
            this.videoMessage = Objects.requireNonNull(videoMessage);
        }
        /**
         * Draw this entry.
         * @param g2 the graphics context
         * @param x0 the base X
         * @param y0 the base Y
         */
        public void draw(Graphics2D g2, int x0, int y0) {
            int colorMain = TextRenderer.RED;
            int colorSub = TextRenderer.GREEN;
            if (videoMessage.seen) {
                colorMain = TextRenderer.GRAY;
                colorSub = TextRenderer.GRAY;
            }
            commons.text().paintTo(g2, x0 + 2, y0 + 2, 10, colorMain, get(videoMessage.title));
            commons.text().paintTo(g2, x0 + 2, y0 + 16, 7, colorSub, get(this.videoMessage.description));
        }
    }
    @Override
    public void onInitialize() {
        videoAppearAnim = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doVideoAppear();
            }
        });
    }
    @Override
    public void onFinish() {
        goingToTest = false;
        if (messageAnim != null) {
            messageAnim.terminate();
        }
        if (projectorAnim != null) {
            projectorAnim.terminate();
        }
        if (videoAnim != null) {
            videoAnim.terminate();
        }
    }
    /**
     * Play the video for the.

     */
    void playMessageAppear() {
        openCloseAnimating = true;
        messageAnim = new MediaPlayer(commons, commons.world().getCurrentLevel().messageAppear, new SwappableRenderer() {
            @Override
            public BufferedImage getBackbuffer() {
                return messageBack;
            }
            @Override
            public void init(int width, int height) {
                messageLock.lock();
                try {
                    messageFront = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    messageFront.setAccelerationPriority(0);
                    messageBack = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    messageBack.setAccelerationPriority(0);
                } finally {
                    messageLock.unlock();
                }
            }
            @Override
            public void swap() {
                messageLock.lock();
                try {
                    BufferedImage temp = messageFront;
                    messageFront = messageBack;
                    messageBack = temp;
                } finally {
                    messageLock.unlock();
                    askRepaint();
                }
            }
        });
        messageAnim.onComplete = new Action0() {
            @Override
            public void invoke() {
                messageOpen = false;
                openCloseAnimating = false;
                messageAppearLast = messageFront;
//                if (onAppearComplete != null) {
//                    onAppearComplete.invoke();
//                    onAppearComplete = null;
//                }
                askRepaint();
            }
        };
        messageAnim.start();
    }
    /**
     * Play message panel opening.
     */
    void playMessageOpen() {
        // we don't want the cursor to change
        // while the message panel is open
        setTransitionsEnabled(false);

        openCloseAnimating = true;
        messageAnim = new MediaPlayer(commons, commons.world().getCurrentLevel().messageOpen, new SwappableRenderer() {
            @Override
            public BufferedImage getBackbuffer() {
                return messageBack;
            }
            @Override
            public void init(int width, int height) {
                messageLock.lock();
                try {
                    messageFront = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    messageFront.setAccelerationPriority(0);
                    messageBack = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    messageBack.setAccelerationPriority(0);
                } finally {
                    messageLock.unlock();
                }
            }
            @Override
            public void swap() {
                messageLock.lock();
                try {
                    BufferedImage temp = messageFront;
                    messageFront = messageBack;
                    messageBack = temp;
                } finally {
                    messageLock.unlock();
                    askRepaint();
                }
            }
        });
        messageAnim.onComplete = new Action0() {
            @Override
            public void invoke() {
                messageOpen = true;
                openCloseAnimating = false;

                if (onMessageComplete != null) {
                    onMessageComplete.invoke();
                    onMessageComplete = null;
                }
                askRepaint();
            }
        };
        messageAnim.start();
    }
    /** Play message panel closing. */
    void playMessageClose() {
        openCloseAnimating = true;
        messageClosing = true;
        messageAnim = new MediaPlayer(commons, commons.world().getCurrentLevel().messageClose, new SwappableRenderer() {
            @Override
            public BufferedImage getBackbuffer() {
                return messageBack;
            }
            @Override
            public void init(int width, int height) {
                messageLock.lock();
                try {
                    messageFront = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    messageFront.setAccelerationPriority(0);
                    messageBack = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    messageBack.setAccelerationPriority(0);
                } finally {
                    messageLock.unlock();
                }
            }
            @Override
            public void swap() {
                messageLock.lock();
                try {
                    BufferedImage temp = messageFront;
                    messageFront = messageBack;
                    messageBack = temp;
                } finally {
                    messageLock.unlock();
                    askRepaint();
                }
            }
        });
        messageAnim.onComplete = new Action0() {
            @Override
            public void invoke() {
                messageOpen = false;
                messageClosing = false;
                openCloseAnimating = false;
                if (onMessageComplete != null) {
                    onMessageComplete.invoke();
                    onMessageComplete = null;
                }
                messageFront = messageAppearLast;

                setTransitionsEnabled(true);
                commons.control().moveMouse();
                askRepaint();
            }
        };
        messageAnim.start();
    }
    /** Play message panel closing. */
    void playProjectorOpen() {
        openCloseAnimating = true;
        projectorAnim = new MediaPlayer(commons, commons.world().getCurrentLevel().projectorOpen, new SwappableRenderer() {
            @Override
            public BufferedImage getBackbuffer() {
                return projectorBack;
            }
            @Override
            public void init(int width, int height) {
                projectorLock.lock();
                try {
                    projectorLast = projectorFront;
                    projectorFront = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    projectorFront.setAccelerationPriority(0);
                    projectorBack = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    projectorBack.setAccelerationPriority(0);
                } finally {
                    projectorLock.unlock();
                }
            }
            @Override
            public void swap() {
                projectorLock.lock();
                try {
                    projectorLast = null;
                    BufferedImage temp = projectorFront;
                    projectorFront = projectorBack;
                    projectorBack = temp;
                } finally {
                    projectorLock.unlock();
                    askRepaint();
                }
            }
        });
        projectorAnim.onComplete = new Action0() {
            @Override
            public void invoke() {
                projectorOpen = true;
                openCloseAnimating = false;
                if (onProjectorComplete != null) {
                    onProjectorComplete.invoke();
                    onProjectorComplete = null;
                }
                askRepaint();
            }
        };
        projectorAnim.start();
    }
    /** Play message panel closing. */
    void playProjectorClose() {
        openCloseAnimating = true;
        projectorClosing = true;
        projectorAnim = new MediaPlayer(commons, commons.world().getCurrentLevel().projectorClose, new SwappableRenderer() {
            @Override
            public BufferedImage getBackbuffer() {
                return projectorBack;
            }
            @Override
            public void init(int width, int height) {
                projectorLock.lock();
                try {
                    projectorLast = projectorFront;
                    projectorFront = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    projectorFront.setAccelerationPriority(0);
                    projectorBack = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    projectorBack.setAccelerationPriority(0);
                } finally {
                    projectorLock.unlock();
                }
            }
            @Override
            public void swap() {
                projectorLock.lock();
                try {
                    projectorLast = null;
                    BufferedImage temp = projectorFront;
                    projectorFront = projectorBack;
                    projectorBack = temp;
                } finally {
                    projectorLock.unlock();
                    askRepaint();
                }
            }
        });
        projectorAnim.onComplete = new Action0() {
            @Override
            public void invoke() {
                projectorOpen = false;
                openCloseAnimating = false;
                projectorClosing = false;
                if (onProjectorComplete != null) {
                    onProjectorComplete.invoke();
                    onProjectorComplete = null;
                }
                commons.control().moveMouse();
                askRepaint();
            }
        };
        projectorAnim.start();
    }

    @Override
    public boolean keyboard(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE && videoRunning) {
            videoAnim.stop();
            e.consume();
        } else
        if (commons.force) {
            e.consume();
            return true;
        }
        return super.keyboard(e);
    }

    @Override
    public boolean mouse(UIMouse e) {
        scaleMouse(e, base, margin());
        if (commons.force) {
            if (e.type == UIMouse.Type.DOWN && videoRunning) {
                videoAnim.stop();
            }
            return true;
        }
        if (messageOpen && !messageClosing) {
            if (listUp.enabled() && listUp.within(e)) {
                return listUp.mouse(e);
            }
            if (listDown.enabled() && listDown.within(e)) {
                return listDown.mouse(e);
            }
            if (send.within(e)) {
                return send.mouse(e);
            }
            if (receive.within(e)) {
                return receive.mouse(e);
            }
            if (e.has(Type.WHEEL)) {
                scrollList(e.z);
                return true;
            }
            if (!videoRunning && !videoAppearAnim.isRunning() && !openCloseAnimating) {
                if (messageListRect.contains(e.x, e.y) && e.has(Type.DOWN)) {
                    int idx = (e.y - messageListRect.y) / ROW_HEIGHT + listOffset;
                    if (idx >= 0 && idx < videos.size()) {
                        VideoMessageEntry selectedVideo = videos.get(idx);
                        selectedVideoId = selectedVideo.videoMessage;
                        playVideo(selectedVideo.videoMessage);
                    } else {
                        selectedVideoId = null;
                    }
                    return true;
                }
            }
        }
        if (e.type == UIMouse.Type.DOWN) {
            if (!openCloseAnimating) {
                if (videoRunning) {
                    videoAnim.stop();
                } else
                if (!messageRect.contains(e.x, e.y) && !videoAppearAnim.isRunning()

                        && messageOpen
                        && !projectorClosing && !messageClosing) {
                    if (projectorOpen) {
                        playProjectorClose();
                    }
                    playMessageClose();
                } else
                if (!messageOpen) {
                    if (messageOpenRect.contains(e.x, e.y)) {
                        playMessageOpen();
                    } else if (overTransitionArea()) {
                        performTransition(e.has(Button.RIGHT));
                    }
                }
            }
        } else if (e.has(Type.MOVE) || e.has(Type.DRAG) || e.has(Type.ENTER)) {
            if (updateTransition(e.x, e.y)) {
                return true;
            }
        }
        return false;
    }
    /** The level specific background. */
    BufferedImage background;
    @Override
    public void onEnter(Screens mode) {
        background = commons.world().bridge.levels.get(commons.world().level).image;

        Level lvl = world().getCurrentLevel();

        listUp = new UIImageButton(lvl.up);
        listUp.setHoldDelay(200);
        listUp.onClick = new Action0() {
            @Override
            public void invoke() {
                scrollList(-1);
                askRepaint();
            }
        };
        listDown = new UIImageButton(lvl.down);
        listDown.setHoldDelay(200);
        listDown.onClick = new Action0() {
            @Override
            public void invoke() {
                scrollList(1);
                askRepaint();
            }
        };
        send = new UIImageToggleButton(lvl.send);
        send.onClick = new Action0() {
            @Override
            public void invoke() {
                sendSelected = true;
                receiveSelected = false;
                send.selected = true;
                receive.selected = false;
                listOffset = 0;
                selectedVideoId = null;
            }
        };
        receive = new UIImageToggleButton(lvl.receive);
        receive.onClick = new Action0() {
            @Override
            public void invoke() {
                sendSelected = false;
                receiveSelected = true;
                send.selected = false;
                receive.selected = true;
                listOffset = 0;
                selectedVideoId = null;
            }
        };

        openCloseAnimating = false;
        projectorOpen = false;
        messageOpen = false;
        messageClosing = false;
        projectorClosing = false;

        clearMessageSurface();
        clearVideoSurface();
        clearProjectorSurface();

        scrollList(0);

        receive.selected = receiveSelected;
        send.selected = sendSelected;

        onResize();

        goingToTest = false;

        playMessageAppear();
        if (gotoTest()) {
            enterTestAnim();
        }

        lastLevel = world().level;
        setTransitionsEnabled(true);
    }
    /**
     * Switch to test screen.
     */
    public void enterTestAnim() {
        goingToTest = true;
        MovieScreen ms = commons.control().getScreen(Screens.MOVIE);
        ms.transitionFinished = new Action0() {
            @Override
            public void invoke() {
                displaySecondary(Screens.TEST);
                goingToTest = false;
            }
        };
        commons.playVideo("test/phsychologist_test", new Action0() {
            @Override
            public void invoke() {
                displaySecondary(Screens.TEST);
            }
        });
    }
    /**
     * Test if we need to go to the test screen instead.
     * @return true if go to the test
     */
    boolean gotoTest() {
        return world().testNeeded && !world().testCompleted

                && commons.control().secondary() != Screens.TEST && !goingToTest;
    }
    @Override
    public void onLeave() {
        resumeAfterVideo = false;
        videoAppearAnim.stop();
        videoAppear = null;
        videoAppearPercent = 0;
        messageAppearLast = null;
        projectorLast = null;
        selectedVideoId = null;
        noStatusbarPlayback = false;
        if (messageAnim != null) {
            onMessageComplete = null;
            messageAnim.stop();
            messageAnim = null;
            clearMessageSurface();
        }
        if (projectorAnim != null) {
            onProjectorComplete = null;
            projectorAnim.stop();
            projectorAnim = null;
            clearProjectorSurface();
        }
        if (videoAnim != null) {
            videoSubtitle = null;
            videoRunning = false;
            videoAnim.onComplete = null;
//            onVideoComplete = null;
            videoAnim.stop();
            videoAnim = null;
            clearVideoSurface();
        }

        commons.force = false;
        onSeen = null;

        videos.clear();
    }
    /**
     * Clear the video surface.
     */
    protected void clearVideoSurface() {
        videoLock.lock();
        try {
            videoFront = null;
            videoBack = null;
        } finally {
            videoLock.unlock();
        }
    }
    /**
     * Clear the projector surface.
     */
    protected void clearProjectorSurface() {
        projectorLock.lock();
        try {
            projectorFront = null;
            projectorBack = null;
        } finally {
            projectorLock.unlock();
        }
    }
    /**
     * Clear the message surface.
     */
    protected void clearMessageSurface() {
        messageLock.lock();
        try {
            messageFront = null;
            messageBack = null;
        } finally {
            messageLock.unlock();
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        if (lastLevel != world().level) {
            onLeave();
            onEnter(null);
            askRepaint();
            return;
        }
        if (gotoTest()) {
            onLeave();
            onEnter(null);
            askRepaint();
            return;
        }
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getInnerWidth(), getInnerHeight());

        AffineTransform save1 = scaleDraw(g2, base, margin());

        g2.drawImage(background, base.x, base.y, null);

        messageLock.lock();
        try {
            if (messageFront != null) {
                g2.drawImage(messageFront, messageRect.x, messageRect.y, null);
            }
        } finally {
            messageLock.unlock();
        }

        projectorLock.lock();
        try {
            if (projectorLast != null) {
                g2.drawImage(projectorLast, projectorRect.x, projectorRect.y, null);
            } else
            if (projectorFront != null) {
                g2.drawImage(projectorFront, projectorRect.x, projectorRect.y, null);
            }
        } finally {
            projectorLock.unlock();
        }

        if (videoAppear != null) {
            if (videoAppearPercent < 200) {
                int p = videoAppearPercent;
                if (videoAppearPercent > 100) {
                    p = (200 - videoAppearPercent);
                }
                int h = videoRect.height * p / 100;
                int dy = (videoRect.height - h) / 2;
                g2.drawImage(videoAppear, videoRect.x, videoRect.y + dy, videoRect.width, h, null);
            }
        }

        videoLock.lock();
        try {
            if (videoFront != null) {
                g2.drawImage(videoFront, videoRect.x, videoRect.y, videoRect.width, videoRect.height, null);
                if (videoSubtitle != null && config.subtitles) {
                    paintLabel(g2, base.x, videoRect.y + videoRect.height, base.width);
                }
            }
        } finally {
            videoLock.unlock();
        }
        if (messageOpen && !messageClosing) {
            if (send.selected) {
                prepareSendList();
            } else
            if (receive.selected) {
                prepareReceiveList();
            }
            int rows = messageListRect.height / ROW_HEIGHT;
            int y = messageListRect.y;
            Shape save0 = g2.getClip();
            g2.clipRect(messageListRect.x, messageListRect.y, messageListRect.width, messageListRect.height);

            int maxOffset = Math.max(0, videos.size() - rows);
            listOffset = Math.max(0, Math.min(listOffset, maxOffset));

            for (int i = listOffset; i < videos.size(); i++) {
                VideoMessageEntry e = videos.get(i);
                e.draw(g2, messageListRect.x, y);
                if (e.videoMessage == selectedVideoId) {
                    g2.setColor(Color.WHITE);
                    g2.drawRect(messageListRect.x, y, messageListRect.width - 1, ROW_HEIGHT);
                }
                y += ROW_HEIGHT;
            }
            g2.setClip(save0);

            if (listOffset > 0) {
                drawComponent(g2, listUp);
            } else {
                g2.drawImage(world().getCurrentLevel().upEmpty, listUp.x, listUp.y, null);
            }
            if (listOffset < maxOffset) {
                drawComponent(g2, listDown);
            } else {
                g2.drawImage(world().getCurrentLevel().downEmpty, listDown.x, listDown.y, null);
            }
            drawComponent(g2, send);
            drawComponent(g2, receive);

        }
        if (!projectorOpen && !messageOpen && overTransitionArea() && !openCloseAnimating) {
            drawTransitionLabel(g2);
        }
        g2.setTransform(save1);
    }
    /**
     * Scroll the list by the given amount.
     * @param delta the delta
     */
    void scrollList(int delta) {
        int rows = messageListRect.height / ROW_HEIGHT;
        int maxOffset = Math.max(0, videos.size() - rows);
        listOffset = Math.max(0, Math.min(listOffset + delta, maxOffset));
        listUp.enabled(listOffset > 0);
        listDown.enabled(listOffset < maxOffset);
    }
    @Override
    public void onResize() {
        scaleResize(base, margin());
        messageOpenRect.setBounds(base.x + 572, base.y + 292, 68, 170);
        projectorRect.setBounds(base.x + (base.width - 524) / 2 - 4, base.y, 524, 258);
        videoRect.setBounds(projectorRect.x + 103, projectorRect.y + 9, 320, 240);
        messageRect.setBounds(base.x + base.width - 298, base.y + base.height - 182, 298, 182);

        messageListRect.setBounds(messageRect.x + 13, messageRect.y + 27, 180, 138);

        if (listUp != null) {
            listUp.location(messageRect.x + 231, messageRect.y + 106);
            listDown.location(messageRect.x + 231, messageRect.y + 142);
            send.location(messageRect.x + 220, messageRect.y + 67);
            receive.location(messageRect.x + 220, messageRect.y + 29);
        }

    }
    /**
     * Paint a word-wrapped label.
     * @param g2 the graphics context.
     * @param x0 the X coordinate
     * @param y0 the Y coordinate
     * @param width the draw width
     */
    public void paintLabel(Graphics2D g2, int x0, int y0, int width) {
        List<String> lines = new ArrayList<>();
        int maxWidth = commons.text().wrapText(videoSubtitle, width, 14, lines);
        int y = y0 + 6;
        Composite cp = g2.getComposite();
        g2.setColor(Color.BLACK);
        g2.setComposite(AlphaComposite.SrcOver.derive(0.8f));
        int x1 = x0 + (width - maxWidth) / 2 - 3;
        int y1 = y0 + 3;
        int w1 = maxWidth + 6;
        int h1 = lines.size() * 17 + 3;
        g2.fillRect(x1, y1, w1, h1);
        g2.setComposite(cp);
        for (String s : lines) {
            int tw = commons.text().getTextWidth(14, s);
            int x = (width - tw) / 2;
            commons.text().paintTo(g2, x0 + x, y, 14, TextRenderer.WHITE, s);
            y += 17;
        }
    }
    @Override
    public Screens screen() {
        return Screens.BRIDGE;
    }
    @Override
    public void onEndGame() {
    }
    /**
     * Play the specific video.
     * @param vm the video
     */
    public void playVideo(final VideoMessage vm) {
        final boolean paused = commons.simulation.paused();
        noStatusbarPlayback = true;
        commons.simulation.pause();
        onVideoComplete = new Action0() {
            @Override
            public void invoke() {
                Profile p = commons.profile;
                p.unlockVideo(vm.media);
                p.save();

                noStatusbarPlayback = false;
                vm.seen = true;
                if (!paused || resumeAfterVideo) {
                    resumeAfterVideo = false;
                    commons.simulation.speed(SimulationSpeed.NORMAL);
                }
            }
        };
        this.video = vm;
        final SwingWorker<BufferedImage, Void> sw = new SwingWorker<BufferedImage, Void>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
                ResourcePlace rp = rl.get(video.media, ResourceType.VIDEO);
                if (rp == null) {
                    Exceptions.add(new AssertionError("Missing resource: " + video.media));
                }
                return VideoRenderer.firstFrame(rp);
            }
        };
        sw.execute();
        if (!projectorOpen) {
            onProjectorComplete = new Action0() {
                @Override
                public void invoke() {
                    playVideoAppear(sw);
                }
            };
            playProjectorOpen();
        } else {
            playVideoAppear(sw);
        }
    }
    /**
     * Start playing the video.
     * @param sw the worker that will return the first frame.
     */
    void playVideoAppear(final SwingWorker<BufferedImage, Void> sw) {
        videoAppearPercent = 0;
        try {
            videoAppear = sw.get();
        } catch (InterruptedException | ExecutionException ex) {
            Exceptions.add(ex);
        }
        buttonSound(SoundType.ACKNOWLEDGE_2);
        videoAppearAnim.start();
    }
    /**
     * Animate the the appearance of the video.
     */
    void doVideoAppear() {
        final VideoMessage fVideo = video;
        videoAppearPercent += videoAppearIncrement;
        if (videoAppearPercent == 100) {
            videoAppearAnim.stop();
            videoAnim = new MediaPlayer(commons, fVideo.media, new SwappableRenderer() {
                @Override
                public BufferedImage getBackbuffer() {
                    return videoBack;
                }
                @Override
                public void init(int width, int height) {
                    videoFront = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    videoFront.setAccelerationPriority(0);
                    videoBack = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    videoBack.setAccelerationPriority(0);
                }
                @Override
                public void swap() {
                    videoLock.lock();
                    try {
                        BufferedImage temp = videoFront;
                        videoFront = videoBack;
                        videoBack = temp;
                    } finally {
                        videoLock.unlock();
                        askRepaint();
                    }
                }
            });
            videoAnim.onLabel = new Action1<String>() {
                @Override
                public void invoke(String value) {
                    videoSubtitle = value;
                }
            };
            videoAnim.onComplete = new Action0() {
                @Override
                public void invoke() {
                    videoRunning = false;
                    videoAppear = videoFront;
                    videoFront = null;
                    videoAppearAnim.start();
                    buttonSound(SoundType.ACKNOWLEDGE_2);
                    videoSubtitle = null;
                }
            };
            videoRunning = true;
            videoAnim.start();
        } else
        if (videoAppearPercent > 200) {
            videoAppearAnim.stop();
            videoAppear = null;
            if (onVideoComplete != null) {
                onVideoComplete.invoke();
                onVideoComplete = null;
            }

            for (VideoMessageEntry vm : videos) {
                if (vm.videoMessage.id.equals(fVideo.id)) {
                    world().scripting.onMessageSeen(vm.videoMessage.id);
                }
            }
            if (onSeen != null) {
                onSeen.invoke();
                onSeen = null;
            }
        }
        askRepaint();
    }
    /**
     * Prepare the send list.
     */
    void prepareSendList() {
        prepareList(world().scripting.getSendMessages());
    }
    /** Prepare the receive list. */
    void prepareReceiveList() {
        prepareList(world().receivedMessages);
    }
    /**
     * Prepare the video listings.
     * @param available the available messages
     */
    void prepareList(List<VideoMessage> available) {
        videos.clear();
        for (VideoMessage msg : available) {
            VideoMessageEntry e = new VideoMessageEntry(msg);
            videos.add(e);
        }
        scrollList(0);
    }
    /**
     * Force the playback of the given message.
     * @param messageId the message id
     * @param onSeen the action to perform when the message ended
     */
    public void forceMessage(final String messageId, final Action0 onSeen) {
        if (commons.simulation == null) {
            return;
        }
        commons.simulation.pause();
        if (videoRunning) {
            videoAnim.stop();
            rerunForce(messageId, onSeen);
            return;
        }
        if (videoAppearAnim.isRunning()) {
            rerunForce(messageId, onSeen);
            return;
        }
        if (openCloseAnimating) {
            rerunForce(messageId, onSeen);
            return;
        }

        commons.force = true;
        this.onSeen = onSeen;
        onMessageComplete = new Action0() {
            @Override
            public void invoke() {
                send.selected = false;
                receive.selected = true;
                List<VideoMessage> list = world().receivedMessages;

                listOffset = 0;
                selectedVideoId = null;
                for (VideoMessage msg : list) {
                    if (msg.id.equals(messageId)) {
                        selectedVideoId = msg;
                        break;
                    }
                    listOffset++;
                }
                if (selectedVideoId == null) {
                    Exceptions.add(new AssertionError("Missing message: " + messageId));
                } else {
                    playProjectorOpen();
                }
            }
        };
        onProjectorComplete = new Action0() {
            @Override
            public void invoke() {
                playVideo(selectedVideoId);
            }
        };
        if (!messageOpen) {
            playMessageOpen();
        } else {
            if (!projectorOpen) {
                onMessageComplete.invoke();
            } else {
                onProjectorComplete.invoke();
            }
        }
    }
    /**
     * Rerun the force message.
     * @param messageId the message id
     * @param onSeen on seen action
     */
    void rerunForce(final String messageId, final Action0 onSeen) {
        Parallels.runDelayedInEDT(1000, new Runnable() {
            @Override
            public void run() {
                forceMessage(messageId, onSeen);
            }
        });
    }
    /**
     * Display the animations to receive a video message.
     */
    void displayReceivePhases() {
        if (commons.control().primary() != Screens.BRIDGE) {
            return;
        }
        if (openCloseAnimating) {
            Parallels.runDelayedInEDT(1000, new Runnable() {
                @Override
                public void run() {
                    displayReceivePhases();
                }
            });
            return;
        }
        send.selected = false;
        receive.selected = true;
        if (!messageOpen) {
            playMessageOpen();
            Parallels.runDelayedInEDT(1000, new Runnable() {
                @Override
                public void run() {
                    displayReceivePhases();
                }
            });
            return;
        }
        List<VideoMessage> vms = world().receivedMessages;
        for (VideoMessage vm : vms) {
            if (!vm.seen) {
                selectedVideoId = vm;
                playVideo(vm);
                break;
            }
        }
    }
    /**
     * Display the message panel and switch to receive.
     */
    public void displayReceive() {
        if (!goingToTest && commons.control().secondary() != Screens.TEST) {
            if (noStatusbarPlayback) {
                return;
            }
            noStatusbarPlayback = true;
            displayReceivePhases();
        }
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

    @Override
    protected WalkPosition getPosition() {
        return ScreenUtils.getWalk("*bridge", world());
    }
}
