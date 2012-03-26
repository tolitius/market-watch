package org.gitpod.publisher.akka.zeromq

import akka.actor.Actor
import org.gitpod.feed.SendTick
import akka.zeromq._

/**
 * TODO: Document Me
 *
 * @author anatoly.polinsky
 */

class AkkaZeroMqPublisher(destination: String) extends Actor {

  val zsocket = context.system.newSocket( SocketType.Pub, Bind( "ipc://market.queue.ipc" ) )

  def receive = {

    case SendTick( tick: Array[Byte] ) =>

      zsocket ! ZMQMessage( Seq( Frame( destination ), Frame( tick ) ) )
  }
}
