package scalpel.port

import net.liftweb.json.{parse, DefaultFormats}
import net.liftweb.json.Serialization
import net.liftweb.json.Serialization.write
import net.liftweb.json.NoTypeHints

import com.google.caliper.MeasurementSet

object JsonConversion {
  def getJson(measurementSet:MeasurementSet):String = {
    implicit val formats = Serialization.formats(NoTypeHints)
    write(measurementSet)
  }
  def getMeasurementSet(jsonStr:String):MeasurementSet = {
    println(" ATTEMPTING TO GET MEASUREMENT SET")
    implicit val formats = DefaultFormats
    parse(jsonStr).extract[MeasurementSet]
  }
}

