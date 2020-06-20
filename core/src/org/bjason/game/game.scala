package org.bjason.game

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.bullet.Bullet
import com.badlogic.gdx.physics.bullet.collision.{btCollisionDispatcher, btCollisionWorld, btDbvtBroadphase, btDefaultCollisionConfiguration}
import org.bjason.game.basic3d.shape.{Basic, CollideShape, MyContactListener, RescueItem, Terrain}

import scala.collection.mutable.ArrayBuffer
import org.bjason.game.Log._
import org.bjason.game.planetdefend.Player

package object game {

  Bullet.init(false, true)
  val collisionConfig = new btDefaultCollisionConfiguration()
  val dispatcher = new btCollisionDispatcher(collisionConfig)
  val broadphase = new btDbvtBroadphase()
  val collisionWorld = new btCollisionWorld(dispatcher, broadphase, collisionConfig)
  val contactListener = new MyContactListener()

  private val deadlist: ArrayBuffer[Basic] = ArrayBuffer()
  private val newlist: ArrayBuffer[Basic] = ArrayBuffer()
  private val bulletlist: ArrayBuffer[CollideShape] = ArrayBuffer()

  val rescueItems:ArrayBuffer[RescueItem] = ArrayBuffer()

  var player: Player = _

  val assets = new AssetManager

  var currentTerrains: IndexedSeq[Terrain] = null

  val debugScreen = new StringBuilder()

  def loadAssetsForMe(list: Array[scala.Tuple2[String, Class[_]]]) {

    for (l <- list) {
      assets.load(l._1, l._2)
      Log.info(s"loading ${l._1} ${l._2}")
    }
    assets.update()

    //assets.finishLoading()
  }

  def waitForAssets = {
    assets.finishLoading()
  }

  def addNewBasic(basic: Basic) {
    newlist += basic
  }

  def addToDead(remove: Basic) {
    deadlist += remove
  }

  def addBulletObjectToDispose(bulletObject: CollideShape) {
    bulletlist += bulletObject
  }


  def doDeadList(implicit objects: ArrayBuffer[Basic]) {
    for (d <- deadlist) {
      if (d != null ) {
        objects -= d
        addBulletObjectToDispose(d.shape)
        d.dispose
      } else {
        //println("d was null ",d)
        //System.exit(0)
      }
    }
    deadlist.clear()
  }

  def doBulletDispose() {
    for (b <- bulletlist) {
      b.dispose
    }
    bulletlist.clear
    collisionWorld.release()
  }


  def doNewList(implicit objects: ArrayBuffer[Basic]) {
    for (b <- newlist) {
      b.init
      b.reset
      objects += b
    }
    newlist.clear()
  }

  def rotate(src: Pixmap, angle: Float, srcX: Int, srcY: Int, width: Int, height: Int) = {

    val rotated = new Pixmap(width, height, src.getFormat());

    val radians = Math.toRadians(angle)
    val cos = Math.cos(radians)
    val sin = Math.sin(radians);

    if (angle != 0) {
      for (x <- 0f to width.asInstanceOf[Float] by 1) {
        for (y <- 0f to height by 1) {

          val centerx = width / 2
          val centery = height / 2
          val m = x - centerx
          val n = y - centery
          val j = (m * cos + n * sin) + centerx
          val k = (n * cos - m * sin) + centery
          if (j >= 0 && j < width && k >= 0 && k < height) {
            var pixel = src.getPixel((k + srcX).asInstanceOf[Int], (j + srcY).asInstanceOf[Int])
            rotated.drawPixel(width - x.asInstanceOf[Int], y.asInstanceOf[Int], pixel);
          }
        }
      }
    } else {
      rotated.drawPixmap(src, 0, 0, srcX, srcY, width, height)
    }
    rotated;
  }
}
