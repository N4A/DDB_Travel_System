package transaction;

import java.rmi.*;


/**
 * A problem occurred that caused the transaction to abort.  Perhaps
 * deadlock was the problem, or perhaps a device or communication
 * failure caused this operation to abort the transaction.
 */
public class TransactionAbortedException extends Exception {
    public TransactionAbortedException(int Xid, String msg) {
        super("The transaction " + Xid + " aborted:" + msg);
    }
}
