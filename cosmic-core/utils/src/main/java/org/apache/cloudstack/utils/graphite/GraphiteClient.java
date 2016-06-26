//

//

package org.apache.cloudstack.utils.graphite;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class GraphiteClient {

    private final String graphiteHost;
    private final int graphitePort;

    /**
     * Create a new Graphite client
     *
     * @param graphiteHost Hostname of the Graphite host
     * @param graphitePort UDP port of the Graphite host
     */
    public GraphiteClient(final String graphiteHost, final int graphitePort) {
        this.graphiteHost = graphiteHost;
        this.graphitePort = graphitePort;
    }

    /**
     * Create a new Graphite client
     *
     * @param graphiteHost Hostname of the Graphite host. Will default to port 2003
     */
    public GraphiteClient(final String graphiteHost) {
        this.graphiteHost = graphiteHost;
        graphitePort = 2003;
    }

    /**
     * Send a array of metrics to graphite.
     *
     * @param metrics the metrics as key-value-pairs
     */
    public void sendMetrics(final Map<String, Integer> metrics) {
        sendMetrics(metrics, getCurrentSystemTime());
    }

    /**
     * Send a array of metrics with a given timestamp to graphite.
     *
     * @param metrics   the metrics as key-value-pairs
     * @param timeStamp the timestamp
     */
    public void sendMetrics(final Map<String, Integer> metrics, final long timeStamp) {
        try (DatagramSocket sock = new DatagramSocket()) {
            java.security.Security.setProperty("networkaddress.cache.ttl", "0");
            final InetAddress addr = InetAddress.getByName(this.graphiteHost);

            for (final Map.Entry<String, Integer> metric : metrics.entrySet()) {
                final byte[] message = new String(metric.getKey() + " " + metric.getValue() + " " + timeStamp + "\n").getBytes();
                final DatagramPacket packet = new DatagramPacket(message, message.length, addr, graphitePort);
                sock.send(packet);
            }
        } catch (final UnknownHostException e) {
            throw new GraphiteException("Unknown host: " + graphiteHost);
        } catch (final IOException e) {
            throw new GraphiteException("Error while writing to graphite: " + e.getMessage(), e);
        }
    }

    /**
     * Get the current system timestamp to pass to Graphite
     *
     * @return Seconds passed since epoch (01-01-1970)
     */
    protected long getCurrentSystemTime() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * Send a single metric with the current time as timestamp to graphite.
     *
     * @param key   The metric key
     * @param value the metric value
     * @throws GraphiteException if sending data to graphite failed
     */
    public void sendMetric(final String key, final int value) {
        sendMetric(key, value, getCurrentSystemTime());
    }

    /**
     * Send a single metric with a given timestamp to graphite.
     *
     * @param key       The metric key
     * @param value     The metric value
     * @param timeStamp the timestamp to use
     * @throws GraphiteException if sending data to graphite failed
     */
    public void sendMetric(final String key, final int value, final long timeStamp) {
        final HashMap metrics = new HashMap<String, Integer>();
        metrics.put(key, value);
        sendMetrics(metrics, timeStamp);
    }
}
