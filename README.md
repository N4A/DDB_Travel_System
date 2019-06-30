# DDB_Travel_System
DDB course project: 分布式旅游预订系统
![arch](./assets/img/arch.PNG)

# 1 要求
## 1.1 系统概述

- 资源管理器(Resource Manager)‏
    - 数据操作：查询、更新(插入，删除) 数据，提供对资源访问的封装，完成对数据的实际访问和数据持久化
- 事务管理器(Transaction Manager)‏
    - 提供事务管理功能，保证事务的ACID特性
- 流程控制器(Workflow Controller)‏
    - 客户端看到的整个系统的调用接口，使系统的其它部分如TM,RM和实际的数据表对客户端透明

## 1.2 数据定义
- 系统存储以下五张表
    - FLIGHTS(flightNum, price, numSeats, numAvail)‏
    - HOTELS(location, price, numRooms, numAvail)‏
    - CARS(location, price, numCars, numAvail)‏
    - CUSTOMERS(custName)‏
    - RESERVATIONS(custName, resvType, resvKey)‏
- 关于数据的一些假设，简化系统数据库模式设计
    - 每个地点(localtion)只有一个旅馆和租车行
    - 只有一个航空公司
    - 一个航班上所有座位的价格相同
    - 同一地点所有房间和车价格相同

## 1.3 数据操作

- 航班
    - 添加新航班
    - 给航班增加座位
    - 取消航班
    - 查询航班上的剩余座位数
    - 查询航班价格
    - 为客户预定航班的座位
- 租车和旅馆数据有着类似操作
- 需要实现的操作在接口文件WorkflowController.java中描述
