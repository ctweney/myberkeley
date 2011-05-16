package edu.berkeley.myberkeley.dynamiclist;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableMap;
import edu.berkeley.myberkeley.api.dynamiclist.DynamicListService;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.jcr.resource.JcrResourceConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sakaiproject.nakamura.api.lite.Repository;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.api.lite.content.ContentManager;
import org.sakaiproject.nakamura.lite.BaseMemoryRepository;
import org.sakaiproject.nakamura.lite.RepositoryImpl;
import org.sakaiproject.nakamura.lite.content.ContentManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletException;

public class DynamicListGetServletTest extends Assert {

  private static final Logger LOGGER = LoggerFactory.getLogger(DynamicListGetServletTest.class);

  @Mock
  private SlingHttpServletRequest request;

  @Mock
  private SlingHttpServletResponse response;

  private ContentManager contentManager;

  public DynamicListGetServletTest() throws AccessDeniedException, StorageClientException, ClassNotFoundException {
    MockitoAnnotations.initMocks(this);
    BaseMemoryRepository baseMemoryRepository = new BaseMemoryRepository();
    Repository repository = baseMemoryRepository.getRepository();
    this.contentManager = repository.loginAdministrative().getContentManager();
  }

  @Test
  public void infiniteGet() throws IOException, ServletException, StorageClientException, JSONException, AccessDeniedException {
    DynamicListGetServlet servlet = new DynamicListGetServlet();

    RequestPathInfo pathInfo = mock(RequestPathInfo.class);
    when(pathInfo.getSelectors()).thenReturn(new String[]{"tidy", "infinity"});
    when(this.request.getRequestPathInfo()).thenReturn(pathInfo);

    Content content = new Content("/my/list/path", ImmutableMap.of(
            JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY,
            (Object) DynamicListService.DYNAMIC_LIST_RT));
    this.contentManager.update(content);

    Content child1 = new Content("/my/list/path/child1", ImmutableMap.of(
            "child1prop1",
            (Object) "prop1val"));
    this.contentManager.update(child1);

    // we have to get the content via contentmanager so that it gets properly set up with internalize(),
    // or else the ExtendedJSONWriter call will fail. Ian insists this is not a code smell.
    Content contentFromCM = this.contentManager.get("/my/list/path");
    Resource resource = mock(Resource.class);
    when(request.getResource()).thenReturn(resource);
    when(resource.adaptTo(Content.class)).thenReturn(contentFromCM);

    ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
    when(response.getWriter()).thenReturn(new PrintWriter(responseStream));

    servlet.doGet(this.request, this.response);

    JSONObject json = new JSONObject(responseStream.toString("utf-8"));
    assertNotNull(json);
    LOGGER.info(json.toString(2));
  }

}