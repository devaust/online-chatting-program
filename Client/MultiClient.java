import java.net.*;
import java.io.IOException;

public class MultiClient {
    public static void main(String[] args) throws IOException{
        //1.�ͻ������ӷ�������,�����׽���Socket����
        //~ Socket socket=new Socket("free.idcfengye.com",10485);
        //~ Socket socket_voice=new Socket("free.idcfengye.com",10233);
        InetAddress localhost = InetAddress.getLocalHost();
        Socket socket=new Socket(localhost,1314);
        Socket socket_voice=new Socket(localhost,12333);
        Socket socket_file=new Socket(localhost,1315);
        
        //2.������ȡ����������Ϣ���̺߳ͷ��ͷ���������Ϣ���߳�
        Thread read=new Thread(new ClientReadServer(socket,socket_voice,socket_file));
        Thread send=new Thread(new ClientSendServer(socket,socket_voice,socket_file));
        //3.�����߳�
        read.start();
        send.start();
    }
}
