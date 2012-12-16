package scalpel.port

trait InterleavedReader {
  val marker = "//ZxJ/"
  
  def log(s:String):Unit

  def handleJson(s:String):Unit

  def readLine(s:String) = {
    if(s.startsWith(marker)) handleJson(s.substring(marker.length))
    else log(s)      
  }
}
