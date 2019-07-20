package transaction;

import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Transaction Manager for the Distributed Travel Reservation System.
 * <p>
 * Description: toy implementation of the TM
 */

public class TransactionManagerImpl
        extends java.rmi.server.UnicastRemoteObject
        implements TransactionManager {

    private Integer xidCounter;
    // resource managers of all transactions
    private HashMap<Integer, HashSet<ResourceManager>> RMs = new HashMap<>();

    public TransactionManagerImpl() throws RemoteException {
        xidCounter = 1;
    }

    public static void main(String[] args) {
        System.setSecurityManager(new RMISecurityManager());

        String rmiPort = System.getProperty("rmiPort");
        if (rmiPort == null) {
            rmiPort = "";
        } else if (!rmiPort.equals("")) {
            rmiPort = "//:" + rmiPort + "/";
        }

        try {
            TransactionManagerImpl obj = new TransactionManagerImpl();
            Naming.rebind(rmiPort + TransactionManager.RMIName, obj);
            System.out.println("TM bound");
        } catch (Exception e) {
            System.err.println("TM not bound:" + e);
            System.exit(1);
        }
    }

    public void ping() throws RemoteException {
    }

    public void enlist(int xid, ResourceManager rm) throws RemoteException {
        HashSet<ResourceManager> xidRMs = RMs.get(xid);
        xidRMs.add(rm);
    }

    @Override
    public int start() throws RemoteException {
        synchronized (xidCounter) {
            Integer newXid = xidCounter++;
            RMs.put(newXid, new HashSet<>());
            return newXid;
        }
    }

    @Override
    public boolean commit(int xid) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        HashSet<ResourceManager> xidRMs = RMs.get(xid);
        // 2pc
        // prepare phase
        for (ResourceManager rm : xidRMs) {
            try {
                if (!rm.prepare(xid)) {
                    // rm is not prepared.
                    this.abort(xid);
                    return false;
                }
            } catch (RemoteException e) {
                // rm dies before or during prepare
                this.abort(xid);
                return false;
            }
        }
        // prepared, commit phase
        for (ResourceManager rm : xidRMs) {
            try {
                rm.commit(xid); // the function return means done signal.
            } catch (RemoteException e) {
                // rm dies before or during commit
                this.abort(xid);
                return false;
            }
        }

        synchronized (RMs) {
            // remove committed transactions
            RMs.remove(xid);
        }
        // success
        return true;
    }

    @Override
    public void abort(int xid) throws RemoteException, InvalidTransactionException {
        HashSet<ResourceManager> xidRMs = RMs.get(xid);
        for (ResourceManager rm : xidRMs) {
            rm.abort(xid); // rm maybe break later
        }
        synchronized (RMs) {
            // remove aborted transactions
            RMs.remove(xid);
        }
    }

    public boolean dieNow()
            throws RemoteException {
        System.exit(1);
        return true; // We won't ever get here since we exited above;
        // but we still need it to please the compiler.
    }

}
