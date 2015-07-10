# Geode security: Sample LDAP implementation

This repository helps explain Geode's security mechanism for AuthN and AuthZ. It contains the following:
* Scripts to start and configure a local Geode cluster; and, to create both partitioned and replicated regions
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
* Clone this repository using your GIT client

## Build the sample security implementation
After you clone this repository, you need to build the JAR file that contains the sample authN and authZ implementation:    
```
cd <path-to-local-repo>/sample-data-grid-security    
mvn clean package
```    
After a successful Maven execution, you can find the JAR file at '/path-to-local-repo/sample-data-grid-security/target/sample-data-grid-security-0.1.0.jar'

## Install JARs to your local Maven repo
As of July 2015, Geode's JAR files are not yet in any public Maven repositories, so we need to manually install these 3 JAR files to your local Maven repo. Please make sure the $GEMFIRE variable is set before executing this Maven comand:

```
mvn install:install-file -Dfile=$GEMFIRE/lib/gemfire-core-1.0.0-incubating-SNAPSHOT.jar -DgroupId=com.gemstone.gemfire -DartifactId=gemfire-core -Dversion=1.0.0-incubating-SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=$GEMFIRE/lib/gemfire-jgroups-1.0.0-incubating-SNAPSHOT.jar -DgroupId=com.gemstone.gemfire -DartifactId=gemfire-jgroups -Dversion=1.0.0-incubating-SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=$GEMFIRE/lib/gemfire-core-dependencies.jar -DgroupId=com.gemstone.gemfire -DartifactId=gemfire-core-dependencies -Dversion=1.0 -Dpackaging=jar
```

We also need to install the sample security JAR file to your local Maven repository

```
cd <path-to-local-repo>/sample-data-grid-security/target    
mvn install:install-file -Dfile=sample-data-grid-security-0.1.0.jar -DgroupId=org.mycompany.security.samples -DartifactId=sample-data-grid-security -Dversion=0.1.0 -Dpackaging=jar
```

## Customize settings of your Geode cluster

**Changing envrionment variables of the Geode script**: edit the '/path-to-local-repo/geode-cluster-scripts/gfsh_start_cluster.sh' and adjust these variables based on your settings. Use 2 of the LDAP service accounts to set the value of EMPLOYEE_APP_USER and CUSTOMER_APP_USER variables: 
```
set variable --name=SAMPLE_SECURITY_IMPL_DIR --value=<path-to-local-repo>/sample-data-grid-security
set variable --name=EMPLOYEE_APP_USER --value=your-employee-app-user
set variable --name=CUSTOMER_APP_USER --value=your-employee-app-user
```
Save your changes.


**Changing LDAP settings**: edit the following properties in the '/path-to-local-repo/geode-cluster-scripts/config/gemfire-server.properties' file to match your LDAP-related settings. Use one of your LDAP service accounts to set the username and password for Geode server authentication

```
security-ldap-url=ldap://localhost:10389/
security-ldap-basedn=o=sevenSeas
security-ldap-filter=(&(objectClass=inetOrgPerson)(uid={0}))
security-username=geode-system
security-password=pass
```
Save your changes.

## Start the Geode cluster
To configure the Geode cluster, we are going to use a _gfhs_ script. Make sure _$GEMFIRE/bin_ is part of  your existing _PATH_ environment variable. The _gfsh_ script performs the following:
* Start a locator
* Configure Portable Data eXchange (PDX) for all the servers in the Geode cluster
* Start a Geode server which will host all regions
* Create the _Account_ partitioned region which will host the Account objects that the sample client will create
* Create the _PermissionPerRole_ replicated region which hosts the role of a service account for a given region
* Create an entry in the _PermissionPerRole_ region for the Employee app's service credential
* Create an entry in the _PermissionPerRole_ region for the Customer app's service credential

Open a terminal window and type:
```
cd <path-to-local-repo>/geode-cluster-scripts
gfsh run --file=gfsh_start_cluster.sh
```

## Customize settings of your sample client app
Now configure the client, edit the the 'sample-client-security/src/main/resources/user_credentials.properties'. This is necessary because the sample client app gets both service credentials as key-value pairs. Use the same 2 LDAP service accounts and their corresponding passwords: 

``` 
 EMPLOYEE_APP_USER=employee-app     
 EMPLOYEE_APP_PASSWORD=pass     
 CUSTOMER_APP_USER=customer-app     
 CUSTOMER_APP_PASSWORD=pass 
```     
Save your changes.

## Run sample client app
To run the sample client app, perform the following steps:

```
cd <path-to-local-repo>/sample-client-security
mvn clean package
java -jar target/sample-client-security-0.1.0.jar
```

To run the test scenarios using the Customer application’s service account, enter 1. To use the Employee application’s service account, enter 2. This screenshot shows the menu that the sample client application displays.

## Query data in Geode regions
To query your data, open a terminal window and type:
```
gfsh
connect --locator=localhost[10334]
query --query='select * from /PermissionPerRole'
query --query='select * from /Account'
```

## Review the Geode server's log
To review the messages that our sample authorization implementation displays, open the '/path-to-local-repo/geode-cluster-scripts/server1/server1.log' file and look for the keyword 'AUDIT_TRAIL'. You will see messages such as:

```
[info 2015/07/09 12:29:18.185 CDT server1 <ServerConnection on port 40404 Thread 1> tid=0x47] AUDIT_TRAIL: ALLOWED This Geode user [customer-app] with this role [customer] invoked this operation [GET] in region [/Account] from this remote client: 192.168.59.3(66444:loner):53650:863ddd73

[info 2015/07/09 12:29:19.279 CDT server1 <ServerConnection on port 40404 Thread 1> tid=0x47] AUDIT_TRAIL: DENIED This Geode user [customer-app] with this role [customer] invoked this operation [PUTALL] in region [/Account] from this remote client: 192.168.59.3(66444:loner):53650:863ddd73
```
## Stop the Geode cluster
Once you are done testing, you can stop the Geode cluster. Change to the 'geode-cluster-scripts' folder and execute the 'shutdown' command:

```
cd <path-to-local-repo>/geode-cluster-scripts
gfsh -e "connect --locator=localhost[10334]" -e "shutdown --include-locators=true"
```

## Feedback is welcome
Please do.
