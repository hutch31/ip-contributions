package chisel.lib.packet

import chisel3._
import chisel3.util._

class ethEncoder extends Module {
  val io = IO(new Bundle {
    val dataIn = Input(new ethXgmii)
    val dataOut = Output(UInt(66.W))
    val encodingError = Output(Bool())
  })
  val sync2 = Wire(UInt(2.W))
  val data64 = Wire(UInt(64.W))
  val dataVec = Wire(Vec(8, UInt(8.W)))
  val codeVec = Wire(Vec(8, UInt(7.W)))
  val O0 = Wire(UInt(4.W))
  val O4 = Wire(UInt(4.W))

  dataVec := io.dataIn.data.asTypeOf(dataVec).reverse
  io.encodingError := false.B

  // Encode the O(0) and O(4) ordered sets.  Since only F and 0 are used, just check for /Fsig/ value
  when (dataVec(0) === 0x5C.U) {
    O0 := 0xF.U
  }.otherwise {
    O0 := 0.U
  }
  when (dataVec(4) === 0x5C.U) {
    O4 := 0xF.U
  }.otherwise {
    O4 := 0.U
  }

  // convert data bytes to code types
  for (i <- 0 to 7) {
    when (dataVec(i) === xg_I) {
      codeVec(i) := 0.U // /I/ code
    }.elsewhen(dataVec(i) === xg_LI) {
      codeVec(i) := 6.U // LPI code
    }.elsewhen(dataVec(i) === xg_E) {
      codeVec(i) := 0x1E.U // Error
    }.elsewhen(dataVec(i) === xg_R) {
      codeVec(i) := 0x2D.U // /R/ reserved0
    }.elsewhen(dataVec(i) === 0x3C.U) {
      codeVec(i) := 0x33.U // reserved1
    }.elsewhen(dataVec(i) === xg_A) {
      codeVec(i) := 0x4B.U // /A/ reserved2
    }.elsewhen(dataVec(i) === xg_K) {
      codeVec(i) := 0x55.U // /K/ reserved3
    }.otherwise {
      codeVec(i) := 0x1E.U // encode error for unknown input values
    }
  }

  when (io.dataIn.code === 0.U) {
    sync2 := 1.U
    data64 := io.dataIn.data
  }.otherwise {
    sync2 := 2.U
    when (io.dataIn.code === 0xFF.U) {
      when (dataVec(0) === xg_T) {
        data64 := Cat(0x87.U(8.W), 0.U(7.W), codeVec(1), codeVec(2), codeVec(3), codeVec(4), codeVec(5), codeVec(6), codeVec(7))
      }.otherwise {
        data64 := Cat(0x1E.U(8.W), Cat(codeVec))
      }
    }.elsewhen (io.dataIn.code === 0xF8.U) {
      when (dataVec(4) === 0x07.U) {
        // Start of packet code
        data64 := Cat(0x33.U(8.W), codeVec(0), codeVec(1), codeVec(2), codeVec(3), 0.U(4.W), dataVec(5), dataVec(6), dataVec(7))
      }.otherwise {
        data64 := Cat(0x2D.U(8.W), codeVec(0), codeVec(1), codeVec(2), codeVec(3), O0, dataVec(5), dataVec(6), dataVec(7))
      }
    }.elsewhen (io.dataIn.code === 0x88.U) {
      when (dataVec(4) === 0x07.U) {
        data64 := Cat(0x66.U(8.W), Cat(dataVec(1), dataVec(2), dataVec(3), O0, 0.U(4.W), dataVec(5), dataVec(6), dataVec(7)))
      }.otherwise {
        data64 := Cat(0x55.U(8.W), Cat(dataVec(1), dataVec(2), dataVec(3), O0, O4, dataVec(5), dataVec(6), dataVec(7)))
      }
    }.elsewhen (io.dataIn.code === 0x80.U) {
      data64 := Cat(0x78.U(8.W), dataVec(1), dataVec(2), dataVec(3), dataVec(4), dataVec(5), dataVec(6), dataVec(7))
    }.elsewhen (io.dataIn.code === 0x8F.U) {
      data64 := Cat(0x4B.U(8.W), dataVec(1), dataVec(2), dataVec(3), O0, codeVec(4), codeVec(5), codeVec(6), codeVec(7))
    }.elsewhen (io.dataIn.code === 0x7F.U) {
      data64 := Cat(0x99.U(8.W), dataVec(0), 0.U(6.W), codeVec(2), codeVec(3), codeVec(4), codeVec(5), codeVec(6), codeVec(7))
    }.elsewhen (io.dataIn.code === 0x3F.U) {
      data64 := Cat(0xAA.U(8.W), dataVec(0), dataVec(1), 0.U(5.W), codeVec(3), codeVec(4), codeVec(5), codeVec(6), codeVec(7))
    }.elsewhen (io.dataIn.code === 0x1F.U) {
      data64 := Cat(0xB4.U(8.W), dataVec(0), dataVec(1), dataVec(2), 0.U(4.W), codeVec(4), codeVec(5), codeVec(6), codeVec(7))
    }.elsewhen (io.dataIn.code === 0x0F.U) {
      data64 := Cat(0xCC.U(8.W), dataVec(0), dataVec(1), dataVec(2), dataVec(3), 0.U(3.W), codeVec(5), codeVec(6), codeVec(7))
    }.elsewhen (io.dataIn.code === 0x07.U) {
      data64 := Cat(0xD2.U(8.W), dataVec(0), dataVec(1), dataVec(2), dataVec(3), dataVec(4), 0.U(2.W), codeVec(6), codeVec(7))
    }.elsewhen (io.dataIn.code === 0x03.U) {
      data64 := Cat(0xE1.U(8.W), dataVec(0), dataVec(1), dataVec(2), dataVec(3), dataVec(4), dataVec(5), 0.U(1.W), codeVec(7))
    }.elsewhen (io.dataIn.code === 0x01.U) {
      data64 := Cat(0xFF.U(8.W), dataVec(0), dataVec(1), dataVec(2), dataVec(3), dataVec(4), dataVec(5), dataVec(6))
    }.otherwise {
      data64 := io.dataIn.data
      io.encodingError := true.B
    }
  }
  io.dataOut := Cat(sync2, data64)
}

class ethScrambler(descram : Boolean) extends Module {
  val io = IO(new Bundle {
    val clockEn = Input(Bool())
    val dataIn = Input(UInt(66.W))
    val dataOut = Output(UInt(66.W))
  })
  val previous = RegInit(0.U(58.W))

  io.dataOut := io.dataIn ^ Cat(io.dataIn(27,0), previous(37,0)) ^ Cat(io.dataIn(7,0), previous)
  when (io.clockEn) {
    if (descram)
      previous := io.dataIn(57,0)
    else
      previous := io.dataOut(57,0)
  }
}

class ethDecoder extends Module {
  val io = IO(new Bundle {
    val dataIn = Input(UInt(66.W))
    val dataOut = Output(new ethXgmii)
    val decodingError = Output(Bool())
  })
}
