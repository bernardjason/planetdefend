package org.bjason.game.basic3d.shape

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute
import com.badlogic.gdx.graphics.g3d.{Material, Model}
import com.badlogic.gdx.graphics.{Color, GL20, Pixmap, Texture, VertexAttributes}
import com.badlogic.gdx.math.Vector3
import org.bjason.game.basic3d.move.Movement
import org.bjason.game.game._
import org.bjason.game.Log._

object AlienMissileShapePool extends Basic {
  val radius: Float = 4f
  lazy val texture = createBlockTexture()
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

  def createBlockTexture() = {
    val pixmap = new Pixmap(48,8,Pixmap.Format.RGB888)

    pixmap.setColor(Color.RED)
    pixmap.fill()

    val texture = new Texture(pixmap.getWidth, pixmap.getHeight, pixmap.getFormat)
    texture.draw(pixmap,0,0)
    pixmap.dispose()

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
case class AlienMissileShape(startPosition: Vector3 = new Vector3, radius: Float = 4f, var movement: Movement ,
                        override val id:Int = Basic.getId) extends Basic  {

  lazy val texture = AlienMissileShapePool.texture
  lazy val genModel = AlienMissileShapePool.genModel
  val rollbackScale= 0f


  lazy val shape: CollideShape = BulletCollideBox(radius,boundingBox,basicObj=this) //,fudge = new Vector3(0.7f, 0.7f, 0.7f))

  override def move(objects: List[Basic]) = {
    movement.move(objects, this)
  }

  override def collision(other: Basic) {

    movement.collision(this,other)
  }

  def dispose() {
    debug("AlienMissileShape dispose not needed")
    //genModel.dispose();
  }



}
