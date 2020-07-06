import java.net.*;  
import java.io.*;
import java.util.Scanner;
  
/**  
 
* 把接收到的信息传到麦克，即播放 
 
*/  
  
  
public class FileSend extends Thread {  

	private Socket socket;
	public FileSend(Socket socket) {  
		this.socket=socket;

	}  
	@Override
	public void run() {  
		try {
            Scanner fscanner=new Scanner(socket.getInputStream());//获得服务机的输入流并转为Scanner对象
            String info = fscanner.nextLine();
            System.out.println(info);
			fscanner=new Scanner(System.in);
			
			String path = fscanner.nextLine();
			File file = new File(path);
			
			if(file.exists()) {
				FileInputStream fis = new FileInputStream(file);
				DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
				
				// 文件名和长度
				dos.writeUTF(file.getName());
				dos.flush();
				dos.writeLong(file.length());
				dos.flush();

				// 开始传输文件
				System.out.println("[开始传输文件]");
				byte[] bytes = new byte[1024];
				int length = 0;
				long progress = 0;
				while((length = fis.read(bytes, 0, bytes.length)) != -1) {
					dos.write(bytes, 0, length);
					dos.flush();
					progress += length;
					int progress_bar = (int)(progress/(file.length()/50));
					System.out.print("\r当前进度：");
					for(int i=0;i<progress_bar;i++) System.out.print("|");
					System.out.print((100*progress/file.length()) + "%");
				}
				System.out.println();
				System.out.println("[件传输成功]");
				fis.close();
			}
        } catch (IOException e) {
            e.printStackTrace();
        }catch(Exception e){  
  
			e.printStackTrace();  
  
		} 

	}  

}  
