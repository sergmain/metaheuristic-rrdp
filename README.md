# metaheuristic-rrdp
Reference implementation RRDP (RFC 8182) in Java


https://datatracker.ietf.org/doc/html/rfc8182

### requirements
java 11.x or 17.x (other version >11.x should work too)  
maven 3.5+  


### build
mvn clean install


### rrdp-srv
application.properties for rrdp-srv
```properties
server.port = 8888
rrdp.server.path.metadata = /path/to/metadata
rrdp.server.path.source = /path/to/actual-data
rrdp.server.timeout.notification-refresh = 10
rrdp.server.timeout.codes-refresh = 10

logging.file.name=<path-to-log-dir>/rrpd-srv.log
logging.level.root = info
```

rrdp.server.path.metadata - path to metadata  
rrdp.server.path.source - path to actual data  
rrdp.server.timeout.notification-refresh - timeout for refreshing a notification.xml, seconds  
rrdp.server.timeout.codes-refresh  - timeout for refreshing a list of codes, seconds

'codes' is a list of top level dirs in rrdp.server.path.source

cmd for running server:
```commandline
java -Dfile.encoding=UTF-8 -jar rrdp-srv.jar
```


### rrdp-client
application.properties for rrdp-client
```properties
rrdp.client.asset.url = http://localhost:8888
rrdp.client.path.metadata = /path/to/metadata
rrdp.client.path.data = /path/to/data

logging.file.name=<path-to-log-dir>/rrpd-client.log
logging.level.root = info
```

rrdp.client.asset.url - address of asset server  
rrdp.client.path.metadata - path to metadata  
rrdp.client.path.data - path where data will be stored  


cmd for running client:
```commandline
java -Dfile.encoding=UTF-8 -jar rrdp-client.jar
```


### rrdp-srv rest commands

start rescanning with concrete code
```text
curl http://localhost:8080/rest/v1/rrdp/command/rescan/<code>
```

request the current status
```text
curl http://localhost:8080/rest/v1/rrdp/command/status
```


rescan with using concrete code and paths
```text
curl -X POST -F "file=@rescan-paths.txt" http://localhost:8080/rest/v1/rrdp/command/rescan-paths/test
```

name of file with paths can be any but you have to change cmd-line accordingly
content of rescan-paths.txt - one absolute path per line 



