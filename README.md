## schedule-job

Springboot整合Quartz基于数据库的调度平台，通过web界面动态地对HTTP任务进行增、删、改、查。
## 使用
1. clone项目，执行建表语句（msyql）：schedule-job/doc/db/zxq_schedule_job_table.sql
2. 修改数据库连接配置
3. 启动项目，访问：http://127.0.0.1:8888/job  (默认管理员登录账号：admin  123456)


## 效果图

首页

![image-20200420143133412](C:\Users\Doctor\AppData\Roaming\Typora\typora-user-images\image-20200420143133412.png)

任务管理页

![image-20200420143159810](C:\Users\Doctor\AppData\Roaming\Typora\typora-user-images\image-20200420143159810.png)

添加任务

![image-20200420143217926](C:\Users\Doctor\AppData\Roaming\Typora\typora-user-images\image-20200420143217926.png)

任务日志

![image-20200420143242054](C:\Users\Doctor\AppData\Roaming\Typora\typora-user-images\image-20200420143242054.png)
