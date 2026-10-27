# API Usage Examples

This guide demonstrates how to interact with the URL Shortening Service API using `curl.exe`.

## Prerequisites

Start the application locally and make sure it is accessible at `http://localhost:8080`.

The examples use PowerShell on Windows. If you use a different environment, adjust the command syntax accordingly.

## 1. Create a shortened URL

**Endpoint:** `POST /api/urls`

Create a shortened URL by sending the original URL in a JSON request body.

```powershell
curl.exe -i -X POST http://localhost:8080/api/urls `
  -H "Content-Type: application/json" `
  -d '{"originalUrl":"https://example.com/articles"}'
```

**Expected status:** `201 Created`

**Example response:**

```json
{
  "id": 1,
  "originalUrl": "https://example.com/articles",
  "shortCode": "abc1234",
  "createdAt": "2026-10-27T20:30:00Z"
}
```

The response contains the resource identifier, original URL, generated short code, and creation timestamp. The values above are illustrative; the application generates the actual values.

Save the returned `shortCode` to use in the following examples.

## 2. Retrieve a shortened URL

**Endpoint:** `GET /api/urls/{shortCode}`

Retrieve the details of an existing shortened URL.

```powershell
curl.exe -i http://localhost:8080/api/urls/abc1234
```

**Expected status:** `200 OK`

**Example response:**

```json
{
  "id": 1,
  "originalUrl": "https://example.com/articles",
  "shortCode": "abc1234",
  "createdAt": "2026-10-27T20:30:00Z"
}
```

If the short code does not exist, the API returns `404 Not Found` with an error response.

## 3. Update a shortened URL

**Endpoint:** `PUT /api/urls/{shortCode}`

Update the original URL associated with an existing short code.

```powershell
curl.exe -i -X PUT http://localhost:8080/api/urls/abc1234 `
  -H "Content-Type: application/json" `
  -d '{"originalUrl":"https://example.com/updated-article"}'
```

**Expected status:** `200 OK`

**Example response:**

```json
{
  "id": 1,
  "originalUrl": "https://example.com/updated-article",
  "shortCode": "abc1234",
  "createdAt": "2026-10-27T20:30:00Z"
}
```

The original URL is updated while the short code and creation timestamp remain unchanged.

An invalid request or URL returns `400 Bad Request`. If the short code does not exist, the API returns `404 Not Found`.

## 4. Delete a shortened URL

**Endpoint:** `DELETE /api/urls/{shortCode}`

Delete an existing shortened URL.

```powershell
curl.exe -i -X DELETE http://localhost:8080/api/urls/abc1234
```

**Expected status:** `204 No Content`

A successful deletion returns no response body.

If the short code does not exist, the API returns `404 Not Found`.

## 5. Redirect to the original URL

**Endpoint:** `GET /{shortCode}`

Access the short URL to redirect to its original destination.

```powershell
curl.exe -i http://localhost:8080/abc1234
```

**Expected status:** `302 Found`

**Example response (simplified):**

```http
HTTP/1.1 302
Location: https://example.com/updated-article
Content-Length: 0
```

The `Location` header contains the destination URL. The redirect response has no body.

The `-i` option displays the response headers without automatically following the redirect. To follow the redirect, use:

```powershell
curl.exe -i -L http://localhost:8080/abc1234
```

## 6. Retrieve URL statistics

**Endpoint:** `GET /api/urls/{shortCode}/statistics`

Retrieve the current statistics for a shortened URL.

```powershell
curl.exe -i http://localhost:8080/api/urls/abc1234/statistics
```

**Expected status:** `200 OK`

**Example response:**

```json
{
  "shortCode": "abc1234",
  "originalUrl": "https://example.com/updated-article",
  "createdAt": "2026-10-27T20:30:00Z",
  "accessCount": 4
}
```

The `accessCount` field reports the number of accesses recorded for the short URL. The example value is illustrative and may vary depending on the number of recorded accesses. Retrieving statistics does not increment the counter.

If the short code does not exist, the API returns `404 Not Found`.

## Error responses

Errors use the `ApiError` response structure.

**Example: `404 Not Found`**

```json
{
  "timestamp": "2026-10-27T22:43:41.572844192",
  "status": 404,
  "error": "Not Found",
  "message": "Short URL not found for code: abc1234",
  "path": "/api/urls/abc1234"
}
```

The timestamp, message, and path depend on the request that caused the error.

## HTTP status codes

| Status code | Description |
| --- | --- |
| `200 OK` | Request completed successfully. |
| `201 Created` | Shortened URL created successfully. |
| `204 No Content` | Shortened URL deleted successfully. |
| `302 Found` | Request redirected to the original URL. |
| `400 Bad Request` | Request payload or URL is invalid. |
| `404 Not Found` | Short code does not exist. |
| `500 Internal Server Error` | Unexpected server error. |

For the complete API contract, see the [OpenAPI specification](openapi.yaml).