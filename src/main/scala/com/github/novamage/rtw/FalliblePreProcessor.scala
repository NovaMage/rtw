package com.github.novamage.rtw

trait FalliblePreProcessor[A, B, C] {

  def tryPreProcessing(using C): Either[B, A]

  def writeEnabled: Boolean

  def readEnabled: Boolean

}
