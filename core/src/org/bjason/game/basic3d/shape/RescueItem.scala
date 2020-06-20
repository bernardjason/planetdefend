package org.bjason.game.basic3d.shape

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g3d.attributes.{ColorAttribute, TextureAttribute}
import com.badlogic.gdx.graphics.g3d.{Material, Model}
import com.badlogic.gdx.graphics.{Color, GL20, Pixmap, Texture, VertexAttributes}
import com.badlogic.gdx.math.Vector3
import org.bjason.game.basic3d.move.{Movement, RescueMovementFloatDown, RescueMovementOnGround}
import org.bjason.game.game._
import org.bjason.game.Log._

import scala.collection.mutable.ArrayBuffer

object RescueItemCache extends Basic {

  val MAXHEIGHT = 300
  override val radius: Float = 3
  val height = radius * 7
  //lazy val texture: Texture = createBlockTexture("data/basic.jpg")
  //  lazy val genModel: Model = makeBox(texture, radius * 2)
  lazy val texureList = List(
    assets.get("data/artifact.3.png", classOf[Texture]),
    assets.get("data/artifact.2.png", classOf[Texture]),
    assets.get("data/artifact.png", classOf[Texture])
  )
  lazy val genModel = assets.get("data/artifact.g3db", classOf[Model])

  val rollbackScale = 0f

  val shape: CollideShape = null

  def dispose() {
    genModel.dispose()
    //texture.dispose()
  }


  def handleRescuedItem(rescueItem: RescueItem): Unit = {
    for (t <- currentTerrains) {
      if (t.underYou(rescueItem.position)) {
        val target = rescueItem.position.cpy
        val off = t.heightOffGround(target)
        target.y = off + RescueItemCache.height
        rescueItem.movement = RescueMovementFloatDown(target, t)
      }
    }
  }

  override val startPosition: Vector3 = null
  override var movement: Movement = _
}

case class RescueItem(startPosition: Vector3 = new Vector3, var movement: Movement) extends Basic {

  val radius: Float = RescueItemCache.radius
  lazy val texture: Texture = assets.get("data/artifact.png")
  lazy val genModel: Model = RescueItemCache.genModel
  val rollbackScale = 0f
  var texCounter = Math.random().toFloat * 1000

  private var available = false
  private var delivered = false
  private var canBaddyGoAfterMe = true

  def deliveredToBase {
    canBaddyGoAfterMe = false;
    delivered = true;
    available = false
  }

  def deliveredToGround {
    canBaddyGoAfterMe = true;
    delivered = false;
    available = true
  }

  def iCanBeRescued {
    available = true
  }

  def pickedUp {
    available = false;
    canBaddyGoAfterMe = false
  }

  def isDelivered = delivered;

  def playerGotMe: Unit = {
    canBaddyGoAfterMe = false
    movement = RescueMovementOnGround
  }

  def alienFinishedDoingBad: Unit = {
    available = true;
    canBaddyGoAfterMe = false
  }

  def canIBePickedUpByBaddies: Boolean = {
    if (delivered || canBaddyGoAfterMe == false) false
    else true
  }

  def canIRescueIt = {
    if (movement.isInstanceOf[RescueMovementFloatDown] || delivered == false && available == true &&
      (position.y >= RescueItemCache.MAXHEIGHT - RescueItemCache.height)) true
    else false
  }

  val shape: CollideShape = BulletCollideBox(radius, boundingBox, basicObj = this, fudge = new Vector3(0.4f, 0.4f, 0.4f))

  override def move(objects: List[Basic]) {

    if (!delivered) {
      val material = instance.materials.get(0)
      texCounter = texCounter + Gdx.graphics.getDeltaTime * 4
      val t = RescueItemCache.texureList(texCounter.toInt % 3)
      material.set(TextureAttribute.createDiffuse(t))
    }

    movement.move(objects, this)
  }

  override def collision(other: Basic) {
    movement.collision(this, other)
  }

  def dispose() {
    dead = true
  }

}