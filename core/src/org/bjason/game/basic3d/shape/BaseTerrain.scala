package org.bjason.game.basic3d.shape

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g3d.{Material, Model}
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute
import com.badlogic.gdx.graphics.{Camera, GL20, Texture, VertexAttributes}
import com.badlogic.gdx.math.{Intersector, Matrix4, Vector3}
import com.badlogic.gdx.math.collision.Ray
import org.bjason.game.Log._
import org.bjason.game.basic3d.move.{Movement, NoMovement}
import org.bjason.game.game._


abstract class BaseTerrain(textureName: String, override val position: Vector3) extends Basic {

  val startPosition: Vector3 = new Vector3
  val radius: Float = 0f
  var movement: Movement = NoMovement
  val terrainGroundZero: Int
  val terrainSize: Int
  val blockSize: Int
  val expectShades = 6
  case class TerrainBlock(var height:Float=0, var textureOffset:Int=terrainGroundZero, var artificial:Boolean=false)
  var c=0
  lazy val matrix:Array[Array[TerrainBlock]] =
    Array.fill[TerrainBlock](terrainSize / blockSize, terrainSize / blockSize){c=c+1 ; TerrainBlock(textureOffset = c%6)}

  lazy val texture = assets.get(textureName, classOf[Texture])
  lazy val genModel = makeGround(texture)

  override def init {
    startPosition.set(-terrainSize / 4, 0, -terrainSize / 2)
    startPosition.add(position)
  }

  override def move(objects: List[Basic]) = {
    movement.move(objects, this)
  }

  override def collision(other: Basic) {
    movement.collision(this, other)
  }

  def dispose(): Unit = {
    if (ifDisposedOf) {
      genModel.dispose()
      texture.dispose()
      debug("Texture disposed")
    }
  }

  val shape = new CollideShape {
    val radius = 0f

    override def intersects(transform: Matrix4, ray: Ray): Float = {
      Float.MaxValue
    }

    override def isVisible(transform: Matrix4, cam: Camera): Boolean = true
  }

  val rayDown = new Vector3(0, -1, 0)
  val intersection = new Vector3
  val adjPos = new Vector3
  val adjPosBug = new Vector3

  def heightOffGround(what:Vector3): Float = {
    val xx = what.x - position.x
    val zz = what.z - position.z
    adjPos.x = xx
    adjPos.y = what.y
    adjPos.z = zz
    adjPosBug.x = xx
    adjPosBug.y = what.y-1
    adjPosBug.z = zz

    var bx = ((xx - 2) / blockSize).toInt
    var bz = ((zz - 2) / blockSize).toInt
    if (bx < 0) bx = 0
    if (bz < 0) bz = 0
    if (bx >= TerrainInfo.cellMax) bx = TerrainInfo.cellMax - 1
    if (bz >= TerrainInfo.cellMax) bz = TerrainInfo.cellMax - 1

    val offx = if (bx == TerrainInfo.cellMax - 1) 0 else 1
    val offz = if (bz == TerrainInfo.cellMax - 1) 0 else 1
    matrix(bz + offz)(bx + offx).height
  }

  var lastDst = 0f;
  def hitMe(what: Vector3, radius: Float): Option[TerrainBlock] = {

    val xx = what.x - position.x
    val zz = what.z - position.z
    adjPos.x = xx
    adjPos.y = what.y
    adjPos.z = zz
    adjPosBug.x = xx
    adjPosBug.y = what.y-1
    adjPosBug.z = zz

    var bx = ((xx - 2) / blockSize).toInt
    var bz = ((zz - 2) / blockSize).toInt
    if (bx < 0) bx = 0
    if (bz < 0) bz = 0
    if (bx >= TerrainInfo.cellMax) bx = TerrainInfo.cellMax - 1
    if (bz >= TerrainInfo.cellMax) bz = TerrainInfo.cellMax - 1

    if (bx <= TerrainInfo.cellMax && bz <= TerrainInfo.cellMax && bx >= 0 && bz >= 0) {

      val offx = if (bx == TerrainInfo.cellMax - 1) 0 else 1
      val offz = if (bz == TerrainInfo.cellMax - 1) 0 else 1
      val ray = new Ray(adjPos, rayDown)
      val rayBug = new Ray(adjPosBug, rayDown)
      val points = Array((bx) * blockSize, matrix(bz)(bx).height, (bz) * blockSize,
        (bx + 1) * blockSize, matrix(bz + offz)(bx + offx).height, (bz + 1) * blockSize,
        (bx + 1) * blockSize, matrix(bz)(bx + offx).height, (bz) * blockSize,

        (bx) * blockSize, matrix(bz + offz)(bx).height, (bz + 1) * blockSize,
        (bx + 1) * blockSize, matrix(bz + offz)(bx + offx).height, (bz + 1) * blockSize,
        (bx) * blockSize, matrix(bz)(bx).height, (bz) * blockSize)


      Intersector.intersectRayTriangles(ray, points, intersection)
      val dst = adjPos.dst(intersection)
      Intersector.intersectRayTriangles(rayBug, points, intersection)
      val dstBug = adjPosBug.dst(intersection)

      if (dst < radius ) {
        debug(s"!!!hit ${dst}" + matrix(bz)(bx))
        //return true
        return Some(matrix(bz)(bx))
      }

      if (dstBug > dst ) {
        debug(s"!!!bug hit ${dst} ${dstBug}" + matrix(bz)(bx))
        //return true
        return Some(matrix(bz)(bx))
      }

      //debug(s"dst ${dst}" + matrix(bz)(bx))

    } else {
      info(s"Terrain.cellMax=${TerrainInfo.cellMax} what=${what}")
      info(s"what=${what.x.toInt},${what.z.toInt}  xx,zz= ${xx.toInt},${zz.toInt} bx=${bx},bz=${bz}  ")
      //info(" more info", s" ${Common.terrain}, ${xx.toInt}, ${zz.toInt}, ${originalStartPosition}, ${position}")
      System.exit(0)
    }
    None
  }

  def makeGround(texture: Texture): Model = {
    val attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates;

    val textureBlockHeight = texture.getHeight
    //val textureBlockWidth = texture.getWidth / textureBlockHeight
    val textureBlockWidth = textureBlockHeight

    modelBuilder.begin();

    val mesh = modelBuilder.part("box", GL20.GL_TRIANGLES, attr, new Material(TextureAttribute.createDiffuse(texture)));


    val textureregion = for( i <- 0 until texture.getWidth/64 ) yield new TextureRegion(texture, textureBlockWidth * i, 0, textureBlockWidth, textureBlockHeight)

    setupHeightMatrix

    val c0 = new Vector3
    val c1 = new Vector3
    val c2 = new Vector3
    val c3 = new Vector3
    val normal = new Vector3(1, 0, 0)

    for (zz <- 0 until (terrainSize) / blockSize) {
      for (xx <- 0 until (terrainSize) / blockSize) {
        val x = xx * blockSize
        val z = zz * blockSize

        val nextx = if (xx == terrainSize / blockSize - 1) 0 else 1
        val nextz = if (zz == terrainSize / blockSize - 1) 0 else 1
        c0.set(x, matrix(zz)(xx).height, z)
        c1.set(x + blockSize, matrix(zz)(xx + nextx).height, z)
        c2.set(x, matrix(zz + nextz)(xx).height, z + blockSize)
        c3.set(x + blockSize, matrix(zz + nextz)(xx + nextx).height, z + blockSize)
        //mesh.setUVRange(textureregion(c))
        mesh.setUVRange(textureregion(matrix(zz)(xx).textureOffset ))
        mesh.rect(c0, c2, c3, c1, normal)

      }
    }
    modelBuilder.end
  }


  def setupHeightMatrix() {

  }

  def underYou(where: Vector3): Boolean = {
    if (
    startPosition.z - TerrainInfo.terrainSize/2  < where.z &&
      startPosition.z + TerrainInfo.terrainSize/2 > where.z
    ) {
      true
    } else {
      false
    }
  }

  def overMe(where: Vector3): Boolean = {
    //println(where,position)
    if (
      //startPosition.x <= where.x &&
        //startPosition.x + TerrainInfo.terrainSize >= where.x &&
        startPosition.z - TerrainInfo.terrainSize*0.25f < where.z &&
        startPosition.z + TerrainInfo.terrainSize*2f > where.z
    ) {
      true
    } else {
      false
    }
  }
}