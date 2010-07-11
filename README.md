Wicket Cassandra Datastore
==========================

This project provides an org.apache.wicket.pageStore.IDataStore implementation that writes pages to an Apache Cassandra cluster.


Requirements
------------
* Apache Cassandra 0.6.3 or newer
* Apache Wicket 1.5-SNAPSHOT (no release of 1.5 available yet)


Keyspace Defintion
------------------
	<Keyspace Name="Wicket">
		<ColumnFamily Name="Session"
				ColumnType="Super"
				CompareWith="UTF8Type"
				CompareSubcolumnsWith="UTF8Type"
				RowsCached="10000"
				KeysCached="50%"
				Comment="A column family with for session ids"/>
		/>
		<ReplicaPlacementStrategy>org.apache.cassandra.locator.RackUnawareStrategy</ReplicaPlacementStrategy>
		<ReplicationFactor>1</ReplicationFactor>
		<EndPointSnitch>org.apache.cassandra.locator.EndPointSnitch</EndPointSnitch>
	</Keyspace>

Schema
-----------------
keystore (Wicket)
   |
   - super column family (Session)
       |
       - super column (session id)
          |
          - Column (page id)


Usage
-----
	see org.wicketstuff.datastore.cassandra.demo.DemoApplication in src/test/java
