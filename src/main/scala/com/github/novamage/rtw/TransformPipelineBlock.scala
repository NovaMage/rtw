package com.github.novamage.rtw

class TransformPipelineBlock[A, B](initialValue: A)(block: A => B) extends PipelineBlock[B] {

  protected override def invokeBlock: B = block(initialValue)

  protected override val writeEnabled: Boolean = false

  protected override val readEnabled: Boolean = false

}

object TransformPipelineBlock {

  def apply[B](block: Unit => B): TransformPipelineBlock[Unit, B] = {
    new TransformPipelineBlock(())(block)
  }

}
