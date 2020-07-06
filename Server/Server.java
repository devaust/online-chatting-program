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
        // 设置数字格式，保留一位有效小数
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
        //1.获取客户端的输入流
        try {
            Scanner scanner=new Scanner(socket.getInputStream());
            String msg=null;
            while(true){
                if(scanner.hasNextLine()){
                    //0.处理客户端输入的字符串
                    msg=scanner.nextLine();

                    //1.注册用户流程,注册用户的格式为:userName:用户名
                    if(msg.startsWith("userName:")){
                        //将用户名保存在userName中
                        String userName=msg.split("\\:")[1];
                        //注册该用户
                        userRegist(userName,socket,socket_v,socket_f);
                        continue;
                    }
                    //2.群聊信息流程,群聊的格式为:G:群id:群聊信息
                    else if(msg.startsWith("G:")){
                        //必须先注册才可以!
                        if(!firstStep(socket)) continue;
                        //保存群聊信息
                        int id = Integer.valueOf(msg.split("\\:")[1]);
                        String str=msg.split("\\:")[2];
                        //发送群聊信息
                        groupChat(socket,str,id);
                        continue;
                    }
                    //3.私聊信息流程,私聊的格式为:P:userName-私聊信息
                    else if(msg.startsWith("P:")&&msg.contains("-")){
                        //必须先注册才可以!
                        if(!firstStep(socket)) continue;
                        //保存需要私聊的用户名
                        String userName=msg.split("\\:")[1].split("-")[0];
                        //保存私聊的信息
                        String str=msg.split("\\:")[1].split("-")[1];
                        //发送私聊信息
                        privateChat(socket,userName,str);
                        continue;
                    }
                    //4.用户退出流程,用户退出格式为:包含exit
                    else if(msg.contains("exit")){
                        //必须先注册才可以!
                        if(!firstStep(socket)) continue;
                        if(!firstStep1(socket_v)) continue;
                        userExit(socket,socket_v);
                        continue;
                    }
                    //5.创建群聊,格式为:create:name
                    else if(msg.startsWith("create:")){
						if(!firstStep(socket)) continue;
						String name=msg.split("\\:")[1];
						createGroup(socket,name);
                        continue;
					}
					//6.加入群聊,格式为:join:id
                    else if(msg.startsWith("join:")){
						if(!firstStep(socket)) continue;
						String id=msg.split("\\:")[1];
						joinGroup(socket,Integer.valueOf(id));
                        continue;
					}
					//7.退出群聊,格式为:quit:id
                    else if(msg.startsWith("quit:")){
						if(!firstStep(socket)) continue;
						String id=msg.split("\\:")[1];
						quitGroup(socket,Integer.valueOf(id));
                        continue;
					}
					//8.列出所在群聊,格式为:listGroup
                    else if(msg.startsWith("listGroup")){
						if(!firstStep(socket)) continue;
						listGroup(socket);
                        continue;
					}
					//9.开启语音聊天,格式为:call:userName
                    else if(msg.startsWith("call:")){
						if(!firstStep1(socket_v)) continue;
						String userName=msg.split("\\:")[1];
						voiceChat(socket,userName);
                        continue;
					}
					//10.关闭语音聊天,格式为:hangup
                    else if(msg.startsWith("hangup")){
						hangUp();
                        continue;
					}
					//11.文件传输,格式为:file:用户名-文件路径
                    else if(msg.startsWith("file:")){
                        //保存用户名
                        String userName=msg.split("\\:")[1].split("-")[0];
                        //发送文件
                        transFile(socket_f,userName);
                        continue;
					}else if(msg.equals("P")){
                        continue;
					}
                    //其他输入格式均错误
                    else{
                        PrintStream printStream=new PrintStream(socket.getOutputStream(),true,"GBK");
                        printStream.println("输入格式错误!请按照以下格式输入!");
                        printStream.println("注册用户格式:[userName:用户名]");
                        printStream.println("群聊格式:[G:群id:群聊信息]");
                        printStream.println("私聊格式:[P:userName-私聊信息]");
                        printStream.println("创建群聊格式:[create:群聊名]");
                        printStream.println("加入群聊格式:[join:群聊id]");
                        printStream.println("退出群聊格式:[quit:群聊id]");
                        printStream.println("列出所在群聊格式:[listGroup]");
                        printStream.println("用户退出格式:[包含exit即可]");
                        printStream.println("语音聊天格式:[call:用户名]");
                        printStream.println("挂断语音聊天格式:[hangup]");
                        printStream.println("文件传输格式:[file:用户名]");
                        continue;
                    }
                }
				
					
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
    /**
     * 第一步必须先注册!
     * @param socket 当前客户端
     */
    private boolean firstStep(Socket socket) throws IOException {
        Set<Map.Entry<String,Socket>> set=map.entrySet();
        for(Map.Entry<String,Socket> entry:set){
            if(entry.getValue().equals(socket)){
                if(entry.getKey()==null){
                    PrintStream printStream=new PrintStream(socket.getOutputStream(),true,"GBK");
                    printStream.println("请先进行注册操作！");
                    printStream.println("注册格式为:[userName:用户名]");
                    return false;
                }
                return true;
            }
        }
        PrintStream printStream=new PrintStream(socket.getOutputStream(),true,"GBK");
        printStream.println("请先进行注册操作！");
        printStream.println("注册格式为:[userName:用户名]");
        return false;
    }
    
    private boolean firstStep1(Socket socket) throws IOException {
        Set<Map.Entry<String,Socket>> set1=map1.entrySet();
        for(Map.Entry<String,Socket> entry:set1){
            if(entry.getValue().equals(socket)){
                if(entry.getKey()==null){
                    PrintStream printStream=new PrintStream(socket.getOutputStream(),true,"GBK");
                    printStream.println("请先进行注册操作！");
                    printStream.println("注册格式为:[userName:用户名]");
                    return false;
                }
                return true;
            }
        }
        PrintStream printStream=new PrintStream(socket.getOutputStream(),true,"GBK");
        printStream.println("请先进行注册操作！");
        printStream.println("注册格式为:[userName:用户名]");
        return false;
    }
    
    private boolean firstStep2(Socket socket) throws IOException {
        Set<Map.Entry<String,Socket>> set1=map2.entrySet();
        for(Map.Entry<String,Socket> entry:set1){
            if(entry.getValue().equals(socket)){
                if(entry.getKey()==null){
                    PrintStream printStream=new PrintStream(socket.getOutputStream(),true,"GBK");
                    printStream.println("请先进行注册操作！");
                    printStream.println("注册格式为:[userName:用户名]");
                    return false;
                }
                return true;
            }
        }
        PrintStream printStream=new PrintStream(socket.getOutputStream(),true,"GBK");
        printStream.println("请先进行注册操作！");
        printStream.println("注册格式为:[userName:用户名]");
        return false;
    }
    
    /**
     * 注册用户信息
     * @param userName 用户名
     * @param socket 用户客户端Socket对象
     */
    private void userRegist(String userName,Socket socket,Socket socket_v,Socket socket_f){
        map.put(userName,socket);
        map1.put(userName,socket_v);
        map2.put(userName,socket_f);
        System.out.println("[用户名为"+userName+"][客户端为"+socket+"]上线了!");
        System.out.println("当前在线人数为:"+map.size()+"人");
    }
 
	/**
	 * 创建群聊
	 * @param socket 创建群聊的客户端
	 * @param name 群聊名称
	 */
	private void createGroup(Socket socket,String name){
		newest_group_ID ++;
		Group temp = new Group(newest_group_ID,name);
		temp.addMember(socket);
		groups.add(temp);
		System.out.println(""+socket+"创建了群聊："+name+"-"+newest_group_ID);
	}
	 
	/**
     * 加入群聊
     * @param socket 申请加入群聊的客户端
     * @param id 群聊id
     */
	private boolean joinGroup(Socket socket,int id) throws IOException {
		for(Group group : groups){
			if(group.getID()==id){
				group.addMember(socket);
				return true;
			}
		}
		PrintStream printStream=new PrintStream(socket.getOutputStream(),true,"GBK");
		printStream.println("当前群聊不存在");
		return false;
	}
	 
	/**
     * 退出群聊
     * @param socket 申请退出群聊的客户端
     * @param id 群聊id
     */
	private boolean quitGroup(Socket socket,int id) throws IOException {
		for(Group group : groups){
			if(group.getID()==id){
				if(group.containsMember(socket))
					group.delMember(socket);
				else{
					PrintStream printStream=new PrintStream(socket.getOutputStream(),true,"GBK");
					printStream.println("你不在当前群聊中");
				}
				if(group.checkEmpty())
					groups.remove(groups.indexOf(group));
				return true;
			}
		}
		PrintStream printStream=new PrintStream(socket.getOutputStream(),true,"GBK");
		printStream.println("当前群聊不存在");
		return false;
	}
	
	/**
     * 列举用户加入的群聊
     * @param socket 列举群聊的客户端
     */
	private void listGroup(Socket socket) throws IOException {
		String result = "";
		for(Group group : groups){
			if(group.containsMember(socket)){
				result = result+"群号："+group.getID()+"，"+"群名："+group.getName()+"\n";
			}
		}
		PrintStream printStream=new PrintStream(socket.getOutputStream(),true,"GBK");
		printStream.println(result);
	}
	
    /**
     * 群聊流程(将Map集合转换为Set集合,从而取得每个客户端Socket,将群聊信息发送给每个客户端)
     * @param socket 发出群聊的客户端
     * @param msg 群聊信息
     */
    private void groupChat(Socket socket,String msg,int id) throws IOException {
		boolean flag = false;
		Group current_group=new Group();
		for(Group group : groups){
			if(group.getID()==id){
				current_group = group;
				if(!group.containsMember(socket)){
					PrintStream printStream=new PrintStream(socket.getOutputStream(),true,"GBK");
					printStream.println("你不在当前群聊中");
					return;
				}
				flag = true;
			}
		}
		if(!flag){
			PrintStream printStream=new PrintStream(socket.getOutputStream(),true,"GBK");
			printStream.println("当前群聊不存在");
			return;	
		}		
				
        //1.将Map集合转换为Set集合
        Set<Map.Entry<String,Socket>> set=map.entrySet();
        //2.遍历Set集合找到发起群聊信息的用户
        String userName=null;
        for(Map.Entry<String,Socket> entry:set){
            if(entry.getValue().equals(socket)){
                userName=entry.getKey();
                break;
            }
        }
        //3.遍历Set集合将群聊信息发给每一个客户端
        for(Map.Entry<String,Socket> entry:set){
            //取得客户端的Socket对象
            Socket client=entry.getValue();
            //取得client客户端的输出流
            if(current_group.containsMember(client)){
				PrintStream printStream=new PrintStream(client.getOutputStream(),true,"GBK");
				printStream.println(userName+"群聊说:"+msg);
			}
        }
    }
    
    /**
     * 私聊流程(利用userName取得客户端的Socket对象,从而取得对应输出流,将私聊信息发送到指定客户端)
     * @param socket 当前客户端
     * @param userName 私聊的用户名
     * @param msg 私聊的信息
     */
    private void privateChat(Socket socket,String userName,String msg) throws IOException {
        //1.取得当前客户端的用户名
        String curUser=null;
        Set<Map.Entry<String,Socket>> set=map.entrySet();
        for(Map.Entry<String,Socket> entry:set){
            if(entry.getValue().equals(socket)){
                curUser=entry.getKey();
                break;
            }
        }
        //2.取得私聊用户名对应的客户端
        Socket client=map.get(userName);
        //3.获取私聊客户端的输出流,将私聊信息发送到指定客户端
        PrintStream printStream=new PrintStream(client.getOutputStream(),true,"GBK");
        printStream.println(curUser+"私聊说:"+msg);
    }
 
	/**
     * 语音私聊流程(利用userName取得客户端的Socket对象,从而取得对应输出流,将私聊信息发送到指定客户端)
     * @param socket 当前客户端
     * @param userName 私聊的用户名
     */
    private void voiceChat(Socket socket,String userName) throws IOException {
        //1.取得私聊用户名对应的客户端
        Socket client=map.get(userName);
        Socket client1=map1.get(userName);
        
        //2.获取私聊客户端的输出流,将标志信息发送到指定客户端
		PrintStream printStream=new PrintStream(client.getOutputStream());
        printStream.println("Voice_stream_on");
        
        audioStream = new AudioStream();
        audioStream.setPipe(socket_v,client1,client);
        audioStream.start();
    }
    
    /**
     * 语音私聊挂断
     */
    private void hangUp() throws IOException {
        audioStream.kill();
    }
    
    /**
     * 文件传输
     * @param socket 当前客户端
     * @param userName 私聊的用户名
     * @param msg 本地文件路径
     */
    private void transFile(Socket socket,String userName) throws IOException {
        //1.取得当前客户端的用户名
        String curUser=null;
        Set<Map.Entry<String,Socket>> set=map2.entrySet();
        for(Map.Entry<String,Socket> entry:set){
            if(entry.getValue().equals(socket)){
                curUser=entry.getKey();
                break;
            }
        }
        
        //2.取得目标用户名对应的客户端
        Socket client=map2.get(userName);
        Socket client_msg=map.get(userName);
        
        //建立通信通道，强行输出数据
        PrintStream writer1 = new PrintStream(socket.getOutputStream(), true,"GBK");
        PrintStream writer2 = new PrintStream(client_msg.getOutputStream(), true,"GBK");
        
        writer1.println("输入文件路径：");
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        writer2.println("Receiving_file");
        DataOutputStream dos = new DataOutputStream(client.getOutputStream());
        
		// 文件名和长度
		String fileName = dis.readUTF();
		long fileLength = dis.readLong();
		
		dos.writeUTF(fileName);//
		dos.flush();//
		dos.writeLong(fileLength);//
		dos.flush();//


		// 开始接收文件
		byte[] bytes = new byte[1024];
		int length = 0;
		long progress = 0;
		while((length = dis.read(bytes, 0, bytes.length)) != -1) {
			dos.write(bytes, 0, length);
			dos.flush();
			progress += length;
			int progress_bar = (int)(progress/(fileLength/50));
			System.out.print("\r当前进度：");
			for(int i=0;i<progress_bar;i++) System.out.print("|");
			System.out.print((100*progress/fileLength) + "%");
			if(progress>=fileLength) break;
		}
		System.out.println();
		System.out.println("[文件中转完成 File Name：" + fileName + ", Size：" 
			+ getFormatFileSize(fileLength) + "]");
    }
    
    /**
     * 用户退出
     * @param socket
     */
    private void userExit(Socket socket,Socket socket_v){
        //1.利用socket取得对应的Key值
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
        //2.将userName,Socket元素从map集合中删除
        if(!map.isEmpty()) map.remove(userName,socket);
        if(!map.isEmpty()) map1.remove(userName1,socket_v);
        //3.提醒服务器该客户端已下线
        if(userName != null) System.out.println("用户:"+userName+"已下线!");
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
