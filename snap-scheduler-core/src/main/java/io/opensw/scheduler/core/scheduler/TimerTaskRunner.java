package io.opensw.scheduler.core.scheduler;

import java.time.Instant;
import java.util.TimerTask;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

import io.opensw.scheduler.core.events.obj.TaskRunEvent;
import io.opensw.scheduler.core.exceptions.BeanDefinitionException;
import io.opensw.scheduler.core.scheduler.task.Task;
import io.opensw.scheduler.core.scheduler.task.TaskData;
import io.opensw.scheduler.core.scheduler.task.TaskDataExecutor;
import io.opensw.scheduler.core.scheduler.task.TaskExecutor;
import io.opensw.scheduler.core.utils.ServerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class TimerTaskRunner extends TimerTask {

	private final Task task;

	private final ApplicationContext context;

	private final ApplicationEventPublisher applicationEventPublisher;

	@Override
	@SuppressWarnings( "unchecked" )
	public void run() {
		final Instant start = Instant.now();
		Exception exception = null;
		try {
			TaskExecutor taskExecutor = this.loadInstance( task.getClazz() );
			// if task executor bean exists
			if ( taskExecutor != null && TaskDataExecutor.class.isAssignableFrom( task.getClazz() ) ) {
				( (TaskDataExecutor< TaskData >) taskExecutor ).execute( task.getData() );
			}
			else if ( taskExecutor != null ) {
				taskExecutor.execute();
			}
			else {
				exception = new BeanDefinitionException( "(SnapTaskHandler.handleTask) TaskExecutor is null." );
				log.error( "(SnapTaskHandler.handleTask) TaskExecutor is null." );
			}
		}
		catch ( Exception e ) {
			exception = e;
			log.error(
					"(TimerTaskRunner.run) Error occur on run timer task with key {}. Error: {}", task.getKey(),
					e.getMessage()
			);
		}
		finally {
			applicationEventPublisher
					.publishEvent(
							TaskRunEvent.create( task, ServerUtils.loadServerName(), start, Instant.now(), exception )
					);
		}
	}

	private TaskExecutor loadInstance( Class< ? > clazz ) {
		try {
			return (TaskExecutor) context.getBean( Class.forName( clazz.getName() ) );
		}
		catch ( Exception e ) {
			log.error(
					"(SnapTaskHandler.loadInstance) Do not have instance in spring context of class {}", clazz.getName()
			);
		}

		return registerBean( clazz );
	}

	private TaskExecutor registerBean( Class< ? > clazz ) {
		log.debug( "(SnapTaskHandler.registerBean) Start register bean." );
		try {
			final String beanName = StringUtils.uncapitalize( clazz.getSimpleName() );
			ConfigurableApplicationContext cnt = (ConfigurableApplicationContext) context;
			DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) cnt.getBeanFactory();

			BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition( clazz );

			beanFactory.registerBeanDefinition( beanName, beanDefinitionBuilder.getBeanDefinition() );

			return (TaskExecutor) context.getBean( Class.forName( clazz.getName() ) );
		}
		catch ( Exception e ) {
			log.error( "(SnapTaskHandler.registerBean) Erro on register bean definition. Error: ", e.getMessage() );
		}

		return null;
	}
}
