# TCP

## Syntax

This protocol follows a Redis-like syntax. In general, a command is composed of:

&lt;`resource`&gt; &lt;`operation`&gt; &lt;`data`&gt;

Every error message follows this format:

`ERROR: `&lt;`error message`&gt;

## Command list

### Domain Commands

#### DOMAIN SET
DOMAIN SET &lt;name&gt; &lt;userid&gt; &lt;currentdate&gt; &lt;expirationdate&gt;

Sets a new domain with specified details.

#### DOMAIN CHECK
DOMAIN CHECK &lt;name&gt;

Checks if a domain with the specified name exists.

#### DOMAIN GET
DOMAIN GET &lt;name&gt;

Retrieves details of the domain with the specified name.

#### DOMAIN GETALL
DOMAIN GETALL [&lt;userid&gt;]

Retrieves all domains optionally filtered by userid.

#### DOMAIN UPDATE
DOMAIN UPDATE &lt;name&gt; &lt;userid&gt; &lt;expirationdate&gt;

Updates the expiration date of the domain with the specified name.

### Order Commands

#### ORDER SET
ORDER SET &lt;domain&gt; &lt;userid&gt; &lt;price&gt; &lt;currentdate&gt; &lt;cvv&gt; &lt;cardnumber&gt; &lt;operationtype&gt; &lt;accountholder&gt;

Places a new order with specified details.

#### ORDER GETALL
ORDER GETALL [&lt;userid&gt;]

Retrieves all orders optionally filtered by userid.

### User Commands

#### ORDER GETALL
USER SET &lt;email&gt; &lt;name&gt; &lt;surname&gt;

Creates a new user with the specified email, name, and surname.

#### ORDER GETALL
USER GET &lt;userid&gt;

Retrieves details of the user with the specified userid.