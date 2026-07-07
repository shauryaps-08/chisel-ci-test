package myproject
import chisel3._
import chisel3.util._

// Simple custom ISA:
// [31:29] opcode  [28:26] rs1  [25:23] rs2  [22:20] rd  [19:0] imm
// opcodes: 0=ADD 1=SUB 2=AND 3=OR 4=LOAD 5=STORE 6=BEQ 7=ADDI
//
// *** THIS FILE INTENTIONALLY CONTAINS A HIDDEN BUG FOR VERIFICATION-PIPELINE TESTING ***
// See the "BUG" comment near loadUseHazard below. Do not use this as your real source.

class PipelineCpu extends Module {
  val io = IO(new Bundle {
    val debugRegOut = Output(UInt(32.W))
  })

  val regFile = Mem(8, UInt(32.W))
  val dataMem = Mem(256, UInt(32.W))
  val instrMem = VecInit(Seq(
    Cat(7.U(3.W), 0.U(3.W), 0.U(3.W), 1.U(3.W), 5.U(20.W)),   // ADDI r1, r0, 5
    Cat(7.U(3.W), 0.U(3.W), 0.U(3.W), 2.U(3.W), 10.U(20.W)),  // ADDI r2, r0, 10
    Cat(0.U(3.W), 1.U(3.W), 2.U(3.W), 3.U(3.W), 0.U(20.W)),   // ADD  r3, r1, r2
    Cat(1.U(3.W), 3.U(3.W), 1.U(3.W), 4.U(3.W), 0.U(20.W)),   // SUB  r4, r3, r1
    Cat(0.U(3.W), 4.U(3.W), 4.U(3.W), 5.U(3.W), 0.U(20.W)),   // ADD  r5, r4, r4
    Cat(5.U(3.W), 0.U(3.W), 5.U(3.W), 0.U(3.W), 0.U(20.W)),   // STORE mem[0] = r5
    Cat(4.U(3.W), 0.U(3.W), 0.U(3.W), 6.U(3.W), 0.U(20.W)),   // LOAD  r6 = mem[0]
  ).map(_(31, 0)))

  val pcReg = RegInit(0.U(log2Ceil(7).W))
  val if_id_pc  = RegInit(0.U(8.W))
  val if_id_ir  = RegInit(0.U(32.W))
  val if_id_valid = RegInit(false.B)

  val id_ex_rd    = Reg(UInt(3.W))
  val id_ex_op    = Reg(UInt(3.W))
  val id_ex_a     = Reg(UInt(32.W))
  val id_ex_b     = Reg(UInt(32.W))
  val id_ex_rs1   = Reg(UInt(3.W))
  val id_ex_rs2   = Reg(UInt(3.W))
  val id_ex_imm   = Reg(UInt(32.W))
  val id_ex_valid = RegInit(false.B)
  val id_ex_regWrite = RegInit(false.B)
  val id_ex_memWrite = RegInit(false.B)
  val id_ex_memRead  = RegInit(false.B)

  val ex_mem_rd       = Reg(UInt(3.W))
  val ex_mem_result   = Reg(UInt(32.W))
  val ex_mem_storeVal = Reg(UInt(32.W))
  val ex_mem_valid    = RegInit(false.B)
  val ex_mem_regWrite = RegInit(false.B)
  val ex_mem_memWrite = RegInit(false.B)
  val ex_mem_memRead  = RegInit(false.B)

  val mem_wb_rd       = Reg(UInt(3.W))
  val mem_wb_result   = Reg(UInt(32.W))
  val mem_wb_valid    = RegInit(false.B)
  val mem_wb_regWrite = RegInit(false.B)

  val stall = WireDefault(false.B)
  val flush = WireDefault(false.B)

  // ---- IF ----
  when(!stall) {
    when(pcReg < instrMem.length.U) {
      if_id_ir    := instrMem(pcReg)
      if_id_pc    := pcReg
      if_id_valid := true.B
      pcReg       := pcReg + 1.U
    }.otherwise {
      if_id_valid := false.B
    }
  }
  when(flush) { if_id_valid := false.B }

  // ---- ID ----
  val ir     = if_id_ir
  val opcode = ir(31, 29)
  val rs1    = ir(28, 26)
  val rs2    = ir(25, 23)
  val rd     = ir(22, 20)
  val imm    = ir(19, 0)
  val immExt = Cat(Fill(12, imm(19)), imm)

  val isAddi  = opcode === 7.U
  val isLoad  = opcode === 4.U
  val isStore = opcode === 5.U
  val regWriteD = !isStore && if_id_valid

  val rawA = regFile.read(rs1)
  val rawB = regFile.read(rs2)

  def forwardID(srcReg: UInt, raw: UInt): UInt = {
    val fromExMem = ex_mem_valid && ex_mem_regWrite && ex_mem_rd === srcReg && srcReg =/= 0.U
    val fromMemWb = mem_wb_valid && mem_wb_regWrite && mem_wb_rd === srcReg && srcReg =/= 0.U
    MuxCase(raw, Seq(
      fromExMem -> ex_mem_result,
      fromMemWb -> mem_wb_result
    ))
  }

  val fwdA = forwardID(rs1, rawA)
  val fwdB = forwardID(rs2, rawB)

  // *** BUG (hidden) ***
  // Load-use hazard detection only checks rs1, silently dropping the rs2 check.
  // This is syntactically and structurally perfect Chisel: it elaborates cleanly,
  // lowers to fully legal FIRRTL/Verilog, and firtool will never flag it because
  // there is nothing structurally wrong -- it's just semantically incomplete.
  // A load followed by an instruction that consumes the loaded value only via rs2
  // (e.g. `ADD rX, rY, r6` right after `LOAD r6, ...`) will read stale/garbage data
  // instead of stalling. The provided PipelineCpuTest program never puts a loaded
  // register in the rs2 position of the very next instruction, so this bug produces
  // a bit-for-bit identical debugRegOut=20 result and the existing test PASSES.
  val loadUseHazard = id_ex_memRead && (id_ex_rd === rs1) && if_id_valid
  stall := loadUseHazard

  when(!stall) {
    id_ex_rd        := rd
    id_ex_op        := opcode
    id_ex_a         := fwdA
    id_ex_b         := fwdB
    id_ex_rs1       := rs1
    id_ex_rs2       := rs2
    id_ex_imm       := immExt
    id_ex_valid     := if_id_valid
    id_ex_regWrite  := regWriteD
    id_ex_memWrite  := isStore
    id_ex_memRead   := isLoad
  }.otherwise {
    id_ex_valid    := false.B
    id_ex_regWrite := false.B
    id_ex_memWrite := false.B
    id_ex_memRead  := false.B
  }

  // ---- EX ----
  def forward(srcReg: UInt, raw: UInt): UInt = {
    val fromExMem = ex_mem_valid && ex_mem_regWrite && ex_mem_rd === srcReg && srcReg =/= 0.U
    val fromMemWb = mem_wb_valid && mem_wb_regWrite && mem_wb_rd === srcReg && srcReg =/= 0.U
    MuxCase(raw, Seq(
      fromExMem -> ex_mem_result,
      fromMemWb -> mem_wb_result
    ))
  }

  val opA = forward(id_ex_rs1, id_ex_a)
  val opBraw = forward(id_ex_rs2, id_ex_b)
  val opB = Mux(id_ex_op === 7.U, id_ex_imm, opBraw)

  val aluResult = MuxLookup(id_ex_op, 0.U)(Seq(
    0.U -> (opA + opB),
    1.U -> (opA - opB),
    2.U -> (opA & opB),
    3.U -> (opA | opB),
    4.U -> (opA + id_ex_imm),
    5.U -> (opA + id_ex_imm),
    7.U -> (opA + opB)
  ))

  ex_mem_rd       := id_ex_rd
  ex_mem_result   := aluResult
  ex_mem_storeVal := opBraw
  ex_mem_valid    := id_ex_valid
  ex_mem_regWrite := id_ex_regWrite
  ex_mem_memWrite := id_ex_memWrite
  ex_mem_memRead  := id_ex_memRead

  // ---- MEM ----
  val memReadData = dataMem.read(ex_mem_result(7, 0))
  when(ex_mem_valid && ex_mem_memWrite) {
    dataMem.write(ex_mem_result(7, 0), ex_mem_storeVal)
  }
  val memStageResult = Mux(ex_mem_memRead, memReadData, ex_mem_result)

  mem_wb_rd       := ex_mem_rd
  mem_wb_result   := memStageResult
  mem_wb_valid    := ex_mem_valid
  mem_wb_regWrite := ex_mem_regWrite

  // ---- WB ----
  when(mem_wb_valid && mem_wb_regWrite && mem_wb_rd =/= 0.U) {
    regFile.write(mem_wb_rd, mem_wb_result)
  }

  io.debugRegOut := regFile.read(6.U)
}

object PipelineCpuMain extends App with emitrtl.Toplevel {
  lazy val topModule = new PipelineCpu
  chisel2firrtl()
  firrtl2sv()
}
