package lockmgr;

/*
 * The transaction requested a lock that it already had.
 */

public class RedundantLockRequestException extends Exception {
    protected int xid = 0;

    public RedundantLockRequestException(int xid, String msg) {
        super(msg);
        this.xid = xid;
    }

    public int getXId() {
        return this.xid;
    }
}