package org.fosu.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;
import org.fosu.workflow.entities.Loan;
import org.fosu.workflow.req.LoanREQ;

public interface LoanMapper extends BaseMapper<Loan> {

    /**
     * 条件分页查询借款数据
     * @param page 分页对象
     * @param req 查询借款列表条件
     * @return 列表
     */
    IPage<Loan> getLoanAndStatusList(IPage<Loan> page, @Param("req") LoanREQ req);
}
