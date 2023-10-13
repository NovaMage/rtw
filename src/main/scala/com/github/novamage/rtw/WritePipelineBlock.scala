package com.github.novamage.rtw

class WritePipelineBlock[A, B](inputValue: A)(block: A => B) extends PipelineBlock[B] {

  protected override def invokeBlock: B = block(inputValue)

  protected override val writeEnabled: Boolean = true

  protected override val readEnabled: Boolean = true
}
