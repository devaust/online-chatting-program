import java.net.*;  
  
import javax.sound.sampled.*;  
  
import java.io.*;

  
/**  
 
* 把接收到的信息传到麦克，即播放 
 
*/  
  
  
public class ChartReceive extends Thread {  
  
   //格式  
	private AudioFormat format = new AudioFormat(
		AudioFormat.Encoding.PCM_SIGNED, 44100.0f, 16, 1, 2, 44100.0f, false);  
   //管道  
	private SourceDataLine line;  
	private byte[] data;  

	private Socket socket;
	private boolean flag;
	public ChartReceive(Socket socket) {  
		this.socket=socket;
		try {  
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);  
			line = (SourceDataLine) AudioSystem.getLine(info);  
		} catch (Exception e) {  
			e.printStackTrace();  
		}  

	}  
	public void kill(){
		flag = false;
	}
	@Override
	public void run() {  
		System.out.println("receive threading start");  
		int length=(int)(format.getFrameSize()*format.getFrameRate()/1000.0f);  
		flag = true;
		try {
            line.open(format);
			line.start();  
			
			int len = -1;
			InputStream inputStream = socket.getInputStream();
			data=new byte[length];
			
            while((len = inputStream.read(data)) != -1&&flag){
				line.write(data,0,data.length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }catch(Exception e){  
  
			e.printStackTrace();  
  
		} 

	}  

}  
