/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.AsyncResult;
import hu.openig.core.AsyncTransform;
import hu.openig.net.MessageUtils;

import java.io.IOException;

/**
 * Transform for a void response.
 * @author akarnokd, 2013.05.02.
 */
public class VoidAsync extends AsyncTransform<Object, Void, IOException> {
	/** The expected void return type. */
	protected final String[] responseType;
	/**
	 * Constructor, sets the fields.
	 * @param out the async result
	 * @param responseType the accepted response types
	 */
	public VoidAsync(AsyncResult<? super Void, ? super IOException> out, String... responseType) {
		super(out);
		this.responseType = responseType;
	}
	@Override
	public void invoke(Object param1) throws IOException {
		MessageUtils.expectObject(param1, responseType);
	}
}