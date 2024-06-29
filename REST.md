## /domains  
  
### GET  
  
**Description:** Returns all the domains with their information. If a `userId` is provided as a query parameter, it returns only `userId`'s domains 
  
**Parameters:** 
* `userId` (string, optional): The unique identifier of the user.
  
**Reqeuest body:** Empty.  
  
**Response**: Returns a JSON object of domain objects with the following properties:
1. `domainName` (string): The domain name.
2. `userId` (string): The user ID who registered the domain.
3. `purchaseDate` (string, timeMillis): The purchase date of the domain.
4. `expirationDate` (string, timeMillis): The expiration date of the domain.

**Response Codes:**  
* `200`: Successful request
* `404 Not Found`: No domains have been found
* `500 Internal Server Error`: An error occurred on the server.


## /domains/{domain}/availability 
  
### GET  
  
**Description:** Checks if a domain name is available.
  
**Parameters:**  
* `domain` (string, required): The domain name to check.
  
**Request body:** Empty.
  
**Response**: Returns a `200` code if domain is available. If the domain is not available, returns a JSON object with the following properties:
1. `name` (string): The name of the registrant.
2. `surname` (string): The surname of the registrant.
3. `email` (string): The email of the registrant.
4. `expirationDate` (string, timeMillis): The expiration date of the domain.
  
**Response Codes:**  
* `200`: Domain is available
* `400 Bad Request`: Domain is not an acceptable domain.
* `409 Conflict`: Domain is already registered.
* `500 Internal Server Error`: An error occurred on the server.

  
## /orders 
  
### GET
  
**Description:** Returns all the orders. If a `userId` is provided as a query parameter, it returns only `userId`'s orders.
  
**Parameters:**  
* `userId` (string, optional): The unique identifier of the user.
  
**Request Body:** Empty.
  
**Response**: Returns a JSON object of order objects with the following properties:
1. `domainName` (string): The domain name.
2. `userId` (string): The user ID who submitted the order.
3. `price` (number): The transaction amount.
4. `date` (string, timeMillis): The date the order was registered.
5. `CVV` (string): The CVV code used in the transaction.
6. `cardNumber` (string): The card number used in the transaction.
7. `accountHolder` (string): The name of the cardholder.
8. `operationType` (string): The type of operation, either renewal or purchase.
  
**Response Codes:**  
* `200`: Successful request.
* `404 Not Found`: No orders found.
* `500 Internal Server Error`: An error occurred on the server.


## /orders 
  
### POST
  
**Description:** Creates a new order, which can be a renewal or a new purchase. It registers or updates the domain accordingly.
  
**Parameters:**  None.
  
**Request Body:** It accepts a JSON with this properties:
* `userId`: (string, required): Unique identifier of a user.
* `duration`: (integer, required): Number of years.
* `price`: (integer, required): Transaction amount.
* `domain`: (string, required): Domain name.
* `accountHolder`: (string, required): Name of the card owner.
* `cardNumber`: (string, required): Card number.
* `cvv`: (string, required): Three digits code.
* `operation`: (string, required): Operation type, either `renewal` or `purchase`.
  
**Response**: Returns a successful response.
  
**Response Codes:**  
* `200`: Successful request.
* `400 Bad Request`: Invalid params in the request body.
* `403 Forbidden`: If during a `renewal` operation the domain doesn't belongs to `userId`.
* `406 Not Acceptable`: If during the operation the number of years is greater than 10 (cumulative).
* `409 Conflict`: If the domain that you are trying to buy is already registered.
* `500 Internal Server Error`: An error occurred on the server.

## /user/{userId}
  
### GET
  
**Description:** Returns the user with the specified `userId`.
  
**Parameters:**
* `userId`: (string, required): The unique identifier of the user.

**Request Body:** Empty.
  
**Response**: Returns a JSON object with the user properties:
1. `name` (string): User name.
2. `surname` (string): User surname.
3. `email` (string): User email address.
4. `userId` (string): User ID.
  
**Response Codes:**  
* `200`: Successful request.
* `404 Not Found`: User not found for the given `userId`.
* `500 Internal Server Error`: An error occurred on the server.

## /user

### POST
  
**Description:** Register a new user.
  
**Parameters:** None.

**Request Body:** Accepts a JSON object with the following properties:
* `name` (string, required): User name
* `surname` (string, required): User surname.
* `email` (string, required): User email address.
  
**Response**: Returns a JSON object with the new user attributes:
1. `name` (string): User name.
2. `surname` (string): User surname.
3. `email` (string): User email address.
4. `userId` (string): Generated unique user ID.
  
**Response Codes:**  
* `200`: Successful request.
* `400 Bad Request`: Empty or invalid request body params.
* `409 Conflict`: User already exists.
* `500 Internal Server Error`