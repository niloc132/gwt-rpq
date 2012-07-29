package com.colinalworth.rpqsample.defer.client;

import com.colinalworth.rpqsample.defer.shared.SampleQueue;
import com.colinalworth.rpqsample.defer.shared.SampleServiceAsync;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

public class SampleQueueImpl implements SampleQueue {
	private final SampleQueue queue = GWT.create(SampleQueue.class);

	@Override
	public void fire() {
		// Doesn't particularly matter how many times we schedule this,
		// especially for demonstration purposes, because empty fire()
		// invocations will do nothing
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				queue.fire();
			}
		});
	}

	@Override
	public void setServiceEntryPoint(String address) {
		queue.setServiceEntryPoint(address);
	}

	@Override
	public SampleServiceAsync service() {
		fire();
		return queue.service();
	}

}
