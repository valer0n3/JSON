import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

class Post {
    private int id;
    private String text;
    private List<Comment> commentaries = new ArrayList<>();

    private Post() {
    }

    public Post(int id, String text) {
        this.id = id;
        this.text = text;
    }

    public void addComment(Comment comment) {
        commentaries.add(comment);
    }

    public int getId() {
        return id;
    }
}

class Comment {
    private String user;
    private String text;

    private Comment() {
    }

    public Comment(String user, String text) {
        this.user = user;
        this.text = text;
    }
}

public class Practicum {
    private static final int PORT = 8080;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private static Gson gson = new Gson();
    private static List<Post> posts = new ArrayList<>();

    static {
        Post post1 = new Post(1, "Это первый пост, который я здесь написал.");
        post1.addComment(new Comment("Пётр Первый", "Я успел откомментировать первым!"));
        posts.add(post1);
        Post post2 = new Post(22, "Это будет второй пост. Тоже короткий.");
        posts.add(post2);
        Post post3 = new Post(333, "Это пока последний пост.");
        posts.add(post3);
    }

    public static void main(String[] args) throws IOException {
        HttpServer httpServer = HttpServer.create();
        httpServer.bind(new InetSocketAddress(PORT), 0);
        httpServer.createContext("/posts", new PostsHandler());
        httpServer.start();
        // тут конфигурирование и запуск сервера
        System.out.println("HTTP-сервер запущен на " + PORT + " порту!");
        //      httpServer.stop(1);
    }

    static class PostsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            Gson json = new Gson();
            String requestMethod = httpExchange.getRequestMethod();
            String path = httpExchange.getRequestURI().getPath();
            // System.out.println(path);
            String[] parsedPath = path.split("/");
            String jsonToSend = "";
            int id = 0;
            switch (requestMethod) {
                case "POST":
                    //do 3d task
                    try {
                        id = Integer.parseInt(parsedPath[2]);
                    } catch (NullPointerException e) {
                        //   System.out.println(e.getMessage());
                    }
                    Post existedPostFromJson = checkIfAPostsIsExists(id);
                    if (existedPostFromJson != null) {
                        InputStream inputStream = httpExchange.getRequestBody();
                        String fromJson = new String(inputStream.readAllBytes());
                        Comment newCommentFromJson = json.fromJson(fromJson, Comment.class);
                        // System.out.println(newCommentFromJson);
                        httpExchange.sendResponseHeaders(201, 0);
                        httpExchange.close();
                        // System.out.println(jsonToSend);
                        //add return JSON
                    } else {
                        //return 404 Not Found
                        httpExchange.sendResponseHeaders(404, 0);
                        httpExchange.close();
                    }
                    //   Comment
                    break;
                case "GET":
                    if (parsedPath.length <= 2) {
                        jsonToSend = json.toJson(posts);
                        //Add return JSON
                        //    System.out.println(jsonToSend);
                        httpExchange.sendResponseHeaders(200, 0);
                        try (OutputStream os = httpExchange.getResponseBody()) {
                            os.write(jsonToSend.getBytes());
                        }
                        // do 1-st task
                    } else if (parsedPath[3].equals("comments")) {
                        try {
                            id = Integer.parseInt(parsedPath[2]);
                        } catch (NullPointerException e) {
                            //        System.out.println(e.getMessage());
                        }
                        Post existedPost = checkIfAPostsIsExists(id);
                        if (existedPost != null) {
                            jsonToSend = json.toJson(existedPost);
                            httpExchange.sendResponseHeaders(200, 0);
                            try (OutputStream os = httpExchange.getResponseBody()) {
                                os.write(jsonToSend.getBytes());
                            }
                            // System.out.println(jsonToSend);
                            //add return JSON
                        } else {
                            //return 404 Not Found
                            httpExchange.sendResponseHeaders(404, 0);
                            httpExchange.close();
                        }
                    } else {
                        //Send that the request is not correct
                    }
                    break;
                default:
                    break;
            }
            //  httpExchange.close();
            // ваш код
        }

        public Post checkIfAPostsIsExists(int id) {
            for (Post onePost : posts) {
                if (onePost.getId() == id) {
                    //  System.out.println("ID found: " + onePost);
                    return onePost;
                }
            }
            return null;
        }
    }
}