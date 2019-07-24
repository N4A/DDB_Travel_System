/*
 * Created on 2005-5-17
 *
 */
package transaction;

/**
 * @author RAdmin
 * <p>
 */
public class InvalidIndexException extends Exception {
    public InvalidIndexException(String indexName) {
        super("Invalid index: " + indexName);
    }
}