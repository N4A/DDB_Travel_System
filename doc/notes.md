## A lock case
```text
2
1 call start
1 return
1 call addFlight xid "347" 100 310 
1 return true 
1 call addFlight xid "3471" 1001 3101 
1 return true 
1 call addRooms xid "Stanford" 200 150 
1 return true 
1 call addCars xid "SFO" 300 30 
1 return true 
1 call newCustomer xid "John" 
1 return true 
1 call commit xid
1 return true
1 call start
1 return
2 call start
2 return
1 call reserveItinerary xid "John" (347,3471) "Stanford" false true 
1 return true 
2 call queryCustomerBill xid "John"
1 call commit xid
1 return true
2 return
2 exit
```
Should line 24 wait for line 22 ?
If so, do like this?
```text
    // lock customer,
    // This is for test case Liti3. The case think the reservation action should modify
    //   the info of customers and get the WRITE lock.
    // But, in our implementations, all new reservations are stored in Reservations table.
    // We just need the READ lock for customers.
    try {
        rmCustomers.update(xid, rmCustomers.getID(), custName, cust); // just to set WRITE lock for the test
    } catch (DeadlockException e) {
        // dead lock happened, quit this transaction
        abort(xid);
        throw new TransactionAbortedException(xid, "This transaction cause dead lock: " + e.getMessage());
    }
```
If not, design a similar case for the test
```text
2
1 call start
1 return
1 call addFlight xid "347" 100 310 
1 return true 
1 call addFlight xid "3471" 1001 3101 
1 return true 
1 call addRooms xid "Stanford" 200 150 
1 return true 
1 call addCars xid "SFO" 300 30 
1 return true 
1 call newCustomer xid "John" 
1 return true 
1 call commit xid
1 return true
1 call start
1 return
2 call start
2 return
1 call addCars "SFO" 300 40 
1 return true 
2 call queryCarsPrice "SFO"
1 call commit xid
1 return true
2 return 40
2 exit
```
line 72 should return 40, but actually return 30.

## About resource lock
wrong case: 
```text
        // TODO should aquire the lock first
        RMTable table = getTable(xid, tablename);
        ResourceItem item = table.get(key);
        if (item != null && !item.isDeleted()) {
            // TODO value has already been read to memory
            table.lock(key, LockManager.READ);
            if (!storeTable(table, new File("data/" + xid + "/" + tablename))) {
                throw new RemoteException("System Error: Can't write table to disk!");
            }
            return item;
        }
```
A simple solution
```text
        // get lock before read from disk
        // will lock the un-existed item that maybe inserted later
        // can not work if we don't know the key.
        if (!lm.lock(xid, tablename + ":" + key.toString(), LockManager.READ))
            throw new RuntimeException();
        RMTable table = getTable(xid, tablename);
        ResourceItem item = table.get(key);
        if (item != null && !item.isDeleted()) {
            // put lock record
            table.putLock(key, LockManager.READ);
            if (!storeTable(table, new File("data/" + xid + "/" + tablename))) {
                throw new RemoteException("System Error: Can't write table to disk!");
            }
            return item;
        }
```
A more reasonable solution
```text
        // read twice, first to get lock, then to read.
        // if the item hasn't been locked by other transactions, just read twice and the results are same
        // if the item has been locked by other transactions, then wait for lock and read new result.
        // first to get lock
        RMTable table = getTable(xid, tablename);
        ResourceItem item = table.get(key);
        if (item != null && !item.isDeleted()) {
            table.lock(key, LockManager.READ);
            
            // then to read values
            // remove old value
            Hashtable xidtables = (Hashtable) tables.get(xid); // can not be null
            synchronized (xidtables) {
                xidtables.remove(tablename);
            }
            // read new value
            table = getTable(xid, tablename);
            item = table.get(key);
            if (!storeTable(table, new File("data/" + xid + "/" + tablename))) {
                throw new RemoteException("System Error: Can't write table to disk!");
            }
            return item;
        }
```
For the case we don't know the keys
```text
        Collection<ResourceItem> result = new ArrayList<>();

        // read twice, first to get lock, then to read.
        // if the item hasn't been locked by other transactions, just read twice and the results are same
        // if the item has been locked by other transactions, then wait for lock and read new result.
        // first to get lock
        RMTable table = getTable(xid, tablename);
        synchronized (table) {
            for (Iterator iter = table.keySet().iterator(); iter.hasNext(); ) {
                Object key = iter.next();
                ResourceItem item = table.get(key);
                if (item != null && !item.isDeleted() && item.getIndex(indexName).equals(indexVal)) {
                    table.lock(key, LockManager.READ);
                }
            }
        }

        // then to read values
        // remove old value
        Hashtable xidtables = (Hashtable) tables.get(xid); // can not be null
        synchronized (xidtables) {
            xidtables.remove(tablename);
        }
        // read new value
        table = getTable(xid, tablename);
        synchronized (table) {
            for (Iterator iter = table.keySet().iterator(); iter.hasNext(); ) {
                Object key = iter.next();
                ResourceItem item = table.get(key);
                if (item != null && !item.isDeleted() && item.getIndex(indexName).equals(indexVal)) {
                    // table.lock(key, LockManager.READ); // have been locked
                    result.add(item);
                }
            }
            if (!result.isEmpty()) {
                if (!storeTable(table, new File("data/" + xid + "/" + tablename))) {
                    throw new RemoteException("System Error: Can't write table to disk!");
                }
            }
        }
        return result;
```