package org.chzz.market.common.quartz;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.TriggerListener;

@Slf4j
public class MyTriggerListener implements TriggerListener {

    @Override
    public String getName() {
        return "MyTriggerListener";
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        // Trigger가 실행될 때 호출됩니다.
        log.info("Trigger '{}'이(가) 실행되었습니다.", trigger.getKey().toString());
    }

    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        // Trigger가 Job을 실행하지 않도록 결정할 때 호출됩니다.
        return false;
    }

    @Override
    public void triggerMisfired(Trigger trigger) {
        // Trigger가 Misfired(제시간에 실행되지 못했을 때) 발생 시 호출됩니다.
        log.warn("Trigger '{}'이(가) Misfired 되었습니다.", trigger.getKey().toString());
    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context,
                                CompletedExecutionInstruction triggerInstructionCode) {
        // Trigger가 완료된 후 호출됩니다.
        log.info("Trigger '{}'이(가) 완료되었습니다.", trigger.getKey().toString());
    }
}
