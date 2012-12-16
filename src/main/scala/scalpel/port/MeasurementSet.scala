package scalpel.port

import scalpel.Statistics

import com.google.caliper

import java.lang
import java.util

import scala.collection.JavaConversions._
/**
 * A port of the caliper MeasurementSet. I tried to do this early on, but using
 * scala classes with gson made thing difficult, so I'm abanoning these
 * changes for now. However, this is a pretty accurate port of that class,
 * so later if I need the MeasurementSet class in scala here it is.
 */
/*case class MeasurementSet(measurements:Array[Measurement]) {
  def this() = this(Array[Measurement]())

  private val raw = measurements.toSeq.map { m => m.raw }
  private val processed = measurements.toSeq.map { m => m.processed }

  def unitNames:util.Map[String,lang.Integer] = {
      if(!measurements.isEmpty) {
        measurements.foldLeft(measurements(0).getUnitNames) { 
          (a:util.Map[String,lang.Integer],b:Measurement) => 
            if(a != b.getUnitNames) 
              throw new IllegalArgumentException(s"incompatible unit names: ${a} and ${b.unitNames}") 
            a
        }
      } else { new util.HashMap[String,lang.Integer]() }
  }
  
  def getUnitNames(default:java.util.Map[String,lang.Integer]):java.util.Map[String,lang.Integer] = 
    if(unitNames == null) 
      default
    else
      new java.util.HashMap[String,lang.Integer]()
  
  def getUnitNames():java.util.Map[String,lang.Integer] = unitNames
  def getMeasurements():util.List[Measurement] = new util.ArrayList[Measurement](measurements.toSeq)

  def size() =  measurements.size

  def getMeasurementsRaw():util.List[Double] = raw.toList
  def getMeasurementUnits():util.List[Double] = processed.toList

  def medianRaw() = Statistics.median(raw)
  def medianUnits() = Statistics.median(processed)

  def meanRaw() = Statistics.mean(raw)
  def meanUnits() = Statistics.mean(processed)

  def standardDeviationRaw() = Statistics.std(raw)
  def standardDeviationUnits() = Statistics.std(processed)

  def minRaw() = raw.min
  def minUnits() = processed.min
  def maxRaw() = raw.max
  def maxUnits() = processed.max

  def plusMeasurement(measurement:Measurement):MeasurementSet = {
    if (unitNames != null && !(unitNames == measurement.getUnitNames())) {
      throw new IllegalArgumentException("new measurement incompatible with units of measurement "
          + s"set. Expected ${unitNames} but got ${measurement.getUnitNames()}")
    }
    new MeasurementSet((measurement :: measurements.toList).toArray)//,systemOutCharCount,systemErrCharCount)
  }
}
*/
