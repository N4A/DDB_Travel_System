package transaction;

import lockmgr.DeadlockException;
import transaction.entity.*;

import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Workflow Controller for the Distributed Travel Reservation System.
 * <p>
 * Description: toy implementation of the WC.  In the real
 * implementation, the WC should forward calls to either RM or TM,
 * instead of doing the things itself.
 */

public class WorkflowControllerImpl
        extends java.rmi.server.UnicastRemoteObject
        implements WorkflowController {

    protected TransactionManager tm = null;
    private HashSet<Integer> xids = new HashSet<>();
    private ResourceManager rmFlights = null;
    private ResourceManager rmRooms = null;
    private ResourceManager rmCars = null;
    private ResourceManager rmCustomers = null;

    // log
    private String xidsLog = "data/WC_xids.log";

    public WorkflowControllerImpl() throws RemoteException {
        recover();

        while (!reconnect()) {
            // would be better to sleep a while
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
        }
    }

    public static void main(String args[]) {
        System.setSecurityManager(new RMISecurityManager());

        String rmiPort = System.getProperty("rmiPort");
        if (rmiPort == null) {
            rmiPort = "";
        } else if (!rmiPort.equals("")) {
            rmiPort = "//:" + rmiPort + "/";
        }

        try {
            WorkflowControllerImpl obj = new WorkflowControllerImpl();
            Naming.rebind(rmiPort + WorkflowController.RMIName, obj);
            System.out.println("WC bound");
        } catch (Exception e) {
            System.err.println("WC not bound:" + e);
            System.exit(1);
        }
    }

    private void recover() {
        Object xids_tmp = utils.loadObject(xidsLog);
        if (xids_tmp != null)
            xids = (HashSet<Integer>) xids_tmp;
    }

    // TRANSACTION INTERFACE
    public int start()
            throws RemoteException {
        int xid = tm.start();
        xids.add(xid);
        utils.storeObject(xids, xidsLog);
        return xid;
    }

    public boolean commit(int xid)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (!xids.contains(xid))
            throw new InvalidTransactionException(xid, "");
        boolean tmResult = tm.commit(xid);
        xids.remove(xid);
        utils.storeObject(xids, xidsLog);
        return tmResult;
    }

    public void abort(int xid)
            throws RemoteException,
            InvalidTransactionException {
        if (!xids.contains(xid))
            throw new InvalidTransactionException(xid, "");
        tm.abort(xid);
        xids.remove(xid);
        utils.storeObject(xids, xidsLog);
    }


    // ADMINISTRATIVE INTERFACE
    public boolean addFlight(int xid, String flightNum, int numSeats, int price)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (flightNum == null || numSeats < 0)
            return false;
        // check whether is flight exists or not
        ResourceItem item = queryItem(rmFlights, xid, flightNum);

        if (item != null) { // exist, then update
            Flight f = (Flight) item;
            f.addSeats(numSeats);
            if (price >= 0)
                f.setPrice(price);
            try {
                return rmFlights.update(xid, rmFlights.getID(), flightNum, f);
            } catch (DeadlockException e) {
                // dead lock happened, quit this transaction
                abort(xid);
                throw new TransactionAbortedException(xid, "This transaction cause dead lock: " + e.getMessage());
            }
        } else { // not exist, then insert new one
            if (price < 0)
                price = 0;
            Flight f = new Flight(flightNum, price, numSeats);
            try {
                return rmFlights.insert(xid, rmFlights.getID(), f);
            } catch (DeadlockException e) {
                // dead lock happened, quit this transaction
                abort(xid);
                throw new TransactionAbortedException(xid, "This transaction cause dead lock: " + e.getMessage());
            }
        }
    }

    public boolean deleteFlight(int xid, String flightNum)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (!xids.contains(xid))
            throw new InvalidTransactionException(xid, "");
        try {
            Collection<ResourceItem> resvs = rmCustomers.query(xid, ResourceManager.TableNameReservations,
                    Reservation.INDEX_RESERV_KEY, flightNum);
            if (!resvs.isEmpty())
                return false;
            ResourceItem item = queryItem(rmFlights, xid, flightNum);
            if (item == null)
                return false;
            rmFlights.delete(xid, rmFlights.getID(), flightNum);
        } catch (DeadlockException e) {
            // dead lock happened, quit this transaction
            abort(xid);
            throw new TransactionAbortedException(xid, "This transaction cause dead lock: " + e.getMessage());
        } catch (InvalidIndexException e) {
            System.err.println("hhh, code is wrong. InvalidIndexException: " + e.getMessage());
        }
        return true;
    }

    private ResourceItem queryItem(ResourceManager rm, int xid, String key)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (!xids.contains(xid))
            throw new InvalidTransactionException(xid, "");

        ResourceItem item = null;
        try {
            item = rm.query(xid, rm.getID(), key);
        } catch (DeadlockException e) {
            // dead lock happened, quit this transaction
            abort(xid);
            throw new TransactionAbortedException(xid, "This transaction cause dead lock: " + e.getMessage());
        }

        return item;
    }

    public boolean addRooms(int xid, String location, int numRooms, int price)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (location == null || numRooms < 0) // see interface doc for requirement
            return false;
        ResourceItem item = queryItem(rmRooms, xid, location);

        if (item != null) {
            Hotel h = (Hotel) item;
            h.addRooms(numRooms);
            if (price >= 0)
                h.setPrice(price);
            try {
                return rmRooms.update(xid, rmRooms.getID(), location, h);
            } catch (DeadlockException e) {
                // dead lock happened, quit this transaction
                abort(xid);
                throw new TransactionAbortedException(xid, "This transaction cause dead lock: " + e.getMessage());
            }
        } else {
            if (price < 0)
                price = 0;
            Hotel h = new Hotel(location, price, numRooms);
            try {
                return rmRooms.insert(xid, rmRooms.getID(), h);
            } catch (DeadlockException e) {
                // dead lock happened, quit this transaction
                abort(xid);
                throw new TransactionAbortedException(xid, "This transaction cause dead lock: " + e.getMessage());
            }
        }
    }

    public boolean deleteRooms(int xid, String location, int numRooms)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (!xids.contains(xid))
            throw new InvalidTransactionException(xid, "");
        if (numRooms < 0)
            return false;
        ResourceItem item = queryItem(rmRooms, xid, location);
        if (item == null)
            return false;
        Hotel h = (Hotel) (item);
        if (h.getNumAvail() < numRooms)
            return false;
        h.deleteRooms(numRooms);
        try {
            return rmRooms.update(xid, rmRooms.getID(), location, h);
        } catch (DeadlockException e) {
            // dead lock happened, quit this transaction
            abort(xid);
            throw new TransactionAbortedException(xid, "This transaction cause dead lock: " + e.getMessage());
        }
    }

    public boolean addCars(int xid, String location, int numCars, int price)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (location == null || numCars < 0)
            return false;

        ResourceItem item = queryItem(rmCars, xid, location);

        if (item != null) {
            Car c = (Car) item;
            c.addCars(numCars);
            if (price >= 0)
                c.setPrice(price);
            try {
                return rmCars.update(xid, rmCars.getID(), location, c);
            } catch (DeadlockException e) {
                // dead lock happened, quit this transaction
                abort(xid);
                throw new TransactionAbortedException(xid, "This transaction cause dead lock: " + e.getMessage());
            }
        } else {
            if (price < 0)
                price = 0;
            Car car = new Car(location, price, numCars);
            try {
                return rmCars.insert(xid, rmCars.getID(), car);
            } catch (DeadlockException e) {
                // dead lock happened, quit this transaction
                abort(xid);
                throw new TransactionAbortedException(xid, "This transaction cause dead lock: " + e.getMessage());
            }
        }
    }

    public boolean deleteCars(int xid, String location, int numCars)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (!xids.contains(xid))
            throw new InvalidTransactionException(xid, "");
        if (numCars < 0)
            return false;
        ResourceItem item = queryItem(rmCars, xid, location);
        if (item == null)
            return false;
        Car c = (Car) item;
        if (c.getNumAvail() < numCars)
            return false;
        c.deleteCars(numCars);
        try {
            return rmCars.update(xid, rmCars.getID(), location, c);
        } catch (DeadlockException e) {
            // dead lock happened, quit this transaction
            abort(xid);
            throw new TransactionAbortedException(xid, "This transaction cause dead lock: " + e.getMessage());
        }
    }

    public boolean newCustomer(int xid, String custName)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        ResourceItem item = queryItem(rmCustomers, xid, custName);
        if (item != null)
            return true;
        Customer customer = new Customer(custName);
        try {
            return rmCustomers.insert(xid, rmCustomers.getID(), customer);
        } catch (DeadlockException e) {
            // dead lock happened, quit this transaction
            abort(xid);
            throw new TransactionAbortedException(xid, "This transaction cause dead lock: " + e.getMessage());
        }
    }

    // un reserve all reservations for the custName
    private void unReserveAll(int xid, String custName) throws InvalidTransactionException,
            RemoteException, TransactionAbortedException, DeadlockException, InvalidIndexException {
        Collection<ResourceItem> results = rmCustomers.query(xid, ResourceManager.TableNameReservations,
                Reservation.INDEX_CUSTNAME, custName);
        for (ResourceItem re : results) {
            Reservation rvt = (Reservation) re;
            String resvKey = rvt.getResvKey();
            switch (rvt.getResvType()) {
                case Reservation.RESERVATION_TYPE_FLIGHT: {
                    Flight f = (Flight) queryItem(rmFlights, xid, resvKey);
                    f.unbookSeats(1);
                    rmFlights.update(xid, rmFlights.getID(), resvKey, f);
                    break;
                }
                case Reservation.RESERVATION_TYPE_CAR: {
                    Car c = (Car) queryItem(rmCars, xid, resvKey);
                    c.unbookCars(1);
                    rmCars.update(xid, rmCars.getID(), resvKey, c);
                    break;
                }
                case Reservation.RESERVATION_TYPE_HOTEL: {
                    Hotel h = (Hotel) queryItem(rmRooms, xid, resvKey);
                    h.unbookRooms(1);
                    rmRooms.update(xid, rmRooms.getID(), resvKey, h);
                }
            }
        }
    }

    public boolean deleteCustomer(int xid, String custName)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (custName == null)
            return false;
        ResourceItem item = queryItem(rmCustomers, xid, custName);
        if (item == null)
            return false;
        try {
            rmCustomers.delete(xid, rmCustomers.getID(), custName);
        } catch (DeadlockException e) {
            // dead lock happened, quit this transaction
            abort(xid);
            throw new TransactionAbortedException(xid, "This transaction cause dead lock: " + e.getMessage());
        }
        try {
            // un reserve all reservations
            unReserveAll(xid, custName);
            // delete reservations
            rmCustomers.delete(xid, ResourceManager.TableNameReservations, Reservation.INDEX_CUSTNAME, custName);
        } catch (DeadlockException e) {
            // dead lock happened, quit this transaction
            abort(xid);
            throw new TransactionAbortedException(xid, "This transaction cause dead lock: " + e.getMessage());
        } catch (InvalidIndexException e) {
            System.err.println(e.getMessage());
        }
        return true;
    }


    // QUERY INTERFACE
    public int queryFlight(int xid, String flightNum)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (flightNum == null)
            return -1;
        ResourceItem item = queryItem(rmFlights, xid, flightNum);
        if (item == null)
            return -1;
        return ((Flight) item).getNumAvail();
    }

    public int queryFlightPrice(int xid, String flightNum)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (flightNum == null)
            return -1;
        ResourceItem item = queryItem(rmFlights, xid, flightNum);
        if (item == null)
            return -1;
        return ((Flight) item).getPrice();
    }

    public int queryRooms(int xid, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (location == null)
            return -1;
        ResourceItem item = queryItem(rmRooms, xid, location);
        if (item == null)
            return -1;
        return ((Hotel) item).getNumAvail();
    }

    public int queryRoomsPrice(int xid, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (location == null)
            return -1;
        ResourceItem item = queryItem(rmRooms, xid, location);
        if (item == null)
            return -1;
        return ((Hotel) item).getPrice();
    }

    public int queryCars(int xid, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (location == null)
            return -1;
        ResourceItem item = queryItem(rmCars, xid, location);
        if (item == null)
            return -1;
        return ((Car) item).getNumAvail();
    }

    public int queryCarsPrice(int xid, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (location == null)
            return -1;
        ResourceItem item = queryItem(rmCars, xid, location);
        if (item == null)
            return -1;
        return ((Car) item).getPrice();
    }

    public int queryCustomerBill(int xid, String custName)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (custName == null)
            return -1;
        ResourceItem item = queryItem(rmCustomers, xid, custName);
        if (item == null)
            return -1;
        Collection<ResourceItem> results = null;
        try {
            results = rmCustomers.query(xid, ResourceManager.TableNameReservations,
                    Reservation.INDEX_CUSTNAME, custName);
        } catch (DeadlockException e) {
            // dead lock happened, quit this transaction
            abort(xid);
            throw new TransactionAbortedException(xid, "This transaction cause dead lock: " + e.getMessage());
        } catch (InvalidIndexException e) {
            System.err.println(e.getMessage());
        }
        if (results == null)
            return 0;

        int total_bill = 0;
        for (ResourceItem re : results) {
            Reservation rvt = (Reservation) re;
            total_bill += rvt.getPrice();
        }
        return total_bill;
    }

    // RESERVATION INTERFACE
    public boolean reserveFlight(int xid, String custName, String flightNum)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (custName == null || flightNum == null)
            return false;
        ResourceItem cust = queryItem(rmCustomers, xid, custName);
        if (cust == null)
            return false;
        ResourceItem flight = queryItem(rmFlights, xid, flightNum);
        if (flight == null)
            return false;
        Flight f = (Flight) flight;
        if (f.getNumAvail() <= 0)
            return false;
        Reservation reserv = new Reservation(custName, Reservation.RESERVATION_TYPE_FLIGHT, flightNum, f.getPrice());
        try {
            rmCustomers.insert(xid, ResourceManager.TableNameReservations, reserv);
            f.bookSeats(1);
            rmFlights.update(xid, rmFlights.getID(), flightNum, f);
        } catch (DeadlockException e) {
            // dead lock happened, quit this transaction
            abort(xid);
            throw new TransactionAbortedException(xid, "This transaction cause dead lock: " + e.getMessage());
        }
        return true;
    }

    public boolean reserveCar(int xid, String custName, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (custName == null || location == null)
            return false;
        ResourceItem cust = queryItem(rmCustomers, xid, custName);
        if (cust == null)
            return false;
        ResourceItem car = queryItem(rmCars, xid, location);
        if (car == null)
            return false;
        Car c = (Car) car;
        if (c.getNumAvail() <= 0)
            return false;
        Reservation reserv = new Reservation(custName, Reservation.RESERVATION_TYPE_CAR, location, c.getPrice());
        try {
            rmCustomers.insert(xid, ResourceManager.TableNameReservations, reserv);
            c.bookCars(1);
            rmCars.update(xid, rmCars.getID(), location, c);
        } catch (DeadlockException e) {
            // dead lock happened, quit this transaction
            abort(xid);
            throw new TransactionAbortedException(xid, "This transaction cause dead lock: " + e.getMessage());
        }
        return true;
    }

    public boolean reserveRoom(int xid, String custName, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        // valid check
        if (custName == null || location == null)
            return false;
        ResourceItem cust = queryItem(rmCustomers, xid, custName);
        if (cust == null)
            return false;
        ResourceItem hotel = queryItem(rmRooms, xid, location);
        if (hotel == null)
            return false;

        // book room
        Hotel h = (Hotel) hotel;
        if (h.getNumAvail() <= 0)
            return false;
        Reservation reserv = new Reservation(custName, Reservation.RESERVATION_TYPE_HOTEL, location, h.getPrice());
        try {
            rmCustomers.insert(xid, ResourceManager.TableNameReservations, reserv);
            h.bookRooms(1);
            rmRooms.update(xid, rmRooms.getID(), location, h);
        } catch (DeadlockException e) {
            // dead lock happened, quit this transaction
            abort(xid);
            throw new TransactionAbortedException(xid, "This transaction cause dead lock: " + e.getMessage());
        }
        return true;
    }

    @Override
    public boolean reserveItinerary(int xid, String custName, List flightNumList, String location, boolean needCar, boolean needRoom) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        // valid check
        if (!xids.contains(xid))
            throw new InvalidTransactionException(xid, "");
        if (custName == null || location == null || flightNumList == null)
            return false;
        ResourceItem cust = queryItem(rmCustomers, xid, custName);
        if (cust == null)
            return false;

        // check flights
        for (Object flight : flightNumList) {
            String flightNum = (String) flight;
            ResourceItem item = queryItem(rmFlights, xid, flightNum);
            if (item == null)
                return false;
            Flight f = (Flight) item;
            if (f.getNumAvail() <= 0)
                return false;
        }
        // check rooms
        if (needRoom) {
            ResourceItem item = queryItem(rmRooms, xid, location);
            if (item == null)
                return false;
            Hotel h = (Hotel) item;
            if (h.getNumAvail() <= 0)
                return false;
        }
        // check cars
        if (needCar) {
            ResourceItem item = queryItem(rmCars, xid, location);
            if (item == null)
                return false;
            Car c = (Car) item;
            if (c.getNumAvail() <= 0)
                return false;
        }

        //   the info of customers and get the WRITE lock.
        // But, in our implementations, all new reservations are stored in Reservations table.
        // We just need the READ lock for customers.
        try {
            rmCustomers.update(xid, rmCustomers.getID(), custName, cust); // just to set WRITE lock for the test
        } catch (DeadlockException e) {
            // dead lock happened, quit this transaction
            abort(xid);
            throw new TransactionAbortedException(xid, "This transaction cause dead lock: " + e.getMessage());
        }
        // add this to pass the test.

        // book flights
        for (Object flight : flightNumList) {
            String flightNum = (String) flight;
            if (!reserveFlight(xid, custName, flightNum))
                return false;
        }
        //book room
        if (needRoom) {
            if (!reserveRoom(xid, custName, location))
                return false;
        }
        //book car
        if (needCar) {
            if (!reserveCar(xid, custName, location))
                return false;
        }
        return true;
    }

    // TECHNICAL/TESTING INTERFACE
    public boolean reconnect()
            throws RemoteException {
        String rmiPort = System.getProperty("rmiPort");
        if (rmiPort == null) {
            rmiPort = "";
        } else if (!rmiPort.equals("")) {
            rmiPort = "//:" + rmiPort + "/";
        }

        try {
            rmFlights =
                    (ResourceManager) Naming.lookup(rmiPort +
                            ResourceManager.RMINameFlights);
            System.out.println("WC bound to RMFlights");
            rmRooms =
                    (ResourceManager) Naming.lookup(rmiPort +
                            ResourceManager.RMINameRooms);
            System.out.println("WC bound to RMRooms");
            rmCars =
                    (ResourceManager) Naming.lookup(rmiPort +
                            ResourceManager.RMINameCars);
            System.out.println("WC bound to RMCars");
            rmCustomers =
                    (ResourceManager) Naming.lookup(rmiPort +
                            ResourceManager.RMINameCustomers);
            System.out.println("WC bound to RMCustomers");
            tm =
                    (TransactionManager) Naming.lookup(rmiPort +
                            TransactionManager.RMIName);
            System.out.println("WC bound to TM");
        } catch (Exception e) {
            System.err.println("WC cannot bind to some component:" + e);
            return false;
        }

        try {
            if (rmFlights.reconnect() && rmRooms.reconnect() &&
                    rmCars.reconnect() && rmCustomers.reconnect()) {
                System.out.println("All RMs connect to TM.");
                return true;
            } else {
                System.err.println("Some RM cannot reconnect.");
            }
        } catch (Exception e) {
            System.err.println("Some RM cannot reconnect:" + e);
            return false;
        }

        return false;
    }

    public boolean dieNow(String who)
            throws RemoteException {
        if (who.equals(TransactionManager.RMIName) ||
                who.equals("ALL")) {
            try {
                tm.dieNow();
            } catch (RemoteException e) {
            }
        }
        if (who.equals(ResourceManager.RMINameFlights) ||
                who.equals("ALL")) {
            try {
                rmFlights.dieNow();
            } catch (RemoteException e) {
            }
        }
        if (who.equals(ResourceManager.RMINameRooms) ||
                who.equals("ALL")) {
            try {
                rmRooms.dieNow();
            } catch (RemoteException e) {
            }
        }
        if (who.equals(ResourceManager.RMINameCars) ||
                who.equals("ALL")) {
            try {
                rmCars.dieNow();
            } catch (RemoteException e) {
            }
        }
        if (who.equals(ResourceManager.RMINameCustomers) ||
                who.equals("ALL")) {
            try {
                rmCustomers.dieNow();
            } catch (RemoteException e) {
            }
        }
        if (who.equals(WorkflowController.RMIName) ||
                who.equals("ALL")) {
            System.exit(1);
        }
        return true;
    }

    public boolean dieRMAfterEnlist(String who)
            throws RemoteException {
        // which RM to kill; must be "RMFlights", "RMRooms", "RMCars", or "RMCustomers".
        //TODO The provided RM use direct String, use predefined
        // dietime constant variable instead of String. The follows are same.
        return dieRMByTime(who, "AfterEnlist");
    }

    private boolean dieRMByTime(String who, String time) throws RemoteException {
        // which RM to kill; must be "RMFlights", "RMRooms", "RMCars", or "RMCustomers".
        switch (who) {
            case ResourceManager.RMINameFlights: {
                rmFlights.setDieTime(time);
                break;
            }
            case ResourceManager.RMINameCars: {
                rmCars.setDieTime(time);
                break;
            }
            case ResourceManager.RMINameCustomers: {
                rmCustomers.setDieTime(time);
                break;
            }
            case ResourceManager.RMINameRooms: {
                rmRooms.setDieTime(time);
                break;
            }
            default: {
                System.err.println("Invalid RM: " + who);
                return false;
            }
        }
        return true;
    }

    public boolean dieRMBeforePrepare(String who)
            throws RemoteException {
        return dieRMByTime(who, "BeforePrepare");
    }

    public boolean dieRMAfterPrepare(String who)
            throws RemoteException {
        return dieRMByTime(who, "AfterPrepare");
    }

    public boolean dieTMBeforeCommit()
            throws RemoteException {
        tm.setDieTime("BeforeCommit");
        return true;
    }

    public boolean dieTMAfterCommit()
            throws RemoteException {
        tm.setDieTime("AfterCommit");
        return true;
    }

    public boolean dieRMBeforeCommit(String who)
            throws RemoteException {
        return dieRMByTime(who, "BeforeCommit");
    }

    public boolean dieRMBeforeAbort(String who)
            throws RemoteException {
        return dieRMByTime(who, "BeforeAbort");
    }
}
