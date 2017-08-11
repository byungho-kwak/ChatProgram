package Chat_Server;

import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class Server extends JFrame implements ActionListener{
	
	private JPanel contentPane;
	private JTextField port_tf;
	private JTextArea textArea = new JTextArea();
	private JButton start_btn = new JButton("���� ����");
	private JButton stop_btn = new JButton("���� ����");
	
	//Network �ڿ� ����
	private ServerSocket server_socket;
	private Socket socket;
	private int port;
	private StringTokenizer st;
	
	private Vector user_vc = new Vector<>();
	
	Server() {	// ������ 
		init();	// ȭ�� ���� �޼ҵ�
		start();	// ������ ���� �޼ҵ�
	}
	
	private void start() {
		// start, stop��ư�� ���� actionListner �����ϱ�
		start_btn.addActionListener(this);	// ��ü Ŭ�������� actionListner�� ��ӹ޾ұ� ������ this
		stop_btn.addActionListener(this);	
		
	}

	// ȭ�� ����
	private void init() {	
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 384, 414);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 10, 344, 243);
		contentPane.add(scrollPane);
		
		
		scrollPane.setViewportView(textArea);
		
		JLabel lblNewLabel = new JLabel("��Ʈ ��ȣ");
		lblNewLabel.setBounds(12, 275, 57, 15);
		contentPane.add(lblNewLabel);
		
		port_tf = new JTextField();
		port_tf.setBounds(81, 272, 275, 21);
		contentPane.add(port_tf);
		port_tf.setColumns(10);
		
		start_btn.setBounds(12, 321, 161, 23);
		contentPane.add(start_btn);
		
		stop_btn.setBounds(188, 321, 168, 23);
		contentPane.add(stop_btn);
		
		this.setVisible(true); // true = ȭ�鿡 ���̰�	false = ȭ�鿡 ������ �ʰ�
	}
	private void Server_start() {
		try {
			server_socket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// ���������� ��Ʈ�� ������ ���
		if(server_socket != null) {
			Connection();
		}
	}
	
	private void Connection() {
		// 1������ �����忡���� 1������ �ϸ� ó���� �� �ִ�.
		// ���� accept�� ���Ѵ���� ���� �ٸ� ��ư ���� �� ���� ���°� �ȴ�.
		// �̸� ���� accpet�κ� thread �߰��ؼ� ó��
		
		// ���ѷ����z ���� ���� ����� �ޱ�(������ ����)
	
		Thread th = new Thread(new Runnable() {
			@Override
			public void run() {	// �����忡�� ó���� ���� ����
				while(true) {
					// ����� ���� ��� ���Ѵ�
					try {
						
						textArea.append("����� ���� �����\n");
						socket = server_socket.accept();
						textArea.append("����� ����!\n");
						
						UserInfo user = new UserInfo(socket);
						user.start();
						
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		
		th.start();
	}
	
	public static void main(String[] args) {
		
		new Server();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == start_btn) {
			port = Integer.parseInt(port_tf.getText().trim());
			System.out.println("���� ��ŸƮ ��ư Ŭ��");
			Server_start();	// ���ϻ��� �� ����� ���� ���
		}
		
		if(e.getSource() == stop_btn)
			System.out.println("���� ���� ��ư Ŭ��");
	}	// �׼� �̺�Ʈ ��
	
	
	/*---------------------------------------------------------------*/
	
	// ���� ������ ��ü ���� �� ���� Vector �� ����
	// ����� ������ �����带 �������� �ϱ� ������, �����带 ��ӹ��� ���� Ŭ������ ������ش�. 
	class UserInfo extends Thread {
		
		// ��Ʈ�� �ڿ� ����
		private InputStream is;
		private OutputStream os;
		private DataInputStream dis;
		private DataOutputStream dos;
		
		private Socket user_socket;
		private String NicName;
		
		UserInfo(Socket soc) {
			this.user_socket = soc;
			UserNetWork();
		}
		
		private void UserNetWork() {
			try {
				is = user_socket.getInputStream();
				dis = new DataInputStream(is);
				os = user_socket.getOutputStream();
				dos = new DataOutputStream(os);
				
				NicName = dis.readUTF();
				textArea.append(NicName+" : ����� ����!\n");
				
				System.out.println("���� ���ӵ� ����� ��: "+user_vc.size());
				
				// ���� �߰��� user���� ���� user ��� ���� �� -�������� : (Existing/User1~N)
				BroadCase("NewUser/"+NicName);
				
				for(int i=0; i<user_vc.size(); i++) {
					UserInfo u = (UserInfo)user_vc.elementAt(i);
					send_message("ExistingUser/"+u.NicName);
				}
				//Ŭ���̾�Ʈ���� vector ��� �� ���������� �˷��ֱ� ���� ��������(ExistingUser/End)
				send_message("ExistingUser/"+"End");
				//vector�� �ڽ� ���
				user_vc.add(this);
				
				
			} catch (IOException e) {
			
			}
		}
		
		public void run() {
			while(true) {
				try {
					String msg = dis.readUTF();
					textArea.append(NicName+"����ڷκ��� ���� �޽��� :"+msg+"\n");
					in_message(msg);
				} catch (IOException e) {
				}
			}
		} // run �޼ҵ� ��
		
		//������ ������ �޽��� ó��
		private void in_message(String str) {
			
			st = new StringTokenizer(str, "/");
			String protocol = st.nextToken();
			
			// ���� ��������/�����/���� ��ũ������ ���� �Ľ� �� �ش� user���� ����
			if(protocol.equals("Note")) {
				String user = st.nextToken();
				String note = st.nextToken();
				
				System.out.println(user+"���� ���� ����: "+note);

				for(int i=0; i<user_vc.size(); i++) {
					UserInfo u = (UserInfo)user_vc.elementAt(i);
					if(u.NicName.equals(user)) {
						send_message("Note/"+NicName+"/"+note);
						break;
					}
				}
			}
		}
		
		private void send_message(String str) {
			try {
				dos.writeUTF(str);
			} catch (IOException e) {}
		}
		
		// ��ü ����ڿ��� �޽��� ����
		private void BroadCase(String str) {
			// ����(����) ����ڵ鿡�� ���ο� user ���� �˸� 
			for(int i=0; i<user_vc.size(); i++) {
				UserInfo u = (UserInfo)user_vc.elementAt(i);
				u.send_message(str);
			}
		}
	}

}
