package com.github.novamage.rtw

trait FalliblePreProcessor[A, B, C] {

  def tryPreProcessing(implicit context: C): Either[B, A]

  def writeEnabled: Boolean

  def readEnabled: Boolean

}
