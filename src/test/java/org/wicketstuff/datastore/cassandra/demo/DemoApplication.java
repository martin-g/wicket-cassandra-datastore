package org.wicketstuff.datastore.cassandra.demo;

import org.apache.thrift.transport.TTransportException;
import org.apache.wicket.IPageManagerProvider;
import org.apache.wicket.Page;
import org.apache.wicket.pageStore.AsynchronousDataStore;
import org.apache.wicket.pageStore.DefaultPageStore;
import org.apache.wicket.pageStore.DiskDataStore;
import org.apache.wicket.pageStore.IDataStore;
import org.apache.wicket.pageStore.IPageManager;
import org.apache.wicket.pageStore.IPageManagerContext;
import org.apache.wicket.pageStore.PersistentPageManager;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.mapper.MountedMapper;
import org.wicketstuff.datastore.cassandra.CassandraDataStore;

public class DemoApplication extends WebApplication {

	@Override
	public Class<? extends Page> getHomePage() {
		return HomePage.class;
	}
	
	@Override
	public void init() {
		super.init();
		
		getRootRequestMapperAsCompound().add(new MountedMapper("/demo", HomePage.class));
		
		setPageManagerProvider(new IPageManagerProvider() {

			public IPageManager get(final IPageManagerContext context) {

			    IDataStore dataStore;
				try {
					dataStore = new CassandraDataStore(getName(), "localhost", 9160);
					
				} catch (TTransportException e) {
					e.printStackTrace();
					dataStore = new DiskDataStore(getName(), 200, 4);
				}
				AsynchronousDataStore asyncDS = new AsynchronousDataStore(dataStore);
			    final DefaultPageStore pageStore = new DefaultPageStore(getName(), asyncDS, 40);
			    return new PersistentPageManager(getName(), pageStore, context);
			}
		});
	}
	
	

}
