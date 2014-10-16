package com.colinalworth.rpqsample.defer.client;

import com.colinalworth.rpqsample.defer.client.SpecificAppEvent.SpecificAppEventHandler;
import com.colinalworth.rpqsample.defer.shared.SampleQueue;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;

public class SimpleAutoBatching implements EntryPoint {
	private EventBus eventBus = new SimpleEventBus();
	private SampleQueue req = new SampleQueueImpl();

	public SimpleAutoBatching() {
		req.setServiceEntryPoint("/rpq");
	}

	@Override
	public void onModuleLoad() {
		// In this example, we demonstrate wrapping the RequestQueue so that fire() is 
		// automatically invoked, so that it can be used like normal RPC, but that this
		// fire() happens at the end of the event loops, so that multiple service calls
		// end up getting automatically batched.

		// One one button is added to the page, and the user will interact with it to
		// make the fire occur. This button click could represent a user logging in, and 
		// as a result multiple client widgets/presenters could attempt to make calls to
		// the server, which are now automatically grouped together.

		Button action = new Button("Action");
		action.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				eventBus.fireEvent(new SpecificAppEvent());
			}
		});
		RootPanel.get().add(action);

		eventBus.addHandler(SpecificAppEvent.getType(), new SpecificAppEventHandler() {
			@Override
			public void onSpecificAppEventHappening(SpecificAppEvent event) {
				req.service().trimString("   test", new AsyncCallback<String>() {
					@Override
					public void onSuccess(String result) {
						Window.alert("4 = " + result.length());
					}

					@Override
					public void onFailure(Throwable caught) {
						//ignore
					}
				});
			}
		});

		eventBus.addHandler(SpecificAppEvent.getType(), new SpecificAppEventHandler() {
			@Override
			public void onSpecificAppEventHappening(SpecificAppEvent event) {
				req.service().addOne(4, new AsyncCallback<Integer>() {
					@Override
					public void onSuccess(Integer result) {
						Window.alert("5 = " + result);
					}
					@Override
					public void onFailure(Throwable caught) {
						//ignore
					}
				});
			}
		});
	}

}
