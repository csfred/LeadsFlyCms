package com.flycms.web.tags;

import com.flycms.core.base.AbstractTagPlugin;
import com.flycms.module.share.model.Share;
import com.flycms.module.share.model.ShareCount;
import com.flycms.module.share.service.ShareService;
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
public class Shareinfo extends AbstractTagPlugin {

	@Autowired
	private ShareService shareService;

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
		Share share = shareService.findShareById(id,status);
		if(share!=null){
			//查询统计信息
			ShareCount count=shareService.findShareCountById(share.getId());
			share.setCountDigg(count.getCountDigg());
			share.setCountBurys(count.getCountBurys());
			share.setCountView(count.getCountView());
			share.setCountComment(count.getCountComment());
		}
		env.setVariable("share", builder.build().wrap(share));
		body.render(env.getOut());
	}

}
