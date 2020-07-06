/** 
* ����ʱ���Լ�����Ϣ���͵��Է� 
* */  
import java.io.*; 
import java.net.*;  
  
import javax.sound.sampled.*;  
  
public class ChartSend extends Thread {  
	private TargetDataLine line;// �ܵ�  
	  
	private byte[] data;// ���ÿ�δ���˻�õ�����  
	
	private Socket socket;
	
	private boolean flag;
	// ��ʽ  
	/** 
	* ���������ƣ�pcm��mu-law�����a-law���� 
	* �ŵ�������������һ���ŵ����������������ŵ� 
	* �������ʣ�����ÿ�ŵ���ÿ���Ӳ��õ���ѹ�������������������Ƕ��٣����ʶ�һ���� 
	* ������С��ָʾ���ڴ洢ÿ�����յ�λ��������ֵ��8��16������16λ�������ֽ�˳�����Ҫ 
	* ÿ�������е��ֽڻ�����little-endian������big-endian��ʽ���С� 
	* ����PCM���룬֡�����ڸ���ʱ�������������������������ɣ����֡�Ĵ�С���ǵ���������С��һ�������� 
	*  
	* �������룬ÿ�벥�Ż���¼�Ƶ������������������е�λ������Ƶ�ŵ�����ÿ�벥�Ż���¼�Ƶ�֡�� �� 
	* �� big-endian ˳���� little-endian ˳��洢��Ƶ���� 
	* */  
	private AudioFormat format = new AudioFormat(  
		AudioFormat.Encoding.PCM_SIGNED, 44100.0f, 16, 1, 2, 44100.0f, false);  
	  
	/** 
	* @param toIp ����Ŀ�ĵص�ip 
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
	 * ʹ��UDPЭ�鴫������ 
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
