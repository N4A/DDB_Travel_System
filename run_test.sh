#!/usr/bin/env bash
# kill the process using the port 3345
# on Windows Powershell
#   FOR /F "tokens=5 delims= " %P IN ('netstat -ano | findstr :3345') DO @ECHO TaskKill.exe /PID %P
#   how kill process by port by hand
#   netstat -ano | findstr :3345
#   taskkill /F /pid pid_number
# on ubuntu
#   sudo kill $(sudo lsof -t -i:3345)

# make lock util
cd src/lockmgr
make

# make server
cd ../transaction
make clean
make server
make client

# run server, if the rmi registry has already been launched, it's ok to be failed
make runregistry &

# run test
cd ../test.part2
# rm test log
rm results/* -rf
rm data/* -rf
export CLASSPATH=.:gnujaxp.jar
javac RunTests.java
java -DrmiPort=3345 RunTests MASTER.xml
