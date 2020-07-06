/** 
* 聊天时把自己的消息发送到对方 
* */  
import java.io.*; 
import java.net.*;  
  
import javax.sound.sampled.*;  
  
public class ChartSend extends Thread {  
	private TargetDataLine line;// 管道  
	  
	private byte[] data;// 存放每次从麦克获得的数据  
	
	private Socket socket;
	
	private boolean flag;
	// 格式  
	/** 
	* 脉冲编码调制：pcm、mu-law编码和a-law编码 
	* 信道数：单声道有一个信道，立体声有两个信道 
	* 样本速率：测量每信道、每秒钟采用的声压快照数，不管声道数是多少，速率都一样。 
	* 样本大小：指示用于存储每个快照的位数，典型值是8和16，对于16位样本，字节顺序很重要 
	* 每个样本中的字节或者以little-endian或者以big-endian样式排列。 
	* 对于PCM编码，帧是由在给定时间点上所有声道的样本集合组成，因此帧的大小总是等于样本大小成一声道数。 
	*  
	* 声道编码，每秒播放或者录制的样本数，声音样本中的位数，音频信道数，每秒播放或者录制的帧数 ， 
	* 以 big-endian 顺序还是 little-endian 顺序存储音频数据 
	* */  
	private AudioFormat format = new AudioFormat(  
		AudioFormat.Encoding.PCM_SIGNED, 44100.0f, 16, 1, 2, 44100.0f, false);  
	  
	/** 
	* @param toIp 发送目的地的ip 
	* */  
	public ChartSend(Socket socket) {  
		this.socket = socket;
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);  
		try {  
			line = (TargetDataLine) AudioSystem.getLine(info);  
		} catch (LineUnavailableException e) {  
			e.printStackTrace();  
		}  
	}  
	
	public void run() {  
		flag = true; 
		try {  
			line.open(format, line.getBufferSize());  
			line.start();  
		  
			int length = (int) (format.getFrameSize() * format.getFrameRate() / 1000.0f);  
			while (flag) {  
				data = new byte[length];  
				line.read(data, 0, data.length);  
				sendData();  
			}  
		} catch (LineUnavailableException e) {  
	  
			e.printStackTrace();  
		}  
	  
	}  

	public void kill(){
		flag = false;
	}
	  
	/** 
	 * 使用UDP协议传输声音 
	 */  
	private void sendData(){  
		try {  
			socket.getOutputStream().write(data,0,data.length);
			socket.getOutputStream().flush();
		} catch (SocketException e) {  
			e.printStackTrace();  
		} catch (IOException e) {  
			e.printStackTrace();  
		}  
	  
	}  
}  
