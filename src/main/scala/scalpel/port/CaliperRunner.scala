package scalpel.port

import com.google.caliper.ConsoleReport
import com.google.caliper.UserException.ExceptionFromUserCodeException
import com.google.caliper.MeasurementSet
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableList
import com.google.common.base.Splitter
import com.google.common.base.Joiner
import com.google.common.io.Closeables;
import com.google.gson.JsonObject

import scala.collection.JavaConversions._
import java.util.Date
import java.util.regex.Pattern
import java.io.InputStreamReader
import java.io.File

case class MeasurementResult(measurements:MeasurementSet, eventLogs:String) 

object CaliperRunner {
  def ARGUMENT_SPLITTER:Splitter = Splitter.on(Pattern.compile("\\s+")).omitEmptyStrings();

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

  def run() = {
    val result = runOutOfProcess(CaliperSetup.Default)

    val run = result.run
    for((benchmark,result) <- run.results) {
      val ms = result.measurementSet
      val d = ms.medianRaw()
      // for(x <- ms.getUnitNames.entrySet()) {
      //   println(s"${x.getKey()} = ${x.getValue()}")
      // }
      println(s"Caliper measure time: ${d/1000000} ms")
    }

//      new ConsoleReport(result.getRun(), arguments).displayResults()
  }

  def runOutOfProcess(setup:CaliperSetup):CaliperResult = {
    val executedDate = new Date()
    var results = Map[String,BenchmarkResult]()

    try {
      var i = 0
      for (benchmarkName <- setup.benchmarkNames) {
        i += 1
        beforeMeasurement(i, setup.benchmarkNames.length, benchmarkName)
        val scenarioResult:BenchmarkResult = runScenario(setup,benchmarkName)
        afterMeasurement(scenarioResult)
        results = results + ((benchmarkName, scenarioResult))
      }
      println()

      val environment = Environment.getEnvironmentSnapshot()
      return new CaliperResult(
        new Run(results, setup.suiteClassName, executedDate), 
        environment)
    } catch {
      case e:Exception => 
        throw new ExceptionFromUserCodeException(e)
    }
  }

  def beforeMeasurement(index:Int, total:Int, benchmarkName:String) = {
    val percentDone:Double = (index.toDouble / total)*100
    print(s"${percentDone.toInt}% $benchmarkName - ")
  }

  def afterMeasurement(result:BenchmarkResult) = {
    val memoryMeasurements = ""

    val timeMeasurementSet:MeasurementSet = result.measurementSet
    val unit:String = ConsoleReport.UNIT_ORDERING.min(timeMeasurementSet.getUnitNames().entrySet()).getKey()
    println(s"  done.")
    // println(" %.2f %s; \u03C3=%.2f %s @ %d trials%s%n".format(timeMeasurementSet.medianUnits(),
    //                   unit, timeMeasurementSet.standardDeviationUnits(), unit,
    //                   timeMeasurementSet.getMeasurements().size(), memoryMeasurements))
  }

  def runScenario(setup:CaliperSetup,benchmarkName:String):BenchmarkResult = {
    val timeMeasurementResult:MeasurementResult = SetupRunner.measure(setup,benchmarkName)

    return new BenchmarkResult(timeMeasurementResult.measurements,
                               timeMeasurementResult.eventLogs)
  }
}
