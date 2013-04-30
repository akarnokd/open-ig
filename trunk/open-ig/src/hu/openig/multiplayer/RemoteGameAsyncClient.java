/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.multiplayer;

import hu.openig.core.Action1;
import hu.openig.core.AsyncException;
import hu.openig.core.AsyncResult;
import hu.openig.core.AsyncTransform;
import hu.openig.core.Scheduler;
import hu.openig.multiplayer.model.MessageUtils;
import hu.openig.multiplayer.model.WelcomeResponse;
import hu.openig.net.ErrorResponse;
import hu.openig.net.MessageArray;
import hu.openig.net.MessageClient;
import hu.openig.net.MessageObject;
import hu.openig.net.MessageSerializable;
import hu.openig.utils.U;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Asynchronous game client.
 * @author akarnokd, 2013.04.30.
 *
 */
public class RemoteGameAsyncClient implements RemoteGameAsyncAPI {
	/**
	 * Dispatches a list of values to a list of async
	 * result receivers and notifies another async
	 * result when the whole batch has been dispatched.
	 * @author akarnokd, 2013.05.01.
	 */
	public static final class BatchResultAsync implements Runnable {
		/** The end notification async result if not null. */
		private final AsyncResult<? super Void, ? super IOException> out;
		/** The list of async results to notify. */
		private final List<AsyncResult<Object, ? super IOException>> callbacks;
		/** The list of results to dispatch. */
		private final List<Object> results;

		/**
		 * Constructor. Initializes the fields.
		 * @param out the end notification async result, may be null
		 * @param callbacks the list async results to notify
		 * @param ma the batch result array
		 */
		public BatchResultAsync(
				AsyncResult<? super Void, ? super IOException> out,
				List<AsyncResult<Object, ? super IOException>> callbacks,
				MessageArray ma) {
			this.out = out;
			this.callbacks = U.newArrayList(callbacks);
			this.results = new ArrayList<Object>(ma.size());
			
			int max = Math.max(ma.size(), this.callbacks.size());
			for (int i = 0; i < max; i++) {
				AsyncResult<Object, ? super IOException> ar = this.callbacks.get(i);
				Object o = ma.get(i);
				ErrorResponse er = ErrorResponse.asError(o);
				if (er != null) {
					results.add(er);
				} else {
					if (ar instanceof Action1<?>) {
						@SuppressWarnings("unchecked")
						Action1<Object> func1 = (Action1<Object>) ar;
						func1.invoke(o);
					}
					results.add(o);
				}
			}
		}

		@Override
		public void run() {
			for (int i = 0; i < results.size(); i++) {
				AsyncResult<Object, ? super IOException> ar = callbacks.get(i);
				Object o = results.get(i);
				if (o instanceof IOException) {
					ar.onError((IOException)o);
				} else {
					ar.onSuccess(o);
				}
			}
			if (out != null) {
				out.onSuccess(null);
			}
		}
	}

	/** The message client object. */
	protected final MessageClient client;
	/** The scheduler used to dispatch results. */
	protected final Scheduler scheduler;
	/** The list of callbacks filled in during batch operation. */
	protected List<AsyncResult<Object, ? super IOException>> callbacks;
	/** The composed batch request. */
	protected MessageArray batchRequest;
	/**
	 * Constructor. Sets the message client object.
	 * @param client the client object
	 * @param scheduler the scheduler
	 */
	public RemoteGameAsyncClient(MessageClient client, Scheduler scheduler) {
		this.client = client;
		this.scheduler = scheduler;
	}

	@Override
	public void begin() {
		if (isBatchMode()) {
			throw new IllegalStateException("Already in batch mode!");
		}
		callbacks = U.newArrayList();
		batchRequest = new MessageArray("BATCH");
	}

	@Override
	public void end() throws IOException {
		if (!isBatchMode()) {
			throw new IllegalStateException("Not in batch mode!");
		}
		// query batch
		MessageArray ma = MessageUtils.expectArray(client.query(batchRequest), "BATCH_RESPONSE");

		
		scheduler.schedule(new BatchResultAsync(null, callbacks, ma));

		batchRequest = null;
		callbacks = null;
	}

	@Override
	public void end(final AsyncResult<? super Void, ? super IOException> out) {
		if (!isBatchMode()) {
			throw new IllegalStateException("Not in batch mode!");
		}
		try {
			// query batch
			MessageArray ma = MessageUtils.expectArray(client.query(batchRequest), "BATCH_RESPONSE");

			scheduler.schedule(new BatchResultAsync(out, callbacks, ma));
		} catch (IOException ex) {
			scheduler.schedule(new AsyncException(ex, out));
		}
		batchRequest = null;
		callbacks = null;

	}
	/**
	 * Test if we are in batch mode.
	 * @return true if in batch mode
	 */
	protected boolean isBatchMode() {
		return callbacks != null;
	}
	/**
	 * Sends out the request or adds it to the batch
	 * request list, depending on the current mode.
	 * @param request the request object
	 * @param out the async result callback
	 */
	protected void send(MessageSerializable request, 
			AsyncResult<Object, ? super IOException> out) {
		if (isBatchMode()) {
			batchRequest.add(request);
			callbacks.add(out);
		} else {
			client.query(request, scheduler, out);
		}
	}
	@Override
	public void ping(AsyncResult<? super Long, ? super IOException> out) {
		MessageObject request = new MessageObject("PING");
		AsyncTransform<Object, Long, IOException> tr = new AsyncTransform<Object, Long, IOException>(out) {
			@Override
			public void invoke(Object value) {
				this.setValue(0L);
			}
		};
		send(request, tr);
	}
	@Override
	public void login(String user, String passphrase, String version,
			AsyncResult<? super WelcomeResponse, ? super IOException> out) {
		MessageObject request = new MessageObject("PING");
		AsyncTransform<Object, WelcomeResponse, IOException> tr = new AsyncTransform<Object, WelcomeResponse, IOException>(out) {
			@Override
			public void invoke(Object value) throws IOException {
				this.setValue(WelcomeResponse.from(MessageUtils.expectObject(value, "WELCOME")));
			}
		};
		send(request, tr);
	}
}
