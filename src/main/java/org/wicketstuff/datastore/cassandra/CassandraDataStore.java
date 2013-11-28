package org.wicketstuff.datastore.cassandra;

import java.nio.ByteBuffer;

import org.apache.wicket.pageStore.IDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;

/**
 * 
 */
public class CassandraDataStore implements IDataStore
{
	private static final Logger logger = LoggerFactory.getLogger(CassandraDataStore.class);

	private static final String COLUMN_SESSION_ID = "sessionId";

	private static final String COLUMN_PAGE_ID = "pageId";

	private static final String COLUMN_DATA = "data";
	
	private final String keyspace = "Wicket";

	private final String table = "PageStore";

	private final Cluster cluster;

	private final Session session;

	private final int timeToLive = 1800; // in seconds. 30 mins

	public CassandraDataStore()
	{
		cluster = Cluster.builder()
				.addContactPoint("localhost").build();
		Metadata metadata = cluster.getMetadata();
		System.out.printf("Connected to cluster: %s\n",
				metadata.getClusterName());
		for ( Host host : metadata.getAllHosts() ) {
			logger.debug("Datatacenter: {}; Host: {}; Rack: {}\n",
					new Object[] {host.getDatacenter(), host.getAddress(), host.getRack()});
		}

		session = cluster.connect();

		session.execute(
				String.format("CREATE KEYSPACE IF NOT EXISTS %s WITH replication " +
				"= {'class':'SimpleStrategy', 'replication_factor':3};", keyspace));

		session.execute(
				String.format(
				"CREATE TABLE IF NOT EXISTS %s.%s (" +
					"%s varchar," +
					"%s int," +
					"%s blob," +
					"PRIMARY KEY (%s, %s)" +
				");", keyspace, table, COLUMN_SESSION_ID, COLUMN_PAGE_ID, COLUMN_DATA,
						COLUMN_SESSION_ID, COLUMN_PAGE_ID));
	}

	@Override
	public byte[] getData(String sessionId, int pageId)
	{
		Select.Where dataSelect = QueryBuilder
				.select("data")
				.from(keyspace, table)
				.where(QueryBuilder.eq(COLUMN_SESSION_ID, sessionId))
				.and(QueryBuilder.eq(COLUMN_PAGE_ID, pageId));
		ResultSet rows = session.execute(dataSelect);
		Row row = rows.one();
		byte[] bytes = null;
		if (row != null)
		{
			ByteBuffer data = row.getBytes(COLUMN_DATA);
			bytes = new byte[data.remaining()];
			data.get(bytes);

			logger.debug("Got {} for session '{}' and page id '{}'",
					new Object[] {bytes != null ? "data" : "'null'", sessionId, pageId});
		}
		return bytes;
	}

	@Override
	public void removeData(String sessionId, int pageId)
	{
		Delete.Where delete = QueryBuilder
				.delete()
				.all()
				.from(keyspace, table)
				.where(QueryBuilder.eq(COLUMN_SESSION_ID, sessionId))
				.and(QueryBuilder.eq(COLUMN_PAGE_ID, pageId));
		ResultSet rows = session.execute(delete);
		if (logger.isDebugEnabled())
		{
			logger.debug("Deleted {} rows for session '{}' and page with id '{}'",
					new Object[] {rows.all().size(), sessionId, pageId});
		}
	}

	@Override
	public void removeData(String sessionId)
	{
		Delete.Where delete = QueryBuilder
				.delete()
				.all()
				.from(keyspace, table)
				.where(QueryBuilder.eq(COLUMN_SESSION_ID, sessionId));
		session.execute(delete);

		logger.debug("Deleted data for session '{}'", sessionId);
	}

	@Override
	public void storeData(String sessionId, int pageId, byte[] data)
	{
		Insert insert = QueryBuilder
				.insertInto(keyspace, table)
				.using(QueryBuilder.ttl(timeToLive))
				.values(new String[]{COLUMN_SESSION_ID, COLUMN_PAGE_ID, COLUMN_DATA},
						new Object[]{sessionId, pageId, ByteBuffer.wrap(data)});
		session.execute(insert);

		logger.debug("Inserted data for session '{}' and page id '{}'",
					sessionId, pageId);
	}

	@Override
	public void destroy()
	{
		if (cluster != null)
		{
			cluster.shutdown();
		}
	}

	@Override
	public boolean isReplicated()
	{
		return true;
	}

	@Override
	public boolean canBeAsynchronous()
	{
		return false;
	}
}
