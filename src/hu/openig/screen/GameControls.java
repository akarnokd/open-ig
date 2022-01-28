/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen;

import hu.openig.core.Action0;
import hu.openig.core.Func1;
import hu.openig.core.SaveMode;
import hu.openig.model.AIManager;
import hu.openig.model.Player;
import hu.openig.model.Screens;
import hu.openig.model.World;
import hu.openig.screen.api.SettingsPage;
import hu.openig.ui.UIComponent;

import java.awt.FontMetrics;
import java.io.Closeable;

import javax.swing.JComponent;

/**
 * Interface for interacting with the game window or other objects in a global manner.
 * @author akarnokd, 2010.01.06.
 */
public interface GameControls {
    /**
     * Switches the current display language to the new one.
     * Reloads all language dependant resources and refreshes the screen.
     * @param newLanguage the new language to change to
     */
    void switchLanguage(String newLanguage);
    /**
     * Display the given screen as the primary object. The secondary object, if any, will be removed.
     * @param screen the new screen to display
     * @return the reference to the new screen.
     */
    ScreenBase displayPrimary(Screens screen);
    /**
     * Display the given secondary screen.
     * @param screen the screen to display as secondary
     * @return the reference to the new screen.
     */
    ScreenBase displaySecondary(Screens screen);
    /** Hide the secondary screen. */
    void hideSecondary();
    /** Display the options screen. */
    void displayOptions();
    /** Hide the options screen. */
    void hideOptions();
    /**
     * Play the given set of videos.
     * @param videos the list of videos to play
     */
    void playVideos(String... videos);
    /**
     * Play the given list of animations then call the given completion handler.
     * @param onComplete the completion handler
     * @param videos the videos to play
     */
    void playVideos(Action0 onComplete, String... videos);
    /** Display the status bar. */
    void displayStatusbar();
    /** Hide the statusbar. */
    void hideStatusbar();
    /** Exit the game. */
    void exit();
    /** Repaint the window. */
    void repaintInner();
    /** @return Get the width of the rendering component. */
    int getInnerWidth();
    /** @return Get the height of the rendering component. */
    int getInnerHeight();
    /**
     * Repaint a particular region within the rendering component.
     * @param x the region X
     * @param y the region Y
     * @param w the region width
     * @param h the region height
     */
    void repaintInner(int x, int y, int w, int h);
    /**
     * Ask for a font metrics.
     * @param size the target font size
     * @return the default font metrics object
     */
    FontMetrics fontMetrics(int size);
    /**
     * Returns the rendering component used for display.
     * @return the rendering component
     */
    JComponent renderingComponent();
    /**
     * Save the world.
     * @param name the name the user entered, ony if mode == MANUAL
     * @param mode the mode
     */
    void save(String name, SaveMode mode);
    /**
     * Load the world state.
     * @param name the save name or null to load the most recent.
     */
    void load(String name);
    /**
     * Restarts the current skirmish game.
     */
    void restart();
    /**
     * Register a periodic timer action with the given delay.
     * @param delay the delay in milliseconds.
     * @param action the action
     * @return the handler to cancel the registration
     */
    Closeable register(int delay, Action0 action);
    /** @return The current world. */
    World world();
    /** @return the current primary screen type or null if none. */
    Screens primary();
    /** @return the current secondary screen type or null if none. */
    Screens secondary();
    /** End the current game. */
    void endGame();
    /** Start the next battle from the pending battles. */
    void startBattle();
    /**

     * Creates a fake mouse move event by sending a MOVE to the current screens.
     * Can be used to update event listeners when the screen changes under the mouse (e.g., ship
     * walk transition).

     */
    void moveMouse();
    /**

     * Display an error text in the status bar for a short period of time.
     * @param text the text to display

     */
    void displayError(String text);
    /**
     * Returns the AI factory function.
     * @return the AI factory function
     */
    Func1<Player, AIManager> aiFactory();
    /**

     * Returns the specific sceen object without switching to it.
     * @param <T> the target type
     * @param screen the screen object
     * @return the requested screen
     */
    <T extends ScreenBase> T getScreen(Screens screen);
    /**
     * Force display of the given message on bridge.
     * @param messageId the message ID
     * @param onSeen the action to perform once the user saw the message
     */
    void forceMessage(String messageId, Action0 onSeen);
    /**
     * Lose the current game. Game over.
     */
    void loseGame();
    /**
     * Win the current game.
     */
    void winGame();
    /**
     * Display or hide the objectives.
     * @param state the state
     */
    void showObjectives(boolean state);
    /** @return is the game in full screen mode? */
    boolean isFullscreen();
    /**
     * Set the full screen mode.
     * @param value the full screen status
     */
    void setFullscreen(boolean value);
    /** Rerun the screen resize calculations. */
    void runResize();
    /** Resize the window to match the current scaling and minimum required window size. */
    void windowToScale();
    /**

     * Notify the renderer that a component's tooltip has changed.
     * @param c the component

     */
    void tooltipChanged(UIComponent c);
    /** @return true if the full screen movie screen is visible. */
    boolean movieVisible();
    /** @return true if the options screen is visible. */
    boolean optionsVisible();
    /**
     * Enable or disable the UIComponent helper rectangles for debug purposes.
     * @param value the state
     */
    void setUIComponentDebug(boolean value);
    /** @return is the UIComponent helper rectangles enabled? */
    boolean isUIComponentDebug();
    /**
     * Display the options screen with the given configuration.
     * @param save may the user save?
     * @param page the page to display
     */
    void displayOptions(boolean save, SettingsPage page);
    /**
     * Make the objectives panel visible or hidden.
     * @param value the visibility status
     */
    void setObjectivesVisible(boolean value);
    /**
     * @return is the objectives panel visible?
     */
    boolean isObjectivesVisible();
    /** Toggle the visibility of the quick research panel. */
    void toggleQuickResearch();
    /** Toggle the visibility of the quick production panel. */
    void toggleQuickProduction();
    /** @return is the fixed frame rate enabled? */
    boolean isFixedFrameRate();
    /**
     * Enable or disable the fixed framerate display mode.
     * @param value enable or disable
     */
    void setFixedFrameRate(boolean value);
}
