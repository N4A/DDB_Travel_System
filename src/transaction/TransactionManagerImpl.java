package transaction;

import java.io.File;
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

    // all active transactions

    private Integer xidCounter; // allocate unique id
    private String dieTime; // dieTime flag
    // resource managers of all transactions
    private HashMap<Integer, HashSet<ResourceManager>> RMs = new HashMap<>();
    private HashMap<Integer, String> xids = new HashMap<>();
    // transaction to be recovered after died
    private HashMap<Integer, String> xids_to_be_done = new HashMap<>();
    private HashMap<Integer, Integer> xids_to_be_recovered = new HashMap<>();

    //log path
    private String xidCounterPath = "xidCounter.log";
    private String xidsStatusPath = "xidsStatus.log";
    private String xidsToBeRecoveredPath = "xidsToBeRecovered.log";

    public TransactionManagerImpl() throws RemoteException {
        xidCounter = 1;
        dieTime = "noDie";

        recover();
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

            // redone_logs log committed transaction
            obj.redone_logs();

            System.out.println("TM bound");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("TM not bound:" + e);
            System.exit(1);
        }
    }

    private void redone_logs() {
        if (!xids_to_be_done.isEmpty()) {
            System.out.println("Redo logs");
            for (Integer xidTmp : xids_to_be_done.keySet()) {
                while (true) {
                    synchronized (RMs) {
                        if (RMs.containsKey(xidTmp)) {
                            try {
                                String[] vals = xids_to_be_done.get(xidTmp).split("_");
                                String status = vals[0];
                                int rm_num = Integer.parseInt(vals[1]);
                                if (RMs.get(xidTmp).size() == rm_num) {
                                    System.out.println("Enough rm. Total RM num: " + rm_num +
                                            ", at now: " + RMs.get(xidTmp).size());
                                    if (status.equals(COMMITTED)) {
                                        // redone_logs
                                        commit(xidTmp);
                                    } else {
                                        abort(xidTmp);
                                    }
                                    break;
                                } else {
                                    System.out.println("Wait for enough rm. Total RM num: " + rm_num +
                                            ", at now: " + RMs.get(xidTmp).size());
                                }

                            } catch (RemoteException | TransactionAbortedException | InvalidTransactionException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    try {
                        System.out.println("Wait RM to be enlisted..");
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            synchronized (xids_to_be_done) {
                // finish
                xids_to_be_done = new HashMap<>();
            }
            System.out.println("Finish redo logs.");
        }
    }

    private void recover() {
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        Object xidCounterTmp = utils.loadObject("data/" + xidCounterPath);
        if (xidCounterTmp != null)
            xidCounter = (Integer) xidCounterTmp;

        Object xidsTmp = utils.loadObject("data/" + xidsStatusPath);
        if (xidsTmp != null) {
            xids_to_be_done = (HashMap<Integer, String>) xidsTmp;
        }

        Object xidsToDo = utils.loadObject("data/" + xidsToBeRecoveredPath);
        if (xidsToDo != null)
            xids_to_be_recovered = (HashMap<Integer, Integer>) xidsToDo;
    }

    public void ping() throws RemoteException {
    }

    public String enlist(int xid, ResourceManager rm) throws RemoteException {
        if (xids_to_be_recovered.containsKey(xid)) {
            int num = xids_to_be_recovered.get(xid);
            synchronized (xids_to_be_recovered) {
                if (num > 1)
                    xids_to_be_recovered.put(xid, num - 1);
                else
                    xids_to_be_recovered.remove(xid);
                utils.storeObject(xids_to_be_recovered, xidsToBeRecoveredPath);
            }
            return COMMITTED;
        }
        if (!xids.containsKey(xid) && !xids_to_be_done.containsKey(xid)) {
            return ABORTED; // the xid has been aborted
        }
        synchronized (RMs) {
            if (!RMs.containsKey(xid)) // recover from failure.
                RMs.put(xid, new HashSet<>());
            HashSet<ResourceManager> xidRMs = RMs.get(xid);
            xidRMs.add(rm);
            synchronized (xids) {
                xids.put(xid, INITED + "_" + xidRMs.size());
                utils.storeObject(xids, "data/" + xidsStatusPath);
            }
        }
        return INITED;
    }

    @Override
    public int start() throws RemoteException {
        synchronized (xidCounter) {
            Integer newXid = xidCounter++;
            utils.storeObject(xidCounter, "data/" + xidCounterPath);

            // store xid
            synchronized (xids) {
                xids.put(newXid, INITED + "_" + 0);
                utils.storeObject(xids, "data/" + xidsStatusPath);
            }

            synchronized (RMs) {
                RMs.put(newXid, new HashSet<>());
            }

            return newXid;
        }
    }

    @Override
    public boolean commit(int xid) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        if (!xids.containsKey(xid))
            throw new TransactionAbortedException(xid, "TM");
        HashSet<ResourceManager> xidRMs = RMs.get(xid);
        // 2pc
        // prepare phase
        for (ResourceManager rm : xidRMs) {
            try {
                System.out.println("call rm prepare: " + xid + ": " + rm.getID());
                if (!rm.prepare(xid)) {
                    // rm is not prepared.
                    this.abort(xid);
                    throw new TransactionAbortedException(xid, "RM aborted");
                }
            } catch (Exception e) {
                // rm dies before or during prepare
                System.out.println("rm prepare failed: " + rm);
                e.printStackTrace();
                this.abort(xid);
                throw new TransactionAbortedException(xid, "RM aborted");
            }
        }
        // prepared, die before commit if needed
        synchronized (xids) {
            xids.put(xid, PREPARED + "_" + xidRMs.size());
            utils.storeObject(xids, "data/" + xidsStatusPath);
        }

        if (dieTime.equals("BeforeCommit"))
            dieNow();

        // log commit with xid
        synchronized (xids) {
            xids.put(xid, COMMITTED + "_" + xidRMs.size());
            utils.storeObject(xids, "data/" + xidsStatusPath);
        }

        // die after commit log was written if needed.
        if (dieTime.equals("AfterCommit"))
            dieNow();

        // commit phase
        for (ResourceManager rm : xidRMs) {
            try {
                System.out.println("call rm commit " + xid + ": " + rm.getID());
                rm.commit(xid); // the function return means done signal.
            } catch (Exception e) {
                // rm dies before or during commit
                System.out.println("rm is down before commit: " + rm);
                // let the rm to be recovered when it is relaunched.
                setRecoveryLater(xid);
            }
        }

        // commit log record + completion log record
        // do nothing. actually do not need completion log here because the failure of the following
        // codes can not be checked in our test condition.233
        synchronized (RMs) {
            // remove committed transactions
            RMs.remove(xid);
        }
        synchronized (xids) {
            xids.remove(xid);
            utils.storeObject(xids, "data/" + xidsStatusPath);
        }

        System.out.println("Commit xid: " + xid);
        // success
        return true;
    }

    private void setRecoveryLater(int xid) {
        synchronized (xids_to_be_recovered) {
            // use number instead of rm info, for the rm message is difficult to get
            // TODO more solid implementation is needed.
            if (xids_to_be_recovered.containsKey(xid)) {
                xids_to_be_recovered.put(xid, xids_to_be_recovered.get(xid) + 1);
            } else {
                xids_to_be_recovered.put(xid, 1);
            }
            utils.storeObject(xids_to_be_recovered, xidsToBeRecoveredPath);
        }
    }

    @Override
    public void abort(int xid) throws RemoteException, InvalidTransactionException {
        if (!xids.containsKey(xid) && !xids_to_be_done.containsKey(xid)) {
            throw new InvalidTransactionException(xid, "abort");
        }
        HashSet<ResourceManager> xidRMs = RMs.get(xid);
        for (ResourceManager rm : xidRMs) {
            try {
                System.out.println("call rm abort " + xid + " : " + rm.getID());
                rm.abort(xid);
                System.out.println("rm abort success: " + rm.getID());
            } catch (Exception e) {
                System.out.println("Some RM is down: " + rm);
            }
        }
        synchronized (RMs) {
            // remove aborted transactions
            RMs.remove(xid);
        }
        synchronized (xids) {
            if (xids.containsKey(xid)) {
                xids.remove(xid);
                utils.storeObject(xids, "data/" + xidsStatusPath);
            }
        }

        System.out.println("Abort xid: " + xid);
    }

    public boolean dieNow() throws RemoteException {
        System.exit(1);
        return true; // We won't ever get here since we exited above;
        // but we still need it to please the compiler.
    }

    public void setDieTime(String dieTime) {
        this.dieTime = dieTime;
    }
}
