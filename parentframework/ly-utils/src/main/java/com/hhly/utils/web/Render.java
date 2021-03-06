package com.hhly.utils.web;

import com.hhly.utils.ValueUtil;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.util.regex.Pattern;

/** 在页面渲染时拼接 url 等的工具类 */
public final class Render {

    private static final Pattern HTTP_PATTERN =  Pattern.compile("(?i)http(s?)://");
    private static final Pattern IMAGE_PATTERN = Pattern.compile("(?i)^.*\\.(css|js|ico|gif|bmp|png|jpg|jpeg)$");

    /** 去掉 html 和 js 注释, 基于正则表达式完成 */
    public static String compress(String source) {
        return CompressorUtil.html(source);
    }

    /** 基于当前项目的绝对路径, 从 spring mvc 中获取 */
    public static MvcUriComponentsBuilder.MethodArgumentBuilder mapping(String name) {
        try {
            return MvcUriComponentsBuilder.fromMappingName(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 如果 path 中包含了 #, 则从 spring mvc 的 controller 里面查找具体的 url. 找到后去掉相关类方法上的空参数.<br>
     * 将 domain 和 path 拼起来返回. 如果是资源文件(css js  ico gif png bmp jpg jpeg)则加上版本号
     *
     * @param domain url 上的前缀
     * @param path 「IC#index ==> 表示 IndexController.index() IC 是类名的大写, index 是方法」或「具体的资源文件路径」
     */
    public static String url(String domain, String path) {
        if (path.contains("#")) {
            // 从 mvc 中获取 url 时的参数类似于 IC#index ==> IC 表示 IndexController 的两个大写字母, index 表示类里的方法
            MvcUriComponentsBuilder.MethodArgumentBuilder mapping = mapping(path);
            if (mapping != null) {
                path = mapping.build();
                // 从 spring mvc 中的 controller 中获取的 path 去掉后面的空参数
                if (path.contains("?")) {
                    path = path.substring(0, path.indexOf("?"));
                }
            }
        }
        if (ValueUtil.isNotBlank(path)) {
            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            // 资源文件前缀改成 // 开头(去掉 http 或 https), 版本只在资源文件中添加
            // path.matches("(?i)^.*\\.(css|js|ico|gif|bmp|png|jpg|jpeg)$")
            if (IMAGE_PATTERN.matcher(path).matches()) {
                // domain = domain.replaceFirst("(?i)http(s?)://", "//");
                domain = HTTP_PATTERN.matcher(domain).replaceFirst("//");
                path = version(path);
            }
        }
        return ValueUtil.addSuffix(domain) + ValueUtil.getNil(path);
    }

    /** 给 url 加上版本 */
    public static String version(String path) {
        return ValueUtil.isBlank(path) ? ValueUtil.EMPTY : (path + (path.contains("?") ? "&" : "?") + RenderViewResolver.getVersion());
    }
}
