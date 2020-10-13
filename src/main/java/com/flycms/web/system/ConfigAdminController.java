package com.flycms.web.system;

import com.flycms.core.base.BaseController;
import com.flycms.core.entity.DataVo;
import com.flycms.core.entity.PageVo;
import com.flycms.core.service.GlobalTagService;
import com.flycms.module.config.model.Areas;
import com.flycms.module.config.model.Smsapi;
import com.flycms.module.config.model.Config;
import com.flycms.module.config.service.AreasService;
import com.flycms.module.config.service.SmsapiService;
import com.flycms.module.config.service.ConfigService;
import com.flycms.module.other.model.FilterKeyword;
import com.flycms.module.other.service.FilterKeywordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author aa
 * @date 2020/10/11
 */
@Slf4j
@Controller
@RequestMapping("/system/site")
public class ConfigAdminController extends BaseController {
    @Resource
    protected ConfigService configService;
    @Resource
    protected GlobalTagService globalTagService;
    @Resource
    protected AreasService areasService;
    @Resource
    protected SmsapiService smsapiService;
    @Resource
    protected FilterKeywordService filterKeywordService;

    @GetMapping(value = "/web_config")
    public String basic(ModelMap modelMap, HttpServletRequest request){
        Map<String,String> map=new HashMap<String,String>(16);
        List<Config> configList=configService.getConfigAllList();
        for (Config List : configList) {
            map.put(List.getKeycode(), List.getKeyvalue());
        }
        modelMap.addAttribute("config",map);
        modelMap.addAttribute("admin", getAdminUser(request));
        return theme.getAdminTemplate("webconfig/web_config");
    }

    /**
     * 修改网站配置
     *
     * @author Administrator
     *
     */
    @ResponseBody
    @PostMapping(value = "/webconfig_updagte")
    public DataVo basicSubmit(
            @RequestParam(value = "fly_title") String fly_title,
            @RequestParam(value = "fly_url") String fly_url,
            @RequestParam(value = "fly_seo_title") String fly_seo_title,
            @RequestParam(value = "fly_seo_keywords") String fly_seo_keywords,
            @RequestParam(value = "fly_seo_description") String fly_seo_description) {
        DataVo data = DataVo.failure("操作失败");
        try {
            if (StringUtils.isBlank(fly_title)) {
                return DataVo.failure("网站名称不能为空！");
            }
            if (StringUtils.isBlank(fly_url)) {
                return DataVo.failure("网站网址不能为空");
            }

            configService.updateConfigByKey("fly_title", fly_title);
            configService.updateConfigByKey("fly_url", fly_url);
            configService.updateConfigByKey("fly_seo_title", fly_seo_title);
            configService.updateConfigByKey("fly_seo_keywords", fly_seo_keywords);
            configService.updateConfigByKey("fly_seo_description", fly_seo_description);
            //更新前台标签
            globalTagService.setSharedVariable();
            return DataVo.success("操作成功", DataVo.NOOP);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            DataVo.failure("未知错误！");
        }
        return data;
    }

    /**
     * 网站底部信息
     * @param flyFooterCode
     * @return
     */
    @ResponseBody
    @PostMapping(value = "/config_footer_updagte")
    public DataVo basicSubmit(
            @RequestParam(value = "fly_footer_code") String flyFooterCode) {
        DataVo data;
        try {
            if (StringUtils.isBlank(flyFooterCode)) {
                return DataVo.failure("网站名称不能为空！");
            }
            configService.updateConfigByKey("fly_footer_code", flyFooterCode);
            //更新前台标签
            globalTagService.setSharedVariable();
            data = DataVo.success("操作成功", DataVo.NOOP);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            data = DataVo.failure("未知错误！");
        }
        return data;
    }

    @GetMapping(value = "/area_list")
    public String getAreaList(@RequestParam(value = "parentId", defaultValue = "0") int parentId,
                              ModelMap modelMap, HttpServletRequest request){
        List<Areas> areas=areasService.selectAreasByPid(parentId);
        modelMap.addAttribute("areas",areas);
        modelMap.addAttribute("admin", getAdminUser(request));
        return theme.getAdminTemplate("webconfig/area_list");
    }

    @GetMapping(value = "/user_config")
    public String userConfig(ModelMap modelMap, HttpServletRequest request){
        Map<String,String> map=new HashMap<>(8);
        List<Config> configList=configService.getConfigAllList();
        for (Config list : configList) {
            map.put(list.getKeycode(), list.getKeyvalue());
        }
        modelMap.addAttribute("config",map);
        modelMap.addAttribute("admin", getAdminUser(request));
        return theme.getAdminTemplate("webconfig/user_config");
    }

    /**
     * 修改用户信息设置配置
     */
    @ResponseBody
    @PostMapping(value = "/userconfig_updagte")
    public DataVo updateUserConfig(
            @RequestParam(value = "user_reg") String userReg,
            @RequestParam(value = "user_reg_verify") String userRegVerify,
            @RequestParam(value = "user_activation_role") String userActivationRole,
            @RequestParam(value = "user_role") String userRole,
            @RequestParam(value = "user_question_verify") String userQuestionVerify,
            @RequestParam(value = "user_answer_verify") String userAnswerVerify
    ) {
        DataVo data;
        try {
            if (!NumberUtils.isNumber(userRole)) {
                return DataVo.failure("权限参数错误");
            }
            if (!NumberUtils.isNumber(userQuestionVerify)) {
                return DataVo.failure("问答审核参数错误");
            }
            if (!NumberUtils.isNumber(userAnswerVerify)) {
                return DataVo.failure("回答审核参数错误");
            }
            configService.updateConfigByKey("user_reg", userReg);
            configService.updateConfigByKey("user_reg_verify", userRegVerify);
            configService.updateConfigByKey("user_activation_role", userActivationRole);
            configService.updateConfigByKey("user_role", userRole);
            configService.updateConfigByKey("user_question_verify", userQuestionVerify);
            configService.updateConfigByKey("user_answer_verify", userAnswerVerify);
            data = DataVo.success("操作成功", DataVo.NOOP);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            data = DataVo.failure("未知错误！");
        }
        return data;
    }

    @GetMapping(value = "/conf_guide")
    public String getAddGuide(ModelMap modelMap, HttpServletRequest request){
        modelMap.addAttribute("admin", getAdminUser(request));
        return theme.getAdminTemplate("webconfig/conf_guide");
    }

    /**
     * 短信接口更新
     * @param modelMap
     * @return
     */
    @GetMapping(value = "/smsapi_edit")
    public String updateSmsapi(ModelMap modelMap,HttpServletRequest request){
        Smsapi sms=smsapiService.findSmsapiByid(1);
        modelMap.addAttribute("sms", sms);
        modelMap.addAttribute("admin", getAdminUser(request));
        return theme.getAdminTemplate("webconfig/smsapi_edit");
    }

    /**
     * 处理短信接口信息
     * @param smsapi
     * @param result
     * @return
     */
    @PostMapping("/smsapi_update")
    @ResponseBody
    public DataVo updateSmsapi(@Valid Smsapi smsapi, BindingResult result){
        DataVo data;
        try {
            if (result.hasErrors()) {
                List<ObjectError> list = result.getAllErrors();
                for (ObjectError error : list) {
                    return DataVo.failure(error.getDefaultMessage());
                }
                return null;
            }
            data = smsapiService.updateSmsapi(smsapi);
        } catch (Exception e) {
            data = DataVo.failure(e.getMessage());
        }
        return data;
    }

    /**
     * 添加违禁关键词
     * @param modelMap
     * @return
     */
    @GetMapping(value = "/add_filterKeyword")
    public String addFilterKeyword(ModelMap modelMap, HttpServletRequest request){
        modelMap.addAttribute("admin", getAdminUser(request));
        return theme.getAdminTemplate("webconfig/add_filterKeyword");
    }

    /**
     * 保存添加违禁关键词
     * @param keyword
     * @return
     */
    @ResponseBody
    @PostMapping(value = "/filterKeyword_save")
    public DataVo saveFilterKeyword(@RequestParam(value = "keyword", required = false) String keyword) {
        DataVo data;
        try {
            if (StringUtils.isBlank(keyword)) {
                return DataVo.failure("关键词不能为空");
            }
            keyword=keyword.trim();
            data = filterKeywordService.addFilterKeyword(keyword);
        } catch (Exception e) {
            data = DataVo.failure(e.getMessage());
        }
        return data;
    }

    /**
     * 违禁关键词列表
     * @param p
     * @param modelMap
     * @return
     */
    @GetMapping(value = "/filterKeyword_list")
    public String filterKeywordList( @RequestParam(value = "p", defaultValue = "1") int p,
                                     ModelMap modelMap,HttpServletRequest request){
        PageVo<FilterKeyword> pageVo=filterKeywordService.getFilterKeywordListPage(p,20);
        modelMap.addAttribute("pageVo", pageVo);
        modelMap.addAttribute("admin", getAdminUser(request));
        return theme.getAdminTemplate("webconfig/list_filterKeyword");
    }

    /**
     * 修改违禁关键词
     * @param id
     * @param modelMap
     * @param request
     * @return
     */
    @GetMapping(value = "/edit_filterKeyword/{id}")
    public String updateFilterKeyword(@PathVariable(value = "id", required = false) String id,
                                      ModelMap modelMap, HttpServletRequest request){
        if (!NumberUtils.isNumber(id)) {
            return theme.getPcTemplate("404");
        }
        FilterKeyword keyword = filterKeywordService.findFilterKeywordById(Long.parseLong(id));
        if(keyword==null){
            return theme.getPcTemplate("404");
        }
        modelMap.addAttribute("keyword", keyword);
        modelMap.addAttribute("admin", getAdminUser(request));
        return theme.getAdminTemplate("webconfig/edit_filterKeyword");
    }

    /**
     * 更新违禁关键词
     * @param id
     * @return
     */
    @ResponseBody
    @PostMapping(value = "/update_filterKeyword")
    public DataVo updateStatus(@RequestParam(value = "id", required = false) String id,
                               @RequestParam(value = "keyword", required = false) String keyword){
        DataVo data;
        if (!NumberUtils.isNumber(id)) {
            return DataVo.failure("id参数错误！");
        }
        if (StringUtils.isBlank(keyword)) {
            return DataVo.failure("关键词不能为空");
        }
        FilterKeyword filterKeyword = filterKeywordService.findFilterKeywordById(Long.parseLong(id));
        if(filterKeyword==null){
            return DataVo.failure("该条信息不存在");
        }
        data = filterKeywordService.updateFilterKeywordById(keyword,Long.parseLong(id));
        return data;
    }

    /**
     * 按id删除违禁关键词
     * @param id
     * @return
     */
    @ResponseBody
    @PostMapping(value = "/delete-filter-keyword")
    public DataVo deleteFilterKeywordById(@RequestParam(value = "id", required = false) String id) {
        DataVo data;
        if (!NumberUtils.isNumber(id)) {
            return DataVo.failure("id错误失败");
        }
        FilterKeyword keyword = filterKeywordService.findFilterKeywordById(Long.parseLong(id));
        if(keyword==null){
            return DataVo.failure("该违禁关键词不存在");
        }
        data = filterKeywordService.deleteFilterKeywordById(Long.parseLong(id));
        return data;
    }
}
