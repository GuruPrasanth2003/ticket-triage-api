# Support Ticket Triage API

A Spring Boot REST API for managing customer support tickets, with AI-based
category and priority classification (in progress).

## Tech stack
Java 17, Spring Boot, Spring Data JPA, MySQL, Swagger

## Run locally
1. Create a MySQL database named `ticketdb`.
2. Set the environment variable `DB_PASSWORD` to your MySQL password.
3. Run `TicketTriageApplication`.
4. Open http://localhost:8081/swagger-ui/index.html

## Endpoints
- POST /api/tickets
- GET /api/tickets (optional ?status=NEW)
- GET /api/tickets/{id}
- PATCH /api/tickets/{id}/status
- DELETE /api/tickets/{id}