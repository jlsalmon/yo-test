package cern.modesti.plugin;

import cern.modesti.ModestiServer;
import cern.modesti.request.Request;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * @author Justin Lewis Salmon
 */
@RestController
@Slf4j
public class PluginAssetController {

  @Autowired
  private PluginRegistry<RequestProvider, Request> requestProviderRegistry;

  @Autowired
  private Environment environment;

  private ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(Thread.currentThread().getContextClassLoader());

  private ObjectMapper mapper = new ObjectMapper();

  @RequestMapping("/api/plugins")
  public VersionDescriptor getPluginInfo() throws IOException {
    String version = ModestiServer.class.getPackage().getImplementationVersion();
    if (version == null) version = "dev";

    // TODO: read plugin versions and insert them here

    return new VersionDescriptor(version, Collections.EMPTY_LIST);
  }

  @RequestMapping("/api/plugins/{id}/assets")
  public List<String> getAssetsForPlugin(@PathVariable("id") String id, HttpServletRequest request) throws IOException, URISyntaxException {
    RequestProvider plugin = getPlugin(id);

    LinkedList<String> assets = new LinkedList<>();
    List<String> javascriptAssets = new ArrayList<>();
    String host;

    String forwardedHost = request.getHeader("X-Forwarded-Host");
    if (forwardedHost != null) {
      host = "https://" + forwardedHost;
    } else {
      host = request.getRequestURL().substring(0, StringUtils.ordinalIndexOf(request.getRequestURL(), "/", 3));
    }

    for (Resource resource : resolver.getResources("classpath*:/static/assets.json")) {
      if (resourceBelongsToPlugin(resource, plugin)) {
        log.trace("found asset descriptor for plugin {}: {}", plugin.getMetadata().getId(), resource.getURL());
        javascriptAssets = mapper.readValue(resource.getInputStream(), mapper.getTypeFactory().constructCollectionType(List.class, String.class));
      }
    }

    assets.addAll(javascriptAssets.stream().map(javascriptAsset -> host + '/' + javascriptAsset).collect(Collectors.toList()));

    // The AngularJS module descriptor must be in the root directory and be
    // named "<lowercase domain id>.js"
    String filename = plugin.getMetadata().getId().toLowerCase().replaceAll(" ", "-") + ".js";

    // FIXME: HACK ALERT
    if (plugin.getMetadata().getId().contains("WinCC OA") || plugin.getMetadata().getId().contains("WINCCOA")) {
      filename = "winccoa-cv.js";
    }

    Resource moduleDescriptor = resolver.getResource("classpath*:/static/" + filename);

    for (Resource resource : resolver.getResources("classpath*:/static/**")) {
      if (resourceBelongsToPlugin(resource, plugin) && !resource.getFilename().equals(moduleDescriptor.getFilename())) {
        log.trace("found resource for plugin {}: {}", plugin.getMetadata().getId(), resource.getURL());

        if (FilenameUtils.isExtension(resource.getFilename(), new String[]{"js", "html", "css"})) {
          String path = host + '/' + resource.getURL().getPath().split("static/")[1];

          if (!assets.contains(path)) {
            assets.add(path);
          }
        }
      }
    }

    assets.add(host + '/' + moduleDescriptor.getFilename());
    return assets;
  }

  
  // TODO: Duplicated from above method!!
  @RequestMapping("/api/plugins/{id}/search-assets")
  public List<String> getSearchAssetsForPlugin(@PathVariable("id") String id, HttpServletRequest request) throws IOException, URISyntaxException {
    RequestProvider plugin = getPlugin(id);

    LinkedList<String> assets = new LinkedList<>();
    List<String> javascriptAssets = new ArrayList<>();
    String host;

    String forwardedHost = request.getHeader("X-Forwarded-Host");
    if (forwardedHost != null) {
      host = "https://" + forwardedHost;
    } else {
      host = request.getRequestURL().substring(0, StringUtils.ordinalIndexOf(request.getRequestURL(), "/", 3));
    }

    for (Resource resource : resolver.getResources("classpath*:/static/search-assets.json")) {
      if (resourceBelongsToPlugin(resource, plugin)) {
        log.trace("found search asset descriptor for plugin {}: {}", plugin.getMetadata().getId(), resource.getURL());
        javascriptAssets = mapper.readValue(resource.getInputStream(), mapper.getTypeFactory().constructCollectionType(List.class, String.class));
      }
    }

    assets.addAll(javascriptAssets.stream().map(javascriptAsset -> host + '/' + javascriptAsset).collect(Collectors.toList()));

    // The AngularJS module descriptor must be in the root directory and be
    // named "<lowercase domain id>.js"
    String filename = plugin.getMetadata().getId().toLowerCase().replaceAll(" ", "-") + ".js";

    // FIXME: HACK ALERT
    if (plugin.getMetadata().getId().contains("WinCC OA") || plugin.getMetadata().getId().contains("WINCCOA")) {
      filename = "winccoa-cv.js";
    }

    Resource moduleDescriptor = resolver.getResource("classpath*:/static/" + filename);
/*
    for (Resource resource : resolver.getResources("classpath*:/static/**")) {
      if (resourceBelongsToPlugin(resource, plugin) && !resource.getFilename().equals(moduleDescriptor.getFilename())) {
        log.trace("found resource for plugin {}: {}", plugin.getMetadata().getId(), resource.getURL());

        if (FilenameUtils.isExtension(resource.getFilename(), new String[]{"js", "html", "css"})) {
          String path = host + '/' + resource.getURL().getPath().split("static/")[1];

          if (!assets.contains(path)) {
            assets.add(path);
          }
        }
      }
    }*/

    assets.add(host + '/' + moduleDescriptor.getFilename());
    return assets;
  }
  
  
  private RequestProvider getPlugin(String id) {
    for (RequestProvider provider : requestProviderRegistry.getPlugins()) {
      if (provider.getMetadata().getId().equals(id)) {
        return provider;
      }
    }

    throw new UnsupportedRequestException(format("no plugin found for domain %s", id));
  }

  private boolean resourceBelongsToPlugin(Resource resource, RequestProvider plugin) throws IOException, URISyntaxException {
    String pluginRootPath =  plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();

    if (!pluginRootPath.contains(".jar")) {
      pluginRootPath = new File(pluginRootPath).getParentFile().getParentFile().getAbsolutePath();
    } else {
      pluginRootPath = pluginRootPath.replace("file:", "").replace("!/", "");
    }

    URLConnection urlConnection = resource.getURL().openConnection();
    String resourceRootPath;

    if (urlConnection instanceof JarURLConnection) {
      resourceRootPath = ((JarURLConnection) urlConnection).getJarFileURL().toURI().getPath();
    } else {
      resourceRootPath = resource.getFile().getParentFile().getParentFile().getAbsolutePath();
    }

    return resourceRootPath != null && resourceRootPath.contains(pluginRootPath);
  }
}
