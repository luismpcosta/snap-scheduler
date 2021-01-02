package io.opensw.scheduler.core.common;

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
