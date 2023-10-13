package com.github.novamage.rtw

import com.github.novamage.typedmap.TypedMap

trait FailedResultBuilder[+A] {

  def buildFailure(metadata: TypedMap): A

}
