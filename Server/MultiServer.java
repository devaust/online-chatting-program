import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.Socket;

public class MultiServer {
    public static void main(String[] args){
        try {
            //1.创建服务器端的ServerSocket对象,等待客户端连接
            ServerSocket serverSocket=new ServerSocket(1314);
            ServerSocket voiceSocket=new ServerSocket(12333);
            ServerSocket fileSocket=new ServerSocket(1315);
            //2.创建线程池,从而可以处理多个客户端
            ExecutorService executorService= Executors.newFixedThreadPool(20);
            for(int i=0;i<20;i++){
                System.out.println("欢迎来到我的聊天室......");
                //3.侦听客户端
                Socket socket=serverSocket.accept();
                Socket socket_v=voiceSocket.accept();
                Socket socket_f=fileSocket.accept();
                System.out.println("有新的朋友加入.....");
                //4.启动线程
                executorService.execute(new Server(socket,socket_v,socket_f));
            }
            //5.关闭线程池
            executorService.shutdown();
            //6.关闭服务器
            serverSocket.close();
            voiceSocket.close();
            fileSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
