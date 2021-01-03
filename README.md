# snap-scheduler
Snap Scheduler it is a project with the main objective to make simple scheduling tasks in spring boot applications and guarantee that task run only once even when you have multiple instances of a microservice. This is a common needs in a spring boot microservices application (run in one or many nodes) and needs to run tasks in background with a specific time or recurrence (run many times).

## Run task once
When you run a tasks with spring boot scheduler annotation "@Scheduled", or similar annotation, and have more than one node of microservice the task start running in all nodes at same time. To guarantee that taks run only once we create an annotation "@SnapLock" that you can configure in method definition and this annotation valite if task is already running on one node, if was running the execution was skipped.

[View Usage](https://github.com/luismpcosta/snap-scheduler/blob/main/README.md#usage-snaplock)

## Schedule tasks
In a spring boot application create and run tasks dynamically wasn´t a easy mission, to simplify this mission we create a task scheduler. With this scheduler in a kick steps you can schedule and save tasks on database, the scheduled tasks run automatically on defined time.

[View Usage](https://github.com/luismpcosta/snap-scheduler/blob/main/README.md#usage-task-scheduler)

## General
Boot features of this project (lock and scheduler tasks) create audit data, to analise if task was locked or run in estipulated time, etc.

At moment the project was developed to support the transactional databases below:
1. [PostgreSQL](https://www.postgresql.org/)
2. [Microsoft SQL Server](https://www.microsoft.com/en-us/sql-server)
3. [MySQL](https://www.mysql.com/)
4. [MariaDB](https://mariadb.org/)
5. [H2](https://www.h2database.com/)

# Usage @SnapLock
To use lock annotation and prevent multiple runs of task you need:
1. Import project dependency
2. Create database tables
3. Annotate @Scheduled methods with @SnapLock

## 1. Import project dependency
To import dependecy to your project you can use maven or download jar [snap-scheduler-core](https://oss.sonatype.org/service/local/repositories/releases/content/io/opensw/scheduler/snap-scheduler-core/0.3.0/snap-scheduler-core-0.3.0-javadoc.jar) and add mannualy to you project.

### Maven
```xml
<dependency>
  <groupId>io.opensw.scheduler</groupId>
  <artifactId>snap-scheduler-core</artifactId>
  <version>0.3.0</version>
</dependency>
```

## 2. Create database tables
To create required tables in your database-schema, if you only use @SnapLock annotation (don´t schedule tasks) you only need to create "snap_lock" and "snap_task_audit" tables. See table definition for [PosgreSQL](https://github.com/luismpcosta/snap-scheduler/blob/main/snap-scheduler-core/sql/postgresql.sql), [Micosoft SQL Server](https://github.com/luismpcosta/snap-scheduler/blob/main/snap-scheduler-core/sql/mssqlserver.sql), [MySQL/MariaDB](https://github.com/luismpcosta/snap-scheduler/blob/main/snap-scheduler-core/sql/mysql.sql) or [H2](https://github.com/luismpcosta/snap-scheduler/blob/main/snap-scheduler-core/sql/h2.sql).

## 3. Annotate @Scheduled methods with @SnapLock
To prevent task run more than once you only need to add "@SnapLock" annotation to your "@Scheduled" method. SnapLock annotation have two properties:
1. key => is the task identifier, this key needs to be equal between all nodes to guarantee unique run
2. time => is the time in seconds that tasks maintainance locked (example if you lock a task for 60 seconds when start run task was locked from this instant to this instant plus 60 seconds).
```java
  @SnapLock(key = "REPORT_CURRENT_TIME", time = 60)
  @Scheduled(fixedRate = 30000)
  public void reportCurrentTime() {
    ...
  }
```
## @SnapLock Conclusion
After configure Snap lock you can audit all running tasks annotated with @SnapLock, for this you only need to query table snap_task_audit.

View [snap_task_audit](https://github.com/luismpcosta/snap-scheduler/blob/main/README.md#task-audit-table-definition) table definition.


# Usage Task Scheduler
To schedule task with this approach you only need:
1. Import project dependency
2. Create database tables
3. Configure snap scheduler properties
4. Schedule tasks


# Task Audit Table Definition
All task runs are saved in snap_task_audit table even if an error occurs.

|Column           |Description                                                                      |
| --------------- | :-------------------------------------------------------------------------------|
|id               |it's an auto icrement key                                                        |
|key              |was the task identifier (in example before the key was "REPORT_CURRENT_TIME")    |
|method           |class and method when @snapLock was called                                       |
|run_on           |server name when task was runned                                                 |
|start_run        |instant that task started                                                        |
|end_run          |instant that task end running                                                    |
|run_time_seconds |task total execution time in seconds                                             |
|task_error       |when task throws an error this error was saved here                              |
