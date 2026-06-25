package myproject

import chisel3._
import chisel3.util.HasBlackBoxInline

private class SimTimeImpl extends BlackBox with HasBlackBoxInline {
  val io = IO(new Bundle {
    val clock = Input(Clock())
    val out   = Output(UInt(64.W))
  })
  setInline(
    "SimTimeImpl.sv",
    """|module SimTimeImpl(input logic clock, output logic [63:0] out);
       |  always @(posedge clock) out <= $time;
       |endmodule
       |""".stripMargin,
  )
}

object SimTime {
  def apply(): UInt = {
    val impl = Module(new SimTimeImpl)
    impl.io.clock := chisel3.Module.clock
    impl.io.out
  }
}
