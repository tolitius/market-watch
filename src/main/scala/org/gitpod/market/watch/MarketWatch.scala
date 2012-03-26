package org.gitpod.market.watch

import akka.util.duration._
import akka.actor.{Cancellable, ActorRef, Actor, ActorLogging}
import collection.mutable
import collection.immutable
import org.gitpod.tick.{BrokerTickCount, FeedTickCount, TickCount, TickRate}
import akka.util.Duration

case class RegisterBroker( brokerId: String )
case class RegisterFeed( feedId: String )

case object GatherStats
case object ShareStats

case object AlreadyRegistered
case object NotRegistered

/**
 * TODO: Document Me
 *
 * @author anatoly.polinsky
 */

class MarketWatch( val shareStats: ( immutable.Map[ActorRef, List[TickRate]],
                                     immutable.Map[ActorRef, List[TickRate]] ) => Any )
    extends Actor with ActorLogging {

  val STATS_GATHERING_INTERVAL = 1 second
  val STATS_SHARING_INTERVAL = 5 second

  private val ACTOR_BASE_PATH = ".." // MarketWatch would be a sibling to brokers and feeds (at least for now)

  private val brokers = mutable.Map[ActorRef, List[TickRate]]()
  private val feeds = mutable.Map[ActorRef, List[TickRate]]()

  private var statistician: Cancellable = _
  private var statsPublisher: Cancellable = _

  def receive = {

    case GatherStats =>

      List( feeds.keys, brokers.keys ).flatten.map( countable => { countable ! TickCount } )

    case ShareStats =>

      shareStats( feeds.toMap, brokers.toMap )

    case FeedTickCount( feedRef: ActorRef, ticks: Long ) =>

      if ( feeds contains feedRef )
        feeds( feedRef ) = recordTickCount( ticks, feeds( feedRef ) )
      else
        sender ! NotRegistered

    case BrokerTickCount( brokerRef: ActorRef, ticks: Long ) =>

      if ( brokers contains brokerRef )
        brokers( brokerRef ) = recordTickCount( ticks, brokers( brokerRef ) )
      else
        sender ! NotRegistered

    case RegisterFeed( feedId: String ) =>

      val feedRef = lookupActorRef( ACTOR_BASE_PATH, feedId )
      if ( ! ( feeds contains feedRef ) ) {
        feeds( feedRef ) = List[TickRate]()
        log.info( "registered a feed\t [" + feedId + "]" )
      }
      else
        sender ! AlreadyRegistered

    case RegisterBroker( brokerId: String ) =>

      val brokerRef = lookupActorRef( ACTOR_BASE_PATH, brokerId )
      if ( ! ( brokers contains brokerRef ) ) {
        brokers( brokerRef ) = List[TickRate]()
        log.info( "registered a broker\t [" + brokerId + "]" )
      }
      else
        sender ! AlreadyRegistered
  }

  override def preStart() {
    statistician = context.system.scheduler.schedule( Duration.Zero, STATS_GATHERING_INTERVAL, self, GatherStats )
    statsPublisher = context.system.scheduler.schedule( 5 seconds, STATS_SHARING_INTERVAL, self, ShareStats )
  }

  override def postStop() {
    if ( Option( statistician ).isDefined ) statistician.cancel()
    if ( Option( statsPublisher ).isDefined ) statsPublisher.cancel()
  }

  /**
   *  adding a previous count to a current new TickRate's [[org.gitpod.tick.TickRate.previous]]
   *  and adding a new TickRate to ratable "count history"
   */
  private def recordTickCount( count: Long, ratable: List[TickRate] ) = {

    if ( ! ratable.isEmpty )
      //log.info( "current: " + count + ", previous: " + ratable.head.current + ", rate: " + new TickRate( count, ratable.head.current, STATS_GATHERING_INTERVAL ).ratePerSecond + " per second" )
      ratable .:: ( new TickRate( count, ratable.head.current, STATS_GATHERING_INTERVAL ) )
    else
      List ( new TickRate( count, 0, STATS_GATHERING_INTERVAL ) )
  }

  private def lookupActorRef( path: String, name: String ) = {
    context.actorFor( path + "/" + name )
  }
}