# QuizHacker_back-end
> The backend of QuizHacker project, built on Spring Boot, powers the quiz creation, management, and user interactions providing RESTful API endpoints.<br>
>
> The client-end side (mobile app) of this project is available [here](https://github.com/alexonthespot7/QuizHackerMobile)<br>
>
> The production version is under [production branch](https://github.com/alexonthespot7/QuizHacker_back-end/tree/production)<br>

## Table of Contents
* [Usage Guide](#usage-guide)
* [Features](#features)
* [Technologies Used](#technologies-used)
* [Dependencies](#dependencies)
* [Documentation](#documentation)
* [Testing](#testing)
* [License](#license)

## Usage Guide
1. Clone the project <br>```git clone https://github.com/alexonthespot7/QuizHacker_back-end.git```<br>
2. Run the following command in a terminal window in the complete directory:<br>
```./mvnw spring-boot:run```<br>
3. Navigate to localhost:8080

## Features
- Restful Endpoints: Provides RESTful API endpoints for seamless communication with the front-end application.

- Authentication with JWT: Implements JSON Web Token (JWT) for secure authentication between the server-side and client-side applications.

- Leaderboard Display:
  - Get the top 10 rankings.
  - Authenticated users who have taken at least one quiz are on the leaderboard with their position.

- Personal Page:
  - Get user information: email, username, score, position in the leaderboard, attempts quantity, and average score among attempts.

- Personal Quizzes:
  - Get list of created quizzes.
  - Update existing quizzes.
  - Details include title, description, question count, rating by other users, time limit, category, and difficulty.

- Creating/Editing Quizzes:
  - Create quizzes with title, description, time limit, category, and difficulty.
  - Update questions and answers; each question has four answers, with one correct.

- List of Unattempted Quizzes:
  - Get quizzes created by other users that the current user hasn't taken.

- Quiz Taking Process:
  - Post quiz-taking attempt with list of questions and answers for them and the user's rating for the quiz.
  - Get score of attempt (questions answered * quiz difficulty: easy - 1, medium - 2, hard - 3).
  
## Technologies Used
- Java Spring Boot
- RESTful APIs
- JWT (JSON Web Token)
- smtp

## Dependencies
- **spring-boot-starter-web**: Starter for building web applications using Spring MVC.
- **spring-boot-devtools**: Provides development-time tools to enhance developer productivity. Automatically triggers application restarts, among other features.
- **spring-boot-starter-data-jpa**: Starter for using Spring Data JPA for database access.
- **h2**: H2 Database Engine, an in-memory relational database for development and testing purposes.
- **spring-boot-starter-security**: Starter for enabling Spring Security and authentication/authorization features.
- **spring-boot-starter-mail**: Starter for sending emails using Spring's JavaMailSender.
- **jjwt-api**: JSON Web Token (JWT) API provided by JJWT library.
- **jjwt-impl**: Implementation of the JSON Web Token (JWT) provided by JJWT library (runtime dependency).
- **jjwt-jackson**: Jackson integration for JSON Web Token (JWT) provided by JJWT library (runtime dependency).
- **spring-boot-starter-test**: Starter for testing Spring Boot applications.
- **spring-security-test**: Spring Security testing support for integration testing.
- **junit-jupiter-api**: JUnit 5 API for writing tests.
- **junit-jupiter-engine**: JUnit 5 test engine implementation.
- **spring-boot-starter-validation**: Starter for using validation in Spring Boot applications.

## Documentation
The documentation for this project is made with Swagger and can be accessed after launching the project at the following endpoints: 
1. [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html): if you're running the app on your pc.
2. [https://quiz-hacker-back-end.onrender.com/swagger-ui.html](https://quiz-hacker-back-end.onrender.com/swagger-ui.html): deployed app.
  
## Testing
### Usage Guide
1. Clone the project <br>```git clone https://github.com/alexonthespot7/QuizHacker_back-end.git```<br>
2. Run the following command in a terminal window (in the complete) directory:<br>
```./mvnw test```<br>
### Info
1. Controllers testing.
2. Repositories testing: CRUD functionalities + custom queries.
3. Rest endpoints methods testing.

## License
This project is under the MIT License.
