package com.github.novamage.rtw

trait MetadataProvider[A] {

  def getMetadata: A

}
