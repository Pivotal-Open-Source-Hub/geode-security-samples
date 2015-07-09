#!/bin/bash

ENV=`dirname $0`
. "$ENV/../common.env"

gfsh -e "connect --locator=localhost[10334]" -e "shutdown --include-locators=true"

