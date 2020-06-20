package org.bjason.game

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.{Color, GL20, Texture}
import com.badlogic.gdx.{Gdx, Input}
import org.bjason.game.planetdefend.GameInfo

class EndScreen extends LoopTrait {
  private[game] var batch:SpriteBatch = null
  lazy val ascii = DrawAscii(titleSize = 27,normalSize = 64)

  lazy val score = new Texture(ascii.getPixmapForString("SCORE",1624,200,Color.YELLOW))
  lazy val value = new Texture(ascii.getPixmapForString(s"${GameInfo.score}",1624,200,Color.YELLOW))
  lazy val instructions = ascii.getRegularTextAsTexture(
    words = "    space to play again\n\n"+
            "    escape to end",
       1024, 200,Color.BLACK )

  var delayMoveOn = 0


  override def create(): Unit = {
    batch = new SpriteBatch()
  }

  override def firstScreenSetup(): Unit = {
    super.firstScreenSetup()
    delayMoveOn = 60
  }



  override def render(): Unit = {
    delayMoveOn = delayMoveOn -1
    if ( delayMoveOn < 0 &&  GameKeyboard.fire ) {
      this.screenComplete = true
    }
    if ( delayMoveOn < 0 && GameKeyboard.escape ) {
      System.exit(0)
    }

    Gdx.gl.glClearColor(0.0f, 0, 0, 1)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
    batch.begin()
    batch.draw(score, 64, 500)
    batch.draw(value, 64, 300)
    batch.draw(instructions, 64,100)
    batch.end()
  }

  override def dispose(): Unit = {
    batch.dispose()
  }
}


