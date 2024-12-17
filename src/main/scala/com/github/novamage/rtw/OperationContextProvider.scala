package com.github.novamage.rtw

trait OperationContextProvider {

  given operationContextProvider: OperationContextProvider = this

  def withinWriteContext[A](block: => A): A

  def withinReadContext[A](block: => A): A

}
