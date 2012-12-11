package scalpel

import org.scalameter._
import org.scalameter.utils._

object SMRunner {
  def computeClasspath = this.getClass.getClassLoader match {
    case urlcl: java.net.URLClassLoader => extractClasspath(urlcl)
    case cl => sys.props("java.class.path")
  }

  def extractClasspath(urlclassloader: java.net.URLClassLoader): String = {
    val fileResource = "file:(.*)".r
    val files = urlclassloader.getURLs.map(_.toString) collect {
      case fileResource(file) => file
    }
    files.mkString(":")
  }

  def run(perfTest:PerformanceTest) = {
    val args = Array[String]()
    val testcp = computeClasspath

    for {
//        _ <- dyn.log.using(complog)
//        _ <- dyn.events.using(tievents)
        _ <- dyn.initialContext.using(initialContext ++ Seq((Key.verbose,false)) ++ Main.Configuration.fromCommandLineArgs(args).context + (Key.classpath -> testcp))
      } {
        val datestart = new java.util.Date

        DSL.setupzipper.value = Tree.Zipper.root[Setup[_]]
        LoopBenchmark.testbody.value.apply()

        val setuptree = DSL.setupzipper.value.result
        val resulttree = perfTest.executor.run(setuptree.asInstanceOf[Tree[Setup[LoopBenchmark.SameType]]], perfTest.reporter, perfTest.persistor)
        
        // Print results
        for(curve <- resulttree) {
          for(measurement <- curve.measurements) {
            println(s"Scalameter time data: ${measurement.time}")
          }
        }

        val dateend = new java.util.Date
        val datedtree = resulttree.copy(context = resulttree.context + (Key.reports.startDate -> datestart) + (Key.reports.endDate -> dateend))

        perfTest.reporter.report(datedtree, LoopBenchmark.persistor)
    }
  }
}
