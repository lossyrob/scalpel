package scalpel

case class Result(raw:Double)

trait ResultOrdering extends scala.math.Ordering[Result] {
  def compare(x:Result,y:Result) = x.raw.compare(y.raw)
}

object Result {
  import scala.language.implicitConversions
  implicit object DefaultResultOrdering extends ResultOrdering
  implicit def resultsToStats(s:Seq[Result]) = Stats(s map { x => x.raw })
}
