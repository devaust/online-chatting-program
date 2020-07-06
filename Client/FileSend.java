import java.net.*;  
import java.io.*;
import java.util.Scanner;
  
/**  
 
* �ѽ��յ�����Ϣ������ˣ������� 
 
*/  
  
  
public class FileSend extends Thread {  

	private Socket socket;
	public FileSend(Socket socket) {  
		this.socket=socket;

	}  
	@Override
	public void run() {  
		try {
            Scanner fscanner=new Scanner(socket.getInputStream());//��÷��������������תΪScanner����
            String info = fscanner.nextLine();
            System.out.println(info);
			fscanner=new Scanner(System.in);
			
			String path = fscanner.nextLine();
			File file = new File(path);
			
			if(file.exists()) {
				FileInputStream fis = new FileInputStream(file);
				DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
				
				// �ļ����ͳ���
				dos.writeUTF(file.getName());
				dos.flush();
				dos.writeLong(file.length());
				dos.flush();

				// ��ʼ�����ļ�
				System.out.println("[��ʼ�����ļ�]");
				byte[] bytes = new byte[1024];
				int length = 0;
				long progress = 0;
				while((length = fis.read(bytes, 0, bytes.length)) != -1) {
					dos.write(bytes, 0, length);
					dos.flush();
					progress += length;
					int progress_bar = (int)(progress/(file.length()/50));
					System.out.print("\r��ǰ���ȣ�");
					for(int i=0;i<progress_bar;i++) System.out.print("|");
					System.out.print((100*progress/file.length()) + "%");
				}
				System.out.println();
				System.out.println("[������ɹ�]");
				fis.close();
			}
        } catch (IOException e) {
            e.printStackTrace();
        }catch(Exception e){  
  
			e.printStackTrace();  
  
		} 

	}  

}  
