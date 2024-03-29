package org.scalameter



import collection._
import scala.util.DynamicVariable
import utils.Tree



trait DSL {

  import DSL._

  val testbody = new DynamicVariable[() => Any](() => ???)

  object performance {
    def of(modulename: String) = Scope(modulename, setupzipper.value.current.context)
  }

  object measure {
    def method(methodname: String) = Scope(methodname, setupzipper.value.current.context)
  }

  protected case class Scope(name: String, context: Context) {
    def config(kvs: (String, Any)*) = Scope(name, context ++ Context(kvs.toMap))
    def in(block: =>Unit): Unit = {
      val oldscope = context.goe(Key.dsl.scope, List())
      descendInScope(name, context + (Key.dsl.scope -> (name :: oldscope))) {
        block
      }
    }
  }

  protected case class Using[T](benchmark: Setup[T]) {
    def setUp(block: T => Any) = Using(benchmark.copy(setup = Some(block)))
    def tearDown(block: T => Any) = Using(benchmark.copy(teardown = Some(block)))
    def warmUp(block: =>Any) = Using(benchmark.copy(customwarmup = Some(() => block)))
    def curve(name: String) = Using(benchmark.copy(context = benchmark.context + (Key.dsl.curve -> name)))
    def config(xs: (String, Any)*) = Using(benchmark.copy(context = benchmark.context ++ Context(xs: _*)))
    def in(block: T => Any) {
      setupzipper.value = setupzipper.value.addItem(benchmark.copy(snippet = block))
    }
  }

  def using[T](gen: Gen[T]) = Using(Setup(setupzipper.value.current.context + (Key.dsl.curve -> freshCurveName()), gen, None, None, None, null))

  def isModule = this.getClass.getSimpleName.endsWith("$")

  def include[T <: PerformanceTest.Initialization: Manifest] = {
    if (isModule) singletonInstance(manifest[T].erasure).testbody.value.apply()
    else manifest[T].erasure.newInstance.asInstanceOf[PerformanceTest].testbody.value.apply()
  }

  /** Runs all the tests in this test class or singleton object.
   */
  def executeTests(): Boolean

}


object DSL {

  val setupzipper = new DynamicVariable(Tree.Zipper.root[Setup[_]])

  def descendInScope(name: String, context: Context)(body: =>Unit) {
    setupzipper.value = setupzipper.value.descend.setContext(context)
    body
    setupzipper.value = setupzipper.value.ascend
  }

  val curveNameCount = new java.util.concurrent.atomic.AtomicInteger(0)

  def freshCurveName(): String = "Test-" + curveNameCount.getAndIncrement()

}





