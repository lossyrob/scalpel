package scalpel.port

object InterProc {
  val jsonMarker = "//ZxJ/"
}

trait InterleavedReader {
  lazy val marker = InterProc.jsonMarker
  
  def log(s:String):Unit

  def handleJson(s:String):Unit

  def readLine(s:String) = {
    if(s.startsWith(marker)) handleJson(s.substring(marker.length))
    else log(s)      
  }
}
