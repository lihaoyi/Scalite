val sharedSettings = Seq(
  organization  := "com.lihaoyi",
  version := "0.1.0",
  publishTo := Some("releases"  at "https://oss.sonatype.org/service/local/staging/deploy/maven2"),
  pomExtra :=
    <url>https://github.com/lihaoyi/Scalite</url>
      <licenses>
        <license>
          <name>MIT license</name>
          <url>http://www.opensource.org/licenses/mit-license.php</url>
        </license>
      </licenses>
      <scm>
        <url>git://github.com/lihaoyi/Scalite.git</url>
        <connection>scm:git://github.com/lihaoyi/Scalite.git</connection>
      </scm>
      <developers>
        <developer>
          <id>lihaoyi</id>
          <name>Li Haoyi</name>
          <url>https://github.com/lihaoyi</url>
        </developer>
      </developers>
)
lazy val api = project.settings(sharedSettings:_*).settings(
  name := "scalite",
  version       := scalite.SbtPlugin.scaliteVersion,
  scalaVersion  := "2.11.4",
  libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value,
  (resources in Test) ++=  (managedClasspath in Compile).value.map(_.data)
)

lazy val scaliteSbtPlugin = project.settings(sharedSettings:_*)
  .settings(
    name := "scalite-sbt-plugin",
    scalaVersion := "2.10.4",
    sbtPlugin := true
  )

lazy val example = project.settings(sharedSettings ++ scalite.SbtPlugin.projectSettings:_*)
                              .settings(
  scalaVersion  := "2.11.4",
  libraryDependencies += "com.lihaoyi" %% "utest" % "0.2.4",
  testFrameworks += new TestFramework("utest.runner.JvmFramework"),
  (compile in Compile) <<= (compile in Compile).dependsOn(publishLocal in api),
  publish := ()
)

publish := ()

