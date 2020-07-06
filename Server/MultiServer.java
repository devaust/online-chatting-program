import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.Socket;

public class MultiServer {
    public static void main(String[] args){
        try {
            //1.�����������˵�ServerSocket����,�ȴ��ͻ�������
            ServerSocket serverSocket=new ServerSocket(1314);
            ServerSocket voiceSocket=new ServerSocket(12333);
            ServerSocket fileSocket=new ServerSocket(1315);
            //2.�����̳߳�,�Ӷ����Դ������ͻ���
            ExecutorService executorService= Executors.newFixedThreadPool(20);
            for(int i=0;i<20;i++){
                System.out.println("��ӭ�����ҵ�������......");
                //3.�����ͻ���
                Socket socket=serverSocket.accept();
                Socket socket_v=voiceSocket.accept();
                Socket socket_f=fileSocket.accept();
                System.out.println("���µ����Ѽ���.....");
                //4.�����߳�
                executorService.execute(new Server(socket,socket_v,socket_f));
            }
            //5.�ر��̳߳�
            executorService.shutdown();
            //6.�رշ�����
            serverSocket.close();
            voiceSocket.close();
            fileSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
