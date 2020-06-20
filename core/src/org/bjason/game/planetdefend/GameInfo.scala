package org.bjason.game.planetdefend

import org.bjason.game.basic3d.shape.Basic

import scala.collection.mutable


object GameInfo {

  var score=0
  var lives=0
  var level = 0
  private var maxbaddies = 0

  def reset = {
    score = 0
    lives = 5
    maxbaddies = 3
    level = 1
  }


  private var playerHit=false
  private var scooped = false
  private var delivered = false
  private val doubleBaddieHitList = mutable.Map[Int,Int]()
  private val baddieHitList = mutable.Map[Int,Int]()

  def MAXBADDIES() = maxbaddies

  def didPlayerJustGetHit = playerHit

  def newLevel = {
    maxbaddies = maxbaddies+2
    level=level+1
  }

  def process: Unit = {
    if ( playerHit ) {
      lives=lives -1
      if ( lives > 0 ) Sound.playLiveAnotherDay
    }

    if ( scooped ) score =score + 1000
    if ( delivered ) score =score + 5000
    for(i <- doubleBaddieHitList ) score = score + 500
    for(i <- baddieHitList ) score = score + 100

    playerHit=false
    scooped=false
    delivered=false
    doubleBaddieHitList.clear()
    baddieHitList.clear()
  }

  def finished:Boolean = if( lives <=0 ) true else false

  def beenHit = playerHit=true

  def scoopRescueItem = scooped=true

  def rescueItem = delivered = true

  def hitDoubleBaddie(id:Int) =  doubleBaddieHitList.put(id,id)

  def hitBaddie(id:Int) =  baddieHitList.put(id,id)
}
