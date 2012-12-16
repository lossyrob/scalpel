package scalpel.port

import com.google.caliper._
import com.google.caliper.ConsoleReport
import com.google.caliper.UserException.ExceptionFromUserCodeException
import com.google.caliper.util.InterleavedReader
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
     val args = Array[String]("--warmupMillis", "3000", 
                             "--runMillis", "1000", 
                             "--measurementType", "TIME", 
                             "--marker", "//ZxJ/", 
                             "scalpel.CaliperBenchmark")


    val arguments = Arguments.parse(args)

    val scenarioSelection = new ScenarioSelection(arguments)

    val result:Result = runOutOfProcess(arguments,scenarioSelection)

    val run = result.getRun()
    for(entry <- run.getMeasurements().entrySet()) {
      val scenario = entry.getKey
      val ms = entry.getValue().getMeasurementSet(MeasurementType.TIME)
      val d = ms.medianRaw()
      // for(x <- ms.getUnitNames.entrySet()) {
      //   println(s"${x.getKey()} = ${x.getValue()}")
      // }
      println(s"Caliper measure time: ${d/1000000} ms")
    }

//      new ConsoleReport(result.getRun(), arguments).displayResults()
  }

  def runOutOfProcess(arguments:Arguments, scenarioSelection:ScenarioSelection):Result = {
    val executedDate = new Date()
    val resultsBuilder:ImmutableMap.Builder[Scenario, ScenarioResult] = ImmutableMap.builder()

    try {
      val scenarios = scenarioSelection.select()

      var i = 0
      for (scenario <- scenarios) {
        i += 1
        beforeMeasurement(i, scenarios.size(), scenario)
        val scenarioResult:ScenarioResult = runScenario(arguments, scenarioSelection, scenario)
        afterMeasurement(arguments.getMeasureMemory(), scenarioResult)
        resultsBuilder.put(scenario, scenarioResult)
      }
      println()

      val environment:Environment = new EnvironmentGetter().getEnvironmentSnapshot()
      return new Result(
        new Run(resultsBuilder.build(), arguments.getSuiteClassName(), executedDate),
        environment)
    } catch {
      case e:Exception => 
        throw new ExceptionFromUserCodeException(e)
    }
  }

  def beforeMeasurement(index:Int, total:Int, scenario:Scenario) = {
    val percentDone:Double = index.toDouble / total
//    println("%2.0f%% %s".format(percentDone * 100, scenario))
  }

  def afterMeasurement(memoryMeasured:Boolean, scenarioResult:ScenarioResult) = {
    val memoryMeasurements = ""
    if (memoryMeasured) {
      val instanceMeasurementSet:MeasurementSet =
        scenarioResult.getMeasurementSet(MeasurementType.INSTANCE)
      val instanceUnit:String = ConsoleReport.UNIT_ORDERING.min(instanceMeasurementSet.getUnitNames().entrySet()).getKey()
      val memoryMeasurementSet:MeasurementSet = scenarioResult.getMeasurementSet(MeasurementType.MEMORY)
      val memoryUnit:String = ConsoleReport.UNIT_ORDERING.min(memoryMeasurementSet.getUnitNames().entrySet()).getKey()
      val memoryMeasurements = ", allocated %s%s for a total of %s%s".format(
                                         Math.round(instanceMeasurementSet.medianUnits()), instanceUnit,
                                         Math.round(memoryMeasurementSet.medianUnits()), memoryUnit)
    }

    val timeMeasurementSet:MeasurementSet = scenarioResult.getMeasurementSet(MeasurementType.TIME)
    val unit:String = ConsoleReport.UNIT_ORDERING.min(timeMeasurementSet.getUnitNames().entrySet()).getKey()
    // println(" %.2f %s; \u03C3=%.2f %s @ %d trials%s%n".format(timeMeasurementSet.medianUnits(),
    //                   unit, timeMeasurementSet.standardDeviationUnits(), unit,
    //                   timeMeasurementSet.getMeasurements().size(), memoryMeasurements))
  }

  def runScenario(arguments:Arguments, scenarioSelection:ScenarioSelection, scenario:Scenario):ScenarioResult = {
    val timeMeasurementResult:MeasurementResult = SetupRunner.measure(CaliperSetup(arguments, scenarioSelection, scenario, MeasurementType.TIME))

    var allocationMeasurements:MeasurementSet = null
    var allocationEventLog:String = null
    var memoryMeasurements:MeasurementSet = null
    var memoryEventLog:String = null

    if (arguments.getMeasureMemory()) {
      val allocationsMeasurementResult:MeasurementResult = SetupRunner.measure(CaliperSetup(arguments, scenarioSelection, scenario, MeasurementType.INSTANCE))
      allocationMeasurements = allocationsMeasurementResult.measurements
      allocationEventLog = allocationsMeasurementResult.eventLogs

      val memoryMeasurementResult:MeasurementResult = SetupRunner.measure(CaliperSetup(arguments, scenarioSelection, scenario, MeasurementType.MEMORY))
      memoryMeasurements = memoryMeasurementResult.measurements
      memoryEventLog = memoryMeasurementResult.eventLogs
    }

    return new ScenarioResult(timeMeasurementResult.measurements,
        timeMeasurementResult.eventLogs,
        allocationMeasurements, allocationEventLog,
        memoryMeasurements, memoryEventLog)
  }
}
