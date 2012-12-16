package scalpel.port

object ConfigurationException {
  def apply(s:String):Throwable = ConfigurationException(s)
  def apply(s:String,c:Throwable):Throwable = ConfigurationException(s).initCause(c)
}

class ConfigurationException(s:String) extends RuntimeException(s) 
