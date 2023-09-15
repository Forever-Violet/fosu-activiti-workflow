package org.fosu.workflow.cmd;

import org.activiti.engine.impl.cmd.NeedsActiveTaskCmd;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntityManager;
import org.fosu.workflow.utils.UserUtils;

/** 自定义 command 模型实现删除任务数据 */
public class DeleteTaskCommand extends NeedsActiveTaskCmd<String> {
    public DeleteTaskCommand(String taskId) {
        super(taskId);
    }

    @Override
    protected String execute(CommandContext commandContext, TaskEntity task) {
        TaskEntityManager taskEntityManager = commandContext.getTaskEntityManager();
        // 删除当前任务，不会把执行表中的 is_active_更新为0，会将任务数据更新到历史任务实例表中
        taskEntityManager.deleteTask(task, "【"+task.getName()+"】任务被" + UserUtils.getUsername() + "】驳回", false, false);
        return null;
    }
}