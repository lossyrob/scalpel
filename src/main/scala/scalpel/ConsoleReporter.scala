package scalpel

import scala.math._

//import scalpel.Result._

case class LinearTranslation(in1:Double,out1:Double,in2:Double,out2:Double) {
  val denom = in1 - in2
  val m = (out1 - out2) / denom
  val b = (in1 * out2 - in2 * out1) / denom

  def translate(in:Double) = m*in + b
}

object ConsoleReporter {
  private val barGraphWidth = 30
  private val unitsForScore100 = 1
  private val unitsForScore10 = 100000000

  val scoreTranslation = LinearTranslation(log(unitsForScore10),10,log(unitsForScore100), 100)
  
  def run(results:Seq[Result]):Unit = {
    val min = results.min
    val max = results.max

    val median = results.median
    
    /* TODO: When there is a mechanism for reporting on multiple
     * experiments, port this completely */
 
    println("Results:")
    println("--------")
    println(s"Max time    = ${max}")
    println(s"Min time    = ${min}")
    println(s"Median time = ${median * .000001} ms")
  }
}

// Scalameter decorator
import org.scalameter.Reporter
import org.scalameter.CurveData
import org.scalameter.Persistor
import org.scalameter.utils.Tree
object ConsoleReporterWrapper extends Reporter {
  def report(result: CurveData, persistor: Persistor): Unit = 
    ConsoleReporter.run(result.results)
  def report(results: Tree[CurveData], persistor: Persistor): Boolean  =
    true
}
