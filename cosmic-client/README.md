# cosmic-client


## Configuration

The deplorable artifact from this module (WAR) does not contain all the required configuration parameters.
These parameters need to be set via properties files and those files need managemente available to the application (running on tomact).
There are several ways to achieve that, but most expose the parameters to the entire tomcat container.
The one explained here configures the container to add an extra directory to the application's class path.

### Configuration parameters

#### Database

For DB access, the following properties need to be available to the JVM.
```properties
# Cluster config
cluster.node.IP=
cluster.servlet.port=9090

# Cloud DB config
db.cloud.username=cloud
db.cloud.password=cloud
db.cloud.host=localhost
db.cloud.port=3306
db.cloud.name=cloud
db.cloud.maxActive=250
db.cloud.maxIdle=30
db.cloud.maxWait=10000
db.cloud.validationQuery=SELECT 1
db.cloud.testOnBorrow=true
db.cloud.testWhileIdle=true
db.cloud.timeBetweenEvictionRunsMillis=40000
db.cloud.minEvictableIdleTimeMillis=240000
db.cloud.autoReconnect=true
db.cloud.keyStorePassphrase=vmops.com
db.cloud.encryption.type=none
db.cloud.encrypt.secret=

# Usage DB config
db.usage.username=cloud
db.usage.password=cloud
db.usage.host=localhost
db.usage.port=3306
db.usage.name=cloud_usage
db.usage.maxActive=100
db.usage.maxIdle=30
db.usage.maxWait=10000
db.usage.autoReconnect=true
```

The properties already filled in the example above are default values, the two mandatory property that is missing (and there is no default for) is:
* `cluster.node.IP`:
The IP (or hostname if resolvable) of the management server node (there can be several for HA). This IP is used by system vm agents to reach the management server and for clustered management servers to rebalance agents between them.
 
To enable encryption of sensitive data (currently using `PBEWithMD5AndDES`), set `db.cloud.encryption.type` to something other than `none`. 
Possibilities are:
* `file`: This option requires a file named `key`, containing the encryption key, to be in the configuration folder (e.g. `/etc/cosmic/management`)
* `env`: This option expects an environment variable named `CLOUD_SECRET_KEY`, containing the encryption key, to be defined and available
* `web`: This options expects the key to be delivered via socket on port `8097`

By default the management server will assume that it operates in region 1 (the default region in a single region setup).
To enable multiple regions, set the property `region.id` to the correct value.

### Tomcat configuration

To add an extra directory to the class path of the application we manipulate its class loader inside it's container context.
The custom context needs to be defined under `$CATALINA_BASE/conf/[enginename]/[hostname]/[application name].xml`.

The application name is alwyas `client`, the engine we use is `Catalina` and the hostname is `localhost`.
Therefore, the file path is `~tomcat/conf/Catalina/localhost/client.xml`.
In this example, the directory that we are adding to the application's class path is `/ect/cosmic/management`.

```xml
<Context path="/client" reloadable="true">
    <!-- http://tomcat.apache.org/tomcat-7.0-doc/config/context.html -->
    <Loader className="org.apache.catalina.loader.VirtualWebappLoader" virtualClasspath="/etc/cosmic/management"/>
    <JarScanner scanAllDirectories="true"/>
</Context>
```
