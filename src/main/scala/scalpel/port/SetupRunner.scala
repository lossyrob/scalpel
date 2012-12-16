package scalpel.port

import com.google.caliper.Json
import com.google.caliper.util.InterleavedReader
import com.google.caliper.Arguments
import com.google.caliper.ScenarioSelection
import com.google.caliper.Scenario
import com.google.caliper.MeasurementSet
import com.google.caliper.MeasurementType
import com.google.caliper.ConfigurationException
import com.google.caliper.VmFactory
import com.google.caliper.InProcessRunner

import com.google.gson.JsonObject

import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableList

import java.io.File
import java.io.IOException
import java.io.InputStreamReader

import scala.collection.JavaConversions._

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

  def measure(setup:CaliperSetup):MeasurementResult = {
    // this must be done before starting the forked process on certain VMs
    val processBuilder:ProcessBuilder = createProcess(setup.arguments, setup.scenarioSelection, 
                                                      setup.scenario, setup.measurementType).redirectErrorStream(true)
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
      reader = new InterleavedReader(setup.arguments.getMarker(),
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
      try {
        reader.close();
      } catch {
        case e:IOException => 
          println(s"[ERROR] IOException thrown while closing Closeable: ${e}")
        case e:Throwable => throw e
      }
      timeProcess.destroy()
    }

  if (measurementSet == null) {
    val cmd = processBuilder.command().reduceLeft { (a,b) => s"${a} ${b}" }
      val message = s"Failed to execute ${cmd}" 
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
    vmArgs.addAll(split(scenario.getVariables().get(Scenario.VM_KEY)))
    if (measurementType == MeasurementType.INSTANCE || measurementType == MeasurementType.MEMORY) {
      val allocationJarFile = System.getenv("ALLOCATION_JAR")
      vmArgs.add("-javaagent:" + allocationJarFile)
    }
    vmArgs.addAll(vm.getVmSpecificOptions(measurementType, arguments))

    val vmParameters:java.util.Map[String, String] = scenario.getVariables(
        scenarioSelection.getVmParameterNames())
    for (vmParameter <- vmParameters.values()) {
      vmArgs.addAll(split(vmParameter))
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
