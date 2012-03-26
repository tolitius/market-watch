package org.gitpod.subscriber.akka

import akka.actor.{ActorLogging, Actor}
import org.gitpod.tick.{TickCount, BrokerTickCount}

/**
 * TODO: Document Me
 *
 * @author anatoly.polinsky
 */

class AkkaSubscriber( feed: String ) extends Actor with ActorLogging {

  private var ticksConsumed = 0L

  def receive = {

    case tick: Array[Byte] =>

      ticksConsumed += 1

    case TickCount =>

      sender ! BrokerTickCount( self, ticksConsumed )

    case other => log.debug( "broker => " + other )
  }
}
