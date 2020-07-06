import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.net.URLEncoder;

class ClientReadServer implements Runnable{
    private Socket socket;
    private Socket socket_voice;
    private Socket socket_file;
    public ClientReadServer(Socket socket,Socket socket_voice,Socket socket_file){
        this.socket=socket;
        this.socket_voice=socket_voice;
        this.socket_file=socket_file;
    }
    @Override
    public void run() {
        try {             
			Scanner scanner=new Scanner(socket.getInputStream());
            ChartReceive listen=new ChartReceive(socket_voice);  
            FileReceive file_receive=new FileReceive(socket_file);        
            while(scanner.hasNext()){
				String msg = scanner.nextLine();
                System.out.println(msg);
                if(msg.startsWith("Voice_stream_on")){
					listen.start();
				}
				if(msg.startsWith("Voice_stream_off")){
					listen.kill();
				}
				if(msg.startsWith("Receiving_file")){
					file_receive.run();
				}
            }
            listen.kill();
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
