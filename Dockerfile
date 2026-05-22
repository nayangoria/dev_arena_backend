# Step 1 — Start from a machine that already has Maven + Java 17
# This is called a "base image" — someone already set up Java for us
FROM maven:3.9-eclipse-temurin-17 AS builder

# Step 2 — Set the working folder inside the container
WORKDIR /app

# Step 3 — Copy pom.xml first
# We do this separately because Docker is smart — if pom.xml hasn't changed,
# it won't re-download all dependencies next time. Saves a lot of time.
COPY pom.xml .

# Step 4 — Download all dependencies
RUN mvn dependency:go-offline -B

# Step 5 — Copy the rest of your code
COPY src ./src

# Step 6 — Build the jar file (skip tests for now)
RUN mvn package -DskipTests -B

# ── Now start fresh with a smaller image ──
# We don't need Maven anymore, just Java to RUN the jar
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Step 7 — Copy ONLY the jar from the builder above
COPY --from=builder /app/target/*.jar app.jar

# Step 8 — Tell Docker this app uses port 8080
EXPOSE 8080

# Step 9 — This command runs when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]