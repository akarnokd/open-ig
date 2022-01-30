/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen;

import java.awt.event.*;

import hu.openig.core.*;
import hu.openig.mechanics.*;
import hu.openig.model.*;
import hu.openig.screen.api.SettingsPage;
import hu.openig.screen.items.CustomBalanceScreen;
import hu.openig.utils.Exceptions;

/**
 * The game keyboard manager.
 * @author akarnokd, 2012.08.03.
 */
public class GameKeyManager extends KeyAdapter {
    /** The common resources. */
    protected CommonResources commons;
    /**
     * Constructor. Initializes the global and back references.
     * @param commons the common resources
     */
    public GameKeyManager(CommonResources commons) {
        this.commons = commons;
    }
    /**
     * @return the current world object or null if outside the game
     */
    protected World world() {
        return commons.world();
    }
    /**
     * @return Returns the primary screen.
     */
    protected ScreenBase primary() {
        Screens pri = control().primary();
        if (pri != null) {
            return control().getScreen(pri);
        }
        return null;
    }
    /**

     * @return the game control object
     */
    private GameControls control() {
        return commons.control();
    }
    /**
     * @return the secondary screen if present
     */
    protected ScreenBase secondary() {
        Screens sec = control().secondary();
        if (sec != null) {
            return control().getScreen(sec);
        }
        return null;
    }
    /** @return the movie screen. */
    protected ScreenBase movie() {
        return control().getScreen(Screens.MOVIE);
    }
    /** @return the options screen. */
    protected ScreenBase options() {
        return control().getScreen(Screens.LOAD_SAVE);
    }
    @Override
    public void keyPressed(KeyEvent e) {
        try {
            boolean rep = false;
            ScreenBase pri = primary();
            ScreenBase sec = secondary();
            if (control().movieVisible()) {
                rep |= movie().keyboard(e);
            } else
            if (control().optionsVisible()) {
                rep |= options().keyboard(e);
            } else
            if (sec != null) {
                rep |= sec.keyboard(e);
            } else
            if (pri != null) {
                rep |= pri.keyboard(e);
            }
            if (!e.isConsumed()) {
                handleDefaultKeyboard(e);
            }
            if (rep) {
                control().repaintInner();
            }
        } catch (Throwable t) {
            Exceptions.add(t);
        }
    }
    /**
     * Display the given primary screen.
     * @param screen the screen enum
     */
    void displayPrimary(Screens screen) {
        control().displayPrimary(screen);
    }
    /**
     * Display the given secondary screen.
     * @param screen the screen enum
     */
    void displaySecondary(Screens screen) {
        control().displaySecondary(screen);
    }
    /** Hide the secondary screen. */
    void hideSecondary() {
        control().hideSecondary();
    }
    /** Repaint the entire inner game area. */
    void repaintInner() {
        control().repaintInner();
    }
    boolean enableForCurrentScreen() {
        if (secondary() != null) {
            if (secondary().screen() == Screens.ABANDON_COLONY) {
                return false;
            }
            if (secondary().screen() == Screens.MOVE_COLONISTS_IN) {
                return false;
            }
            if (secondary().screen() == Screens.MOVE_COLONISTS_OUT) {
                return false;
            }
        }
        return true;
    }
    /**
     * Handle the screen switch if the appropriate key is pressed.
     * @param e the key event
     */
    void handleDefaultKeyboard(KeyEvent e) {
        if (e.isAltDown()) {
            if (e.getKeyCode() == KeyEvent.VK_F4) {
                control().exit();
                e.consume();
                return;
            }
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                control().setFullscreen(!control().isFullscreen());
                e.consume();
                return;
            }
            e.consume();
        }
        if (e.getKeyCode() == KeyEvent.VK_Y) {
            if (e.isControlDown()) {
                control().setUIComponentDebug(!control().isUIComponentDebug());
                e.consume();
                control().moveMouse();
                control().repaintInner();
                return;
            }
        } else
        if (e.getKeyCode() == KeyEvent.VK_F) {
            if (e.isControlDown()) {
                commons.config.showFPS = !commons.config.showFPS;
                e.consume();
                return;
            }
        }
        ScreenBase primary = primary();
        ScreenBase secondary = secondary();
        if (!commons.worldLoading && world() != null && !control().movieVisible()) {
            switch (e.getKeyCode()) {
            case KeyEvent.VK_1:
                if (e.isControlDown() && !commons.battleMode) {
                    world().level = 1;
                    world().scripting.onLevelChanged();
                    if (primary != null) {
                        primary.onLeave();
                    }
                    primary = null;
                    displayPrimary(Screens.BRIDGE);
                } else {
                    commons.simulation.speed(SimulationSpeed.NORMAL);
                    commons.playSound(SoundTarget.BUTTON, SoundType.CLICK_LOW_1, null);
                    repaintInner();
                }
                e.consume();
                break;
            case KeyEvent.VK_2:
                if (e.isControlDown() && !commons.battleMode) {
                    world().level = 2;
                    world().scripting.onLevelChanged();
                    if (primary != null) {
                        primary.onLeave();
                    }
                    primary = null;
                    displayPrimary(Screens.BRIDGE);
                } else {
                    commons.simulation.speed(SimulationSpeed.FAST);
                    commons.playSound(SoundTarget.BUTTON, SoundType.CLICK_LOW_1, null);
                    repaintInner();
                }
                e.consume();
                break;
            case KeyEvent.VK_3:
                if (e.isControlDown() && !commons.battleMode) {
                    world().level = 3;
                    world().scripting.onLevelChanged();
                    if (primary != null) {
                        primary.onLeave();
                    }
                    primary = null;
                    displayPrimary(Screens.BRIDGE);
                } else {
                    commons.simulation.speed(SimulationSpeed.ULTRA_FAST);
                    commons.playSound(SoundTarget.BUTTON, SoundType.CLICK_LOW_1, null);
                    repaintInner();
                }
                e.consume();
                break;
            case KeyEvent.VK_SPACE:
                if (commons.simulation.paused()) {
                    commons.simulation.resume();
                    commons.playSound(SoundTarget.BUTTON, SoundType.UI_ACKNOWLEDGE_1, null);
                } else {
                    commons.simulation.pause();
                    commons.playSound(SoundTarget.BUTTON, SoundType.PAUSE, null);
                }
                repaintInner();
                e.consume();
                break;
            case KeyEvent.VK_ESCAPE:
                if (commons.battleMode) {
                    control().displayOptions(false, SettingsPage.AUDIO);
                    e.consume();
                }
                break;
            case KeyEvent.VK_TAB:
                control().setObjectivesVisible(!control().isObjectivesVisible());
                repaintInner();
                e.consume();
                break;
            case KeyEvent.VK_L:
                if (e.isControlDown()) {
                    control().load(null);
                    e.consume();
                }
                break;
            default:
            }
        }
        if (!commons.worldLoading && world() != null

                && !control().movieVisible() && !commons.battleMode) {
            if (e.getKeyChar() == '+' || e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
                if (enableForCurrentScreen()) {
                    world().player.moveNextPlanet();
                    repaintInner();
                }
                e.consume();
            } else
            if (e.getKeyChar() == '-' || e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
                if (enableForCurrentScreen()) {
                    world().player.movePrevPlanet();
                    repaintInner();
                }
                e.consume();
            }
            int keycode = e.getKeyCode();
            switch (keycode) {
            case KeyEvent.VK_F1:
                displayPrimary(Screens.BRIDGE);
                e.consume();
                break;
            case KeyEvent.VK_F2:
                displayPrimary(Screens.STARMAP);
                e.consume();
                break;
            case KeyEvent.VK_F3:
                displayPrimary(Screens.COLONY);
                e.consume();
                break;
            case KeyEvent.VK_F4:
                if (secondary != null) {
                    if (secondary.screen() == Screens.EQUIPMENT) {
                        hideSecondary();
                    } else {
                        displaySecondary(Screens.EQUIPMENT);
                    }
                } else

                if (primary != null) {
                    switch (primary.screen()) {
                    case COLONY:
                        world().player.selectionMode = SelectionMode.PLANET;
                        displaySecondary(Screens.EQUIPMENT);
                        break;
                    default:
                        displaySecondary(Screens.EQUIPMENT);
                    }
                }
                e.consume();
                break;
            case KeyEvent.VK_F5:
                if (world().level >= 2) {
                    if (secondary != null && secondary.screen() == Screens.PRODUCTION) {
                        hideSecondary();
                    } else {
                        displaySecondary(Screens.PRODUCTION);
                    }
                    e.consume();
                }
                break;
            case KeyEvent.VK_F6:
                if (world().level >= 3) {
                    if (secondary != null && secondary.screen() == Screens.RESEARCH) {
                        hideSecondary();
                    } else {
                        displaySecondary(Screens.RESEARCH);
                    }
                    e.consume();
                }
                break;
            case KeyEvent.VK_F7:
                if (secondary != null) {
                    switch (secondary.screen()) {
                    case DIPLOMACY:
                        displaySecondary(Screens.INFORMATION_ALIENS);
                        break;
                    case INFORMATION_ALIENS:
                    case INFORMATION_BUILDINGS:
                    case INFORMATION_COLONY:
                    case INFORMATION_FINANCIAL:
                    case INFORMATION_FLEETS:
                    case INFORMATION_INVENTIONS:
                    case INFORMATION_MILITARY:
                    case INFORMATION_PLANETS:
                        hideSecondary();
                        break;
                    case RESEARCH:
                    case PRODUCTION:
                    case EQUIPMENT:
                        displaySecondary(Screens.INFORMATION_INVENTIONS);
                        break;
                    default:
                        displaySecondary(Screens.INFORMATION_PLANETS);
                    }
                } else {
                    switch (primary.screen()) {
                    case STARMAP:
                        displaySecondary(Screens.INFORMATION_PLANETS);
                        break;
                    case COLONY:
                        displaySecondary(Screens.INFORMATION_COLONY);
                        break;
                    default:
                        displaySecondary(Screens.INFORMATION_PLANETS);
                    }

                }
                e.consume();
                break;
            case KeyEvent.VK_F8:
                if (secondary != null && secondary.screen() == Screens.DATABASE) {
                    hideSecondary();
                } else {
                    displaySecondary(Screens.DATABASE);
                }
                e.consume();
                break;
            case KeyEvent.VK_F9:
                if (world().getShip().positions.containsKey("*bar")) {
                    if (secondary != null && secondary.screen() == Screens.BAR) {
                        hideSecondary();
                    } else {
                        displaySecondary(Screens.BAR);
                    }
                }
                e.consume();
                break;
            case KeyEvent.VK_F10:
                if (world().hasDiplomacy()) {
                    if (secondary != null && secondary.screen() == Screens.DIPLOMACY) {
                        hideSecondary();
                    } else {
                        displaySecondary(Screens.DIPLOMACY);
                    }
                }
                e.consume();
                break;
            case KeyEvent.VK_F11:
                if (e.isControlDown()) {
                    if (secondary != null && secondary.screen() == Screens.STATISTICS) {
                        hideSecondary();
                    } else {
                        displaySecondary(Screens.STATISTICS);
                    }
                } else {
                    if (secondary != null && secondary.screen() == Screens.SPYING) {
                        hideSecondary();
                    } else {
                        displaySecondary(Screens.SPYING);
                    }
                }
                e.consume();
                break;
            case KeyEvent.VK_F12:
                if (e.isControlDown()) {
                    if (secondary != null && secondary.screen() == Screens.ACHIEVEMENTS) {
                        hideSecondary();
                    } else {
                        displaySecondary(Screens.ACHIEVEMENTS);
                    }
                } else {
                    if (secondary != null && secondary.screen() == Screens.TRADE) {
                        hideSecondary();
                    } else {
                        displaySecondary(Screens.TRADE);
                    }
                }
                e.consume();
                break;
            case KeyEvent.VK_4:
                if (e.isControlDown()) {
                    world().level = 4;
                    world().scripting.onLevelChanged();
                    if (primary != null) {
                        primary.onLeave();
                    }
                    displayPrimary(Screens.BRIDGE);
                    e.consume();
                }
                break;
            case KeyEvent.VK_5:
                if (e.isControlDown()) {
                    world().level = 5;
                    world().scripting.onLevelChanged();
                    if (primary != null) {
                        primary.onLeave();
                    }
                    displayPrimary(Screens.BRIDGE);
                    e.consume();
                }
                break;
            case KeyEvent.VK_6:
                if (e.isControlDown()) {
                    CustomBalanceScreen cbs = (CustomBalanceScreen)control().displaySecondary(Screens.CUSTOM_BALANCE);
                    cbs.setCustomBalance(world().customBalanceSettings);
                    cbs.onComplete = new Action1<CustomBalanceSettings>() {
                        @Override
                        public void invoke(CustomBalanceSettings value) {
                            if (value != null) {
                                world().customBalanceSettings.copyFrom(value);
                            }
                        }
                    };

                    e.consume();
                }
                break;
            case KeyEvent.VK_7:
                if (e.isControlDown()) {
                    world().difficulty = Difficulty.EASY;
                    for (Player p : world().players.values()) {
                        p.difficulty = world().difficulty;
                    }
                    e.consume();
                }
                break;
            case KeyEvent.VK_8:
                if (e.isControlDown()) {
                    world().difficulty = Difficulty.NORMAL;
                    for (Player p : world().players.values()) {
                        p.difficulty = world().difficulty;
                    }
                    e.consume();
                }
                break;
            case KeyEvent.VK_9:
                if (e.isControlDown()) {
                    world().difficulty = Difficulty.HARD;
                    for (Player p : world().players.values()) {
                        p.difficulty = world().difficulty;
                    }
                    e.consume();
                }
                break;
            case KeyEvent.VK_B:
                if (e.isControlDown()) {
                    if (world().hasDiplomacy()) {
                        Player p = world().player;
                        for (Player p0 : p.knownPlayers().keySet()) {
                            if (!p0.noDiplomacy) {
                                if (e.isShiftDown()) {
                                    p.offers.put(p0.id, new DiplomaticOffer(CallType.RESIGN, ApproachType.HUMBLE));
                                } else {
                                    p.offers.put(p0.id, new DiplomaticOffer(CallType.ALLIANCE, ApproachType.HUMBLE));
                                }
                            }
                        }
                        signalMessage(p);
                    }
                }
                break;
            // FIXME CHEAT
            case KeyEvent.VK_O:
                if (e.isControlDown()) {
                    Planet p = world().player.currentPlanet;

                    if (p != null) {
                        if (p.owner == null) {
                            p.owner = world().player;
                            p.population(5000); // initial colony
                            p.race = world().player.race;
                            p.owner.statistics.planetsColonized.value++;
                            p.owner.planets.put(p, PlanetKnowledge.BUILDING);
                            world().scripting.onColonized(p);
                        } else {
                            p.takeover(world().player);
                        }
                        repaintInner();
                    }
                    e.consume();
                } else {
                    control().displayOptions(true, SettingsPage.AUDIO);
                }
                break;
            // FIXME CHEAT
            case KeyEvent.VK_I:
                // CHEAT: add more money
                if (e.isControlDown()) {
                    ResearchType crt = world().player.currentResearch();
                    if (crt != null) {
                        boolean researched = world().player.setAvailable(crt);
                        Integer cnt = world().player.inventory.get(crt);
                        cnt = cnt != null ? cnt + 1 : 1;
                        world().player.inventory.put(crt, cnt);
                        if (researched) {
                            commons.researchChanged(crt);
                        }
                        repaintInner();
                    }
                    e.consume();
                }
                break;
            case KeyEvent.VK_S:
                if (e.isControlDown()) {
                    control().save(null, SaveMode.QUICK);
                    e.consume();
                }
                break;
            case KeyEvent.VK_L:
                if (e.isControlDown()) {
                    control().load(null);
                    e.consume();
                }
                break;
            // FIXME CHEAT
            case KeyEvent.VK_G:
                // CHEAT: add more money
                if (e.isControlDown()) {
                    if (e.isShiftDown()) {
                        world().player.addMoney(100000);
                    } else {
                        world().player.addMoney(10000);
                    }
                    repaintInner();

                    e.consume();
                }
                break;
            // FIXME CHEAT:
            case KeyEvent.VK_K:
                if (e.isControlDown()) {
                    world().player.blackMarketRestock = null; // trigger instant restock
                }
                break;
            // FIXME CHEAT
            case KeyEvent.VK_J:
                // TOGGLE test AI on player
                if (e.isControlDown()) {
                    if (e.isShiftDown()) {
                        Player p = world().player;
                        for (Player p0 : p.knownPlayers().keySet()) {
                            if (!p0.noDiplomacy) {
                                DiplomaticOffer dio = new DiplomaticOffer(CallType.ALLIANCE, ApproachType.HUMBLE);
                                dio.value(1000);
                                p.offers.put(p0.id, dio);
                            }
                        }
                        signalMessage(p);
                    } else {
                        Player p = world().player;

                        if (p.aiMode == AIMode.NONE) {
                            p.aiMode = AIMode.TEST;
                            p.ai = new AITest();
                            p.ai.init(p);
                            System.out.println("Switching to TEST AI.");
                        } else {
                            p.aiMode = AIMode.NONE;
                            p.ai = new AIUser();
                            p.ai.init(p);
                            System.out.println("Switching to no AI.");
                        }
                    }

                    e.consume();
                }
                break;
            // FIXME CHEAT
            case KeyEvent.VK_P:
                // set current player
                if (e.isControlDown()) {
                    Player p = world().player;
                    Planet pl = p.currentPlanet;
                    if (p.currentPlanet != null && p.currentPlanet.owner != null) {
                        world().player = p.currentPlanet.owner;
                        world().player.currentPlanet = pl;
                        world().player.selectionMode = SelectionMode.PLANET;
                    }
                    e.consume();
                }
                break;
            // FIXME CHEAT
            case KeyEvent.VK_Z:
                Planet cp = world().player.currentPlanet;
                if (e.isControlDown() && cp != null) {
                    if (cp.quarantineTTL > 0) {
                        cp.quarantineTTL = 0;
                        world().cureFleets(cp);
                        world().scripting.onPlanetCured(cp);
                    } else {
                        cp.quarantineTTL = Planet.DEFAULT_QUARANTINE_TTL;
                        world().scripting.onPlanetInfected(cp);
                    }
                    e.consume();
                    repaintInner();
                }
                break;
            case KeyEvent.VK_X:
                if (e.isControlDown()) {
                    control().setFixedFrameRate(!control().isFixedFrameRate());
                    e.consume();
                    repaintInner();
                } else {
                    control().toggleQuickResearch();
                    e.consume();
                    control().moveMouse();
                    repaintInner();
                    break;
                }
                break;
            case KeyEvent.VK_C:
                control().toggleQuickProduction();
                e.consume();
                control().moveMouse();
                repaintInner();
                break;
            case KeyEvent.VK_H:
                if (e.isControlDown()) {
                    world().scripting.debug();
                    e.consume();
                    repaintInner();
                }
                break;
            case KeyEvent.VK_ESCAPE:
                control().displayOptions(true, SettingsPage.LOAD_SAVE);
                e.consume();
                break;
            default:
            }
        }
    }
    /**

     * Audio signal for new message.
     * @param p the target player

     */
    void signalMessage(Player p) {
        if (p == p.world.player) {
            if (ModelUtils.randomBool()) {
                p.world.env.playSound(SoundTarget.COMPUTER, SoundType.NEW_MESSAGE_1, null);
            } else {
                p.world.env.playSound(SoundTarget.COMPUTER, SoundType.NEW_MESSAGE_2, null);
            }
        }
    }
}
