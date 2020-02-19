package chisel.lib.dclib

import chisel3._
import chisel3.iotesters.PeekPokeTester

class DCInputOutputTestbench extends Module {
  val io = IO(new Bundle {
    val srcPat = Input(UInt(16.W))
    val dstPat = Input(UInt(16.W))
    val colorError = Output(Bool())
    val seqError = Output(Bool())
    val okCount = Output(UInt(32.W))
  })

  val src = Module(new ColorSource(1, 16))
  val dst = Module(new ColorSink(1, 16))

  src.io.pattern := io.srcPat
  dst.io.pattern := io.dstPat
  io.colorError := dst.io.colorError
  io.seqError := dst.io.seqError

  src.io.color := 0.U
  dst.io.color := 0.U

  src.io.enable := true.B
  dst.io.enable := true.B

  dst.io.c <> DCOutput(DCInput(src.io.p))
  io.okCount := dst.io.okCount
}
