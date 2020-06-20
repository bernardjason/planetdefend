package org.bjason.game.basic3d.shape

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute
import com.badlogic.gdx.graphics.g3d.{Material, Model}
import com.badlogic.gdx.graphics.{GL20, Pixmap, Texture, VertexAttributes}
import com.badlogic.gdx.math.Vector3
import org.bjason.game.basic3d.move.Movement
import org.bjason.game.game._
import org.bjason.game.Log._

object MissileShapePool extends Basic {
  val radius: Float = 4f
  lazy val texture = createBlockTexture("data/missile.jpg")
  lazy val genModel = makeBox(texture, radius/2 )

  def makeBox(texture: Texture, size: Float = 2f): Model = {
    val attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates;

    val textureBlockWidth = texture.getWidth / 6
    val textureBlockHeight = texture.getHeight

    modelBuilder.begin();

    val mesh = modelBuilder.part("box", GL20.GL_TRIANGLES, attr, new Material(TextureAttribute.createDiffuse(texture)));

    val textureregion = Array(
      new TextureRegion(texture, textureBlockWidth * 0, 0, textureBlockWidth, textureBlockHeight),
      new TextureRegion(texture, textureBlockWidth * 1, 0, textureBlockWidth, textureBlockHeight),
      new TextureRegion(texture, textureBlockWidth * 2, 0, textureBlockWidth, textureBlockHeight),
      new TextureRegion(texture, textureBlockWidth * 3, 0, textureBlockWidth, textureBlockHeight),
      new TextureRegion(texture, textureBlockWidth * 4, 0, textureBlockWidth, textureBlockHeight),
      new TextureRegion(texture, textureBlockWidth * 5, 0, textureBlockWidth, textureBlockHeight))
    mesh.setUVRange(textureregion(0));
    mesh.rect(-size, -size, -size, -size, size, -size, size, size, -size, size, -size, -size, 0, 0, -1);
    mesh.setUVRange(textureregion(1));
    mesh.rect(-size, size, size, -size, -size, size, size, -size, size, size, size, size, 0, 0, 1);
    mesh.setUVRange(textureregion(2));
    mesh.rect(-size, -size, size, -size, -size, -size, size, -size, -size, size, -size, size, 0, -1, 0);
    mesh.setUVRange(textureregion(3));
    mesh.rect(-size, size, -size, -size, size, size, size, size, size, size, size, -size, 0, 1, 0);
    mesh.setUVRange(textureregion(4));
    mesh.rect(-size, -size, size, -size, size, size, -size, size, -size, -size, -size, -size, -1, 0, 0);
    mesh.setUVRange(textureregion(5));
    mesh.rect(size, -size, -size, size, size, -size, size, size, size, size, -size, size, 1, 0, 0);

    modelBuilder.end();
  }

  /**
   * creates a texture for use in this class that has had the list of regions rotated to match expectation.
   *  imagine a jpg with a list of 6 areas, 1 2 3 4 5 6.    1,5 & 6 are okay, but the others are wrong way up.
   * @param textureName which is expected to be a loaded asset
   * @return
   */
  def createBlockTexture(textureName: String) = {
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
  override val rollbackScale: Float = -1
  override var movement: Movement = null
  override val shape: CollideShape = null

  override def dispose(): Unit = {
    genModel.dispose()
  }
}
case class MissileShape(startPosition: Vector3 = new Vector3, radius: Float = 4f, var movement: Movement ,
                        override val id:Int = Basic.getId) extends Basic  {

  lazy val texture = MissileShapePool.texture
  lazy val genModel = MissileShapePool.genModel
  val rollbackScale= 0f


  lazy val shape: CollideShape = BulletCollideBox(radius,boundingBox,basicObj=this) //,fudge = new Vector3(0.7f, 0.7f, 0.7f))

  override def move(objects: List[Basic]) = {
    movement.move(objects, this)
  }

  override def collision(other: Basic) {

    movement.collision(this,other)
  }

  def dispose() {
    debug("MissileShape dispose not needed")
    //genModel.dispose();
  }



}
