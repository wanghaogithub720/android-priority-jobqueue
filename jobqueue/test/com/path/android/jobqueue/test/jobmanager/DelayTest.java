package com.path.android.jobqueue.test.jobmanager;


import com.path.android.jobqueue.JobHolder;
import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.test.jobs.DummyJob;
import com.path.android.jobqueue.test.jobs.PersistentDummyJob;
import org.fest.reflect.method.Invoker;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;

@RunWith(RobolectricTestRunner.class)
public class DelayTest extends JobManagerTestBase {
    @Test
    public void testDelay() throws Exception {
        testDelay(false);
        testDelay(true);
    }

    public void testDelay(boolean persist) throws Exception {
        JobManager jobManager = createJobManager();
        jobManager.stop();
        DummyJob delayedJob = persist ? new PersistentDummyJob() : new DummyJob();
        DummyJob nonDelayedJob = persist ? new PersistentDummyJob() : new DummyJob();
        long jobId = jobManager.addJob(10, 1000, delayedJob);
        long nonDelayedJobId = jobManager.addJob(0, 0, nonDelayedJob);

        Invoker<JobHolder> nextJobMethod = getNextJobMethod(jobManager);
        Invoker<Void> removeJobMethod = getRemoveJobMethod(jobManager);

        JobHolder receivedJob = nextJobMethod.invoke();
        MatcherAssert.assertThat("non-delayed job should be served", receivedJob, notNullValue());
        MatcherAssert.assertThat("non-delayed job should id should match",  receivedJob.getId(), equalTo(nonDelayedJobId));
        removeJobMethod.invoke(receivedJob);
        MatcherAssert.assertThat("delayed job should not be served",  nextJobMethod.invoke(), nullValue());
        MatcherAssert.assertThat("job count should still be 1",  jobManager.count(), equalTo(1));
        Thread.sleep(500);
        MatcherAssert.assertThat("delayed job should not be served",  nextJobMethod.invoke(), nullValue());
        MatcherAssert.assertThat("job count should still be 1",  jobManager.count(), equalTo(1));
        Thread.sleep(2000);
        MatcherAssert.assertThat("job count should still be 1",  jobManager.count(), equalTo(1));
        receivedJob = nextJobMethod.invoke();
        MatcherAssert.assertThat("now should be able to receive the delayed job.", receivedJob, notNullValue());
        if(receivedJob != null) {
            MatcherAssert.assertThat("received job should be the delayed job", receivedJob.getId(), equalTo(jobId));
        }
    }
}