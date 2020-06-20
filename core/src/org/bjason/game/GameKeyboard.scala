package org.bjason.game

import com.badlogic.gdx.{Input, InputAdapter}

import scala.collection.mutable.ArrayBuffer

object GameKeyboard extends InputAdapter {

  val listeners = new ArrayBuffer[CareAboutKeyboard]()

  var left = false
  var right = false
  var up = false
  var down = false
  var shift_left = false
  var shift_right = false
  var fire = false
  var escape = false

  def reset(): Unit = {
    listeners.clear
    left = false
    right = false
    up = false
    down = false
    shift_left = false
    shift_right = false
    fire = false
    escape = false
  }

  def render(): Unit = {
    for (l <- listeners) {
      if (shift_left) l.forward()
      if (shift_right) l.shiftRight()
      if (left) l.left()
      if (right) l.right()
      if (up  ) l.up()
      if (down  ) l.down()
      if (fire) l.fire()
    }
  }

  def change(keyCode: Int, to: Boolean): Unit = {
    if (keyCode == Input.Keys.SHIFT_LEFT) shift_left = to
    if (keyCode == Input.Keys.SHIFT_RIGHT) shift_right = to
    if (keyCode == Input.Keys.LEFT) left = to
    if (keyCode == Input.Keys.RIGHT) right = to
    if (keyCode == Input.Keys.UP) up = to
    if (keyCode == Input.Keys.DOWN) down = to
    if (keyCode == Input.Keys.SPACE) fire = to
    if (keyCode == Input.Keys.ESCAPE) escape = to

  }

  override def keyDown(keyCode: Int): Boolean = {
    super.keyDown(keyCode)
    change(keyCode, true)
    true
  }

  override def keyUp(keyCode: Int): Boolean = {
    super.keyUp(keyCode)
    change(keyCode, false)
    true
  }

  override def touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean = {
    fire = true
    true
  }
}
