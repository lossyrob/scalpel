package scalpel.port

import com.google.caliper

import java.lang
import java.util

import scala.collection.JavaConverters._

/**
 * A port of the caliper measurement. I tried to do this early on, but using
 * scala classes with gson made thing difficult, so I'm abanoning these
 * changes for now. However, this is a pretty accurate port of that class,
 * so later if I need the Measurement class in scala here it is.
 */
case class Measurement(unitNames:Map[String,Int], raw:Double,processed:Double) {

  val SORT_BY_NANOS:util.Comparator[Measurement] = new util.Comparator[Measurement] {
    override def compare(a:Measurement,b:Measurement):Int = lang.Double.compare(a.raw,b.raw)
  }

  val SORT_BY_UNITS = new util.Comparator[Measurement] {
    override def compare(a:Measurement,b:Measurement):Int = lang.Double.compare(a.processed,b.processed)
  }
  
  def this(unitNames: util.Map[String,lang.Integer], raw:Double,processed:Double) = {
    
    this((Map[String,Int](unitNames.asScala.toSeq.map {kv=> (kv._1,kv._2.intValue) }:_*)),0.0,0.0)
  }

  def getUnitNames():util.Map[String,lang.Integer] = {
    val m = new util.HashMap[String,lang.Integer]()
    for((key,value) <- unitNames) { m.put(key,new lang.Integer(value)) }
    m
  }
  def getRaw():Double = raw
  def getProcessed():Double = processed

  override def toString():lang.String = if(raw != processed) s"${raw}/${processed}"
                                        else s"${raw}"
}

