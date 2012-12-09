package scalpel

import org.scalameter._
import org.scalameter.utils._

import org.scalatest.FunSpec
import org.scalatest.ShouldMatchers

@org.junit.runner.RunWith(classOf[org.scalatest.junit.JUnitRunner])
class RunnerSpec extends FunSpec with ShouldMatchers {
  describe("run scalameter") {
    it("runs") {  
      val datestart = new java.util.Date

      DSL.setupzipper.value = Tree.Zipper.root[Setup[_]]
      LoopBenchmark.testbody.value.apply()

      val setuptree = DSL.setupzipper.value.result
      val resulttree = LoopBenchmark.executor.run(setuptree.asInstanceOf[Tree[Setup[LoopBenchmark.SameType]]], LoopBenchmark.reporter, LoopBenchmark.persistor)

      val dateend = new java.util.Date
      val datedtree = resulttree.copy(context = resulttree.context + (Key.reports.startDate -> datestart) + (Key.reports.endDate -> dateend))

      LoopBenchmark.reporter.report(datedtree, LoopBenchmark.persistor)
    }
  }

  describe("run caliper") {
    it("runs") { CaliperRunner.run() }
  }
}
