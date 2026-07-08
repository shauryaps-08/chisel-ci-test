package myproject
import chisel3._
import chisel3.util._

class SimpleFifo(width: Int, depth: Int) extends Module {
  val io = IO(new Bundle {
    val enq = Flipped(Decoupled(UInt(width.W)))
    val deq = Decoupled(UInt(width.W))
  })
  val q = Module(new Queue(UInt(width.W), depth))
  q.io.enq <> io.enq
  io.deq <> q.io.deq
}
