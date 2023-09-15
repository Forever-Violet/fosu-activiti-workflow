package org.fosu.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.fosu.workflow.entities.ProcessConfig;
import org.fosu.workflow.mapper.ProcessConfigMapper;
import org.fosu.workflow.service.ProcessConfigService;
import org.fosu.workflow.utils.Result;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class ProcessConfigServiceImpl extends ServiceImpl<ProcessConfigMapper, ProcessConfig> implements ProcessConfigService {
    /** 通过流程key查询配置数据 */
    @Override
    public ProcessConfig getByProcessKey(String processKey) {
        QueryWrapper<ProcessConfig> query = new QueryWrapper<>();
        query.eq("process_key", processKey);
        return baseMapper.selectOne(query);
    }

    /** 通过流程key删除配置数据 */
    @Override
    public Result deleteByProcessKey(String processKey) {
        QueryWrapper<ProcessConfig> query = new QueryWrapper<>();
        query.eq("process_key", processKey);
        baseMapper.delete(query);
        return Result.ok();
    }

    /** 通过业务路由名查询流程定义配置信息（目的查询获取流程定义key) */
    @Override
    public ProcessConfig getByBusinessRoute(String businessRoute) {
        QueryWrapper<ProcessConfig> query = new QueryWrapper<>();
        query.eq("upper(business_route)", businessRoute.toUpperCase());
        List<ProcessConfig> list = baseMapper.selectList(query);
        if(CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }
}
