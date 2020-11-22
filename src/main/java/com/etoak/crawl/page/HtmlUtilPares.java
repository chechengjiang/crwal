package com.etoak.crawl.page;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableBody;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public class HtmlUtilPares {
    public static Document parse(String url) throws Exception {
        final WebClient webClient = new WebClient(BrowserVersion.CHROME);//新建一个模拟谷歌Chrome浏览器的浏览器客户端对象

        webClient.getOptions().setThrowExceptionOnScriptError(false);//当JS执行出错的时候是否抛出异常, 这里选择不需要
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);//当HTTP的状态非200时是否抛出异常, 这里选择不需要
        webClient.getOptions().setActiveXNative(false);
        webClient.getOptions().setCssEnabled(false);//是否启用CSS, 因为不需要展现页面, 所以不需要启用
        webClient.getOptions().setJavaScriptEnabled(true); //很重要，启用JS
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());//很重要，设置支持AJAX
        webClient.waitForBackgroundJavaScript(1000);//异步JS执行需要耗时,所以这里线程要阻塞30秒,等待异步JS执行结束
        HtmlPage page = null;
        try {
            page = webClient.getPage(url);//尝试加载上面图片例子给出的网页
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // webClient.close();
        }
        String pageXml = page.asXml();//直接将加载完成的页面转换成xml格式的字符串
        //获取总页数
        List<HtmlElement> totalPage = (List) page.getByXPath("//a[@class='paginate_button']");
        HtmlElement lastPage = totalPage.get(totalPage.size() - 1);
        String totoalPageInt = lastPage.getFirstChild().getTextContent();

        //拼接所有页Tbody
        StringBuilder totalPageXml = new StringBuilder();
        totalPageXml.append(pageXml);

        //点击下一页并获取数据
        for (int i = 0; i < Integer.parseInt(totoalPageInt) - 1; i++) {
            //点击下一页之后page对象应该是发生了变化，因此需要重新调用下面 这段话，如果放到循环外则一直是第2页。
            List<HtmlElement> nextPage = (List) page.getByXPath("//a[@class='next paginate_button']");
            HtmlElement nextHtml = nextPage.get(0);
            //这步是最耗时的步骤
            long startTiem =System.currentTimeMillis();
            HtmlPage pageXml1 = nextHtml.click();
            long endTiem =System.currentTimeMillis();
            System.out.println("第"+i+"页："+(endTiem-startTiem));
            HtmlTable pageXml11 = (HtmlTable) pageXml1.getByXPath("//table").get(0);
            totalPageXml.append(pageXml11.asXml());
        }
        webClient.close();
        Document document = Jsoup.parse(totalPageXml.toString());//获取html文档
        return document;
    }

    @Test
    public void test(){
        String htmlStr = "<table id=kbtable >"
                + "<tr> "
                + "<td width=123>"
                + "<div id=12>这里是要获取的数据1</div>"
                + "<div id=13>这里是要获取的数据2</div>"
                + "</td>"
                + "<td width=123>"
                + "<div id=12>这里是要获取的数据3</div>"
                + "<div id=13>这里是要获取的数据4</div>"
                + "</td>	"
                + "</tr>"
                + "</table>";
        Document doc = Jsoup.parse(htmlStr);
        System.out.println(doc);
    }
}
