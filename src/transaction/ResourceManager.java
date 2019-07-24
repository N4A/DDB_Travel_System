package transaction;

import lockmgr.DeadlockException;
import transaction.entity.ResourceItem;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Set;

/**
 * Interface for the Resource Manager of the Distributed Travel Reservation
 * System.
 * <p>
 * Unlike WorkflowController.java, you are supposed to make changes to this
 * file.
 */

public interface ResourceManager extends Remote {
    /**
     * The RMI names a ResourceManager binds to.
     */
    String RMINameFlights = "RMFlights";
    public static final String RMINameRooms = "RMRooms";
    public static final String RMINameCars = "RMCars";
    public static final String RMINameCustomers = "RMCustomers";

    /**
     * Resource table name;
     */
    public static final String TableNameReservations = "Reservations";

    public void setDieTime(String time) throws RemoteException;

    public boolean reconnect() throws RemoteException;

    public boolean dieNow() throws RemoteException;

    public String getID() throws RemoteException;

    public ResourceItem query(int xid, String tablename, Object key)
            throws DeadlockException, InvalidTransactionException,
            RemoteException;

    public Collection<ResourceItem> query(int xid, String tablename, String indexName,
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
}