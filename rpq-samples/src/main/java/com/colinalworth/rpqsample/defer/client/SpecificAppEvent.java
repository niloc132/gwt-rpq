package com.colinalworth.rpqsample.defer.client;

import com.colinalworth.rpqsample.defer.client.SpecificAppEvent.SpecificAppEventHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class SpecificAppEvent extends GwtEvent<SpecificAppEventHandler> {
	private static GwtEvent.Type<SpecificAppEventHandler> TYPE = new GwtEvent.Type<SpecificAppEventHandler>();
	public static GwtEvent.Type<SpecificAppEventHandler> getType() {
		return TYPE;
	}
	@Override
	public GwtEvent.Type<SpecificAppEventHandler> getAssociatedType() {
		return getType();
	}
	@Override
	protected void dispatch(SpecificAppEventHandler handler) {
		handler.onSpecificAppEventHappening(this);
	}

	public interface SpecificAppEventHandler extends EventHandler {
		void onSpecificAppEventHappening(SpecificAppEvent event);
	}
}
