package org.bjason.game.basic3d.shape

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector3

case class Terrain(textureName: String, override val position: Vector3, previous: Option[Terrain] = None, last: Option[Terrain])
  extends BaseTerrain(textureName, position) {
  val blockSize: Int = TerrainInfo.blockSize
  val terrainSize: Int = TerrainInfo.terrainSize
  val terrainGroundZero: Int = 1 // -25
  val MAX = 100
  override val rollbackScale: Float = 0f


  override def setupHeightMatrix {
    previous.foreach { t =>
      val zz = 0
      for (xx <- 0 until terrainSize / blockSize) {
        matrix(zz)(xx).height = t.matrix(zz + terrainSize / blockSize - 1)(xx).height
      }
    }
    readInTerrainColourOffset

    //crater

    val workingMax = if (last.nonEmpty) MAX / 2 else MAX
    var diff = 0f

    for (xx <- 2 until terrainSize / blockSize /2) {
      val startDiff = (Math.random() * 15).asInstanceOf[Float]
      doZEntries(startDiff,xx)
    }
    /*
    diff = 0f
    for (xx <- terrainSize / blockSize - 1 to terrainSize / blockSize / 2 by -1) {
      val startDiff = (Math.random() * 10).asInstanceOf[Float]
      doZEntries(startDiff,xx)
    }
     */
    for (xx <- terrainSize / blockSize - 1 to terrainSize / blockSize / 2 by -1) {
      val opp =terrainSize / blockSize - 1
      for (zz <- 1 until terrainSize / blockSize) {
        matrix(zz)(xx).height = matrix(zz)(opp-xx).height
      }

    }


    def doZEntries(startDiff:Float,xx:Int) = {
      for (zz <- 1 until terrainSize / blockSize) {
        if (matrix(zz - 1)(xx - 1).height > terrainGroundZero + workingMax) {
          diff = -startDiff
        }
        if (matrix(zz - 1)(xx - 1).height <= terrainGroundZero) {
          diff = startDiff
        }
        if (last.nonEmpty) {
          if (zz > terrainSize / blockSize * 0.8f) {
            diff = -startDiff
          }
          if (matrix(zz - 1)(xx).height <= terrainGroundZero) diff = 0
        }
/*
        if (matrix(zz)(xx).textureOffset <= 5) {
          matrix(zz)(xx).height = matrix(zz - 1)(xx - 1).height + diff
          matrix(zz)(xx).textureOffset = (xx + zz) % 6
        } else {
          matrix(zz)(xx).height = matrix(zz - 1)(xx - 1).height
        }
 */
        matrix(zz)(xx).height = matrix(zz - 1)(xx - 1).height + diff
      }
    }


    last.foreach { t =>
      val zz = terrainSize / blockSize - 1
      for (xx <- 0 until terrainSize / blockSize) {
        matrix(zz)(xx).height = t.matrix(0)(xx).height
      }
    }
  }


  private def readInTerrainColourOffset = {
    val terrainOffsets = Gdx.files.internal("data/terrain.txt.csv").readString().
      replaceAll(",", "").replaceAll("\n", "").toCharArray
    val size = TerrainInfo.terrainSize / TerrainInfo.blockSize
    for (row <- 0 until size) {
      for (column <- 0 until size) {
        //val c = terrainOffsets(row*size + column )
        val c = terrainOffsets(column * size + row)
        matrix(row)(size - 1 - column).textureOffset = c.toInt - '0'
      }
    }
  }

  private def crater = {
    val size = (Math.random() * 1000 % (terrainSize / blockSize - 8)).toInt
    val randomX = size
    val randomZ = size
    var i = 0
    var c = 0
    for (xx <- randomX until terrainSize / blockSize - 1) {
      i = size
      c=(Math.random()*1000).toInt
      for (zz <- randomZ until terrainSize / blockSize) {
        c=c%5
        if (i > 0) {
          matrix(zz)(xx).textureOffset = 6+c
          matrix(zz)(xx).artificial = true
          c=c+1
        }
        i = i - 1
      }
    }
  }
}

object TerrainInfo {
  val blockSize: Int = 16
  val terrainSize: Int = 512
  val cellMax = terrainSize / blockSize
}