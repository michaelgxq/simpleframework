package demo.pattern.template;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

public class TemplateDemo {
    public static void main(String[] args) {
        KTVRoom room1 = new RoomForChineseSinger();
        room1.procedure();
        KTVRoom room2 = new RoomForAmericanSinger();
        room2.procedure();
    }
}
