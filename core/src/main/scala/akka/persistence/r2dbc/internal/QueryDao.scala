/*
 * Copyright (C) 2022 - 2025 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.persistence.r2dbc.internal

import akka.NotUsed
import akka.annotation.InternalApi
import JournalDao.SerializedJournalRow
import akka.stream.scaladsl.Source

import java.time.Instant
import scala.concurrent.Future

/**
 * INTERNAL API
 */
@InternalApi
private[r2dbc] trait QueryDao extends BySliceQuery.Dao[SerializedJournalRow] {

  /**
   * Events are append only
   */
  override def countBucketsMayChange: Boolean = false

  def timestampOfEvent(persistenceId: String, seqNr: Long): Future[Option[Instant]]

  def latestEventTimestamp(entityType: String, minSlice: Int, maxSlice: Int): Future[Option[Instant]]

  def loadEvent(persistenceId: String, seqNr: Long, includePayload: Boolean): Future[Option[SerializedJournalRow]]

  def loadLastEvent(persistenceId: String, toSeqNr: Long, includeDeleted: Boolean): Future[Option[SerializedJournalRow]]

  def eventsByPersistenceId(
      persistenceId: String,
      fromSequenceNr: Long,
      toSequenceNr: Long,
      includeDeleted: Boolean): Source[SerializedJournalRow, NotUsed]

  def persistenceIds(entityType: String, afterId: Option[String], limit: Long): Source[String, NotUsed]

  def persistenceIds(afterId: Option[String], limit: Long): Source[String, NotUsed]

}
