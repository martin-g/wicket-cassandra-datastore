Wicket Cassandra Datastore
==========================

This project provides an org.apache.wicket.pageStore.IDataStore implementation that writes pages to an Apache Cassandra cluster.

Requirements
------------
* Scala 2.8.0.RC2
* Akka 0.9 (not yet released)
* Apache Wicket 1.5-SNAPSHOT (no release of 1.5 available yet)



Usage
-----
	class WicketApplication extends WebApplication {

		val HOST = ...
		val PORT = ...		

		override def getHomePage = classOf[MyHomePage]
		
		override def init = {
			setPageManagerProvider(new IPageManagerProvider() {
				override def get(IPageManagerContext) = {
					val asyncDS = new AsynchronousDataStore(new CassandraDataStore(getName, HOST, val))
					val ps = new DefaultPageStore(getName, asyncDS, 40)
					new PersistentPageManager(getName, ps, context)
				}
			}
		}
	
	}
