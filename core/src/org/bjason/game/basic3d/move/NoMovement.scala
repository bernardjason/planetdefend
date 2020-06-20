package org.bjason.game.basic3d.move

import org.bjason.game.basic3d.shape.{Basic, MissileShape}
import org.bjason.game.game.addToDead

object NoMovement extends Movement {
  
  override def move(objects:List[Basic],me:Basic) {
    
  }

  override def collision(me: Basic, other: Basic): Unit = {
    super.collision(me, other)
    addToDead(me)
    addToDead(other)
  }
}