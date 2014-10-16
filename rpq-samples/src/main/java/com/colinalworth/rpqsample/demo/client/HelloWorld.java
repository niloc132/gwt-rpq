package com.colinalworth.rpqsample.demo.client;

import java.util.Date;

import com.colinalworth.rpqsample.demo.shared.HelloRequest;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.i18n.shared.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.RootPanel;

public class HelloWorld implements EntryPoint {

	@Override
	public void onModuleLoad() {
		// Start by making a new request to send to the server
		// This instance can have one or several requests added to it before it is fired
		final HelloRequest req = GWT.create(HelloRequest.class);
		req.setServiceEntryPoint("/rpq");
		
		// Make a few buttons that do several things for us...
		
		// First, we'll make two ways for the page to queue up requests to send
		Button hiButton = new Button("Say Hi");
		hiButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// Have to get the user's name from somewhere - usually you'd have a form or some
				// other way to interact with the page
				String name = Window.prompt("What is your name?", "");
				
				// Get a reference to the HelloServiceAsync, and make the call, just like in normal 
				// RPC.
				req.helloService().sayHiTo(name, new AsyncCallback<String>() {
					@Override
					public void onSuccess(String result) {
						// As in normal RPC, make use of the result when it arrives. Note however
						// that this may take longer than usual - just clicking this has no effect 
						// directly!
						Window.alert("Success: " + result);
					}
					@Override
					public void onFailure(Throwable caught) {
						// failure is unlikely for such a simple example, but still good to keep an
						// eye on why it failed
						Window.alert("Error: " + caught.getMessage());
					}
				});
				// Note that we didn't call fire - this button will have no effect right away!
			}
		});

		Button timeButton = new Button("Get Current Time");
		timeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// Again, we'll queue up a request to be made when we finally fire.
				req.helloService().getCurrentTime(new AsyncCallback<Date>() {
					@Override
					public void onSuccess(Date result) {
						Window.alert("Current time is " + DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM).format(result));
					}
					
					@Override
					public void onFailure(Throwable caught) {
						Window.alert("Error: " + caught.getMessage());
					}
				});
			}
		});
		
		// Next, a button to artificially add processing time on the server via Thread.sleep. This
		// allows us to demonstrate that multiple concurrent requests can be made and will work
		Button delayButton = new Button("Add Extra Delay");
		delayButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Window.alert("Whoops, there is an issue with this button (#2), stay tuned...");
//				String delay = Window.prompt("Time to delay server response (in seconds)", "5");
//				
//				try {
//					int delayInt = Integer.parseInt(delay);
//					// Note that there is no callback here - RPQ does not require all methods
//					// to have a callback, but it is expected then that some other method will
//					// be used to communicate success/failure to the client
//					req.helloService().delay(delayInt);
//				} catch (NumberFormatException ex) {
//					Window.alert("Unable to parse number, request not added: " + delay);
//				}
			}
		});

		
		// Finally, we'll add a way to actually fire these requests - the above buttons have no
		// effect until this button is called!
		Button fireButton = new Button("Fire Request");
		fireButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				req.fire();
			}
		});

		HTMLPanel panel = new HTMLPanel("<h3>This first set of buttons adds requests on the queue " +
				"to be run when the request is sent. Clicking a button multiple times will result " +
				"in the call running several times on the server</h3>" +
				"<div id='hi'></div>" +
				"<div id='time'></div>" +
				"<h3>Next, this button introduces an artificial delay in loading the data on the " +
				"server (by using Thread.sleep()), allowing for a demonstration in multiple " +
				"concurrent requests and queues:</h3>" +
				"<div id='delay'></div>" +
				"<h3>Finally, this actually fires the request, asking the server to process the " +
				"invocations made by the above buttons.</h3>" +
				"<div id='fire'></div>");
		panel.add(hiButton, "hi");
		panel.add(timeButton, "time");
		
		panel.add(delayButton, "delay");
		
		panel.add(fireButton, "fire");
		
		RootPanel.get().add(panel);
	}

}
