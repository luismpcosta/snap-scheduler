CREATE TABLE IF NOT EXISTS snap_lock
(
    task_key varchar(255) NOT NULL,
	task_method varchar(255) NOT NULL,
    lock_until datetime NOT NULL,
    lock_at datetime NOT NULL,
    lock_by text NOT NULL,
    PRIMARY KEY (task_key, task_method)
);

CREATE TABLE IF NOT EXISTS snap_task_audit
(
	id BIGINT AUTO_INCREMENT NOT NULL,
    task_key varchar(100) NOT NULL,
  	task_method varchar(255) NOT NULL,
	run_on varchar(100) NOT NULL,
    start_run datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_run datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    run_time_seconds BIGINT NOT NULL,
    task_error text,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS snap_scheduler (
	name text NOT NULL,
	task_key varchar(255) NOT NULL,
	type text NOT NULL,
	task_class text NOT NULL,
	task_data text,
	task_data_class text,
	run_at datetime NOT NULL,
	recurrence text,
	picked boolean NOT NULL DEFAULT false,
	picked_by text,
	end_run datetime,
	PRIMARY KEY (task_key)
);
