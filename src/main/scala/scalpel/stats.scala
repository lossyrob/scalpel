package scalpel

import scala.math._

package object stats {
  import scala.language.implicitConversions
  implicit def doublesToStats(s:Seq[Double]) = Stats(s)

  case class Stats(values:Seq[Double]) {
    lazy val median:Double = {
      val sorted = values.sorted
      val len = values.length
      if(len % 2 == 0 ) 
        (sorted(len/2 - 1) + sorted(len/2)) / 2
      else
        sorted(len / 2)
    }
    
    lazy val mean = {
      values.sum / values.length
    }

    lazy val standardDeviation = {
      val m = mean
      val sumOfSquares = values.map { x => val d = x - m ; m*m }.sum
      sqrt(sumOfSquares/(values.length - 1))
    }
  }
}

