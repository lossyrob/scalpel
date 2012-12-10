package scalpel

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

class MeasurementResult(measurements:MeasurementSet, eventLog:String) {
  val _measurements = measurements
  val _eventLog = eventLog
  def getMeasurements() = _measurements
  def getEventLog() = _eventLog
}

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

      new ConsoleReport(result.getRun(), arguments).displayResults()
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
    println("%2.0f%% %s".format(percentDone * 100, scenario))
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
    println(" %.2f %s; \u03C3=%.2f %s @ %d trials%s%n".format(timeMeasurementSet.medianUnits(),
                      unit, timeMeasurementSet.standardDeviationUnits(), unit,
                      timeMeasurementSet.getMeasurements().size(), memoryMeasurements))
  }

  def runScenario(arguments:Arguments, scenarioSelection:ScenarioSelection, scenario:Scenario):ScenarioResult = {
    println("Caliper start measure")
    val timeMeasurementResult:MeasurementResult = measure(arguments, scenarioSelection, scenario, MeasurementType.TIME)
    println("Caliper end measure")

    var allocationMeasurements:MeasurementSet = null
    var allocationEventLog:String = null
    var memoryMeasurements:MeasurementSet = null
    var memoryEventLog:String = null

    if (arguments.getMeasureMemory()) {
      val allocationsMeasurementResult:MeasurementResult = measure(arguments, scenarioSelection, scenario, MeasurementType.INSTANCE)
      allocationMeasurements = allocationsMeasurementResult.getMeasurements()
      allocationEventLog = allocationsMeasurementResult.getEventLog()

      val memoryMeasurementResult:MeasurementResult = measure(arguments, scenarioSelection, scenario, MeasurementType.MEMORY)
      memoryMeasurements = memoryMeasurementResult.getMeasurements()
      memoryEventLog = memoryMeasurementResult.getEventLog()
    }

    return new ScenarioResult(timeMeasurementResult.getMeasurements(),
        timeMeasurementResult.getEventLog(),
        allocationMeasurements, allocationEventLog,
        memoryMeasurements, memoryEventLog)
  }

/*
*
*
  *          CALL HERE! v      
  *                     v
  *                     v
  *                     v
  */                    

def measure(arguments:Arguments, scenarioSelection:ScenarioSelection,scenario:Scenario, measurementType:MeasurementType):MeasurementResult = {
    // this must be done before starting the forked process on certain VMs
    val processBuilder:ProcessBuilder = createProcess(arguments, scenarioSelection, scenario, measurementType).redirectErrorStream(true)
    var timeProcess:Process = null
    try {
      timeProcess = processBuilder.start()
    } catch {
      case e:java.io.IOException => throw new RuntimeException("failed to start subprocess", e)
    }

    // Create measurements based on results read from the process's input stream.
    var measurementSet:MeasurementSet = null
    val eventLog = new StringBuilder()
    var reader:InterleavedReader = null
    try {
      reader = new InterleavedReader(arguments.getMarker(),
                                     new InputStreamReader(timeProcess.getInputStream()))
      var o:Object = reader.read()
      while (o != null) {
        o match {
          case s:String => eventLog.append(o)
          case _ => 
            if (measurementSet == null) {
              val jsonObject:JsonObject = o.asInstanceOf[JsonObject]
              measurementSet = Json.measurementSetFromJson(jsonObject)
            } else {
              throw new RuntimeException("Unexpected value: " + o)
            }
        }
        o = reader.read()
      }
    } catch {
      case e:java.io.IOException => throw new RuntimeException(e);
    } finally {
      Closeables.closeQuietly(reader)
      timeProcess.destroy()
    }

    if (measurementSet == null) {
      val message = "Failed to execute " + Joiner.on(" ").join(processBuilder.command());
      System.err.println("  " + message)
      System.err.println(eventLog.toString())
      throw new ConfigurationException(message)
    }

    return new MeasurementResult(measurementSet, eventLog.toString());
  }

  def createProcess(arguments:Arguments, scenarioSelection:ScenarioSelection, scenario:Scenario, measurementType:MeasurementType):ProcessBuilder = {
    val vm = new VmFactory().createVm(scenario)

    val workingDirectory:File = new File(System.getProperty("user.dir"))

    val classPath = defaultClasspath

    val vmArgs:ImmutableList.Builder[String] = ImmutableList.builder()
    vmArgs.addAll(ARGUMENT_SPLITTER.split(scenario.getVariables().get(Scenario.VM_KEY)))
    if (measurementType == MeasurementType.INSTANCE || measurementType == MeasurementType.MEMORY) {
      val allocationJarFile = System.getenv("ALLOCATION_JAR")
      vmArgs.add("-javaagent:" + allocationJarFile)
    }
    vmArgs.addAll(vm.getVmSpecificOptions(measurementType, arguments))

    val vmParameters:java.util.Map[String, String] = scenario.getVariables(
        scenarioSelection.getVmParameterNames())
    for (vmParameter <- vmParameters.values()) {
      vmArgs.addAll(ARGUMENT_SPLITTER.split(vmParameter))
    }

    val caliperArgs:ImmutableList.Builder[String] = ImmutableList.builder();
    caliperArgs.add("--warmupMillis").add(arguments.getWarmupMillis().toLong.toString)
    caliperArgs.add("--runMillis").add(arguments.getRunMillis().toLong.toString)
    caliperArgs.add("--measurementType").add(measurementType.toString())
    caliperArgs.add("--marker").add(arguments.getMarker())

    val userParameters:java.util.Map[String,String] = scenario.getVariables(
        scenarioSelection.getUserParameterNames())
    for (entry <- userParameters.entrySet()) {
      caliperArgs.add("-D" + entry.getKey() + "=" + entry.getValue())
    }
    caliperArgs.add(arguments.getSuiteClassName())

    return vm.newProcessBuilder(workingDirectory, classPath,
        vmArgs.build(), classOf[InProcessRunner].getName(), caliperArgs.build())
  }
}
