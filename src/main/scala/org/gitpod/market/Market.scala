package org.gitpod.market

import config.MarketConfig
import org.gitpod.feed.{StreamTicks, MarketFeed}
import watch._
import akka.actor.{Props, ActorSystem}
import org.gitpod.publisher.zeromq.ZeroMqPublisher
import org.gitpod.subscriber.zeromq.ZeroMqSubscriber

/**
 * TODO: Document Me
 *
 * @author anatoly.polinsky
 */

object Market extends App {

  val system = ActorSystem( MarketConfig.MARKET_KIND )

  // val publisher = system.actorOf( Props( new AkkaZeroMqPublisher( MarketConfig.NYSE_UNDERLYING ) ) )
  val publisher = system.actorOf( Props( new ZeroMqPublisher( MarketConfig.NYSE_UNDERLYING ) ) )

  val feed = system.actorOf( Props( new MarketFeed( publisher ) ), name = MarketConfig.NYSE_FEED )
  // val broker = system.actorOf( Props( new AkkaZeroMqSubscriber( MarketConfig.NYSE ) ), name = MarketConfig.VIP_BROKER )
  val broker = system.actorOf( Props( new ZeroMqSubscriber( MarketConfig.NYSE ) ), name = MarketConfig.VIP_BROKER )

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