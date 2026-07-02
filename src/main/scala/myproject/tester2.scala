package myproject
import chisel3._

// A parameterized Accumulator module
class Accumulator(val bitWidth: Int = 4) extends Module {
  val io = IO(new Bundle {
    val en    = Input(Bool())
    val dataIn = Input(UInt(bitWidth.W))
    val out    = Output(UInt(bitWidth.W))
  })

  // Register initialized to 0 on rese
  val countReg = RegInit(0.U(bitWidth.W))

  // Conditional combinational logic
  when(io.en) {
    countReg := countReg + io.dataIn
  }

  // Assign register output to the I/O port
  io.out := countReg
}

// Object to generate the SystemVerilog output
object AccumulatorMain extends App {
  emitVerilog(new Accumulator(4))
}
