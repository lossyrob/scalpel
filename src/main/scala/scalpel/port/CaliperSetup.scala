package scalpel.port

import com.google.caliper._
import org.scalameter._

import scala.collection.JavaConversions._

/* Caliper data type */

case class CaliperSetup(arguments:Arguments,scenarioSelection:ScenarioSelection,
                        scenario:Scenario,measurementType:MeasurementType)

object CaliperSetup {
  val Default = defaultCaliperSetup

  def defaultCaliperSetup:CaliperSetup = {
    val args = Array[String]("--warmupMillis", "3000", 
                             "--runMillis", "1000", 
                             "--measurementType", "TIME", 
                             "--marker", "//ZxJ/", 
                             "scalpel.CaliperBenchmark")
    val arguments = Arguments.parse(args)
    val scenarioSelection = new ScenarioSelection(arguments)
    val scenario = scenarioSelection.select()(0)
    val measurementType = MeasurementType.TIME
    CaliperSetup(arguments,scenarioSelection,scenario,measurementType)
  }
}
