organization := "au.csiro.ict"

name := "SensorDB"

version := "1.0"

scalaVersion := "2.9.1"

seq(webSettings :_*)

libraryDependencies ++= Seq(
  "org.scalatra" %% "scalatra" % "2.0.4",
  "org.scalatra" %% "scalatra-scalate" % "2.0.4",
  "org.scalatra" %% "scalatra-specs2" % "2.0.4" % "test",
  "ch.qos.logback" % "logback-classic" % "1.0.0" % "runtime",
  "org.eclipse.jetty" % "jetty-webapp" % "7.6.0.v20120127" % "container",
  "javax.servlet" % "servlet-api" % "2.5" % "provided" ,
  "org.apache.commons" % "commons-math" % "2.2",
  "com.mongodb.casbah" %% "casbah" % "2.1.5-1",
  "redis.clients" % "jedis" % "2.0.0",
  "me.prettyprint" % "hector-core" % "1.0-3",
  "me.prettyprint" % "hector" % "1.0-3",
  "joda-time" % "joda-time" % "2.1",
  "org.joda" % "joda-convert" % "1.2" % "provided",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "commons-validator"% "commons-validator" % "1.4.0",
  "com.novus" %% "salat-core" % "0.0.8-SNAPSHOT"
)

resolvers += "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

resolvers += "repo.novus rels" at "http://repo.novus.com/releases/"