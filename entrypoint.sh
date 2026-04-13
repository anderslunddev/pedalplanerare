#!/bin/sh
# Convert Render's DATABASE_URL (postgres://user:pass@host:port/db)
# into Spring Boot datasource properties.
if [ -n "$DATABASE_URL" ]; then
  # Strip credentials from the URL for the JDBC connection string
  HOST_PORT_DB="${DATABASE_URL#*@}"
  SPRING_DATASOURCE_URL="jdbc:postgresql://${HOST_PORT_DB}"
  export SPRING_DATASOURCE_URL

  USERINFO="${DATABASE_URL#*://}"
  USERINFO="${USERINFO%%@*}"
  export SPRING_DATASOURCE_USERNAME="${USERINFO%%:*}"
  export SPRING_DATASOURCE_PASSWORD="${USERINFO#*:}"
  export SPRING_DATASOURCE_DRIVER_CLASS_NAME="org.postgresql.Driver"
fi

exec java -jar /app/app.jar "$@"
