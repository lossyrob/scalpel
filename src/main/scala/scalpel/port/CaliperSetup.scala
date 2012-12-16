package scalpel.port

import com.google.caliper.Benchmark
import com.google.caliper.ConfiguredBenchmark

import org.scalameter._

import scala.collection.JavaConversions._

/* Caliper data type */

class CaliperSetup {
  def suiteClassName = "scalpel.CaliperBenchmark"
  def warmupTime:Long = 3000
  def runTime:Long = 1000
  def trial = 1

  def benchmark:ConfiguredBenchmark = 
    if(benchmarkNames.length != 1)
      sys.error("Benchmarks cannot be created off of a setup with multiple benchmark names")
    else
      getBenchmark(benchmarkNames(0))

  def getBenchmark(name:String):ConfiguredBenchmark = 
    Activator.createInstance[Benchmark](suiteClassName)
             .createBenchmark("IntArrayWhileLoop")

  def benchmarkNames = Seq[String]("IntArrayWhileLoop")
}

object CaliperSetup {
  val Default = new CaliperSetup()

  def fromArgs(args:Seq[String]):CaliperSetup = {
    Default
  }
}
