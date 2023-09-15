package org.fosu.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.fosu.workflow.entities.Loan;
import org.fosu.workflow.req.LoanREQ;
import org.fosu.workflow.utils.Result;

public interface LoanService extends IService<Loan> {
    /**
     * 添加借款业务数据
     * @param loan 借款业务实体类
     * @return 统一数据封装实体类
     */
    Result add(Loan loan);

    /**
     * 条件分页查询借款数据
     * @param req 查询借款列表条件
     * @return 统一数据封装实体类
     */
    Result listPage(LoanREQ req);

    /**
     * 更新借款详情信息
     * @param loan 借款实体类
     * @return 统一数据封装实体类
     */
    Result update(Loan loan);

}
