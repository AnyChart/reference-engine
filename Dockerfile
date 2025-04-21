# Use Clojure base image for building
FROM clojure:openjdk-8-lein as builder

# Install required build dependencies
RUN apt-get update && apt-get install -y \
    closure-compiler \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

RUN apt-get update && apt-get install -y ca-certificates && update-ca-certificates

# Set working directory
WORKDIR /build

# Copy project files
COPY project.clj /build/
COPY src /build/src
COPY resources /build/resources
COPY test /build/test
COPY src-scss /build/src-scss

# Download dependencies first (better caching)
RUN lein deps

# Compile SASS and build JAR
RUN lein sass4clj once && \
    lein uberjar

# Final runtime image
FROM openjdk:8-jdk

# Set working directory
WORKDIR /apps/reference-local

# Create necessary directories
RUN mkdir -p /apps/reference-local/data /var/log/supervisor /usr/local/bin

# Install Node.js
RUN apt-get update && apt-get install -y nodejs \
  npm \
  && npm install -g jsdoc \
  apt-get clean && rm -rf /var/lib/apt/lists/*

# Copy the built JAR from builder stage
COPY --from=builder /build/target/reference-3.0.0-standalone.jar /apps/reference-local/

# Copy configuration
COPY .docker/reference-local.toml /apps/reference-local/reference-local.toml

# Copy static resources from builder stage
COPY --from=builder /build/resources/public /apps/reference-local/resources/public


# Expose port
EXPOSE 8080

# Command to run the application
CMD ["java", "-jar", "/apps/reference-local/reference-3.0.0-standalone.jar", "all", "/apps/reference-local/reference-local.toml"]
