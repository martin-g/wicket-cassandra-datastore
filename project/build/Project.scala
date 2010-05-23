import sbt._

class Project(info: ProjectInfo) extends DefaultProject(info)
{
  val embeddedrepo = "embedded repo" at (info.projectPath / "embedded-repo").asURL.toString
  val sunjdmk = "sunjdmk" at "http://wp5.e-taxonomy.eu/cdmlib/mavenrepo"
  val databinder = "DataBinder" at "http://databinder.net/repo"
  // val configgy = "Configgy" at "http://www.lag.net/repo"
  val codehaus = "Codehaus" at "http://repository.codehaus.org"
  val codehaus_snapshots = "Codehaus Snapshots" at "http://snapshots.repository.codehaus.org"
  val jboss = "jBoss" at "https://repository.jboss.org/nexus/content/groups/public/"
  val guiceyfruit = "GuiceyFruit" at "http://guiceyfruit.googlecode.com/svn/repo/releases/"
  val google = "Google" at "http://google-maven-repository.googlecode.com/svn/repository"
  val java_net = "java.net" at "http://download.java.net/maven/2"
  val scala_tools_snapshots = "scala-tools snapshots" at "http://scala-tools.org/repo-snapshots"
  val scala_tools_releases = "scala-tools releases" at "http://scala-tools.org/repo-releases"
	
  val mavenLocal = "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"

  
  
  
  val akka_persistence_common = "se.scalablesolutions.akka" %% "akka-persistence-common" % "0.9"
  val akka_persistence_cassandra = "se.scalablesolutions.akka" %% "akka-persistence-cassandra" % "0.9"
  val wicket = "org.apache.wicket" % "wicket" % "1.5-SNAPSHOT"
}

