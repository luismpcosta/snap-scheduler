package io.opensw.scheduler.interfaces;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TaskEndpoints {

	@PostMapping("/task")
	public ResponseEntity<Void> createTask() {

		return ResponseEntity.ok().build();
	}

}
