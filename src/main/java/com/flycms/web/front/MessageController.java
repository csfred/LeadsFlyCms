package com.flycms.web.front;

import com.flycms.core.base.BaseController;
import com.flycms.core.entity.DataVo;
import com.flycms.module.message.model.Message;
import com.flycms.module.message.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 *
 * @author aa
 * @date 2020/10/11
 */
@Slf4j
public class MessageController extends BaseController {
    @Resource
    private MessageService messageService;

    /**
     * 我的站内短信
     * @param modelMap
     * @return
     */
    @GetMapping(value = "/ucenter/message")
    public String userMessage(ModelMap modelMap,HttpServletRequest request){
        modelMap.addAttribute("user", getUser(request));
        return theme.getPcTemplate("user/message");
    }

    /**
     * 保存用户发送的短信
     * @param message
     * @param result
     * @return
     */
    @PostMapping("/ucenter/message/add_message")
    @ResponseBody
    public DataVo addMessage(@Valid Message message, BindingResult result,
                             HttpServletRequest request){
        DataVo data = DataVo.failure("操作失败");
        try {
            if (result.hasErrors()) {
                List<ObjectError> list = result.getAllErrors();
                for (ObjectError error : list) {
                    return DataVo.failure(error.getDefaultMessage());
                }
                return null;
            }
            message.setFromId(getUser(request).getUserId());
            data = messageService.addMessage(message);
        } catch (Exception e) {
            data = DataVo.failure(e.getMessage());
        }
        return data;
    }
}
