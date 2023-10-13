package com.github.novamage.rtw

class FallibleReadPipelineBlock[A, B](inputValue: A)(block: A => B) extends FalliblePipelineBlock[B] {

  protected override def invokeBlock: B = block(inputValue)

  protected override val writeEnabled: Boolean = false

  protected override val readEnabled: Boolean = true

}
