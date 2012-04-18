package au.csiro.ict

import grizzled.slf4j.{Logger=>gl}

trait Logger {
  val logger = gl[this.type]
}
