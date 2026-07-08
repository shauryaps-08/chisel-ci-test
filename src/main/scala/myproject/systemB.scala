package myproject
import chisel3._
import chisel3.util._

class SystemB(width: Int) extends Module {
  val io = IO(new Bundle {
    val in  = Flipped(Decoupled(UInt(width.W)))
    val out = Decoupled(UInt(width.W))
  })
  val buf1 = Module(new SimpleFifo(width, 2))
  val buf2 = Module(new SimpleFifo(width, 2)) // SystemB chains two FIFOs directly
  buf1.io.enq <> io.in
  buf2.io.enq <> buf1.io.deq
  io.out <> buf2.io.deq
}

