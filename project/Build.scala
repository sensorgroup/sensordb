import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

  val appName         = "SensorDB"
  val appVersion      = "1.0"

  val appDependencies = Seq(
    // Add your project dependencies here,
    "org.apache.commons" % "commons-math" % "2.2",
//    "com.codahale" %% "jerkson" % "0.5.0" ,  // not required, it is already included in play 2
    "org.specs2" %% "specs2" % "1.7.1",
    "com.mongodb.casbah" %% "casbah" % "2.1.5-1",
    "redis.clients" % "jedis" % "2.0.0",
  	"postgresql" % "postgresql" % "9.1-901.jdbc4",
	  "me.prettyprint" % "hector-core" % "1.0-3",
    "me.prettyprint" % "hector" % "1.0-3",
    "joda-time" % "joda-time" % "2.1",
    "org.mindrot" % "jbcrypt" % "0.3m",
    "commons-validator"% "commons-validator" % "1.4.0",
//    "org.scalaquery" % "scalaquery_2.9.0-1" % "0.9.5" ,
//    "org.squeryl" %% "squeryl" % "0.9.5-RC1",
//    "com.twitter"%% "querulous" % "2.7.0" ,
    "com.novus" %% "salat-core" % "0.0.8-SNAPSHOT"
  )

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
//    resolvers += "Twitter" at "http://maven.twttr.com"
    resolvers += "repo.novus rels" at "http://repo.novus.com/releases/"
  )

}
