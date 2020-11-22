package com.etoak.crawl.main;

import com.etoak.crawl.link.LinkFilter;
import com.etoak.crawl.link.Links;
import com.etoak.crawl.mapper.ShareDao;
import com.etoak.crawl.page.HtmlUtilPares;
import com.etoak.crawl.page.PageParserTool;
import com.etoak.crawl.page.Share;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class MyCrawler {

    /**
     * 使用种子初始化 URL 队列
     *
     * @param seeds 种子 URL
     * @return
     */
    private void initCrawlerWithSeeds(String[] seeds) {
        for (int i = 0; i < seeds.length; i++) {
            Links.addUnvisitedUrlQueue(seeds[i]);
        }
    }


    /**
     * 抓取过程
     *
     * @param seeds
     * @return
     */
    public void crawling(String[] seeds) throws Exception {

        //初始化 URL 队列
        initCrawlerWithSeeds(seeds);

        //定义过滤器，提取以 http://www.baidu.com 开头的链接
        LinkFilter filter = new LinkFilter() {
            public boolean accept(String url) {
                if (url.startsWith("http://quote.eastmoney.com/center/gridlist.html#hs_a_board"))
                    return true;
                else
                    return false;
            }
        };

        //循环条件：待抓取的链接不空且抓取的网页不多于 1000
        while (!Links.unVisitedUrlQueueIsEmpty() && Links.getVisitedUrlNum() <= 1000) {

            //先从待访问的序列中取出第一个；
            String visitUrl = (String) Links.removeHeadOfUnVisitedUrlQueue();
            if (visitUrl == null) {
                continue;
            }

            //根据URL得到page;
            //Page page = RequestAndResponseTool.sendRequstAndGetResponse(visitUrl);

            Document document = HtmlUtilPares.parse("http://quote.eastmoney.com/center/gridlist.html#hs_a_board");
            //对page进行处理： 访问DOM的某个标签
            Elements es = PageParserTool.selectHtmlUtil(document, "tbody tr");
            if (!es.isEmpty()) {
                /*System.out.println("下面将打印所有a标签： ");
                System.out.println(es);*/
            }

            List<Share> sharesList = new LinkedList<>();
            Iterator iterator = es.iterator();
            while (iterator.hasNext()) {
                Element trElement = (Element) iterator.next();
                Elements tdElements = trElement.select("td");

                String shareCode = tdElements.eq(1).select("a").text();
                String shareName = tdElements.eq(2).select("a").text();
                String latestPrice = tdElements.eq(4).select("span").text();
                String upAndDownRange = tdElements.eq(5).select("span").text();
                String upAndDownPirce = tdElements.eq(6).select("span").text();
                String turnoverHand = tdElements.eq(7).text();
                String turnoverAmount = tdElements.eq(8).text();
                String amplitude = tdElements.eq(9).text();
                String high = tdElements.eq(10).select("span").text();
                String low = tdElements.eq(11).select("span").text();
                String open = tdElements.eq(12).select("span").text();
                String previousClose = tdElements.eq(13).text();
                String volumeRate = tdElements.eq(14).text();
                String turnoverRate = tdElements.eq(15).text();
                String peRation = tdElements.eq(16).text();
                String pb = tdElements.eq(17).text();

                Share share = new Share();
                share.setShareCode(shareCode);
                share.setShareName(shareName);
                share.setLatestPrice(latestPrice);
                share.setUpAndDownRange(upAndDownRange);
                share.setUpAndDownPirce(upAndDownPirce);
                share.setTurnoverHand(turnoverHand);
                share.setTurnoverAmount(turnoverAmount);
                share.setAmplitude(amplitude);
                share.setHigh(high);
                share.setLow(low);
                share.setOpen(open);
                share.setPreviousClose(previousClose);
                share.setVolumeRate(volumeRate);
                share.setTurnoverRate(turnoverRate);
                share.setPeRation(peRation);
                share.setPb(pb);

                sharesList.add(share);
            }
            insertShareDataDaily(sharesList);
            /*//将保存文件
            FileTool.saveToLocal(page);

            //将已经访问过的链接放入已访问的链接中；
            Links.addVisitedUrlSet(visitUrl);

            //得到超链接
            Set<String> links = PageParserTool.getLinks(page,"img");
            for (String link : links) {
                Links.addUnvisitedUrlQueue(link);
                System.out.println("新增爬取路径: " + link);
            }*/
        }
    }


    //main 方法入口
    public static void main(String[] args) throws Exception {
        MyCrawler crawler = new MyCrawler();
        crawler.crawling(new String[]{"http://quote.eastmoney.com/center/gridlist.html#hs_a_board"});
    }

    public void insertShareDataDaily(List<Share> shareList){
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream("mybatis-config.xml");
        } catch (IOException e) {
            e.printStackTrace();
        }
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        ShareDao shareDao = sqlSession.getMapper(ShareDao.class);
        shareDao.insertSharesDaily(shareList);
        sqlSession.commit();
        sqlSession.close();
    }
    @Test
    public void test() {
        try {
            InputStream inputStream = Resources.getResourceAsStream("mybatis-config.xml");
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            SqlSession sqlSession = sqlSessionFactory.openSession();
            ShareDao testMapper = sqlSession.getMapper(ShareDao.class);
            /*Map<String, String> param = new HashMap<String, String>();
            param.put("CountryCode","AFG");
            List<Map<String,String>> result = testMapper.testList(param);*/
            Share shares1 = new Share();
            shares1.setShareCode("111");
            shares1.setHigh("aa");
            shares1.setOpen("bb");
            Share shares2 = new Share();
            shares2.setShareCode("112");
            shares2.setHigh("aa");
            shares2.setOpen("bb");
            List<Share> list  = new ArrayList<>();
            list.add(shares1);
            list.add(shares2);
            testMapper.insertSharesDaily(list);
            sqlSession.commit();
            sqlSession.close();
            /*Connection conn = sqlSession.getConnection();
            if (conn != null) {
                System.out.println("数据库已建立连接!");
            } else {
                System.out.println("数据库未建立连接!");
            }*/
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
