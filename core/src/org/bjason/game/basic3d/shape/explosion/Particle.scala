package org.bjason.game.basic3d.shape.explosion

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g3d.{Material, Model}
import com.badlogic.gdx.graphics.{Camera, GL20, Pixmap, Texture, VertexAttributes}
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute
import com.badlogic.gdx.math.{Matrix4, Vector3}
import com.badlogic.gdx.math.collision.Ray
import org.bjason.game.Log.debug
import org.bjason.game.basic3d.move.Movement
import org.bjason.game.basic3d.shape.{Basic, Block, BulletCollideBox, CollideShape}
import org.bjason.game.game.{assets, rotate}

object ParticleCache extends Basic {

  override val radius = 2f
  //lazy val texture = createBlockTexture("data/basic.jpg")
  lazy val texture = explodeTexture()
  lazy val genModel = makeBox(texture, radius)


  def dispose() {
    genModel.dispose()
    texture.dispose()
    debug("Particle disposed")
  }

  def makeBox(texture: Texture, size: Float = 2f): Model = {
    val attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates

    val textureBlockWidth = texture.getWidth / 6
    val textureBlockHeight = texture.getHeight

    modelBuilder.begin()

    val mesh = modelBuilder.part("box", GL20.GL_TRIANGLES, attr, new Material(TextureAttribute.createDiffuse(texture)))

    val textureregion = Array(
      new TextureRegion(texture, textureBlockWidth * 0, 0, textureBlockWidth, textureBlockHeight),
      new TextureRegion(texture, textureBlockWidth * 1, 0, textureBlockWidth, textureBlockHeight),
      new TextureRegion(texture, textureBlockWidth * 2, 0, textureBlockWidth, textureBlockHeight),
      new TextureRegion(texture, textureBlockWidth * 3, 0, textureBlockWidth, textureBlockHeight),
      new TextureRegion(texture, textureBlockWidth * 4, 0, textureBlockWidth, textureBlockHeight),
      new TextureRegion(texture, textureBlockWidth * 5, 0, textureBlockWidth, textureBlockHeight))
    mesh.setUVRange(textureregion(0))
    mesh.rect(-size, -size, -size, -size, size, -size, size, size, -size, size, -size, -size, 0, 0, -1)
    mesh.setUVRange(textureregion(1))
    mesh.rect(-size, size, size, -size, -size, size, size, -size, size, size, size, size, 0, 0, 1)
    mesh.setUVRange(textureregion(2))
    mesh.rect(-size, -size, size, -size, -size, -size, size, -size, -size, size, -size, size, 0, -1, 0)
    mesh.setUVRange(textureregion(3))
    mesh.rect(-size, size, -size, -size, size, size, size, size, size, size, size, -size, 0, 1, 0)
    mesh.setUVRange(textureregion(4))
    mesh.rect(-size, -size, size, -size, size, size, -size, size, -size, -size, -size, -size, -1, 0, 0)
    mesh.setUVRange(textureregion(5))
    mesh.rect(size, -size, -size, size, size, -size, size, size, size, size, -size, size, 1, 0, 0)

    modelBuilder.end()
  }

  def explodeTexture() = {
    val pixmap = new Pixmap(48,8,Pixmap.Format.RGB888)

    val textureBlockWidth = pixmap.getWidth / 6
    val textureBlockHeight = pixmap.getHeight

    for(x <- 0 to pixmap.getWidth by textureBlockWidth) {
      pixmap.setColor(Math.random().toFloat*100, Math.random().toFloat*20,Math.random().toFloat*20,1)
      pixmap.fillRectangle(x,0,textureBlockWidth,textureBlockHeight)
    }

    val texture = new Texture(pixmap.getWidth, pixmap.getHeight, pixmap.getFormat)
    texture.draw(pixmap,0,0)
    pixmap.dispose()

    texture
  }


  override val startPosition: Vector3 = null
  override val rollbackScale: Float = 0f
  override var movement: Movement = null
  override val shape: CollideShape = null
}

case class Particle(override val startPosition: Vector3 = new Vector3, override var movement: Movement, startSize: Float) extends
  Basic {

  val direction = new Vector3(Math.random().toFloat - 0.5f, Math.random().toFloat - 0.5f, Math.random().toFloat - 0.5f)

  var speed = 3 + (Math.random() * 1000).toFloat % 3
  var ttl = 1f
  override val shape = new CollideShape {
    val radius = 0f

    override def intersects(transform: Matrix4, ray: Ray): Float = {
      Float.MaxValue
    }

    def isVisible(transform: Matrix4, cam: Camera): Boolean = true
  }
  override val rollbackScale: Float = 0f
  override val radius: Float = 1
  override val genModel: Model = ParticleCache.genModel

  override def move(objects: List[Basic]) = {
    movement.move(objects, this)
  }

  override def dispose(): Unit = {}
}

