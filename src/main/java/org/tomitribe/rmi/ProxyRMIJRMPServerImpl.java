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
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.util.Map;

public class ProxyRMIJRMPServerImpl extends RMIJRMPServerImpl {
    private final int port;
    private final RMIClientSocketFactory csf;
    private final RMIServerSocketFactory ssf;
    private final Map<String, ?> env;
    private final RMIServer delegateServer;

    public ProxyRMIJRMPServerImpl(
            final int port,
            final RMIClientSocketFactory csf,
            final RMIServerSocketFactory ssf,
            final Map<String, ?> env,
            final RMIServer delegateServer) throws IOException {

        super(port, csf, ssf, env);
        this.port = port;
        this.csf = csf;
        this.ssf = ssf;
        this.env = env;
        this.delegateServer = delegateServer;
    }

    @Override
    public RMIConnection newClient(Object credentials) throws IOException {

        // steal the credentials here

        final RMIConnection delegateConnection = delegateServer.newClient(credentials);
//        final RMIConnection connection = (RMIConnection) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
//                new Class[]{Unreferenced.class, RMIConnection.class},
//                new DelegatingInvocationHandler(delegateConnection));
//
//        export(connection, null);
        return delegateConnection;
    }


//    private void export(Remote obj, ObjectInputFilter typeFilter) throws RemoteException {
//        RMIExporter exporter = (RMIExporter)this.env.get("com.sun.jmx.remote.rmi.exporter");
//        boolean daemon = EnvHelp.isServerDaemon(this.env);
//        if (daemon && exporter != null) {
//            throw new IllegalArgumentException("If jmx.remote.x.daemon is specified as true, com.sun.jmx.remote.rmi.exporter cannot be used to specify an exporter!");
//        } else {
//            if (exporter != null) {
//                exporter.exportObject(obj, this.port, this.csf, this.ssf, typeFilter);
//            } else if (this.csf == null && this.ssf == null) {
//                (new UnicastServerRef(new LiveRef(this.port), typeFilter)).exportObject(obj, (Object)null, daemon);
//            } else {
//                (new UnicastServerRef2(this.port, this.csf, this.ssf, typeFilter)).exportObject(obj, (Object)null, daemon);
//            }
//        }
//    }




}
