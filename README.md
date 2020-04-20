## schedule-job

Springboot整合Quartz基于数据库的调度平台，通过web界面动态地对HTTP任务进行增、删、改、查。
## 使用
1. clone项目，执行建表语句（msyql）：schedule-job/doc/db/zxq_schedule_job_table.sql
2. 修改数据库连接配置
3. 启动项目，访问：http://127.0.0.1:8888/job  (默认管理员登录账号：admin  123456)


## 效果图

首页

![image-20200420143133412](https://raw.githubusercontent.com/zhangxq4811/resource-center/master/%E9%A6%96%E9%A1%B5.png)

任务管理页

![image-20200420143159810](https://raw.githubusercontent.com/zhangxq4811/resource-center/master/%E4%BB%BB%E5%8A%A1%E7%AE%A1%E7%90%86%E7%95%8C%E9%9D%A2.png)

添加任务

![image-20200420143217926](https://raw.githubusercontent.com/zhangxq4811/resource-center/master/%E6%96%B0%E5%A2%9E%E4%BB%BB%E5%8A%A1.png)

任务日志

![image-20200420143242054](https://github.com/zhangxq4811/resource-center/blob/master/%E6%97%A5%E5%BF%97%E8%AF%A6%E6%83%85.png)
