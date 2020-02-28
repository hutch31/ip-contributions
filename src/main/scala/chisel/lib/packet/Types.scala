package chisel.lib.packet

import chisel3._
import chisel3.util._

class packetCode extends Bundle {
  val sop :: dat :: eop :: err = Enum(4)
  val code = UInt(2.W)
}

class packetData(size: Int) extends Bundle {
  val data = Vec(size, UInt(8.W))
  val count = UInt(log2Ceil(size).W)
  val code = new packetCode
}
