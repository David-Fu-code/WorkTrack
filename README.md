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
| GET    | /api/v1/auth/users/me | Get current user profile |
| PUT    | /api/v1/auth/users/me | Update profile (display name) |
| PUT    | /api/v1/auth/users/me/password | Change password |

### Admin

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET    | /api/v1/auth/admin/users | Get list of all users (ADMIN only) |

---

## Postman

✅ This project was fully tested using Postman.

---

## Email Testing (MailDev)

✅ For testing email confirmation and password reset flows, [MailDev](https://maildev.github.io/maildev/) was used.

👉 To start MailDev (if installed globally):

```bash
npm install -g maildev
maildev

MailDev runs at:
SMTP → port 1025
Web UI → http://localhost:1080

---
## How to Run

### 1️⃣ Clone the repo

```bash
git clone https://github.com/yourusername/worktrack.git
cd worktrack-auth-backend
