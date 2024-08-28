package org.chzz.market.common.quartz;

import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

@Slf4j
public class MyJobListener implements JobListener {

    @Override
    public String getName() {
        return "MyJobListener";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        // Job이 실행되기 직전에 호출됩니다.
        String jobName = context.getJobDetail().getKey().toString();
        log.info("Job '{}' 실행을 준비 중입니다.", jobName);
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        // Job이 실행되지 않도록 거부되었을 때 호출됩니다.
        String jobName = context.getJobDetail().getKey().toString();
        log.warn("Job '{}' 실행이 거부되었습니다.", jobName);
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        // Job이 실행된 후 호출됩니다.
        String jobName = context.getJobDetail().getKey().toString();
        if (jobException != null) {
            log.error("Job '{}' 실행 중 오류가 발생했습니다: {} (실행 시간: {} seconds)",
                    jobName, jobException.getMessage(), context.getJobRunTime() / 1000.0, jobException);
        } else {
            log.info("Job '{}'이(가) 성공적으로 실행되었습니다. (실행 시간: {} seconds)",
                    jobName, context.getJobRunTime() / 1000.0);
        }
    }
}
