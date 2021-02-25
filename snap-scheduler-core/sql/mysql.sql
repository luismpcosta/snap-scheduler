CREATE TABLE `snap_lock` (
  `task_key` varchar(100) NOT NULL,
  `task_method` varchar(255) NOT NULL,
  `lock_until` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `lock_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `lock_by` varchar(100) NOT NULL,
  PRIMARY KEY (`task_key`,`task_method`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `snap_task_audit`
(
	`id` INT NOT NULL AUTO_INCREMENT,
    `task_key` varchar(100) NOT NULL,
  	`task_method` varchar(255) NOT NULL,
  	`run_on` varchar(100) NOT NULL,
    `start_run` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
    `end_run` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
    `run_time_seconds` BIGINT NOT NULL,
    `task_error` json,
	PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `snap_scheduler` (
	`name` varchar(100) NOT NULL,
	`task_key` varchar(100) NOT NULL,
	`type` varchar(50) NOT NULL,
	`task_class` varchar(255) NOT NULL,
	`task_data` json,
	`task_data_class` varchar(255),
	`run_at` timestamp NOT NULL,
	`recurrence` varchar(50),
	`picked` boolean NOT NULL DEFAULT false,
	`picked_by` varchar(100),
	`end_run` timestamp null default null,
	PRIMARY KEY (`task_key`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;
