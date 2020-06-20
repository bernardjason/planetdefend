package org.bjason.game.planetdefend

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType

object Warp {

  lazy val shapeRenderer = new ShapeRenderer()
  var drawWarp = false
  lazy val centreX = Gdx.graphics.getWidth / 2
  lazy val centreY = Gdx.graphics.getHeight / 2
  var radius = 0

  def create(): Unit = {
    drawWarp = true
    radius = 10
  }

  def draw(): Unit = {
    if (drawWarp) {
      shapeRenderer.begin(ShapeType.Line)

      for (r <- radius to radius * 40 by radius) {
        shapeRenderer.setColor(Math.random().toFloat, Math.random().toFloat, Math.random().toFloat, 1)
        for( rr <- r to r + 8){
          shapeRenderer.box(centreX-rr/2, centreY-rr/2, 0, rr, rr, 1)
        }
      }
      shapeRenderer.end()
      radius = radius + 8

      if (radius > centreX / 4) drawWarp = false
    }
  }

}
