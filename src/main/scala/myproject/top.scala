package myproject
import chisel3._

object TopMain extends App with emitrtl.Toplevel {
  val choice = if (args.length == 0) "A" else args(0)

  lazy val topModule = choice match {
    case "A" => new SystemA(8)
    case "B" => new SystemB(8)
  }

  chisel2firrtl()
  firrtl2sv()
}
