organization  := "scalite"

name := "scalite"

version       := "0.1"

scalaVersion  := "2.11.0-M4"

libraryDependencies ++= Seq(
  "org.scalatest" % "scalatest_2.11.0-M4" % "2.0.M6-SNAP35",
  "org.scala-lang" % "scala-compiler" % "2.11.0-M4"
)

unmanagedSourceDirectories in Test <+= baseDirectory(_ / "src" / "test" / "resources")


