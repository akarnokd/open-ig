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
import hu.openig.net.ErrorResponse;
import hu.openig.net.ErrorType;
import hu.openig.net.MessageUtils;
import hu.openig.net.MissingAttributeException;

import java.io.IOException;

/**
 * Transform for a MessageObject response.
 * @author akarnokd, 2013.05.02.
 * @param <T> the message object result
 */
public class MessageObjectAsync<T extends MessageObjectIO> extends AsyncTransform<Object, T, IOException> {
	/** The expected void return type. */
	protected final String[] responseType;
	/** The response container type. */
	protected final T response;
	/**
	 * Constructor, sets the fields.
	 * @param out the async result
	 * @param response the response object to fill in
	 * @param responseType the accepted response types
	 */
	public MessageObjectAsync(
			AsyncResult<? super T, ? super IOException> out,
			T response,
			String... responseType) {
		super(out);
		this.response = response;
		this.responseType = responseType;
	}
	@Override
	public void invoke(Object param1) throws IOException {
		try {
			response.fromMessage(MessageUtils.expectObject(param1, responseType));
			setValue(response);
		} catch (MissingAttributeException ex) {
			throw new ErrorResponse(ErrorType.FORMAT, ex.toString(), ex);
		}
	}
}