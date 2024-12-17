package com.github.novamage.rtw

import com.github.novamage.typedmap.TypedMap

trait ReadTransformWritePipeline[Context <: TypedMap] extends OperationContextProvider {

  def onReadStage[B]()(block: => B): PipelineBlock[B] = new ReadPipelineBlock[Unit, B](())(_ => block)

  def onReadStage[A, B](value: A)(block: A => B): PipelineBlock[B] = new ReadPipelineBlock[A, B](value)(block)

  def onReadStage[A, B, C](
    processor: FalliblePreProcessor[A, B, Context]
  )(block: A => C)(using MetadataProvider[Context]): FalliblePipelineBlock[C] = {
    obtainResultFromPreProcessorInOperationContext(processor) match {
      case Right(value) => new FallibleReadPipelineBlock(value)(block)
      case Left(error)  => buildFailedBlock(error)
    }
  }

  def onWriteStage[A]()(block: => A): PipelineBlock[A] = new WritePipelineBlock[Unit, A](())(_ => block)

  def onWriteStage[A, B](value: A)(block: A => B): PipelineBlock[B] = new WritePipelineBlock[A, B](value)(block)

  def onWriteStage[A, B, C](
    processor: FalliblePreProcessor[A, B, Context]
  )(block: A => C)(using MetadataProvider[Context]): FalliblePipelineBlock[C] = {
    obtainResultFromPreProcessorInOperationContext(processor) match {
      case Right(value) =>
        new FallibleWritePipelineBlock(value)(block)
      case Left(error) => buildFailedBlock(error)
    }
  }

  def onTransformStage[A]()(block: => A): PipelineBlock[A] = new TransformPipelineBlock[Unit, A](())(_ => block)

  def onTransformStage[A, B](value: A)(block: A => B): PipelineBlock[B] = new TransformPipelineBlock[A, B](value)(block)

  def onTransformStage[A, B, C](
    processor: FalliblePreProcessor[A, B, Context]
  )(block: A => C)(using MetadataProvider[Context]): FalliblePipelineBlock[C] = {
    obtainResultFromPreProcessorInOperationContext(processor) match {
      case Right(value) =>
        new FallibleTransformPipelineBlock(value)(block)
      case Left(error) => buildFailedBlock(error)
    }
  }

  def onNestedStage[A, B, C](
    processor: FalliblePreProcessor[A, B, Context]
  )(block: A => FalliblePipelineBlock[C])(using MetadataProvider[Context]): FalliblePipelineBlock[C] = {
    obtainResultFromPreProcessorInOperationContext(processor) match {
      case Right(value) => block(value)
      case Left(error)  => buildFailedBlock(error)
    }
  }

  private def obtainResultFromPreProcessorInOperationContext[A, B](
    processor: FalliblePreProcessor[A, B, Context]
  )(using metadataProvider: MetadataProvider[Context]): Either[B, A] = {
    given context: Context = metadataProvider.getMetadata
    if (processor.writeEnabled) {
      withinWriteContext {
        processor.tryPreProcessing
      }
    } else if (processor.readEnabled) {
      withinReadContext {
        processor.tryPreProcessing
      }
    } else {
      processor.tryPreProcessing
    }
  }

  private def buildFailedBlock(
    error: Any
  )(using metadataProvider: MetadataProvider[Context]): FailedPipelineBlock[Nothing] = {
    val metadata            = metadataProvider.getMetadata
    val metadataWithFailure = metadata.+(RTWMetadataKeys.FailedPipelineBlockErrorValue -> error)
    new FailedPipelineBlock(metadataWithFailure)
  }

}
