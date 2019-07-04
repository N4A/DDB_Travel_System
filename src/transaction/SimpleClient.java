package transaction;

import java.rmi.Naming;

/**
 * A toy client of the Distributed Travel Reservation System.
 */

public class SimpleClient {

    public static void main(String args[]) {
        String rmiPort = System.getProperty("rmiPort");
        if (rmiPort == null) {
            rmiPort = "";
        } else if (!rmiPort.equals("")) {
            rmiPort = "//:" + rmiPort + "/";
        }

        WorkflowController wc = null;
        try {
            wc = (WorkflowController) Naming.lookup(rmiPort + WorkflowController.RMIName);
            System.out.println("Bound to WC");
        } catch (Exception e) {
            System.err.println("Cannot bind to WC:" + e);
            System.exit(1);
        }

        try {
            int xid = wc.start();

            if (!wc.addFlight(xid, "347", 230, 999)) {
                System.err.println("Add flight failed");
            }
            if (!wc.addRooms(xid, "SFO", 500, 150)) {
                System.err.println("Add room failed");
            }

            System.out.println("Flight 347 has " +
                    wc.queryFlight(xid, "347") +
                    " seats.");
            if (!wc.reserveFlight(xid, "John", "347")) {
                System.err.println("Reserve flight failed");
            }
            System.out.println("Flight 347 now has " +
                    wc.queryFlight(xid, "347") +
                    " seats.");

            if (!wc.commit(xid)) {
                System.err.println("Commit failed");
            }

        } catch (Exception e) {
            System.err.println("Received exception:" + e);
            System.exit(1);
        }

    }
}
