/*
 * Copyright (C) 2022 - 2025 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.persistence.r2dbc.internal

import akka.actor.typed.ActorSystem
import akka.annotation.InternalApi
import akka.persistence.r2dbc.ConnectionPoolSettings
import akka.persistence.r2dbc.internal.h2.H2Dialect
import akka.persistence.r2dbc.internal.postgres.PostgresDialect
import akka.persistence.r2dbc.internal.postgres.YugabyteDialect
import akka.persistence.r2dbc.internal.sqlserver.SqlServerDialect
import akka.util.Helpers.toRootLowerCase
import com.typesafe.config.Config
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import akka.persistence.r2dbc.ConnectionFactoryProvider.ConnectionFactoryOptionsProvider

/**
 * INTERNAL API
 */
@InternalApi
private[r2dbc] object ConnectionFactorySettings {

  private val log = LoggerFactory.getLogger(getClass)

  def apply(system: ActorSystem[_]): ConnectionFactorySettings =
    apply(system.settings.config.getConfig("akka.persistence.r2dbc.connection-factory"))

  def apply(config: Config): ConnectionFactorySettings = {
    val dialect: Dialect = toRootLowerCase(config.getString("dialect")) match {
      case "yugabyte" => YugabyteDialect: Dialect
      case "postgres" => PostgresDialect: Dialect
      case "h2"       => H2Dialect: Dialect
      case "sqlserver" =>
        log.warn("The `sqlserver` dialect is currently marked as experimental and not yet production ready.")
        SqlServerDialect: Dialect
      case other =>
        throw new IllegalArgumentException(
          s"Unknown dialect [$other]. Supported dialects are [postgres, yugabyte, sqlserver, h2].")
    }

    // pool settings are common to all dialects but defined inline in the connection factory block
    // for backwards compatibility/convenience
    val poolSettings = new ConnectionPoolSettings(config)

    // H2 dialect doesn't support options-provider
    val optionsProvider = if (dialect == H2Dialect) "" else config.getString("options-provider")

    ConnectionFactorySettings(dialect, config, poolSettings, optionsProvider)
  }

}

/**
 * INTERNAL API
 */
@InternalApi
private[r2dbc] case class ConnectionFactorySettings(
    dialect: Dialect,
    config: Config,
    poolSettings: ConnectionPoolSettings,
    optionsProvider: String)
