/*
 * Created on 2005-5-17
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package transaction.entity;

import transaction.InvalidIndexException;

import java.io.Serializable;

/**
 * @author RAdmin
 * <p>
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
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