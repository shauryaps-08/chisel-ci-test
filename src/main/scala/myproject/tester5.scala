package myproject

import chisel3._

class MyModule extends Module {
  val io = IO(new Bundle {
    val in  = Input(UInt(3.W))
    val out = Output(UInt(8.W))
  })
  io.out := (1.U << io.in)
}

object MyModuleMain extends App with emitrtl.Toplevel {
  lazy val topModule = new MyModule
  chisel2firrtl()
  firrtl2sv()
}
