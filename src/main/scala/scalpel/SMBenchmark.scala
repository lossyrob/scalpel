package scalpel

import org.scalameter.api._

import scala.util.Random

object LoopBenchmark extends PerformanceTest {

  /* configuration */

  lazy val executor = SeparateJvmsExecutor(new Executor.Warmer.Default, Aggregator.min, new Measurer.Default)
  lazy val reporter = Reporter.None
  lazy val persistor = Persistor.None

  /* Input */
  val size = 1000
  var ints:Array[Int] = null

  def init[A:Manifest](size:Int)(init: => A) = {
    val data = Array.ofDim[A](size)
    for (i <- 0 until size) data(i) = init
    data
  }

  val len = size * size
  ints = init(len)(Random.nextInt)

  performance of "while loop" in {
    measure method "int array" in {
      using(Gen.single("ints")(ints)) in { ints =>
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
  }
}

object LocalBenchmark extends PerformanceTest {

  /* configuration */

  lazy val executor = LocalExecutor(new Executor.Warmer.Default, Aggregator.min, new Measurer.Default)
  lazy val reporter = Reporter.None
  lazy val persistor = Persistor.None

  /* Input */
  val size = 1000
  var ints:Array[Int] = null

  def init[A:Manifest](size:Int)(init: => A) = {
    val data = Array.ofDim[A](size)
    for (i <- 0 until size) data(i) = init
    data
  }

  val len = size * size
  ints = init(len)(Random.nextInt)

  performance of "while loop" in {
    measure method "int array" in {
      using(Gen.single("ints")(ints)) in { ints =>
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
  }
}

