package scalpel.port

import scala.collection.mutable
import scala.collection.JavaConversions._
import sys.process._

import java.net.UnknownHostException;
import java.net.InetAddress;

case class Environment(properties:Map[String,String])

object Environment {
  def getEnvironmentSnapshot() = {
    var props = Map[String,String]()

    var sysProps = Map() ++ System.getProperties()

    var version = sysProps("java.version")
    val alternateVersion = sysProps("java.runtime.version")
    if(alternateVersion != null && alternateVersion.length > version.length)
      version = alternateVersion

    props = props ++ Map(("jre.version",version),
                         ("jre.vmname",sysProps("java.vm.name")),
                         ("jre.vmversion",sysProps("java.vm.version")),
                         ("jre.availableProcessors",Runtime.getRuntime.availableProcessors.toString),
                         ("os.name",sysProps("os.name")),
                         ("os.versoin",sysProps("os.version")),
                         ("os.arch",sysProps("os.arch")))

    try { 
      props = props + (("host.name",InetAddress.getLocalHost().getHostName()))
    } catch { case ignored:UnknownHostException => }

    // if(sysProps("os.name") == "Linux") {
    //   try {
    //     var procProps = mutable.HashMap[String,String]()
    //     val processLine:String=>Unit = { s => 
    //       val splits = s.split(':')
    //       if(s.length == 2) { procProps(splits(0).trim) = procProps(splits(1).trim) }
    //     }
    //     "/bin/cat" :: "/proc/cpuinfo" :: Nil ! ProcessLogger(processLine)
    //     props ++ Map(("host.cpu.cores", procProps("cpu cores")),
    //                  ("host.cpu.cores", procProps("cpu cores")),
        
    //   } catch { case ignored:Throwable => }
    // } 
    // TODO: Get the environment from /proc/cpuinfo and /proc/meminfo
    Environment(props)
  }
}
