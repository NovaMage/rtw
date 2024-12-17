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

  def intoWriteStage[B](block: A => B)(using OperationContextProvider): PipelineBlock[B] = {
    new WritePipelineBlock(execute)(block)
  }

  def intoTransformStage[B](block: A => B)(using OperationContextProvider): PipelineBlock[B] = {
    new TransformPipelineBlock(execute)(block)
  }

  def build[B, C](using MetadataProvider[C], ResultBuilder[A, C, B], OperationContextProvider): B = {
    buildTargetActionWithProvidersAndExecute(identity)
  }

  def build[B, C, D](
    converter: A => B
  )(using MetadataProvider[D], ResultBuilder[B, D, C], OperationContextProvider): C = {

    buildTargetActionWithProvidersAndExecute(converter)
  }

  def execute(using OperationContextProvider): A = {
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

  private def buildTargetActionWithProvidersAndExecute[C, B, D](
    converter: A => B
  )(using MetadataProvider[D], ResultBuilder[B, D, C], OperationContextProvider): C = {
    executeTargetActionInOperationContext {
      buildTargetAction(converter, summon, summon)
    }
  }

  // noinspection ScalaWeakerAccess
  protected def executeTargetActionInOperationContext[B](
    targetAction: => B
  )(using operationContextProvider: OperationContextProvider): B = {
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
  given Conversion[PipelineBlock[_], FalliblePipelineBlock[_]] = _.asFallible
}
