package com.flycms.web.tags;

import com.flycms.core.base.AbstractTagPlugin;
import com.flycms.core.entity.PageVo;
import com.flycms.module.question.model.Answer;
import com.flycms.module.question.service.AnswerService;
import freemarker.core.Environment;
import freemarker.template.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Open source house, All rights reserved
 * 开发公司：28844.com<br/>
 * 版权：开源中国<br/>
 *
 * @author sunkaifei
 * 
 */
@Service
public class Answerpage extends AbstractTagPlugin {

	@Autowired
	private AnswerService answerService;

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void execute(Environment env, Map params, TemplateModel[] loopVars,
			TemplateDirectiveBody body) throws TemplateException, IOException {
		DefaultObjectWrapperBuilder builder = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_25);
		// 获取页面的参数
		//问题id
		Long questionId = null;
		//用户id
		Long userId = null;
		//添加时间
		String addTime = null;
		//审核状态
		Integer status = null;

        String orderby=null;

        String order=null;
		//当前页数
		int p = 1;
		//每页记录数
		int rows = 10;
		//处理标签变量
		Map<String, TemplateModel> paramWrap = new HashMap<String, TemplateModel>(params);
		for(String str:paramWrap.keySet()){ 
			if("questionId".equals(str)){
				questionId = Long.parseLong(paramWrap.get(str).toString());
			}
			if("userId".equals(str)){
				userId = Long.parseLong(paramWrap.get(str).toString());
			}
			if("addTime".equals(str)){
				addTime = paramWrap.get(str).toString();
			}
			if("status".equals(str)){
				status = Integer.parseInt(paramWrap.get(str).toString());
			}
            if("orderby".equals(str)){
                orderby = paramWrap.get(str).toString();
            }
            if("order".equals(str)){
                order = paramWrap.get(str).toString();
            }
			if("p".equals(str)){
				p = Integer.parseInt(paramWrap.get(str).toString());
			}
			if("rows".equals(str)){
				rows = Integer.parseInt(paramWrap.get(str).toString());
			}
		}
		// 获取文件的分页
		try {
			PageVo<Answer> pageVo = answerService.getAnswerListPage(questionId,userId,addTime,status,orderby,order,p,rows);
			env.setVariable("answer_page", builder.build().wrap(pageVo));
		} catch (Exception e) {
			env.setVariable("answer_page", builder.build().wrap(null));
		}
		body.render(env.getOut());
	}
}
