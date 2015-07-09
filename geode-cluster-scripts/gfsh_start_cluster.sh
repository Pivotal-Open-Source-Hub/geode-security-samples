set variable --name=SAMPLE_SECURITY_IMPL_DIR --value=/Users/guillermotantachuco/Documents/GT/__geode-security-samples/sample-data-grid-security
set variable --name=EMPLOYEE_APP_USER --value=employee-app
set variable --name=CUSTOMER_APP_USER --value=customer-app

start locator --name=locator1 --bind-address=localhost --properties-file=config/gemfire-server.properties --classpath=${SAMPLE_SECURITY_IMPL_DIR}/target/sample-data-grid-security-0.1.0.jar

connect --locator=localhost[10334]

configure pdx --read-serialized=true

start server --name=server1 --locators=localhost[10334] --bind-address=localhost --server-bind-address=localhost --properties-file=config/gemfire-server.properties --classpath=${SAMPLE_SECURITY_IMPL_DIR}/target/sample-data-grid-security-0.1.0.jar

create region --name=Account --type=PARTITION

create region --name=PermissionPerRole --type=REPLICATE

put --key="${EMPLOYEE_APP_USER},/Account" --value='employee' --region=PermissionPerRole
put --key="${CUSTOMER_APP_USER},/Account" --value='customer'  --region=PermissionPerRole
