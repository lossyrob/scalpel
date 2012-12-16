package scalpel

import org.scalameter
import com.google.caliper

object MeasurementTranslator {
  object toInternal {
    import scala.collection.JavaConversions._

    def apply(measurementSet:caliper.MeasurementSet):Seq[Result] = {
      measurementSet.getMeasurements()
                    .map { m => Result(m.getRaw) }
    }
  }

  object toExternal {
    import scalameter._
    def apply(results:Seq[Result]):CurveData = {
      val measurements = results.map { r => Measurement(r.raw, Parameters(), None) }
      CurveData(
        measurements,
        Map[String,Any](),
        Context(),
        results
      )
    }
  }

  def translate(measurementSet:caliper.MeasurementSet):scalameter.CurveData = 
      toExternal(toInternal(measurementSet))
}

