package scalpel
import scalpel.Functional._

import org.scalameter
//import org.scalameter.utils._

class CaliperExecutor() extends scalameter.Executor {
  def translateSetup[T](setup:scalameter.Setup[T]):port.CaliperSetup = {
    import SetupTranslator._
    translate(setup)
  }

  def runSetup[T](setup: scalameter.Setup[T]): scalameter.CurveData = {
    println(" RUNNING SETUP ")
    setup |> translateSetup |> port.CaliperRunner.measure |> translateData
  }

  def translateData(measurementResult:port.MeasurementResult):scalameter.CurveData = {
    import MeasurementTranslator._   
    translate(measurementResult.measurements)            
  }
}
