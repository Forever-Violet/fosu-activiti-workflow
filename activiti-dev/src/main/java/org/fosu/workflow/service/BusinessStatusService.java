package org.fosu.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.fosu.workflow.entities.BusinessStatus;
import org.fosu.workflow.enums.BusinessStatusEnum;
import org.fosu.workflow.utils.Result;

public interface BusinessStatusService extends IService<BusinessStatus> {
    /**
     * 新增数据，状态：待处理
     * @param businessKey 业务id
     * @return 受影响行数
     */
    int add(String businessKey);

    /**
     * 根据业务id更新业务状态
     * @param businessKey 业务id
     * @param statusEnum 业务状态
     * @param procInstId 流程实例id
     * @return 封装数据实体类
     */
    Result updateState(String businessKey, BusinessStatusEnum statusEnum, String procInstId);

    /**
     * 根据业务id更新业务状态
     * @param businessKey 业务id
     * @param statusEnum 业务状态
     * @return 封装数据实体类
     */
    Result updateState(String businessKey, BusinessStatusEnum statusEnum);

}
