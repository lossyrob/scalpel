package scalpel.port

import com.google.caliper.MeasurementSet

object InProcessRunner { 
  def main(args:Array[String]) = {
    try {
      val setup = CaliperSetup.fromArgs(args)
      if(setup.benchmarkNames.length != 1) {
        throw new Exception("InProcessRunner can only run exactly one test.")
      }
      val results = Measurer.run(() => setup.benchmark,setup.warmupTime,setup.runTime)
      println(InterProc.jsonMarker+JsonConversion.getJson(results))
      System.exit(0)
    } catch {
      case e:UserException =>
        e.display() // Would want to communicate this back to the host process
      System.exit(1)
    }
  }
}
