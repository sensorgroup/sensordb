## SensorDB Data Lifecycle
![](https://github.com/alisalehi/sensordb/raw/master/documents/data-lifecycle.png)

## High-level architecture of SensorDB
![](https://github.com/alisalehi/sensordb/raw/master/documents/big-picture.png)

To compile javascript, use _coffee -o src/main/webapp/j/ -wc src/main/coffeescript/*.coffee_

To compile CSS use *lessc main.less >main.css*

If you would like to use Cassandra for storing sensor data, start it with (v1.0.8+):  *sudo ./cassandra*

Note that before using sensordb, you need to create sensordb namespace within cassandra.

GUI to investigate contents of cassandra, use [Cassandra-Cluster-Admin(PHP)](https://github.com/sebgiroux/Cassandra-Cluster-Admin.git)

Redis (v2.4.8+) compiled in 32bit mode *redis-server /path/to/sensordb/conf/redis.conf*

## Data Model
Data model is stored on MongoDB(V2.0.4+) (./mongod --journal). In SensorDB:

-  Any user can have zero or more experiments
-  Any experiment can have zero or more nodes
-  Any node can have zero or more streams. Each node can also have latitude, longitude and altitude values.
-  Any stream is a set of (timestamp,value) pairs. Each stream has one unit of measurement.

Internally, timestamps are presented in unix format (milliseconds since epoch). Values (sensor readings) are presented as real numbers (double precision float).

### Data model in a Tree View
![](https://github.com/alisalehi/sensordb/raw/master/documents/er-tree.png)

### SensorDB's logical ER diagram

![](https://github.com/alisalehi/sensordb/raw/master/documents/e-r-diagram.png)

### Data Stream Insertion Process diagram

![](https://github.com/alisalehi/sensordb/raw/master/documents/data-flow.png)


Markdown syntax http://daringfireball.net/projects/markdown/syntax


