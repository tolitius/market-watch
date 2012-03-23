package org.gitpod.broker

import akka.zeromq._
import akka.actor.{ActorLogging, Actor}
import org.gitpod.tick.{BrokerTickCount, TickCount}

class Broker extends Actor with ActorLogging {

  context.system.newSocket( SocketType.Sub, Listener( self ), Connect( "ipc://market.queue.ipc" ), Subscribe( "nyse" ) )

  private var ticksConsumed = 0L

  def receive = {

    case m: ZMQMessage =>

      ticksConsumed += 1

    case TickCount =>

      sender ! BrokerTickCount( self, ticksConsumed )

    case other => log.debug( "broker => " + other )
  }

}
