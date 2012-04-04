package org.gitpod.market.watch

import org.gitpod.tick.TickRate
import akka.actor.ActorRef

/**
 * TODO: Document Me
 *
 * @author anatoly.polinsky
 */

/* to start simple, assume the rate is per second. TODO: replace assumption with [[akka.util.Duration]] */

class reportStats( val displayStats: ( String,  Map[ActorRef, Rate] ) => Any )

  extends ( ( Map[ActorRef, List[TickRate]],
              Map[ActorRef, List[TickRate]]  ) => Unit ) {

  def apply( feedStats: Map[ActorRef, List[TickRate]],
             brokerStats: Map[ActorRef, List[TickRate]] ) {

    displayStats ( "feeds", ratify( feedStats ) )
    displayStats ( "brokers", ratify( brokerStats ) )
  }
}

object ratify extends ( ( Map[ActorRef,  List[TickRate]] ) => Map[ActorRef, Rate] ) {

  def apply( ratable: Map[ActorRef, List[TickRate]] ) = {

    ratable.mapValues( tickRates => sampleRates( tickRates ) )
  }
}

object sampleRates extends ( ( List[TickRate] ) => Rate ) {

  def apply( tickRates: List[TickRate] ) = {

    if ( ! tickRates.isEmpty ) {

    val currentRate = tickRates.head.ratePerSecond
    val averageRate = tickRates.map( _.ratePerSecond ).sum / tickRates.size
    
    new Rate(  currentRate, averageRate )

    } else
      new Rate( 0, 0 )
  }
}

class statsToString extends ( ( String, Map[ActorRef, Rate] ) => Any ) {

  val log = org.slf4j.LoggerFactory.getLogger( this.getClass )

  def apply( it: String, stats: Map[ActorRef, Rate] ) {

    for ( ( id, rate ) <- stats ) { log.info( "%20s".format( "{"+ id.path.name + "}" ) + "\t => " + rate ) }
  }
}

class Rate( currentRate: Double, averageRate: Double ) {

  override def toString = {

    "[ current rate: " + "%20s".format( currentRate + " ticks/s, " ) +
      "  average rate: " + "%20s".format( averageRate + " ticks/s " ) + " ]"
  }
}

