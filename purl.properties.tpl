##################################################
##        PURL server configuration file        ##
##################################################

# This file must be placed in a directory whose
# location is defined by a system property named
# "nl.naturalis.purl.conf.dir". With Wildfly
# system properties are best set in standalone.xml

# PROD
#nba.baseurl=http://api.biodiversitydata.nl/v2
# DEV
nba.baseurl=http://145.136.242.164:8080/v2/

# Template for the Bioportal's specimen detail page.
# Use ${unitID} as a placeholder for the actual unitID.
bioportal.specimen.url=http://bioportal.naturalis.nl/specimen/${unitID}

# Template for the Xeno-canto's specimen detail page.
# Use ${sourceSystemId} as a placeholder for the actual
# source system ID.
xenocanto.observation.url=https://www.xeno-canto.org/${sourceSystemId}

# If true no redirect to a new URL (e.g. a Bioportal
# URL) will take place. Instead, the contents of that
# URL will be retrieved and displayed "under" the
# PURL.
noredirect=false
