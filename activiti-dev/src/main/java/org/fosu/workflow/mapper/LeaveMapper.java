package org.fosu.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.fosu.workflow.entities.Leave;
import org.fosu.workflow.req.LeaveREQ;


public interface LeaveMapper extends BaseMapper<Leave> {

    /**
     * 查询请假和业务状态表数据列表
     * @param page 分页实体类
     * @param req 请假查询列表条件
     * @return 请假分页数据
     */
    IPage<Leave> getLeaveAndStatusList(IPage page, @Param("req") LeaveREQ
            req);

}
