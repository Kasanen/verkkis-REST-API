# API endpoint contracts

This document lists the endpoint contracts

---

## SupplierController

#### GET /api/suppliers
Purpose: Return all suppliers with their product counts.

Request body: none

Query params and path params:
- none

Response body:
```json
[
  {
    "id": 1,
    "name": "Nordic Audio",
    "contactName": "Sara Hiltunen",
    "phone": "+358401234567",
    "email": "sales@nordicaudio.fi",
    "productCount": 3
  }
]
```

Status codes:
- `200 OK` on success

#### GET /api/suppliers/{id}
Purpose: Return one supplier including address data and product count.

Request body: none

Query params and path params:
- `id`: required integer path parameter

Response body:
```json
{
  "id": 1,
  "name": "Nordic Audio",
  "contactName": "Sara Hiltunen",
  "phone": "+358401234567",
  "email": "sales@nordicaudio.fi",
  "productCount": 3,
  "addresses": [
    {
      "id": 10,
      "streetAddress": "Kauppakatu 1",
      "postalCode": "00100",
      "city": "Helsinki",
      "country": "Finland"
    }
  ]
}
```

Status codes:
- `200 OK` on success
- `404 Not Found` when the supplier does not exist (`SupplierService.get` calls `find` and throws `NotFoundException`)

#### GET /api/suppliers/{id}/products
Purpose: Return the products for one supplier in paginated form.

Request body: none

Query params and path params:
- `id`: required integer path parameter
- Spring pagination params are supported: `page`, `size`, `sort`
- default: `@PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC)` in `SupplierController.products`

Response body:
```json
{
  "content": [
    {
      "id": 5,
      "name": "Laptop",
      "description": "Business laptop",
      "price": 899.99,
      "stockQuantity": 12,
      "inStock": true,
      "category": {
        "id": 2,
        "name": "Electronics"
      },
      "supplier": {
        "id": 1,
        "name": "Nordic Audio"
      }
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

Status codes:
- `200 OK` on success
- `404 Not Found` if the supplier does not exist (`supplierService.requireExists(id)`)

#### POST /api/suppliers
Purpose: Create a new supplier.

Request body:
```json
{
  "name": "Nordic Audio",
  "contactName": "Sara Hiltunen",
  "phone": "+358401234567",
  "email": "sales@nordicaudio.fi"
}
```

Field details:
- `name`: string, required, validated by `Validation.requireText(req.name(), "name", 255)`
- `contactName`: string, optional, `Validation.optionalText(req.contactName(), "contactName", 100)`
- `phone`: string, optional, `Validation.optionalText(req.phone(), "phone", 30)`
- `email`: string, optional, but if present it is validated with `Validation.optionalEmail(req.email(), "email")` which checks the email pattern and trims input

Response body:
```json
{
  "id": 1,
  "name": "Nordic Audio",
  "contactName": "Sara Hiltunen",
  "phone": "+358401234567",
  "email": "sales@nordicaudio.fi",
  "productCount": 0,
  "addresses": []
}
```

Status codes:
- `201 Created` on success
- `400 Bad Request` for missing/invalid request data (`BadRequestException` in `SupplierService.apply`)
- `409 Conflict` for database integrity issues (`DataIntegrityViolationException` handling in `ApiExceptionHandler`)

#### PUT /api/suppliers/{id}
Purpose: Update an existing supplier.

Request body:
```json
{
  "name": "Nordic Audio",
  "contactName": "Sara Hiltunen",
  "phone": "+358401234567",
  "email": "sales@nordicaudio.fi"
}
```

Field details:
- same as `POST /api/suppliers`
- validation is performed in `SupplierService.apply`

Response body:
```json
{
  "id": 1,
  "name": "Nordic Audio",
  "contactName": "Sara Hiltunen",
  "phone": "+358401234567",
  "email": "sales@nordicaudio.fi",
  "productCount": 3,
  "addresses": [
    {
      "id": 10,
      "streetAddress": "Kauppakatu 1",
      "postalCode": "00100",
      "city": "Helsinki",
      "country": "Finland"
    }
  ]
}
```

Status codes:
- `200 OK` on success
- `400 Bad Request` when validation fails or request body is malformed
- `404 Not Found` if the supplier does not exist
- `409 Conflict` for database constraint errors

#### DELETE /api/suppliers/{id}
Purpose: Delete a supplier after checking it has no linked products.

Request body: none

Query params and path params:
- `id`: required integer path parameter

Response body: none (`204 No Content`)

Status codes:
- `204 No Content` on success
- `404 Not Found` if supplier does not exist
- `409 Conflict` if the supplier still has products (`SupplierService.delete` checks `productRepository.countBySupplier_Id(id)` and throws `ConflictException`)

#### POST /api/suppliers/{id}/discount
Purpose: Apply a percentage discount to all products supplied by a supplier.

Request body:
```json
{
  "percent": 15
}
```

Field details:
- `percent`: decimal, required
- validation in `SupplierService.applyDiscount`: `percent` must be greater than 0 and at most 90
- `null` request body throws `BadRequestException("Request body is required")`

Response body:
```json
{
  "affectedRows": 3,
  "message": "Prices reduced by 15 %"
}
```

Status codes:
- `200 OK` on success
- `400 Bad Request` if request body is missing or percent is invalid
- `404 Not Found` if the supplier does not exist

#### POST /api/suppliers/{id}/restock
Purpose: Increase stock by 50 for each product supplied by the supplier.

Request body: none

Query params and path params:
- `id`: required integer path parameter

Response body:
```json
{
  "affectedRows": 3,
  "message": "Stock increased by 50 for each product"
}
```

Status codes:
- `200 OK` on success
- `404 Not Found` if the supplier does not exist

---

## ProductController

#### GET /api/products
Purpose: Search and list products with optional filters and pagination.

Request body: none

Query params and path params:
- `q`: optional string
- `categoryId`: optional integer
- `supplierId`: optional integer
- `minPrice`: optional decimal
- `maxPrice`: optional decimal
- `inStock`: optional boolean
- Spring pagination params: `page`, `size`, `sort`
- default sort: `id` ascending

Validation constraints:
- `minPrice` and `maxPrice` must satisfy `minPrice <= maxPrice` in `ProductService.search`
- if `inStock == true`, only products with `stockQuantity > 0` are returned

Response body:
```json
{
  "content": [
    {
      "id": 5,
      "name": "Laptop",
      "description": "Business laptop",
      "price": 899.99,
      "stockQuantity": 12,
      "inStock": true,
      "category": {
        "id": 2,
        "name": "Electronics"
      },
      "supplier": {
        "id": 1,
        "name": "Nordic Audio"
      }
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

Status codes:
- `200 OK` on success
- `400 Bad Request` if `minPrice > maxPrice`

#### GET /api/products/{id}
Purpose: Return one product with category and supplier data.

Request body: none

Query params and path params:
- `id`: required integer path parameter

Response body:
```json
{
  "id": 5,
  "name": "Laptop",
  "description": "Business laptop",
  "price": 899.99,
  "stockQuantity": 12,
  "inStock": true,
  "category": {
    "id": 2,
    "name": "Electronics"
  },
  "supplier": {
    "id": 1,
    "name": "Nordic Audio"
  }
}
```

Status codes:
- `200 OK` on success
- `404 Not Found` if the product does not exist

#### POST /api/products
Purpose: Create a product.

Request body:
```json
{
  "name": "Laptop",
  "description": "Business laptop",
  "price": 899.99,
  "stockQuantity": 12,
  "categoryId": 2,
  "supplierId": 1
}
```

Field details:
- `name`: string, required, `Validation.requireText(req.name(), "name", 255)`
- `description`: string, optional, `Validation.optionalText(req.description(), "description", 65535)`
- `price`: decimal, required, `Validation.requirePrice(req.price(), "price")`
  - must be between `0` and `99999999.99`
  - maximum 2 decimal places
- `stockQuantity`: integer, optional, if null treated as `0`, must be `>= 0`
- `categoryId`: integer, optional, if provided it must exist in `productcategories`
- `supplierId`: integer, optional, if provided it must exist in `suppliers`

Response body:
```json
{
  "id": 5,
  "name": "Laptop",
  "description": "Business laptop",
  "price": 899.99,
  "stockQuantity": 12,
  "inStock": true,
  "category": {
    "id": 2,
    "name": "Electronics"
  },
  "supplier": {
    "id": 1,
    "name": "Nordic Audio"
  }
}
```

Status codes:
- `201 Created` on success
- `400 Bad Request` for missing/invalid validation or unknown category/supplier ids
- `409 Conflict` for database constraint issues

#### PUT /api/products/{id}
Purpose: Update an existing product.

Request body:
```json
{
  "name": "Laptop",
  "description": "Updated business laptop",
  "price": 949.99,
  "stockQuantity": 11,
  "categoryId": 2,
  "supplierId": 1
}
```

Field details:
- same validations as `POST /api/products`

Response body:
```json
{
  "id": 5,
  "name": "Laptop",
  "description": "Updated business laptop",
  "price": 949.99,
  "stockQuantity": 11,
  "inStock": true,
  "category": {
    "id": 2,
    "name": "Electronics"
  },
  "supplier": {
    "id": 1,
    "name": "Nordic Audio"
  }
}
```

Status codes:
- `200 OK` on success
- `400 Bad Request` for invalid request data
- `404 Not Found` if product does not exist
- `409 Conflict` for database constraint issues

#### DELETE /api/products/{id}
Purpose: Delete a product only if it is not included in any order history.

Request body: none

Query params and path params:
- `id`: required integer path parameter

Response body: none (`204 No Content`)

Status codes:
- `204 No Content` on success
- `404 Not Found` if the product does not exist
- `409 Conflict` if the product appears in orders and cannot be deleted (`ProductService.delete` checks `orderItemRepository.existsByProduct_Id(id)`) 

---

## CustomerController

#### GET /api/customers
Purpose: Search and list customers by name/email prefix.

Request body: none

Query params and path params:
- `q`: optional string query
- Spring pagination params: `page`, `size`, `sort`
- default sort: `id` ascending

Response body:
```json
{
  "content": [
    {
      "id": 5,
      "firstName": "Anna",
      "lastName": "Smith",
      "email": "anna@example.com",
      "phone": "+358401234567",
      "addresses": [
        {
          "id": 12,
          "streetAddress": "Main Street 1",
          "postalCode": "00100",
          "city": "Helsinki",
          "country": "Finland"
        }
      ]
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

Status codes:
- `200 OK` on success

#### GET /api/customers/{id}
Purpose: Get one customer with addresses.

Request body: none

Query params and path params:
- `id`: required integer path parameter

Response body:
```json
{
  "id": 5,
  "firstName": "Anna",
  "lastName": "Smith",
  "email": "anna@example.com",
  "phone": "+358401234567",
  "addresses": [
    {
      "id": 12,
      "streetAddress": "Main Street 1",
      "postalCode": "00100",
      "city": "Helsinki",
      "country": "Finland"
    }
  ]
}
```

Status codes:
- `200 OK` on success
- `404 Not Found` if customer does not exist

#### POST /api/customers
Purpose: Register a new customer.

Request body:
```json
{
  "firstName": "Anna",
  "lastName": "Smith",
  "email": "anna@example.com",
  "phone": "+358401234567"
}
```

Field details:
- `firstName`: string, required, `Validation.requireText(req.firstName(), "firstName", 100)`
- `lastName`: string, required, `Validation.requireText(req.lastName(), "lastName", 100)`
- `email`: string, required, `Validation.requireEmail(req.email(), "email")`
- `phone`: string, optional, `Validation.optionalText(req.phone(), "phone", 30)`

Response body:
```json
{
  "id": 5,
  "firstName": "Anna",
  "lastName": "Smith",
  "email": "anna@example.com",
  "phone": "+358401234567",
  "addresses": []
}
```

Status codes:
- `201 Created` on success
- `400 Bad Request` for malformed/invalid fields
- `409 Conflict` if a customer with the same email already exists (`CustomerService.register` checks `customerRepository.existsByEmail(...)`)

#### PUT /api/customers/{id}
Purpose: Update an existing customer.

Request body:
```json
{
  "firstName": "Anna",
  "lastName": "Smith",
  "email": "anna@example.com",
  "phone": "+358401234567"
}
```

Field details:
- same validation as `POST /api/customers`
- duplicate email check: `existsByEmailAndIdNot(email, id)`

Response body:
```json
{
  "id": 5,
  "firstName": "Anna",
  "lastName": "Smith",
  "email": "anna@example.com",
  "phone": "+358401234567",
  "addresses": [
    {
      "id": 12,
      "streetAddress": "Main Street 1",
      "postalCode": "00100",
      "city": "Helsinki",
      "country": "Finland"
    }
  ]
}
```

Status codes:
- `200 OK` on success
- `400 Bad Request` for invalid request data
- `404 Not Found` if customer does not exist
- `409 Conflict` if the new email already belongs to another customer

#### DELETE /api/customers/{id}
Purpose: Delete a customer only if the customer has no existing orders.

Request body: none

Query params and path params:
- `id`: required integer path parameter

Response body: none (`204 No Content`)

Status codes:
- `204 No Content` on success
- `404 Not Found` if customer does not exist
- `409 Conflict` if the customer has orders (`CustomerService.delete` checks `orderRepository.existsByCustomer_Id(id)`) 

#### GET /api/customers/{id}/addresses
Purpose: List all addresses for a customer.

Request body: none

Query params and path params:
- `id`: required integer path parameter

Response body:
```json
[
  {
    "id": 12,
    "streetAddress": "Main Street 1",
    "postalCode": "00100",
    "city": "Helsinki",
    "country": "Finland"
  }
]
```

Status codes:
- `200 OK` on success
- `404 Not Found` if customer does not exist (`CustomerService.find(customerId)`) 

#### POST /api/customers/{id}/addresses
Purpose: Add an address to a customer.

Request body:
```json
{
  "streetAddress": "Main Street 1",
  "postalCode": "00100",
  "city": "Helsinki",
  "country": "Finland"
}
```

Field details:
- `streetAddress`: string, required, `Validation.requireText(req.streetAddress(), "streetAddress", 255)`
- `postalCode`: string, optional, `Validation.optionalText(req.postalCode(), "postalCode", 20)`
- `city`: string, required, `Validation.requireText(req.city(), "city", 100)`
- `country`: string, optional, `Validation.optionalText(req.country(), "country", 100)`

Response body:
```json
{
  "id": 12,
  "streetAddress": "Main Street 1",
  "postalCode": "00100",
  "city": "Helsinki",
  "country": "Finland"
}
```

Status codes:
- `201 Created` on success
- `400 Bad Request` for invalid request data
- `404 Not Found` if the customer does not exist

#### PUT /api/customers/{id}/addresses/{addressId}
Purpose: Update one address belonging to a customer.

Request body:
```json
{
  "streetAddress": "Main Street 2",
  "postalCode": "00120",
  "city": "Vantaa",
  "country": "Finland"
}
```

Field details:
- same as `POST /api/customers/{id}/addresses`

Response body:
```json
{
  "id": 12,
  "streetAddress": "Main Street 2",
  "postalCode": "00120",
  "city": "Vantaa",
  "country": "Finland"
}
```

Status codes:
- `200 OK` on success
- `400 Bad Request` for invalid input
- `404 Not Found` if customer or address does not exist

#### DELETE /api/customers/{id}/addresses/{addressId}
Purpose: Delete one customer address if it is not used in an order.

Request body: none

Query params and path params:
- `id`: required integer customer path parameter
- `addressId`: required integer address path parameter

Response body: none (`204 No Content`)

Status codes:
- `204 No Content` on success
- `404 Not Found` if customer or address does not exist
- `409 Conflict` if the address is used as a shipping address in an order (`CustomerService.deleteAddress` checks `orderRepository.existsByShippingAddress_Id(addressId)`) 

#### GET /api/customers/{id}/orders
Purpose: Return order summaries for one customer.

Request body: none

Query params and path params:
- `id`: required integer path parameter

Response body:
```json
[
  {
    "id": 101,
    "customerId": 5,
    "orderDate": "2026-10-17T14:30:00",
    "deliveryDate": null,
    "status": "NEW",
    "lineCount": 2,
    "totalQuantity": 3,
    "total": 1829.88
  }
]
```

Status codes:
- `200 OK` on success
- `404 Not Found` if customer does not exist

---

## OrderController

#### POST /api/orders
Purpose: Create a new order in a single transaction and validate stock before saving.

Request body:
```json
{
  "customerId": 5,
  "shippingAddressId": 12,
  "items": [
    { "productId": 10, "quantity": 2 },
    { "productId": 21, "quantity": 1 }
  ]
}
```

Field details:
- `customerId`: integer, required, must reference a customer
- `shippingAddressId`: integer, required, must belong to the same customer
- `items`: array, required, minimum 1 item, maximum 50 items
- each item:
  - `productId`: integer, required, must exist in `products`
  - `quantity`: integer, required, must be `> 0`, and each merged product total may not exceed 1000
- validation is performed in `OrderService.placeOrder` and `OrderService.mergeLines`

Response body:
```json
{
  "id": 101,
  "customer": {
    "id": 5,
    "name": "Anna Smith",
    "email": "anna@example.com"
  },
  "orderDate": "2026-10-17T14:30:00",
  "deliveryDate": null,
  "status": "NEW",
  "shippingAddress": {
    "id": 12,
    "streetAddress": "Main Street 1",
    "postalCode": "00100",
    "city": "Helsinki",
    "country": "Finland"
  },
  "items": [
    {
      "productId": 10,
      "productName": "Laptop",
      "quantity": 2,
      "unitPrice": 899.99,
      "lineTotal": 1799.98
    },
    {
      "productId": 21,
      "productName": "Mouse",
      "quantity": 1,
      "unitPrice": 29.9,
      "lineTotal": 29.9
    }
  ],
  "total": 1829.88
}
```

Status codes:
- `201 Created` on success
- `400 Bad Request` for malformed JSON, missing `customerId`, missing `shippingAddressId`, empty items, invalid quantity, unknown product ids, or invalid customer/address relation
- `409 Conflict` if stock is insufficient or a database constraint is violated

#### GET /api/orders/{id}
Purpose: Return one order with item lines and shipping data.

Request body: none

Query params and path params:
- `id`: required integer path parameter

Response body:
```json
{
  "id": 101,
  "customer": {
    "id": 5,
    "name": "Anna Smith",
    "email": "anna@example.com"
  },
  "orderDate": "2026-10-17T14:30:00",
  "deliveryDate": null,
  "status": "PAID",
  "shippingAddress": {
    "id": 12,
    "streetAddress": "Main Street 1",
    "postalCode": "00100",
    "city": "Helsinki",
    "country": "Finland"
  },
  "items": [
    {
      "productId": 10,
      "productName": "Laptop",
      "quantity": 2,
      "unitPrice": 899.99,
      "lineTotal": 1799.98
    }
  ],
  "total": 1799.98
}
```

Status codes:
- `200 OK` on success
- `404 Not Found` if the order does not exist (`OrderService.findWithDetails` throws `NotFoundException`)

#### GET /api/orders/{id}/history
Purpose: Return the status history for an order.

Request body: none

Query params and path params:
- `id`: required integer path parameter

Response body:
```json
[
  {
    "id": 1,
    "status": "NEW",
    "changedAt": "2026-10-17T14:30:00"
  },
  {
    "id": 2,
    "status": "PAID",
    "changedAt": "2026-10-17T14:35:00"
  }
]
```

Status codes:
- `200 OK` on success
- `404 Not Found` if the order does not exist

#### GET /api/orders
Purpose: List and filter orders for admin use.

Request body: none

Query params and path params:
- `status`: optional string, accepted values are `NEW`, `PAID`, `SHIPPED`, `DELIVERED`, `CANCELLED`
- Spring pagination params: `page`, `size`, `sort`
- default sort: `orderDate` descending

Response body:
```json
{
  "content": [
    {
      "id": 101,
      "customerId": 5,
      "orderDate": "2026-10-17T14:30:00",
      "deliveryDate": null,
      "status": "NEW",
      "lineCount": 2,
      "totalQuantity": 3,
      "total": 1829.88
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

Status codes:
- `200 OK` on success
- `400 Bad Request` if `status` is unknown or the request parameters are malformed

#### POST /api/orders/{id}/cancel
Purpose: Cancel an order and return the items to stock.

Request body: none

Query params and path params:
- `id`: required integer path parameter

Response body:
```json
{
  "id": 101,
  "customer": {
    "id": 5,
    "name": "Anna Smith",
    "email": "anna@example.com"
  },
  "orderDate": "2026-10-17T14:30:00",
  "deliveryDate": null,
  "status": "CANCELLED",
  "shippingAddress": {
    "id": 12,
    "streetAddress": "Main Street 1",
    "postalCode": "00100",
    "city": "Helsinki",
    "country": "Finland"
  },
  "items": [
    {
      "productId": 10,
      "productName": "Laptop",
      "quantity": 2,
      "unitPrice": 899.99,
      "lineTotal": 1799.98
    }
  ],
  "total": 1799.98
}
```

Status codes:
- `200 OK` on success
- `404 Not Found` if the order does not exist
- `409 Conflict` if the order status is not cancellable (`OrderStatus.isCancellable()` only allows `NEW` and `PAID`)

#### PATCH /api/orders/{id}/status
Purpose: Change the order status according to the allowed lifecycle.

Request body:
```json
{
  "status": "SHIPPED"
}
```

Field details:
- `status`: string, required
- allowed values: `NEW`, `PAID`, `SHIPPED`, `DELIVERED`, `CANCELLED`
- rules are enforced by `OrderStatus.parse` and `OrderStatus.canMoveTo`

Response body:
```json
{
  "id": 101,
  "customer": {
    "id": 5,
    "name": "Anna Smith",
    "email": "anna@example.com"
  },
  "orderDate": "2026-10-17T14:30:00",
  "deliveryDate": "2026-10-18T09:00:00",
  "status": "DELIVERED",
  "shippingAddress": {
    "id": 12,
    "streetAddress": "Main Street 1",
    "postalCode": "00100",
    "city": "Helsinki",
    "country": "Finland"
  },
  "items": [
    {
      "productId": 10,
      "productName": "Laptop",
      "quantity": 2,
      "unitPrice": 899.99,
      "lineTotal": 1799.98
    }
  ],
  "total": 1799.98
}
```

Status codes:
- `200 OK` on success
- `400 Bad Request` if the request body is missing, malformed, or status is not a valid enum value
- `404 Not Found` if the order does not exist
- `409 Conflict` if the transition is not allowed (`OrderStatus.canMoveTo` denies it)

#### DELETE /api/orders/cancelled
Purpose: Delete old cancelled orders older than a configured threshold.

Request body: none

Query params and path params:
- `olderThanDays`: optional integer, default `90`, must be `> 0`

Response body:
```json
{
  "deletedOrders": 12
}
```

Status codes:
- `200 OK` on success
- `400 Bad Request` if `olderThanDays` is not a positive integer

---

## CategoryController

#### GET /api/categories
Purpose: Return all product categories.

Request body: none

Query params and path params:
- none

Response body:
```json
[
  {
    "id": 1,
    "name": "Electronics",
    "description": "Electronic devices",
    "productCount": 5,
    "inStockCount": 4
  }
]
```

Status codes:
- `200 OK` on success

#### GET /api/categories/{id}
Purpose: Return one category.

Request body: none

Query params and path params:
- `id`: required integer path parameter

Response body:
```json
{
  "id": 1,
  "name": "Electronics",
  "description": "Electronic devices",
  "productCount": 5,
  "inStockCount": 4
}
```

Status codes:
- `200 OK` on success
- `404 Not Found` if category does not exist (`CategoryService.get` throws `NotFoundException`)

#### GET /api/categories/{id}/products
Purpose: Return products in a category, optionally filtering for in-stock items.

Request body: none

Query params and path params:
- `id`: required integer path parameter
- `inStock`: optional boolean
- Spring pagination params: `page`, `size`, `sort`
- default sort: `id` ascending

Response body:
```json
{
  "content": [
    {
      "id": 5,
      "name": "Laptop",
      "description": "Business laptop",
      "price": 899.99,
      "stockQuantity": 12,
      "inStock": true,
      "category": {
        "id": 1,
        "name": "Electronics"
      },
      "supplier": {
        "id": 1,
        "name": "Nordic Audio"
      }
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

Status codes:
- `200 OK` on success
- `404 Not Found` if the category does not exist

#### POST /api/categories
Purpose: Create a new category.

Request body:
```json
{
  "name": "Electronics",
  "description": "Electronic devices"
}
```

Field details:
- `name`: string, required, `Validation.requireText(req.name(), "name", 100)`
- `description`: string, optional, `Validation.optionalText(req.description(), "description", 65535)`

Response body:
```json
{
  "id": 1,
  "name": "Electronics",
  "description": "Electronic devices",
  "productCount": 0,
  "inStockCount": 0
}
```

Status codes:
- `201 Created` on success
- `400 Bad Request` for invalid data
- `409 Conflict` if category name already exists (`CategoryService.create` checks `categoryRepository.existsByNameIgnoreCase(...)`)

#### PUT /api/categories/{id}
Purpose: Update an existing category.

Request body:
```json
{
  "name": "Electronics",
  "description": "Updated electronic devices"
}
```

Field details:
- same as `POST /api/categories`

Response body:
```json
{
  "id": 1,
  "name": "Electronics",
  "description": "Updated electronic devices",
  "productCount": 5,
  "inStockCount": 4
}
```

Status codes:
- `200 OK` on success
- `400 Bad Request` for invalid input
- `404 Not Found` if category does not exist
- `409 Conflict` for database constraints

#### DELETE /api/categories/{id}
Purpose: Delete a category only if it has no products attached.

Request body: none

Query params and path params:
- `id`: required integer path parameter

Response body: none (`204 No Content`)

Status codes:
- `204 No Content` on success
- `404 Not Found` if category does not exist
- `409 Conflict` if the category still has products (`CategoryService.delete` checks `productRepository.countByCategory_Id(id)`) 

---

## ReportController

#### GET /api/reports/monthly-sales
Purpose: Return monthly sales summary from the database view.

Request body: none

Query params and path params:
- none

Response body:
```json
[
  {
    "salesMonth": "2026-09",
    "totalOrders": 123,
    "totalItemsSold": 456,
    "totalRevenue": 12500.00
  }
]
```

Status codes:
- `200 OK` on success

#### GET /api/reports/low-stock
Purpose: Return products whose stock quantity is at or below a threshold.

Request body: none

Query params and path params:
- `threshold`: optional integer, default `5`, must be `>= 0`
- `limit`: optional integer, default `20`, validated in `ReportService.checkLimit` as `1 <= limit <= 100`

Response body:
```json
[
  {
    "productId": 7,
    "productName": "Keyboard",
    "price": 49.90,
    "stockQuantity": 2,
    "categoryName": "Accessories",
    "supplierName": "Nordic Audio",
    "supplierEmail": "sales@nordicaudio.fi"
  }
]
```

Status codes:
- `200 OK` on success
- `400 Bad Request` if `threshold < 0` or `limit` is outside `1..100`

#### GET /api/reports/top-products
Purpose: Return the best-selling products in an optional date range.

Request body: none

Query params and path params:
- `limit`: optional integer, default `10`, valid `1..100`
- `from`: optional date in ISO format, `@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)`
- `to`: optional date in ISO format, `@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)`

Validation constraints:
- if both `from` and `to` are set and `from` is after `to`, then `BadRequestException("from must be before to")`
- `limit` must be between 1 and 100

Response body:
```json
[
  {
    "productId": 10,
    "productName": "Laptop",
    "categoryName": "Electronics",
    "unitsSold": 42,
    "orderCount": 18,
    "revenue": 37500.00
  }
]
```

Status codes:
- `200 OK` on success
- `400 Bad Request` if `from > to`, invalid date format, or invalid `limit`

#### GET /api/reports/top-customers
Purpose: Return the customers with the largest spend.

Request body: none

Query params and path params:
- `limit`: optional integer, default `10`, valid `1..100`

Response body:
```json
[
  {
    "id": 5,
    "name": "Anna Smith",
    "email": "anna@example.com",
    "orderCount": 12,
    "totalSpent": 12450.00
  }
]
```

Status codes:
- `200 OK` on success
- `400 Bad Request` if `limit` is outside `1..100`

