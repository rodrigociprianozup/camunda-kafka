# Kafka Camunda Publisher - example simple

This module uses automatic BPMN deployment

## Tecnologias utilizadas

- Java
- Maven
- PostgreSQL
- Docker
- Spring Boot
- Kafka
- Camunda
- Kafka Connector
- Redis

## Instalação

- Docker (No caso de sistemas linux, ex.: Linux Ubuntu 18.04, para detalhes segue o link - compose install - na 
descrição): 

    ```
    $ sudo apt update && sudo apt upgrade
    
    $ sudo apt-get install apt-transport-https ca-certificates curl gnupg software-properties-common
    
    $ curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
    
    $ sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu bionic stable"
    
    $ sudo apt install docker-ce
    
    $ sudo systemctl enable docker
    
    $ sudo systemctl start docker
    
    $ sudo systemctl status docker
    
    $ docker -v
    ```
- Docker Compose: https://docs.docker.com/compose/install/

    ```
    $ sudo apt-get install docker-compose
    ```
- Maven: https://maven.apache.org/install.html

## Iniciando os serviços

### 1. Iniciar o docker compose

```
$ docker-compose -f docker-compose-redis.yml up
```

### 2. Build no projeto

```
$ mvn clean install
```


### 3. Configurar Redis
curl -s -X POST -H 'Content-Type: application/json' --data @redis-sink-config.json http://localhost:8083/connectors


### 4. Verificar banco Redis
#docker-compose -f docker-compose-redis.yml exec redis redis-cli

### 5. Ao finalizar rodar
docker-compose -f docker-compose-redis.yml down --remove-orphans

