import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }

    //用来与服务端通信的Socket
    private Socket socket;

    /**
     * 构造方法，用来初始化客户端
     * 构造方法常用来初始化对象属性等操作。
     */
    public Client() {
        try {
            /*
             * 初始化Socket时需要传入两个参数
             * 1:服务端的IP地址
             * 2:服务端的端口号
             */
            System.out.println("正在尝试连接服务端...");
            socket = new Socket("localhost", 8088);
            System.out.println("与服务端连接成功!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 客户端开始工作的方法
     */
    public void start() {
        try {
            /*
             * 当客户端启动后，就启动接收服务端发送过来
             * 消息的线程
             */
            GetServerMessageHandler handler = new GetServerMessageHandler();
            Thread t = new Thread(handler);
            t.start();

            Scanner scanner = new Scanner(System.in);

            /*
             * OutputStream getOutputStream()
             * Socket提供了该方法，用来获取输出流来向
             * 服务端发送数据。
             */
            OutputStream out = socket.getOutputStream();

            OutputStreamWriter osw = new OutputStreamWriter(out, "UTF-8");

            PrintWriter pw = new PrintWriter(osw, true);

            while (true) {
                String message = scanner.nextLine();
                pw.println(message);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 由于接收服务端发送过来的消息，与我们给服务端
     * 发送消息没有必然关系，所以两者应当在两个不同
     * 的线程上完成，各做各的，互不干涉。
     *
     * @author Administrator
     */
    private class GetServerMessageHandler implements Runnable {
        public void run() {
            try {
                /*
                 * 该线程的职责就是读取服务端发送过来的
                 * 每一条消息，并输出到控制台。
                 */
                InputStream in = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(in, "UTF-8");
                BufferedReader br = new BufferedReader(isr);

                String message = null;
                while ((message = br.readLine()) != null) {
                    System.out.println(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
