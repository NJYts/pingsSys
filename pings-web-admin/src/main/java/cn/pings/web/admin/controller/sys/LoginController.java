package cn.pings.web.admin.controller.sys;

import cn.pings.service.api.common.exception.UnauthorizedException;
import cn.pings.service.api.common.util.ApiResponse;
import cn.pings.service.api.sys.entity.Role;
import cn.pings.service.api.sys.entity.User;
import cn.pings.service.api.sys.service.UserService;
import cn.pings.web.admin.controller.AbstractBaseController;
import cn.pings.web.admin.util.JwtUtil;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 *********************************************************
 ** @desc  ： 登陆管理
 ** @author  Pings
 ** @date    2019/1/22
 ** @version v1.0
 * *******************************************************
 */
@RestController
@RequestMapping("/api/login")
public class LoginController extends AbstractBaseController {

    @Autowired
    private UserService iUserService;
    @Value("${sys.jwt.expireTime}")
    private long expireTime;

    /**
     *********************************************************
     ** @desc ： 测试
     ** @author Pings
     ** @date   2019/1/22
     ** @param  id  用户编号
     ** @return ApiResponse
     * *******************************************************
     */
    @ApiOperation(value="测试", notes="根据用户编号获取用户")
    @GetMapping(value = "/test/{id}")
    @RequiresAuthentication
    public ApiResponse test(@PathVariable("id") int id){
        return new ApiResponse(200, this.iUserService.getById(id));
    }

    /**
     *********************************************************
     ** @desc ： 登录
     ** @author Pings
     ** @date   2019/1/22
     ** @param  userName  用户名称
     ** @param  password  用户密码
     ** @return ApiResponse
     * *******************************************************
     */
    @ApiOperation(value="登录", notes="验证用户名和密码")
    @PostMapping(value = "/account")
    public ApiResponse account(String userName, String password){
        if(StringUtils.isBlank(userName) || StringUtils.isBlank(password))
            throw new UnauthorizedException("用户名/密码不能为空");

        //**md5加密
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        User user = this.iUserService.getByUserName(userName);
        if(user != null && user.getPassword().equals(password)) {
            if(expireTime > 0)
                response.setHeader("Authorization", JwtUtil.sign(userName, password, expireTime * 60 * 1000));
            else
                response.setHeader("Authorization", JwtUtil.sign(userName, password));
            response.setHeader("Access-Control-Expose-Headers", "Authorization");

            //**用户角色
            Set<String> roles = user.getRoles().stream().map(Role::getCode).collect(toSet());
            return new ApiResponse(200, "登录成功", roles);
        } else
            return new ApiResponse(500, "用户名/密码错误");
    }
}
