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

import com.sun.jmx.remote.internal.RMIExporter;
import com.sun.jmx.remote.util.EnvHelp;
import sun.rmi.server.UnicastServerRef;
import sun.rmi.server.UnicastServerRef2;

import javax.management.remote.rmi.RMIConnection;
import javax.management.remote.rmi.RMIJRMPServerImpl;
import javax.management.remote.rmi.RMIServer;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Map;

public class ProxyRMIJRMPServerImpl extends RMIJRMPServerImpl {
    private final int port;
    private final RMIClientSocketFactory csf;
    private final RMIServerSocketFactory ssf;
    private final Map<String, ?> env;
    private final RMIServer delegateServer;
    private final PrintWriter pw;
    private final Boolean interceptCalls;

    public ProxyRMIJRMPServerImpl(
            final int port,
            final RMIClientSocketFactory csf,
            final RMIServerSocketFactory ssf,
            final Map<String, ?> env,
            final RMIServer delegateServer,
            final PrintWriter pw,
            final Boolean interceptCalls) throws IOException {

        super(port, csf, ssf, env);
        this.port = port;
        this.csf = csf;
        this.ssf = ssf;
        this.env = env;
        this.delegateServer = delegateServer;
        this.pw = pw;
        this.interceptCalls = interceptCalls;
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

        final RMIConnection delegateConnection = delegateServer.newClient(credentials);

        if (interceptCalls) {
            final RMIConnection wrappedConnection = (RMIConnection)
                    Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                            new Class<?>[]{RMIConnection.class},
                            new ConnectionInvocationHandler(delegateConnection));

            export(wrappedConnection);
            return wrappedConnection;
        } else {
            return delegateConnection;
        }
    }

    private void export(Remote obj) throws RemoteException {
        final RMIExporter exporter = (RMIExporter) env.get(RMIExporter.EXPORTER_ATTRIBUTE);
        final boolean daemon = EnvHelp.isServerDaemon(env);

        if (daemon && exporter != null) {
            throw new IllegalArgumentException("If "+EnvHelp.JMX_SERVER_DAEMON+
                    " is specified as true, "+RMIExporter.EXPORTER_ATTRIBUTE+
                    " cannot be used to specify an exporter!");
        }

        if (daemon) {
            if (csf == null && ssf == null) {
                new UnicastServerRef(port).exportObject(obj, null, true);
            } else {
                new UnicastServerRef2(port, csf, ssf).exportObject(obj, null, true);
            }
        } else if (exporter != null) {
            exporter.exportObject(obj, port, csf, ssf);
        } else {
            UnicastRemoteObject.exportObject(obj, port, csf, ssf);
        }
    }

    public class ConnectionInvocationHandler implements InvocationHandler {
        private final RMIConnection delegateConnection;

        public ConnectionInvocationHandler(final RMIConnection delegateConnection) {
            this.delegateConnection = delegateConnection;
        }

        @Override
        public Object invoke(final Object o, final Method method, final Object[] args) throws Throwable {

            // This just passes calls along, but we easily intercept specific data and return something
            // manipulated back to the end user.
            final Object result = method.invoke(delegateConnection, args);

            final StringBuilder sb = new StringBuilder();
            sb.append("Returning ").append(result == null ? null : result)
                    .append(" from ").append(method.toString());

            if (args != null && args.length > 0) {
                sb.append(" with ").append(Arrays.asList(args));
            }

            pw.println(sb.toString());
            pw.flush();

            return result;
        }
    }
}