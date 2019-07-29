package transaction;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface for the Transaction Manager of the Distributed Travel
 * Reservation System.
 * <p>
 * Unlike WorkflowController.java, you are supposed to make changes
 * to this file.
 */

public interface TransactionManager extends Remote {

    /**
     * The RMI name a TransactionManager binds to.
     */
    public static final String RMIName = "TM";

    // transaction status
    public static final String INITED = "inited";
    public static final String PREPARING = "preparing";
    public static final String COMMITTED = "committed";
    public static final String ABORTED = "aborted";

    public boolean dieNow()
            throws RemoteException;

    public void setDieTime(String time) throws RemoteException;

    public void ping() throws RemoteException;

    //////////
    // TRANSACTION INTERFACE
    //////////

    /**
     * Add a resource manager to the participants list
     * @param xid transaction id
     * @param rm resource manager
     * @throws RemoteException
     * @return true for success, false for fail (the xid has been aborted)
     */
    public String enlist(int xid, ResourceManager rm) throws RemoteException;

    /**
     * Start a new transaction, and return its transaction id.
     *
     * @return A unique transaction ID > 0.  Return <=0 if server is not accepting new transactions.
     * @throws RemoteException on communications failure.
     */
    public int start()
            throws RemoteException;

    /**
     * Commit transaction.
     *
     * @param xid id of transaction to be committed.
     * @return true on success, false on failure.
     * @throws RemoteException             on communications failure.
     * @throws TransactionAbortedException if transaction was aborted.
     * @throws InvalidTransactionException if transaction id is invalid.
     */
    public boolean commit(int xid)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException;

    /**
     * Abort transaction.
     *
     * @param xid id of transaction to be aborted.
     * @throws RemoteException             on communications failure.
     * @throws InvalidTransactionException if transaction id is invalid.
     */
    public void abort(int xid)
            throws RemoteException,
            InvalidTransactionException;
}
