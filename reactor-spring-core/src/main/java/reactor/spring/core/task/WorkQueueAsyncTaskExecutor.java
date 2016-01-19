package reactor.spring.core.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.ProcessorWorkQueue;
import reactor.core.support.WaitStrategy;
import reactor.core.timer.Timer;
import reactor.core.timer.Timers;

import org.springframework.context.ApplicationEventPublisherAware;

/**
 * Implementation of an {@link org.springframework.core.task.AsyncTaskExecutor} that is backed by a Reactor {@link
 * ProcessorWorkQueue}.
 *
 * @author Jon Brisbin
 * @author Stephane Maldini
 * @since 1.1, 2.5
 */
public class WorkQueueAsyncTaskExecutor extends AbstractAsyncTaskExecutor implements ApplicationEventPublisherAware {

	private final Logger log = LoggerFactory.getLogger(WorkQueueAsyncTaskExecutor.class);

	private WaitStrategy                      waitStrategy;
	private ProcessorWorkQueue<Runnable> workQueue;

	public WorkQueueAsyncTaskExecutor() {
		this(Timers.globalOrNew());
	}

	public WorkQueueAsyncTaskExecutor(Timer timer) {
		super(timer);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (!isShared()) {
			this.workQueue = ProcessorWorkQueue.create(
			  getName(),
			  getBacklog(),
			  (null != waitStrategy ? waitStrategy : new WaitStrategy.Blocking())
			);
		} else {
			this.workQueue = ProcessorWorkQueue.share(
			  getName(),
			  getBacklog(),
			  (null != waitStrategy ? waitStrategy : new WaitStrategy.Blocking())
			);
		}
		if (isAutoStartup()) {
			start();
		}
	}

	/**
	 * Get the {@link reactor.core.support.WaitStrategy} this {@link reactor.core.support.rb.disruptor.RingBuffer} is using.
	 *
	 * @return the {@link reactor.core.support.WaitStrategy}
	 */
	public WaitStrategy getWaitStrategy() {
		return waitStrategy;
	}

	/**
	 * Set the {@link reactor.core.support.WaitStrategy} to use when creating the internal {@link
	 * reactor.core.support.rb.disruptor.RingBuffer}.
	 *
	 * @param waitStrategy
	 * 		the {@link reactor.core.support.WaitStrategy}
	 */
	public void setWaitStrategy(WaitStrategy waitStrategy) {
		this.waitStrategy = waitStrategy;
	}

	@Override
	protected ProcessorWorkQueue<Runnable> getProcessor() {
		return workQueue;
	}

}
