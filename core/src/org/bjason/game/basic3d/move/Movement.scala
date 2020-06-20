package org.bjason.game.basic3d.move

import org.bjason.game.basic3d.shape.Basic
import com.badlogic.gdx.math.Vector3
import org.bjason.game.Log._

trait Movement {
  val direction = new Vector3
  def move(objects: List[Basic], me: Basic) = {

  }

  def collision(me: Basic,other:Basic) = {
    debug(s"${me} hit by ${other}")
  }
}