package org.gitpod.tick

import akka.util.Duration

/**
 * A luxury data structure (does not save on memory) that keeps a [current] and a [previous] number of "things" happened
 * along with a given time [interval] between the two.
 *
 * @author anatoly.polinsky
 */

class TickRate( val current: Long,  val previous: Long, val interval: Duration ) {

  def ratePerSecond = {
    ( ( current - previous ) / interval.toSeconds )
  }
}