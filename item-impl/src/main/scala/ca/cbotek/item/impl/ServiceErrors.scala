package ca.cbotek.item.impl

import ca.cbotek.shared.ErrorResponse

object ServiceErrors {
  type ServiceError = ErrorResponse
  final val CartNotFound: ServiceError = ErrorResponse(404, "Not found", "Cart not found.")
  final val CartConflict: ServiceError = ErrorResponse(409, "Conflict", "Cart already exists for this user.")
  final val CartCheckedOut: ServiceError = ErrorResponse(400, "Bad request", "Cart cannot be updated.")

  final val BundleNotFound: ServiceError = ErrorResponse(404, "Not Found", "Bundle not found.")
  final val BundleConflict: ServiceError = ErrorResponse(409, "Conflict", "Bundle already exists with this name.")

  final val ItemsNotFoundInInventory: ServiceError = ErrorResponse(404, "Not Found", "One or more items were not found in the inventory.")
  final val ItemCannotBeRemoved: ServiceError = ErrorResponse(400, "Bad request", "Item is being used by a bundle, remove bundle first.")
  final val ItemNotFound: ServiceError = ErrorResponse(404, "Not Found", "Item not found.")
  final val ItemConflict: ServiceError = ErrorResponse(409, "Conflict", "Item already exists with this name.")
}
