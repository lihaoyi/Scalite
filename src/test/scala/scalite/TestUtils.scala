package scalite

import reflect._
import tools.nsc.io._
import tools.nsc.{Global, Settings}
import tools.nsc.reporters.ConsoleReporter
import scala.reflect.io.{AbstractFile, VirtualDirectory}

object TestUtils {


  def getFilePaths(src: String): List[String] = {
    val f = new java.io.File(src)
    if (f.isDirectory) f.list.toList.flatMap(x => getFilePaths(src + "/" + x))
    else List(src)
  }

  def make(name: String) = {

    val sources = List("src/test/resources/" + name.replace('.', '/') + ".scala")

    val vd = new VirtualDirectory("(memory)", None)

    lazy val cl = new ClassLoader(this.getClass.getClassLoader){
      override protected def loadClass(name: String, resolve: Boolean): Class[_] = {
        try{
          if (!name.startsWith("scalite")) throw new ClassNotFoundException()
          findClass(name)

        } catch { case e: Throwable =>
          try{
            getParent.loadClass(name)
          }catch{case e: Throwable =>
            e.printStackTrace()
            null
          }
        }
      }

      override protected def findClass(name: String): Class[_] = {
        Option(findLoadedClass(name)) getOrElse {

          val pathParts :+ className = name.split('.').toSeq

          val finalDir = pathParts.foldLeft(vd: AbstractFile)((dir, part) => dir.lookupName(part, true))

          finalDir.lookupName(className + ".class", false) match {
            case null   => throw new ClassNotFoundException()
            case file   =>
              val bytes = file.toByteArray
              this.defineClass(name, bytes, 0, bytes.length)
          }
        }
      }
    }

    lazy val settings = {
      val s =  new Settings
      //s.Xprint.value = List("all")
      val classPath = getFilePaths("/Runtimes/scala-2.11.0-M4/lib") :+ "target/scala-2.10/classes"

      classPath.map(new java.io.File(_).getAbsolutePath).foreach{ f =>
        s.classpath.append(f)
        s.bootclasspath.append(f)
      }
      s.outputDirs.setSingleOutput(vd)
      s
    }

    lazy val compiler = new Globalite(settings, new ConsoleReporter(settings))

    val run = new compiler.Run()
    run.compile(sources)

    if (vd.toList.isEmpty) throw CompilationException

    val cls = cl.loadClass(name)

  }

  object CompilationException extends Exception
}