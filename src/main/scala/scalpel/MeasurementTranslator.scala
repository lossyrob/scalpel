package scalpel

import com.google.caliper
import org.scalameter

object MeasurementTranslator {
  object toInternal {
    def apply(caliperMeasurements:caliper.MeasurementSet):Seq[Result] = 
      Seq(Result())
  }

  object toExternal {
    import scalameter._
    def apply(results:Seq[Result]):CurveData = 
      CurveData(
        Seq(
          Measurement(1.0,Parameters(),
                      Some(Measurement.Data(Seq(1.0,2.0),true))
                    )
        ),
        Map[String,Any](),
        Context()
      )
  }

  def translate(measurementSet:caliper.MeasurementSet):scalameter.CurveData = 
      toExternal(toInternal(measurementSet))
}

/* Scalpel Type */
case class Result()
