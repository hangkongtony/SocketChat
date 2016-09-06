package Server;

import java.io.*;
import java.net.Socket;

public class ChatServerClientThread extends Thread{

	private ChatServer chatServer;
	private Socket userSocket;
	private String userName;
	private DataOutputStream dos;
	public ChatServerClientThread(String userName, Socket client,
			ChatServer chatServer) 
	{
		this.chatServer = chatServer;
		this.userSocket = client;
		this.userName = userName;
	}

	@Override
	public void run()
	{  
	    System.out.println("start user = " + userName);  
	    try {  
	        DataInputStream bufferedReader = new DataInputStream(userSocket.getInputStream());  
	        byte[] cbuff = new byte[256];  
	        char[] tbuff = new char[256];  
	        int size = 0;  
	        int byteCount = 0;  
	        int length = 0;  
	        //无限循环
	        while(true)
	        {  
	            if((size = bufferedReader.read(cbuff))> 0) 
	            {  
	                char[] temp = convertByteToChar(cbuff, size);  
	                length = temp.length;  
	                if((length + byteCount) > 256)
	                {  
	                    length = 256 - byteCount;  
	                }  
	                System.arraycopy(temp, 0, tbuff, byteCount, length);  
	                byteCount += size;  
	                
	                //
	                if(String.valueOf(tbuff).indexOf(ChatServer.END_FLAG) > 0) 
	                {  
	                    String receivedContent = String.valueOf(tbuff);  
	                    int endFlag = receivedContent.indexOf(ChatServer.END_FLAG);  
	                    receivedContent = receivedContent.substring(0, endFlag);  
	                    
	                   
	                    //leyValue[0]:sender's username keyValue[1]:message
	                    String[] keyValue = receivedContent.split("#");  
	                    if(keyValue.length > 1) {  
	                    	chatServer.dispatchMessage(keyValue, userName);  
	                    }  
	                    byteCount = 0;  
	                    clearTempBuffer(tbuff);  
	                }  
	            }  
	        }  
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    }  
	}

	private void clearTempBuffer(char[] tbuff) 
	{
		  for(int i=0; i<tbuff.length; i++) {  
	            tbuff[i] = ' ';  
	        }  
	}

	private char[] convertByteToChar(byte[] cbuff, int size) 
	{  
	        char[] charBuff = new char[size];  
	        for(int i=0; i<size; i++)
	        {  
	            charBuff[i] = (char)cbuff[i];  
	        }  
	        return charBuff;  
	}

	public String getUserName() 
	{
		return userName;
	}



	public synchronized void dispatchMessage(String textMsg) 
	{
		//向客户端socket 发送 message
		 if(dos == null)
		 {  
			 try {
					dos = new DataOutputStream(userSocket.getOutputStream());
				} catch (IOException e) 
				{
					e.printStackTrace();
				} 
	      }  
	      byte[] contentBytes = textMsg.getBytes();  
	        //向client socket 写数据 
	      try 
	      {
	    	  dos.write(contentBytes, 0, contentBytes.length);
	      } catch (IOException e)
	      {
				e.printStackTrace();
	      }  
	}
}
