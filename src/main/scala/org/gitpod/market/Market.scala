package org.gitpod.market

import config.MarketConfig
import org.gitpod.feed.StreamTicks
import watch._
import akka.actor.{Props, ActorSystem}

/**
 * TODO: Document Me
 *
 * @author anatoly.polinsky
 */

object Market extends App {

  val system = ActorSystem( MarketConfig.MARKET_KIND )

  val ( feed, broker ) = MarketConfig.configMeUp( system )

  feed ! StreamTicks( MarketConfig.STREAM_FOR_SECONDS fromNow )

  val marketWatch = system.actorOf( Props(
    new MarketWatch( new reportStats( new statsToString ) ) ), name = "marketWatch" )

  marketWatch ! RegisterFeed( MarketConfig.NYSE_FEED )
  marketWatch ! RegisterBroker( MarketConfig.VIP_BROKER )

  // ala context.setReceiveTimeout( MarketConfig.MARKET_OPEN_SECONDS )
  while ( MarketConfig.MARKET_OPEN_SECONDS.hasTimeLeft ) {}

  // Shutdown problems in ZeroMQ Akka: http://www.assembla.com/spaces/akka/tickets/1949
  system.shutdown()
}