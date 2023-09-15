package org.fosu.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.fosu.workflow.entities.BusinessStatus;
import org.fosu.workflow.enums.BusinessStatusEnum;
import org.fosu.workflow.mapper.BusinessStatusMapper;
import org.fosu.workflow.service.BusinessStatusService;
import org.fosu.workflow.utils.Result;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class BusinessStatusServiceImpl extends ServiceImpl<BusinessStatusMapper, BusinessStatus> implements BusinessStatusService {
    /** 新增数据，状态：待处理 */
    @Override
    public int add(String businessKey) {
        BusinessStatus businessStatus = new BusinessStatus();
        businessStatus.setBusinessKey(businessKey);
        businessStatus.setStatus(BusinessStatusEnum.WAIT.getCode());
        return baseMapper.insert(businessStatus);
    }

    /** 根据业务id更新业务状态 */
    @Override
    public Result updateState(String businessKey, BusinessStatusEnum statusEnum, String procInstId) {
        BusinessStatus businessStatus = baseMapper.selectById(businessKey);
        // 状态
        businessStatus.setStatus(statusEnum.getCode());
        businessStatus.setUpdateDate(new Date());
        // 值为null，不会更新此字段
        if (procInstId != null) {
            businessStatus.setProcessInstanceId(procInstId);
        }
        baseMapper.updateById(businessStatus);
        return Result.ok();
    }

    /** 根据业务id更新业务状态 */
    @Override
    public Result updateState(String businessKey, BusinessStatusEnum statusEnum) {
        return updateState(businessKey, statusEnum, null);
    }
}
