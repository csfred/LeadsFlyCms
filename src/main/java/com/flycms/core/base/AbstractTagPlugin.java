package com.flycms.core.base;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.flycms.core.utils.StringHelperUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateModelException;

/**
 * 标签解析抽象类
 *
 * @author cs
 * @date 2020/10/11
 */

@Service
public abstract class AbstractTagPlugin extends ApplicationObjectSupport implements TemplateDirectiveModel, Plugin {
	@Resource
	protected HttpServletRequest request;

	@Resource
	private FreeMarkerConfigurer freeMarkerConfigurer;

	@Override
	@PostConstruct
	public void init() throws TemplateModelException {
		String className = this.getClass().getName().substring(this.getClass().getName().lastIndexOf(".") + 1);
		String beanName = StringUtils.uncapitalize(className);
		String tagName = "fly_" + StringHelperUtils.toUnderline(beanName);
		freeMarkerConfigurer.getConfiguration().setSharedVariable(tagName, this.getApplicationContext().getBean(beanName));
	}

}
