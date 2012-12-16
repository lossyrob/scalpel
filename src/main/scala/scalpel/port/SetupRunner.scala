package scalpel.port

import com.google.caliper.Arguments
import com.google.caliper.ScenarioSelection
import com.google.caliper.Scenario
import com.google.caliper.MeasurementSet
import com.google.caliper.MeasurementType
import com.google.caliper.InProcessRunner

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

  def measure(setup:CaliperSetup):MeasurementResult = {
    val arguments = setup.arguments
    val scenario = setup.scenario
    val scenarioSelection = setup.scenarioSelection
    val eventLog = new StringBuilder()

    var measurementSet:MeasurementSet = null

    val reader = new InterleavedReader {
      def log(s:String) = eventLog.append(s)
      def handleJson(s:String) = {
        measurementSet = parseJson(s)
      }
    }

    val caliperArgs = mutable.ListBuffer("--warmupMillis",arguments.getWarmupMillis().toLong.toString,
                                         "--runMillis",arguments.getRunMillis().toLong.toString,
                                         "--measurementType",MeasurementType.TIME.toString(),
                                         "--marker",reader.marker)

    for(entry <- scenario.getVariables(scenarioSelection.getUserParameterNames()).entrySet()) {
      caliperArgs.append("-D" + entry.getKey() + "=" + entry.getValue())
    }

    caliperArgs.add(arguments.getSuiteClassName())

    val (cmd,rc) = try {
      JavaRunner.run(defaultClasspath,
                     classOf[InProcessRunner].getName,
                     caliperArgs)(reader.readLine)
    } catch {
      case e:java.io.IOException => throw new RuntimeException("failed to start subprocess", e)
    }

    if (measurementSet == null) {
      val message = s"Failed to execute ${cmd}" 
      System.err.println("  " + message)
      System.err.println(eventLog.toString())
      throw new ConfigurationException(message)
    }

    return new MeasurementResult(measurementSet, eventLog.toString());
  }
}
