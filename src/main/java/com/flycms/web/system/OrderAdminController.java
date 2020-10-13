package com.flycms.web.system;

import com.flycms.core.base.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author aa
 * @date 2020/10/11
 */
@Controller
@RequestMapping("/system/order")
public class OrderAdminController  extends BaseController {
    /**
     * 订单列表
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/order_list")
    public String getOrderList(ModelMap modelMap, HttpServletRequest request){
        modelMap.addAttribute("admin", getAdminUser(request));
        return theme.getAdminTemplate("order/order_list");
    }

    /**
     * 添加订单
     * @param modelMap
     * @return
     */
    @RequestMapping(value = "/order_add")
    public String getOrderAdd(ModelMap modelMap, HttpServletRequest request){
        modelMap.addAttribute("admin", getAdminUser(request));
        return theme.getAdminTemplate("order/order_add");
    }
}
