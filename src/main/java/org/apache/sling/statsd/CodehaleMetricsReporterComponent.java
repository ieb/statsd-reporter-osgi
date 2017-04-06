/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.sling.statsd;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.readytalk.metrics.StatsDReporter;
import org.apache.felix.scr.annotations.*;
import org.omg.PortableInterceptor.INACTIVE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by boston on 02/02/2017.
 */
@Component(immediate = true , metatype = true)
@References(value = {@Reference(
        referenceInterface = MetricRegistry.class,
        cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE,
        policy = ReferencePolicy.DYNAMIC,
        bind = "bindMetricRegistry",
        unbind = "unbindMetricRegistry")}
)
public class CodehaleMetricsReporterComponent {

    private static final Logger LOG = LoggerFactory.getLogger(CodehaleMetricsReporterComponent.class);

    private ScheduledReporter reporter;

    @Property(value = "127.0.0.1", description = "the hostname of the statsd server")
    public static final String STATSD_SERVER = "host";
    @Property(intValue = 8125, description = "The port of the statsd db server")
    public static final String STATSD_PORT = "port";
    @Property(intValue = 5, description = "The period in seconds the reporter reports at")
    public static final String REPORT_PERIOD = "period";

    private ConcurrentMap<String, CopyMetricRegistryListener> listeners = new ConcurrentHashMap<String, CopyMetricRegistryListener>();
    private MetricRegistry metricRegistry = new MetricRegistry();

    @Activate
    public void acivate(Map<String, Object> properties) throws IOException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        LOG.info("Starting Statsd Metrics reporter ");
        String server = (String) properties.get(STATSD_SERVER);
        int port = (int) properties.get(STATSD_PORT);
        int period = (int) properties.get(REPORT_PERIOD);
        reporter = StatsDReporter.forRegistry(metricRegistry)
                .prefixedWith(getHostName())
                .build(server,port);
        reporter.start(period, TimeUnit.SECONDS);
        LOG.info("Started Statsd Metrics reporter to {}:{}  ", new Object[]{server, port});
    }

    @Deactivate
    public void deacivate(Map<String, Object> properties) {
        reporter.stop();
        reporter = null;
    }

    protected void bindMetricRegistry(MetricRegistry metricRegistry, Map<String, Object> properties) {
        String name = (String) properties.get("name");
        if (name == null) {
            name = metricRegistry.toString();
        }
        CopyMetricRegistryListener listener = new CopyMetricRegistryListener(this.metricRegistry, name);
        listener.start(metricRegistry);
        this.listeners.put(name, listener);
        LOG.info("Bound Metrics Registry {} ",name);
    }
    protected void unbindMetricRegistry(MetricRegistry metricRegistry, Map<String, Object> properties) {
        String name = (String) properties.get("name");
        if (name == null) {
            name = metricRegistry.toString();
        }
        CopyMetricRegistryListener metricRegistryListener = listeners.get(name);
        if ( metricRegistryListener != null) {
            metricRegistryListener.stop(metricRegistry);
            this.listeners.remove(name);
        }
        LOG.info("Unbound Metrics Registry {} ",name);
    }

    public String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch ( Exception ex ) {
            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                return "Unknown ip";
            }
        }
    }
}
