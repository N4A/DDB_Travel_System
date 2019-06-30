package transaction;

import java.rmi.*;
import java.util.*;

/**
 * Interface for the Workflow Controller of the Distributed Travel
 * Reservation System.
 * <p>
 * Failure reporting is done using two pieces, exceptions and boolean
 * return values.  Exceptions are used for systemy things - like
 * transactions that were forced to abort, or don't exist.  Return
 * values are used for operations that would affect the consistency of
 * the database, like the deletion of more cars than there are.
 * <p>
 * If there is a boolean return value and you're not sure how it would
 * be used in your implementation, ignore it.  We used boolean return
 * values in the interface generously to allow flexibility in
 * implementation.  But don't forget to return true when the operation
 * has succeeded.
 * <p>
 * All methods in the interface are declared to throw RemoteException.
 * This exception is thrown by the RMI system during a remote method
 * call to indicate that either a communication failure or a protocol
 * error has occurred. Your code will never have to directly throw
 * this exception, but any client code that you write must catch the
 * exception and take the appropriate action.
 */

public interface WorkflowController extends Remote {

    //////////
    // TRANSACTION INTERFACE
    //////////
    /**
     * Start a new transaction, and return its transaction id.
     *
     * @return A unique transaction ID > 0.  Return <=0 if server is not accepting new transactions.
     *
     * @throws RemoteException on communications failure.
     */
    public int start()
	throws RemoteException;
    /**
     * Commit transaction.
     *
     * @param xid id of transaction to be committed.
     * @return true on success, false on failure.
     *
     * @throws RemoteException on communications failure.
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
     *
     * @throws RemoteException on communications failure.
     * @throws InvalidTransactionException if transaction id is invalid.
     */
    public void abort(int xid)
	throws RemoteException,
	       InvalidTransactionException;


    //////////
    // ADMINISTRATIVE INTERFACE
    //////////
    /**
     * Add seats to a flight.  In general this will be used to create
     * a new flight, but it should be possible to add seats to an
     * existing flight.  Adding to an existing flight should overwrite
     * the current price of the available seats.
     *
     * @param xid id of transaction.
     * @param flightNum flight number, cannot be null.
     * @param numSeats number of seats to be added to the flight.(>=0)
     * @param price price of each seat. If price < 0, don't overwrite the current price; leave price at 0 if price<0 for very first add for this flight.
     * @return true on success, false on failure. (flightNum==null; numSeats<0...)
     *
     * @throws RemoteException on communications failure.
     * @throws TransactionAbortedException if transaction was aborted.
     * @throws InvalidTransactionException if transaction id is invalid.
     */
    public boolean addFlight(int xid, String flightNum, int numSeats, int price)
	throws RemoteException,
	       TransactionAbortedException,
	       InvalidTransactionException;
    /**
     * Delete an entire flight.
     * Should fail if a customer has a reservation on this flight.
     *
     * @param xid id of transaction.
     * @param flightNum flight number, cannot be null.
     * @return true on success, false on failure. (flight doesn't exist;has reservations...)
     *
     * @throws RemoteException on communications failure.
     * @throws TransactionAbortedException if transaction was aborted.
     * @throws InvalidTransactionException if transaction id is invalid.
     */
    public boolean deleteFlight(int xid, String flightNum)
	throws RemoteException,
	       TransactionAbortedException,
	       InvalidTransactionException;

    /**
     * Add rooms to a location.
     * This should look a lot like addFlight, only keyed on a location
     * instead of a flight number.
     *
     * @return true on success, false on failure. (location==null; numRooms<0...)
     *
     * @throws RemoteException on communications failure.
     * @throws TransactionAbortedException if transaction was aborted.
     * @throws InvalidTransactionException if transaction id is invalid.
     *
     * @see #addFlight
     */
    public boolean addRooms(int xid, String location, int numRooms, int price)
	throws RemoteException,
	       TransactionAbortedException,
	       InvalidTransactionException;
    /**
     * Delete rooms from a location.
     * This subtracts from both the toal and the available room count
     * (rooms not allocated to a customer).  It should fail if it
     * would make the count of available rooms negative.
     *
     * @return true on success, false on failure. (location doesn't exist; numRooms<0; not enough available rooms...)
     *
     * @throws RemoteException on communications failure.
     * @throws TransactionAbortedException if transaction was aborted.
     * @throws InvalidTransactionException if transaction id is invalid.
     *
     * @see #deleteFlight
     */
    public boolean deleteRooms(int xid, String location, int numRooms)
	throws RemoteException,
	       TransactionAbortedException,
	       InvalidTransactionException;

    /**
     * Add cars to a location.
     * Cars have the same semantics as hotels (see addRooms).
     *
     * @return true on success, false on failure.
     *
     * @throws RemoteException on communications failure.
     * @throws TransactionAbortedException if transaction was aborted.
     * @throws InvalidTransactionException if transaction id is invalid.
     *
     * @see #addRooms
     * @see #addFlight
     */
    public boolean addCars(int xid, String location, int numCars, int price)
	throws RemoteException,
	       TransactionAbortedException,
	       InvalidTransactionException;
    /**
     * Delete cars from a location.
     * Cars have the same semantics as hotels.
     *
     * @return true on success, false on failure.
     *
     * @throws RemoteException on communications failure.
     * @throws TransactionAbortedException if transaction was aborted.
     * @throws InvalidTransactionException if transaction id is invalid.
     *
     * @see #deleteRooms
     * @see #deleteFlight
     */
    public boolean deleteCars(int xid, String location, int numCars)
	throws RemoteException,
	       TransactionAbortedException,
	       InvalidTransactionException; 

    /**
     * Add a new customer to database.  Should return success if
     * customer already exists.
     *
     * @param xid id of transaction.
     * @param custName name of customer.
     * @return true on success, false on failure.
     *
     * @throws RemoteException on communications failure.
     * @throws TransactionAbortedException if transaction was aborted.
     * @throws InvalidTransactionException if transaction id is invalid.
     */
    public boolean newCustomer(int xid, String custName)
	throws RemoteException,
	       TransactionAbortedException,
	       InvalidTransactionException;
    /**
     * Delete this customer and un-reserve associated reservations.
     *
     * @param xid id of transaction.
     * @param custName name of customer.
     * @return true on success, false on failure. (custName==null or doesn't exist...)
     *
     * @throws RemoteException on communications failure.
     * @throws TransactionAbortedException if transaction was aborted.
     * @throws InvalidTransactionException if transaction id is invalid.
     */
    public boolean deleteCustomer(int xid, String custName)
	throws RemoteException,
	       TransactionAbortedException,
	       InvalidTransactionException;


    //////////
    // QUERY INTERFACE
    //////////
    /**
     * Return the number of empty seats on a flight.
     *
     * @param xid id of transaction.
     * @param flightNum flight number.
     * @return # empty seats on the flight. (-1 if flightNum==null or doesn't exist)
     *
     * @throws RemoteException on communications failure.
     * @throws TransactionAbortedException if transaction was aborted.
     * @throws InvalidTransactionException if transaction id is invalid.
     */
    public int queryFlight(int xid, String flightNum)
	throws RemoteException,
	       TransactionAbortedException,
	       InvalidTransactionException;

    /** Return the price of a seat on this flight. Return -1 if flightNum==null or doesn't exist.*/
    public int queryFlightPrice(int xid, String flightNum)
	throws RemoteException,
	       TransactionAbortedException,
	       InvalidTransactionException;

    /** Return the number of rooms available at a location. */
    public int queryRooms(int xid, String location)
	throws RemoteException,
	       TransactionAbortedException,
	       InvalidTransactionException;

    /** Return the price of rooms at this location. */
    public int queryRoomsPrice(int xid, String location)
	throws RemoteException,
	       TransactionAbortedException,
	       InvalidTransactionException;

    /** Return the number of cars available at a location. */
    public int queryCars(int xid, String location)
	throws RemoteException,
	       TransactionAbortedException,
	       InvalidTransactionException;

    /** Return the price of rental cars at this location. */
    public int queryCarsPrice(int xid, String location)
	throws RemoteException,
	       TransactionAbortedException,
	       InvalidTransactionException;

    /* Return the total price of all reservations held for a customer. Return -1 if custName==null or doesn't exist.*/
    public int queryCustomerBill(int xid, String custName)
	    throws RemoteException,
		   TransactionAbortedException,
		   InvalidTransactionException;


    //////////
    // RESERVATION INTERFACE
    //////////
    /**
     * Reserve a flight on behalf of this customer.
     *
     * @param xid id of transaction.
     * @param custName name of customer.
     * @param flightNum flight number.
     * @return true on success, false on failure. (cust or flight doesn't exist; no seats left...)
     *
     * @throws RemoteException on communications failure.
     * @throws TransactionAbortedException if transaction was aborted.
     * @throws InvalidTransactionException if transaction id is invalid.
     */
    public boolean reserveFlight(int xid, String custName, String flightNum)
	throws RemoteException,
	       TransactionAbortedException,
	       InvalidTransactionException;

    /** Reserve a car for this customer at the specified location. */
    public boolean reserveCar(int xid, String custName, String location)
	throws RemoteException,
	       TransactionAbortedException,
	       InvalidTransactionException;

    /** Reserve a room for this customer at the specified location. */
    public boolean reserveRoom(int xid, String custName, String location)
	throws RemoteException,
	       TransactionAbortedException,
	       InvalidTransactionException;

    //////////
    // TECHNICAL/TESTING INTERFACE
    //////////
    /**
     * If some component has died and was restarted, this function is
     * called to refresh the RMI references so that everybody can talk
     * to everybody else again.  Specifically, the WC should reconnect
     * to all other components, and each RM's reconnect() is called so
     * that the RM can reconnect to the TM.
     * <p>
     * This method is used for testing and is not part of a transaction.
     *
     * @return true on success, false on failure. (some component not up yet...)
     */
    public boolean reconnect() 
	throws RemoteException;
    /**
     * Kill the component immediately.  Used to simulate a system
     * failure such as a power outage.
     * <p>
     * This method is used for testing and is not part of a transaction.
     *
     * @param who which component to kill; must be "TM", "RMFlights", "RMRooms", "RMCars", "RMCustomers", "WC", or "ALL" (which kills all 6 in that order).
     * @return true on success, false on failure.
     */
    public boolean dieNow(String who)
	throws RemoteException;
    /**
     * Sets a flag so that the RM fails after the next enlist()
     * operation.  That is, the RM immediately dies on return of the
     * enlist() call it made to the TM, before it could fulfil the
     * client's query/reservation request.
     * <p>
     * This method is used for testing and is not part of a transaction.
     *
     * @param who which RM to kill; must be "RMFlights", "RMRooms", "RMCars", or "RMCustomers".
     * @return true on success, false on failure.
     */
    public boolean dieRMAfterEnlist(String who)
	throws RemoteException;
    /**
     * Sets a flag so that the RM fails when it next tries to prepare,
     * but before it gets a chance to save the update list to disk.
     * <p>
     * This method is used for testing and is not part of a transaction.
     *
     * @param who which RM to kill; must be "RMFlights", "RMRooms", "RMCars", or "RMCustomers".
     * @return true on success, false on failure.
     */
    public boolean dieRMBeforePrepare(String who)
	throws RemoteException;
    /**
     * Sets a flag so that the RM fails when it next tries to prepare:
     * after it has entered the prepared state, but just before it
     * could reply "prepared" to the TM.
     * <p>
     * This method is used for testing and is not part of a transaction.
     *
     * @param who which RM to kill; must be "RMFlights", "RMRooms", "RMCars", or "RMCustomers".
     * @return true on success, false on failure.
     */
    public boolean dieRMAfterPrepare(String who)
	throws RemoteException;
    /**
     * Sets a flag so that the TM fails after it has received
     * "prepared" messages from all RMs, but before it can log
     * "committed".
     * <p>
     * This method is used for testing and is not part of a transaction.
     *
     * @return true on success, false on failure.
     */
    public boolean dieTMBeforeCommit()
	throws RemoteException;
    /**
     * Sets a flag so that the TM fails right after it logs
     * "committed".
     * <p>
     * This method is used for testing and is not part of a transaction.
     *
     * @return true on success, false on failure.
     */
    public boolean dieTMAfterCommit()
	throws RemoteException;
    /**
     * Sets a flag so that the RM fails when it is told by the TM to
     * commit, by before it could actually change the database content
     * (i.e., die at beginning of the commit() function called by TM).
     * <p>
     * This method is used for testing and is not part of a transaction.
     *
     * @param who which RM to kill; must be "RMFlights", "RMRooms", "RMCars", or "RMCustomers".
     * @return true on success, false on failure.
     */
    public boolean dieRMBeforeCommit(String who)
	throws RemoteException;
    /**
     * Sets a flag so that the RM fails when it is told by the TM to
     * abort, by before it could actually do anything.  (i.e., die at
     * beginning of the abort() function called by TM).
     * <p>
     * This method is used for testing and is not part of a transaction.
     *
     * @param who which RM to kill; must be "RMFlights", "RMRooms", "RMCars", or "RMCustomers".
     * @return true on success, false on failure.
     */
    public boolean dieRMBeforeAbort(String who)
	throws RemoteException;


    /** The RMI name a WorkflowController binds to. */
    public static final String RMIName = "WC";
}
