#!/bin/bash

kafka-console-consumer.sh --zookeeper $INOIO_ZK_IP --topic test3 --from-beginning
