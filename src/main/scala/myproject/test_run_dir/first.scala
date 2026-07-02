class Counter {
  // Private variable restricts direct modifications from outside
  private var count: Int = 0

  def increment(): Unit = {
    count += 1
  }

  def decrement(): Unit = {
    if (count > 0) count -= 1
  }

  def currentValue: Int = count
}

object Main extends App {
  val myCounter = new Counter()
  
  myCounter.increment()
  myCounter.increment()
  println(myCounter.currentValue) // Outputs: 2
  
  myCounter.decrement()
  println(myCounter.currentValue) // Outputs: 1
}
