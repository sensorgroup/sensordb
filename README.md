## SensorDB Data Lifecycle
![](https://github.com/alisalehi/sensordb/raw/master/documents/data-lifecycle.png)

## High-level architecture of SensorDB
![](https://github.com/alisalehi/sensordb/raw/master/documents/big-picture.png)

To compile javascript, use _coffee -o src/main/webapp/j/ -wc src/main/coffeescript/*.coffee_

To compile CSS use *lessc main.less >main.css*

If you would like to use HBase for storing sensor data, start it with (v0.92.1):  *./start-hbase.sh*

Note that before using sensordb, you need to create sensordb table within hbase.

```./bin/hbase shell
create "data",{NAME => 'data', VERSIONS => 1}

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

### Data Stream Storage Model using Column-based Storage systems

![](https://github.com/alisalehi/sensordb/raw/master/documents/column-based-model.png)

### Data Stream Insertion Process diagram

![](https://github.com/alisalehi/sensordb/raw/master/documents/data-flow-2.png)


Markdown syntax `http://daringfireball.net/projects/markdown/syntax` or `http://warpedvisions.org/projects/markdown-cheat-sheet/`

#Restful API Documentation#

##User Access API##
|Resource|Method|Description|
|:-------|------|-----------|
|/users|GET|Retrieving user profile information for all users
|/session|GET|Retrieving user's structural information


### GET /users###

There is no input parameter defined for this request.

Sample output:

	[
		{
		_id: "4fbda653da0633d1756a0c5f",
		name: "sample1",
		picture: "http://127.0.0.1:9001/i/ali-user.png",
		website: "http://www.csiro.au/",
		description: "This is a test user ..."
		},
		{
		_id: "4fbda653da0633d1756a0c60",
		name: "sample2",
		picture: "http://127.0.0.1:9001/i/phenomics-user.png",
		website: "http://www.csiro.au/",
		description: "<p>HTML: <a href="http://www.example.com"">here</a> </p>"
		}
	]

**_id** refers to internal id of a user and **name** refers to username.

###GET /session ###
This request is used to retrieve information about experiments,nodes and streams of a particular user. If no username specified, it returns back the structure of currently logged in user (if any), otherwise returns an empty JSON set.

|Parameter|Required|Default|
|:--------|--------|-------|
|name     | No     |Current username  

Example, if a user is not logged in, a request to /session results to

	{}

If a valid username is provided, the response is like

	{
	    "user": {
	        "_id": "4fbdb9e1da06e19c2a4d5171",
	        "name": "sample1",
	        "picture": "http://127.0.0.1:9001/i/ali-user.png",
	        "website": "http://www.csiro.au/",
	        "description": "This is a test user",
	        "created_at": 1337833953654,
	        "updated_at": 1337833953654
	    },
	    "experiments": [
	        {
	            "_id": "4fbdb9e1da06e19c2a4d5173",
	            "access_restriction": "0",
	            "created_at": 1337833953909,
	            "description": "",
	            "metadata": {
	                "deployment status": {
	                    "updated_by": "sample1",
	                    "description": "",
	                    "start-ts": 1330947746220,
	                    "updated_at": 1337833953920,
	                    "value": "Active",
	                    "end-ts": 1336218146220
	                },
	                "location": {
	                    "value": "Australia",
	                    "updated_at": 1337833953913,
	                    "updated_by": "sample1",
	                    "description": ""
	                },
	                "sensor type": {
	                    "value": "Arduino",
	                    "updated_at": 1337833953917,
	                    "updated_by": "sample1",
	                    "description": ""
	                }
	            },
	            "name": "yanco new setup",
	            "picture": "",
	            "timezone": "Australia/Sydney",
	            "uid": "4fbdb9e1da06e19c2a4d5171",
	            "updated_at": 1337833953909,
	            "website": ""
	        }
	    ],
	    "nodes": [
	        {
	            "_id": "4fbdb9e1da06e19c2a4d5175",
	            "alt": "",
	            "created_at": 1337833953928,
	            "description": "",
	            "eid": "4fbdb9e1da06e19c2a4d5174",
	            "lat": "",
	            "lon": "",
	            "metadata": {
	                "altitude": {
	                    "value": "100m",
	                    "updated_at": 1337833953938,
	                    "updated_by": "sample1",
	                    "description": ""
	                },
	                "status": {
	                    "updated_by": "sample1",
	                    "description": "",
	                    "start-ts": 1325418146220,
	                    "updated_at": 1337833953942,
	                    "value": "active"
	                },
	                "watering": {
	                    "value": "Rainfed",
	                    "updated_at": 1337833953935,
	                    "updated_by": "sample1",
	                    "description": ""
	                },
	                "wheat type": {
	                    "value": "Janz",
	                    "updated_at": 1337833953931,
	                    "updated_by": "sample1",
	                    "description": ""
	                }
	            },
	            "name": "node1",
	            "picture": "",
	            "uid": "4fbdb9e1da06e19c2a4d5171",
	            "updated_at": 1337833953928,
	            "website": ""
	        }
	    ],
	    "streams": [
	        {
	            "_id": "4fbdb9e1da06e19c2a4d517d",
	            "name": "stream1",
	            "uid": "4fbdb9e1da06e19c2a4d5171",
	            "nid": "4fbdb9e1da06e19c2a4d517c",
	            "mid": "4f8a5889163640e98de5d293",
	            "picture": "",
	            "website": "",
	            "updated_at": 1337833953975,
	            "created_at": 1337833953975,
	            "description": ""
	        }
	    ]
	}

Response is a JSON map with four keys as outlined below

 1. User section, a map, contains user profile information.
 2. Experiments, an array of experiment objects, contain information about experiments.
 3. Nodes, an array of node objects, contains information about nodes.
 4. Streams, an array of stream objects, containing information about streams.

Important fields used in the response:

|Field|Description|
|:---------|-----------|
|uid | UserId
|_id | id of an object
|mid| Measurement id from GET /measurements
|eid| Experiment id
|nid| Node Id
|access_restriction| applies only to experiments. 0 means experiments is publicly accessible, 1 means experiment is private and 2 means accessible only by my friends (not implemented yet)
|alt| applies only to nodes, double precision number,  altitude value
|lat| applies only to nodes, double precision number, latitude value
|lon| applies only to nodes, double precision number, longitude value

Note that any experiment, node and stream can have on or more metadata entries associated with it. Each metadata is a JSON object with the following attributes:

|Field|Description|
|:----------|-----------|
|value | String representing the value of a metadata
|updated_at | The timestamp this metadata entry is created at
|updated_by | The username of the user who created/updated this metadata entry
|description| A textual description of this metadata entry
|start-ts| Starting timestamp for this metadata, since when this metadata is relevant
|end-ts| End timestamp for this metadata, until when this metadata is relevant

##User management API ##

|Resource|Method|Description|
|:--------|------|-----------|
|/register|POST| Register a user to SensorDB
|/login|POST|Login to SensorDB with username and password
|/logout|POST|To logout from SensorDB
|/remove|POST|To remove a registered user and all his experiments, nodes and streams

###POST /remove###
Removes a registered user and all of his experiments, nodes and streams.

|Parameter|Required|Description|
|---------|--------|-----------|
|name|Yes|Username, must be unique, 3 to 30 alphanumeric characters
|password|Yes|Must consist of 6 or more and less than 30 characters. The characters should be printable ascii characters (ascii code 32 to 126).

###POST /login###

Used to login with a username and password. Note that a user must be activated before he can login.

|Parameter|Required|Description|
|---------|--------|-----------|
|name|Yes|Username, must be unique, 3 to 30 alphanumeric characters
|password|Yes|Must consist of 6 or more and less than 30 characters. The characters should be printable ascii characters (ascii code 32 to 126).

Once login is successful, the output of this request is equivalent of GET /session request.
If login is not successful, an error message like below is produced:

	{"errors":["Login failed"]}

Note: To use this request, the user must first logout and no valid session should exist. If there is a valid session, SensorDB redirects this request to GET /session

###POST /logout###

Invalidates current user session (if there is any available). This request doesn't require any parameter and doesn't return any value.

###POST /register###

|Parameter|Required|Description|
|---------|--------|-----------|
|name|Yes|Username, must be unique, 3 to 30 alphanumeric characters
|password|Yes|Must consist of 6 or more and less than 30 characters. The characters should be printable ascii characters (ascii code 32 to 126)
|email|Yes|Must be unique
|description|No|A short description about this user
|picture|No|A URL pointing to an image, containing a picture of this user
|website|No|A URL pointing to a page containing more information about this user

If a user is created successfully, the output of this request is equivalent of GET /session request.
If user is not created successfully, the output contains error messages describing the problem, like below:

	{"errors":["Username is not available","Email is already used"]}

Note: Upon successful registration, the user is automatically logs in (no separate login request required).

##Experiment Management API##

|Resource|Method|Description|
|:--------|------|-----------|
|/experiments|POST|Create a new experiment
|/experiments|PUT|Update and existing experiment
|/experiments|DELETE|Remove an existing experiment

### POST /experiments ###

|Parameter|Required|Description|
|---------|--------|-----------|
|name|Yes|Name of this experiment, must be unique per user, 3 to 30 characters (alphanumeric with space, dot, underscore and hyphen)
|timezone|Yes|Timezone of this experiment, valid timezone values are listed (here)[https://raw.github.com/alisalehi/sensordb/master/timezones.txt]
|description|No|Description of this experiment, limited HTML allowed
|website|No|A URL for a website containing more information about this experiment
|picture|No|URL pointing to a picture
|public_access|No|Integer value, 0 means public, 1 means private, 2 means accessible by friends (not implemented)

Note: To use this request, the callee must have a valid session (a logged in user).
Note2: The user session in which this request is made, owns the experiment

### PUT /experiments ###

Use this request to update experiment information

|Parameter|Required|Description|
|---------|--------|-----------|
|eid|Yes|experiment Id
|field|Yes|valid values for this field are: name, website, description, picture or access_restriction
|value|Yes|New value

Note: To use this request, the callee must have a valid session (a logged in user) and should own this experiment.

### DELETE /experiments?eid=_eid_ ###

|Parameter|Required|Description|
|---------|--------|-----------|
|eid|Yes|Experiment Id to be deleted

Note: To use this request, the callee must have a valid session (a logged in user) and should own this experiment.

##Node Management API##

|Resource|Method|Description|
|:--------|------|-----------|
|/nodes|POST|Create a new node
|/nodes|PUT|Update and existing node
|/nodes|DELETE|Remove an existing node

### POST /nodes ###

|Parameter|Required|Description|
|---------|--------|-----------|
|name|Yes|Name of the node, must be unique within a experiment, 3 to 30 characters (alphanumeric with space, dot, underscore and hyphen)
|eid|Yes| Experiment Id which this node belongs to
|description|No|Description of this experiment, limited HTML is allowed
|website|No|A URL for a website containing more information about this experiment
|picture|No|URL pointing to a picture
|lat|No|Double precision number presenting latitude of this node
|alt|No|Double precision number presenting altitude of this node
|lon|No|Double precision number presenting longitude of this node

Note: To use this request, the callee must have a valid session (a logged in user) and should own the experiment id (eid).
Note2: The user session in which is request is made, owns the newly created node

### PUT /nodes ###

Use this request to update node information

|Parameter|Required|Description|
|---------|--------|-----------|
|nid|Yes|node Id
|field|Yes| Valid values for field are:  lat, lon, alt, website, description, picture or eid
|value|Yes|New value

Note: To use this request, the callee must have a valid session (a logged in user) and should own this node.

### DELETE /nodes=?nid=_nid_ ###

|Parameter|Required|Description|
|---------|--------|-----------|
|nid|Yes|Node Id to be deleted

Note: To use this request, the callee must have a valid session (a logged in user) and should own this node.

##Stream Management API##

|Resource|Method|Description|
|:--------|------|-----------|
|/streams|POST|Create a new stream
|/streams|PUT|Update and existing stream
|/streams|DELETE|Remove an existing stream
|/tokens|GET|retrieves the list of all security tokens for all the streams belonging to current user session
|/measurements|GET|retrieves measurement ids and their information

### POST /streams ###

|Parameter|Required|Description|
|---------|--------|-----------|
|name|Yes|Name of the stream, must be unique within a node, 3 to 30 characters (alphanumeric with space, dot, underscore and hyphen)
|nid|Yes|Parent node id
|description|No|Description of this stream, limited HTML is allowed
|website|No|A URL for a website containing more information about this stream
|picture|No|URL pointing to a picture
|mid|Yes|Measurement Id

Note: To use this request, the callee must have a valid session (a logged in user) and should own the node id (nid).
Note2: The user session in which is request is made, owns the newly created stream

### PUT /streams ###

Use this request to update stream information

|Parameter|Required|Description|
|---------|--------|-----------|
|sid|Yes|Stream Id
|field|Yes|Valid values for this field are: name, mid, website, description, picture or nid
|value|Yes|New value

Note: To use this request, the callee must have a valid session (a logged in user) and should own this stream.

### DELETE /streams?sid=_sid_ ###

|Parameter|Required|Description|
|---------|--------|-----------|
|sid|Yes|Stream Id to be deleted

Note: To use this request, the callee must have a valid session (a logged in user) and should own this stream.

### GET /tokens ###

Note: To use this request, the callee must have a valid session (a logged in user)

	[
	    {
	        "_id": "4fbdb9e1da06e19c2a4d5176",
	        "token": "2671ace1-dc5a-4bc2-9c11-55e19f3be141"
	    },
	    {
	        "_id": "4fbdb9e1da06e19c2a4d5177",
	        "token": "c6013332-646f-4a9e-82c9-8d6c1b88cdeb"
	    }, ...
	]

### GET /measurements ###
Measurements are referenced from streams (mid field in the stream definition.)

Sample output of `/measurements`

	[
	    {
	        "_id": "4f8a5889163640e98de5d293",
	        "name": "Celsius",
	        "website": "http://en.wikipedia.org/wiki/Celsius",
	        "description": "Celsius, formerly known as centigrades, a scale and unit of measurement for temperature.",
	        "created_at": 1334466697374,
	        "updated_at": 1334466697374
	    },
	    {
	        "_id": "4f8a5889163640e98de5d294",
	        "name": "Relative Humidity",
	        "website": "http://en.wikipedia.org/wiki/Relative_humidity",
	        "description": "Relative humidity is a term used to describe the amount of water vapor ...",
	        "created_at": 1334466697374,
	        "updated_at": 1334466697374
	    }, ...
	]

At this stage, measurements are inserted directly (manually) into MongoDB. Look into mongodb.readme to see how it is done.

##Metadata API ##
|Resource|Method|Description|
|:--------|------|-----------|
|/metadata/add|GET|To Add/update a metadata entry to/of an experiment, node or stream.
|/metadata/remove|GET|To remove a metadata entry from an experiment, node or stream.
|/metadata/retrieve/__ObjectId__|GET|To retrieve all metadata of a given experiment, node or stream.

General information: In all the above methods, the _id_ parameter can refer to a stream id, a node id or an experiment id. This is possible because in SensorDB all ids are unique.

###GET /metadata/remove###
This call is useful for removing a metadata entry from a stream, a node or experiment.

|Parameter|Required|Description|
|---------|--------|-----------|
|id|Yes|String representing object Id whose metadata will be removed
|name|Yes|Metadata name can have at most 30 characters, consisting of alphanumeric, underscore and space characters

If removal is successful, returns nothing otherwise an string describing the problem.

###GET /metadata/add###
Adds or updates a metadata entry for a given object id.

Note: To use this request, the callee must have a valid session (a logged in user) and the object should belong to the current user.

|Parameter|Required|Description|
|---------|--------|-----------|
|id|Yes|String representing an object id whose metadata will be altered
|name|Yes|Metadata name can have at most 30 characters, consisting of alphanumeric, underscore and space characters
|value|Yes|Metadata value can have at most 30 characters, simple HTML tags are allowed
|description|No|Description of this metadata, simple limited HTML tags are allowed
|start-ts|No|Integer, the timestamp since this metadata is relevant (format: number of seconds since epoch in the timezone of the related experiment)
|end-ts|No|Integer, the timestamp until which this metadata is relevant (format: number of seconds since epoch in the timezone of the related experiment)

Example request GET http://localhost:9001/metadata/add?id=4fbdb9e1da06e19c2a4d5173&name=abc&value=12345 

Output: if stream id and user session are valid, returns no results otherwise returns error message describing the problem.

###GET /metadata/retrieve/*ObjectId*###
Example output for a valid objectId. The objectId may point to a stream, a node or an experiment.

	{
	    "deployment status": {
	        "updated_by": "sample1",
	        "description": "",
	        "start-ts": 1330947746220,
	        "updated_at": 1337833953920,
	        "value": "Active",
	        "end-ts": 1336218146220
	    },
	    "location": {
	        "value": "Australia",
	        "updated_at": 1337833953913,
	        "updated_by": "sample1",
	        "description": ""
	    },
	    "sensor type": {
	        "value": "Arduino",
	        "updated_at": 1337833953917,
	        "updated_by": "sample1",
	        "description": ""
	    }
	}

If SensorDB can't find the requested object (there is no stream, node or experiment with a given ObjectId), it returns:

	{}

##Data Access API##

|Resource|Method|Description|
|--------|------|-----------|
|/data|GET|Download raw or aggregated sensor data
|/data|POST|Publishing sensor data

### GET /data ###
This request is for downloading raw or aggregated sensor data from one or more streams. The aggregation level is specified per request.

|Parameter|Required|Default|Description|Format|
|---------|--------|-------|-----------|------|
|level|no|raw|Aggregation level|level is text and can be set to one of the following values: raw, 1-minute, 5-minute, 15-minute, 1-hour, 3-hour, 6-hour, 1-day, 1-month, 1-year
|sd|yes||Start Date|Date in the UK format, e.g., 30-01-2012 for 30th of Jan, 2012 (Start date is assumed to be in the same timezone as the experiment which holds the stream)
|ed|yes||End Date| Date in the UK format, e.g., 20-12-2012 for 20th of Dec, 2012  (End date is assumed to be in the same timezone as the experiment which holds the stream)
|sid|yes||stream id(s)| sid _or_ ["sid1","sid2","sid3",...]

Response: 	JSON array
If the aggregation level is raw, the output format is

	{
		sid1:[[time1,value1],[time2,value2],...],
		sid2:[[time1,value1],[time2,value2],...],
		...
	}

In the above response, sid is stream id in string, time is an integer, presenting number of seconds since epoch (timezone aware) and value is a double precision number.

If aggregation level is not raw, the output format is

	{
		sid1:[[time1,min1,max1,count1,sum1,sumSq1],[time2,min2,max2,count2,sum2,sumSq2],...],
		sid2:[[timeA,minA,maxA,countA,sumA,sumSqA],[timeB,minB,maxB,countB,sumB,sumSqB],...],
	}

### POST /data ###
This request can be used for pushing sensor data into SensorDB. One or more value from one or more streams can be pushed at the same time. The total request body size must be less than 500Kb.

|Parameter|Required|Description|Format|
|---------|--------|-----------|------|
|data|yes|Data|Data is in a two level-deep nested JSON map. Key is security  token,value is a map with timestamp as a key and value as a double precision number.

Example data value:

	{"1234-1234-1234-1234":{
		"122324124":18.2,
		"122324125":null,
		"122324126":18.4},
	"1234-1234-1234-1234":{
		"122324124":18.2,
		"122324125":null,
		"122324126":18.4}
	}

Response:
An integer, confirming the total number of stream elements, (timestamp,value) pairs, sent received through this request.

##SensorDB, Storage, Cache and Queue Configuration##

SensorDB is using Akka for creating distributed actors with persistent message queues.

SensorDB is using Redis for caching, web-based session information management, storage helper (a.k.a, storage bit index) and the actual storage (instead of HBase if configured to do so).

SensorDB is using MongoDB for structural data storage (information about user, streams, experiments, nodes, etc.).

All SensorDB configurations can be found at `src/main/resources/application.conf`

SensorDB's logging configuration is specified using `logback.xml` at `src/main/resources/logback.xml`

## SensorDB expects the underlying server to have an accurate time ##

1. Configure timezone using `sudo dpkg-reconfigure tzdata`
1. Install NTPD `sudo apt-get install ntp`
1. Add ntp servers to NTPD `echo "server ntp.ubuntu.com" > /etc/ntp.conf`
1. Add ntp servers to NTPD `echo "server pool.ntp.org" > /etc/ntp.conf`

Note: You may need to reconfigure your firewall settings as NTP uses port 123/UDP. Here is how to configure a firewall to allow NTP in Linux

	iptables -A OUTPUT -p udp --dport 123 -j ACCEPT
	iptables -A INPUT -p udp --sport 123 -j ACCEPT

