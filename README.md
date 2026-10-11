# Support Ticket Triage API

A Spring Boot REST API for managing customer support tickets. When a ticket is
created, it is saved first, then classified by an LLM (Gemini) into a category
and a priority, with a suggested reply. If the AI call fails, the ticket is
still saved, so no customer message is lost.

The project code is in the [project folder](ticket-trinage).

## Tech stack

Java 17, Spring Boot, Spring Data JPA (Hibernate), MySQL, Gemini API,
Swagger (springdoc), JUnit 5, Mockito, Maven

## How it works

1. `POST /api/tickets` receives a subject and a message and validates them.
2. The ticket is saved to MySQL with status `NEW`.
3. `AiTriageService` calls the Gemini API with a JSON schema, so the reply has
   a fixed shape: category, priority and suggested reply.
4. On success, the ticket becomes `TRIAGED` with those fields filled in.
5. On failure, the ticket stays `NEW` with category `UNCLASSIFIED`.
   - Temporary errors (HTTP 429 and 5xx) get one retry after 2 seconds.
   - Permanent errors (wrong key, bad model name) are not retried.

Values sent by the client for id, status, category, priority and reply are
ignored, so a client cannot fake an AI result. The system instruction tells the
model to treat ticket text as data and never follow instructions inside it.

## Endpoints

| Method | URL | Purpose |
|---|---|---|
| POST | `/api/tickets` | Create a ticket and triage it with AI |
| GET | `/api/tickets` | List tickets (optional `?status=NEW`) |
| GET | `/api/tickets/{id}` | View one ticket |
| PATCH | `/api/tickets/{id}/status` | Change the status |
| DELETE | `/api/tickets/{id}` | Delete a ticket |

Errors come back as clean JSON (for example 404 for an unknown id and 400 for
invalid input) through a global exception handler.

## Example

Request:

```json
{ "subject": "Payment Failed", "message": "I was charged twice but my order shows unpaid" }
```

Response (201):

```json
{
  "id": 9,
  "subject": "Payment Failed",
  "message": "I was charged twice but my order shows unpaid",
  "status": "TRIAGED",
  "category": "BILLING",
  "priority": "HIGH",
  "suggestedReply": "We apologize for the double charge and any frustration this has caused. Our billing team is looking into this issue to resolve the discrepancy and process your refund as quickly as possible.",
  "createdAt": "2026-10-11T14:00:43"
}
```

## Run locally

1. Create a MySQL database named `ticketdb`.
2. Set these environment variables:
   - `DB_PASSWORD`: your MySQL root password
   - `GEMINI_API_KEY`: a key from Google AI Studio (without it the app still
     runs, and every ticket goes to the fallback)
3. Run `TicketTriageApplication` (or `./mvnw spring-boot:run` from the project folder).
4. Open http://localhost:8081/swagger-ui/index.html

The model name is the `gemini.model` setting in `application.properties`
(currently `gemini-3.5-flash-lite`). Model names change over time, so update it
if the API returns a 404.

## Tests

- `TicketServiceTest`: 8 JUnit 5 and Mockito unit tests covering AI success,
  AI failure fallback, save-before-AI ordering, ignored client values, and
  not-found handling. They need no database, network or API key.
- `AiAccuracyTest`: an evaluation against the real Gemini API. It runs only
  when `GEMINI_API_KEY` is set.

## Evaluation

I labelled 30 sample support tickets by hand before running the test, and sent
each one through the LLM triage (model: gemini-3.5-flash-lite).

- **Clear set:** 20 / 20 correct categories (4 each for billing, delivery,
  technical, account and other)
- **Hard set:** 9 / 10 correct categories. It covers mixed issues, vague text,
  broken English, all-caps, Hinglish, borderline cases and a prompt-injection
  attempt
- The one miss was the vague ticket "Nothing works, please help", which I
  labelled OTHER and the model labelled TECHNICAL
- Priority is not scored, since it is subjective
- The sets are small, so these figures are a sanity check, not a production
  accuracy claim

## Possible improvements

- Authentication (JWT) so only support staff can read tickets
- A larger labelled test set and a confidence score
- A review queue for `UNCLASSIFIED` tickets
- Filtering tickets by category and priority
