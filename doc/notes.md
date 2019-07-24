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
2 return 
2 exit
```
line 72 should return 40, but actually return 30.