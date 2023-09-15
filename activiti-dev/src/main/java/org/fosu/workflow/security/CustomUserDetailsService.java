package org.fosu.workflow.security;

import org.fosu.workflow.entities.SysUser;
import org.fosu.workflow.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private SysUserService sysUserService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = sysUserService.findByUsername(username);
        if(sysUser == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }

        Set<GrantedAuthority> authorities = new HashSet<>();
        // 添加用户拥有角色 ACTIVITI_USER，才可以使用 ProcessRuntime/TaskRuntime
        // 候选组 MANAGER_TEAM
        authorities.add(new SimpleGrantedAuthority("ROLE_ACTIVITI_USER"));
        authorities.add(new SimpleGrantedAuthority("GROUP_MANAGER_TEAM"));
        sysUser.setAuthorities(authorities);
        return sysUser;
    }

}
