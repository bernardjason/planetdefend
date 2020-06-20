package org.bjason.game.planetdefend

import com.badlogic.gdx.Gdx

object Sound {


  lazy private val comeback = Gdx.audio.newMusic(Gdx.files.internal("data/comeback.wav"))
  lazy private val fire = Gdx.audio.newMusic(Gdx.files.internal("data/fire.wav"))
  lazy private val hit = Gdx.audio.newMusic(Gdx.files.internal("data/explosion.wav"))

  def create = {
  }

  def playFire  {
    fire.play()
  }
  def playHit = {
    hit.play()
  }
  def playLiveAnotherDay = {
    comeback.play
  }
}


