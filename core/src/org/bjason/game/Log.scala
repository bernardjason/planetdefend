package org.bjason.game

import com.badlogic.gdx.{Application, Gdx}

object Log {

  var tag:String  = "planetdefend"

  val when = java.time.LocalDateTime.now()

  def info(in: Any*)  {
    if ( Gdx.app.getLogLevel >= Application.LOG_INFO ) {
      val message = new StringBuilder
      for (s <- in) {
        if (s != null) message.append(s.toString()).append(" ")
      }
      if (Gdx.app != null) Gdx.app.log(tag, message.toString)
      else println(message.toString)
    }
  }
  private val screenBuffer = new StringBuilder()
  def screenDebugReset = screenBuffer.clear()

  def screenDebugGet = {
    if ( Gdx.app.getLogLevel >= Application.LOG_DEBUG ) {
      screenBuffer.toString()
    } else {
      ""
    }
  }
  def screenDebug  (in: Any*): Unit =  {
    if ( Gdx.app.getLogLevel >= Application.LOG_DEBUG ){
      for (s <- in) {
        if (s != null) screenBuffer.append(s.toString()).append(" ")
      }
    }
  }

  def debug  (in: Any*)  {
    if ( Gdx.app.getLogLevel >= Application.LOG_DEBUG ) {
      val message = new StringBuilder
      for (s <- in) {
        if (s != null) message.append(s.toString()).append(" ")
      }
      val o = s"(D) ${when} ${message.toString} \n"
      if (Gdx.app != null) Gdx.app.debug(tag, message.toString)
    }
  }
}
