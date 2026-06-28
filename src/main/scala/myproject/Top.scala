// See README.md for license details.

package myproject

/** To run from a terminal shell
  * {{{
  * ./mill gcd.runMain gcd.gcd8
  * }}}
  */
object gcd8 extends App with emitrtl.Toplevel {

  lazy val topModule = new DecoupledGcd(8)
  // lazy val topModule = new Test
  chisel2firrtl()
  firrtl2sv()

}



/** To run from a terminal shell
  * {{{
  * ./mill gcd.runMain gcd.gcd16
  * }}}
  */
object gcd16 extends App with emitrtl.Toplevel {

  lazy val topModule = new DecoupledGcd(16)
  chisel2firrtl()
  firrtl2sv()
}
