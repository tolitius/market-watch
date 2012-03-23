package org.gitpod.tick

import akka.actor.ActorRef

sealed trait TickCounter
case object TickCount extends TickCounter
case class FeedTickCount( feedRef: ActorRef, ticks: Long ) extends TickCounter
case class BrokerTickCount( brokerRef: ActorRef, ticks: Long ) extends TickCounter

