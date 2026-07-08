package myproject
import chisel3._
import chisel3.util._

class SystemA(width: Int) extends Module {
  val io = IO(new Bundle {
    val in  = Flipped(Decoupled(UInt(width.W)))
    val sel = Input(UInt(2.W))
    val out = Vec(4, Decoupled(UInt(width.W)))
  })
  val router = Module(new Router(width, 4)) // SystemA depends on Router
  router.io.in  <> io.in
  router.io.sel := io.sel
  router.io.out <> io.out
}
