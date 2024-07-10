# snap-scheduler
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
![Maven Central with version prefix filter](https://img.shields.io/maven-central/v/io.opensw.scheduler/snap-scheduler-core/0.22.0)
[![SonarCloud Coverage](https://sonarcloud.io/api/project_badges/measure?project=luismpcosta_snap-scheduler&metric=coverage)](https://sonarcloud.io/component_measures/metric/coverage/list?id=luismpcosta_snap-scheduler)

Snap Scheduler project main objective is to simplify scheduling tasks in Spring Boot applications. Guarantees that one given task only runs once, even when you have multiple instances of a microservice. This is a common need in a Spring Boot applications where often we need to run tasks in background with a specific time or recurrence (run many times). To summarize this Project provides two main functionalities: 1. Run a task only once and 2. Simplified tasks' scheduler.

## Run task once - @SnapLock
When you run a task in Spring Boot scheduler with annotation "@Scheduled" (or similar annotation), and have multiple nodes of a microservice that task will start running in all nodes at same time. To guarantee that task only runs once we created an annotation "@SnapLock" which you can configure in your method definition. "@SnapLock" annotation valites if one given task is already running in another node: if is already running the task will not start in another node (skips this task), otherwise will start the task.

[View Usage](https://github.com/luismpcosta/snap-scheduler/blob/main/README.md#usage-snaplock)

## Schedule tasks - Snap Scheduler
In a Spring Boot application creating and running tasks dynamically is not a easy mission. To make it easier we created a simplified task scheduler. In this scheduler only with a few steps you can schedule and save tasks on a database. The scheduled tasks will run automatically on predefined time. Similarly to "@SnapLock" annotation all tasks scheduled and runned with Snap Scheduler have prevention to avoid multiple runs.

[View Usage](https://github.com/luismpcosta/snap-scheduler/blob/main/README.md#usage-snap-scheduler)

## General
Both functionalities of this project ("@SnapLock" and Snap Scheduler) create audit data. This allows to later understand if one given task was locked, started at the predefined time, execution time, error logs, etc.

The project supports the following transactional databases:
1. [PostgreSQL](https://www.postgresql.org/)
2. [Microsoft SQL Server](https://www.microsoft.com/en-us/sql-server)
3. [MySQL](https://www.mysql.com/)
4. [MariaDB](https://mariadb.org/)
5. [H2](https://www.h2database.com/)

---

---

# Usage @SnapLock
To use @SnapLock annotation and prevent multiple runs of task you need to:
1. Import this project dependency
2. Create required database tables
3. Annotate @Scheduled methods with @SnapLock

## 1. Import project dependency
To import this project dependecy to your project you can use maven, gradle, etc. or download jar [snap-scheduler-core](https://oss.sonatype.org/service/local/repositories/releases/content/io/opensw/scheduler/snap-scheduler-core/0.22.0/snap-scheduler-core-0.22.0-javadoc.jar) and configure mannualy in your project.

### Maven
```xml
<dependency>
  <groupId>io.opensw.scheduler</groupId>
  <artifactId>snap-scheduler-core</artifactId>
  <version>0.22.0</version>
</dependency>
```

### Gradle
```xml
implementation 'io.opensw.scheduler:snap-scheduler-core:0.22.0'
```

[See all](https://search.maven.org/artifact/io.opensw.scheduler/snap-scheduler-core/0.22.0/jar) dependency management.

## 2. Create required database tables
The @SnapLock mandatory tables that need to be created are "snap_lock" and "snap_task_audit" tables. 

See table definition for [PosgreSQL](https://github.com/luismpcosta/snap-scheduler/blob/main/snap-scheduler-core/sql/postgresql.sql), [Micosoft SQL Server](https://github.com/luismpcosta/snap-scheduler/blob/main/snap-scheduler-core/sql/mssqlserver.sql), [MySQL/MariaDB](https://github.com/luismpcosta/snap-scheduler/blob/main/snap-scheduler-core/sql/mysql.sql) or [H2](https://github.com/luismpcosta/snap-scheduler/blob/main/snap-scheduler-core/sql/h2.sql).

## 3. Import SnapAppConfig
To setup SnapScheduler you need to import predefined confiuration (SnapAppConfig) and add DataSource creation/configuration.

```java
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.opensw.scheduler.core.config.SnapSchedulerConfig;

@Configuration
@Import( SnapAppConfig.class )
public class SchedulerConfig {
	
	private static final String SNAP_DB_POOL_NAME = "snap-pool";
	
	@Bean( name = "snapDataSource" )
	public DataSource customDataSource( final DataSourceProperties properties,
			@Value( "${spring.datasource.hikari.schema}" ) final String schema ) {

		final HikariDataSource dataSource = properties.initializeDataSourceBuilder()
				.type( HikariDataSource.class ).build();

		dataSource.setPoolName( SNAP_DB_POOL_NAME );
		
		if ( !StringUtils.isEmpty( schema ) ) {
			dataSource.setSchema( schema );
		}

		return dataSource;
	}
	
}
```


## 4. Annotate @Scheduled methods with @SnapLock
To prevent tasks from running more than once you only need to add "@SnapLock" annotation to your "@Scheduled" method. SnapLock annotation has two properties:
1. **key** is the task identifier. This key needs to be equal between all nodes to guarantee an unique run.
2. **time** is the time in seconds the tasks stays locked (to prevent from start running in another node). For example, if you start a task in a given node and set time lock for 60 seconds then this task cannot start again within that time.

```java
import io.opensw.scheduler.core.annotations.SnapLock;
...

  @SnapLock(key = "REPORT_CURRENT_TIME", time = 60)
  @Scheduled(fixedRate = 30000)
  public void reportCurrentTime() {
    ...
  }
  
```

## @SnapLock Conclusion
You can audit all tasks that runned and had @SnapLock annotation. For this you only need to query table snap_task_audit.

View [snap_task_audit](https://github.com/luismpcosta/snap-scheduler/blob/main/README.md#task-audit-table-definition) table definition.

---

---

# Usage Snap Scheduler
To schedule tasks with this functionality you only need to:
1. Import this project dependency
2. Create required database tables
3. Configure snap scheduler properties
4. Schedule your tasks

**Do not forget that Snap Scheduler prevents multiple runs of the same task when you have a microservice deployed in many nodes.**

## 1. Import project dependency
To import this project dependecy to your project you can use maven, gradle, etc. or download jar [snap-scheduler-core](https://oss.sonatype.org/service/local/repositories/releases/content/io/opensw/scheduler/snap-scheduler-core/0.22.0/snap-scheduler-core-0.22.0-javadoc.jar) and configure mannualy in your project.

### Maven
```xml
<dependency>
  <groupId>io.opensw.scheduler</groupId>
  <artifactId>snap-scheduler-core</artifactId>
  <version>0.22.0</version>
</dependency>
```

### Gradle
```xml
implementation 'io.opensw.scheduler:snap-scheduler-core:0.22.0'
```

[See all](https://search.maven.org/artifact/io.opensw.scheduler/snap-scheduler-core/0.22.0/jar) dependency management.

## 2. Create required database tables
The Snap Scheduler mandatory tables that need to be created are "snap_scheduler" and "snap_task_audit" tables. 

See table definition for [PosgreSQL](https://github.com/luismpcosta/snap-scheduler/blob/main/snap-scheduler-core/sql/postgresql.sql), [Micosoft SQL Server](https://github.com/luismpcosta/snap-scheduler/blob/main/snap-scheduler-core/sql/mssqlserver.sql), [MySQL/MariaDB](https://github.com/luismpcosta/snap-scheduler/blob/main/snap-scheduler-core/sql/mysql.sql) or [H2](https://github.com/luismpcosta/snap-scheduler/blob/main/snap-scheduler-core/sql/h2.sql).

## 3. Import SnapAppConfig
To setup SnapScheduler you need to import predefined confiuration (SnapAppConfig) and add DataSource creation/configuration.

```java
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.opensw.scheduler.core.config.SnapSchedulerConfig;

@Configuration
@Import( SnapAppConfig.class )
public class SchedulerConfig {
	
	private static final String SNAP_DB_POOL_NAME = "snap-pool";
	
	@Bean( name = "snapDataSource" )
	public DataSource customDataSource( final DataSourceProperties properties,
			@Value( "${spring.datasource.hikari.schema}" ) final String schema ) {

		final HikariDataSource dataSource = properties.initializeDataSourceBuilder()
				.type( HikariDataSource.class ).build();

		dataSource.setPoolName( SNAP_DB_POOL_NAME );
		
		if ( !StringUtils.isEmpty( schema ) ) {
			dataSource.setSchema( schema );
		}

		return dataSource;
	}
	
}
```


## 4. Configure snap scheduler properties
In application.yml file you can configure snap scheduler properties.
1. **snap.scheduler.enabled** property by default is set true and enables polling taks from database. If false you can not run scheduled tasks.
2. **snap.scheduler.db-polling-interval** property defines time between polling tasks from database.

```xml
snap:
  scheduler:
    enabled: true
    db-polling-interval: PT1M
```

## 5. Schedule your tasks
To schedule your tasks with Snap Scheduler you have 2 options:
1. Schedule tasks **without data**
2. Schedule tasks **with data**

---

### 1. Schedule tasks without data
To schedule a task that needs no data to start running your task needs to ***implement*** **TaskExecutor**. Also, you can add/inject spring beans into your implementation of TaskExecutor.

```java
import org.springframework.stereotype.Component;

import io.opensw.scheduler.core.scheduler.task.TaskExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoidTask implements TaskExecutor {

	private final SomeService someService;
  
	@Override
	public void execute() {
		log.debug( "Task run without data" );
		someService.doSomething();
	}

}
```

With **TaskExecutor** implementation concluded you can schedule your task in Snap Scheduler. Hence, later your task can be identified and start in a given node. 

**See steps bellow to how you can schedule your task in Snap Scheduler as OneTimeTask or RecurringTask.**

#### 1.1 Schedule VoidTask as OneTimeTask
OneTimeTask only runs one time.

##### 1.1.1 Create task (OneTimeTask).
* **key** - task identifier and needs to be unique.
* **name** - task name.
* **runAt** - time that task starts to run.

```java
import io.opensw.scheduler.core.scheduler.task.OneTimeTask;

...

final String key = UUID.randomUUID().toString();
OneTimeTask oneTimeTask = OneTimeTask.create( VoidTask.class ).key( key ).name( "Task name" )
				.runAt( Instant.now().plusSeconds( 60 ) );
```

##### 1.1.2 Schedule your task
Task configurations are stored in a database. Later, tasks start running following predefined configurations time "runAt", and audit log are stored in table "snap_task_audit".


```java
import io.opensw.scheduler.core.scheduler.SnapScheduler;

...

private final SnapScheduler snapScheduler;

...

snapScheduler.schedule( oneTimeTask );

```

See complete [example](https://github.com/luismpcosta/snap-scheduler/blob/main/snap-scheduler-postgresql-example/src/main/java/io/opensw/scheduler/interfaces/SchedulerEndpoints.java).


#### 1.2 Configure VoidTask as RecurringTask
RecurringTask runs many times with prefedined recurrence duration.

##### 1.2.1 Create task (RecurringTask).
* **key** - task identifier and needs to be unique.
* **name** - task name.
* **runAt** - time that task starts to run.
* **recurrence** - recurrence duration.

```java
import io.opensw.scheduler.core.scheduler.task.RecurringTask;

...

final String key = UUID.randomUUID().toString();
RecurringTask recurringTask = RecurringTask.create( VoidTask.class ).key( key )
				.recurrence( Duration.ofSeconds( 60 ) ).name( "Task name" )
				.runAt( Instant.now() );
```

##### 1.2.2 Schedule the task
Task configurations are stored in a database. Later, tasks start running following predefined configurations time "runAt", and audit log are stored in table "snap_task_audit".


```java
import io.opensw.scheduler.core.scheduler.SnapScheduler;

...

private final SnapScheduler snapScheduler;

...

snapScheduler.schedule( recurringTask );

```

See complete [example](https://github.com/luismpcosta/snap-scheduler/blob/main/snap-scheduler-postgresql-example/src/main/java/io/opensw/scheduler/interfaces/SchedulerEndpoints.java).

---

### 2. Schedule tasks with data
To schedule tasks with data your task needs to ***extends*** **TaskDataExecutor** and your data needs to ***extends*** **TaskData**. Also, in TaskDataExecutor implementation you can add/inject spring beans, this beans are injected automatically.

**Create Data Object**
```java
import java.io.Serializable;

import io.opensw.scheduler.core.scheduler.task.TaskData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode( callSuper = false )
@NoArgsConstructor
public class Email extends TaskData implements Serializable {

	private static final long serialVersionUID = -2483934526341948446L;

	private String email;

}
```

**Create TaskDataExecutor Implementation**
```java
import org.springframework.stereotype.Component;

import io.opensw.scheduler.core.scheduler.task.TaskDataExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
public class EmailTask extends TaskDataExecutor< Email > {

	private final TestService testService;

	@Override
	public void execute( Email data ) {
		testService.test();
	}

}
```

With **TaskDataExecutor** implementation concluded you can schedule your task in Snap Scheduler. Hence, later your task can be identified and start in a given node. 

**See steps bellow to how you can schedule your task in Snap Scheduler as OneTimeTask or RecurringTask.**

#### 2.1 Configure EmailTask as OneTimeTask
OneTimeTask only run one time.

##### 2.1.1 Create task (OneTimeTask)
* **key** - task identifier and needs to be unique.
* **name** - task name.
* **runAt** - time that task starts to run.
* **data** - data to run task.

```java
import io.opensw.scheduler.core.scheduler.task.OneTimeTask;

...

Email email = new Email();
email.setEmail( "one.teste@gmail.com" );
		
final String key = UUID.randomUUID().toString();
OneTimeTask oneTimeTask = OneTimeTask.create( VoidTask.class ).key( key ).name( "Task name" )
				.data(email).runAt( Instant.now().plusSeconds( 60 ) );
```

##### 2.1.2 Schedule the task
Task configurations are stored in a database. Later, tasks start running following predefined configurations time "runAt", and audit log are stored in table "snap_task_audit".

```java
import io.opensw.scheduler.core.scheduler.SnapScheduler;

...

private final SnapScheduler snapScheduler;

...

snapScheduler.schedule( oneTimeTask );

```

See complete [example](https://github.com/luismpcosta/snap-scheduler/blob/main/snap-scheduler-postgresql-example/src/main/java/io/opensw/scheduler/interfaces/SchedulerEndpoints.java).

#### 2.2 Configure EmailTask as RecurringTask
RecurringTask runs many times with recurrence defined.

##### 2.2.1 Create task (RecurringTask).
* **key** - task identifier and needs to be unique.
* **name** - task name.
* **runAt** - time that task starts to run.
* **recurrence** - recurrence duration.
* **data** - data to run task.

```java
import io.opensw.scheduler.core.scheduler.task.RecurringTask;

...

Email email = new Email();
email.setEmail( "one.teste@gmail.com" );

final String key = UUID.randomUUID().toString();
RecurringTask recurringTask = RecurringTask.create( VoidTask.class ).key( key ).data(email)
				.recurrence( Duration.ofSeconds( 60 ) ).name( "Task name" )
				.runAt( Instant.now() );
```

##### 2.2.2 Schedule the task
Task configurations are stored in a database. Later, tasks start running following predefined configurations time "runAt", and audit log are stored in table "snap_task_audit".

```java
import io.opensw.scheduler.core.scheduler.SnapScheduler;

...

private final SnapScheduler snapScheduler;

...

snapScheduler.schedule( recurringTask );

```

See complete [example](https://github.com/luismpcosta/snap-scheduler/blob/main/snap-scheduler-postgresql-example/src/main/java/io/opensw/scheduler/interfaces/SchedulerEndpoints.java).

---

---

## Task Scheduler Usage Conclusion
All tasks scheduled and configured with this project are audited and you can analise all running dates and status, for this you only need to query table snap_task_audit.

View [snap_task_audit](https://github.com/luismpcosta/snap-scheduler/blob/main/README.md#task-audit-table-definition) table definition.

---

---

# Custom Database Configuration
To configure a database diferent of application database you can configure it like below.

```xml
snap:
  scheduler:
    enabled: true
    db-polling-interval: PT1M
    datasource:
      url: ${PG_JDBC_URL:jdbc:postgresql://localhost/snap}
      username: ${PG_USRNAME:postgres}
      password: ${PG_PASSWORD:root}
```

---

---

# Task Audit Table Definition
All tasks that runned in a given node are saved in snap_task_audit table including tasks that triggered an error.

|Column           |Description                                                                  |
| --------------- | :---------------------------------------------------------------------------|
|id               |is an auto increment key	                                                |
|key              |task identifier (in example before the key was "REPORT_CURRENT_TIME")	|
|method           |class and method when @SnapLock is called                                    |
|run_on           |server name where the task runned                                            |
|start_run        |time when the task started                                                   |
|end_run          |time when the task ends                                                    	|
|run_time_seconds |task total execution time (in seconds)                                       |
|task_error       |when task throws an error. Error description is stored here                  |

---

---

# License
This project was completely free and open source, under [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0.txt), all feedback and pull-requests are welcome!

# Releases
The release notes

## 0.18.0
* Upgrade spring-boot release to 2.6.2

## 0.9.0 to 0.15.0
* Bug fix after first integration in marketplace platform
* Improve lock queries

## 0.8.0
* Improve code of repositories implementation
* Change database schema to remove databse reserved words from table schema, columns, keys, etc 

## 0.7.0
* Externalization of DataSource configuration

## 0.6.0
* Change event listener and upgrade versions of example projects 

## 0.5.0
* Change project build 

## 0.4.0
* Custom database configuration

## 0.3.0
* First release with implementation of @SnapLock and Snap Scheduler


