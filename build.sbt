lazy val api = project.settings(
  organization  := "com.lihaoyi",
  name := "scalite",
  version       := "0.1.0",
  scalaVersion  := "2.11.4",
  libraryDependencies ++= Seq(
    "com.lihaoyi" %% "utest" % "0.1.4",
    "com.lihaoyi" %% "utest-runner" % "0.1.4",
    "org.scala-lang" % "scala-compiler" % scalaVersion.value
  ),
  testFrameworks += new TestFramework("utest.runner.JvmFramework"),
  (resources in Test) ++=  (managedClasspath in Compile).value.map(_.data)
)

lazy val scaliteSbtPlugin = project
  .settings(
    organization  := "com.lihaoyi",
    name := "scalite-sbt-plugin",
    scalaVersion := "2.10.4",
    sbtPlugin := true
  )

lazy val example = project.settings(scalite.SbtPlugin.projectSettings:_*)
                              .settings(
  autoCompilerPlugins := true,
  scalaVersion  := "2.11.4",
  libraryDependencies += "com.lihaoyi" %% "utest" % "0.2.4",
  testFrameworks += new TestFramework("utest.runner.JvmFramework"),
  (compile in Compile) <<= (compile in Compile).dependsOn(publishLocal in api),
  addCompilerPlugin("scalite" %% "scalite" % "0.1.0")
)

