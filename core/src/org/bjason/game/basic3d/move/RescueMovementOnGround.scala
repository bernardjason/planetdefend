package org.bjason.game.basic3d.move

import org.bjason.game.basic3d.shape.{AlienMissileShape, AlienSprite, BaseSite, Basic, PlayerSprite, RescueItem, RescueItemCache}
import org.bjason.game.game._
import org.bjason.game.planetdefend.GameInfo

object RescueMovementOnGround extends Movement {

  override def move(objects:List[Basic],me:Basic) {
    
  }

  override def collision(me: Basic, other: Basic): Unit = {
    super.collision(me, other)
    other match {
      case _:AlienSprite =>
      case _:AlienMissileShape =>
      case _:PlayerSprite =>
        RescueItemCache.handleRescuedItem(me.asInstanceOf[RescueItem])
      case o:RescueItem =>
        if ( o.movement == RescueMovementOnGround) {
          RescueItemCache.handleRescuedItem(o)
        }
      case _:BaseSite =>
        GameInfo.rescueItem
        player.rescueItem = null
        me.asInstanceOf[RescueItem].deliveredToBase
      case _ =>
        addToDead(me)
        addToDead(other)
        rescueItems -= me.asInstanceOf[RescueItem]

    }
  }
}