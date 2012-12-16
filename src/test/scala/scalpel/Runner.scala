package scalpel

import org.scalatest.FunSpec
import org.scalatest.ShouldMatchers

@org.junit.runner.RunWith(classOf[org.scalatest.junit.JUnitRunner])
class RunCaliper extends FunSpec with ShouldMatchers {
  describe("run caliper") { it("runs") { port.CaliperRunner.run() } }
}

@org.junit.runner.RunWith(classOf[org.scalatest.junit.JUnitRunner])
class RunSM extends FunSpec with ShouldMatchers {
  describe("run scalameter") { it("runs") { SMRunner.run(LocalBenchmark) } }
}

@org.junit.runner.RunWith(classOf[org.scalatest.junit.JUnitRunner])
class RunScalpel extends FunSpec with ShouldMatchers {
  describe("run scalpel") { it("runs") { ScalpelRunner.run() } }
}
