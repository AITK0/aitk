package org.ai.toolkit.aitk.config;

import java.io.File;
import org.ai.toolkit.aitk.common.util.PathUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
public class ResourceConfig extends WebMvcConfigurationSupport  {

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/", "classpath:/META-INF/");
        registry.addResourceHandler("/modelimage/**").addResourceLocations("classpath:/modelimage/");
        registry.addResourceHandler("/image/**").addResourceLocations("classpath:/image/");
        registry.addResourceHandler("/.attachment/**")
            .addResourceLocations("file:" + PathUtil.getParentUserDir() + File.separator + ".attachment/");
        super.addResourceHandlers(registry);
    }



}
