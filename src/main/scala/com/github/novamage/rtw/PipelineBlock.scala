package com.github.novamage.rtw

import scala.language.implicitConversions

trait PipelineBlock[+A] {

  protected def invokeBlock: A

  protected def writeEnabled: Boolean

  protected def readEnabled: Boolean

  def asFallible: FalliblePipelineBlock[A] = new FalliblePipelineBlock[A] {

    protected override def invokeBlock: A = PipelineBlock.this.invokeBlock

    protected override def writeEnabled: Boolean = PipelineBlock.this.writeEnabled

    protected override def readEnabled: Boolean = PipelineBlock.this.readEnabled

  }

  def intoWriteStage[B](
    block: A => B
  )(implicit operationContextProvider: OperationContextProvider): PipelineBlock[B] = {
    new WritePipelineBlock(execute)(block)
  }

  def intoTransformStage[B](
    block: A => B
  )(implicit operationContextProvider: OperationContextProvider): PipelineBlock[B] = {
    new TransformPipelineBlock(execute)(block)
  }

  def build[B, C](
    implicit metadataProvider: MetadataProvider[C],
    resultBuilder: ResultBuilder[A, C, B],
    operationContextProvider: OperationContextProvider
  ): B = {
    buildTargetActionWithProvidersAndExecute(identity)
  }

  def build[B, C, D](converter: A => B)(
    implicit metadataProvider: MetadataProvider[D],
    resultBuilder: ResultBuilder[B, D, C],
    operationContextProvider: OperationContextProvider
  ): C = {

    buildTargetActionWithProvidersAndExecute(converter)
  }

  def execute(implicit operationContextProvider: OperationContextProvider): A = {
    executeTargetActionInOperationContext {
      invokeBlock
    }
  }

  private def buildTargetAction[B, C, D](
    converter: A => B,
    metadataProvider: MetadataProvider[D],
    resultBuilder: ResultBuilder[B, D, C]
  ): C = {
    resultBuilder.build(converter(invokeBlock), metadataProvider.getMetadata)
  }

  private def buildTargetActionWithProvidersAndExecute[C, B, D](converter: A => B)(
    implicit metadataProvider: MetadataProvider[D],
    resultBuilder: ResultBuilder[B, D, C],
    operationContextProvider: OperationContextProvider
  ): C = {
    executeTargetActionInOperationContext {
      buildTargetAction(converter, metadataProvider, resultBuilder)
    }
  }

  protected def executeTargetActionInOperationContext[B](
    targetAction: => B
  )(implicit operationContextProvider: OperationContextProvider): B = {
    if (writeEnabled) {
      operationContextProvider.withinWriteContext {
        targetAction
      }
    } else if (readEnabled) {
      operationContextProvider.withinReadContext {
        targetAction
      }
    } else {
      targetAction
    }
  }

}

object PipelineBlock {
  implicit def asFallible[A](pipelineBlock: PipelineBlock[A]): FalliblePipelineBlock[A] = pipelineBlock.asFallible
}
