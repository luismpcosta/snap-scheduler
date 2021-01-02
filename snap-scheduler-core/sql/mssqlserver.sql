CREATE TABLE snap_lock (
  [key] varchar(100) NOT NULL,
  method varchar(255) NOT NULL,
  lock_until datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  lock_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  lock_by varchar(100) NOT NULL,
  PRIMARY KEY ([key],method)
);

CREATE TABLE snap_task_audit
(
	id BIGINT IDENTITY(1,1) NOT NULL,
    [key] varchar(100) NOT NULL,
  	method varchar(255) NOT NULL,
	run_on varchar(100) NOT NULL,
    start_run datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_run datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    run_time_seconds BIGINT NOT NULL,
    task_error text,
    PRIMARY KEY (id)
);

CREATE TABLE snap_scheduler (
	name varchar(100) NOT NULL,
	[key] varchar(100) NOT NULL,
	type varchar(100) NOT NULL,
	task_class varchar(255) NOT NULL,
	task_data text,
	task_data_class varchar(255),
	run_at datetime NOT NULL,
	recurrence varchar(50),
	picked BIT NOT NULL DEFAULT 0,
	picked_by varchar(100),
	end_run datetime,
	CONSTRAINT snap_scheduler_pkey PRIMARY KEY ([key])
);
