package org.bjason.game.basic3d.shape

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.math.Vector3
import org.bjason.game.Log.debug
import org.bjason.game.basic3d.move.{AlienMovement, Movement}
import org.bjason.game.game._

object AlienSpritePool {
  lazy val genModel = assets.get("data/alien.g3db", classOf[Model])
}

case class AlienSprite(startPosition: Vector3 = new Vector3, radius: Float = 40f,
                       override val id: Int = Basic.getId) extends Basic {

  val alientMove = AlienMovement()
  var movement:Movement = alientMove

  lazy val genModel = AlienSpritePool.genModel

  val rollbackScale = -2f
  var texCounter = 0f;

  lazy val shape: CollideShape = BulletCollideBox(radius, boundingBox, basicObj = this, fudge = new Vector3(0.1f, 0.65f, 0.4f))

  override def move(objects: List[Basic]) = {


    super.move(objects)

    if (alientMove.iAmSuperNow) {
      val material = instance.materials.get(0)
      material.set(ColorAttribute.createDiffuse(Color.RED));
    }

    movement.move(objects, this)
  }

  val none = None

  override def collision(other: Basic) {
    movement.collision(this, other)
  }

  def dispose(): Unit = {
    debug("No need to dispose")
  }


}

