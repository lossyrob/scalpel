package scalpel.port

import com.google.caliper.MeasurementSet
import com.google.caliper

import com.google.gson.GsonBuilder


object JsonConversion {
  val gson = new GsonBuilder().create()

  def getJson(ms:MeasurementSet) = gson.toJson(ms)

  def getMeasurementSet(json:String):MeasurementSet = {
    gson.fromJson(json,classOf[MeasurementSet])
  }
}

