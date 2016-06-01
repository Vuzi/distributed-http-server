package fr.vuzi.http.impl;

import fr.vuzi.http.error.HttpException;
import fr.vuzi.http.request.IHttpRequest;
import fr.vuzi.http.request.IHttpResponse;
import fr.vuzi.http.service.IHttpService;

import java.io.*;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class HttpServiceStaticFile implements IHttpService {

    private File folderPath;

    public HttpServiceStaticFile(Map<String, String> parameters) throws IOException {
        this(parameters.get("path"));
    }

    public HttpServiceStaticFile(String folderPath) throws IOException {
        this.folderPath = new File(folderPath);
        if(!this.folderPath.isDirectory())
            throw new IOException("The static directory " + this.folderPath.getAbsolutePath() + " does not exists or is not " +
                    "accessible");
    }

    @Override
    public void serve(IHttpRequest request, IHttpResponse response) throws HttpException {
        // File
        String path = request.getLocation();
        File resource = new File(folderPath, path);

        if(!resource.exists())
            throw new HttpException(404, String.format("File %s not found", resource.getName()));
        else if(resource.isDirectory())
            showDirectory(resource, request, response);
        else
            uploadFile(resource, request, response);
    }

    private void uploadFile(File resource, IHttpRequest request, IHttpResponse response) {
        // Cache check
        Date modifiedSince = getDateFrom(request.getHeader("If-Modified-Since"));
        Date fileLastModified = new Date(resource.lastModified());

        if (modifiedSince != null && !fileLastModified.before(modifiedSince)) {
            response.setStatus(304);
        } else {
            response.setStatus(200);

            try {
                response.setBody(new FileInputStream(resource));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Set file information
        response.setHeader("Last-Modified", getFormattedDate(fileLastModified));
        response.setHeader("ContentType", URLConnection.guessContentTypeFromName(resource.getName()));
    }

    private void showDirectory(File resource, IHttpRequest request, IHttpResponse response) {
        StringBuilder sb = new StringBuilder();

        sb.append(
                "<html>" +
                "<head>" +
                "<title>" + resource.getName() + "</title>" +
                "</head>" +
                "<body>" +
                "<h1>" + (resource.getName().isEmpty() ? "/" : resource.getName()) + "</h1>" +
                getInner(resource.getParentFile(), resource) +
                "</body>" +
                "</html>");

        response.setHeader("ContentType", URLConnection.guessContentTypeFromName("text/html"));

        response.setBody(sb.toString());
    }

    private String getInner(File root, File dir) {
        StringBuilder sb = new StringBuilder();

        sb.append("<ul>");

        Path pathAbsolute = Paths.get(root.getAbsolutePath());

        for(File file : dir.listFiles()) {
            Path pathBase = Paths.get(file.getAbsolutePath());
            Path pathRelative = pathAbsolute.relativize(pathBase);

            if(file.isDirectory()) {
                sb.append("<li><a href=\"" + pathRelative.toString() + "\">" + file.getName() + "</a>");
                sb.append(getInner(root, file));
                sb.append("</li>");
            } else if (file.isFile()) {
                sb.append("<li><a href=\"" + pathRelative.toString() + "\">" + file.getName() + "</a></li>");
            }
        }

        sb.append("</ul>");
        return sb.toString();
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
