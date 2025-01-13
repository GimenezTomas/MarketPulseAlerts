levantar db
docker run -d \
--name mkt-pulse-postgres \
-e POSTGRES_USER=user \
-e POSTGRES_PASSWORD=password \
-e POSTGRES_DB=market_pulse_alerts \
-p 5432:5432 \
postgres
