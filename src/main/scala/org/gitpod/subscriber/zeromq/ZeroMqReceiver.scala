package org.gitpod.subscriber.zeromq

import org.zeromq.ZMQ.Socket
import akka.actor.{ActorLogging, Actor}

/**
 * TODO: Document Me
 *
 * @author anatoly.polinsky
 */

class ZeroMqReceiver( val subscriber: Socket,
                      var running: Boolean,
                      var ticksConsumed: Long ) extends Actor with ActorLogging {

    def receive = {

      case ReceiveIt =>

        while( running ) {
          subscriber.recv( 0 ).asInstanceOf[Array[Byte]]
          ticksConsumed += 1
        }

        log.info( "stopping..." )
        context.stop( self )
    }
  }
