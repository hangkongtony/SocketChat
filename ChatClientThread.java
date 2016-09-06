package Client;

import java.io.DataInputStream;  
import java.io.DataOutputStream;  
import java.io.IOException;  
import java.net.Socket;  
  
public class ChatClientThread extends Thread {  
    private ChatClient _mClient;  
    private Socket _mSocket;  
    private DataOutputStream dos;  
      
    public ChatClientThread(ChatClient cclient, Socket socket) 
    {
        this._mClient = cclient;  
        this._mSocket = socket;  
    }  
      
    //接受message
    @Override
    public void run() 
    {  
        try {  
            DataInputStream bufferedReader = new DataInputStream(_mSocket.getInputStream());  
            byte[] cbuff = new byte[256];  
            char[] tbuff = new char[256];  
            int size = 0;  
            int byteCount = 0;  
            int length = 0;  
            
            //无限循环，接受message
            while(true)
            {  
                if((size = bufferedReader.read(cbuff))> 0) 
                {  
                    char[] temp = convertByteToChar(cbuff, size);  
                    length = temp.length;  
                    // one time only 256 bytes most
                    if((length + byteCount) > 256) 
                    {  
                        length = 256 - byteCount;  
                    }  
                    System.arraycopy(temp, 0, tbuff, byteCount, length);  
                    byteCount += size;  
                    //get the end message
                    if(String.valueOf(tbuff).indexOf(ChatClient.END_FLAG) > 0)
                    {  
                        _mClient.handleMessage(tbuff, byteCount);  
                        byteCount = 0;  
                        clearTempBuffer(tbuff);  
                    }  
                }  
            }  
        } catch (IOException e) 
        {  
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
        for(int i=0; i<size; i++) {  
            charBuff[i] = (char)cbuff[i];  
        }  
        return charBuff;  
    }  
      
    /**
     * 
     * @param textMsg write to the client socket of server 
     * @throws IOException
     */
    public synchronized void dispatchMessage(String textMsg) throws IOException 
    {  
        if(dos == null) 
        {  
            dos = new DataOutputStream(_mSocket.getOutputStream());  
        }  
        byte[] contentBytes = textMsg.getBytes();  
        //向服务器端client socket 写数据
        dos.write(contentBytes, 0, contentBytes.length);  
    }  
}  
