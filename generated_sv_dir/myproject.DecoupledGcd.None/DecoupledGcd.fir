FIRRTL version 3.3.0
circuit DecoupledGcd :%[[
  {
    "class":"firrtl.transforms.BlackBoxInlineAnno",
    "target":"DecoupledGcd.SimTimeImpl",
    "name":"SimTimeImpl.sv",
    "text":"module SimTimeImpl(input logic clock, output logic [63:0] out);\n  always @(posedge clock) out <= $time;\nendmodule\n"
  }
]]
  extmodule SimTimeImpl : @[src/main/scala/myproject/SimTime.scala 6:15]
    input clock : Clock
    output out : UInt<64>
    defname = SimTimeImpl

  module DecoupledGcd : @[src/main/scala/myproject/DecoupledGCD.scala 23:7]
    input clock : Clock @[src/main/scala/myproject/DecoupledGCD.scala 23:7]
    input reset : UInt<1> @[src/main/scala/myproject/DecoupledGCD.scala 23:7]
    input input : { flip ready : UInt<1>, valid : UInt<1>, bits : { value1 : UInt<8>, value2 : UInt<8>}} @[src/main/scala/myproject/DecoupledGCD.scala 24:18]
    output output : { flip ready : UInt<1>, valid : UInt<1>, bits : { value1 : UInt<8>, value2 : UInt<8>, gcd : UInt<8>}} @[src/main/scala/myproject/DecoupledGCD.scala 25:18]

    reg xInitial : UInt, clock @[src/main/scala/myproject/DecoupledGCD.scala 27:24]
    reg yInitial : UInt, clock @[src/main/scala/myproject/DecoupledGCD.scala 28:24]
    reg x : UInt, clock @[src/main/scala/myproject/DecoupledGCD.scala 29:24]
    reg y : UInt, clock @[src/main/scala/myproject/DecoupledGCD.scala 30:24]
    regreset busy : UInt<1>, clock, reset, UInt<1>(0h0) @[src/main/scala/myproject/DecoupledGCD.scala 31:28]
    regreset resultValid : UInt<1>, clock, reset, UInt<1>(0h0) @[src/main/scala/myproject/DecoupledGCD.scala 32:28]
    node _input_ready_T = eq(busy, UInt<1>(0h0)) @[src/main/scala/myproject/DecoupledGCD.scala 34:19]
    connect input.ready, _input_ready_T @[src/main/scala/myproject/DecoupledGCD.scala 34:16]
    connect output.valid, resultValid @[src/main/scala/myproject/DecoupledGCD.scala 35:16]
    invalidate output.bits.gcd @[src/main/scala/myproject/DecoupledGCD.scala 36:16]
    invalidate output.bits.value2 @[src/main/scala/myproject/DecoupledGCD.scala 36:16]
    invalidate output.bits.value1 @[src/main/scala/myproject/DecoupledGCD.scala 36:16]
    when busy : @[src/main/scala/myproject/DecoupledGCD.scala 38:14]
      node _T = gt(x, y) @[src/main/scala/myproject/DecoupledGCD.scala 39:12]
      when _T : @[src/main/scala/myproject/DecoupledGCD.scala 39:17]
        node _x_T = sub(x, y) @[src/main/scala/myproject/DecoupledGCD.scala 40:14]
        node _x_T_1 = tail(_x_T, 1) @[src/main/scala/myproject/DecoupledGCD.scala 40:14]
        connect x, _x_T_1 @[src/main/scala/myproject/DecoupledGCD.scala 40:9]
      else :
        node _y_T = sub(y, x) @[src/main/scala/myproject/DecoupledGCD.scala 42:14]
        node _y_T_1 = tail(_y_T, 1) @[src/main/scala/myproject/DecoupledGCD.scala 42:14]
        connect y, _y_T_1 @[src/main/scala/myproject/DecoupledGCD.scala 42:9]
      node _T_1 = eq(x, UInt<1>(0h0)) @[src/main/scala/myproject/DecoupledGCD.scala 44:12]
      node _T_2 = eq(y, UInt<1>(0h0)) @[src/main/scala/myproject/DecoupledGCD.scala 44:25]
      node _T_3 = or(_T_1, _T_2) @[src/main/scala/myproject/DecoupledGCD.scala 44:20]
      when _T_3 : @[src/main/scala/myproject/DecoupledGCD.scala 44:34]
        node _T_4 = eq(x, UInt<1>(0h0)) @[src/main/scala/myproject/DecoupledGCD.scala 45:14]
        when _T_4 : @[src/main/scala/myproject/DecoupledGCD.scala 45:23]
          connect output.bits.gcd, y @[src/main/scala/myproject/DecoupledGCD.scala 46:25]
        else :
          connect output.bits.gcd, x @[src/main/scala/myproject/DecoupledGCD.scala 48:25]
        connect output.bits.value1, xInitial @[src/main/scala/myproject/DecoupledGCD.scala 51:26]
        connect output.bits.value2, yInitial @[src/main/scala/myproject/DecoupledGCD.scala 52:26]
        connect resultValid, UInt<1>(0h1) @[src/main/scala/myproject/DecoupledGCD.scala 53:26]
        inst impl of SimTimeImpl @[src/main/scala/myproject/SimTime.scala 22:22]
        connect impl.clock, clock @[src/main/scala/myproject/SimTime.scala 23:19]
        node _T_5 = eq(reset, UInt<1>(0h0)) @[src/main/scala/myproject/DecoupledGCD.scala 54:13]
        when _T_5 : @[src/main/scala/myproject/DecoupledGCD.scala 54:13]
          printf(clock, UInt<1>(0h1), "time=%d resultvalid\n", impl.out) : printf @[src/main/scala/myproject/DecoupledGCD.scala 54:13]
        node _T_6 = and(output.ready, resultValid) @[src/main/scala/myproject/DecoupledGCD.scala 56:25]
        when _T_6 : @[src/main/scala/myproject/DecoupledGCD.scala 56:41]
          connect busy, UInt<1>(0h0) @[src/main/scala/myproject/DecoupledGCD.scala 58:21]
          connect resultValid, UInt<1>(0h0) @[src/main/scala/myproject/DecoupledGCD.scala 59:21]
    else :
      when input.valid : @[src/main/scala/myproject/DecoupledGCD.scala 63:23]
        connect input.ready, UInt<1>(0h1) @[src/main/scala/chisel3/util/Decoupled.scala 76:20]
        connect x, input.bits.value1 @[src/main/scala/myproject/DecoupledGCD.scala 65:16]
        connect y, input.bits.value2 @[src/main/scala/myproject/DecoupledGCD.scala 66:16]
        connect xInitial, input.bits.value1 @[src/main/scala/myproject/DecoupledGCD.scala 67:16]
        connect yInitial, input.bits.value2 @[src/main/scala/myproject/DecoupledGCD.scala 68:16]
        connect busy, UInt<1>(0h1) @[src/main/scala/myproject/DecoupledGCD.scala 69:16]


