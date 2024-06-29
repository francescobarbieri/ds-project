# TCP

This document-oriented database implements a protocol inspired by both Redis and SQL, providing a simple and efficient way to manage your data. It accepts JSON strings as payload, storing them exactly as provided.

## Network layer

A client can connect to the database creating a TCP connection on port 3030.

## Protocol description

The database accepts commands that consist of various arguments. Once a command is processed, a response is sent back to the client. The server response is detailed in the following section.

Since the database saves the data into JSON collections, it accepts only JSON string representing the documents to be created or updated.

Collections are not case sensitive. "users" and "UsErS" are the same collection.

Each command follows this structure:

```
<OperationName> <CollectionName> [DocumentKey] [Payload] [WHERE key value]\n
```

Below is a detailed description of each component and the optional parameters:

* `OperationName`: The operation to be performed.
* `CollectionName`: The name of the collection on which the operation is performed.
* `DocumentKey` (optional): The unique identifier of the document to be manipulated.
* `Payload` (optional): A JSON representing the object to be created or updated.
* `WHERE` (optional): A clause to specify conditions for selecting documents. Available only for certain operations.
    * `key` (optional): The field name used in the condition.
    * `value` (optional): The value associated with the field name in the condition.

Refer to the following sections for the list of the available operations.

### Server response

All server responses begin with a prefix indicating the result of the operation:

* `+`: Indicates that the operation was successful, followed by the payload string.
* `-`: Indicates that there was an error with the operation, followed by a description of the error.

### Available Operations

* `SET`: Creates a new document in the specified collection. This operation adds a new document based on the provided JSON string.
* `GET`: Retrieves a document using its `DocumentKey`.
* `GETALL`: Retrieves all documents in a collection. Supports the WHERE filter to apply conditions.
* `UPDATE`: Updates an existing document identified by its `DocumentKey`. This operation adds new fields and updates existing ones based on the provided JSON string, but it does not delete any fields that are not included in the JSON string.


### Command Examples

#### GET a document given its DocumentKey

To retrieve a document with the DocumentKey "0iu3d" from the "Orders" collection:

```
GET orders 0iu3d\n
```

#### GET all documents using filters

To retrieve all documents from the "Orders" collection where userId is equal to "15483":

```
GETALL orders WHERE userId 15483\n
```

#### SET a mew document

To add a new document with DocumentKey "15483" to the "Users" collection, registering a new user:

```
SET users 15483 {"username": "newUserName", "email": "user@gmail.com"}\n
```

#### UPDATE a document

To update an existing document with DocumentKey "15483" in the "Users" collection:

```
UPDATE users 15483 {"username": "updatedUserName", "email": "updatedUser@gmail.com"}\n
```