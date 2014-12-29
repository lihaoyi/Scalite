package scalite

import sbt.Keys._
import sbt._
object SbtPlugin extends sbt.AutoPlugin{
  val scalatexVersion = "0.1.0"
  val scaliteDirectory = taskKey[sbt.File]("Clone stuff from github")
  val mySeq = Seq(
    scaliteDirectory := sourceDirectory.value / "scalite",
    managedSources ++= {
      val inputDir = scaliteDirectory.value
      val outputDir = sourceManaged.value / "scalite"
      val inputFiles = (inputDir ** "*.scalite").get

      val outputFiles = for(inFile <- inputFiles) yield {
        val outFile = new sbt.File(
          outputDir.getAbsolutePath + inFile.getAbsolutePath.drop(inputDir.getAbsolutePath.length)
        )
        IO.write(
          outFile,
          IO.readLines(inFile).map("//"+_).mkString("\n")
        )
        outFile
      }
      outputFiles
    }
  )
  override val projectSettings = inConfig(Test)(mySeq) ++ inConfig(Compile)(mySeq) ++ Seq(
    watchSources ++= ((scaliteDirectory in Compile).value ** "*.scalite").get
  )
}
