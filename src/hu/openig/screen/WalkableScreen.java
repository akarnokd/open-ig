package hu.openig.screen;

import hu.openig.model.Cursors;
import hu.openig.model.WalkPosition;
import hu.openig.model.WalkTransition;
import hu.openig.screen.items.ScreenUtils;

import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Represents a screen having clickable transition areas that trigger
 * transitions to other screens.
 * <p>
 * Screens deriving from this class have transitions set up between themselves
 * in walks.xml.
 * @author p-smith, 2016.01.22.
 */
public abstract class WalkableScreen extends ScreenBase {
    /** The transition the mouse is pointing at. */
    private WalkTransition pointerTransition;

    /** The screen origins. */
    protected final Rectangle base;

    /** Are the transition areas enabled? */
    private boolean transitionsEnabled = true;

    /**
     * Initializes this screen with a zero-width, zero-height rectangle as the
     * screen origins.
     */
    protected WalkableScreen() {
        this.base = new Rectangle();
    }

    /**
     * Initializes this screen with the specified rectangle that defines the
     * screen origins.
     * @param base the rectangle defining the screen origins
     */
    protected WalkableScreen(Rectangle base) {
        this.base = base;
    }

    /**
     * Draws the transition label using the active transition area.
     * @param g2 the target canvas to draw the label on
     * @throws IllegalStateException if no transition is active
     */
    protected void drawTransitionLabel(Graphics2D g2) {
        ScreenUtils.drawTransitionLabel(g2, getTransition(), base, commons);
    }

    /**
     * Returns the current position.
     * <p>
     * This is a template method subclasses must override to supply
     * the current position when queried.
     */
    protected abstract WalkPosition getPosition();

    /**
     * Updates which transition the mouse is pointing at from the specified
     * mouse coordinates. Does nothing when transitions are disabled.
     * @param mouseX x coord of the current mouse position
     * @param mouseY y coord of the current mouse position
     * @return true if the pointed transition changed since the last call,
     * false otherwise or if the transitions are disabled
     */
    protected boolean updateTransition(int mouseX, int mouseY) {
        if (!transitionsEnabled)
            return false;

        WalkTransition prev = pointerTransition;
        WalkTransition current = null;

        for (WalkTransition wt : getPosition().transitions) {
            if (wt.area.contains(mouseX - base.x, mouseY - base.y)) {
                current = wt;
                break;
            }
        }

        if (current == prev)
            return false;

        setActiveTransition(current);
        return true;
    }

    /**
     * Returns true if the mouse is hovering over a transition area.
     * <p>
     * This method only returns the result of the last
     * {@link #updateTransition(int, int)} call
     * and does not actually determine if the mouse is over a transition area.
     * @return true if the mouse is hovering over a transition area
     */
    protected boolean overTransitionArea() {
        return pointerTransition != null;
    }

    /**
     * Returns the active transition.
     * @return the active transition
     * @throws IllegalStateException if no transition is active
     */
    protected WalkTransition getTransition() {
        if (pointerTransition == null)
            throw new IllegalStateException("No active transition.");

        return pointerTransition;
    }

    /**
     * Performs the transition from the current position to the target position.
     * @param skipVideo indicates if the transition video should be skipped
     * @throws IllegalStateException if no transition is active
     */
    protected void performTransition(boolean skipVideo) {
        ScreenUtils.doTransition(getPosition(), getTransition(), commons, skipVideo);
    }

    /**
     * Clears the active transition.
     */
    protected void clearTransition() {
        setActiveTransition(null);
    }

    /**
     * Enables or disables the transition areas. When disabled, transitions are
     * not possible and transition areas will not be "highlighted".
     * @param enable indicates if transition areas should be enabled
     */
    protected void setTransitionsEnabled(boolean enable) {
        transitionsEnabled = enable;
        if (!enable)
            setActiveTransition(null);
    }

    private void setActiveTransition(WalkTransition transition) {
        pointerTransition = transition;
        if (pointerTransition == null)
            commons.setCursor(Cursors.POINTER);
        else if (pointerTransition.cursor != null)
            commons.setCursor((pointerTransition.cursor));
    }
}
