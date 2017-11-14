##################################################
##        PURL server configuration file        ##
##################################################

# This file must be placed in a directory whose
# location is defined by a system property named
# "nl.naturalis.purl.conf.dir". With Wildfly
# system properties are best set in standalone.xml

nba.baseurl=http://localhost:8080/v2

# Template for the Bioportal's specimen detail page.
# You can use ${unitID} placeholder for the actual
# unitID.
bioportal.specimen.url=http://bioportal.naturalis.nl/specimen/${unitID}

# If set to true, PURLs will not be redirected
# using the HTTP 303 redirect mechanism. In
# browsers this has the effect that the PURL will
# stay in the address bar; it will not be replaced
# by the bioportal/NBA/medialib URL.
noredirect=true

# Media type served when no Accept headers present
specimen.purl.accept.default=text/html
# Other media types we can serve. Do not include
# media types from image/multimedia repositories
# like the medialib
specimen.purl.accept.0=application/json
specimen.purl.accept.1=
specimen.purl.accept.2=
# Never serve, even if available
specimen.purl.accept.blacklist.0=
specimen.purl.accept.blacklist.1=

multimedia.purl.accept.default=text/html
multimedia.purl.accept.0=application/json
multimedia.purl.accept.1=
multimedia.purl.accept.2=
multimedia.purl.accept.blacklist.0=
multimedia.purl.accept.blacklist.1=

# Media types served by the medialib and other
# multimedia repositories. Wildcard types allowed
medialib.mediatypes.0=image/*
medialib.mediatypes.1=video/*
medialib.mediatypes.2=application/pdf
medialib.mediatypes.3=
