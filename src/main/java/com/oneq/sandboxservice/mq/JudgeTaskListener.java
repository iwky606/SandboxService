package com.oneq.sandboxservice.mq;

import com.oneq.sandboxservice.model.dto.JudgeTask;
import com.oneq.sandboxservice.service.ManageTask;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JudgeTaskListener {
    @RabbitListener(queues = "test.queue")
    public void testQueue(String msg) {
        log.info(msg);
    }

    @Resource
    ManageTask manageTask;

    @RabbitListener(queues = "judge.queue")
    public void judgeListener(JudgeTask judgeTask) {
        manageTask.submitTask(judgeTask);
    }
}
