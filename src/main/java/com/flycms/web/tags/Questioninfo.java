package com.flycms.web.tags;

import com.flycms.core.base.AbstractTagPlugin;
import com.flycms.module.question.model.Question;
import com.flycms.module.question.model.QuestionCount;
import com.flycms.module.question.service.QuestionService;
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
 * 版权：开源中国<br/>*
 *
 * 用户信息查询标签
 * 
 * @author sunkaifei
 * 
 */
@Service
public class Questioninfo extends AbstractTagPlugin {

	@Autowired
	private QuestionService questionService;

	@SuppressWarnings("rawtypes")
	public void execute(Environment env, Map params, TemplateModel[] loopVars,
			TemplateDirectiveBody body) throws TemplateException, IOException {
		DefaultObjectWrapperBuilder builder = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_25);
		// 获取页面的参数
		Long id = null;
		Integer status = 0;
		
		@SuppressWarnings("unchecked")
		Map<String, TemplateModel> paramWrap = new HashMap<String, TemplateModel>(params);
		for(String str:paramWrap.keySet()){ 
			if("id".equals(str)){
				id = Long.parseLong(paramWrap.get(str).toString());
			}
			if("status".equals(str)){
				status = Integer.parseInt(paramWrap.get(str).toString());
			}
		}
		// 获取文件的分页
		Question question = questionService.findQuestionById(id,status);
		if(question!=null){
			//查询统计信息
			QuestionCount count=questionService.findQuestionCountById(question.getId());
			question.setCountAnswer(count.getCountAnswer());
			question.setCountView(count.getCountView());
			question.setCountDigg(count.getCountDigg());
			question.setCountBurys(count.getCountBurys());
			question.setCountFollow(count.getCountFollow());
		}

		env.setVariable("question", builder.build().wrap(question));
		body.render(env.getOut());
	}

}
