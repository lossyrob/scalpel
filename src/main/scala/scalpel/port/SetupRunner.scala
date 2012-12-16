package scalpel.port

import com.google.caliper.Arguments
import com.google.caliper.ScenarioSelection
import com.google.caliper.Scenario
import com.google.caliper.MeasurementSet
import com.google.caliper.MeasurementType
import com.google.caliper/*.InProcessRunner*/

import java.io.File
import java.io.IOException

import scala.collection.JavaConversions._
import scala.collection.mutable

object SetupRunner {
  def split(args:String) = args.split("\\s+") 
                               .map { x => x.replace(" ","").replace("\t","") }
                               .filter { x => x != "" } 
                               .toSeq

  def defaultClasspath = this.getClass.getClassLoader match {
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

  def parseJson(s:String):MeasurementSet = {
    println(s)
    JsonConversion.getMeasurementSet(s)
  }

  def measure(setup:CaliperSetup,benchmarkName:String):MeasurementResult = {
    val eventLog = new StringBuilder()

    var measurementSet:MeasurementSet = null

    val reader = new InterleavedReader {
      def log(s:String) = eventLog.append(s+"\n")
      def handleJson(s:String) = {
        measurementSet = parseJson(s)
      }
    }

    var caliperArgs = Seq("--warmupMillis",setup.warmupTime.toString,
                          "--runMillis",setup.runTime.toLong.toString,
                          "--measurementType",MeasurementType.TIME.toString(),
                          "--marker",reader.marker,
                          "-Dbenchmarks=$benchmarkName",
                          setup.suiteClassName)

    val (cmd,rc) = try {
      JavaRunner.run(defaultClasspath,
                     "scalpel.port.InProcessRunner", // TODO: Use reflection to get the name
                     caliperArgs)(reader.readLine)
    } catch {
      case e:java.io.IOException => throw new RuntimeException("failed to start subprocess", e)
    }

    println(s"\n${eventLog.toString}")
    
    if (measurementSet == null) {
      val message = s"Failed to execute ${cmd}" 
      System.err.println("  " + message)
      System.err.println(eventLog.toString())
      throw new ConfigurationException(message)
    }

    return new MeasurementResult(measurementSet, eventLog.toString());
  }
}
