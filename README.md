# FreeCell

A FreeCell solitaire web application powered by a Java game engine, a Spring
Boot REST API, and a React + TypeScript interface.

## Demo

https://github.com/user-attachments/assets/48a74fba-6ce7-46eb-b086-3e532f4b8062

## Requirements

- Java Development Kit (JDK) 21 or newer
- Apache Maven 3.9 or newer
- Node.js 20 or newer and npm

## Run the game

```sh
mvn spring-boot:run
```

Open http://localhost:8080 in your browser. Maven builds the React frontend and
Spring Boot serves both the web interface and the `/api/game` endpoints.

### How to play

- At launch, review the rules and select **I understand — Start playing**.
- Click a stack to select its top card, then click a destination stack to move
  it (or click the same stack again to clear the selection).
- Use **Undo** to reverse the last move, **New deal** to start a new game,
  **Rules** to review the rules, and **Stats** to view your session results.
- The toolbar tracks your move count and elapsed time, pausing when all 52
  cards reach the foundations.

## Run the tests

```sh
sh test.sh
```

This builds the project and runs both the Spring Boot API tests and the Java
FreeCell engine test suite.

Card-face art credits and license details are in
[CARD-ART-CREDITS.txt](CARD-ART-CREDITS.txt).
