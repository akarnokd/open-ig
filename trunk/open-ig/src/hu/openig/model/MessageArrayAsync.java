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
import hu.openig.core.Func0;
import hu.openig.net.ErrorResponse;
import hu.openig.net.ErrorType;
import hu.openig.net.MessageArray;
import hu.openig.net.MessageObject;
import hu.openig.net.MessageUtils;
import hu.openig.net.MissingAttributeException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Transform for a MessageArray response by using a factory object.
 * @author akarnokd, 2013.05.02.
 * @param <T> the message object result
 */
public class MessageArrayAsync<T extends MessageObjectIO> 
extends AsyncTransform<Object, List<T>, IOException> {
	/** The expected void return type. */
	protected final String[] responseType;
	/** The response container type. */
	protected final Func0<? extends T> factory;
	/**
	 * Constructor, sets the fields.
	 * @param out the async result
	 * @param factory the response object to fill in
	 * @param responseType the accepted response types
	 */
	public MessageArrayAsync(
			AsyncResult<? super List<T>, ? super IOException> out,
			Func0<? extends T> factory,
			String... responseType) {
		super(out);
		this.factory = factory;
		this.responseType = responseType.clone();
	}
	@Override
	public void invoke(Object param1) throws IOException {
		MessageArray ma = MessageUtils.expectArray(param1, responseType);
		try {
			List<T> result = new ArrayList<>(ma.size());
			for (Object o : ma) {
				if (o instanceof MessageObject) {
					T t = factory.invoke();
					t.fromMessage((MessageObject)o);
					result.add(t);
				} else {
					throw new ErrorResponse(ErrorType.FORMAT, o != null ? o.getClass().toString() : "null");
				}
			}
			setValue(result);
		} catch (MissingAttributeException ex) {
			throw new ErrorResponse(ErrorType.FORMAT, ex.toString(), ex);
		}
	}
}