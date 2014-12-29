lazy val api = project.settings(
  organization  := "com.lihaoyi",
  name := "scalite",
  version       := scalite.SbtPlugin.scaliteVersion,
  scalaVersion  := "2.11.4",
  libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value,
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
  scalaVersion  := "2.11.4",
  autoCompilerPlugins := true,
  addCompilerPlugin("com.lihaoyi" %% "scalite" % scalite.SbtPlugin.scaliteVersion),
  libraryDependencies += "com.lihaoyi" %% "utest" % "0.2.4",
  testFrameworks += new TestFramework("utest.runner.JvmFramework"),
  (compile in Compile) <<= (compile in Compile).dependsOn(publishLocal in api)
)

