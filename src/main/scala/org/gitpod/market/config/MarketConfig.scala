package org.gitpod.market.config

import akka.util.duration._

/**
 * TODO: Document Me
 *
 * @author anatoly.polinsky
 */

object MarketConfig {

  val NYSE = "nyse"
  val NYSE_FEED = "nyse.feed"
  val VIP_BROKER = "vip.broker"
  val NYSE_UNDERLYING = "nyse.ibm"

  val STREAM_FOR_SECONDS = 20 seconds
  val MARKET_OPEN_SECONDS = ( 30 seconds ) fromNow

  val MARKET_KIND = "freeMarket"
}
