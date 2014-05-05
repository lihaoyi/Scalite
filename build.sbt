organization  := "scalite"

name := "scalite"

version       := "0.1"

scalaVersion  := "2.11.0"

libraryDependencies ++= Seq(
  "com.lihaoyi" %% "utest" % "0.1.4",
  "com.lihaoyi" %% "utest-runner" % "0.1.4",
  "org.scala-lang" % "scala-compiler" % scalaVersion.value
)

testFrameworks += new TestFramework("utest.runner.JvmFramework")

(resources in Test) ++=  (managedClasspath in Compile).value.map(_.data)


