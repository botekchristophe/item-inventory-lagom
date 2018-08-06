# item-inventory-lagom
This project is an implementation example of how to manage an item inventory with Lagom Framework (1.4.7)

## Item API

```scala
  override final def descriptor: Descriptor = {
      named("item-service").withCalls(
        restCall(Method.GET,    "/api/rest/catalog",                            getCatalog _),
        restCall(Method.POST,   "/api/rest/items",                              addItem _),
        restCall(Method.DELETE, "/api/rest/items/:id",                          removeItem _),
        restCall(Method.POST,   "/api/rest/bundles",                            addBundle _),
        restCall(Method.DELETE, "/api/rest/bundles/:id",                        removeBundle _),
        restCall(Method.GET,    "/api/rest/carts",                              getCarts _),
        restCall(Method.POST,   "/api/rest/carts",                              createCart _),
        restCall(Method.PUT,    "/api/rest/carts/:id/items/:id/quantity/:qtt",  setQuantityForCartItem _),
        restCall(Method.POST,   "/api/rest/carts/:id/checkout",                 checkout _)
      )
    }
```

## Definition

### Catalog

The catalog is composed of all items and bundles available in the application.
For simplicity sake, all items and bundles supplies are unlimited.

### Item

An Item has a price, a name and a description. Its price cannot be negative and its
name has to be unique among all the items.

### Bundle

A bundle is a set of items plus a quantity for each items of the bundle.
A bundle item has to also exists in the catalog set of items.

### Cart

A user Cart can be in two states `IN_USE` or `CHECKED_OUT`. Once a Cart is checked out
its list of items cannot be updated.

## Usage

In order to start the project, sbt needs to be available. It can be downloaded here: https://www.scala-sbt.org/download.html
One can run the project by running the following line in a terminal: 
```sbtshell
sbt clean runAll
```
In order to make the project easier to use a *postman* collection is available in `/postman` folder.

##### Feel free to comment and/or submit a PR
