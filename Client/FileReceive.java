/** 
* 聊天时把自己的消息发送到对方 
* */  
import java.io.*; 
import java.net.*;  
import java.util.Scanner;
import java.text.DecimalFormat;  
import java.math.RoundingMode;

public class FileReceive extends Thread {  
	
	private Socket socket_file;
	private static DecimalFormat df = null;
	static {
        // 设置数字格式，保留一位有效小数
        df = new DecimalFormat("#0.0");
        df.setRoundingMode(RoundingMode.HALF_UP);
        df.setMinimumFractionDigits(1);
        df.setMaximumFractionDigits(1);
    }
    
	public FileReceive(Socket socket_file) {  
		this.socket_file = socket_file;
	}  
	@Override
	public void run() {  
		try {  
            DataInputStream dis = new DataInputStream(socket_file.getInputStream());
            
            String fileName = dis.readUTF();
			long fileLength = dis.readLong();
			
			File directory = new File("FTCache");
			if(!directory.exists()) {
				directory.mkdir();
			}
			
			File file = new File(directory.getAbsolutePath() + File.separatorChar + fileName);
			FileOutputStream fos = new FileOutputStream(file);
			
			// 开始接收文件
			byte[] bytes = new byte[1024];
			int length = 0;
			long progress = 0;
			while((length = dis.read(bytes, 0, bytes.length)) != -1) {
				fos.write(bytes, 0, length);
				fos.flush();
				progress += length;
				int progress_bar = (int)(progress/(fileLength/50));
				System.out.print("\r当前进度：");
				for(int i=0;i<progress_bar;i++) System.out.print("|");
				System.out.print((100*progress/fileLength) + "%");
				if(progress>=fileLength) break;
			}
			System.out.println();
			System.out.println("[文件传输完成 File Name：" + fileName + ", Size：" 
				+ getFormatFileSize(fileLength) + "]");
			fos.close();

				
		} catch (Exception e) {  
			e.printStackTrace();  
		}
	  
	} 
	
	/**

     * 格式化文件大小

     * @param length

     * @return

     */

    private String getFormatFileSize(long length) {
        double size = ((double) length) / (1 << 30);
        if(size >= 1) {
            return df.format(size) + "GB";
        }
        size = ((double) length) / (1 << 20);
        if(size >= 1) {
            return df.format(size) + "MB";
        }
        size = ((double) length) / (1 << 10);
        if(size >= 1) {
            return df.format(size) + "KB";
        }
        return length + "B";
    }
}  
