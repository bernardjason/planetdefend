package org.bjason.game.basic3d.move

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector3
import org.bjason.game.basic3d.shape.explosion.Explosion
import org.bjason.game.basic3d.shape.{AlienSprite, Basic, MissileShape, PlayerSprite}
import org.bjason.game.game._
import org.bjason.game.planetdefend.Sound

case class MissileMovement(startDirection:Vector3,speed: Float = 500f) extends Movement {

  direction.set(startDirection)
  private val translation = new Vector3

  var ttl = 2f

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
      case m:MissileShape =>
      case p:PlayerSprite =>
      case _  =>
        super.collision(me, other)
        addToDead(me)
        addToDead(other)
        Explosion.create(other.position)
        Sound.playHit
    }
  }


}