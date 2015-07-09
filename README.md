# Geode security - Sample implementation

This repository helps explain Geode's security mechanism for AuthN and AuthZ. It contains the following:
* Scripts to start and stop a local Geode cluster
* Sample Java implementation of the authN and authZ callbacks
* Sample Java client application to test both authN and authZ callbacks

## Pre-requisites
In your local environment, perform the following steps:
* Install Java SE 7 because Geode requires it
* Build Geode from source as specified in https://github.com/apache/incubator-geode#geode-in-5-minutes
* Configure both _GEMFIRE_ and _GF_JAVA_ environment variables to ensure Geode functions properly
* Add _$GEMFIRE/bin_ to your existing _PATH_ environment variable
* Install an LDAP server and define 3 service accounts. Alternatively, you can use an existing LDAP server and define 3 service accounts
* Install Maven 3.x and configure the M2_HOME environment variable

## Adapt settings to your environment
After you clone this repository, edit the following properties in the '<path-to-local-repo>/geode-cluster-scripts/config/gemfire-server.properties' file to match your LDAP-related settings:

```
security-ldap-url=ldap://localhost:10389/
security-ldap-basedn=o=sevenSeas
security-ldap-filter=(&(objectClass=inetOrgPerson)(uid={0}))
security-username=geode-system
security-password=pass
```

Then edit the 'common.env' file:
* Set GEODE_SECURITY_HOME with the full path to the 'sample-data-grid-security' folder. For instance (Mac and *nix users):   
 ```export GEODE_SECURITY_HOME=<path-to-local-repo>/sample-data-grid-security```
* Take 2 of the service accounts you defined in the Pre-requisites section and set the following variables that correspond to the Employee app and Customer app, respectively. For example (Mac and *nix users):      
``` 
 EMPLOYEE_APP_USER=employee-app     
 EMPLOYEE_APP_PASSWORD=pass     
 CUSTOMER_APP_USER=customer-app     
 CUSTOMER_APP_PASSWORD=pass 
```     
* Open a terminal and source the 'common.env' file. Make sure the above environment variables are set with the correct values

Finally, copy the 'common.env' file to the 'sample-client-security/src/main/resources' folder. This is necessary because the sample client app gets both service credentials as key-value pairs

## Build the sample security implementation
To build the JAR file that contains the sample authN and authZ implementation, you need to:    
```
cd $GEODE_SECURITY_HOME    
mvn clean package
```    
After a successful Maven execution, you can find the JAR file at '$GEODE_SECURITY_HOME/target/sample-data-grid-security-0.1.0.jar'

## Install JARs to your local Maven repo
Geode's JAR files are not yet in any public Maven repositories, so we need to manually install these 3 JAR files to your local Maven repo. . Please make sure the $GEMFIRE variable is set before executing this Maven comand:

```
mvn install:install-file -Dfile=$GEMFIRE/lib/gemfire-core-1.0.0-incubating-SNAPSHOT.jar -DgroupId=com.gemstone.gemfire -DartifactId=gemfire-core -Dversion=1.0.0-incubating-SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=$GEMFIRE/lib/gemfire-jgroups-1.0.0-incubating-SNAPSHOT.jar -DgroupId=com.gemstone.gemfire -DartifactId=gemfire-jgroups -Dversion=1.0.0-incubating-SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=$GEMFIRE/lib/gemfire-core-dependencies.jar -DgroupId=com.gemstone.gemfire -DartifactId=gemfire-core-dependencies -Dversion=1.0 -Dpackaging=jar
```

We also need to install the sample security JAR file. Please make sure the $GEODE_SECURITY_HOME variable is set before executing this Maven comand:

```
mvn install:install-file -Dfile=$GEODE_SECURITY_HOME/target/sample-data-grid-security-0.1.0.jar -DgroupId=org.mycompany.security.samples -DartifactId=sample-data-grid-security -Dversion=0.1.0 -Dpackaging=jar
```

## Start the Geode cluster
Now you are ready to start the Geode cluster. Change to the 'geode-cluster-scripts' folder and execute the 'start-cluster.sh' script:

```
cd <path-to-local-repo>/geode-cluster-scripts
./start-cluster.sh
```

## Run sample client app
To run the sample client app, perform the following steps:

```
cd <path-to-local-repo>/sample-client-security
mvn clean package
java -jar target/sample-client-security-0.1.0.jar
```

To run the test scenarios using the Customer application’s service account, enter 1. To use the Employee application’s service account, enter 2. This screenshot shows the menu that the sample client application displays.

## Review the Geode server's log
To review the messages that our sample authorization implementation displays, open the 'path-to-local-repo/geode-cluster-scripts/server1/server1.log' file and look for the keyword 'AUDIT_TRAIL'

## Stop the Geode cluster
Once you are done testing, you can stop the Geode cluster. Change to the 'geode-cluster-scripts' folder and execute the 'stop-cluster.sh' script:

```
cd <path-to-local-repo>/geode-cluster-scripts
./stop-cluster.sh
```
Happy testing!
