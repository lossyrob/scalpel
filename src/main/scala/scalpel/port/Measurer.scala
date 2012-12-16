package scalpel.port

import com.google.caliper/*.Measurement*/
import com.google.caliper.MeasurementSet
import com.google.caliper.ConfiguredBenchmark

import scalpel.stats._

import scala.language.implicitConversions

object Measurer {
  implicit def measurementsToStats(s:Seq[caliper.Measurement]):Stats = Stats(s.map { m => m.getRaw() })

  val toleranceFactor = 0.01
  val maxTrials = 10

  def log(message:String) = println(s"[scalpel.port]  ${message}")

  def prepareForTest() = {
    System.gc()
    System.gc()
  }

  def run(createBenchmark:() => ConfiguredBenchmark, warmupTime:Long,runTime:Long):MeasurementSet = {
    val warmupScaled = warmupTime  * 1000000
    val runScaled = runTime * 1000000
    val estimatedRepTime = warmup(createBenchmark,warmupScaled)

    val lowerBound = 0.1
    val upperBound = 10000000000.0

    if(estimatedRepTime < lowerBound || upperBound < estimatedRepTime) 
      throw new UserException.RuntimeOutOfRangeException(estimatedRepTime,lowerBound,upperBound)

    log("measuring nanos per rep with scale 1.00")
    val m1 = measure(createBenchmark(), 1.00, estimatedRepTime,runScaled)
    log("measuring nanos per rep with scale 0.50")
    val m2 = measure(createBenchmark(), 0.50, m1.getRaw(),runScaled)
    log("measuring nanos per rep with scale 1.50")
    val m3 = measure(createBenchmark(), 1.50, m1.getRaw(),runScaled)

    var measures = Seq(m1,m2,m3)
    var i = 3
    while(measures.standardDeviation < toleranceFactor * measures.mean && i < maxTrials) {
      log("performing additional measurement with scale 1.00")
      measures = measures :+ measure(createBenchmark(),1.00,m1.getRaw(),runTime)
      i += 1
    }
    
    new MeasurementSet(measures:_*)
  }

  def warmup(createBenchmark:()=>ConfiguredBenchmark,warmupTime:Long) = {
    log("starting warmup")

    var elapsed = 0L
    var netReps = 0l
    var reps = 1
    var checkedScalesLinearly = false

    while(elapsed < warmupTime) {
      elapsed +=  measureReps(createBenchmark(),reps)
      netReps += reps
      reps *= 2
      
      // if reps overflowed, that's suspicious! Check that it time scales with reps
      if(reps <= 0) {
        if(!checkedScalesLinearly) {
          checkScalesLinearly(createBenchmark)
          checkedScalesLinearly = true
        }
        reps = Int.MaxValue
      }
    }
    
    log("ending warmup")

    elapsed / netReps
  }

  def checkScalesLinearly(createBenchmark:()=>ConfiguredBenchmark) = {
    var half = measureReps(createBenchmark(),Int.MaxValue/2)
    var one = measureReps(createBenchmark(),Int.MaxValue)
    if(half/one > 0.75)
      throw new UserException.DoesNotScaleLinearlyException
  }

  def measureReps(benchmark:ConfiguredBenchmark,reps:Int):Long = {
    prepareForTest()
    log(s"measurement starting. reps = ${reps}")
    val startNanos = System.nanoTime()
    benchmark.run(reps)
    val endNanos = System.nanoTime()
    log("measurement ending.")
    return endNanos - startNanos
  }

  def measure(benchmark:ConfiguredBenchmark,durationScale:Double,
                                            estimatedRepTime:Double,
                                            runTime:Double) = {
    var reps = (durationScale * runTime / estimatedRepTime).toInt
    if(reps == 0) reps = 1
    
    log(s"running trial with ${reps} reps")
    val elapsed = measureReps(benchmark,reps)
    val repTime = elapsed / reps.toDouble
    
    new caliper.Measurement(benchmark.timeUnitNames,repTime,benchmark.nanosToUnits(repTime))
  }
}
