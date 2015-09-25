Reactive Kafka Activator template
===============================

Demonstrates Akka streams wrapper for Apache Kafka: https://github.com/softwaremill/reactive-kafka

## Gettting started

The repo has been modified to use a local kafka / zookeeper installation.

### Start kafka

```
bash-3.2$ cd docker
bash-3.2$ docker-compose up -d
Starting docker_zookeeper_1...
Starting docker_kafka_1...
bash-3.2$ docker-compose ps
       Name                     Command               State                          Ports                        
-----------------------------------------------------------------------------------------------------------------
docker_kafka_1       /bin/sh -c start-kafka.sh        Up      0.0.0.0:32773->9092/tcp                             
docker_zookeeper_1   /bin/sh -c /usr/sbin/sshd  ...   Up      0.0.0.0:32772->2181/tcp, 22/tcp, 2888/tcp, 3888/tcp 
```

### Install direnv

```
brew install direnv
```

### Modify .envrc

```
# adjust these settings
export INOIO_DOCKER_IP=`boot2docker ip`
export INOIO_KAFKA_IP=$INOIO_DOCKER_IP:32771
export INOIO_ZK_IP=$INOIO_DOCKER_IP:32770
```

### start direnv

```
direnv allow .
```

### start sample

```
sbt run
```


### hack

```
sbt
eclipse with-source=true
```
