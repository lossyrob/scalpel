package scalpel

object ScalpelRunner {
  def main(args:Array[String]):Unit = run
  def run() = {
    val number_of_times = 5
    for(i <- 0 to number_of_times) {
      CaliperRunner.run()
      SMRunner.run(LoopBenchmark)
      SMRunner.run(LocalBenchmark)
    }
  }
}
