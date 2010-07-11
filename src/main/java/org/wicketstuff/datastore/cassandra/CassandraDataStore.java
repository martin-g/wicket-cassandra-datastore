package org.wicketstuff.datastore.cassandra;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.Column;
import org.apache.cassandra.thrift.ColumnOrSuperColumn;
import org.apache.cassandra.thrift.ColumnPath;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.Deletion;
import org.apache.cassandra.thrift.Mutation;
import org.apache.cassandra.thrift.NotFoundException;
import org.apache.cassandra.thrift.SuperColumn;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.apache.wicket.pageStore.IDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author gseitz 
 * 	<br/>Initial (Scala) version - http://github.com/gseitz/wicket-cassandra-datastore
 * @author martin-g 
 * 	<br/>Translation to Java - http://github.com/martin-g/wicket-cassandra-datastore
 */
public class CassandraDataStore implements IDataStore {

	private static final Logger logger = LoggerFactory.getLogger(CassandraDataStore.class);
	
	private static final String KEYSPACE = "Wicket";
	
	private static final String COLUMN_FAMILY = "Session";
	
	private final String applicationName;
	private final TTransport transport;
	private final TProtocol protocol;
	private final Cassandra.Client client;

	public CassandraDataStore(final String appName, final String hostname, final int port) 
		throws TTransportException {
	
		this.applicationName = appName;
		this.transport = new TSocket(hostname, port);
		this.protocol = new TBinaryProtocol(transport);
		this.client = new Cassandra.Client(protocol);
		transport.open();
	}

	@Override
	public byte[] getData(final String sessionId, final int pageId) {

		final ColumnPath columnPath = new ColumnPath();
		columnPath.setColumn(bytes(pageId));
		columnPath.setColumn_family(COLUMN_FAMILY);
		columnPath.setSuper_column(bytes(sessionId));
		
		byte[] pageAsBytes = null;
		try {
			final ColumnOrSuperColumn columnOrSuperColumn = client.get(KEYSPACE, sessionId, columnPath, ConsistencyLevel.QUORUM);
			pageAsBytes = columnOrSuperColumn.column.getValue();
		} catch (final NotFoundException nfx) {
			// no such page
			logger.debug("No page with id '{}' in session with id '{}'", pageId, sessionId);
		} catch (final Exception x) {
			logger.error("Cannot get page with id '{}' in session with id '{}'", pageId, sessionId);
			logger.error(x.getMessage(), x);
		}
		
		return pageAsBytes;
	}

	@Override
	public void removeData(final String sessionId, final int pageId) {

		final ColumnPath columnPath = new ColumnPath();
		columnPath.setColumn(bytes(pageId));
		columnPath.setColumn_family(COLUMN_FAMILY);
		columnPath.setSuper_column(bytes(sessionId));
		try {
			client.remove(KEYSPACE, sessionId, columnPath, System.currentTimeMillis(), ConsistencyLevel.QUORUM);
		} catch (final Exception x) {
			logger.error("Cannot remove page with id '{}'", pageId);
			logger.error(x.getMessage(), x);
		}
	}

	@Override
	public void removeData(final String sessionId) {
		
		final Deletion deletion = new Deletion();
		deletion.setSuper_column(bytes(sessionId));
		
		final Mutation removeMutation = new Mutation();
		removeMutation.setDeletion(deletion);
		
		final List<Mutation> mutations = new ArrayList<Mutation>();
		mutations.add(removeMutation);
		
		final Map<String, List<Mutation>> job = new HashMap<String, List<Mutation>>();
		job.put(COLUMN_FAMILY, mutations);
		
		final Map<String, Map<String, List<Mutation>>> batch = new HashMap<String, Map<String,List<Mutation>>>();
		batch.put(sessionId, job);
		
		try {
			client.batch_mutate(KEYSPACE, batch, ConsistencyLevel.ANY);
		} catch (final Exception x) {
			logger.error("Cannot remove session with id '{}'", sessionId);
			logger.error(x.getMessage(), x);
		}
	}

	@Override
	public void storeData(final String sessionId, final int pageId, final byte[] data) {

		final Column pageVersionDataColumn = new Column(bytes(pageId), data, System.currentTimeMillis());

		final SuperColumn sessionIdSuperColumn = new SuperColumn();
		sessionIdSuperColumn.setName(bytes(sessionId));
		sessionIdSuperColumn.setColumns(Arrays.asList(pageVersionDataColumn));

		final ColumnOrSuperColumn newVersionColumnOrSuperColumn = new ColumnOrSuperColumn();
		newVersionColumnOrSuperColumn.setSuper_column(sessionIdSuperColumn);
		
		final Mutation storeMutation = new Mutation();
		storeMutation.setColumn_or_supercolumn(newVersionColumnOrSuperColumn);
		
		final List<Mutation> mutations = new ArrayList<Mutation>();
		mutations.add(storeMutation);
		
		final Map<String, List<Mutation>> job = new HashMap<String, List<Mutation>>();
		job.put(COLUMN_FAMILY, mutations);
		
		final Map<String, Map<String, List<Mutation>>> batch = new HashMap<String, Map<String,List<Mutation>>>();
		batch.put(sessionId, job);
		
		try {
			client.batch_mutate(KEYSPACE, batch, ConsistencyLevel.ANY);
		} catch (final Exception x) {
			logger.error("Cannot store new page version. Id: '{}'!", pageId);
			logger.error(x.getMessage(), x);
		}
	}
	
	@Override
	public void destroy() {
		try {
			transport.flush();
		} catch (TTransportException ttx) {
			logger.warn("Cannot flush the connection: {}", ttx.getMessage());
		}
		transport.close();
	}

	@Override
	public boolean isReplicated() {
		return true;
	}
	

	private byte[] bytes(final String target) {
		byte[] result = null;
		try {
			result = target.getBytes("UTF-8");
		} catch (final UnsupportedEncodingException e) {
			logger.error("Unsupported UTF-8 encoding?!");
		}
		return result;
	}
	
	private byte[] bytes(final int i) {
		return bytes(String.valueOf(i));
	}

}