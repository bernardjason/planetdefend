package org.bjason.game.planetdefend

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.math.{Quaternion, Vector3}
import org.bjason.game.Log.debug
import org.bjason.game.{CareAboutKeyboard, GameKeyboard}
import org.bjason.game.basic3d.move.{MissileMovement, Movement, NoMovement, RescueMovementOnGround}
import org.bjason.game.basic3d.shape.{AlienSprite, BaseSite, Basic, MissileShape, PlayerSprite, RescueItem, RescueItemCache, TerrainInfo}
import org.bjason.game.game._
import org.bjason.game.Log._
import org.bjason.game.basic3d.shape.explosion.Explosion

case class Player(start: Vector3, cam: Camera) extends Movement {

  val radius = 6
  val MAX_SPEED = 4
  val accelerate = 15
  var speed = 0f
  direction.set(0, 0, -1)
  val position = new Vector3()
  position.set(start)
  val oldPosition = position.cpy
  var rescueItem: RescueItem = null
  val rescueItemPosition = new Vector3()
  var cameraOffX = 200
  var cameraOffXChange = 0f
  var countDownWhenNew = 3f

  GameKeyboard.listeners += spriteMove
  val sprite = PlayerSprite(position, movement = this)
  addNewBasic(sprite)
  var autoRepeatDelay = 0f

  override def move(objects: List[Basic], me: Basic) = {
    countDownWhenNew = countDownWhenNew - Gdx.graphics.getDeltaTime
    autoRepeatDelay = autoRepeatDelay - Gdx.graphics.getRawDeltaTime
    if (speed > 0) {
      val add = direction.cpy.scl(speed)
      position.add(add)
      speed = speed - Gdx.graphics.getDeltaTime * accelerate / 10
    }
    if (position.x <= 0) position.x = 0
    if (position.x >= TerrainInfo.terrainSize) position.x = TerrainInfo.terrainSize

    var hitGround = false
    for (t <- currentTerrains) {
      if (t.overMe(position)) {
        if (rescueItem != null) {
          t.hitMe(rescueItem.position, RescueItemCache.height / 2).map { tb =>
            rescueItem.deliveredToGround
            rescueItem = null
          }
        } else t.hitMe(position, radius).map { tb =>
          hitGround = true
          screenDebug(s"HIT ${tb.artificial} ")
        }
      }
    }
    if (rescueItem != null) {
      rescueItem.movement = RescueMovementOnGround // bad name...
      val dst = rescueItem.position.dst(position)
      if (dst > RescueItemCache.height) {
        rescueItemPosition.set(position).sub(0, RescueItemCache.height / 2, 0).sub(rescueItem.position)
        rescueItemPosition.scl(0.000001f)
        rescueItemPosition.nor()
        rescueItemPosition.scl(0.000001f)
        rescueItemPosition.add(rescueItem.position)
        rescueItem._setTranslation(rescueItemPosition)
      } else {
        rescueItemPosition.set(position).add(0, -RescueItemCache.height / 2, 0)
        rescueItem._setTranslation(rescueItemPosition)

      }
    }

    if (hitGround) {
      position.set(oldPosition)
      screenDebug(" HIT ")
      info(" HIT!! ", System.currentTimeMillis().toString)
    } else {
      me._setTranslation(position)
    }
    cam.position.set(position)
    cam.position.x = position.x - cameraOffX
    cam.position.y = position.y + 75
    cam.lookAt(position)
    oldPosition.set(position)
  }

  override def collision(me: Basic, other: Basic) = {
    other match {
      case r: RescueItem =>
        if (r.canIRescueIt) {
          rescueItem = r
          r.playerGotMe
          GameInfo.scoopRescueItem
        }
      case _: BaseSite =>
        position.set(oldPosition)
        position.y = position.y + 2f
      case a: AlienSprite =>
        Explosion.create(other.position)
        Warp.create()
        Sound.playHit
        GameInfo.beenHit
      case _ =>
        if (countDownWhenNew < 0) {
          Explosion.create(other.position)
          Sound.playHit
          GameInfo.beenHit
          Warp.create()
        }
    }
    debug(s"PLAYER!!! ${me} hit by ${other}")
  }

  trait Fire {

    def fire(dir: Vector3): Unit = {
      if (autoRepeatDelay <= 0) {
        autoRepeatDelay = 0.2f
        val q = new Quaternion()
        sprite._getRotation(q)
        val startHere = sprite.position.cpy()
        startHere.mulAdd(dir, 20)
        val m = MissileMovement(startDirection = dir.cpy)
        val b = MissileShape(startHere.cpy, movement = m)
        addNewBasic(b)
        Sound.playFire
      }
    }
  }

  object spriteMove extends CareAboutKeyboard with Fire {
    val rotateSpeed = 3f
    val upDownSpeed = 2f

    override def left(): Unit = {
      sprite._rotate(0, 1, 0, rotateSpeed)
      direction.rotate(rotateSpeed, 0, 1, 0)
    }

    override def right(): Unit = {
      sprite._rotate(0, 1, 0, -rotateSpeed)
      direction.rotate(-rotateSpeed, 0, 1, 0)
    }

    override def up(): Unit = {
      position.add(0, upDownSpeed, 0)
    }

    override def down(): Unit = {
      position.add(0, -upDownSpeed, 0)
    }

    override def shiftRight(): Unit = {
      cameraOffXChange = cameraOffXChange - 1
      if (cameraOffXChange <= 0) {
        cameraOffX = cameraOffX * -1
        cameraOffXChange = 10
      }
    }

    override def forward(): Unit = {
      speed = speed + Gdx.graphics.getDeltaTime * accelerate
      if (speed > MAX_SPEED) speed = MAX_SPEED
    }

    override def fire(): Unit = {
      super[Fire].fire(direction)
    }
  }

}
