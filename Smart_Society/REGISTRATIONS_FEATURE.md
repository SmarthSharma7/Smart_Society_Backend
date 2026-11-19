# Event registrations API

The registration feature uses the existing PostgreSQL `event_registrations` and `society_members` tables. No schema migration is required.

All routes require the existing JWT bearer token.

- `POST /api/registrations/events/{eventId}` — registers the authenticated `STUDENT` for an approved, upcoming event. Request body is empty. The server uses the authenticated account; no student details are accepted from the browser.
- `GET /api/registrations/my` — returns registrations belonging to the authenticated account. Used by students and society members for their personal view.
- `GET /api/registrations/society` — returns registrations for events hosted by all societies in which the authenticated `SOCIETY_MEMBER` has an `ACTIVE` membership. Includes registrant name and email.

Registration checks include role, event approval, event start time, registration deadline, duplicate registration, and event capacity. A cancelled registration can be reactivated. The database's unique `(event_id, student_id)` constraint remains the final duplicate safeguard.
