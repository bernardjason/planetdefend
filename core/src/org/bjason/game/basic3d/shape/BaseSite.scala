package org.bjason.game.basic3d.shape

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute
import com.badlogic.gdx.graphics.g3d.{Material, Model}
import com.badlogic.gdx.graphics.{GL20, Pixmap, Texture, VertexAttributes}
import com.badlogic.gdx.math.Vector3
import org.bjason.game.basic3d.move.{Movement, NoMovement}
import org.bjason.game.game._

object BaseSiteCache extends Basic {

  override val radius: Float =15
  val height=10
  lazy val texture:Texture= createBlockTexture("data/baseground.jpg")
  lazy val genModel: Model = makeBox(texture, radius*2 )
  val rollbackScale= 0f

  val shape: CollideShape = null

  def dispose() {
      genModel.dispose()
      texture.dispose()
  }

  def makeBox(texture: Texture, size: Float = 2f): Model = {
    val attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates

    val textureBlockWidth = texture.getWidth / 6
    val textureBlockHeight = texture.getHeight
    val downHeight = -height *2

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
    mesh.rect(-size, downHeight, -size, -size, height/2, -size, size, height/2, -size, size, downHeight, -size, 0, 0, -1)
    mesh.setUVRange(textureregion(1))
    mesh.rect(-size, height/2, size, -size, downHeight, size, size, downHeight, size, size, height/2, size, 0, 0, 1)
    mesh.setUVRange(textureregion(2))
    mesh.rect(-size, downHeight, size, -size, downHeight, -size, size, downHeight, -size, size, downHeight, size, 0, -1, 0)
    mesh.setUVRange(textureregion(3))
    mesh.rect(-size, height/2, -size, -size, height/2, size, size, height/2, size, size, height/2, -size, 0, 1, 0)
    mesh.setUVRange(textureregion(4))
    mesh.rect(-size, downHeight, size, -size, height/2, size, -size, height/2, -size, -size, downHeight, -size, -1, 0, 0)
    mesh.setUVRange(textureregion(5))
    mesh.rect(size, downHeight, -size, size, height/2, -size, size, height/2, size, size, downHeight, size, 1, 0, 0)

    modelBuilder.end()
  }

  /**
   * creates a texture for use in this class that has had the list of regions rotated to match expectation.
   *  imagine a jpg with a list of 6 areas, 1 2 3 4 5 6.    1,5 & 6 are okay, but the others are wrong way up.
   * @param textureName which is expected to be a loaded asset
   * @return
   */
  def createBlockTexture(textureName: String) :Texture = {
    val pixmap = assets.get(textureName, classOf[Pixmap])

    val textureBlockWidth = pixmap.getWidth / 6
    val textureBlockHeight = pixmap.getHeight

    val texture = new Texture(pixmap.getWidth, pixmap.getHeight, pixmap.getFormat)

    val rototeAngle = Array(0, 180, 0, 0, 0, 0)
    for (i <- 0 to 5) {
      val pixmapX = i * textureBlockWidth
      val rotated = rotate(pixmap, rototeAngle(i), pixmapX, 0, textureBlockWidth, textureBlockHeight)
      texture.draw(rotated, pixmapX, 0)
      rotated.dispose()
    }
    texture
  }

  override val startPosition: Vector3 = null
  override var movement: Movement = _
}

case class BaseSite( startPosition: Vector3 = new Vector3 ) extends Basic {

  var movement: Movement = NoMovement

  val radius: Float = BaseSiteCache.radius
  lazy val texture: Texture = BaseSiteCache.texture
  lazy val genModel: Model = BaseSiteCache.genModel
  val rollbackScale= 0f
  var taken = false

  val shape: CollideShape = BulletCollideBox(radius, boundingBox, basicObj = this,fudge = new Vector3(0.6f,0.3f,0.6f))

  override def move(objects: List[Basic])  {
    movement.move(objects, this)
  }

  override def collision(other: Basic) {
    other match {
      case _:RescueItem =>
      case _:PlayerSprite => other.collision(this)
      case _:AlienSprite =>
      case _ => movement.collision(this,other)
    }
  }

  def dispose() {

  }

}