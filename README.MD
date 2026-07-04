# Rigel Collection

Rigel Collection is a modular management software project that consolidates and reimagines 
several previous unfinished iterations developed during my learning process.

## What is Rigel Collection?
Rigel aims to provide a modular business management solution focused on inventory, sales, and operational control for small businesses.

## Will it work?

Well, the purpose of this project is to create different modules that communicate with each other. Rigel is the backend,
built with Spring Boot; rigel_dream is the frontend for the web view, built with React; and finally, I plan to add one
more module for the Android version, built with Jetpack Compose. It might include KMP, but I'm not sure.

## Technologies used

- Spring Boot
- React
- Jetpack Compose (planned)
- Kotlin Multiplatform (planned)
- PostgreSQL
- Docker

## Installation

To run the Rigel collection, you will need to have Java and Node.js installed on your machine. You can then clone the
repository and follow the next steps:

1. Navigate to the `rigel` directory and run `./gradlew build bootRun` to start the backend server.
2. Navigate to the `rigel_dream` directory and run `npm install` to install the dependencies, then run `npm run dev` to
   start the frontend server.

If you prefer using Docker, you can build and run the Docker container, but keep in mind that the container currently
only works for the backend service:

```bash
docker build -t rigel:1.0 .
docker run -p 8080:8080 rigel:1.0
```

If you want to use the backend service's JAR file directly, you can simply do the following:

```bash
java -jar Rigel/build/libs/Rigel-0.1.23.jar
```

Note: The JAR file name may vary depending on the version and build configuration, so make sure to adjust the command
accordingly. The normal thing is to first do a `./gradlew clean build`

## Contributing

If you want to contribute to the project, feel free to fork the repository and submit a pull request. Any help is
greatly appreciated!
