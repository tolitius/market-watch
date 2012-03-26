package org.gitpod.publisher.akka

import org.gitpod.feed.SendTick
import akka.actor.{ActorRef, Actor}

/**
 * TODO: Document Me
 *
 * @author anatoly.polinsky
 */

class AkkaPublisher( destination: ActorRef ) extends Actor {

  def receive = {

    case SendTick( tick: Array[Byte] ) =>

      destination ! tick
  }
}
