#What is Zakka?
ZeroMQ + AKKA playground, where several different approaches are compared:

* Akka's 2.0 ZeroMQ module => JNA wrapper around ZeroMQ
* JZMQ => JNI wrapper around ZeroMQ, a.k.a ZeroMQ Java Bindings
* Akka 2.0 built in unbound mailbox => 'java.util.concurrent.ConcurrentLinkedQueue'

All the above (for now) are "inproc" e.g. local in-process (inter-thread) communication transport

#What is Inside?
The use case is simple. There are:

* "Market Feed"s that send (in this example) 200 byte orders
* "Broker"s that can subsribe to any feed and consume the orders
* A single "Maket Watch" that feeds and brokers can be registered with. It monitors and reports the rate.

It is a simplified versioned that does not imply the final numbers, and not geared towards "how fast", but rather "how different" the approaches are.

#How to Run It?

It is built and runs with SBT by specifying a desired profile that starts a Market Simulation on top of:

* "-Dprofile=zakka" => Akka 2.0 ZeroMQ module
* "-Dprofile=zmq" => ZeroMQ JNI API (JZMQ)
* "-Dprofile=akka" => Akka's built in (default) mailboxes

#Show Me The Money

###Akka ZeroMQ

Orders are sent and received as 'akka.zeromq.ZMQMessage's e.g. 

```scala
zsocket ! ZMQMessage( Seq( Frame( destination ), Frame( tick ) ) )
```

```bash
$ SBT_OPTS="-Djava.library.path=/usr/local/lib -Xms2G -Xmx3G" sbt -Dprofile=zakka clean run                           (master ✔) 

[freeMarket-akka.actor.default-dispatcher-2] [akka://freeMarket/user/marketWatch] registered a feed     [nyse.feed]
[freeMarket-akka.actor.default-dispatcher-2] [akka://freeMarket/user/marketWatch] registered a broker   [vip.broker]

     {nyse.feed}     => [ current rate:   914230.0 ticks/s,   average rate:    777879.0 ticks/s  ]
    {vip.broker}     => [ current rate:     1576.0 ticks/s,   average rate:      1431.0 ticks/s  ]
```


###ZeroMQ Java Bindings (JZMQ)

Orders are sent and received as Array[Byte]s

```bash
$ SBT_OPTS="-Djava.library.path=/usr/local/lib -Xms2G -Xmx3G" sbt -Dprofile=zmq clean run                             (master ✔) 

[freeMarket-akka.actor.default-dispatcher-5] [akka://freeMarket/user/marketWatch] registered a feed	     [nyse.feed]
[freeMarket-akka.actor.default-dispatcher-5] [akka://freeMarket/user/marketWatch] registered a broker	 [vip.broker]

     {nyse.feed}	 => [ current rate:  1094854.0 ticks/s,   average rate:   1034253.0 ticks/s  ]
    {vip.broker}	 => [ current rate:   296233.0 ticks/s,   average rate:    272157.0 ticks/s  ]
```

###Akka 2.0 Build In Mailbox

Orders are sent and received as Array[Byte]s

```bash
$ SBT_OPTS="-Djava.library.path=/usr/local/lib -Xms2G -Xmx3G" sbt -Dprofile=akka clean run                            (master ✔)

[freeMarket-akka.actor.default-dispatcher-1] [akka://freeMarket/user/marketWatch] registered a feed	     [nyse.feed]
[freeMarket-akka.actor.default-dispatcher-1] [akka://freeMarket/user/marketWatch] registered a broker	 [vip.broker]

     {nyse.feed}	 => [ current rate:  2074426.0 ticks/s,   average rate:   1995846.0 ticks/s  ]
    {vip.broker}	 => [ current rate:  2074434.0 ticks/s,   average rate:   1995846.0 ticks/s  ]
```

#Gotchas

1. In order to run a JZMQ example, ZeroMQ [Java Bindings](https://github.com/zeromq/jzmq) need to be installed
2. Akka 2.0 ZeroMQ module "hiccups" on shutdown: [http://www.assembla.com/spaces/akka/tickets/1949](http://www.assembla.com/spaces/akka/tickets/1949) 
