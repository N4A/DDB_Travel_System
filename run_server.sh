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

# run server
make runregistry &
make runtm &
make runrmflights &
make runrmrooms &
make runrmcars &
make runrmcustomers &
make runwc &
