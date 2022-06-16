# Prerequisites

1. **Docker 19.03** or above and permission to [manage Docker as a non-root user](https://docs.docker.com/engine/install/linux-postinstall/#manage-docker-as-a-non-root-user)
2. **Docker Compose 1.25** or above

## Start game
```sh
docker-compose up
```
## APIS
### Rest API
```
http://localhost:9190/docs/shared-game-api.html
```

### WS
```
wss://localhost:9194/ws
```

## Stop game
```sh
docker-compose down
```

## Clean up all
```sh
docker-compose down -v
```