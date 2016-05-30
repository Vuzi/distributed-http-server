package fr.vuzi.http.impl;

import fr.vuzi.http.IHttpRequest;
import fr.vuzi.http.IHttpResponse;
import fr.vuzi.http.IHttpService;

import java.io.*;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class HttpServiceStaticFile implements IHttpService {

    private File folderPath;

    public HttpServiceStaticFile(String folderPath) throws IOException {
        this.folderPath = new File(folderPath);
        if(!this.folderPath.isDirectory())
            throw new IOException("The static directory does not exists or is not accessible");
    }

    @Override
    public void serve(IHttpRequest request, IHttpResponse response) {

        if(!request.getMethod().equals("GET")) {
            response.setStatus(404);
            return;
        }

        String path = request.getLocation();

        File resource = new File(folderPath, path);

        if(!resource.exists() || resource.isDirectory()) {
            response.setStatus(404);
        }

        // File
        Date modifiedSince = getDateFrom(request.getHeader("If-Modified-Since"));
        Date fileLastModified = new Date(resource.lastModified());

        if(modifiedSince != null && !fileLastModified.before(modifiedSince)) {
            response.setStatus(304);
        } else {
            response.setStatus(200);

            try {
                response.setHeader("Last-Modified", getFormattedDate(resource.lastModified()));
                response.setHeader("ContentType", URLConnection.guessContentTypeFromName(resource.getName()));
                response.setBody(new FileInputStream(resource));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            response.write();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String dateFormat = "EEE, dd MMM yyyy HH:mm:ss z";
    public static SimpleDateFormat dateFormatter;

    static {
        dateFormatter = new SimpleDateFormat(HttpServiceStaticFile.dateFormat, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private Date getDateFrom(String date) {
        if(date == null)
            return null;

        try {
            return dateFormatter.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    private String getFormattedDate(long timestamp) {
        return getFormattedDate(new Date(timestamp));
    }

    private String getFormattedDate(Date date) {
        return dateFormatter.format(date);
    }
}
