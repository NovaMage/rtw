package com.github.novamage.rtw

class ReadPipelineBlock[A, B](inputValue: A)(block: A => B) extends PipelineBlock[B] {

  protected override def invokeBlock: B = block(inputValue)

  protected override val writeEnabled: Boolean = false

  protected override val readEnabled: Boolean = true
}

class UnitReadPipelineBlock[A, B](block: => B) extends PipelineBlock[B] {

  protected override def invokeBlock: B = block

  protected override val writeEnabled: Boolean = false

  protected override val readEnabled: Boolean = true
}
