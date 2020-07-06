import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

class ClientSendServer implements Runnable{
    private Socket socket;
    private Socket socket_voice;
    private Socket socket_file;
    public ClientSendServer(Socket socket,Socket socket_voice,Socket socket_file){
        this.socket=socket;
        this.socket_voice=socket_voice;
        this.socket_file=socket_file;
    }
    @Override
    public void run() {
        try {
            //1.获取服务器端的输出流
            PrintStream printStream=new PrintStream(socket.getOutputStream());
            //2.从键盘中输入信息
            Scanner scanner=new Scanner(System.in);
            ChartSend speak=new ChartSend(socket_voice);
            FileSend send_file=new FileSend(socket_file);
            while(true){
                String msg=null;
                if(scanner.hasNext()){
                    msg=scanner.nextLine();
                    printStream.println(msg);
                }
                try{
					if(msg.equals("exit")){
						scanner.close();
						printStream.close();
						speak.kill();
						break;
					}
					if(msg.startsWith("call:")){
						speak.start();
					}
					if(msg.startsWith("hangup")){
						speak.kill();
					}
					if(msg.startsWith("file:")){
						send_file.run();
					}
				}catch (Exception e){continue;}
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
