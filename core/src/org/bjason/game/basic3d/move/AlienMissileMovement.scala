package org.bjason.game.basic3d.move

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector3
import org.bjason.game.Log.info
import org.bjason.game.basic3d.shape.{AlienMissileShape, AlienSprite, Basic, MissileShape, PlayerSprite}
import org.bjason.game.game._

case class AlienMissileMovement(startDirection:Vector3, speed: Float = 130f) extends Movement {

  direction.set(startDirection)
  private val translation = new Vector3

  var ttl = 6f

  override def move(objects: List[Basic], me: Basic) {

    me.save()

    translation.set(direction)
    translation.scl(Gdx.graphics.getDeltaTime() * speed )

    me._translate(translation)
    ttl = ttl - Gdx.graphics.getDeltaTime
    if ( ttl < 0 ) {
      addToDead(me)
    }
  }

  override def collision(me: Basic, other: Basic): Unit = {
    other match {
      case p:PlayerSprite =>
        addToDead(me)
      case _:AlienSprite | _:AlienMissileShape => info("Ignore others like me")
      case _  =>
        //super.collision(me, other)
        addToDead(me)
        //addToDead(other)
    }
  }


}