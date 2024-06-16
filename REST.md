## `/domains  
  
### GET  
  
**Description:** Returns all the domains available with their informations: domain name, userId that registered the domain, purchase date, expiration date. If a `userId` is provided as a query parameters, it returns only `userId`'s domains 
  
**Parameters:** 
* `userId`: Optional. The unique identifier of the user.
  
**Reqeuest body:** Empty.  
  
**Response**: Returns a JSON array of domain JSON objects.
  
**Response Code:**  
* `200`: Successful request
* `404 Not Found`: No domains have been found
* `500 Internal Server Error`
  

## `/domains/{domain}/availability`  
  
### GET  
  
**Description:** Returns if a domain name is available. 
  
**Parameters:**  
* `domain`: Required. The domain name to check.
  
**Request body:** Empty.
  
**Response**: Returns a JSON if domain is available: {`domain` = `domainName`, `availability` = `true` | `false`}.  
  
**Codici di stato restituiti:**  
* `400 Bad Request`: c'è un errore nel JSON fornito dal client (mancano dei dati o la sintassi è errata).
* `404 Not Found`: l'id del film è inesistente. 
* `500 Internal Server Error`
  
  
## `/aggiungiPosto/{idProiezione}{posti}  
  
### POST  
  
**Descrizione:** crea una nuova prenotazione, restituendone l'URL.  
  
**Parametri:**  
* idProiezione: numero intero identificatore di un film.  
* posti: lista JSON di posti.  
  
**Body richiesta:** un oggetto JSON contenente l'`id` della proiezione e una lista `posti` di interi.  
  
**Risposta**: in caso di successo viene restituito un oggetto JSON avente un campo `motivation` contenente un messaggio di successo e un campo `location` che è l'URL
creato per la prenotazione.  
  
**Codici di stato restituiti:**  

* `400 Bad Request`: c'è un errore nel JSON fornito dal client (mancano dei dati o i parametri non sono corretti).
* `404 Not Found`: il posto scelto non esiste nella sala.
* `406 Not Acceptable` : qualche posto di quelli scelti sono già occupati oppure non ci sono abbastanza posti disponibili.
* `409 Conflict`: il client ha fornito una lista di posti vuota.
* `500 Internal Server Error`
  
  
## `/aggiungiPosto/{idProiezione}{idPrenotazione}{posti}  
  
### PUT  
  
**Descrizione:**  aggiunge dei posti ad una prenotazione già esistente, rendendoli occupati.
  
**Parametri:**  
* idProiezione: numero intero identificatore di un film.  
* idPrenotazione: numero intero identificatore di una prenotazione.  
* posti: lista JSON di posti.  
  
**Body richiesta:** un oggetto JSON contenente l'`id` della proiezione, l'`id` della prenotazione e una lista `posti` di interi.  
  
**Risposta**: se la richiesta ha avuto successo i posti dati vengono raggiunti alla prenotazione.  
  
**Codici di stato restituiti:**  
* `400 Bad Request`: c'è un errore nel JSON fornito dal client (mancano dei dati o i parametri non sono corretti).
* `404 Not Found`: il posto scelto non esiste nella sala.
* `406 Not Acceptable`: qualche posto di quelli scelti sono già occupati oppure non ci sono abbastanza posti disponibili.
* `409 Conflict`: il client ha fornito una lista di posti vuota.
* `500 Internal Server Error`
  
  
## `/rimuoviPosti/{idProiezione}{idPrenotazione}{posti}  
  
### DELETE  
  
**Descrizione:**  rimuove dei posti da una prenotazione, rendendoli di nuovo disponibili.
  
**Parametri:**  
* idProiezione: numero intero identificatore di un film.  
* idPrenotazione: numero intero identificatore di una prenotazione.  
* posti: lista JSON di posti.  
  
**Body richiesta:** un oggetto JSON contenente l'`id` della proiezione, l'`id` della prenotazione e una lista `posti` di interi.  
  
**Risposta**: se la richiesta ha avuto successo, i posti vengono rimossi dalla prenotazione.  
  
**Codici di stato restituiti:**
* `200 OK`: i posti sono stati rimossi dalla prenotazione con successo.
* `400 Bad Request`: c'è un errore nel JSON fornito dal client (mancano dei dati o i parametri non sono corretti).
* `404 Not Found`: il posto scelto non esiste nella sala.
* `406 Not Acceptable`: qualche posto di quelli scelti sono già occupati oppure non ci sono abbastanza posti disponibili.
* `406 Not Acceptable`: i posti selezionati non esistono oppure nella lista ci sono dei posti duplicati.
* `500 Internal Server Error`