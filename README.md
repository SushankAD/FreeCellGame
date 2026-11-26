# FreeCell

A full-stack FreeCell web application built with Java, Spring Boot, React,
and TypeScript. The Java engine remains independent of the web presentation
layer, with a REST API and a responsive browser UI.

## Requirements

- Java Development Kit (JDK) 21 or newer
- Apache Maven 3.9 or newer
- Node.js 20 or newer
- npm

## Run the game

```sh
mvn spring-boot:run
```

Open http://localhost:8080. The Spring Boot application serves the React
single-page application and exposes the game through `/api/game`.

For frontend development:

```sh
cd frontend
npm install
npm run dev
```

The React development server runs on http://localhost:5173 and proxies API
requests to Spring Boot.

## Run the tests

Run the test suite from this directory:

```sh
sh test.sh
```

The script runs the model and game checks through Maven.
