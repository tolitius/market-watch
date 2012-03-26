package org.gitpod.feed

import org.gitpod.tick.{FeedTickCount, TickCount}
import akka.util.Deadline
import akka.actor.{ActorRef, ActorLogging, Actor}

case class StreamTicks( deadline: Deadline )
case class SendTick( tick: Array[Byte] )

/**
 * TODO: Document Me
 *
 * @author anatoly.polinsky
 */

class MarketFeed( publisher: ActorRef ) extends Actor with ActorLogging {

  // placeholder for a 200 byte order
  val order = Array.fill( 200 ) { 42.toByte }

  private var ticksSent = 0L

  def receive: Receive = {

    case StreamTicks( deadline: Deadline ) if deadline.hasTimeLeft =>

        publisher ! SendTick( order )
        ticksSent += 1

        self ! StreamTicks( deadline )

    case TickCount =>

      sender ! FeedTickCount( self, ticksSent )

    case other => println( "market feed => " + other )
  }
}

