package org.fosu.workflow.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.fosu.workflow.entities.Leave;
import org.fosu.workflow.mapper.LeaveMapper;
import org.fosu.workflow.req.LeaveREQ;
import org.fosu.workflow.service.BusinessStatusService;
import org.fosu.workflow.service.LeaveService;
import org.fosu.workflow.utils.Result;
import org.fosu.workflow.utils.UserUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class LeaveServiceImpl extends ServiceImpl<LeaveMapper, Leave> implements LeaveService {

    @Autowired
    private BusinessStatusService businessStatusService;

    /**
     * 新增请假数据
     */
    @Override
    @Transactional
    public Result add(Leave leave) {
        // 保存到请假业务表
        leave.setUsername(UserUtils.getUsername());
        int size = baseMapper.insert(leave);
        if (size == 1) {
            // 保存到业务流程关系中间表
            businessStatusService.add(leave.getId());
        }
        return Result.ok();
    }

    /**
     * 请假业务条件分页查询
     */
    @Override
    public Result listPage(LeaveREQ req) {
        if (StringUtils.isEmpty(req.getUsername())) {
            // 当前登录用户
            req.setUsername(UserUtils.getUsername());
        }
        IPage<Leave> page =
                baseMapper.getLeaveAndStatusList(req.getPage(), req);
        return Result.ok(page);
    }

    /**
     * 更新请假信息
     */
    @Override
    public Result update(Leave leave) {
        if (leave == null || StringUtils.isEmpty(leave.getId())) {
            return Result.error("数据不合法");
        }
        // 查询元数据
        Leave entity = baseMapper.selectById(leave.getId());
        // 拷贝新数据
        BeanUtils.copyProperties(leave, entity);
        entity.setUpdateDate(new Date());
        baseMapper.updateById(entity);
        return Result.ok();
    }
}
