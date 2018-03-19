import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    //运行在服务端的ServerSocket
    private ServerSocket server;

    //存放所有娃娃机输出流的集合，用于传输指令
    private List<Device> devices;
    
    /**
     * 构造方法，用来初始化服务端
     */
    public Server(){
        try {
            devices = new ArrayList<>();
            /*
             * 初始化ServerSocket的同时需要指定服务端口
             * 该端口不能与当前系统使用TCP协议的其他程序
             * 申请的端口冲突，否则会抛出端口被占用的异常
             */
            server = new ServerSocket(8088);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void addDevice(Device device) {        // 同步添加设备
        devices.add(device);
    }

    private synchronized void removeDevice(Device device) {         // 同步移除设备
        devices.remove(device);
    }

    private synchronized void sendMessageToClient(String m){        // 同步发送指令
        for(Device devices : devices){

        }
    }

    private synchronized void sendMessageToAllClient(String m){
//        for(PrintWriter pw : allClientOut){
//            pw.println(m);
//        }
    }


    /**
     * 服务端开始工作的方法
     */
    public void start(){
        try {
            /*
             * Socket accept()
             * ServerSocket提供的该方法用来监听打开的
             * 服务端口(8088),该方法是一个阻塞方法，直到
             * 一个客户端尝试连接才会解除阻塞，并创建一个
             * Socket与刚连接的客户端进行通讯。
             *
             * accept方法每次调用都会等待一个客户端连接，
             * 所以若希望服务端能接受若干客户端的连接，就
             * 需要多次调用该方法，来分别获取对应这些客户
             * 端的Socket与他们通讯。
             *
             */
            while(true){
                System.out.println("等待客户端连接...");
                Socket socket = server.accept();
                System.out.println("一个客户端连接了!");
                /*
                 * 当一个客户端连接后，起动一个线程，来负责
                 * 与该客户端交互。
                 */
                ClientHandler handler = new ClientHandler(socket);
                Thread t = new Thread(handler);
                t.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }

    private class Device {

        private String id;
        private String address;
        private boolean type;       // 1: 娃娃机 0: App



        private PrintWriter Out;

        public Device(String id, String address, boolean type) {
            this.id = id;
            this.address = address;
            this.type = type;
        }


        public String getId() {
            return id;
        }

        public String getAddress() {
            return address;
        }

        public boolean isType() {
            return type;
        }

        public void setOut(PrintWriter out) {
            this.Out = out;
        }

        public PrintWriter getOut() {
            return Out;
        }

    }

    /**
     * 该线程用来与一个指定的客户端进行交互。
     * 每当一个客户端连接服务端后，都会起动
     * 当前线程来负责与之交互工作。
     * @author Administrator
     *
     */
    private class ClientHandler implements Runnable{
        //当前线程交互的客户端的Socket
        private Socket socket;

        //客户端的地址信息
        private String host;

        private String id;      // 设备ID

        private boolean type;       // 设备类型

        private Device device;

        public ClientHandler(Socket socket){
            this.socket = socket;
            //通过socket可以得知远端计算机信息
            InetAddress address = socket.getInetAddress();
            //获取远程计算机IP
            host = address.getHostAddress();
            device = new Device(id, host, type);
        }

        public void run() {
            PrintWriter pw = null;
            try {
                /*
                 * 通过客户端的Socket获取输出流，以便将
                 * 消息发送给客户端
                 */
                OutputStream out = socket.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(out,"UTF-8");

                pw = new PrintWriter(osw,true);

                //共享该客户端的输出流
                device.setOut(pw);
                addDevice(device);

                //广播该用户上线
                sendMessageToAllClient(host+"上线了");

                /*
                 * InputStream getInputStream()
                 * Socket提供的该方法用来获取输入流，读取
                 * 远端计算机发送过来的数据
                 */
                InputStream in = socket.getInputStream();

                InputStreamReader isr = new InputStreamReader(in,"UTF-8");

                BufferedReader br = new BufferedReader(isr);

                String message = null;
                /*
                 * 当我们使用BufferedReader读取来自远端计算机
                 * 发送过来的内容时，由于远端计算机的操作系统
                 * 不同，当他们断开连接时，这里readLine方法
                 * 的结果也不同:
                 * 当远端计算机操作系统是windows时，若断开
                 * 连接，这里的readLine方法直接会抛出异常。
                 * 当远端计算机操作系统是linux时，若断开连
                 * 接，这里的readLine方法返回null。
                 */
                while((message = br.readLine())!=null){
                    sendMessageToAllClient(host+"说:"+message);
                }

            } catch (Exception e) {

            } finally{
                /*
                 * 当该客户端与服务端断开连接时，应当将该客
                 * 户端的输出流从共享集合删除。
                 */
                removeDevice(device);

                //广播该用户下线
                sendMessageToAllClient(host+"下线了");

                /*
                 * 无论是linux的客户端，还是windows的
                 * 客户端，当与服务端断开连接后，都应当
                 * 将与该客户端交互的Socket关闭，来释放
                 * 底层资源。
                 */
                if(socket != null){
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }
}
