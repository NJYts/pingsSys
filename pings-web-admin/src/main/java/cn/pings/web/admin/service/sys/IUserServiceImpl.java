package cn.pings.web.admin.service.sys;

import cn.pings.service.api.sys.entity.User;
import cn.pings.service.api.sys.service.UserService;
import com.alibaba.dubbo.config.annotation.Reference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 *********************************************************
 ** @desc  ： 用户管理
 ** @author  Pings
 ** @date    2019/1/25
 ** @version v1.0
 * *******************************************************
 */
@Component
@CacheConfig(cacheNames="user")
public class IUserServiceImpl implements UserService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Reference(version = "${sys.service.version}")
    private UserService userService;

    @Override
    public User getById(int id) {
        return this.userService.getById(id);
    }

    @Override
    @Cacheable
    public User getByUserName(String userName) {
        return this.userService.getByUserName(userName);
    }

    @Override
    public IPage<User> findPage(IPage<User> page, User entity) {
        return this.userService.findPage(page, entity);
    }

    @Override
    public User save(User user) {
        user =  this.userService.save(user);

        //**更新缓存
        this.redisTemplate.opsForValue().set("user::" + user.getUserName(), user);

        return user;
    }
}
