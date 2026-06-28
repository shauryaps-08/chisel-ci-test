package myproject

import chisel3._

class FullAdder extends Module {
  val io = IO(new Bundle {
    val a   = Input(UInt(1.W))
    val b   = Input(UInt(1.W))
    val cin = Input(UInt(1.W))
    val sum  = Output(UInt(1.W))
    val cout = Output(UInt(1.W))
  })

  val result = Wire(UInt(2.W))
  result := io.a +& io.b +& io.cin  // +& prevents truncation, keeps carry

  io.sum  := result(0)
  io.cout := result(1)
}
