package transaction;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Set;

import lockmgr.DeadlockException;

/**
 * Interface for the Resource Manager of the Distributed Travel Reservation
 * System.
 * <p>
 * Unlike WorkflowController.java, you are supposed to make changes to this
 * file.
 */

public interface ResourceManager extends Remote {
    public Set getTransactions() throws RemoteException;

    public Collection getUpdatedRows(int xid, String tablename)
            throws RemoteException;

    public Collection getUpdatedRows(String tablename) throws RemoteException;

    public void setDieTime(String time) throws RemoteException;

    public boolean reconnect() throws RemoteException;

    public boolean dieNow() throws RemoteException;

    public void ping() throws RemoteException;

    public String getID() throws RemoteException;

    public Collection query(int xid, String tablename)
            throws DeadlockException, InvalidTransactionException,
            RemoteException;

    public ResourceItem query(int xid, String tablename, Object key)
            throws DeadlockException, InvalidTransactionException,
            RemoteException;

    public Collection query(int xid, String tablename, String indexName,
                            Object indexVal) throws DeadlockException,
            InvalidTransactionException, InvalidIndexException, RemoteException;

    public boolean update(int xid, String tablename, Object key,
                          ResourceItem newItem) throws DeadlockException,
            InvalidTransactionException, RemoteException;

    public boolean insert(int xid, String tablename, ResourceItem newItem)
            throws DeadlockException, InvalidTransactionException,
            RemoteException;

    public boolean delete(int xid, String tablename, Object key)
            throws DeadlockException, InvalidTransactionException,
            RemoteException;

    public int delete(int xid, String tablename, String indexName,
                      Object indexVal) throws DeadlockException,
            InvalidTransactionException, InvalidIndexException, RemoteException;

    public boolean prepare(int xid) throws InvalidTransactionException,
            RemoteException;

    public void commit(int xid) throws InvalidTransactionException,
            RemoteException;

    public void abort(int xid) throws InvalidTransactionException,
            RemoteException;

    /**
     * The RMI names a ResourceManager binds to.
     */
    public static final String RMINameFlights = "RMFlights";

    public static final String RMINameRooms = "RMRooms";

    public static final String RMINameCars = "RMCars";

    public static final String RMINameCustomers = "RMCustomers";
}