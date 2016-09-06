package Client;
import java.awt.BorderLayout;  
import java.awt.FlowLayout;  
import java.awt.GridLayout;  
import java.awt.event.ActionEvent;  
import java.awt.event.ActionListener;  
import java.io.IOException;  
import java.net.InetSocketAddress;  
import java.net.Socket;  
import java.net.SocketAddress;
import java.net.UnknownHostException;
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
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;  
import javax.swing.JTextArea;  
import javax.swing.JTextField;  
  
//import com.gloomyfish.custom.swing.ui.CurvedGradientPanel;  
  
public class ChatClient extends JFrame implements ActionListener {  
    public final static String CONNECT_CMD = "Connect";  
    public final static String DISCONNECT_CMD = "Disconnect";  
    public final static String SEND_CMD = "Send";  
    public final static String END_FLAG = "EOF";  
  
    //private Vector<String> userNameList = new Vector<String>();
    /** 
     *  
     */  
    private static final long serialVersionUID = 5837742337463099673L;  
    private String winTitle;  
    private JLabel userLabel;  
    private JLabel passwordLabel;  
    private JLabel ipLabel;  
    private JLabel portLabel;  
      
    // text field  
    JTextField userField;  
    private JPasswordField passwordField;  
    private JTextField ipField;  
    private JTextField portField;  
      
    private JList<String> friendList;  
    private JTextArea historyRecordArea;  
    private JTextArea chatContentArea;  
      
    // buttons  
    private JButton connectBtn;  
    private JButton disConnectBtn;  
    private JButton sendBtn;  
    private JRadioButton send2AllBtn;  
      
    // socket  
    private Socket mSocket;  
    private SocketAddress address;  
    private ChatClientThread m_client;  
      
    public ChatClient() {  
        super("Chat Client");  
        initComponents();  
        setupListener();  
    }  
      
    private void initComponents() {  
        JPanel settingsPanel = new JPanel();  
        JPanel chatPanel = new JPanel();  
        GridLayout gy = new GridLayout(1,2,10,2);  
        getContentPane().setLayout(gy);  
        getContentPane().add(settingsPanel);  
        getContentPane().add(chatPanel);  
          
        // set up settings info  
        settingsPanel.setLayout(new BorderLayout());  
        settingsPanel.setOpaque(false);  
        JPanel gridPanel = new JPanel(new GridLayout(4, 2));  
        gridPanel.setBorder(BorderFactory.createTitledBorder("Server Settings & User Info"));  
        gridPanel.setOpaque(false);  
        userLabel = new JLabel("User Name:");  
        passwordLabel = new JLabel("User Password:");  
        ipLabel = new JLabel("Server IP Address:");  
        portLabel = new JLabel("Server Port");  
        userLabel.setOpaque(false);  
        passwordLabel.setOpaque(false);  
        ipLabel.setOpaque(false);  
        portLabel.setOpaque(false);  
        userField = new JTextField();  
        passwordField = new JPasswordField();  
        ipField = new JTextField("127.0.0.1");  
        portField = new JTextField("9999");  
        connectBtn = new JButton(CONNECT_CMD);  
        disConnectBtn = new JButton(DISCONNECT_CMD);  
        JPanel btnPanel = new JPanel();  
        btnPanel.setOpaque(false);  
        btnPanel.setLayout(new FlowLayout());  
        btnPanel.add(connectBtn);  
        btnPanel.add(disConnectBtn);  
          
        gridPanel.add(userLabel);  
        gridPanel.add(userField);  
        gridPanel.add(passwordLabel);  
        gridPanel.add(passwordField);  
        gridPanel.add(ipLabel);  
        gridPanel.add(ipField);  
        gridPanel.add(portLabel);  
        gridPanel.add(portField);  
        friendList = new JList<String>();  
        JScrollPane friendPanel = new JScrollPane(friendList);  
        friendPanel.setOpaque(false);  
        friendPanel.setBorder(BorderFactory.createTitledBorder("Friend List:"));  
        settingsPanel.add(btnPanel, BorderLayout.SOUTH);  
        settingsPanel.add(gridPanel, BorderLayout.NORTH);  
        settingsPanel.add(friendPanel,BorderLayout.CENTER);  
          
        chatPanel.setLayout(new GridLayout(3,1));  
        chatPanel.setOpaque(false);  
        historyRecordArea = new JTextArea();  
        JScrollPane histroyPanel = new JScrollPane(historyRecordArea);  
        histroyPanel.setBorder(BorderFactory.createTitledBorder("Chat History Record:"));  
        histroyPanel.setOpaque(false);  
        chatContentArea = new JTextArea();  
        JScrollPane messagePanel = new JScrollPane(chatContentArea);  
        messagePanel.setBorder(BorderFactory.createTitledBorder("Message:"));  
        messagePanel.setOpaque(false);  
        // chatPanel.add(friendPanel);  
        chatPanel.add(histroyPanel);  
        chatPanel.add(messagePanel);  
        sendBtn = new JButton(SEND_CMD);  
        send2AllBtn = new JRadioButton("Send to All online Users");  
        send2AllBtn.setOpaque(false);  
        JPanel sendbtnPanel = new JPanel();  
        sendbtnPanel.setOpaque(false);  
        sendbtnPanel.setLayout(new FlowLayout());  
        sendbtnPanel.add(sendBtn);  
        sendbtnPanel.add(send2AllBtn);  
        chatPanel.add(sendbtnPanel);  
    }  
      
    private void setupListener() 
    {  
        connectBtn.addActionListener(this);  
        disConnectBtn.addActionListener(this);  
        disConnectBtn.setEnabled(false);  
        sendBtn.addActionListener(this);
    }  
      
    /** 
     * <p></p> 
     *  
     * @param message - char array  
     * @param bsize - the size of bytes 
     */  
    public synchronized void handleMessage(char[] message, int bsize) {  
        // char[] inputMessage = convertByteToChar(content, bsize);  
        String receivedContent = String.valueOf(message);  
        int endFlag = receivedContent.indexOf(END_FLAG);  
        receivedContent = receivedContent.substring(0, endFlag);  
        System.out.println("Client " + userField.getText() + " Message:" + receivedContent);  
        //userlist
         if(receivedContent.contains("&"))
        {
        	 String[] UserList = receivedContent.split("&");
        	 friendList.removeAll();
        	 friendList.setListData(UserList);
        }
        //message
        else {  
            // just append to chat history record...  
            appendHistoryRecord(receivedContent + "\r\n");  
        }  
    }  
      
    public synchronized void appendHistoryRecord(String record) {  
        historyRecordArea.append(record);  
    }  
      
    private String getSelectedUser() 
    {  
        int index = friendList.getSelectedIndex();  
       if(send2AllBtn.isSelected()){  
        	//send to all
            return "Server";  
        }
       else if(index >= 0)
       {  
       	String user = (String)friendList.getSelectedValue();  
       	return user;  
       }
        return "Server";
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
        ChatClient client = new ChatClient();  
        client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
        client.pack();  
        client.setVisible(true);  
    }  
  
    //@Override
    public void actionPerformed(ActionEvent e)
    {  
        if(SEND_CMD.equals(e.getActionCommand())) 
        {  
            String chatContent = chatContentArea.getText();  
            if(checkNull(chatContent)) 
            {  
                JOptionPane.showMessageDialog(this, "Please enter the message at least 6 characters!");  
                return;  
            } else if(chatContent.getBytes().length > 200) 
            {  
                JOptionPane.showMessageDialog(this, "The length of the message must be less than 200 characters!");  
                return;  
            }  
            try {  
            	//规则，协议：加# EOF
                m_client.dispatchMessage(getSelectedUser() + "#" + chatContent);  
                m_client.dispatchMessage(END_FLAG);  
                appendHistoryRecord("me :" + chatContent + "\r\n");  
                chatContentArea.setText(""); // try to clear user enter......  
            } catch (IOException e1) {  
                e1.printStackTrace();  
            }  
        }
        else if(DISCONNECT_CMD.equals(e.getActionCommand()))
        { 
        		
        	try
        	{
        		if(mSocket!=null)
        		{
        			mSocket.close();
        			System.out.println(" client socket is closed!!");
        		}
			} 
        	catch (IOException e1) 
        	{
				e1.printStackTrace();
			}
            enableSettingsUI(true);
        } 
        else if(CONNECT_CMD.equals(e.getActionCommand())) 
        {  
            String serverHostName = ipField.getText();  
            String portStr = portField.getText();  
            String userName = userField.getText();  
            char[] password = passwordField.getPassword();  
            System.out.println("Password = " + password.length);  
            if(checkNull(serverHostName) || checkNull(portStr) || checkNull(userName)) {  
                JOptionPane.showMessageDialog(this, "Please enter user name, server host name, server port!");  
                return;  
            }  
            setTitle("Chat Client-" + userName);  
            address = new InetSocketAddress(serverHostName, Integer.parseInt(portStr));  
            //mSocket = new Socket(); 
            try {
            	//create the object and connect
				mSocket = new Socket(serverHostName, Integer.parseInt(portStr));
			} catch (NumberFormatException e1) {
				e1.printStackTrace();
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
            try {  
               // mSocket.connect(address); 
                m_client = new ChatClientThread(this, mSocket);
                //when first link send user name
                m_client.dispatchMessage(userName); 
                //start the thread to get message from server's socket
                m_client.start();  
                enableSettingsUI(false);  
            } catch (IOException ioe) {  
                ioe.printStackTrace();  
            }  
        }  
    }  
      
    private void enableSettingsUI(boolean enable) 
    {  
        ipField.setEditable(enable);  
        portField.setEnabled(enable);  
        userField.setEditable(enable);  
        passwordField.setEnabled(enable);  
        connectBtn.setEnabled(enable);  
        disConnectBtn.setEnabled(!enable);  
    }  
      
    private boolean checkNull(String inputString) 
    {  
        if(inputString ==  null || inputString.length() == 0) 
        {  
            return true;  
        } else 
        {  
            return false;  
        }  
    }  
}  
