package scalpel

object ScalpelRunner {
  def main(args:Array[String]):Unit = run_timing
  def run_timing() = {
    val number_of_times = 5
    for(i <- 0 to number_of_times) {
      port.CaliperRunner.run()
      SMRunner.run(LoopBenchmark)
      SMRunner.run(LocalBenchmark)
    }
  }

  def run() = {
    SMRunner.run(ScalpelBenchmark)
  }
}
