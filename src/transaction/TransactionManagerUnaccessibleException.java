/*
 * Created on 2005-5-18
 *
 */
package transaction;

/**
 * @author RAdmin
 * <p>
 */
public class TransactionManagerUnaccessibleException extends Exception {
    public TransactionManagerUnaccessibleException() {
        super("Transaction Manager Unaccessible");
    }
}
