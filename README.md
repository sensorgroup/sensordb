
![](https://github.com/alisalehi/sensordb/raw/master/documents/big-picture.png)

To compile javascript, use _coffee -o public/j/ --watch --compile app/assets/javascripts/*.coffee_

To compile CSS use *lessc main.less >main.css*

If you would like to use Cassandra for storing sensor data, start it with (v1.0.8+):  *sudo ./cassandra*

GUI to investigate contents of cassandra, use [Cassandra-Cluster-Admin(PHP)](https://github.com/sebgiroux/Cassandra-Cluster-Admin.git)

Markdown syntax (http://daringfireball.net/projects/markdown/syntax)