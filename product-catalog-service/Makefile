COMPOSE_FILE := docker-compose.local.yml
ENV_FILE := .env
ENV_EXAMPLE := .env.example
COMPOSE := docker compose -f $(COMPOSE_FILE) --env-file $(ENV_FILE)

.PHONY: setup run test verify doctor infra-up infra-down reset logs clean ensure-env

setup:
	./scripts/setup.sh

run:
	./scripts/run-local.sh

test:
	./scripts/test.sh

verify: test

doctor:
	./scripts/doctor.sh

infra-up: ensure-env
	$(COMPOSE) up -d

infra-down:
	@if [ -f "$(ENV_FILE)" ]; then \
		$(COMPOSE) down; \
	else \
		docker compose -f $(COMPOSE_FILE) down; \
	fi

reset:
	./scripts/reset-local.sh

logs: ensure-env
	$(COMPOSE) logs -f

clean:
	rm -rf target .flattened-pom.xml

ensure-env:
	@if [ ! -f "$(ENV_FILE)" ]; then \
		cp "$(ENV_EXAMPLE)" "$(ENV_FILE)"; \
		echo "Created $(ENV_FILE) from $(ENV_EXAMPLE)."; \
	fi
