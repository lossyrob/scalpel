package scalpel

import scala.math._

object Statistics {
  def median(s:Seq[Double]):Double = 
    if(s.length % 2 == 0) 
      0.5 * s(s.length/2 - 1) * s(s.length/2) 
    else
      s(s.length/2)

  def mean(s:Seq[Double]):Double = 
    if(s.isEmpty) 0.0 else
      s.sum / s.length

  def std(s:Seq[Double]):Double = {
    val m = mean(s) 
    sqrt( s.map { v => val d = m - v ; d*d }.sum /
                      (s.length - 1))
  }
}
