package com.github.novamage.rtw

trait ResultBuilder[-Input, Context, +Output] {

  def build(jsonObject: Input, context: Context): Output

}
