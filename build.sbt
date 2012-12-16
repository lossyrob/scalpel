

name := "scalameter"

organization := "com.github.axel22"

version := "0.2"

scalaVersion := "2.10.0-RC5"

resolvers ++= Seq("Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
                 "Scala Test" at "http://www.scala-tools.org/repo-reloases/",
                 "spray" at "http://repo.spray.io/")

libraryDependencies ++= List(
"org.scalatest" % "scalatest_2.10.0-RC5" % "2.0.M5-B1" % "test",
  "org.reflections" % "reflections" % "0.9.5",
  "jfree" % "jfreechart" % "1.0.12",
  "org.apache.commons" % "commons-math3" % "3.0",
  "org.scala-tools.testing" % "test-interface" % "0.5",
  "com.google.guava" % "guava" % "12.0.1",
  "com.google.code.gson" % "gson" % "2.2.1",
  "com.google.code.java-allocation-instrumenter" % "java-allocation-instrumenter" % "2.0",
  "com.google.code.findbugs" % "jsr305" % "1.3.9",
  "joda-time" % "joda-time" % "2.1",
  "junit" % "junit" % "4.5" % "test",
  "net.liftweb" % "lift-json_2.10.0-RC2" % "2.5-SNAPSHOT" from "http://n0d.es/jars/lift-json_2.10.0-RC2.jar",
  "com.thoughtworks.paranamer" % "paranamer" % "2.4.1"
)


publishMavenStyle := true

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

scalacOptions ++= Seq("-deprecation","-feature")

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <url>http://axel22.github.com/scalameter/</url>
  <licenses>
    <license>
      <name>BSD-style</name>
      <url>http://opensource.org/licenses/BSD-3-Clause</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:axel22/scalameter.git</url>
    <connection>scm:git:git@github.com:axel22/scalameter.git</connection>
  </scm>
  <developers>
    <developer>
      <id>axel22</id>
      <name>Aleksandar Prokopec</name>
      <url>http://axel22.github.com/</url>
    </developer>
  </developers>)


