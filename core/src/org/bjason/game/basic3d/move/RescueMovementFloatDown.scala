package org.bjason.game.basic3d.move

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector3
import org.bjason.game.basic3d.shape.{AlienMissileShape, AlienSprite, BaseSite, Basic, PlayerSprite, RescueItem, RescueItemCache, Terrain}
import org.bjason.game.game.addToDead
import org.bjason.game.game._
import org.bjason.game.Log._

case class RescueMovementFloatDown(target: Vector3, terrain: Terrain) extends Movement {

  val translation = new Vector3()
  direction.set(0, -1, 0)
  val speed = 10

  override def move(objects: List[Basic], me: Basic) {

    if (me.position.y > target.y) {
      translation.set(direction).scl(Gdx.graphics.getDeltaTime() * speed)
      me._trn(translation)
      info(me.id, "Down....", me.position.y, target.y)
    } else {
      me.asInstanceOf[RescueItem].deliveredToGround
      me.movement = RescueMovementOnGround
    }

  }

  override def collision(me: Basic, other: Basic): Unit = {
    super.collision(me, other)
    other match {
      case _: AlienSprite =>
      case _: AlienMissileShape =>
      case o: RescueItem =>
        if (o.movement == RescueMovementOnGround) {
          RescueItemCache.handleRescuedItem(o)
        }
      case _: BaseSite =>
        player.rescueItem = null
        me.asInstanceOf[RescueItem].deliveredToBase
      case _: PlayerSprite =>
      case _ =>
        addToDead(me)
        addToDead(other)
        rescueItems -= me.asInstanceOf[RescueItem]
    }
  }
}