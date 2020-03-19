package chisel.lib

import chisel3._

package object packet {
  val xg_T = 0xFD.U(8.W)
  val xg_I = 0x07.U(8.W)
  val xg_LI = 0x06.U(8.W)
  val xg_S = 0xFB.U(8.W)
  val xg_E = 0xFE.U(8.W)
  val xg_Q = 0x9C.U(8.W)
  val xg_R = 0x1C.U(8.W)
  val xg_A = 0x7C.U(8.W)
  val xg_K = 0xBC.U(8.W)
}
