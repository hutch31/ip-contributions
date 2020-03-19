package chisel.lib.packet

import chisel3._
import chisel3.util._

class packetCode extends Bundle {
  val sop :: dat :: eop :: err :: Nil = Enum(4)
  val code = UInt(2.W)
  def isSop() : Bool = { this.code === sop }
  def isEop() : Bool = { (this.code === eop) || (this.code === err)}
  def isGoodEop() : Bool = { this.code === eop}
  def isBadEop() : Bool = { this.code === err}
}

/** Class for carrying packetized data around within a design.  By convention this class stores
  * data in network byte order, meaning the first byte transmitted is data(0) and the last byte
  * transmitted is data(size-1).
  */
class packetData(size: Int) extends Bundle {
  val data = Vec(size, UInt(8.W))
  val count = UInt(log2Ceil(size).W)
  val code = new packetCode
}

/** Data for transmission over XGMII interface.  Data in this bundle is assumed to be in network
  * byte order, meaning that the first-transmitted byte will be in bits 63:56 and the last-transmitted
  * byte will be in bits 7:0
  *
  */
class ethXgmii extends Bundle {
  val code = UInt(8.W)
  val data = UInt(64.W)
}
