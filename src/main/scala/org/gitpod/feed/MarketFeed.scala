package org.gitpod.feed

import akka.zeromq._
import org.gitpod.tick.{FeedTickCount, TickCount}
import akka.util.Deadline
import akka.actor.{ActorLogging, Actor}

case class StreamTicks( deadline: Deadline )
case class SendTick( tick: Array[Byte] )

class MarketFeed( id: String ) extends Actor with ActorLogging {

  // placeholder for a 200 byte order
  val order = Array.fill( 200 ) { 42.toByte }

  val zsocket = context.system.newSocket( SocketType.Pub, Bind( "ipc://market.queue.ipc" ) )

  private var ticksSent = 0L

  def receive: Receive = {

    case StreamTicks( deadline: Deadline ) =>

      if ( deadline.hasTimeLeft ) {
        self ! SendTick( order )
        self ! StreamTicks( deadline )
      }
      else
        log.debug( "{" + id + "} feed streamed " + ticksSent + " ticks" )

    case SendTick( tick: Array[Byte] ) =>
      
      zsocket ! ZMQMessage( Seq( Frame( id ), Frame( tick ) ) )
      ticksSent += 1
      
    case TickCount =>

      sender ! FeedTickCount( self, ticksSent )

    case other => println( "market feed => " + other )
  }
}

