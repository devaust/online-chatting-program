import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sound.sampled.*; 
import java.text.DecimalFormat;
import java.math.RoundingMode;

class Server implements Runnable{
	
    private static Map<String,Socket> map = new ConcurrentHashMap<>();
    private static Map<String,Socket> map1 = new ConcurrentHashMap<>();
    private static Map<String,Socket> map2 = new ConcurrentHashMap<>();
    private static ArrayList<Group> groups = new ArrayList<Group>();
    private static int newest_group_ID = 0; 
    
    private Socket socket;
    private Socket socket_v;
    private Socket socket_f;
	private AudioStream audioStream;
    private static DecimalFormat df = null;
    static {
        // �������ָ�ʽ������һλ��ЧС��
        df = new DecimalFormat("#0.0");
        df.setRoundingMode(RoundingMode.HALF_UP);
        df.setMinimumFractionDigits(1);
        df.setMaximumFractionDigits(1);
    }
    
    
    public Server(Socket socket,Socket socket_v,Socket socket_f){
        this.socket=socket;
        this.socket_v = socket_v;
        this.socket_f = socket_f;
    }
    @Override
    public void run() {
        //1.��ȡ�ͻ��˵�������
        try {
            Scanner scanner=new Scanner(socket.getInputStream());
            String msg=null;
            while(true){
                if(scanner.hasNextLine()){
                    //0.����ͻ���������ַ���
                    msg=scanner.nextLine();

                    //1.ע���û�����,ע���û��ĸ�ʽΪ:userName:�û���
                    if(msg.startsWith("userName:")){
                        //���û���������userName��
                        String userName=msg.split("\\:")[1];
                        //ע����û�
                        userRegist(userName,socket,socket_v,socket_f);
                        continue;
                    }
                    //2.Ⱥ����Ϣ����,Ⱥ�ĵĸ�ʽΪ:G:Ⱥid:Ⱥ����Ϣ
                    else if(msg.startsWith("G:")){
                        //������ע��ſ���!
                        if(!firstStep(socket)) continue;
                        //����Ⱥ����Ϣ
                        int id = Integer.valueOf(msg.split("\\:")[1]);
                        String str=msg.split("\\:")[2];
                        //����Ⱥ����Ϣ
                        groupChat(socket,str,id);
                        continue;
                    }
                    //3.˽����Ϣ����,˽�ĵĸ�ʽΪ:P:userName-˽����Ϣ
                    else if(msg.startsWith("P:")&&msg.contains("-")){
                        //������ע��ſ���!
                        if(!firstStep(socket)) continue;
                        //������Ҫ˽�ĵ��û���
                        String userName=msg.split("\\:")[1].split("-")[0];
                        //����˽�ĵ���Ϣ
                        String str=msg.split("\\:")[1].split("-")[1];
                        //����˽����Ϣ
                        privateChat(socket,userName,str);
                        continue;
                    }
                    //4.�û��˳�����,�û��˳���ʽΪ:����exit
                    else if(msg.contains("exit")){
                        //������ע��ſ���!
                        if(!firstStep(socket)) continue;
                        if(!firstStep1(socket_v)) continue;
                        userExit(socket,socket_v);
                        continue;
                    }
                    //5.����Ⱥ��,��ʽΪ:create:name
                    else if(msg.startsWith("create:")){
						if(!firstStep(socket)) continue;
						String name=msg.split("\\:")[1];
						createGroup(socket,name);
                        continue;
					}
					//6.����Ⱥ��,��ʽΪ:join:id
                    else if(msg.startsWith("join:")){
						if(!firstStep(socket)) continue;
						String id=msg.split("\\:")[1];
						joinGroup(socket,Integer.valueOf(id));
                        continue;
					}
					//7.�˳�Ⱥ��,��ʽΪ:quit:id
                    else if(msg.startsWith("quit:")){
						if(!firstStep(socket)) continue;
						String id=msg.split("\\:")[1];
						quitGroup(socket,Integer.valueOf(id));
                        continue;
					}
					//8.�г�����Ⱥ��,��ʽΪ:listGroup
                    else if(msg.startsWith("listGroup")){
						if(!firstStep(socket)) continue;
						listGroup(socket);
                        continue;
					}
					//9.������������,��ʽΪ:call:userName
                    else if(msg.startsWith("call:")){
						if(!firstStep1(socket_v)) continue;
						String userName=msg.split("\\:")[1];
						voiceChat(socket,userName);
                        continue;
					}
					//10.�ر���������,��ʽΪ:hangup
                    else if(msg.startsWith("hangup")){
						hangUp();
                        continue;
					}
					//11.�ļ�����,��ʽΪ:file:�û���-�ļ�·��
                    else if(msg.startsWith("file:")){
                        //�����û���
                        String userName=msg.split("\\:")[1].split("-")[0];
                        //�����ļ�
                        transFile(socket_f,userName);
                        continue;
					}else if(msg.equals("P")){
                        continue;
					}
                    //���������ʽ������
                    else{
                        PrintStream printStream=new PrintStream(socket.getOutputStream(),true,"GBK");
                        printStream.println("�����ʽ����!�밴�����¸�ʽ����!");
                        printStream.println("ע���û���ʽ:[userName:�û���]");
                        printStream.println("Ⱥ�ĸ�ʽ:[G:Ⱥid:Ⱥ����Ϣ]");
                        printStream.println("˽�ĸ�ʽ:[P:userName-˽����Ϣ]");
                        printStream.println("����Ⱥ�ĸ�ʽ:[create:Ⱥ����]");
                        printStream.println("����Ⱥ�ĸ�ʽ:[join:Ⱥ��id]");
                        printStream.println("�˳�Ⱥ�ĸ�ʽ:[quit:Ⱥ��id]");
                        printStream.println("�г�����Ⱥ�ĸ�ʽ:[listGroup]");
                        printStream.println("�û��˳���ʽ:[����exit����]");
                        printStream.println("���������ʽ:[call:�û���]");
                        printStream.println("�Ҷ����������ʽ:[hangup]");
                        printStream.println("�ļ������ʽ:[file:�û���]");
                        continue;
                    }
                }
				
					
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
    /**
     * ��һ��������ע��!
     * @param socket ��ǰ�ͻ���
     */
    private boolean firstStep(Socket socket) throws IOException {
        Set<Map.Entry<String,Socket>> set=map.entrySet();
        for(Map.Entry<String,Socket> entry:set){
            if(entry.getValue().equals(socket)){
                if(entry.getKey()==null){
                    PrintStream printStream=new PrintStream(socket.getOutputStream(),true,"GBK");
                    printStream.println("���Ƚ���ע�������");
                    printStream.println("ע���ʽΪ:[userName:�û���]");
                    return false;
                }
                return true;
            }
        }
        PrintStream printStream=new PrintStream(socket.getOutputStream(),true,"GBK");
        printStream.println("���Ƚ���ע�������");
        printStream.println("ע���ʽΪ:[userName:�û���]");
        return false;
    }
    
    private boolean firstStep1(Socket socket) throws IOException {
        Set<Map.Entry<String,Socket>> set1=map1.entrySet();
        for(Map.Entry<String,Socket> entry:set1){
            if(entry.getValue().equals(socket)){
                if(entry.getKey()==null){
                    PrintStream printStream=new PrintStream(socket.getOutputStream(),true,"GBK");
                    printStream.println("���Ƚ���ע�������");
                    printStream.println("ע���ʽΪ:[userName:�û���]");
                    return false;
                }
                return true;
            }
        }
        PrintStream printStream=new PrintStream(socket.getOutputStream(),true,"GBK");
        printStream.println("���Ƚ���ע�������");
        printStream.println("ע���ʽΪ:[userName:�û���]");
        return false;
    }
    
    private boolean firstStep2(Socket socket) throws IOException {
        Set<Map.Entry<String,Socket>> set1=map2.entrySet();
        for(Map.Entry<String,Socket> entry:set1){
            if(entry.getValue().equals(socket)){
                if(entry.getKey()==null){
                    PrintStream printStream=new PrintStream(socket.getOutputStream(),true,"GBK");
                    printStream.println("���Ƚ���ע�������");
                    printStream.println("ע���ʽΪ:[userName:�û���]");
                    return false;
                }
                return true;
            }
        }
        PrintStream printStream=new PrintStream(socket.getOutputStream(),true,"GBK");
        printStream.println("���Ƚ���ע�������");
        printStream.println("ע���ʽΪ:[userName:�û���]");
        return false;
    }
    
    /**
     * ע���û���Ϣ
     * @param userName �û���
     * @param socket �û��ͻ���Socket����
     */
    private void userRegist(String userName,Socket socket,Socket socket_v,Socket socket_f){
        map.put(userName,socket);
        map1.put(userName,socket_v);
        map2.put(userName,socket_f);
        System.out.println("[�û���Ϊ"+userName+"][�ͻ���Ϊ"+socket+"]������!");
        System.out.println("��ǰ��������Ϊ:"+map.size()+"��");
    }
 
	/**
	 * ����Ⱥ��
	 * @param socket ����Ⱥ�ĵĿͻ���
	 * @param name Ⱥ������
	 */
	private void createGroup(Socket socket,String name){
		newest_group_ID ++;
		Group temp = new Group(newest_group_ID,name);
		temp.addMember(socket);
		groups.add(temp);
		System.out.println(""+socket+"������Ⱥ�ģ�"+name+"-"+newest_group_ID);
	}
	 
	/**
     * ����Ⱥ��
     * @param socket �������Ⱥ�ĵĿͻ���
     * @param id Ⱥ��id
     */
	private boolean joinGroup(Socket socket,int id) throws IOException {
		for(Group group : groups){
			if(group.getID()==id){
				group.addMember(socket);
				return true;
			}
		}
		PrintStream printStream=new PrintStream(socket.getOutputStream(),true,"GBK");
		printStream.println("��ǰȺ�Ĳ�����");
		return false;
	}
	 
	/**
     * �˳�Ⱥ��
     * @param socket �����˳�Ⱥ�ĵĿͻ���
     * @param id Ⱥ��id
     */
	private boolean quitGroup(Socket socket,int id) throws IOException {
		for(Group group : groups){
			if(group.getID()==id){
				if(group.containsMember(socket))
					group.delMember(socket);
				else{
					PrintStream printStream=new PrintStream(socket.getOutputStream(),true,"GBK");
					printStream.println("�㲻�ڵ�ǰȺ����");
				}
				if(group.checkEmpty())
					groups.remove(groups.indexOf(group));
				return true;
			}
		}
		PrintStream printStream=new PrintStream(socket.getOutputStream(),true,"GBK");
		printStream.println("��ǰȺ�Ĳ�����");
		return false;
	}
	
	/**
     * �о��û������Ⱥ��
     * @param socket �о�Ⱥ�ĵĿͻ���
     */
	private void listGroup(Socket socket) throws IOException {
		String result = "";
		for(Group group : groups){
			if(group.containsMember(socket)){
				result = result+"Ⱥ�ţ�"+group.getID()+"��"+"Ⱥ����"+group.getName()+"\n";
			}
		}
		PrintStream printStream=new PrintStream(socket.getOutputStream(),true,"GBK");
		printStream.println(result);
	}
	
    /**
     * Ⱥ������(��Map����ת��ΪSet����,�Ӷ�ȡ��ÿ���ͻ���Socket,��Ⱥ����Ϣ���͸�ÿ���ͻ���)
     * @param socket ����Ⱥ�ĵĿͻ���
     * @param msg Ⱥ����Ϣ
     */
    private void groupChat(Socket socket,String msg,int id) throws IOException {
		boolean flag = false;
		Group current_group=new Group();
		for(Group group : groups){
			if(group.getID()==id){
				current_group = group;
				if(!group.containsMember(socket)){
					PrintStream printStream=new PrintStream(socket.getOutputStream(),true,"GBK");
					printStream.println("�㲻�ڵ�ǰȺ����");
					return;
				}
				flag = true;
			}
		}
		if(!flag){
			PrintStream printStream=new PrintStream(socket.getOutputStream(),true,"GBK");
			printStream.println("��ǰȺ�Ĳ�����");
			return;	
		}		
				
        //1.��Map����ת��ΪSet����
        Set<Map.Entry<String,Socket>> set=map.entrySet();
        //2.����Set�����ҵ�����Ⱥ����Ϣ���û�
        String userName=null;
        for(Map.Entry<String,Socket> entry:set){
            if(entry.getValue().equals(socket)){
                userName=entry.getKey();
                break;
            }
        }
        //3.����Set���Ͻ�Ⱥ����Ϣ����ÿһ���ͻ���
        for(Map.Entry<String,Socket> entry:set){
            //ȡ�ÿͻ��˵�Socket����
            Socket client=entry.getValue();
            //ȡ��client�ͻ��˵������
            if(current_group.containsMember(client)){
				PrintStream printStream=new PrintStream(client.getOutputStream(),true,"GBK");
				printStream.println(userName+"Ⱥ��˵:"+msg);
			}
        }
    }
    
    /**
     * ˽������(����userNameȡ�ÿͻ��˵�Socket����,�Ӷ�ȡ�ö�Ӧ�����,��˽����Ϣ���͵�ָ���ͻ���)
     * @param socket ��ǰ�ͻ���
     * @param userName ˽�ĵ��û���
     * @param msg ˽�ĵ���Ϣ
     */
    private void privateChat(Socket socket,String userName,String msg) throws IOException {
        //1.ȡ�õ�ǰ�ͻ��˵��û���
        String curUser=null;
        Set<Map.Entry<String,Socket>> set=map.entrySet();
        for(Map.Entry<String,Socket> entry:set){
            if(entry.getValue().equals(socket)){
                curUser=entry.getKey();
                break;
            }
        }
        //2.ȡ��˽���û�����Ӧ�Ŀͻ���
        Socket client=map.get(userName);
        //3.��ȡ˽�Ŀͻ��˵������,��˽����Ϣ���͵�ָ���ͻ���
        PrintStream printStream=new PrintStream(client.getOutputStream(),true,"GBK");
        printStream.println(curUser+"˽��˵:"+msg);
    }
 
	/**
     * ����˽������(����userNameȡ�ÿͻ��˵�Socket����,�Ӷ�ȡ�ö�Ӧ�����,��˽����Ϣ���͵�ָ���ͻ���)
     * @param socket ��ǰ�ͻ���
     * @param userName ˽�ĵ��û���
     */
    private void voiceChat(Socket socket,String userName) throws IOException {
        //1.ȡ��˽���û�����Ӧ�Ŀͻ���
        Socket client=map.get(userName);
        Socket client1=map1.get(userName);
        
        //2.��ȡ˽�Ŀͻ��˵������,����־��Ϣ���͵�ָ���ͻ���
		PrintStream printStream=new PrintStream(client.getOutputStream());
        printStream.println("Voice_stream_on");
        
        audioStream = new AudioStream();
        audioStream.setPipe(socket_v,client1,client);
        audioStream.start();
    }
    
    /**
     * ����˽�ĹҶ�
     */
    private void hangUp() throws IOException {
        audioStream.kill();
    }
    
    /**
     * �ļ�����
     * @param socket ��ǰ�ͻ���
     * @param userName ˽�ĵ��û���
     * @param msg �����ļ�·��
     */
    private void transFile(Socket socket,String userName) throws IOException {
        //1.ȡ�õ�ǰ�ͻ��˵��û���
        String curUser=null;
        Set<Map.Entry<String,Socket>> set=map2.entrySet();
        for(Map.Entry<String,Socket> entry:set){
            if(entry.getValue().equals(socket)){
                curUser=entry.getKey();
                break;
            }
        }
        
        //2.ȡ��Ŀ���û�����Ӧ�Ŀͻ���
        Socket client=map2.get(userName);
        Socket client_msg=map.get(userName);
        
        //����ͨ��ͨ����ǿ���������
        PrintStream writer1 = new PrintStream(socket.getOutputStream(), true,"GBK");
        PrintStream writer2 = new PrintStream(client_msg.getOutputStream(), true,"GBK");
        
        writer1.println("�����ļ�·����");
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        writer2.println("Receiving_file");
        DataOutputStream dos = new DataOutputStream(client.getOutputStream());
        
		// �ļ����ͳ���
		String fileName = dis.readUTF();
		long fileLength = dis.readLong();
		
		dos.writeUTF(fileName);//
		dos.flush();//
		dos.writeLong(fileLength);//
		dos.flush();//


		// ��ʼ�����ļ�
		byte[] bytes = new byte[1024];
		int length = 0;
		long progress = 0;
		while((length = dis.read(bytes, 0, bytes.length)) != -1) {
			dos.write(bytes, 0, length);
			dos.flush();
			progress += length;
			int progress_bar = (int)(progress/(fileLength/50));
			System.out.print("\r��ǰ���ȣ�");
			for(int i=0;i<progress_bar;i++) System.out.print("|");
			System.out.print((100*progress/fileLength) + "%");
			if(progress>=fileLength) break;
		}
		System.out.println();
		System.out.println("[�ļ���ת��� File Name��" + fileName + ", Size��" 
			+ getFormatFileSize(fileLength) + "]");
    }
    
    /**
     * �û��˳�
     * @param socket
     */
    private void userExit(Socket socket,Socket socket_v){
        //1.����socketȡ�ö�Ӧ��Keyֵ
        String userName=null;
        String userName1=null;
        for(String key:map.keySet()){
            if(map.get(key).equals(socket)){
                userName=key;
                break;
            }
        }
        for(String key:map1.keySet()){
            if(map1.get(key).equals(socket_v)){
                userName1=key;
                break;
            }
        }
        //2.��userName,SocketԪ�ش�map������ɾ��
        if(!map.isEmpty()) map.remove(userName,socket);
        if(!map.isEmpty()) map1.remove(userName1,socket_v);
        //3.���ѷ������ÿͻ���������
        if(userName != null) System.out.println("�û�:"+userName+"������!");
    }
    
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
    
    private class Group{
		private int id;
		private String group_name;
		private ArrayList<Socket> member;
		
		public Group()
		{
			id = -1;
			group_name = "None";
			member = new ArrayList<Socket>();
		}
		public Group(int id,String group_name)
		{
			this.id = id;
			this.group_name = group_name;
			member = new ArrayList<Socket>();
		}
		private void addMember(Socket socket){
			member.add(socket);
		}
		private boolean delMember(Socket socket){
			if(member.contains(socket)){
				member.remove(member.indexOf(socket));
				return true;
			}
			else 
				return false;
		}
		boolean containsMember(Socket socket){
			return member.contains(socket);
		}
		boolean checkEmpty(){
			return member.isEmpty();
		}
		int getID(){
			return id;
		}
		String getName(){
			return group_name;
		}
	}
    
    private class AudioStream extends Thread{
		private Socket source;
		private Socket destination;
		private Socket destination_msg;
		private AudioFormat format = new AudioFormat(
			AudioFormat.Encoding.PCM_SIGNED, 44100.0f, 16, 1, 2, 44100.0f, false); 
		private boolean flag;
		public AudioStream(){
			source = null;
			destination = null;
			destination_msg = null;
		}
		
		public void setPipe(Socket src,Socket dst,Socket dst_m){
			source = src;
			destination = dst;
			destination_msg = dst_m;
		}
		
		public void kill(){
			try{
				flag = false;
				PrintStream printStream=new PrintStream(destination_msg.getOutputStream());
				printStream.println("Voice_stream_off");
				printStream.close();
			} catch (Exception e){
				e.printStackTrace();
				return;
			}
		}
		
		@Override
		public void run(){
			flag = true;
			try{
				int length=(int)(format.getFrameSize()*format.getFrameRate()/2.0f);
				int len = -1;
				InputStream voiceInputStream = source.getInputStream();
				byte[] data=new byte[length];
				
				while((len = voiceInputStream.read(data)) != -1 && flag){
					destination.getOutputStream().write(data,0,data.length);
					destination.getOutputStream().flush();
				}
				destination.getOutputStream().close();	
				voiceInputStream.close();
			}catch (Exception e) {
				e.printStackTrace(); 
				flag = false; 
				return;  
			}  
		}
	}
}
