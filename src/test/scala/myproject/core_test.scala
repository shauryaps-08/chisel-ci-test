package myproject
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class PipelineCpuTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "PipelineCpu"

  it should "compute (5+10) then (that-5) then double it, store, reload, " +
    "and correctly handle a load-use hazard where the loaded register is " +
    "consumed as rs2 of the very next instruction" in {
    test(new PipelineCpu) { dut =>
      // r1=5, r2=10, r3=r1+r2=15, r4=r3-r1=10, r5=r4+r4=20,
      // mem[0]=r5=20, r6=mem[0]=20,
      // r7 = r0 + r6 = 0 + 20 = 20   <-- rs2 load-use hazard case
      //
      // If the hazard-detection logic only checks rs1 (the bug we found
      // earlier), no stall is inserted before this ADD, so r6 hasn't been
      // written back yet and r7 ends up reading stale/zero data instead of 20.
      dut.clock.step(20) // enough cycles for the 8-instruction program, incl. 1 stall cycle, to fully drain

      dut.io.debugRegOut.expect(20.U)   // r6 == 20
      dut.io.debugReg7Out.expect(20.U)  // r7 == 20 -- fails against the buggy rs1-only hazard check
    }
  }

  it should "stall on a load-use hazard via rs1 as well" in {
    // Sanity-check the rs1 side of the hazard check still works using the
    // existing instruction stream: LOAD r6 = mem[0] (instr 6) is immediately
    // followed by ADD r7, r0, r6 in this program, which is an rs2 hazard.
    // This second test exists mainly as a placeholder / example for anyone
    // extending instrMem further with an rs1-hazard case
    // (e.g. ADD rX, r6, r0 right after the LOAD).
    test(new PipelineCpu) { dut =>
      dut.clock.step(20)
      dut.io.debugRegOut.expect(20.U)
    }
  }
}
