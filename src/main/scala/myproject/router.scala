package myproject
import chisel3._
import chisel3.util._

class Router(width: Int, numPorts: Int) extends Module {
  val io = IO(new Bundle {
    val in    = Flipped(Decoupled(UInt(width.W)))
    val sel   = Input(UInt(log2Ceil(numPorts).W))
    val out   = Vec(numPorts, Decoupled(UInt(width.W)))
  })
  val buf = Module(new SimpleFifo(width, 4)) // Router depends on SimpleFifo
  buf.io.enq <> io.in

  for (i <- 0 until numPorts) {
    io.out(i).valid := buf.io.deq.valid && (io.sel === i.U)
    io.out(i).bits  := buf.io.deq.bits
  }
  buf.io.deq.ready := io.out(io.sel).ready
}

