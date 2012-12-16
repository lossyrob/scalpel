package scalpel

case class Stats(values:Seq[Double]) {
  def median:Double = {
    val sorted = values.sorted
    val len = values.length
    if(len % 2 == 0 ) 
      (sorted(len/2 - 1) + sorted(len/2)) / 2
    else
      sorted(len / 2)
  }
}

object Stats {
  implicit def doublesToStats(s:Seq[Double]) = Stats(s)
}
