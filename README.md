# item-inventory-lagom

![Image of Travis CI](https://travis-ci.org/botekchristophe/item-inventory-lagom.svg?branch=master)

This project is an example implementation of how to manage an item inventory with Lagom Framework (1.4.7)
It covers the basic of fetching a catalog of items and bundles, create/removing items and managing a user Cart.

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

In order to start the project, sbt needs to be available. It can be downloaded here: 
https://www.scala-sbt.org/download.html.

One can run the project by running the following line in a terminal: 
```sbtshell
sbt clean runAll
```

## Unit tests

In order to check the application unit test coverage, `scoverage` plugin is added to `/project/plugins.sbt` file.

To see the coverage results:
```scala
sbt coverage test
sbt coverageReport
```

Here are the latest results:

```sbtshell
[info] Statement coverage.: 80.66%
[info] Branch coverage....: 75.00%
[info] Coverage reports completed
[info] All done. Coverage was [80.66%]
```

## Regression tests

As regression test suite is available in `regression_tests/postman.collection.json`. It covers basic behaviour of the API
including HTTP status of the responses and some of the most important fields.

In order to run the regression test suite, these options are available:
- Download newman plugin and run the collection in the terminal (https://www.npmjs.com/package/newman#getting-started)
- Use Postman desktop or Chrome extension and import the collection (https://www.getpostman.com/apps)

Here are the latest results:
```sbtshell
┌─────────────────────────┬──────────┬──────────┐
│                         │ executed │   failed │
├─────────────────────────┼──────────┼──────────┤
│              iterations │        1 │        0 │
├─────────────────────────┼──────────┼──────────┤
│                requests │       12 │        0 │
├─────────────────────────┼──────────┼──────────┤
│            test-scripts │       12 │        0 │
├─────────────────────────┼──────────┼──────────┤
│      prerequest-scripts │        0 │        0 │
├─────────────────────────┼──────────┼──────────┤
│              assertions │       17 │        0 │
├─────────────────────────┴──────────┴──────────┤
│ total run duration: 566ms                     │
├───────────────────────────────────────────────┤
│ total data received: 1.58KB (approx)          │
├───────────────────────────────────────────────┤
│ average response time: 22ms                   │
└───────────────────────────────────────────────┘
```


##### Feel free to comment and/or submit a PR
