package main;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddressReptile {
    final static String user_agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.88 Safari/537.36";
    final static String host = "www.stats.gov.cn";
    final static OkHttpClient client = new OkHttpClient()
            .newBuilder()
            .connectTimeout(60000, TimeUnit.MILLISECONDS)
            .readTimeout(60000, TimeUnit.MILLISECONDS)
            .build();
    static File file = new File("D:/test.txt");
    static BufferedSink sink;
    static int id = 1;

    static {
        try {
            sink = Okio.buffer(Okio.sink(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        String url = "http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2020/";
        method(url, "", 0, 0);
//        System.out.println(getHtml(url));
        sink.close();
    }

    private static void method(String url, String end, int level, int pid) throws Exception {
        url = url.replaceAll("\\d+.html", "");
        url += end;
        String html = getHtml(url);
        Pattern pattern = Pattern.compile("<table class='(.*?)'>");
        Matcher matcher = pattern.matcher(html);
        String belong = "";
        while (matcher.find()) {
            belong = matcher.group(1);
            break;
        }
        if (!belong.equals("towntable")) {
            System.out.println("begin sleep");
            Thread.sleep(2000);
            System.out.println("end sleep");
            creatData(url, html, level, pid);
        }
    }

    private static void creatData(String url, String html, int level, int pid) throws Exception {
        Pattern pattern = Pattern.compile("<td><a href='(.*?)'>(.*?)<");
        Matcher matcher = pattern.matcher(html);
        level++;
        int count = 0;
        while (matcher.find()) {
            count++;
            if (level > 1 && count % 2 != 0) continue;      // 数字区域的去掉
            String end = matcher.group(1);
            String name = matcher.group(2);
            if (level == 1) System.out.println(name);
            if (name.equals("市辖区")) {
                method(url, end, level - 1, pid);
                continue;
            }
            StringBuilder sb = new StringBuilder("{\"id\": ");
            sb.append(id).append(", \"pid\": ").append(pid).append(", \"name\": \"")
                    .append(name).append("\", \"level\": ").append(level)
                    .append("}\n");
            sink.writeUtf8(sb.toString());
            id++;
            method(url, end, level, id - 1);
        }
    }

    private static String getHtml(String url) {
        String html = "";
//        System.out.println(url);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("user-agent", user_agent)
                .addHeader("Host", host)
                .build();
        try {
            Response response = client.newCall(request).execute();
//            System.out.println(response.body().string());
            html = new String(response.body().bytes(), "GB2312");
//            System.out.println(html);
        } catch (Exception e) {
            System.out.println("url + : " + url);
            System.out.println();
            e.printStackTrace();
        }
        return html;
    }

}
/*
 *
 * {"id": 1, "pid": 0, "name": "广东省", "level": 0}
 * {"id": 2, "pid": 1, "name": "深圳市", "level": 1}
 */
