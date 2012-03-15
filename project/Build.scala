import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

  val appName         = "Phenonet"
  val appVersion      = "2.0"

  val appDependencies = Seq(
    // Add your project dependencies here,
    "org.apache.commons" % "commons-math" % "2.2",
    "com.codahale" %% "jerkson" % "0.5.0" ,
    "org.specs2" %% "specs2" % "1.7.1",
    "com.mongodb.casbah" %% "casbah" % "2.1.5-1",
    "redis.clients" % "jedis" % "2.0.0",
  	"org.squeryl" %% "squeryl" % "0.9.5-RC1",
	  "postgresql" % "postgresql" % "8.4-701.jdbc4",
	  "me.prettyprint" % "hector-core" % "1.0-3",
    "me.prettyprint" % "hector" % "1.0-3",
    "joda-time" % "joda-time" % "2.1"
  )

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
    // Add your own project settings here
  )

}
