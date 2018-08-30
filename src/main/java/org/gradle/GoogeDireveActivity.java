/**
 * 
 */
package org.gradle;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import com.google.api.services.appsactivity.AppsactivityScopes;
import com.google.api.services.appsactivity.model.*;
import com.google.api.services.appsactivity.Appsactivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

/**
 * @author karunakar.medamoni
 *
 */
public class GoogeDireveActivity {

	 /** Application name. */
    private static final String APPLICATION_NAME =
        "G Suite Activity API Java Quickstart";

    /** Directory to store authorization tokens for this application. */
    private static final java.io.File DATA_STORE_DIR = new java.io.File("tokens");

    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY =
        JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

    /** Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/appsactivity-java-quickstart
     */
    private static final List<String> SCOPES = Arrays.asList(AppsactivityScopes.ACTIVITY);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in =
        		GoogeDireveActivity.class.getResourceAsStream("/client_secrets.json");
        GoogleClientSecrets clientSecrets =
            GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(DATA_STORE_FACTORY)
                .setAccessType("offline")
                .build();
        Credential credential = new AuthorizationCodeInstalledApp(
            flow, new LocalServerReceiver()).authorize("user");
        System.out.println(
                "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    /**
     * Build and return an authorized Apps Activity client service.
     * @return an authorized Appsactivity client service
     * @throws IOException
     */
    public static Appsactivity getAppsactivityService() throws IOException {
        Credential credential = authorize();
        return new Appsactivity.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static void main(String[] args) throws IOException {
        // Build a new authorized API client service.
        Appsactivity service = getAppsactivityService();

        // Print the recent activity in your Google Drive.
        ListActivitiesResponse result = service.activities().list()
             .setSource("drive.google.com")
             .setDriveAncestorId("root")
             .setPageSize(10)
             .execute();
        List<Activity> activities = result.getActivities();
        if (activities == null || activities.size() == 0) {
            System.out.println("No activity.");
        } else {
            System.out.println("Recent activity:");
            for (Activity activity : activities) {
                Event event = activity.getCombinedEvent();
                User user = event.getUser();
                Target target = event.getTarget();
                if (user == null || target == null ) {
                    continue;
                }
                String date = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                        .format(new java.util.Date(event.getEventTimeMillis().longValue()));
                System.out.printf("%s: %s, %s, %s (%s)\n",
                        date,
                        user.getName(),
                        event.getPrimaryEventType(),
                        target.getName(),
                        target.getMimeType());
            }
        }
    }

}
