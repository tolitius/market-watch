package org.gitpod.market

import akka.util.duration._
import org.gitpod.broker.Broker
import org.gitpod.feed.{StreamTicks, MarketFeed}
import watch._
import akka.actor.{Props, ActorSystem}

object Market extends App {

  val STREAM_FOR_SECONDS = 20 seconds
  val MARKET_OPEN_SECONDS = ( 30 seconds ) fromNow

  val system = ActorSystem( "freeMarket" )

  val feed = system.actorOf( Props( new MarketFeed( "nyse.ibm" ) ), name = "nyse.feed" )
  val broker = system.actorOf( Props[Broker], name = "vip.broker" )

  feed ! StreamTicks( STREAM_FOR_SECONDS fromNow )

  val marketWatch = system.actorOf( Props(
    new MarketWatch( new reportStats( new statsToString ) ) ), name = "marketWatch" )

  marketWatch ! RegisterFeed( "nyse.feed" )
  marketWatch ! RegisterBroker( "vip.broker" )

  while ( MARKET_OPEN_SECONDS.hasTimeLeft ) {}

  // Shutdown problems in ZeroMQ Akka: http://www.assembla.com/spaces/akka/tickets/1949
  system.shutdown()
}