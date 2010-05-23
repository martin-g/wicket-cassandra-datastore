/**
 * 
 */
package com.gerolfseitz.wicket.pagestore

import org.apache.wicket.pageStore.IDataStore 
import org.apache.cassandra.service._
import org.apache.thrift.protocol._


import se.scalablesolutions.akka.persistence.cassandra._
import se.scalablesolutions.akka.persistence.common._

/**
 * @author gseitz
 *
 */
class CassandraDataStore(val applicationName: String, hostname: String, port: Int) extends IDataStore {
	
	private val sessions = new CassandraSessionPool(
			getKeyspace,
			StackPool(SocketProvider(hostname, port)),
			Protocol.Binary,
			2)
	
	implicit def intToByteArray(i: Int) : Array[Byte] = String.valueOf(i).getBytes
	
	def |<(k: String) = applicationName + "_" + k
	
	override def getData(sessionId: String, id: Int) : Array[Byte] = {
		sessions.withSession { session =>
			try {
				val option = session | (|<(sessionId), new ColumnPath(getColumnFamily, null, id))
				val col = option.get.getColumn
				col.getValue
			} catch {
				case e => null
			}
		}
	}

	override def removeData(sessionId: String, id: Int) = {
		sessions.withSession { session =>
			session -- ( |<(sessionId), new ColumnPath(getColumnFamily, null, id), System.currentTimeMillis)
		}
	}

	override def removeData(sessionId: String) = {
		sessions.withSession { session =>
			session -- ( |<(sessionId), new ColumnPath(getColumnFamily, null, null), System.currentTimeMillis)
		}
	}

	override def storeData(sessionId: String, id: Int, data: Array[Byte]) = {
		sessions.withSession { session =>
			session ++| ( |<(sessionId), new ColumnPath(getColumnFamily, null, id), data, System.currentTimeMillis)
		}
	}

	override def destroy = {
		sessions.close
	}

	override def isReplicated = true
	
	def getKeyspace = "wicket"
	
	def getColumnFamily = "Session"

}