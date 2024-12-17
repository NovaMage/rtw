package com.github.novamage.rtw

import com.github.novamage.typedmap.TypedMap

trait FalliblePipelineBlock[+A] {

  def intoWriteStage[B](block: A => B)(using OperationContextProvider): FalliblePipelineBlock[B] = {
    if (this.failed) {
      new FailedPipelineBlock[B](this.failedMetadata)
    } else {
      new FallibleWritePipelineBlock(wrappedPipelineBlock.execute)(block)
    }
  }

  def intoTransformStage[B](block: A => B)(using OperationContextProvider): FalliblePipelineBlock[B] = {
    if (this.failed) {
      new FailedPipelineBlock[B](this.failedMetadata)
    } else {
      new FallibleTransformPipelineBlock(wrappedPipelineBlock.execute)(block)
    }
  }

  def map[B](converter: A => B): FalliblePipelineBlock[B] = new FalliblePipelineBlock[B] {

    protected override def invokeBlock: B = {
      converter(FalliblePipelineBlock.this.invokeBlock)
    }

    protected override def writeEnabled: Boolean = FalliblePipelineBlock.this.writeEnabled

    protected override def readEnabled: Boolean = FalliblePipelineBlock.this.readEnabled

  }

  protected def invokeBlock: A

  protected def writeEnabled: Boolean

  protected def readEnabled: Boolean

  protected def failed: Boolean = false

  protected def failedMetadata: TypedMap = TypedMap.empty

  // noinspection ScalaWeakerAccess
  protected final def metadataAtTimeOfFailure: TypedMap = if (failed) {
    failedMetadata
  } else {
    throw new UnsupportedOperationException("Can not retrieve failure metadata for a block that hasn't failed  yet")
  }

  private def wrappedPipelineBlock: PipelineBlock[A] = new PipelineBlock[A] {

    protected override def invokeBlock: A = FalliblePipelineBlock.this.invokeBlock

    protected override def writeEnabled: Boolean = FalliblePipelineBlock.this.writeEnabled

    protected override def readEnabled: Boolean = FalliblePipelineBlock.this.readEnabled

  }

  private def wrapBuildOrExecuteFailed[B](block: => B)(using failedResultBuilder: FailedResultBuilder[B]): B = {
    if (failed) {
      failedResultBuilder.buildFailure(metadataAtTimeOfFailure)
    } else {
      block
    }
  }

  def build[B, C](
    using MetadataProvider[C],
    ResultBuilder[A, C, B],
    FailedResultBuilder[B],
    OperationContextProvider
  ): B = {
    wrapBuildOrExecuteFailed {
      wrappedPipelineBlock.build
    }
  }

  def build[B, C, D](
    converter: A => B
  )(using MetadataProvider[D], ResultBuilder[B, D, C], FailedResultBuilder[C], OperationContextProvider): C = {
    wrapBuildOrExecuteFailed {
      wrappedPipelineBlock.build(converter)
    }
  }

}
