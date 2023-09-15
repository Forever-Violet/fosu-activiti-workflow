package org.fosu.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.fosu.workflow.entities.Leave;
import org.fosu.workflow.req.LeaveREQ;
import org.fosu.workflow.utils.Result;

public interface LeaveService extends IService<Leave> {
    /**
     * 新增请假数据
     * @param leave 请假业务实体类
     * @return 数据封装实体类
     */
    Result add(Leave leave);

    /**
     * 请假业务条件分页查询
     * @param req 请假查询列表条件
     * @return 数据封装实体类
     */
    Result listPage(LeaveREQ req);


    /**
     * 更新请假信息
     * @param leave 请假业务实体类
     * @return 数据封装实体类
     */
    Result update(Leave leave);
}
