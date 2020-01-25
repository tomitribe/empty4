/*
 * Tomitribe Confidential
 *
 * Copyright Tomitribe Corporation. 2019
 *
 * The source code for this program is not published or otherwise divested
 * of its trade secrets, irrespective of what has been deposited with the
 * U.S. Copyright Office.
 */
package org.tomitribe.rmi;

import javax.management.remote.rmi.RMIConnection;
import javax.management.remote.rmi.RMIJRMPServerImpl;
import javax.management.remote.rmi.RMIServer;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.util.Map;

public class ProxyRMIJRMPServerImpl extends RMIJRMPServerImpl {
    private final int port;
    private final RMIClientSocketFactory csf;
    private final RMIServerSocketFactory ssf;
    private final Map<String, ?> env;
    private final RMIServer delegateServer;
    private final PrintWriter pw;

    public ProxyRMIJRMPServerImpl(
            final int port,
            final RMIClientSocketFactory csf,
            final RMIServerSocketFactory ssf,
            final Map<String, ?> env,
            final RMIServer delegateServer,
            final PrintWriter pw) throws IOException {

        super(port, csf, ssf, env);
        this.port = port;
        this.csf = csf;
        this.ssf = ssf;
        this.env = env;
        this.delegateServer = delegateServer;
        this.pw = pw;
    }

    @Override
    public RMIConnection newClient(Object credentials) throws IOException {
        if (credentials != null && String[].class.isInstance(credentials)) {
            final String[] strings = String[].class.cast(credentials);
            for (final String credential : strings) {
                pw.println("Captured: " + credential);
            }
            pw.println();
            pw.flush();
        }
        return delegateServer.newClient(credentials);
    }
}