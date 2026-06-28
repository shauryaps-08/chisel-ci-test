import chisel3._

class FullAdder extends Module {
  val io = IO(new Bundle {
    val a   = Input(UInt(1.W))
    val b   = Input(UInt(1.W))
    val cin = Input(UInt(1.W))
    val sum  = Output(UInt(1.W))
    val cout = Output(UInt(1.W))
  })

  // The "+" operator adds the values and automatically computes the sum and carry bits
  // We extract them using bit extraction (.asUInt or casting)
  val result = io.a + io.b + io.cin
  
  io.sum  := result(0)
  io.cout := result(1
}
