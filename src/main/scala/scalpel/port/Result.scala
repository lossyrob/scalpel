package scalpel.port

import com.google.caliper.MeasurementSet

import java.util.Date

case class CaliperResult(run:Run, environment:Environment)

case class Run(results:Map[String,BenchmarkResult],suiteName:String,date:Date)

case class BenchmarkResult(measurementSet:MeasurementSet,evenLog:String)
