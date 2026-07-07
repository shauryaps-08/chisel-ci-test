package myproject
import chisel3._
import chisel3.util._

class PipelinedAccumulator(width: Int) extends Module {
  val io = IO(new Bundle {
    val in    = Flipped(Decoupled(UInt(width.W)))
    val out   = Valid(UInt(width.W))
    val flush = Input(Bool())
  })

  val acc = RegInit(0.U(width.W))

  val s1_valid = RegInit(false.B)
  val s1_data  = Reg(UInt(width.W))
  val s2_valid = RegInit(false.B)
  val s2_data  = Reg(UInt(width.W))
  val s3_valid = RegInit(false.B)
  val s3_data  = Reg(UInt(width.W))

  io.in.ready := true.B

  when(io.flush) {
    s1_valid := false.B
    s2_valid := false.B
    s3_valid := false.B
  }.otherwise {
    s1_valid := io.in.valid
    s1_data  := io.in.bits

    s2_valid := s1_valid
    s2_data  := s1_data + acc

    s3_valid := s2_valid
    s3_data  := s2_data
  }

  when(s3_valid) {
    acc := s3_data
  }

  io.out.valid := s3_valid
  io.out.bits  := s3_data
}

object PipelinedAccumulatorMain extends App with emitrtl.Toplevel {
  lazy val topModule = new PipelinedAccumulator(16)
  chisel2firrtl()
  firrtl2sv()
}
