##################################################
##        PURL server configuration file        ##
##################################################

# This file must be placed in a directory whose
# location is defined by a system property named
# "nl.naturalis.purl.conf.dir". With Wildfly
# system properties are best set in standalone.xml

# Base URL under which this app runs. It may not
# really know this because of load balancers
# confusing wildfly. Leave empty on PROD or set to
# http://data.biodiversitydata.nl. This setting is
# only relevant for the welcome page.
#purl.baseurl=http://data.biodiversitydata.nl
purl.baseurl=http://localhost:8080/purl/

# NBA PROD
#nba.baseurl=http://api.biodiversitydata.nl/v2
# NBA TEST
# nba.baseurl=http://145.136.242.164:8080/v2/
# NBA DEV
nba.baseurl=http://145.136.242.164:8080/v2/

# HTML detail pages. Use ${unitID} as a placeholder
# for the actual unitID. Use ${sourceSystemId} as a
# placeholder for the source system's original ID.
bioportal.specimen.url=http://bioportal.naturalis.nl/specimen/${unitID}
xenocanto.observation.url=https://www.xeno-canto.org/${unitID}
waarneming.observation.url=https://waarneming.nl/waarneming/view/${sourceSystemId}

