unmanagedSources in Compile ++= {
  val root = baseDirectory.value.getParentFile
  (root / "scaliteSbtPlugin" ** "*.scala").get
}
