package scalpel

import com.google.caliper
import org.scalameter


//import scala.collection.JavaConversions._

object SetupTranslator {
  object toInternal {
    import scalameter._
    def apply[T](setup:Setup[T]):Experiment = Experiment()
  }

  object toExternal {
    import caliper._
    def apply(experiment:Experiment):port.CaliperSetup = 
      port.CaliperSetup.Default
  }

  def translate[T](setup:scalameter.Setup[T]):port.CaliperSetup =
      toExternal(toInternal(setup))    
}

/* Scalpel Type */
case class Experiment()
