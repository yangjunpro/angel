package com.singlab.angel

import breeze.linalg.Matrix

private[angel] abstract class AngelMessage extends Serializable

private[angel] case class PSRegister() extends AngelMessage

private[angel] case class WorkerRegister() extends AngelMessage

private[angel] case class GetMatrix() extends AngelMessage

private[angel] case class PutMatrix(update: Matrix[_]) extends AngelMessage

private[angel] object Shutdown extends AngelMessage