CREATE TABLE `snap_lock` (
  `key` varchar(100) NOT NULL,
  `method` varchar(255) NOT NULL,
  `lock_until` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `lock_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `lock_by` varchar(100) NOT NULL,
  PRIMARY KEY (`key`,`method`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `snap_task_audit`
(
	`id` INT NOT NULL AUTO_INCREMENT,
    `key` varchar(100) NOT NULL,
  	`method` varchar(255) NOT NULL,
  	`run_on` varchar(100) NOT NULL,
    `start_run` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
    `end_run` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
    `run_time_seconds` BIGINT NOT NULL,
    `task_error` json,
	PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `snap_scheduler` (
	`name` varchar(100) NOT NULL,
	`key` varchar(100) NOT NULL,
	`type` varchar(50) NOT NULL,
	`task_class` varchar(255) NOT NULL,
	`task_data` json,
	`task_data_class` varchar(255),
	`run_at` timestamp NOT NULL,
	`recurrence` varchar(50),
	`picked` boolean NOT NULL DEFAULT false,
	`picked_by` varchar(100),
	`end_run` timestamp null default null,
	PRIMARY KEY (`key`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;
