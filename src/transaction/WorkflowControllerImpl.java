package transaction;

import lockmgr.DeadlockException;
import transaction.entity.Car;
import transaction.entity.Flight;
import transaction.entity.Hotel;
import transaction.entity.ResourceItem;

import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.HashSet;

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

    public WorkflowControllerImpl() throws RemoteException {

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

    // TRANSACTION INTERFACE
    public int start()
            throws RemoteException {
        int xid = tm.start();
        xids.add(xid);
        return xid;
    }

    public boolean commit(int xid)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (!xids.contains(xid))
            throw new InvalidTransactionException(xid, "");
        return tm.commit(xid);
    }

    public void abort(int xid)
            throws RemoteException,
            InvalidTransactionException {
        if (!xids.contains(xid))
            throw new InvalidTransactionException(xid, "");
        tm.abort(xid);
    }


    // ADMINISTRATIVE INTERFACE
    public boolean addFlight(int xid, String flightNum, int numSeats, int price)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        ResourceItem item = queryItem(rmFlights, xid, flightNum, numSeats);

        if (item != null) {
            Flight f = (Flight) item;
            f.addSeats(numSeats);
            if (price >= 0)
                f.setPrice(price);
            try {
                return rmFlights.update(xid, rmFlights.getID(), flightNum, f);
            } catch (DeadlockException e) {
                e.printStackTrace();
            }
        } else {
            if (price < 0)
                price = 0;
            Flight f = new Flight(flightNum, price, numSeats);
            try {
                return rmFlights.insert(xid, rmFlights.getID(), f);
            } catch (DeadlockException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean deleteFlight(int xid, String flightNum)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        return deleteItem(rmFlights, xid, flightNum);
    }

    private boolean deleteItem(ResourceManager rm, int xid, String key)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (!xids.contains(xid))
            throw new InvalidTransactionException(xid, "");
        try {
            return rm.delete(xid, rm.getID(), key);
        } catch (DeadlockException e) {
            e.printStackTrace();
        }
        return false;
    }

    private ResourceItem queryItem(ResourceManager rm, int xid, String key, int num)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (!xids.contains(xid))
            throw new InvalidTransactionException(xid, "");
        if (key == null || num < 0)
            return null;

        ResourceItem item = null;
        try {
            item = rm.query(xid, rm.getID(), key);
        } catch (DeadlockException e) {
            e.printStackTrace();
        }

        return item;
    }

    public boolean addRooms(int xid, String location, int numRooms, int price)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        ResourceItem item = queryItem(rmRooms, xid, location, numRooms);

        if (item != null) {
            Hotel h = (Hotel) item;
            h.addRooms(numRooms);
            if (price >= 0)
                h.setPrice(price);
            try {
                return rmFlights.update(xid, rmFlights.getID(), numRooms, h);
            } catch (DeadlockException e) {
                e.printStackTrace();
            }
        } else {
            if (price < 0)
                price = 0;
            Hotel h = new Hotel(location, price, numRooms);
            try {
                return rmFlights.insert(xid, rmFlights.getID(), h);
            } catch (DeadlockException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean deleteRooms(int xid, String location, int numRooms)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        return deleteItem(rmRooms, xid, location);
    }

    public boolean addCars(int xid, String location, int numCars, int price)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        ResourceItem item = queryItem(rmCars, xid, location, numCars);

        if (item != null) {
            Car c = (Car) item;
            c.addCars(numCars);
            if (price >= 0)
                c.setPrice(price);
            try {
                return rmCars.update(xid, rmCars.getID(), numCars, c);
            } catch (DeadlockException e) {
                e.printStackTrace();
            }
        } else {
            if (price < 0)
                price = 0;
            Car car = new Car(location, price, numCars);
            try {
                return rmCars.insert(xid, rmCars.getID(), car);
            } catch (DeadlockException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean deleteCars(int xid, String location, int numCars)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        return true;
    }

    public boolean newCustomer(int xid, String custName)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        return true;
    }

    public boolean deleteCustomer(int xid, String custName)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        return true;
    }


    // QUERY INTERFACE
    public int queryFlight(int xid, String flightNum)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        return 0;
    }

    public int queryFlightPrice(int xid, String flightNum)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        return 0;
    }

    public int queryRooms(int xid, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        return 0;
    }

    public int queryRoomsPrice(int xid, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        return 0;
    }

    public int queryCars(int xid, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        return 0;
    }

    public int queryCarsPrice(int xid, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        return 0;
    }

    public int queryCustomerBill(int xid, String custName)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        return 0;
    }


    // RESERVATION INTERFACE
    public boolean reserveFlight(int xid, String custName, String flightNum)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        return true;
    }

    public boolean reserveCar(int xid, String custName, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        return true;
    }

    public boolean reserveRoom(int xid, String custName, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
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
                return true;
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
        return true;
    }

    public boolean dieRMBeforePrepare(String who)
            throws RemoteException {
        return true;
    }

    public boolean dieRMAfterPrepare(String who)
            throws RemoteException {
        return true;
    }

    public boolean dieTMBeforeCommit()
            throws RemoteException {
        return true;
    }

    public boolean dieTMAfterCommit()
            throws RemoteException {
        return true;
    }

    public boolean dieRMBeforeCommit(String who)
            throws RemoteException {
        return true;
    }

    public boolean dieRMBeforeAbort(String who)
            throws RemoteException {
        return true;
    }
}
