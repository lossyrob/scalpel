package scalpel

import com.google.caliper.Benchmark
import com.google.caliper.Param
import com.google.caliper.Runner 
import com.google.caliper.SimpleBenchmark
import com.google.caliper.UserException
import com.google.caliper.UserException.{DisplayUsageException}
import com.google.common.base.Splitter

import scala.util.Random

class CaliperBenchmark extends SimpleBenchmark {
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
