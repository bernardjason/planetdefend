package org.bjason.game.basic3d.move

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.{MathUtils, Vector2, Vector3}
import org.bjason.game.basic3d.shape.{AlienMissileShape, AlienSprite, BaseSite, Basic, MissileShape, PlayerSprite, RescueItem, RescueItemCache, TerrainInfo}
import org.bjason.game.Log._
import org.bjason.game.game._
import org.bjason.game.planetdefend.GameInfo

case class AlienMovement() extends Movement {

  val EVERY = 0.25f
  var checkEvery = EVERY
  //direction.set( (1-Math.random()*1000 % 3).toInt,-1,1-(Math.random()*1000 % 3).toInt)
  direction.set((1 - Math.random() * 1000 % 3).toInt, -1, 1 - (Math.random() * 1000 % 3).toInt)
  if (direction.x == 0) direction.x = 1
  if (direction.z == 0) direction.z = 1
  val CHANGE = 100f
  var change = CHANGE
  var angle = 0f
  var fireEvery = 0f
  val FIRE_EVERY = 20f
  val skill = 10f
  val speed = 30f
  var doubleSpeed = 1
  var iAmSuperNow = false
  var pickedUp = false
  var rescueItem: RescueItem = _

  def normalize(value: Float, min: Float, max: Float): Float = 1 - ((value - min) / (max - min))

  override def move(objects: List[Basic], me: Basic) {

    me.save()
    val spin = 2f

    fireEvery = fireEvery - Gdx.graphics.getDeltaTime
    checkEvery = checkEvery - Gdx.graphics.getDeltaTime
    change = change - Gdx.graphics.getDeltaTime

    me._rotate(0, 1, 0, spin)
    angle = angle + spin
    if (angle >= 360) angle = angle - 360
    if (angle < 0) angle = angle + 360

    if (fireEvery < 0) {
      fireAtPlayer(me)
    }

    if (!pickedUp) {
      moveTowardRescue(me)
    } else {
      moveUp(me)
    }

  }


  private def moveTowardRescue(me: Basic): Unit = {
    var nearestDst = Float.MaxValue
    var target: Option[RescueItem] = None
    //for (r <- rescueItems.filter( x => ! x.available && ! x.delivered )) {
    for (r <- rescueItems.filter(x => x.canIBePickedUpByBaddies)) {
      val d = r.position.dst(me.position)
      if (d < nearestDst) {
        target = Some(r)
        nearestDst = d
      }
    }
    target.map { t =>
      val b4 = direction.cpy
      direction.set(t.position).sub(me.position).add(0, RescueItemCache.height + 5, 0)
      direction.nor()
      if (b4.y < 0 && direction.y > 0 && me.position.y > 0) {
        direction.set(0, -1, 0)
      }
      val applySpeed = direction.cpy.scl(Gdx.graphics.getDeltaTime * speed * doubleSpeed)
      me._trn(applySpeed)
    } orElse {
      direction.set(0, 1, 0)
      val applySpeed = direction.cpy.scl(Gdx.graphics.getDeltaTime * speed * doubleSpeed)
      me._trn(applySpeed)
      None
    }
  }

  private def moveUp(me: Basic): Unit = {
    if (me.position.y <= RescueItemCache.MAXHEIGHT) {
      direction.set(0, 1, 0)
      direction.nor()
      val applySpeed = direction.cpy.scl(Gdx.graphics.getDeltaTime * speed * doubleSpeed)
      me._trn(applySpeed)
      rescueItem._trn(applySpeed)
    } else {
      val dst = me.position.dst(rescueItem.position)
      if (!rescueItem.dead && dst < RescueItemCache.height * 1.5f) {
        me.flash
        doubleSpeed = 2
        iAmSuperNow = true
        addToDead(rescueItem)
      }
      pickedUp = false
      rescueItem.alienFinishedDoingBad
    }

  }

  private def fireAtPlayer(me: Basic) = {
    val distanceAway = me.position.dst(player.position)
    if (distanceAway < TerrainInfo.terrainSize) {

      val relativeAngle = Math.toDegrees(MathUtils.atan2(me.position.z - player.position.z, me.position.x - player.position.x)).toInt

      var abs = Math.abs(relativeAngle + angle)
      if (abs > 360) abs = abs - 360
      if (abs < 0) abs = abs + 360
      screenDebug(s" between=${relativeAngle},  a=${angle.toInt} ")

      if (abs >= 89 && abs <= 91 || abs >= 269 && abs <= 271) {
        fireEvery = (FIRE_EVERY * Math.random().toFloat + skill) / doubleSpeed
        val fireAt = new Vector3(player.position).sub(me.position)
        val vv = new Vector2(fireAt.x.toInt, fireAt.z.toInt)
        vv.angle()

        debug(s"FIRE abs=${abs}  relativeAngle=${relativeAngle} vvAngle=${vv.angle()} fireAt=${fireAt.x.toInt},${fireAt.z.toInt}")
        fireAt.nor()
        val startHere = fireAt.cpy.scl(50).add(me.position)
        val am = AlienMissileShape(startHere, movement = AlienMissileMovement(startDirection = fireAt))
        addNewBasic(am)
      }
    }
  }


  override def collision(me: Basic, other: Basic): Unit = {
    other match {
      case _: MissileShape =>
        super.collision(me, other)
        addToDead(me)
        addToDead(other)
        if (pickedUp) RescueItemCache.handleRescuedItem(rescueItem)
        if (iAmSuperNow) GameInfo.hitDoubleBaddie(me.id)
        else GameInfo.hitBaddie(me.id)
      case r: RescueItem =>
        //if (!pickedUp && !r.available && !r.delivered) {
        if (!pickedUp && r.canIBePickedUpByBaddies) {
          pickedUp = true
          rescueItem = r
          rescueItem.pickedUp
          rescueItem._setTranslation(me.position)
          rescueItem._translate(0, -RescueItemCache.height, 0)
        }
      case _: AlienSprite | _: AlienMissileShape | _: BaseSite => info("Ignore others like me")
      case _: PlayerSprite =>
        super.collision(me, other)
        addToDead(me)
        if (pickedUp) RescueItemCache.handleRescuedItem(rescueItem)
        if (iAmSuperNow) GameInfo.hitDoubleBaddie(me.id)
        else GameInfo.hitBaddie(me.id)
        GameInfo.beenHit
      case _ =>
        super.collision(me, other)
        addToDead(me)
        addToDead(other)
    }
  }


}