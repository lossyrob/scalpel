package scalpel
import scalpel.Functional._

import org.scalameter

class CaliperExecutor() extends scalameter.Executor {
  def translateSetup[T](setup:scalameter.Setup[T]):port.CaliperSetup = {
    import SetupTranslator._
    translate(setup)
  }

  def runSetup[T](setup: scalameter.Setup[T]): scalameter.CurveData = {
    println("[TRANSLATION]: SETUP")
    setup |> translateSetup |> port.SetupRunner.measure |> translateData
  }

  def translateData(measurementResult:port.MeasurementResult):scalameter.CurveData = {
    import MeasurementTranslator._   
    println("[TRANSLATION]: RESULTS")
    translate(measurementResult.measurements)
  }
}
