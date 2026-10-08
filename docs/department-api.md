# Department API

Base URL: `/api/v1/departments`

The `/api` prefix is configured with `server.servlet.context-path=/api` in application.properties. Controller mappings use `/v1/departments`. Postman baseUrl should be `http://localhost:8080` when requests already include `/api` (avoid `/api/api`). Restart the application after changing the context path.

| Method | Path | Success |
| --- | --- | --- |
| POST | `/` | 201 + Location header |
| GET | `/{id}` | 200 |
| GET | `/` | 200, paginated data |
| PUT | `/{id}` | 200 |
| DELETE | `/{id}` | 200, data = null |

POST/PUT body:

```json
{"departmentName":"Engineering"}
```

Names are required, at most 30 characters, trimmed before saving, and unique according to the database collation. IDs must be between 1 and 255 to match TINYINT UNSIGNED.

List parameters: `keyword`, `page` (0-based, default 0), `size` (1..100, default 10), `sortBy` (`departmentId` or `departmentName`), `direction` (`asc` or `desc`). Sorting by name also sorts by ID for deterministic paging.

All responses share the same envelope:

```json
{
  "success": true,
  "status": 200,
  "code": "SUCCESS",
  "message": "Lấy phòng ban thành công",
  "data": {"departmentId": 1, "departmentName": "Engineering"},
  "errors": {},
  "timestamp": "2026-10-08T12:00:00Z"
}
```

For lists, data contains `content`, `page`, `size`, `totalElements`, `totalPages`.

Errors return data = null and success = false. HTTP status matches the status field:

- 400: invalid input, JSON, ID, pagination or sorting; body validation includes field messages in errors.
- 404 / RESOURCE_NOT_FOUND: department does not exist.
- 409 / DUPLICATE_RESOURCE: name already exists.
- 409 / RESOURCE_CONFLICT: department still has accounts.
- 409 / DATA_INTEGRITY_CONFLICT: database constraint violation, including concurrent writes.
- 405 / HTTP_405: unsupported method; Allow header is preserved.
- 500 / INTERNAL_ERROR: unexpected failure; details are logged on the server.

This changes the earlier raw response bodies and changes DELETE from 204 to 200. Clients must read results from `data`.

Run `mvnw.cmd test` from the project root with `.env` and MySQL initialized using the SQL scripts. Department API integration tests run in rollback transactions, preserving records; MySQL auto-increment counters may advance.
