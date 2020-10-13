package com.flycms.web.front;
import com.flycms.core.utils.Base64HelperUtils;
import com.flycms.core.utils.CookieUtils;
import com.flycms.core.utils.DateUtils;
import com.flycms.core.utils.StringHelperUtils;
import com.flycms.constant.Const;
import com.flycms.core.base.BaseController;
import com.flycms.core.entity.DataVo;
import com.flycms.module.question.service.ImagesService;
import com.flycms.module.user.model.User;
import com.flycms.module.user.service.UserService;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.List;

/**
 * @author cs
 * @date 2020/10/11
 */
@Controller
public class UserController extends BaseController {

    @Resource
    protected UserService userService;

    @Resource
    private ImagesService imagesService;

    /**
     * 用户注册
     * @param invite
     * @param modelMap
     * @return
     */
    @GetMapping(value = "/reg")
    public String userReg(@RequestParam(value = "invite", required = false) String invite,
                          ModelMap modelMap,
                          HttpServletRequest request){
        if(invite==null){
            invite=CookieUtils.getCookie(request,"invite");
        }
        modelMap.addAttribute("invite",invite);
        return theme.getPcTemplate("user/reg");
    }

    /**
     * 用户提交手机号码申请获取验证码
     *
     * @param username
     *        提交的手机号码
     * @param captcha
     *        验证码
     * @return
     * @throws Exception
     */
    @ResponseBody
    @PostMapping(value = "/ucenter/mobilecode")
    public DataVo getAddUserMobileCode(@RequestParam(value = "username", required = false) String username,
                                       @RequestParam(value = "captcha", required = false) String captcha) throws Exception {
        DataVo data;
        String kaptcha = getObjString(session.getAttribute("kaptcha"));
        // 校验验证码
        data = checkVerifyCode(captcha,kaptcha);
        if (data != null) {
            return data;
        }else {
            session.removeAttribute(Const.KAPTCHA_SESSION_KEY);
        }
        if(!StringHelperUtils.checkPhoneNumber(username)) {
            return DataVo.failure("手机号码错误！");
        }
        data = userService.regMobileCode(username);
        return data;
    }

    /**
     * 添加新用户
     *
     * @param username
     * @param password
     * @return
     */
    @ResponseBody
    @PostMapping(value = "/ucenter/reg_user")
    public DataVo addUser(@RequestParam(value = "username", required = false) String username,
                          @RequestParam(value = "password", required = false) String password,
                          @RequestParam(value = "password2", required = false) String password2,
                          @RequestParam(value = "invite", required = false) String invite,
                          @RequestParam(value = "captcha", required = false) String captcha,
                          HttpServletRequest request, HttpServletResponse response) {
        DataVo data;
        try {
            username=username.trim();
            password=password.trim();
            password2=password2.trim();
            captcha=captcha.trim();
            String kaptcha = (String) session.getAttribute(Const.KAPTCHA_SESSION_KEY);
            // 校验验证码
            if (captcha == null && "".equals(captcha)) {
                return DataVo.failure("验证码不能为空");
            }
            captcha=captcha.toLowerCase();
            if(!captcha.equals(kaptcha)){
                return DataVo.failure("验证码错误");
            }

            if (StringUtils.isBlank(username)) {
                return DataVo.failure("用户名不能为空");
            }
            if (StringUtils.isBlank(password)) {
                return DataVo.failure("密码不能为空");
            }
            if (password.length() < 6) {
                return DataVo.failure("密码不能小于6位");
            }
            if (password.length() > 16) {
                return DataVo.failure("密码不能大于16位");
            }
            if (!password.equals(password2)) {
                return DataVo.failure("密码两次输入不一致");
            }
            userService.addUserReg(3,username, password,null,invite,request,response);
            return DataVo.success("操作成功");
        } catch (Exception e) {
            data = DataVo.failure(e.getMessage());
        }
        return data;
    }

    @ResponseBody
    @PostMapping(value = "/ucenter/addMobileUser")
    public DataVo addMobileUser(@RequestParam(value = "phoneNumber", required = false) String phoneNumber,
                                @RequestParam(value = "mobilecode", required = false) String mobilecode,
                                @RequestParam(value = "password", required = false) String password,
                                @RequestParam(value = "password2", required = false) String password2,
                                @RequestParam(value = "invite", required = false) String invite,
                                HttpServletRequest request, HttpServletResponse response) throws Exception{
        if (mobilecode == null) {
            return DataVo.failure("验证码不能为空");
        }
        if (password == null) {
            return DataVo.failure("密码不能为空");
        }
        if(!password.equals(password2)){
            return DataVo.failure("两次密码输入不一样");
        }
        return userService.addUserReg(1,phoneNumber, password,mobilecode,invite,request,response);
    }

    /**
     * 用户登录页面
     * @param redirectUrl
     * @param modelMap
     * @return
     */
    @GetMapping(value = "/login")
    public String userLogin(@RequestParam(value = "redirectUrl",required = false) String redirectUrl,
                            ModelMap modelMap,
                            HttpServletRequest request){
        if(getUser(request) != null){
            return "redirect:/index";
        }
        modelMap.addAttribute("redirectUrl",redirectUrl);
        return theme.getPcTemplate("user/login");
    }

    /**
     * 登录处理
     * @param username
     * @param password
     * @param rememberMe
     * @param redirectUrl
     * @param captcha
     * @return
     */
    @ResponseBody
    @PostMapping(value = "/ucenter/login_act")
    public DataVo userLogin(
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "rememberMe", required = false) String rememberMe,
            @RequestParam(value = "redirectUrl",required = false) String redirectUrl,
            @RequestParam(value = "captcha", required = false) String captcha,
            HttpServletRequest request, HttpServletResponse response) {
        try {
            String kaptcha = getObjString(session.getAttribute("kaptcha"));
            if (StringUtils.isBlank(username)) {
                return DataVo.failure("用户名不能为空");
            }
            if (StringUtils.isBlank(password)) {
                return DataVo.failure("密码不能为空");
            } else if (password.length() < 6 || password.length() > 30) {
                return DataVo.failure("密码最少6个字符，最多30个字符");
            }
            //校验验证码
            DataVo verify = checkVerifyCode(captcha,kaptcha);
            if(null != verify){
                return verify;
            }
            boolean keepLogin = "1".equals(rememberMe);
            User entity = userService.userLogin(username,password,keepLogin,request,response);
            if(entity==null){
                return DataVo.failure("帐号或密码错误。");
            }else{
                session.removeAttribute(Const.KAPTCHA_SESSION_KEY);
                if (StringUtils.isNotEmpty(redirectUrl)){
                    return DataVo.jump("操作成功", redirectUrl);
                }
                return DataVo.jump("操作成功", "/");
            }
        } catch (Exception e) {
            return DataVo.failure("帐号或密码错误。");
        }
    }

    private String getObjString(Object obj){
        return obj == null ? "" : obj.toString();
    }

    /**
     * 校验验证码
     * @param captcha
     * @param kaptcha
     * @return
     */
    private DataVo checkVerifyCode(String captcha, String kaptcha){
        if (captcha != null) {
            if (!captcha.equalsIgnoreCase(kaptcha)) {
                return DataVo.failure("验证码错误");
            }
        }else{
            return DataVo.failure("验证码不能为空");
        }
        return null;
    }

    /**
     * 页面ajax登录处理
     *
     * @param username
     * @param password
     * @return
     */
    @ResponseBody
    @PostMapping(value = "/ucenter/ajaxlogin")
    public DataVo userAjaxLogin( @RequestParam(value = "username", required = false) String username,
                                 @RequestParam(value = "password", required = false) String password,
                                 @RequestParam(value = "rememberMe", required = false) String rememberMe,
                                 @RequestParam(value = "code", required = false) String code,
                                 HttpServletRequest request, HttpServletResponse response) {
        DataVo data;
        try {
            if (StringUtils.isBlank(username)) {
                return DataVo.failure("用户名不能为空");
            }
            if (StringUtils.isBlank(password)) {
                return DataVo.failure("密码不能为空");
            } else if (password.length() < 6 || password.length() > 30) {
                return DataVo.failure("密码最少6个字符，最多30个字符");
            }
            // 校验验证码
            DataVo verify = checkVerifyCode(code,getObjString(session.getAttribute("kaptcha")));
            if(null != verify){
                return verify;
            }
            boolean keepLogin = "1".equals(rememberMe);
            User entity = userService.userLogin(username,password,keepLogin,request,response);
            if(entity==null){
                return DataVo.failure("帐号或密码错误。");
            }else{
                session.removeAttribute(Const.KAPTCHA_SESSION_KEY);
                return DataVo.jump("操作成功", "/");
            }
        } catch (Exception e) {
            data = DataVo.failure(e.getMessage());
        }
        return data;
    }

    /**
     * 修改用户基本信息
     * @param modelMap
     * @return
     */
    @GetMapping(value = "/ucenter/account")
    public String userAccount(ModelMap modelMap,HttpServletRequest request){
        modelMap.addAttribute("user", getUser(request));
        return theme.getPcTemplate("user/account");
    }


    /**
     * 更新用户基本信息
     * @param user
     * @param result
     * @return
     */
    @PostMapping("/ucenter/account_update")
    @ResponseBody
    public DataVo updateUserAccount(@Valid User user, BindingResult result,
                                    HttpServletRequest request){
        DataVo data;
        try {
            if (result.hasErrors()) {
                List<ObjectError> list = result.getAllErrors();
                for (ObjectError error : list) {
                    return DataVo.failure(error.getDefaultMessage());
                }
                return null;
            }
            if(!StringUtils.isNumeric(user.getUserId().toString())){
                return DataVo.failure("请勿非法传递数据！");
            }
            if(!user.getUserId().equals(getUser(request).getUserId())){
                return DataVo.failure("请勿非法传递数据！");
            }
            if(!getUser(request).getUserId().equals(user.getUserId())){
                return DataVo.failure("只能修改属于自己的基本信息！");
            }
            if(StringUtils.isBlank(user.getNickName())){
                return DataVo.failure("昵称不能为空！");
            }
            if(user.getBirthday()==null || "".equals(user.getBirthday())){
                return DataVo.failure("请选择您的生日日期！");
            }
            if(DateUtils.isValidDate(user.getBirthday().toString())){
                return DataVo.failure("生日日期格式错误！");
            }
            if(user.getProvince()==0){
                return DataVo.failure("省份未选择！");
            }
            if(user.getCity()==0){
                return DataVo.failure("地区为选择！");
            }
            data = userService.updateUserAccount(user);
        } catch (Exception e) {
            data = DataVo.failure(e.getMessage());
        }
        return data;
    }

    /**
     * 安全手机账号设置
     * @param modelMap
     * @return
     */
    @GetMapping(value = "/ucenter/safe_mobile")
    public String safeMobile(ModelMap modelMap,HttpServletRequest request){
        modelMap.addAttribute("user", getUser(request));
        return theme.getPcTemplate("user/safe_mobile");
    }

    /**
     * 用户提交手机号码申请获取验证码
     *
     * @param mobile
     *        提交的手机号码
     * @return
     * @throws Exception
     */
    @ResponseBody
    @PostMapping(value = "/ucenter/safemobilecode")
    public DataVo safeMobileCode(@RequestParam(value = "mobile", required = false) String mobile,
                                 @RequestParam(value = "captcha", required = false) String captcha,
                                 HttpServletRequest request) throws Exception {
        DataVo data;
        String kaptcha = getObjString(session.getAttribute("kaptcha"));
        // 校验验证码
        if (captcha != null) {
            if (!captcha.equalsIgnoreCase(kaptcha)) {
                return DataVo.failure("验证码错误");
            }
            session.removeAttribute(Const.KAPTCHA_SESSION_KEY);
        }else{
            return DataVo.failure("验证码不能为空");
        }
        if(!StringHelperUtils.checkPhoneNumber(mobile)) {
            return DataVo.failure("手机号码错误！");
        }
        data = userService.safeMobileCode(getUser(request).getUserId(),mobile);
        return data;
    }

    @ResponseBody
    @PostMapping(value = "/ucenter/safe_mobile_update")
    public DataVo safeMobile(
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "mobile", required = false) String mobile,
            @RequestParam(value = "code", required = false) String code,
            HttpServletRequest request) {
        DataVo data;
        try {
            if (StringUtils.isBlank(password)) {
                return DataVo.failure("密码不能为空");
            } else if (password.length() < 6 || password.length() >= 32) {
                return DataVo.failure("密码最少6个字符，最多32个字符");
            }
            if(!StringHelperUtils.checkPhoneNumber(mobile)) {
                return DataVo.failure("手机号码错误！");
            }
            if (code == null || "".equals(code)) {
                return DataVo.failure("验证码不能为空");
            }
            data=userService.updateSafeMobile(getUser(request).getUserId(),password, mobile, code);
        } catch (Exception e) {
            data = DataVo.failure(e.getMessage());
        }
        return data;
    }

    /**
     * 设置安全邮箱账号
     * @param modelMap
     * @return
     */
    @GetMapping(value = "/ucenter/safe_email")
    public String safeEmail(ModelMap modelMap,HttpServletRequest request){
        modelMap.addAttribute("user", getUser(request));
        return theme.getPcTemplate("user/safe_email");
    }

    @ResponseBody
    @PostMapping(value = "/ucenter/safe_email_code")
    public DataVo userAjaxMailCaptcha(@RequestParam(value = "userEmail", required = false) String userEmail,
                                      HttpServletRequest request) {
        DataVo data;
        try {
            if (!StringHelperUtils.emailFormat(userEmail)) {
                return DataVo.failure("邮箱格式错误！");
            }
            return userService.safeEmailVerify(userEmail,getUser(request).getUserId());
        } catch (Exception e) {
            data = DataVo.failure(e.getMessage());
        }
        return data;
    }

    @ResponseBody
    @PostMapping(value = "/ucenter/safe_email_update")
    public DataVo safeEmail(
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "userEmail", required = false) String userEmail,
            @RequestParam(value = "code", required = false) String code,
            HttpServletRequest request) {
        DataVo data;
        try {
            // 校验验证码
            if (code == null || "".equals(code)) {
                return DataVo.failure("验证码不能为空");
            }
            if (StringUtils.isBlank(password)) {
                return DataVo.failure("新密码不能为空");
            } else if (password.length() < 6 || password.length() >= 32) {
                return DataVo.failure("密码最少6个字符，最多32个字符");
            }
            if(!StringHelperUtils.emailFormat(userEmail)) {
                return DataVo.failure("邮箱地址错误！");
            }
            data=userService.updateSafeEmail(getUser(request).getUserId(), password, userEmail,code);
        } catch (Exception e) {
            data = DataVo.failure(e.getMessage());
        }
        return data;
    }

    /**
     * 我的积分
     * @param modelMap
     * @return
     */
    @GetMapping(value = "/ucenter/integral")
    public String userIntegral(ModelMap modelMap,HttpServletRequest request){
        modelMap.addAttribute("user", getUser(request));
        return theme.getPcTemplate("user/integral");
    }

    /**
     * 我的退款申请
     * @param modelMap
     * @return
     */
    @GetMapping(value = "/ucenter/refunds")
    public String userRefunds(ModelMap modelMap,HttpServletRequest request){
        modelMap.addAttribute("user", getUser(request));
        return theme.getPcTemplate("user/refunds");
    }

    /**
     * 我的网站建议
     * @param modelMap
     * @return
     */
    @GetMapping(value = "/ucenter/complain")
    public String userComplain(ModelMap modelMap,HttpServletRequest request){
        modelMap.addAttribute("user", getUser(request));
        return theme.getPcTemplate("user/complain");
    }

    /**
     * 我的产品收藏
     * @param modelMap
     * @return
     */
    @GetMapping(value = "/ucenter/favorite")
    public String userFavorite(ModelMap modelMap,HttpServletRequest request){
        modelMap.addAttribute("user", getUser(request));
        return theme.getPcTemplate("user/favorite");
    }

    /**
     * 线上推广列表
     * @param p
     * @param modelMap
     * @return
     */
    @GetMapping(value = "/ucenter/invite")
    public String userInvite(@RequestParam(value = "p", defaultValue = "1") int p,
                             ModelMap modelMap,
                             HttpServletRequest request){
        modelMap.addAttribute("user", getUser(request));
        modelMap.addAttribute("p", p);
        return theme.getPcTemplate("user/invite");
    }

    /**
     * 我的账户余额
     * @param p
     * @param modelMap
     * @return
     */
    @GetMapping(value = "/ucenter/account_log")
    public String userAccount_log(@RequestParam(value = "p", defaultValue = "1") int p,
                                  ModelMap modelMap,
                                  HttpServletRequest request){
        modelMap.addAttribute("p", p);
        modelMap.addAttribute("user", getUser(request));
        return theme.getPcTemplate("user/account_log");
    }

    /**
     * 我的账户余额体现申请
     * @param modelMap
     * @return
     */
    @GetMapping(value = "/ucenter/withdraw")
    public String userWithdraw(ModelMap modelMap,HttpServletRequest request){
        modelMap.addAttribute("user", getUser(request));
        return theme.getPcTemplate("user/withdraw");
    }

    /**
     * 我的在线充值
     * @param modelMap
     * @return
     */
    @GetMapping(value = "/ucenter/online_recharge")
    public String userOnlineRecharge(ModelMap modelMap,HttpServletRequest request){
        modelMap.addAttribute("user", getUser(request));
        return theme.getPcTemplate("user/online_recharge");
    }

    /**
     * 我的收货地址管理
     * @param modelMap
     * @return
     */
    @GetMapping(value = "/ucenter/address")
    public String userAddress(ModelMap modelMap,HttpServletRequest request){
        modelMap.addAttribute("user", getUser(request));
        return theme.getPcTemplate("user/address");
    }

    /**
     * 我的个人信息
     * @param modelMap
     * @return
     */
    @GetMapping(value = "/ucenter/info")
    public String userInfo(ModelMap modelMap,HttpServletRequest request){
        modelMap.addAttribute("user", getUser(request));
        return theme.getPcTemplate("user/info");
    }

    /**
     * 我的密码修改
     * @param modelMap
     * @return
     */
    @GetMapping(value = "/ucenter/password")
    public String userPassword(ModelMap modelMap,HttpServletRequest request){
        modelMap.addAttribute("user", getUser(request));
        return theme.getPcTemplate("user/password");
    }

    @ResponseBody
    @PostMapping(value = "/ucenter/password_update")
    public DataVo updatePassword(
            @RequestParam(value = "old_password", required = false) String old_password,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "password_confirmation", required = false) String password_confirmation,
            @RequestParam(value = "captcha", required = false) String captcha,
            HttpServletRequest request) {
        String kaptcha = getObjString(session.getAttribute("kaptcha"));
        DataVo data ;
        // 校验验证码
        if (captcha == null || "".equals(captcha)) {
            return DataVo.failure("验证码不能为空");
        }
        captcha = captcha.toLowerCase();
        if(!captcha.equals(kaptcha)){
            return DataVo.failure("验证码错误");
        }
        try {
            if (StringUtils.isBlank(old_password)) {
                return DataVo.failure("原来密码不能为空");
            } else if (old_password.length() < 6 || old_password.length() >= 32) {
                return DataVo.failure("密码最少6个字符，最多32个字符");
            }
            if (StringUtils.isBlank(password)) {
                return DataVo.failure("新密码不能为空");
            } else if (password.length() < 6 || password.length() >= 32) {
                return DataVo.failure("密码最少6个字符，最多32个字符");
            }
            if (!password.equals(password_confirmation)) {
                return DataVo.failure("两次密码必须一样");
            }
            data=userService.updatePassword(getUser(request).getUserId(), old_password, password);
            session.removeAttribute(Const.KAPTCHA_SESSION_KEY);
        } catch (Exception e) {
            data = DataVo.failure(e.getMessage());
        }
        return data;
    }

    /**
     * 保存头像
     *
     * @param avatar
     * @return
     * @throws IOException
     * @throws ParseException
     */
    @ResponseBody
    @PostMapping("/ucenter/avatar.json")
    public DataVo changeAvatar(String avatar,HttpServletRequest request) throws IOException, ParseException {
        DataVo data;
        if (StringUtils.isEmpty(avatar)) {
            return DataVo.failure("头像不能为空");
        }
        byte[] bytes;
        try {
            String _avatar = avatar.substring(avatar.indexOf(",") + 1, avatar.length());
            bytes = Base64HelperUtils.decode(_avatar);
        } catch (Exception e) {
            e.printStackTrace();
            return DataVo.failure("头像格式不正确");
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        BufferedImage bufferedImage = ImageIO.read(bais);
        data =imagesService.saveAvatarDataFile(getUser(request), bufferedImage);
        bais.close();
        return data;
    }

    /**
     * 处理关注用户信息
     * @param id
     * @return
     */
    @ResponseBody
    @PostMapping(value = "/ucenter/user/follow")
    public DataVo userFollow(@RequestParam(value = "id", required = false) String id,
                             HttpServletRequest request) {
        DataVo data;
        try {
            if (!NumberUtils.isNumber(id)) {
                return DataVo.failure("问题参数错误");
            }
            if(getUser(request)==null){
                return DataVo.failure("请登陆后关注");
            }
            if((getUser(request).getUserId().equals(Long.parseLong(id)))){
                return DataVo.failure("无法关注自己！");
            }
            data=userService.addUserFans(Long.parseLong(id),getUser(request).getUserId());
        } catch (Exception e) {
            data = DataVo.failure(e.getMessage());
        }
        return data;
    }

    /**
     *
     * 前台JS读取用户登录状态判断
     *
     */
    @ResponseBody
    @GetMapping(value = "/user/status.json")
    public void userSession(HttpServletRequest request,HttpServletResponse response) throws Exception {
        PrintWriter out;
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-type", "text/html;charset=utf-8");
        response.setContentType("text/javascript;charset=utf-8");
        /*response.setHeader("Cache-Control", "no-cache");*/
        out = response.getWriter();
        if(null == out){
            return;
        }
        //out.flush();//清空缓存
        if(getUser(request)!=null){
            out.println("var userid='"+getUser(request).getUserId()+"';");
            out.println("var nickname = '"+getUser(request).getNickName()+"';");
            String signature="";
            if(getUser(request).getSignature()!=null){
                signature=getUser(request).getSignature();
            }else{
                signature="这个家伙很懒，啥也没留下！";
            }
            out.println("var signature = '"+signature+"';");
            String avatar=getUser(request).getAvatar();
            if(avatar==null){
                avatar="/assets/skin/pc_theme/defalut/images/avatar/default.jpg";
            }
            out.println("var smallAvatar = '"+avatar+"';");
        }else{
            out.println("var userid='';");
            out.println("var nickname = '';");
            out.println("var signature = '';");
            out.println("var smallAvatar = '/assets/skin/pc_theme/defalut/images/avatar/default.jpg';");
        }
        out.close();
    }

    /**
     * 用户退出登录
     *
     */
    @GetMapping(value = "/logout")
    public String logout(HttpServletRequest request,HttpServletResponse response) {
        //清除cookie、session
        userService.signOutLogin(request,response);
        return "redirect:/index-hot";
    }

    /**
     * 用户选择找回密码方式
     *
     */
    @GetMapping(value = "/forget_password")
    public String forgetPassword(ModelMap modelMap,HttpServletRequest request) {
        if(getUser(request)!=null){
            modelMap.addAttribute("user", getUser(request));
        }
        return theme.getPcTemplate("user/forget_password");
    }

    /**
     * 用户选择找回密码方式
     *
     */
    @GetMapping(value = "/forget_password/mobile")
    public String forgetPasswordMobile(ModelMap modelMap,HttpServletRequest request) {
        if(getUser(request)!=null){
            modelMap.addAttribute("user", getUser(request));
        }
        return theme.getPcTemplate("user/forget_password_mobile");
    }

    /**
     * 用户提交手机号码申请获取验证码
     *
     * @param username
     *        提交的手机号码
     * @param captcha
     *        验证码
     * @return
     * @throws Exception
     */
    @ResponseBody
    @PostMapping(value = "/forget_password/getbackcode")
    public DataVo getBackCode(@RequestParam(value = "username", required = false) String username,
                              @RequestParam(value = "captcha", required = false) String captcha) throws Exception {
        DataVo data;
        // 校验验证码
        DataVo verifyDo = checkVerifyCode(captcha,getObjString(session.getAttribute("kaptcha")));
        if(null != verifyDo){
            return verifyDo;
        }
        if (StringUtils.isBlank(username)) {
            return DataVo.failure("手机号码不能为空");
        }
        if(!StringHelperUtils.checkPhoneNumber(username)) {
            return DataVo.failure("手机号码错误！");
        }
        data = userService.userGetBackCode(username);
        return data;
    }

    /**
     * 用户选择找回密码方式
     *
     */
    @GetMapping(value = "/forget_password/email")
    public String forgetPasswordEmail(ModelMap modelMap,HttpServletRequest request) {
        if(getUser(request)!=null){
            modelMap.addAttribute("user", getUser(request));
        }
        return theme.getPcTemplate("user/forget_password_email");
    }

    /**
     * 用户提交邮箱地址申请获取验证码
     *
     * @param username
     *        提交的手机号码
     * @return
     * @throws Exception
     */
    @ResponseBody
    @PostMapping(value = "/forget_password/getbackemailcode")
    public DataVo getEmailBackCode(@RequestParam(value = "username", required = false) String username) throws Exception {
        if (StringUtils.isBlank(username)) {
            return DataVo.failure("手机号码不能为空");
        }
        if (!StringHelperUtils.emailFormat(username)) {
            return DataVo.failure("邮箱格式错误！");
        }
        return userService.getEmailBackCode(username);
    }

    /**
     * 用户提交手机号码申请获取验证码
     *
     * @param username
     *        用户邮箱或者手机号码
     * @param code
     *        验证码
     * @param password
     *        重新设置的新密码
     * @return
     * @throws Exception
     */
    @ResponseBody
    @PostMapping(value = "/forget_password/update_password")
    public DataVo updateUserPassword(@RequestParam(value = "username", required = false) String username,
                                     @RequestParam(value = "code", required = false) String code,
                                     @RequestParam(value = "password", required = false) String password) throws Exception {
        DataVo data;
        if (StringUtils.isBlank(username)) {
            return DataVo.failure("用户名不能为空");
        }
        if (StringUtils.isBlank(code)){
            return DataVo.failure("验证码不能为空");
        }
        if (StringUtils.isBlank(password)) {
            return DataVo.failure("密码不能为空");
        } else if (password.length() < 6 || password.length() > 30) {
            return DataVo.failure("密码最少6个字符，最多30个字符");
        }
        data = userService.updateGetBackPassword(username,code,password);
        return data;
    }
}
