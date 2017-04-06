This provides a OSGi bundle that has a component shipping Dropwizard metrics out to StatsD over UDP.

This bundle currently depends on metrics3-statsd-4.2.1-SNAPSHOT with patch https://github.com/ReadyTalk/metrics-statsd/pull/33 The Travis build will be broken until that pull request is merged. See below for local build.

[![Build Status](https://travis-ci.org/ieb/statsd-reporter-osgi.svg?branch=master)](https://travis-ci.org/ieb/statsd-reporter-osgi)

# Quick Setup for the impatient

Build metrics3-statsd-4.2.1-SNAPSHOT locally from https://github.com/ieb/metrics-statsd which has been patched to fix stats names.

    git clone git@github.com:ieb/metrics-statsd.git
    cd metrics-statsd
    ./gradlew
    

Build this bundle

    mvn clean install
   

Install the bundle and configure pointing to a StatsD server.

# OSGi Properties for org.apache.sling.influxdb.CodehaleMetricsReporter

## host (default: 127.0.0.1)

The host where StatsD is running.

## port (default: 8125)

Port where StatsD is running.





