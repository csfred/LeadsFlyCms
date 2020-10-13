package com.flycms.web.system;

import com.flycms.core.base.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author aa
 * @date 2020/10/11
 */
@Controller
@RequestMapping("/system/market")
public class MarketAdminController extends BaseController{

    /**
     * 促销活动列表
     * @param modelMap
     * @return
     */
    @GetMapping(value = "/pro_rule_list")
    public String getProRuleList(ModelMap modelMap, HttpServletRequest request){
        modelMap.addAttribute("admin", getAdminUser(request));
        return theme.getAdminTemplate("market/pro_rule_list");
    }

    /**
     * 积分兑换列表
     * @param modelMap
     * @return
     */
    @GetMapping(value = "/cost_point_list")
    public String getCostPointList(ModelMap modelMap, HttpServletRequest request){
        modelMap.addAttribute("admin", getAdminUser(request));
        return theme.getAdminTemplate("market/cost_point_list");
    }

    /**
     * 限时抢购列表
     * @param modelMap
     * @return
     */
    @GetMapping(value = "/pro_speed_list")
    public String getProSpeedList(ModelMap modelMap, HttpServletRequest request){
        modelMap.addAttribute("admin", getAdminUser(request));
        return theme.getAdminTemplate("market/pro_speed_list");
    }

    /**
     * 团购列表
     * @param modelMap
     * @return
     */
    @GetMapping(value = "/regiment_list")
    public String getRegimentList(ModelMap modelMap, HttpServletRequest request){
        modelMap.addAttribute("admin", getAdminUser(request));
        return theme.getAdminTemplate("market/regiment_list");
    }

    /**
     * 特价活动列表
     * @param modelMap
     * @return
     */
    @GetMapping(value = "/sale_list")
    public String getSaleList(ModelMap modelMap, HttpServletRequest request){
        modelMap.addAttribute("admin", getAdminUser(request));
        return theme.getAdminTemplate("market/sale_list");
    }

    /**
     * 代金券列表列表
     * @param modelMap
     * @return
     */
    @GetMapping(value = "/ticket_list")
    public String getTicketList(ModelMap modelMap, HttpServletRequest request){
        modelMap.addAttribute("admin", getAdminUser(request));
        return theme.getAdminTemplate("market/ticket_list");
    }
}
