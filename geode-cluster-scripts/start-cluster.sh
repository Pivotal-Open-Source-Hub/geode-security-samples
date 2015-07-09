#!/bin/bash 

ENV=`dirname $0`
. "$ENV/../common.env"

echo "Starting locator"
echo " "
gfsh start locator --name=locator1 --bind-address=localhost --properties-file=config/gemfire-server.properties --classpath=/Users/guillermotantachuco/Documents/GT/__geode-security-samples/sample-data-grid-security/target/sample-data-grid-security-0.1.0.jar

gfsh -e "connect --locator=localhost[10334]" -e "configure pdx --read-serialized=true"

echo "Starting server"
echo " "
gfsh start server --name=server1 --locators=localhost[10334] --bind-address=localhost --server-bind-address=localhost --properties-file=config/gemfire-server.properties --classpath=/Users/guillermotantachuco/Documents/GT/__geode-security-samples/sample-data-grid-security/target/sample-data-grid-security-0.1.0.jar

echo "Creating Account region"
echo " "
gfsh -e "connect --locator=localhost[10334]" -e "create region --name=Account --type=PARTITION"

echo "Creating PermissionPerRole region"
echo " "
gfsh -e "connect --locator=localhost[10334]" -e "create region --name=PermissionPerRole --type=REPLICATE"

gfsh -e "connect --locator=localhost[10334]" -e "put --key="employee-app,/Account" --value='employee' --region=PermissionPerRole"
gfsh -e "connect --locator=localhost[10334]" -e "put --key="customer-app,/Account" --value='customer'  --region=PermissionPerRole"

