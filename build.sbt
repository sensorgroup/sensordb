organization := "au.csiro.ict"

name := "SensorDB"

version := "1.0"

parallelExecution in (Test) := false

//testOptions in Test += Tests.Argument("sequential")

scalaVersion := "2.9.1"

seq(webSettings :_*)

port in container.Configuration := 9001

libraryDependencies ++= Seq(
  "org.scalatra" %% "scalatra" % "2.0.4",
  "org.scalatra" %% "scalatra-scalate" % "2.0.4",
  "org.scalatra" %% "scalatra-specs2" % "2.0.4" % "test",
  "org.scalatra" %% "scalatra-scalatest" % "2.0.4" % "test",
  "ch.qos.logback" % "logback-classic" % "1.0.0" % "runtime",
  "org.scalatra" %% "scalatra-fileupload" % "2.0.4",
  "org.eclipse.jetty" % "jetty-webapp" % "7.6.0.v20120127" % "container",
  "javax.servlet" % "servlet-api" % "2.5" % "provided" ,
  "org.apache.commons" % "commons-math3" % "3.0",
  "org.mongodb" %% "casbah" % "2.4.0",
  "joda-time" % "joda-time" % "2.1",
  "org.joda" % "joda-convert" % "1.2" % "provided",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "commons-validator"% "commons-validator" % "1.4.0",
  "com.codahale" %% "jerkson" % "0.5.0",
  "org.clapper" %% "grizzled-slf4j" % "0.6.9",
  "net.debasishg" %% "redisclient" % "2.5",
  "com.typesafe.akka" % "akka-actor" % "2.0.1",
  "com.typesafe.akka" % "akka-remote" % "2.0.1",
  "com.typesafe.akka" % "akka-kernel" % "2.0.1",
  "com.typesafe.akka" % "akka-testkit" % "2.0.1",
  "com.typesafe.akka" % "akka-redis-mailbox" % "2.0.1",
  "org.apache.commons" % "commons-lang3" % "3.1",
  "org.jsoup" % "jsoup" % "1.6.2",
  "com.typesafe" % "config" % "0.4.1",
  "redis.clients" % "jedis" % "2.1.0",
  "org.apache.hadoop" % "hadoop-core" % "1.0.3",
  "org.apache.hbase"  % "hbase"  % "0.94.0" exclude("org.slf4j", "slf4j-log4j12")
)

resolvers ++= Seq("Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
    "repo.novus rels" at "http://repo.novus.com/releases/"  ,
    "codehale for jerkson" at "http://repo.codahale.com",
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
    )
