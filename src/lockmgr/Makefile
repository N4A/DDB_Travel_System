PROJECTROOT = ..

lockmgr : DataObj.class DeadlockException.class LockManager.class RedundantLockRequestException.class TimeObj.class TPHashTable.class TrxnObj.class WaitObj.class XObj.class

lmtest : LockManagerTest.class lockmgr

runlmtest : lmtest
	java -classpath $(PROJECTROOT) lockmgr.LockManagerTest

clean :
	$(RM) *.class

%.class : %.java
	javac -classpath $(PROJECTROOT) $<

.PHONY : runlmtest clean

