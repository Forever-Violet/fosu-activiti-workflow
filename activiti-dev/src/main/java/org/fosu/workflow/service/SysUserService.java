package org.fosu.workflow.service;

import org.fosu.workflow.entities.SysUser;

import java.util.List;

public interface SysUserService {
    /**
     * 通过用户名获取用户信息
     * @param username 用户名
     * @return 用户信息实体类
     */
    SysUser findByUsername(String username);

    /**
     * 随机获取10条用户信息
     * @return 用户信息实体类列表
     */
    List<SysUser> getTenUsers();
}
