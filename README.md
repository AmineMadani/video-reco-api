 Video Recommendation API

A simple **Spring Boot REST API** for managing and searching videos (movies, series, etc.)
The project demonstrates validation, search, similarity detection, and soft deletion of videos.

---

 Features
- Add a new video (movie, series, or base type).
- Retrieve a video by its ID.
- Search videos by title token.
- List all movies or all series.
- Soft delete a video (kept in memory but excluded from results).
- Retrieve deleted video IDs.
- Find similar videos based on common labels.
- Preload initial data from `videos.json`.

---
 Project Structure

video-reco-api/
└─ src/
├─ main/
│ ├─ java/com/example/videos/
│ │ ├─ controller/ # REST controllers
│ │ ├─ model/ # Entities (Video, VideoType)
│ │ ├─ repo/ # In-memory repository
│ │ └─ service/ # Business logic & validation
│ └─ resources/
│ ├─ application.properties
│ └─ videos.json # Initial dataset
└─ test/
└─ java/com/example/videos/
└─ VideoControllerTest.java

Tech Stack
- **Java 17**
- **Spring Boot 3**
- **Maven**
- **Jackson** (JSON serialization/deserialization)
- **JUnit 5** + **Spring Boot Test** (integration & unit tests)
- **Swagger/OpenAPI** for API documentation

How to Run

1. Clone the repository:
   git clone https://github.com/AmineMadani/video-reco-api.git
2. cd video-reco-api
3. Build the project:
   mvn clean install 
4. Run the application:
   mvn spring-boot:run

The API will be available to test at:

http://localhost:8080/api/v1/videos

5. Tests

   Run all tests:
   mvn test

Tests include:

Valid and invalid video creation.
Search functionality.
Deletion and deleted IDs tracking.
Similarity matching.
JSON preload validation.

Notes

- @JsonInclude(JsonInclude.Include.NON_NULL) ensures only relevant fields are serialized in JSON responses.

- Videos are stored in memory (InMemoryRepo) for simplicity.
