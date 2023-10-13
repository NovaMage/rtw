package com.github.novamage.rtw

import com.github.novamage.typedmap.TypedMap

class FailedPipelineBlock[A](metadataWithFailure: TypedMap) extends FalliblePipelineBlock[A] {

  protected override def invokeBlock: A = throw new UnsupportedOperationException(
    "Can not invoke block on pipeline blocks that have already failed"
  )

  protected override val writeEnabled: Boolean = false

  protected override val readEnabled: Boolean = false

  protected override def failed: Boolean = true

  protected override def failedMetadata: TypedMap = metadataWithFailure

}
