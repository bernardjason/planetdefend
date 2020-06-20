package org.bjason.game.basic3d.shape

import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.math.Vector3
import org.bjason.game.Log.debug
import org.bjason.game.basic3d.move.Movement
import org.bjason.game.game._

object PlayerSpritePool {
  lazy val genModel = assets.get("data/invade.g3db", classOf[Model])
}
case class PlayerSprite(startPosition: Vector3 = new Vector3, radius: Float = 8f, var movement: Movement, override val id: Int = Basic.getId) extends Basic {

  lazy val genModel = PlayerSpritePool.genModel

  val rollbackScale = -2f

  lazy val shape: CollideShape = BulletCollideBox(radius, boundingBox, basicObj = this, fudge = new Vector3(0.1f, 0.65f, 0.4f))

  override def move(objects: List[Basic]) = {
    super.move(objects)
    movement.move(objects, this)
  }

  val none = None

  override def collision(other: Basic) {
    movement.collision(this,other)
  }

  def dispose(): Unit = {
    debug("No need to dispose")
  }


}

