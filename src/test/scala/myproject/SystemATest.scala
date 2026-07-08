package myproject

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class SystemATest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "SystemA"

  it should "route enqueued data to the selected output port" in {
    test(new SystemA(8)) { dut =>
      dut.io.sel.poke(2.U)
      dut.io.in.valid.poke(true.B)
      dut.io.in.bits.poke(77.U)
      dut.io.out(2).ready.poke(true.B)
      dut.clock.step(2)
      dut.io.out(2).valid.expect(true.B)
      dut.io.out(2).bits.expect(77.U)
    }
  }
}

