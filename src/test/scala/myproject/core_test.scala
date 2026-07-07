package myproject

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class PipelineCpuTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "PipelineCpu"

  it should "compute (5+10) then (that-5) then double it, store, and reload correctly" in {
    test(new PipelineCpu) { dut =>
      // r1=5, r2=10, r3=r1+r2=15, r4=r3-r1=10, r5=r4+r4=20, mem[0]=r5=20, r6=mem[0]=20.
      dut.clock.step(15) // enough cycles for the 7-instruction program to fully drain
      dut.io.debugRegOut.expect(20.U)
    }
  }
}
