import java.net.*;
import java.io.IOException;

public class MultiClient {
    public static void main(String[] args) throws IOException{
        //1.客户端连接服务器端,返回套接字Socket对象
        //~ Socket socket=new Socket("free.idcfengye.com",10485);
        //~ Socket socket_voice=new Socket("free.idcfengye.com",10233);
        InetAddress localhost = InetAddress.getLocalHost();
        Socket socket=new Socket(localhost,1314);
        Socket socket_voice=new Socket(localhost,12333);
        Socket socket_file=new Socket(localhost,1315);
        
        //2.创建读取服务器端信息的线程和发送服务器端信息的线程
        Thread read=new Thread(new ClientReadServer(socket,socket_voice,socket_file));
        Thread send=new Thread(new ClientSendServer(socket,socket_voice,socket_file));
        //3.启动线程
        read.start();
        send.start();
    }
}
