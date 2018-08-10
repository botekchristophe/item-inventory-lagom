package ca.cbotek.item.impl

import ca.cbotek.shared.ErrorResponse

/**
  * ServiceErrors object acts as a enumeration of pre-defined errors that can be used as response for the public REST api.
  *
  * Internally these errors can be created by a read action to the Read-side or a message sent to the persistent entities.
  * It defines all errors related to [[ca.cbotek.item.api.Cart]], [[ca.cbotek.item.api.Bundle]] and [[ca.cbotek.item.api.Item]].
  */
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
  final val ItemNegativeQuantity: ServiceError = ErrorResponse(400, "Bad request", "Item quantity cannot be negative.")
}
