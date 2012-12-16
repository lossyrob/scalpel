package scalpel.port

import scala.language.existentials

object Activator {
  def createInstance[T](className:String):T = {
    val cls = try {
      java.lang.Class.forName(className)
    } catch {
      case ignored:ClassNotFoundException =>
        // try replacing the last dot with a $, in case that helps
        // example: tutorial.Tutorial.Benchmark1 becomes tutorial.Tutorial$Benchmark1
        // amusingly, the $ character means three different things in this one line alone
        val newName = className.replaceFirst("\\.([^.]+)$", "\\$$1");
      
      java.lang.Class.forName(newName)
    }

    val constructor = cls.getDeclaredConstructor()
    constructor.setAccessible(true)
    constructor.newInstance().asInstanceOf[T]
  }
}
