package com.flycms.module.search.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.flycms.constant.SolrConst;
import com.flycms.core.entity.PageVo;
import com.flycms.core.utils.DateUtils;
import com.flycms.core.utils.Md5Utils;
import com.flycms.module.article.model.Article;
import com.flycms.module.article.service.ArticleService;
import com.flycms.module.question.model.Question;
import com.flycms.module.question.service.QuestionService;
import com.flycms.module.search.model.Info;
import com.flycms.module.share.model.Share;
import com.flycms.module.share.service.ShareService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * solr服务
 * @author cs
 * @date 2020/10/11
 */
@Slf4j
@Service
public class SolrService {
	@Resource
	private ArticleService articleService;

    @Resource
    private ShareService shareService;

    @Resource
    private QuestionService questionService;

    @Resource
    private SolrConst solrConst;

    private static HttpSolrClient server = null;
    //private static String url = solrConst;//其中db为自定义的core名称

    @SuppressWarnings("deprecation")
    public HttpSolrClient getServer() {
        if (server == null) {
            server = new HttpSolrClient(solrConst.getServerUrl());
            server.setDefaultMaxConnectionsPerHost(1000);
            // 最大连接数
            server.setMaxTotalConnections(10000);
            // 设置连接超时时间（单位毫秒） 1000
            server.setConnectionTimeout(60000);
            // 设置读数据超时时间(单位毫秒) 1000
            server.setSoTimeout(60000);
            // 遵循从定向
            server.setFollowRedirects(false);
            // 允许压缩
            server.setAllowCompression(true);
        }
        return server;
    }

    /**
     * 索引单个文章信息
     *
     * @param id
     * @return
     * @throws ParseException
     */
    public boolean indexQuestionId(long id) throws ParseException {
        try {
            Question question=questionService.findQuestionById(id,2);
            if(question!=null) {
                SolrInputDocument doc = new SolrInputDocument();
                doc.addField("id", Md5Utils.getMD5("0-"+question.getId()));
                doc.addField("shortUrl", question.getShortUrl());
                doc.addField("title", question.getTitle());
                doc.addField("infoId", question.getId());
                doc.addField("infoType", 0);
                doc.addField("userId", question.getUserId());
                doc.addField("categoryId","");
                doc.addField("content", question.getContent());
                doc.addField("createTime", DateUtils.getSolrDate(DateUtils.formatDateToString(question.getCreateTime())));
                doc.addField("recommend", question.getRecommend());
                doc.addField("weight", question.getWeight());
                getServer().add(doc);
                getServer().commit();
                return true;
            }
            return false;
        } catch (SolrServerException | IOException e) {
            log.error(e.getMessage(),e);
        }
        return false;
    }

    /**
     * 将所有的分享信息都索引
     *
     * @return
     * @throws ParseException
     */
    public boolean indexAllQuestion() throws ParseException {
        try {
            List<SolrInputDocument> docs = new ArrayList<>();
            int j=questionService.getQuestionIndexCount();
            int c = j%1000==0?j/1000:j/1000+1;
            for (int i=0; i<c;i++) {
                List<Question> list = questionService.getQuestionIndexList(i,1000);
                for (Question info:list) {
                    Question question=questionService.findQuestionById(info.getId(),2);
                    SolrInputDocument doc = new SolrInputDocument();
                    doc.addField("id", Md5Utils.getMD5("0-"+question.getId()));
                    doc.addField("shortUrl", question.getShortUrl());
                    doc.addField("title", question.getTitle());
                    doc.addField("infoId", question.getId());
                    doc.addField("infoType", 0);
                    doc.addField("userId", question.getUserId());
                    doc.addField("categoryId", "");
                    doc.addField("content", question.getContent());
                    doc.addField("createTime", DateUtils.getSolrDate(DateUtils.formatDateToString(question.getCreateTime())));
                    doc.addField("recommend", question.getRecommend());
                    doc.addField("weight", question.getWeight());
                    docs.add(doc);
                }
                getServer().add(docs);
                getServer().commit();
            }
            return true;
        } catch (SolrServerException | IOException e) {
            log.error(e.getMessage(),e);
        }
        return false;
    }

    /**
     * 将所有的文章信息都索引
     *
     * @return
     * @throws ParseException
     */
    public boolean indexAllArticle() throws ParseException {
        try {
            List<SolrInputDocument> docs = new ArrayList<>();
            int j=articleService.getArticleIndexCount();
            int c = j%1000==0?j/1000:j/1000+1;
            for (int i=0; i<c;i++) {
                List<Article> list = articleService.getArticleIndexList(i,1000);
                for (Article info:list) {
                    Article article=articleService.findArticleById(info.getId(),2);
                    SolrInputDocument doc = new SolrInputDocument();
                    doc.addField("id", Md5Utils.getMD5("1-"+article.getId()));
                    doc.addField("shortUrl", article.getShortUrl());
                    doc.addField("title", article.getTitle());
                    doc.addField("infoId", article.getId());
                    doc.addField("infoType", 1);
                    doc.addField("userId", article.getUserId());
                    doc.addField("categoryId", article.getCategoryId());
                    doc.addField("content", article.getContent());
                    doc.addField("createTime", DateUtils.getSolrDate(DateUtils.formatDateToString(article.getCreateTime())));
                    doc.addField("recommend", article.getRecommend());
                    doc.addField("weight", article.getWeight());
                    docs.add(doc);
                }
                getServer().add(docs);
                getServer().commit();
            }
            return true;
        } catch (SolrServerException | IOException e) {
            log.error(e.getMessage(),e);
        }
        return false;
    }

    /**
     * 索引单个文章信息
     *
     * @param id
     * @return
     * @throws ParseException 
     */
    public boolean indexArticleId(long id) throws ParseException {
        try {
            Article article=articleService.findArticleById(id,2);
        	if(article!=null) {
            	SolrInputDocument doc = new SolrInputDocument();
                doc.addField("id", Md5Utils.getMD5("1-"+article.getId()));
                doc.addField("shortUrl", article.getShortUrl());
                doc.addField("title", article.getTitle());
                doc.addField("infoId", article.getId());
                doc.addField("infoType", 1);
                doc.addField("userId", article.getUserId());
                doc.addField("categoryId", article.getCategoryId());
                doc.addField("content", article.getContent());
                doc.addField("createTime", DateUtils.getSolrDate(DateUtils.formatDateToString(article.getCreateTime())));
                doc.addField("recommend", article.getRecommend());
                doc.addField("weight", article.getWeight());
                getServer().add(doc);
                getServer().commit();
                return true;
        	}
        	return false;
        } catch (SolrServerException | IOException e) {
            log.error(e.getMessage(),e);
        }
        return false;
    }

    /**
     * 将所有的分享信息都索引
     *
     * @return
     * @throws ParseException
     */
    public boolean indexAllShare() throws ParseException {
        try {
            List<SolrInputDocument> docs = new ArrayList<>();
            int j=shareService.getShareCount(null,null,null,2);
            int c = j%1000==0?j/1000:j/1000+1;
            for (int i=0; i<c;i++) {
                List<Share> list = shareService.getShareIndexList(i,1000);
                for (Share info:list) {
                    Share share=shareService.findShareById(info.getId(),2);
                    SolrInputDocument doc = new SolrInputDocument();
                    doc.addField("id", Md5Utils.getMD5("2-"+share.getId()));
                    doc.addField("shortUrl", share.getShortUrl());
                    doc.addField("title", share.getTitle());
                    doc.addField("infoId", share.getId());
                    doc.addField("infoType", 2);
                    doc.addField("userId", share.getUserId());
                    doc.addField("categoryId", "");
                    doc.addField("content", share.getContent());
                    doc.addField("createTime", DateUtils.getSolrDate(DateUtils.formatDateToString(share.getCreateTime())));
                    doc.addField("recommend", share.getRecommend());
                    doc.addField("weight", share.getWeight());
                    docs.add(doc);
                }
                getServer().add(docs);
                getServer().commit();
            }
            return true;
        } catch (SolrServerException | IOException e) {
            log.error(e.getMessage(),e);
        }
        return false;
    }

    /**
     * 索引单个文章信息
     *
     * @param id
     * @return
     * @throws ParseException
     */
    public boolean indexShareId(Long id) throws ParseException {
        try {
            Share share=shareService.findShareById(id,2);
            if(share!=null) {
                SolrInputDocument doc = new SolrInputDocument();
                doc.addField("id", Md5Utils.getMD5("2-"+share.getId()));
                doc.addField("shortUrl", share.getShortUrl());
                doc.addField("title", share.getTitle());
                doc.addField("infoId", share.getId());
                doc.addField("infoType", 2);
                doc.addField("userId", share.getUserId());
                doc.addField("categoryId",share.getCategoryId());
                doc.addField("content", share.getContent());
                doc.addField("createTime", DateUtils.getSolrDate(DateUtils.formatDateToString(share.getCreateTime())));
                doc.addField("recommend", share.getRecommend());
                doc.addField("weight", share.getWeight());
                getServer().add(doc);
                getServer().commit();
                return true;
            }
            return false;
        } catch (SolrServerException | IOException e) {
            log.error(e.getMessage(),e);
        }
        return false;
    }

    /**
     * 删除索引
     * @param infoType
     *
     * @param infoId
     */
    public void indexDeleteInfo(Integer infoType,long infoId) {
        try {
            getServer().deleteById(Md5Utils.getMD5(infoType+"-"+infoId));
            getServer().commit();
        } catch (SolrServerException | IOException e) {
            log.error(e.getMessage(),e);
        }
    }

    /**
     * 删除所有索引
     */
    public void deleteAllInfoindex() {
        try {
            //删除查询到的索引信息
            getServer().deleteByQuery("*");
            getServer().commit(true, true);
        } catch (SolrServerException | IOException e) {
            log.error(e.getMessage(),e);
        }
    }

    /**
     * 根据相关要求查询信息列表
     *
     * @param title
     *        信息标题
     * @param userId
     *        用户id
     * @param infoType
     *        信息类型，0是全部，1问答，2文章，3分享
     * @param page
     *        当前页码
     * @param rows
     *        每页记录数
     * @return
     * @throws IOException
     * @throws SolrServerException
     * @throws ParseException
     */
    public PageVo<Info> searchInfo(String title, Long userId, Integer infoType, Long categoryId, String notId,String orderby,int page,int rows) throws IOException, SolrServerException, ParseException {
        PageVo<Info> pageVo = new PageVo<Info>(page);
        pageVo.setRows(rows);
        if(page<1){
            page=1;
        }
        SolrQuery query = new SolrQuery();
        boolean bIsOk = !StringUtils.isEmpty(title) || (userId!=null && userId > 0) || (infoType!=null);
        if(bIsOk) {
            // 创建组合条件串
            StringBuilder params = new StringBuilder("");
            // 按标题查询
            if (!StringUtils.isEmpty(title)) {
                String escapedkey = ClientUtils.escapeQueryChars(title);
                params.append(" AND title:" + escapedkey + " OR content:" + escapedkey);
            }

            // 按用户id查询
            if (userId!=null && userId > 0) {
                params.append(" AND userId:" + userId);
            }

            // 按信息分类查询
            if (infoType!=null) {
                if(infoType==0){
                    params.append(" AND infoType:*");
                }else if(infoType==1){
                    params.append(" AND infoType:0");
                }else if(infoType==2){
                    params.append(" AND infoType:1");
                }else if(infoType==3){
                    params.append(" AND infoType:2");
                }
            }
            // 按信息分类id查询
            if (categoryId != null && categoryId > 0) {
                params.append(" AND categoryId:" + categoryId);
            }

            // 按用户id查询
            if (!StringUtils.isEmpty(notId)) {
                params.append(" AND -id:"+Md5Utils.getMD5(notId));
            }
            query.setQuery(this.StrStartFind(params.toString()));
        }else {
            query.setQuery("*:*");
        }
        // 设置"起始位置"：表示从结果集的第几条数据开始显示。默认下标是0开始
        query.setStart(pageVo.getOffset());
        // 设置每页显示的行数
        query.setRows(pageVo.getRows());

        query.set("qf","title^1");
        // 设置输出每条记录的索引分值
        query.set("fl", "*,score");

        if (!StringUtils.isEmpty(title)) {
            // 设置高亮显示——————— // 开启高亮功能
            query.setHighlight(true);
            query.setParam("hl", "true");
            //高亮字段
            query.setParam("hl.fl", "title");
            // 渲染标签
            query.setHighlightSimplePre("<font color=\"red\">");
            // 渲染标签
            query.setHighlightSimplePost("</font>");
            //结果分片数，默认为1
            query.setHighlightSnippets(1);
            //每个分片的最大长度，默认为100
            query.setHighlightFragsize(80);
        }
        if(!StringUtils.isEmpty(orderby)){
            if("recommend".equals(orderby)){
                //按推荐值排序
                query.set("sort", "recommend desc");
            }else if("hot".equals(orderby)){
                //按权重值排序
                query.set("sort", "weight desc");
            }
        }else{
            //排序条件
            query.set("sort", "score desc,createTime desc");
        }

        QueryResponse response = getServer().query(query);
        SolrDocumentList dlist = response.getResults();
        //Map<String,Map<String,List<String>>> highlightMap=response.getHighlighting()
        //System.out.println("文档个数：" + dlist.getNumFound())
        // 第一个Map的键是文档的ID，第二个Map的键是高亮显示的字段名
        List<Info> list = new ArrayList<Info>();
        Info bean;
        JSONArray jsonArray;
        for (SolrDocument solrDocument : dlist) {
            bean = new Info();
            bean.setId(solrDocument.getFieldValue("id").toString());
            jsonArray = JSON.parseArray(solrDocument.getFieldValue("shortUrl").toString().replace("[","[\"").replace("]","\"]"));
            bean.setShortUrl(jsonArray.getString(0));
            jsonArray = JSON.parseArray(solrDocument.getFieldValue("userId").toString());
            bean.setUserId(jsonArray.getLong(0));
            jsonArray = JSON.parseArray(solrDocument.getFieldValue("title").toString().replace("[","[\"").replace("]","\"]"));
            bean.setTitle(jsonArray.getString(0));
            jsonArray = JSON.parseArray(solrDocument.getFieldValue("infoId").toString());
            bean.setInfoId(jsonArray.getLong(0));
            jsonArray = JSON.parseArray(solrDocument.getFieldValue("infoType").toString());
            bean.setInfoType(jsonArray.getInteger(0));
            jsonArray = JSON.parseArray(solrDocument.getFieldValue("content").toString().replace("[","[\"").replace("]","\"]"));
            bean.setContent(jsonArray.getString(0));
            jsonArray = JSON.parseArray(solrDocument.getFieldValue("createTime").toString().replace("[","[\"").replace("]","\"]"));
            String dt=jsonArray.getString(0);
            SimpleDateFormat sdf1= new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
            SimpleDateFormat sdf2= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            bean.setCreateTime(sdf2.format(sdf1.parse(dt)));
            list.add(bean);
        }
        pageVo.setList(list);
        pageVo.setCount(Integer.parseInt(String.valueOf(dlist.getNumFound())));
        return pageVo;
    }

    /**
     * Solrj查询语句拼接处理，字符串是否以AND起始，如果有则去除，没有的直接返回
     * @param str
     * @return
     */
    public static String StrStartFind(String str){
        try {
            //判断字符串是否以‘AND’开始
            boolean b = str.startsWith(" AND ");
            if(b) {
                return str.substring(str.indexOf(" AND ")+5,str.length());
            }return str ;
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return null;
        }
    }

    /**
     * 企业搜索翻页处理
     * @param fullName
     * @param city
     * @param industry
     * @param scale
     * @param capital
     * @param page
     * @param rows
     * @param maxdoc
     * @return
     * @throws IOException
     */
	public String labelPage(
			String fullName,
			String city,
			String industry,
			String scale,
			String capital, 
			int page,
			int rows,
			int maxdoc
		) throws IOException{
		
		StringBuffer link=new StringBuffer();
		if(maxdoc!=0){
            //每页显示记录数
            int pagesize = 10;
            if(rows>0) {
            	pagesize=rows;
            }
            //最多显示分页页数
            int liststep = 10;
            //默认显示第一页
            int pages = 1;
            if(page>1) {
            	//分页页码变量
            	pages = page;
            }
            int count = 0;
            //假设取出记录总数
            if(maxdoc>0) {
            	count = maxdoc;
            }
            //求总页数，ceil（num）取整不小于num
            int pagescount = (int) Math.ceil((double) count / pagesize);
            if (pagescount < pages) {
                //如果分页变量大总页数，则将分页变量设计为总页数
                pages = pagescount;
            }
            if (pages < 1) {
                //如果分页变量小于１,则将分页变量设为１
                pages = 1;
            }
            //从第几页开始显示分页信息
            int listbegin = (pages - (int) Math.ceil((double) liststep / 2));
            if (listbegin < 1) {
                listbegin = 1;
            }
            if(pages>=26 && pagescount>30){
            	listbegin=21;
            }
            if(pages>=26 && pagescount<30){
            	listbegin=pagescount-9;
            }
            if(pages<=pagescount && pagescount<10){
            	listbegin=1;
            }
            //分页信息显示到第几页
            int listend = pages + liststep/2;
            if (listend > pagescount) {
                listend = pagescount + 1;
            }
            
            if(listend<=10 && pagescount>=10){
            	listend = 11;
            }
            if(listend<10 && pagescount<10){
            	listend = pagescount + 1;
            }
            //获取搜索参数处理  
            StringBuffer buffer = new StringBuffer(); 
	        if (!StringUtils.isEmpty(fullName)) {  
	        	buffer.append("name="+java.net.URLEncoder.encode(fullName,"UTF-8")); 
	        }
	        if (!StringUtils.isEmpty(city)) {  
	        	buffer.append("&city="+city);  
	        }
	        if (!StringUtils.isEmpty(industry)) {  
	        	buffer.append("&it="+industry);  
	        }
	        if (!StringUtils.isEmpty(scale)) {  
	        	buffer.append("&sc="+scale); 
	        }
	        if (!StringUtils.isEmpty(capital)) {  
	        	buffer.append("&ct="+capital); 
	        }

            //<显示分页信息
            //<显示上一页
            if (pages > 1) {
            	link.append("<li class=\"prev\"><a href=?"+buffer+"&p=" + (pages + -1) + " rel=\"prev\">上一页</a></li>");
            }//>显示上一页
            //<显示分页码
            for (int i = listbegin; i < listend; i++) {
	            if (i != pages) {
	            	//如果i不等于当前页
	    	        if(i<=30){
	    	        	link.append("<li><a href=?"+buffer+"&p=" + i + ">" + i + "</a></li>");
	    	        }
	            } else {
	            	if(i<=30){
	            		if((listend-1)>1) {
		            		link.append("<li class=\"active\"><a href=\"javascript:void(0);\">" + i + "</a></li>");
	            		}
	            	}
	            }
            }//显示分页码>
            //<显示下一页
            if (pages != pagescount) {
	        	if(pages<30){
	        		link.append("<li class=\"next\"><a href=?"+buffer+"&p=" + (pages + 1) +" rel=\"next\">下一页</a></li>");
	        	}
            }//>显示下一页
            //>显示分页信息
		}
		return link.append("").toString();
	}

    public static void main(String[] args) {
        SolrService solr=new SolrService();
        solr.indexDeleteInfo(1,47);
        System.out.println();
    }
}
