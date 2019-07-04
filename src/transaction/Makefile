RMIREGPORT =3345 
PROJECTROOT = ..
# Path = usr/bin # use java in environment
all : server client

server : ResourceManager.class \
         ResourceManagerImpl.class \
         ResourceManagerImpl_Stub.class \
         TransactionManager.class \
         TransactionManagerImpl.class \
         TransactionManagerImpl_Stub.class \
         WorkflowController.class \
         WorkflowControllerImpl.class \
         WorkflowControllerImpl_Stub.class \
         InvalidTransactionException.class \
         TransactionAbortedException.class

client: Client.class SimpleClient.class

runregistry :
	rmiregistry -J-classpath -J$(PROJECTROOT) $(RMIREGPORT)

runtm : server
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -Djava.security.policy=./security-policy transaction.TransactionManagerImpl

runrmflights : server
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DrmiName=RMFlights -Djava.security.policy=./security-policy transaction.ResourceManagerImpl

runrmrooms : server
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DrmiName=RMRooms -Djava.security.policy=./security-policy transaction.ResourceManagerImpl

runrmcars : server
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DrmiName=RMCars -Djava.security.policy=./security-policy transaction.ResourceManagerImpl

runrmcustomers : server
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -DrmiName=RMCustomers -Djava.security.policy=./security-policy transaction.ResourceManagerImpl

runwc : server
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) -Djava.security.policy=./security-policy transaction.WorkflowControllerImpl

runclient : client
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) transaction.Client

runsimpleclient : client
	java -classpath $(PROJECTROOT) -DrmiPort=$(RMIREGPORT) transaction.SimpleClient

clean :
	$(RM) *.class

%.class : %.java
	javac -classpath $(PROJECTROOT) $<

%_Stub.class : %.java
	rmic -classpath $(PROJECTROOT) -d $(PROJECTROOT) transaction.$*

.PHONY : runregistry runtm runrmflights runrmrooms runrmcars runrmcustomers runwc runclient clean
