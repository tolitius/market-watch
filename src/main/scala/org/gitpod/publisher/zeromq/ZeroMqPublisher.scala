package org.gitpod.publisher.zeromq

import akka.actor.Actor
import org.gitpod.feed.SendTick
import org.zeromq.ZMQ

/**
 * TODO: Document Me
 *
 * @author anatoly.polinsky
 */

class ZeroMqPublisher( destination: String ) extends Actor {

  val zsocket  = ZMQ.context( 1 ).socket( ZMQ.PUB )

  zsocket.bind( "ipc://market.queue.ipc" )

  def receive = {

    case SendTick( tick: Array[Byte] ) =>

      zsocket.send( tick, 0 )
  }
}
