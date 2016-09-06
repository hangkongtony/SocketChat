package Server;
import java.awt.BorderLayout;  
import java.awt.Container;
import java.awt.FlowLayout;  
import java.awt.GridLayout;  
import java.awt.event.ActionEvent;  
import java.awt.event.ActionListener;  
import java.io.DataInputStream;
import java.io.IOException;  
import java.net.InetSocketAddress;  
import java.net.ServerSocket;
import java.net.Socket;  
import java.net.SocketAddress;  
  



import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;  
import javax.swing.JButton;  
import javax.swing.JCheckBox;  
import javax.swing.JFrame;  
import javax.swing.JLabel;  
import javax.swing.JList;  
import javax.swing.JOptionPane;  
import javax.swing.JPanel;  
import javax.swing.JPasswordField;  
import javax.swing.JScrollPane;  
import javax.swing.JTextArea;  
import javax.swing.JTextField;  
  
//import com.gloomyfish.custom.swing.ui.CurvedGradientPanel;  
  
public class ChatServer extends JFrame implements ActionListener {  
    public final static String START_SERVER = "START";  
    public final static String END_SERVER = "END";  
    public final static String END_FLAG = "EOF";
  
    /** 
     *  
     */  
    private static final long serialVersionUID = 5837742337463099673L;  
    private String winTitle;  
   
      
    private JList<String> friendList;  
    private JTextArea historyRecordArea;  
      
    // buttons  
    private JButton BtnStartServer;  
    private JButton BtnStopServer;  
      
    // socket  
    private Socket mSocket;  
    private SocketAddress address;  
    private ServerSocket serverSocket;
      
    //容器
    private List<ChatServerClientThread> clientList = new ArrayList<ChatServerClientThread>(); ;
    private Vector<String> userNameList = new Vector<String>();
    
    public ChatServer() {  
        super("Chat Server");  
        initComponents();  
        setupListener();  
    }  
      
    private void initComponents() 
    {  
        JPanel ListPanel = new JPanel();  
        JPanel chatPanel = new JPanel();  
        GridLayout gy = new GridLayout(1,2,10,2);  
        getContentPane().setLayout(gy);  
        getContentPane().add(ListPanel);  
        getContentPane().add(chatPanel);  
          
        ListPanel.setLayout(new BorderLayout());  
        ListPanel.setOpaque(false);  
        friendList = new JList<String>();  
        JScrollPane friendPanel = new JScrollPane(friendList);  
        friendPanel.setOpaque(false);  
        friendPanel.setBorder(BorderFactory.createTitledBorder("Friend List:"));  
        ListPanel.add(friendPanel,BorderLayout.CENTER);  
       
        
        JPanel btnPanel = new JPanel();  
        btnPanel.setOpaque(false);  
        btnPanel.setLayout(new FlowLayout());  
        BtnStartServer = new JButton(START_SERVER);
        BtnStopServer = new JButton(END_SERVER);
        btnPanel.add(BtnStartServer);
        btnPanel.add(BtnStopServer);
        chatPanel.add(btnPanel, BorderLayout.SOUTH);  
        
          
        chatPanel.setLayout(new GridLayout(3,1));  
        chatPanel.setOpaque(false);  
        historyRecordArea = new JTextArea();  
        JScrollPane histroyPanel = new JScrollPane(historyRecordArea);  
        histroyPanel.setBorder(BorderFactory.createTitledBorder("Chat History Record:"));  
        histroyPanel.setOpaque(false);    
        chatPanel.add(histroyPanel,BorderLayout.CENTER);    
    }  
      
    private void setupListener() 
    {  
    	BtnStartServer.addActionListener(this);  
    	BtnStopServer.addActionListener(this);  
    }  
      
    //
    public synchronized void dispatchMessage(String[] keyValue, String userName) throws IOException 
    {  
    	historyRecordArea.append(userName + " to " + keyValue[0] + " : " + keyValue[1] + "\r\n");  
    	//send to all
    	if(keyValue[0].equals("Server"))
    	{
    		for(ChatServerClientThread client : clientList) 
    		{  
                     client.dispatchMessage(userName + " says: " + keyValue[1]);  
                     client.dispatchMessage(END_FLAG);  
            }  
    	}
    	else
    	{
    		for(ChatServerClientThread client : clientList) 
    		{  
                if(client.getUserName().equals(keyValue[0])) 
                {  
                     client.dispatchMessage(userName + " says: " + keyValue[1]);  
                     client.dispatchMessage(END_FLAG);  
                    break;  
                }  
            }  
    	}
    } 
    
    
    /** 
     * <p></p> 
     *  
     * @param content - byte array 
     * @param bsize - the size of bytes 
     */  
    public synchronized void handleMessage(char[] content, int bsize) 
    {  
        // char[] inputMessage = convertByteToChar(content, bsize);  
        String receivedContent = String.valueOf(content);  
        int endFlag = receivedContent.indexOf(END_FLAG);  
        receivedContent = receivedContent.substring(0, endFlag);  
        if(receivedContent.contains("#")) {  
            String[] onlineUserList = receivedContent.split("#");  
            friendList.setListData(onlineUserList);  
        } else {  
            // just append to chat history record...  
            appendHistoryRecord(receivedContent + "\r\n");  
        }  
    }  
      
    public synchronized void appendHistoryRecord(String record) 
    {  
        historyRecordArea.append(record);  
    }  
      
    public void setTitle(String title)
    {  
        winTitle = title;  
        super.setTitle(winTitle);  
    }  
      
    public String getTitle()
    {  
        return super.getTitle();  
    }  
      
    public static void main(String[] args) 
    {  
        ChatServer client = new ChatServer();  
        client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
        client.pack();  
        client.setVisible(true);  
    }  
  
    //@Override
    public void actionPerformed(ActionEvent e) 
    {  
    	   if(START_SERVER.equals(e.getActionCommand())) 
    	   {
    		   // new a thread to start server
    		   Thread startThread = new Thread(new Runnable() 
    		   {  
                   public void run() 
                   {  
                	   //default port 9999
                    startServer(9999);  
                   }
               });  
    		   startThread.start();  
    		   BtnStartServer.setEnabled(false);  
    		   BtnStopServer.setEnabled(true);  
    	   }
    	   else if(END_SERVER.equals(e.getActionCommand()))
    	   {
    		   //stop server
    		   stopServer();
    	   }
    }  
      
	private void stopServer() 
	{
		try 
		{
			if(serverSocket!=null)
			{
				serverSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void startServer(int port) 
	{
		 try {  
		        serverSocket = new ServerSocket(port);  
		        System.out.println("Server started at port :" + port);
		        //无限循环，接受 mutil client socket
		        while(true)
		        {  
		        	if(!serverSocket.isClosed())
		        	{
		        			Socket client = serverSocket.accept(); // blocked & waiting for income socket  
				            System.out.println("Just connected to " + client.getRemoteSocketAddress());  
				            //in order to get client socket's username
				            DataInputStream bufferedReader = new DataInputStream(client.getInputStream());  
				            byte[] cbuff = new byte[256]; 
				            int size = bufferedReader.read(cbuff);  
				            char[] charBuff = convertByteToChar(cbuff, size);  
				            String userName = String.valueOf(charBuff);  
				            ChatServerClientThread clentThread = new ChatServerClientThread(userName, client, this);  
				            //run for get message
				            clentThread.start();  
							clientList.add(clentThread);  
				            userNameList.add(userName);  
				            updateUserList();  
		        	}
		        }  
		    } catch (IOException e) {  
		        e.printStackTrace();  
		    }  
	}

	private void updateUserList()
	{
		friendList.removeAll();
		String[] usernames = new String[userNameList.size()];
		for(int i = 0;i<userNameList.size();i++)
		{
			usernames[i] = userNameList.get(i);
		}
		friendList.setListData(usernames);
		
		StringBuffer namesString = new StringBuffer();
		for(int i =0;i<usernames.length;i++)
		{
			namesString.append(usernames[i]+"&");
		}
		
		for(ChatServerClientThread client:clientList)
		{
			client.dispatchMessage(namesString.toString());
			client.dispatchMessage(END_FLAG);
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
}  
