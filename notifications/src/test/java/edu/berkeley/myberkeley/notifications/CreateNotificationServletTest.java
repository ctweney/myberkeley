package edu.berkeley.myberkeley.notifications;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.SessionAdaptable;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessControlManager;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.api.lite.content.ContentManager;
import org.sakaiproject.nakamura.util.parameters.ContainerRequestParameter;

import java.io.IOException;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

public class CreateNotificationServletTest extends NotificationTests {

    private CreateNotificationServlet servlet;

    @Before
    public void setup() {
        this.servlet = new CreateNotificationServlet();
    }

    @Test
    public void badParam() throws ServletException, IOException {
        SlingHttpServletRequest request = mock(SlingHttpServletRequest.class);
        SlingHttpServletResponse response = mock(SlingHttpServletResponse.class);

        this.servlet.doPost(request, response);
        verify(response).sendError(Mockito.eq(HttpServletResponse.SC_BAD_REQUEST),
                Mockito.anyString());
    }

    @Test
    public void doPost() throws ServletException, IOException, StorageClientException, AccessDeniedException {
        SlingHttpServletRequest request = mock(SlingHttpServletRequest.class);
        SlingHttpServletResponse response = mock(SlingHttpServletResponse.class);

        String json = readNotificationFromFile();
        when(request.getRequestParameter(CreateNotificationServlet.POST_PARAMS.notification.toString())).thenReturn(
                new ContainerRequestParameter(json, "utf-8"));

        // sparse store
        Session session = mock(Session.class);
        ContentManager contentManager = mock(ContentManager.class);
        when(session.getContentManager()).thenReturn(contentManager);
        ResourceResolver resolver = mock(ResourceResolver.class);
        when(request.getResourceResolver()).thenReturn(resolver);

        // user's home dir
        Resource resource = mock(Resource.class);
        when(request.getResource()).thenReturn(resource);
        when(resource.adaptTo(Content.class)).thenReturn(new Content("/_user/home", new HashMap<String, Object>()));

        javax.jcr.Session jcrSession = mock(javax.jcr.Session.class, Mockito.withSettings().extraInterfaces(SessionAdaptable.class));
        when(((SessionAdaptable) jcrSession).getSession()).thenReturn(session);
        when(resolver.adaptTo(javax.jcr.Session.class)).thenReturn(jcrSession);

        AccessControlManager accessControlManager = mock(AccessControlManager.class);
        when(session.getAccessControlManager()).thenReturn(accessControlManager);

        String storePath = StorageClientUtils.newPath("/_user/home", Notification.STORE_NAME);
        when(contentManager.get(storePath)).thenReturn(
                new Content(storePath, new HashMap<String, Object>()));

        String notificationPath = StorageClientUtils.newPath(storePath, "b6455aa7-1cf4-4839-8a90-62dc352648f4");
        when(contentManager.get(notificationPath)).thenReturn(
                new Content(notificationPath, new HashMap<String, Object>()));

        this.servlet.doPost(request, response);

    }
}
