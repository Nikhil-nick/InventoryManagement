📦 Inventory Management System
A Spring Boot application to manage inventory items, including creation, reservation, and cancellation of item quantities.
Supports caching via Redis and exposes RESTFul APIs with Swagger documentation.

🛠 Tech Stack

| Component   | Tool                 |
| ----------- | -------------------- |
| Language    | Java 17     |
| Framework   | Spring Boot |
| ORM         | Spring Data JPA      |
| Database    | MySQL     |
| Caching     | Redis                |
| Build Tool  | Maven                |
| API Testing | Postman              |
| VCS         | Git & GitHub         |
| Testing     | JUnit, Mockito       |



🚀 Features

Create and retrieve inventory items

Reserve quantity for items

Cancel reservations

Redis caching for faster read performance

Unit and integration tests with JUnit 5

🧪 API Endpoints

1️⃣ Create Item

POST /api/items

2️⃣ Reserve Item

POST /api/items/{itemId}/reserve

2️⃣ Cancel Reserve

POST /api/items/reservation/{reservationId}/cancel

3️⃣ Get All Items

GET /api/items
4️⃣ Get Item by ID

GET /api/items/{id}