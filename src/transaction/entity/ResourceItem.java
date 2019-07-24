/*
 * Created on 2005-5-17
 *
 */
package transaction.entity;

import transaction.InvalidIndexException;

import java.io.Serializable;

/**
 * @author RAdmin
 * <p>
 */
public abstract class ResourceItem implements Cloneable, Serializable {
    protected boolean isdeleted = false;

    public abstract Object getIndex(String indexName) throws InvalidIndexException;

    public abstract Object getKey();

    public  boolean isDeleted() {
        return isdeleted;
    }

    public void delete() {
        isdeleted = true;
    }

    public abstract Object clone();
}