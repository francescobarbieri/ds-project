## `/domains  
  
### GET  
  
**Description:** Returns all the domains with their informations: domain name, userId that registered the domain, purchase date, expiration date. If a `userId` is provided as a query parameters, it returns only `userId`'s domains 
  
**Parameters:** 
* `userId`: Optional Query Params. The unique identifier of the user.
  
**Reqeuest body:** Empty.  
  
**Response**: Returns a JSON array of domain type JSON objects.
  
**Response Codes:**  
* `200`: Successful request
* `404 Not Found`: No domains have been found
* `500 Internal Server Error`
  

## `/domains/{domain}/availability 
  
### GET  
  
**Description:** Returns if a domain name is available. 
  
**Parameters:**  
* `domain`: Required. The domain name to check.
  
**Request body:** Empty.
  
**Response**: Returns a 200 if domain is available. If domain is not available returns a JSON with this parameters: `name`, `surname`, `email` and `expirationDate`.  
  
**Response Codes:**  
* `200`: Domain is available
* `400 Bad Request`: Domain is not an acceptable domain.
* `409 Conflict`: If domain is already registered, returns the JSON described before.
* `500 Internal Server Error`
  
  
## `/orders 
  
### GET
  
**Description:** Returns all the orders with their informations: `domainname`, `userId` that submitted the order, `price`, `date` of the order, `CVV`, `card number`, `operationType` and accountholder. If a `userId` is provided as a query parameters, it returns only `userId`'s domains 
  
**Parametri:**  
* `userId`: Optional Query Params. The unique identifier of the user.
  
**Request Body:** Empty.
  
**Response**: Returns a JSON array of order type JSON objects.
  
**Response Codes:**  
* `200`: Successful request.
* `404 Not Found`: There are no orders.
* `500 Internal Server Error`
  
## `/orders 
  
### POST
  
**Description:** Sets a new order, can be a renewal or a new purchase. It register or updates the domain accordingly.
  
**Parameters:**  None.
  
**Request Body:** It accepts a JSON with this parameters:
* `userId`: Required. Unique identifier of a user.
* `duration`: Required. Integer representing the number of years.
* `price`: Required. Transaction amount.
* `domain`: Required. Domain name.
* `accountHolder`: Required. Name of the card owner.
* `cardNumber`: Required. Card number.
* `cvv`: Required. Three digits code.
* `operation`: Required. Operation type, can be either a `renewal` or a `purchase`.
  
**Response**: Returns a successful response.
  
**Response Codes:**  
* `200`: Successful request.
* `400 Bad Request`: Invalid params in the request body.
* `403 Forbidden`: If during a `renewal` operation the domain doesn't belongs to `userId`.
* `406 Not Acceptable`: If during a `renewal` operation the number of years is greater than 10 (cumulative).
* `409 Conflict`: If the domain that you are trying to buy is already registered.
* `500 Internal Server Error`

## `/user/{userId}
  
### GET
  
**Description:** Returns the user with userId equals to the path parameter `userId`.
  
**Parameters:**
* `userId`: Required. Unique identifier of a user.

**Request Body:** Empty.
  
**Response**: Returns a JSON with the user attributes: `name`, `surname`, `email`, `userId`.
  
**Response Codes:**  
* `200`: Successful request.
* `404 Not Found`: User not found for the given `userId`.
* `500 Internal Server Error`

## `/user

### POST
  
**Description:** Register a new user.
  
**Parameters:** None.

**Request Body:** It accepts a JSON with this parameters:
* `name`: Required. User name.
* `surname`: Required. User surname.
* `email`: Required. User email address.
  
**Response**: Returns a JSON with the new user attributes: `name`, `surname`, `email`, `userId`.
  
**Response Codes:**  
* `200`: Successful request.
* `400 Bad Request`: Empty or invalid request body params.
* `409 Conflict`: User already exists.
* `500 Internal Server Error`