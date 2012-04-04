package org.gitpod.market.config

import akka.util.duration._
import org.gitpod.subscriber.akka.AkkaSubscriber
import org.gitpod.publisher.akka.AkkaPublisher
import org.gitpod.feed.MarketFeed
import akka.actor.{ActorRef, Props, ActorSystem}
import org.gitpod.subscriber.akka.zeromq.AkkaZeroMqSubscriber
import org.gitpod.publisher.akka.zeromq.AkkaZeroMqPublisher
import org.gitpod.subscriber.zeromq.ZeroMqSubscriber
import org.gitpod.publisher.zeromq.ZeroMqPublisher

/**
 * TODO: Document Me
 *
 * @author anatoly.polinsky
 */

object MarketConfig {

  /* bizz */
  val NYSE = "nyse"
  val NYSE_FEED = "nyse.feed"
  val VIP_BROKER = "vip.broker"
  val NYSE_UNDERLYING = "nyse.ibm"

  val STREAM_FOR_SECONDS = 15 seconds
  val MARKET_OPEN_SECONDS = ( 20 seconds ) fromNow

  val MARKET_KIND = "freeMarket"

  /* geek */
  val DEFAULT_PROFILE = "akka"

  def configMeUp( system: ActorSystem ) = {

    val ( broker: ActorRef, publisher ) = sys.props.getOrElse( "profile", DEFAULT_PROFILE ) match {

      case "akka.zmq" =>

        ( system.actorOf( Props( new AkkaZeroMqSubscriber( MarketConfig.NYSE ) ), name = MarketConfig.VIP_BROKER ),
          system.actorOf( Props( new AkkaZeroMqPublisher( MarketConfig.NYSE_UNDERLYING ) ) ) )

      case "zmq" =>

        ( system.actorOf( Props( new ZeroMqSubscriber( MarketConfig.NYSE ) ), name = MarketConfig.VIP_BROKER ),
          system.actorOf( Props( new ZeroMqPublisher( MarketConfig.NYSE_UNDERLYING ) ) ) )

      // "bad profile? no profile? no problem!" :)
      case akka =>

        val akkaBroker = system.actorOf( Props[AkkaSubscriber], name = MarketConfig.VIP_BROKER )
        ( akkaBroker,
          system.actorOf( Props( new AkkaPublisher( akkaBroker ) ) ) )

    }

    val feed = system.actorOf( Props( new MarketFeed( publisher ) ), name = MarketConfig.NYSE_FEED )

    ( feed, broker )
  }
}
