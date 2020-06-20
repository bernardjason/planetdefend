package org.bjason.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.badlogic.gdx.graphics.g2d.{Animation, SpriteBatch, TextureAtlas}
import com.badlogic.gdx.graphics.{Color, GL20, Texture}

class StartScreen extends LoopTrait {
  private[game] var batch: SpriteBatch = null

  import com.badlogic.gdx.graphics.g2d.SpriteBatch

  lazy val spriteBatch = new SpriteBatch
  var stateTime = 0f
  lazy val ascii = DrawAscii(titleSize = 23, normalSize = 64)
  lazy val titleascii = DrawAscii(titleSize = 23)
  val bernSoftHeight = 250
  lazy val bernsoft = List(
    new Texture(ascii.getPixmapForString("BERNIESOFT", 1280, bernSoftHeight, Color.YELLOW)),
    new Texture(ascii.getPixmapForString("BERNIESOFT", 1280, bernSoftHeight, Color.RED)),
    new Texture(ascii.getPixmapForString("BERNIESOFT", 1280, bernSoftHeight, Color.BLUE))
  )
  lazy val title = new Texture(titleascii.getPixmapForString("PlanetDefend", 1280, bernSoftHeight, Color.CYAN))

  val backgroundColour = Color.BLACK //new Color(0,0,0.3f,1)

  lazy val instructions = ascii.getRegularTextAsTexture(
    words = "    arrow keys up,down,rotate \n" +
      "    left shift thrust \n" +
      "    right shift alt camera view \n" +
      "    space to fire\n" +
      "    escape end\n" +
      "    GOOD LUCK!!", 1024, 400, backgroundColour)
  var mainTitleShow = -1f
  var mainTitley = 0f
  var instructionsy = 0f
  val SHOW_MAIN_TITLE = 0.5f //2
  var delayMoveOn = 0
  //lazy val demo= new Animation[TextureRegion](0.033f, atlas.findRegions("running"), PlayMode.LOOP)
  var demo: Option[Animation[TextureAtlas.AtlasRegion]] = None
  var loadRenderAfterAFewScreenDraws = 2

  override def create(): Unit = {
    Gdx.input.setInputProcessor(GameKeyboard)
    batch = new SpriteBatch()
    instructionsy = -instructions.getHeight


  }

  override def firstScreenSetup(): Unit = {
    super.firstScreenSetup()
    mainTitleShow = 0
    mainTitley = 0
    instructionsy = -instructions.getHeight
    delayMoveOn = 60
  }

  override def render(): Unit = {

    loadDemoDataAfterFirstRender

    delayMoveOn = delayMoveOn - 1
    if (delayMoveOn < 0 && GameKeyboard.fire) {
      this.screenComplete = true
    }

    if (GameKeyboard.escape) {
      System.exit(0)
    }

    Gdx.gl.glClearColor(backgroundColour.r, backgroundColour.g, backgroundColour.b, 1)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
    batch.begin()
    var y = mainTitley
    for (t <- bernsoft) {
      batch.draw(t, 0, y)
      y = y + bernSoftHeight
    }
    batch.draw(title, 100, instructionsy + 200)
    batch.draw(instructions, Gdx.graphics.getWidth / 2 - instructions.getWidth / 2, instructionsy - 100)
    batch.end()

    mainTitleShow = mainTitleShow + 1
    val speed = 3

    if (mainTitleShow > SHOW_MAIN_TITLE) {
      mainTitley = mainTitley + speed
      if (instructionsy <= Gdx.graphics.getHeight * 0.3f) {
        instructionsy = instructionsy + speed
      } else {
        playVideo
      }
    }

  }

  private def loadDemoDataAfterFirstRender = {
    if (loadRenderAfterAFewScreenDraws == 0) {
      val atlas = new TextureAtlas(Gdx.files.internal("data/packed/demo.atlas"))
      demo = Some(new Animation(0.2f, atlas.getRegions, PlayMode.LOOP))
    }

    loadRenderAfterAFewScreenDraws = loadRenderAfterAFewScreenDraws - 1
  }

  private def playVideo = {
    demo.foreach { d =>
      stateTime = stateTime + Gdx.graphics.getDeltaTime
      val currentFrame: TextureAtlas.AtlasRegion = d.getKeyFrame(stateTime, true)
      currentFrame.setRegion(0,100,1480,700) // miss out score
      spriteBatch.begin
      spriteBatch.draw(currentFrame, 740, 100, 740, 350)
      spriteBatch.end
    }
  }

  override def dispose(): Unit = {
    batch.dispose()
    spriteBatch.dispose()
  }
}


