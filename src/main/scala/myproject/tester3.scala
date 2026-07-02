import chisel3._
import chisel3.util._

class ALU extends Module {
  val io = IO(new Bundle {
    val fn     = Input(UInt(2.W)) // Operation select: 0=Add, 1=Sub, 2=OR, 3=AND
    val a      = Input(UInt(4.W)) // 4-bit input A
    val b      = Input(UInt(4.W)) // 4-bit input B
    val result = Output(UInt(4.W)) // 4-bit output
  })

  // Default output value to prevent inferred latches
  io.result := 0.U

  // ALU selection using a switch statement
  switch(io.fn) {
    is(0.U) { io.result := io.a + io.b } // Addition
    is(1.U) { io.result := io.a - io.b } // Subtraction
    is(2.U) { io.result := io.a | io.b } // Bitwise OR
    is(3.U) { io.result := io.a & io.b } // Bitwise AND
  }
}
