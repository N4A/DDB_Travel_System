package transaction;

import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;

/**
 * Transaction Manager for the Distributed Travel Reservation System.
 * <p>
 * Description: toy implementation of the TM
 */

public class TransactionManagerImpl
        extends java.rmi.server.UnicastRemoteObject
        implements TransactionManager {

    protected int xidCounter;

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
    }

    @Override
    public int start() throws RemoteException {
        return 0;
    }

    @Override
    public boolean commit(int xid) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        return false;
    }

    @Override
    public void abort(int xid) throws RemoteException, InvalidTransactionException {

    }

    public boolean dieNow()
            throws RemoteException {
        System.exit(1);
        return true; // We won't ever get here since we exited above;
        // but we still need it to please the compiler.
    }

}
