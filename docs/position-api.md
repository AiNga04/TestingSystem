# Position API

Base URL: `/api/v1/positions` (`/api` is configured in application.properties).

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/` | Create, 201 with Location header |
| GET | `/{id}` | Get active position |
| GET | `/` | List active positions |
| GET | `/?deleted=true` | List deleted positions |
| PUT | `/{id}` | Update active position |
| DELETE | `/{id}` | Soft delete, 200 |
| PATCH | `/{id}/restore` | Restore, 200; no request body |

POST/PUT body:

```json
{"positionName":"DEV"}
```

Allowed names: `DEV`, `TEST`, `SCRUM_MASTER`, `PM`. The converter stores `Dev`, `Test`, `Scrum Master`, `PM` in MySQL. Missing/null/unknown names return 400. All four names already exist in the seed data, so creating them again returns 409. Names remain reserved after soft delete; restore the existing row instead.

Query parameters: `keyword` (matches the database name, e.g. Dev or Scrum), `deleted` (default false), `page` (default 0), `size` (1..100, default 10), `sortBy` (`positionId` or `positionName`), `direction` (`asc` or `desc`). IDs must be 1..255.

All endpoints use ApiResponse. Position data contains positionId, positionName and deletedAt (null when active). Lists use PageResponse.

Deleted positions are hidden from GET by ID, update and default lists. DELETE on an already deleted row returns 404; restore on an active row returns 409. A position with accounts cannot be deleted (409). Names are unique across active and deleted rows. Concurrent mutations of a position use row locks.

For a new Docker volume, 01_init.sql includes DeletedAt. To update an existing database while preserving rows:

```powershell
Get-Content database/migrations/04_position_soft_delete.sql -Raw | docker compose -f docker/docker-compose.yml exec -T mysql sh -c 'MYSQL_PWD=$MYSQL_ROOT_PASSWORD mysql -u root'
```

Restart the application after updating the code/schema. Run `mvnw.cmd test` with MySQL and .env available. API tests use rollback transactions; successful create/delete service paths also have isolated tests because seed data reserves all four enum names.
