package org.gitpod.subscriber.zeromq

import org.gitpod.tick.{BrokerTickCount, TickCount}
import org.zeromq.ZMQ
import akka.util.Duration
import akka.actor.{Props, ActorLogging, Actor}

/**
 * TODO: Document Me
 *
 * @author anatoly.polinsky
 */

case object ReceiveIt

class ZeroMqSubscriber( feed: String ) extends Actor with ActorLogging {

  val subscriber = ZMQ.context( 1 ).socket( ZMQ.SUB )

  subscriber.connect( "ipc://market.queue.ipc" )
  // subscriber.subscribe( feed.getBytes )
  subscriber.subscribe( "".getBytes ) // subscribing to ALL

  private var ticksConsumed = 0L
  private var receiving = true

  // "subscriber.recv" is blocking => need a poppet actor
  val receiver = context.actorOf( Props ( new Actor {
    def receive = {
      case ReceiveIt =>
        while( receiving ) {
          subscriber.recv( 0 ).asInstanceOf[Array[Byte]]
          ticksConsumed += 1
        }
        context.stop( self )
    }
  } ) )
  
  def receive = {

    case ReceiveIt =>

      receiver ! ReceiveIt

    case TickCount =>

      sender ! BrokerTickCount( self, ticksConsumed )

    case other => log.debug( "broker => " + other )
  }

  override def preStart {
    context.system.scheduler.scheduleOnce( Duration.Zero, self, ReceiveIt )
  }

  override def postStop {
    receiving = false
    subscriber.close()
  }
}
