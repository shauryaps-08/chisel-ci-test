import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class FullAdderSpec extends AnyFlatSpec with ChiselScalatestTester {
  "FullAdder" should "compute sum and carry correctly" in {
    test(new FullAdder) { c =>
      // 0 + 0 + 0 = 0, carry 0
      c.io.a.poke(0.U)
      c.io.b.poke(0.U)
      c.io.cin.poke(0.U)
      c.io.sum.expect(0.U)
      c.io.cout.expect(0.U)

      // 1 + 1 + 0 = 0, carry 1
      c.io.a.poke(1.U)
      c.io.b.poke(1.U)
      c.io.cin.poke(0.U)
      c.io.sum.expect(0.U)
      c.io.cout.expect(1.U)

      // 1 + 1 + 1 = 1, carry 1
      c.io.a.poke(1.U)
      c.io.b.poke(1.U)
      c.io.cin.poke(1.U)
      c.io.sum.expect(1.U)
      c.io.cout.expect(1.U)
    }
  }
}
