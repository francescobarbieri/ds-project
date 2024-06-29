# Distributed System Project 2023/2024 - Domain Broker

## Group components

* Francesco Barbieri 856375 f.barbieri17@campus.unimib.it

## Description

The project consists of the design and development of a distributed application for the purchase and management of Internet domains.

Here you can find the full [specification pdf](/_utils/specifications.pdf).

![homepage](/_utils/1.png)
![buy domain page](/_utils/2.png)
![domains list page](/_utils/3.png)
![order list page](/_utils/4.png)
![register page](/_utils/5.png)

## System Components

- **Web Client**: Provides a user-friendly web interface for domain registration and management. It communicates with the Web Server via REST APIs.

- **Web Server**: Handles domain management logic and serves as an intermediary between the Web Client and the database. It exposes REST APIs to the Web Client and utilizes a custom TCP socket-based protocol to communicate with the database.

- **Database**: A minimal, document-oriented, agnostic database that stores data as unprocessed strings. It communicates with the Web Server via TCP sockets.

## Compilation and Execution

Using Visual Studio Code:

* Client Web: VS Code live server
* Server Web: Open a new terminal and run `mvn jetty:run`
* Database: Open a new terminal and run `mvn compile` first, then `mvn exeec:java`

## Ports and addresses
The web server listens to the `localhost` address on port `8080`. The database listens to the same address as the web server but on port `3030`.