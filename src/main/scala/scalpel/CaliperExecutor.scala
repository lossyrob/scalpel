package scalpel
import scalpel.Functional._

import org.scalameter

class CaliperExecutor() extends scalameter.Executor {
  def translateSetup[T](setup:scalameter.Setup[T]):port.CaliperSetup = {
    import SetupTranslator._
    translate(setup)
  }

  def runSetup[T](smSetup: scalameter.Setup[T]): scalameter.CurveData = {
    println("[TRANSLATION]: SETUP")
    val setup = smSetup |> translateSetup
    val results = for(name <- setup.benchmarkNames) yield {
      port.SetupRunner.measure(setup,name) |> translateData
    }

    results(0) // Need to run multiple
  }

  def translateData(measurementResult:port.MeasurementResult):scalameter.CurveData = {
    import MeasurementTranslator._   
    println("[TRANSLATION]: RESULTS")
    translate(measurementResult.measurements)
  }
}
