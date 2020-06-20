package org.bjason.game.basic3d.shape.explosion

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.math.collision.Ray
import com.badlogic.gdx.math.{Matrix4, Vector3}
import org.bjason.game.basic3d.move.Movement
import org.bjason.game.basic3d.shape.{Basic, Block, CollideShape}
import org.bjason.game.game.{addNewBasic, addToDead}

object Explosion extends Movement {

  override def move(objects: List[Basic], me: Basic) {

    val p = me.asInstanceOf[Particle]
    val translation = p.direction.cpy.scl(p.speed)
    p.speed = p.speed * 0.98f

    p.instance.transform.rotate(Math.random().toFloat, Math.random().toFloat, Math.random().toFloat, 10)
    p._trn(translation)
    p.ttl = p.ttl - Gdx.graphics.getDeltaTime
    if (p.ttl < 0) {
      addToDead(p)
    }
    p.instance.transform.scl(0.95f)

  }

  override def collision(me: Basic, other: Basic) {
  }

  val startSize = 3f
  val max = 30

  def create(startPosition: Vector3): Unit = {
    for (particles <- 0 to 4) {
      val p = new Particle(startPosition = startPosition, movement = this, startSize = 8) {
        ttl = 0.125f
      }
      addNewBasic(p)
    }
    for (particles <- 0 to max) {
      val p = new Particle(startPosition = startPosition, movement = this, startSize = startSize) {
      }
      addNewBasic(p)
    }
  }
}
