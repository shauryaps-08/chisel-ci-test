import chisel3._

class Mux2to1 extends Module {
  val io = IO(new Bundle {
    val in0 = Input(UInt(8.W))
    val in1 = Input(UInt(8.W))
    val sel = Input(Bool())
    val out = Output(UInt(8.W))
  })

  // Mux(condition, true_case, false_case)
  io.out := Mux(io.sel: io.in1, io.in0)
}
