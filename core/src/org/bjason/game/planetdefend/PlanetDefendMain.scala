package org.bjason.game.planetdefend

import java.io.IOException

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.environment.{DirectionalLight, DirectionalShadowLight}
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider
import com.badlogic.gdx.graphics.g3d.{Environment, Model, ModelBatch}
import com.badlogic.gdx.graphics._
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.ScreenUtils
import org.bjason.game.Log._
import org.bjason.game.basic3d.move.RescueMovementOnGround
import org.bjason.game.basic3d.shape._
import org.bjason.game.game._
import org.bjason.game.{DrawAscii, GameKeyboard, LoopTrait}

import scala.collection.mutable.ArrayBuffer

class PlanetDefendMain extends LoopTrait {

  lazy val environment = new Environment()
  lazy val cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight())
  lazy val modelBatch = new ModelBatch()

  import com.badlogic.gdx.graphics.g2d.SpriteBatch

  lazy val spriteBatch = new SpriteBatch
  lazy val ascii = DrawAscii(normalSize = 64, flip = false)

  implicit val objects: ArrayBuffer[Basic] = ArrayBuffer()
  //noinspection ScalaDeprecation
  lazy val shadowLight = new DirectionalShadowLight(2048, 2048, 1060f, 1460f, .1f, 550f)
  lazy val shadowBatch = new ModelBatch(new DepthShaderProvider())

  lazy val starTexture = assets.get("data/stars.png", classOf[Texture])
  lazy val starTextureWidth = starTexture.getWidth

  private var checkBadiesEvery = 0
  private var checkRescueStateEvery = 0

  private val HIGH = 1000

  private val groundTextureName = "data/basictex.jpg"
  val myAssets: Array[scala.Tuple2[String, Class[_]]] = Array(
    ("data/basic.jpg", classOf[Pixmap]),
    ("data/stars.png", classOf[Texture]),
    ("data/missile.jpg", classOf[Pixmap]),
    ("data/baseground.jpg", classOf[Pixmap]),
    ("data/ground.png", classOf[Texture]),
    (groundTextureName, classOf[Texture]),
    ("data/alien.g3db", classOf[Model]),
    ("data/artifact.png", classOf[Texture]),
    ("data/artifact.2.png", classOf[Texture]),
    ("data/artifact.3.png", classOf[Texture]),
    ("data/artifact.g3db", classOf[Model]),
    ("data/invade.g3db", classOf[Model])
  )


  def cameraEnvironment() {
    environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.8f, 0.8f, 1f))
    environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f))

    environment.add(shadowLight.set(0.8f, 0.8f, 0.8f, -0f, -0.8f, -0.1f))
    environment.shadowMap = shadowLight
  }

  override def firstScreenSetup(): Unit = {

    GameKeyboard.reset()

    GameInfo.reset

    var zz = -MAX_TERRAIN
    for (t <- currentTerrains) {
      t._setToTranslation(new Vector3(0, 0, zz))
      t.init
      zz = zz + TerrainInfo.terrainSize
    }

    createNewPlayer()

    addBaseToRescueTo(HIGH)

    newScreen

    cam.position.set(0, 0, 0)
    cam.lookAt(0, 0, 50)
    cam.near = 1f
    cam.far = 800
    cam.update()
    doNewList

    Warp.create()
  }

  def newScreen: Unit = {
    info("**************** NEW SCREEN ******************")
    objects.foreach(f = o =>
      o match {
        case r: RescueItem => addToDead(r)
        case a: AlienSprite => addToDead(a)
        case _ =>
      }
    )

    doDeadList

    addItemsToRescue(HIGH)
    makeSureWeHaveEnoughBadies(true)

  }

  private def makeSureWeHaveEnoughBadies(force: Boolean = false) = {
    checkBadiesEvery = checkBadiesEvery + 1
    if (checkBadiesEvery % 777 == 0 || force) {
      var currently = objects.count(_.isInstanceOf[AlienSprite])
      while (currently <= GameInfo.MAXBADDIES) {
        val multiple = (Math.random() * 1000).toInt % 3 - 1
        val z = (Math.random() * 10000).toFloat % MAX_TERRAIN * multiple
        val a = AlienSprite(new Vector3(100, 200, z))
        addNewBasic(a)
        currently = currently + 1
        info(s"Added baddie to tally ${currently}")
      }
    }
  }

  private def numberOfRescuesToDo: Int = {
    checkRescueStateEvery = checkRescueStateEvery + 1
    var todo = 9999
    if (checkRescueStateEvery % 666 == 0) {
      todo = 0
      objects.foreach(f = r => {
        r match {
          case r: RescueItem if !r.dead && !r.isDelivered => todo = todo + 1
          case _ =>
        }
      })
    }
    todo
  }

  private def createNewPlayer(start: Vector3 = new Vector3(0, 100, 0)) = {
    player = Player(start.cpy, cam)
  }


  private def addItemsToRescue(HIGH: Int) = {
    rescueItems.clear()
    for (t <- currentTerrains) {
      info(" DOING ", t)
      val zz = t.position.z + TerrainInfo.blockSize + (Math.random().toFloat * 1000) % TerrainInfo.terrainSize * 0.40f
      val rescueWhere = new Vector3(TerrainInfo.terrainSize / 2, HIGH, zz)
      rescueWhere.x = TerrainInfo.terrainSize * 0.8f * Math.random().toFloat
      val off = t.heightOffGround(rescueWhere)
      rescueWhere.y = off + RescueItemCache.height / 2
      val rescue = RescueItem(startPosition = rescueWhere, movement = RescueMovementOnGround)
      addNewBasic(rescue)
      rescueItems += rescue
    }
    info("ADDED ", rescueItems.length, " terrain ", currentTerrains.length)
  }

  private def addBaseToRescueTo(HIGH: Int): Unit = {
    for (zz <- -MAX_TERRAIN to MAX_TERRAIN - TerrainInfo.terrainSize by TerrainInfo.terrainSize) {
      var added = false
      for (t <- currentTerrains) {
        val putHere = new Vector3(TerrainInfo.terrainSize / 2, HIGH, zz + TerrainInfo.terrainSize / 4 * 3)
        putHere.x = TerrainInfo.terrainSize * 0.8f * Math.random().toFloat
        if (t.overMe(putHere) && !added) {
          val off = t.heightOffGround(putHere)
          putHere.y = off + BaseSiteCache.height
          putHere.add(0, 0, BaseSiteCache.radius)
          val base = BaseSite(startPosition = putHere)
          addNewBasic(base)
          added = true
        }
      }
    }
  }

  def clearUp(): Unit = {
    objects.filter(o => !o.isInstanceOf[Terrain]).foreach { o =>
      addToDead(o)
      info("Clear up ", o)
    }
    doDeadList
    doBulletDispose()
    info("Object list now", objects.length)
  }

  private val MAX_TERRAIN: Int = TerrainInfo.terrainSize * 4

  override def create() {
    loadAssetsForMe(myAssets)
    Gdx.input.setInputProcessor(GameKeyboard)
    cameraEnvironment()
    waitForAssets


    val terrains: ArrayBuffer[Terrain] = new ArrayBuffer[Terrain]()
    var first: Option[Terrain] = None
    if (1 == 1) {
      var c = 1
      for (zz <- -MAX_TERRAIN until MAX_TERRAIN - TerrainInfo.terrainSize by TerrainInfo.terrainSize) {
        val another = Terrain(groundTextureName, new Vector3(0, 0, zz), first, None)
        terrains += another
        first = Some(another)
        c = c + 1


      }

      val zz = MAX_TERRAIN - TerrainInfo.terrainSize - 1
      val another = Terrain(groundTextureName, new Vector3(0, 0, zz), first, Some(terrains.head))
      terrains += another


    } else {
      val another = Terrain(groundTextureName, new Vector3(0, 0, 0), None, None)
      terrains += another

    }
    currentTerrains = terrains


    for (t <- currentTerrains) {
      addNewBasic(t)
    }
    doNewList
  }

  private def sidewaysScroll: Unit = {
    for (t <- currentTerrains) {
      val diff = (t.startPosition.z + TerrainInfo.terrainSize / 2 - player.position.z).toInt
      if (player.direction.z > 0 && diff <= -MAX_TERRAIN) {
        t._translate(0, 0, MAX_TERRAIN * 2)
        t.init
      }
      if (player.direction.z < 0 && diff >= MAX_TERRAIN) {
        t._translate(0, 0, -MAX_TERRAIN * 2)
        t.init
      }
    }
    objects.filter(o => !o.isInstanceOf[Terrain]).foreach {
      o =>
        val diff = (o.position.z - player.position.z).toInt
        //if (o.movement.direction.z < 0 && diff <= -MAX_TERRAIN) {
        if (diff <= -MAX_TERRAIN) {
          o._trn(0, 0, MAX_TERRAIN * 2)
          debug(s"was scrolled ${o.position} right")
          screenDebug(s"${o} scrolled")
          debug(s"is  scrolled ${o.position} right")
        }
        //if (o.movement.direction.z > 0 && diff >= MAX_TERRAIN) {
        if (diff >= MAX_TERRAIN) {
          debug(s"was scrolled ${o.position} left")
          o._trn(0, 0, -MAX_TERRAIN * 2)
          screenDebug(s"${o} scrolled")
          debug(s"is  scrolled ${o.position} left")
        }

    }
  }


  override def render() {

    screenDebugReset

    GameKeyboard.render()

    cam.update()

    makeSureWeHaveEnoughBadies()

    for (o <- objects) o.move(objects.toList)


    collisionWorld.performDiscreteCollisionDetection()

    if (GameInfo.didPlayerJustGetHit) {
      addToDead(player.sprite)
      createNewPlayer(player.position.add(0, 40, 0).scl(0, 1, 1))
    }
    GameInfo.process

    Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth, Gdx.graphics.getHeight)
    Gdx.gl.glClearColor(0, 0, 0, 1)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT)

    doTheStars

    doShadow()
    modelBatch.begin(cam)
    for (o <- objects) {
      if (o.display) {
        modelBatch.render(o.instance, environment)
      }
    }
    modelBatch.end()

    spriteBatch.begin()
    ascii.normalFont.draw(spriteBatch, s"Score:${GameInfo.score} lives:${GameInfo.lives} level:${GameInfo.level} ${screenDebugGet} fps ${Gdx.graphics.getFramesPerSecond}", 0, Gdx.graphics.getHeight)
    spriteBatch.end()

    //recordScreen()

    doDeadList
    doNewList
    doBulletDispose

    if (numberOfRescuesToDo == 0) {
      GameInfo.newLevel
      newScreen
    }

    if (GameKeyboard.escape || GameInfo.lives <= 0) {
      clearUp()
      this.screenComplete = true
    }
    sidewaysScroll

    Warp.draw()

  }

  var filename = 0
  var every = 0


  def recordScreen(): Unit = {
    every = every + 1
    if (every % 30 == 0) {
      val pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, Gdx.graphics.getWidth, Gdx.graphics.getHeight)

      writePNG(Gdx.files.absolute(s"data/record/pixmap_${filename}.png"), pixmap)
      filename = filename + 1
    }
  }

  def writePNG(file: FileHandle, pixmap: Pixmap): Unit = {
    try {
      val writer = new PixmapIO.PNG((pixmap.getWidth * pixmap.getHeight * 1.5f).toInt) // Guess at deflated size.
      try {
        writer.setFlipY(true)
        writer.write(file, pixmap)
      } finally writer.dispose()
    } catch {
      case ex: IOException =>
        println("Error writing PNG: " + file, ex)
    }
  }


  private def doTheStars = {
    spriteBatch.begin()
    val skyOff =
      if (player.direction.len() < 0)
        player.position.z % starTextureWidth
      else
        -player.position.z % starTextureWidth
    for (xx <- -MAX_TERRAIN + starTextureWidth to MAX_TERRAIN - starTextureWidth by starTextureWidth) {
      spriteBatch.draw(starTexture, xx + skyOff, Gdx.graphics.getHeight / 2)
    }
    spriteBatch.end()
  }

  val tempPosition = new Vector3

  def doShadow() = {
    player.sprite.instance.transform.getTranslation(tempPosition)
    tempPosition.y = tempPosition.y - 200
    shadowLight.begin(tempPosition, cam.direction)
    shadowBatch.begin(shadowLight.getCamera())
    objects.filter(!_.isInstanceOf[Terrain]).map(o => shadowBatch.render(o.instance))
    shadowBatch.end()
    shadowLight.end()
  }


  override def dispose(): Unit = {
    super.dispose()
    modelBatch.dispose()
  }

}
