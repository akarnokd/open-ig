package hu.openig.core;

import hu.openig.model.Cursors;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author p-smith, 2016.01.22.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface CursorResource {
    int x();
    int y();
    Cursors key();
}
