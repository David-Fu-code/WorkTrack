# WorkTrack Auth Backend - Spring Boot JWT Authentication

🚀 **Production-ready Backend Authentication System** built with Java + Spring Boot + PostgreSQL

---

## Features

✅ User Registration with Email Confirmation  
✅ Secure Login → JWT + Refresh Token  
✅ Refresh Token flow → get new Access Token  
✅ Secure Logout → Invalidate Refresh Token  
✅ User Profile → `/users/me`  
✅ Update Profile (Display Name)  
✅ Change Password (with old password verification)  
✅ Password Reset → Forgot Password + Reset with Token  
✅ Role-based Authorization → Admin Endpoints  
✅ Admin → List all users → `/admin/users`  
✅ Full security flow tested  
✅ Production-ready architecture

---

## Tech Stack

- Java 21
- Spring Boot 3
- Spring Security 6
- JWT (JSON Web Tokens)
- PostgreSQL
- Hibernate / JPA
- Lombok
- RESTful API
- Postman for testing

---

## API Endpoints

### Auth

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST   | /api/v1/auth/register | Register new user |
| POST   | /api/v1/auth/login | Login → Get access + refresh token |
| POST   | /api/v1/auth/refresh-token | Get new access token using refresh token |
| POST   | /api/v1/auth/logout | Invalidate refresh token |
| POST   | /api/v1/auth/forgot-password | Request password reset |
| POST   | /api/v1/auth/reset-password | Reset password with token |

### Users

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET    | /api/v1/users/me | Get current user profile |
| PUT    | /api/v1/users/me | Update profile (display name) |
| PUT    | /api/v1/users/me/password | Change password |

### Admin

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET    | /api/v1/admin/users | Get list of all users (ADMIN only) |

## Job Applications

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST   | /api/v1/applications | Create a new application |
| GET    | /api/v1/applications | Get user's applications |
| PUT    | /api/v1/applications/{id} | Update an application |
| DELETE | /api/v1/applications/{id} | Delete an application |

---

## Postman

✅ This project was fully tested using Postman.

---
## Postman Collection

👉 [Download WorkTrack-Auth-API.postman_collection.json](./postman/WorkTrack-Auth-API.postman_collection.json)
---

## Email Testing (MailDev)

✅ For testing email confirmation and password reset flows, [MailDev](https://maildev.github.io/maildev/) was used.

👉 To start MailDev (if installed globally):

MailDev runs at:
SMTP → port 1025
Web UI → http://localhost:1080**

```bash
npm install -g maildev
maildev
```
---
## How to Run

### 1️⃣ Clone the repo

```bash
git clone https://github.com/David-Fu-code/WorkTrack.git
```
---
## How to Test the API (Example Flows)

### 1️⃣ Register a new user
```bash
http
POST /api/v1/auth/register
Content-Type: application/json

{
    "email": "test@example.com",
    "password": "YourPassword123!",
    "displayName": "Test User"
}
```
✅ An email confirmation will be sent → view it in MailDev → confirm the email.
---

### 2️⃣ Login
```bash
http
POST /api/v1/auth/login
Content-Type: application/json

{
    "email": "test@example.com",
    "password": "YourPassword123!"
}
```
✅ Response:

```bash
json
{
    "accessToken": "JWT_TOKEN_HERE",
    "refreshToken": "REFRESH_TOKEN_HERE"
}
```
---

### 3️⃣ Using the access token
For all protected endpoints → add header:

```bash
http
Authorization: Bearer JWT_TOKEN_HERE
```
Example:
```bash
http
GET /api/v1/auth/users/me
Authorization: Bearer JWT_TOKEN_HERE
```
---
### 4️⃣ Refresh Token
```bash
http
POST /api/v1/auth/refresh-token
Content-Type: application/json

{
    "refreshToken": "REFRESH_TOKEN_HERE"
}
```
---
### 5️⃣ Logout
```bash
http
POST /api/v1/auth/logout
Content-Type: application/json

{
    "refreshToken": "REFRESH_TOKEN_HERE"
}
```
---
### 6️⃣ Password Reset - Forgot Password
```bash
http
POST /api/v1/auth/forgot-password
Content-Type: application/json

{
    "email": "test@example.com"
}
```
✅ A password reset email will be sent → view in MailDev → get token → use it:

```bash
http
POST /api/v1/auth/reset-password
Content-Type: application/json

{
    "token": "TOKEN_FROM_EMAIL",
    "newPassword": "NewPassword123!"
}
```
---
### 7️⃣ Update Profile
```bash
http
PUT /api/v1/auth/users/me
Authorization: Bearer JWT_TOKEN_HERE
Content-Type: application/json

{
    "name": "New Display Name"
}
```
---
### 8️⃣ Change Password
```bash
http
PUT /api/v1/auth/users/me/password
Authorization: Bearer JWT_TOKEN_HERE
Content-Type: application/json

{
    "currentPassword": "YourPassword123!",
    "newPassword": "YourNewPassword123!"
}
```
### 9 JobApplication (Create)
```bash
http
POST http://localhost:8080/api/v1/applications
Authorization: Bearer <token>
Content-Type: application/json

{
    "companyName": "Google",
    "position": "Backend Developer",
    "status": "APPLIED",
    "appliedDate": "2025-06-08",
    "notes": "Referred by a friend"
}
```

### 10 JobApplication (Get all applications per user)
```bash
GET http://localhost:8080/api/v1/applications
Authorization: Bearer <token>
}
```

### 11 JobApplication (update)
```bash
json
PUT http://localhost:8080/api/v1/applications/1
Authorization: Bearer <your_token>
Content-Type: application/json

{
  "companyName": "Google",
  "position": "Backend Developer",
  "status": "INTERVIEW",
  "appliedDate": "2025-06-08",
  "notes": "They sent me a HackerRank test"
}
```

### 12 JobApplication (Delete)
```bash
http
DELETE http://localhost:8080/api/v1/applications/1
Authorization: Bearer <your_token>
```
---
## Notes


- ✅ All flows require proper usage of JWT access token → in Authorization: Bearer ... header.
- ✅ Email confirmations and password reset tokens can be viewed in MailDev: http://localhost:1080.
- ✅ A complete Postman collection is available to test all flows.

---

## Author

**David Fu**  
[LinkedIn](https://linkedin.com/in/david-fu-ji) · [GitHub](https://github.com/David-Fu-code)  

Junior Backend Developer · Java | Spring Boot | PostgreSQL · Based in Dublin / Remote

---

## License

MIT

---

## Improvements (Planned)

- [ ] Add Docker Compose for PostgreSQL (easy local setup)
- [ ] Add pagination and filtering to `/admin/users`
- [ ] Add integration tests with Testcontainers
- [ ] Add Swagger / OpenAPI documentation
- [ ] Add email rate-limiting (to prevent spam on forgot-password)
- [ ] Add account deletion flow







