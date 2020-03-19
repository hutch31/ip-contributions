package chisel.lib.packet

import chisel3._
import chiseltest._
import org.scalatest._
import chiseltest.experimental.TestOptionBuilder._
import chiseltest.internal.WriteVcdAnnotation

import scala.util.Random

class EthEncoderTester extends FlatSpec with ChiselScalatestTester with Matchers {
  behavior of "Testers2"

  it should "send data without errors" in {
    test(new ethEncoder) {
      c => {
        c.io.dataIn.code.poke(0.U)
        c.io.dataIn.data.poke(0.U)
        c.clock.step(1)
        println(c.io.dataOut.peek())
      }
    }
  }
}

class scramDescram extends Module {
  val io = IO(new Bundle {
    val dataIn = Input(UInt(66.W))
    val dataOut= Output(UInt(66.W))
    val notEqual = Output(Bool())
  })
  val scram = Module(new ethScrambler(false))
  val descram = Module(new ethScrambler(true))

  scram.io.clockEn := true.B
  descram.io.clockEn := true.B
  scram.io.dataIn := io.dataIn
  descram.io.dataIn := scram.io.dataOut
  io.dataOut := descram.io.dataOut
  io.notEqual := (io.dataIn =/= io.dataOut)
}

class EthScramTester extends FlatSpec with ChiselScalatestTester with Matchers {
  behavior of "Testers2"

  it should "scramble and descramble" in {
    test(new scramDescram).withAnnotations(Seq(WriteVcdAnnotation)) {
      c => {
        for (i <- 0 to 1000) {
          c.io.dataIn.poke(i.U)
          c.clock.step(1)
          c.io.dataOut.expect(i.U)
          c.io.notEqual.expect(false.B)
        }
      }
    }
  }
}
