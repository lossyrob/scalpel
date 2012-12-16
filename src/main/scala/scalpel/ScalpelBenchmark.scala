package scalpel

import org.scalameter.api.PerformanceTest
import org.scalameter.api.Reporter
import org.scalameter.api.Persistor
import org.scalameter.api.Gen
import org.scalameter.reporting.LoggingReporter

import com.google.caliper.SimpleBenchmark

import scala.util.Random

object ScalpelBenchmark extends PerformanceTest {

  /* configuration */

  lazy val executor = new CaliperExecutor()
//  lazy val reporter = new LoggingReporter
  lazy val reporter = ConsoleReporterWrapper
  lazy val persistor = Persistor.None

  performance of "while loop" in {
    measure method "int array" in {
      using(Gen.single("dummy")(1)) in { x => println("Not yet reading test info from body") }
    }
  }
}

class ScalpelBenchmark extends SimpleBenchmark {
  val size = 1000
  var ints:Array[Int] = null

  /**
   * Sugar for building arrays using a per-cell init function.
   */
  def init[A:Manifest](size:Int)(init: => A) = {
    val data = Array.ofDim[A](size)
    for (i <- 0 until size) data(i) = init
    data
  }

  /**
   * Sugar to run 'f' for 'reps' number of times.
   */
  def run(reps:Int)(f: => Unit) = {
    var i = 0
    while (i < reps) { f; i += 1 }
  }

  override def setUp() {
    val len = size * size
    ints = init(len)(Random.nextInt)
  }


  def timeIntArrayWhileLoop(reps:Int) = run(reps)(intArrayWhileLoop)
  def intArrayWhileLoop = {
    val goal = ints.clone
    var i = 0
    val len = goal.length
    while (i < len) {
      val z = goal(i)
      if (z != Int.MinValue) goal(i) = z * 2
      i += 1
    }
  }  
}
