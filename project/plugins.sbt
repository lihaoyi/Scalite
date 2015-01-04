unmanagedSources in Compile ++= {
  val root = baseDirectory.value.getParentFile
  (root / "scaliteSbtPlugin" ** "*.scala").get
}

addSbtPlugin("org.scala-lang.modules.scalajs" % "scalajs-sbt-plugin" % "0.5.5")