package lockmgr;

/**
 * Thrown to indicate that the transaction is deadlocked and should be aborted.
 */
public class DeadlockException extends Exception {
    private int xid = 0;

    public DeadlockException(int xid, String msg) {
        super("The transaction " + xid + " is deadlocked:" + msg);
        this.xid = xid;
    }

    int GetXId() {
        return xid;
    }
}