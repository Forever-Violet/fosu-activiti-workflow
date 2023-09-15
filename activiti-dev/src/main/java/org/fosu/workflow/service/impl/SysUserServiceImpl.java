package org.fosu.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.fosu.workflow.entities.SysUser;
import org.fosu.workflow.mapper.SysUserMapper;
import org.fosu.workflow.service.SysUserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service  // 不要少
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
    /** 通过用户名获取用户信息 */
    @Override
    public SysUser findByUsername(String username) {
        if (StringUtils.isEmpty(username)) {
            return null;
        }

        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);

        // baseMapper对应的就是SysUserMapper
        return baseMapper.selectOne(wrapper);
    }

    /** 随机获取10条用户信息 */
    @Override
    public List<SysUser> getTenUsers() {
        // 总记录数
        int count = baseMapper.selectCount(null);
        // 随机起始位置
        int randomCount = count > 10 ? (int)(Math.random() * (count - 10)) : 0;

        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("id");
        wrapper.last("limit " + String.valueOf(randomCount) + ", 10");
        return baseMapper.selectList(wrapper);
    }
}
