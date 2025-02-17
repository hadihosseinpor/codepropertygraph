package io.shiftleft.cpgvalidator

import io.shiftleft.codepropertygraph.cpgloading.{CpgLoader, CpgLoaderConfig}
import io.shiftleft.cpgvalidator.validators.CpgValidator

object CpgValidatorMain extends App {
  val cpgFileName = args(0)
  val cpg = CpgLoader.load(cpgFileName, CpgLoaderConfig.withoutOverflow)
  val validator = new CpgValidator()
  val cpgValid = validator.validate(cpg)
  validator.logValidationErrors()

  if (cpgValid) {
    System.exit(0)
  } else {
    System.exit(1)
  }
}
