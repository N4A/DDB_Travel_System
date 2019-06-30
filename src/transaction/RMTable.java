/*
 * Created on 2005-5-18
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package transaction;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import lockmgr.DeadlockException;
import lockmgr.LockManager;

/**
 * @author RAdmin
 * <p>
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class RMTable implements Serializable {
    protected Hashtable table = new Hashtable();

    transient protected RMTable parent;

    protected Hashtable locks = new Hashtable();

    transient protected LockManager lm;

    protected String tablename;

    protected int xid;

    public RMTable(String tablename, RMTable parent, int xid, LockManager lm) {
        this.xid = xid;
        this.tablename = tablename;
        this.parent = parent;
        this.lm = lm;
    }

    public void setLockManager(LockManager lm) {
        this.lm = lm;
    }

    public void setParent(RMTable parent) {
        this.parent = parent;
    }

    public String getTablename() {
        return tablename;
    }

    public void relockAll() throws DeadlockException {
        for (Iterator iter = locks.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry entry = (Map.Entry) iter.next();
            if (!lm.lock(xid, tablename + ":" + entry.getKey().toString(), ((Integer) entry.getValue()).intValue()))
                throw new RuntimeException();
        }
    }

    public void lock(Object key, int lockType) throws DeadlockException {
        if (!lm.lock(xid, tablename + ":" + key.toString(), lockType))
            throw new RuntimeException();
        locks.put(key, new Integer(lockType));
    }

    public ResourceItem get(Object key) {
        ResourceItem item = (ResourceItem) table.get(key);
        if (item == null && parent != null)
            item = parent.get(key);
        return item;
    }

    public void put(ResourceItem item) {
        table.put(item.getKey(), item);
    }

    public void remove(ResourceItem item) {
        table.remove(item.getKey());
    }

    public Set keySet() {
        Hashtable t = new Hashtable();
        if (parent != null) {
            t.putAll(parent.table);
        }
        t.putAll(table);
        return t.keySet();
    }
}