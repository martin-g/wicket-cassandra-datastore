package org.wicketstuff.datastore.cassandra.demo;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class HomePage extends WebPage {

	private int counter;
	
	public HomePage(final PageParameters pageParameters) {
		super(pageParameters);
		
		final Label label = new Label("counter", new PropertyModel<Integer>(this, "counter"));
		add(label);
		
		final Link<Void> link = new Link<Void>("link") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
//				getPage().dirty();
				counter++;
			}
		};
		add(link);
	}
	
	
}
