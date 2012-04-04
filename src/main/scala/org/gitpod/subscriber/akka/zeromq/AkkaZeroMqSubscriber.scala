package org.gitpod.subscriber.akka.zeromq

import akka.zeromq._
import akka.actor.{ActorLogging, Actor}
import org.gitpod.tick.{BrokerTickCount, TickCount}

/**
 * TODO: Document Me
 *
 * @author anatoly.polinsky
 */

class AkkaZeroMqSubscriber( feed: String ) extends Actor with ActorLogging {

  context.system.newSocket( SocketType.Sub, Listener( self ),
    Connect( "ipc://market.queue.ipc" ),
    Subscribe( feed ) )

  private var ticksConsumed = 0L

  def receive = {

    case m: ZMQMessage =>

      ticksConsumed += 1

    case TickCount =>

      sender ! BrokerTickCount( self, ticksConsumed )

    case other => log.debug( "broker => " + other )
  }
}
