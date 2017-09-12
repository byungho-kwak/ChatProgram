package Chat_Server;

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
import javax.swing.JOptionPane;
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
	
	// ������ ���� ��ü�� ����	
	private Vector user_vc = new Vector();
	
	// ������ �� ��ü�� ����
	private Vector room_vc = new Vector();
	
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
			JOptionPane.showMessageDialog
			(null, "�ش� ��Ʈ�� ��� �� �Դϴ�..","�˸�", JOptionPane.CLOSED_OPTION);
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
						JOptionPane.showMessageDialog
						(null, "accept ���� �߻�","�˸�", JOptionPane.CLOSED_OPTION);
						break;
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
		
		if(e.getSource() == stop_btn) {
			System.out.println("���� ���� ��ư Ŭ��");
			
		}
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

		// UserInfo�� �ִ� ���� : � ����ڰ� �� ����� ��û�ߴ��� �˱� ����
		private boolean RoomCheck;
		private String AttendRoom;
		
		UserInfo(Socket soc) {
			this.user_socket = soc;
			this.RoomCheck = true;
			this.AttendRoom = null;
			UserNetWork();
		}
		
		// �ű� user ��ü�� ���� stream ����
		private void UserNetWork() {
			try {
				is = user_socket.getInputStream();
				dis = new DataInputStream(is);
				os = user_socket.getOutputStream();
				dos = new DataOutputStream(os);
				
				NicName = dis.readUTF();
				textArea.append(NicName+" : ����� ����!\n");
				
				System.out.println("���� ���ӵ� ����� ��: "+user_vc.size());
				
				// ���� �������� ���� ���ӵ� ���� �˸���
				BroadCase("NewUser/"+NicName);
				// ���� �߰��� user���� ���� user ��� ���� ��(Existing/User1~N), ���� �Ϸ� �������� ����(send_message("ExistingUser/"+"Update"))
				for(int i=0; i<user_vc.size(); i++) {
					UserInfo u = (UserInfo)user_vc.elementAt(i);
					this.send_message("ExistingUser/"+u.NicName);
				}
				//Ŭ���̾�Ʈ���� vector ��� �� ���������� �˷��ֱ� ���� ��������(ExistingUser/End)
				send_message("ExistingUser/"+"Update");
				
				//vector�� �ڽ� ���
				user_vc.add(this);
				
				for(int i=0; i<room_vc.size(); i++) {
					RoomInfo r = (RoomInfo)room_vc.elementAt(i);
					send_message("ExistingRoom/"+r.Room_name);
				}
				send_message("ExistingRoom/"+"Update");
				
			} catch (IOException e) {
				try {
					is.close();
					os.close();
					dis.close();
					dos.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				JOptionPane.showMessageDialog
				(null, "Server�� User Stream ���� ����","�˸�", JOptionPane.CLOSED_OPTION);
			}
		}
		
		public void run() {
			while(true) {
				try {
					String msg = dis.readUTF();
					textArea.append(NicName+"����ڷκ��� ���� �޽��� :"+msg+"\n");
					in_message(msg);
				} catch (IOException e) {
					
					// this ����ڰ� ������ �������� dis.readUTF���� ������ �߻��Ѵ�.\
					// ���� �Ʒ� ����ó���� ������ ��
					e.printStackTrace();
					textArea.append(NicName+" : ����� ���� ������\n");
					
					try {
						dis.close();
						dos.close();
						user_socket.close();
						user_vc.remove(this);
						BroadCase("User_out/"+NicName);
						if(AttendRoom!=null) {
							for(int i=0; i<room_vc.size(); i++) {
								RoomInfo r = (RoomInfo)room_vc.elementAt(i);
								if(r.Room_name.equals(AttendRoom)&&r.Room_user_vc.size()==1) {
									room_vc.remove(i);
									BroadCase("Room_delete/"+AttendRoom);
									break;
								}
							}
						}
						
					} catch (IOException e1) {}
					break;
				}
			}
		} // run �޼ҵ� ��
		
		//������ ������ �޽��� ó��
		private void in_message(String str) {
			
			st = new StringTokenizer(str, "/");
			String protocol = st.nextToken();
			String message = st.nextToken();
			
			// ���� ��������/�����/���� ��ũ������ ���� �Ľ� �� �ش� user���� ����
			if(protocol.equals("Note")) {
			
				String note = st.nextToken();
				System.out.println(message+"���� ���� ����: "+note);

				for(int i=0; i<user_vc.size(); i++) {
					UserInfo u = (UserInfo)user_vc.elementAt(i);
					if(u.NicName.equals(message)) {
						send_message("Note/"+NicName+"/"+note);
						break;
					}
				}
			} 
			else if(protocol.equals("CreateRoom")) { // �� ����� ��û��
				// 1. ���� ���� ���� �����ϴ��� Ȯ�� for�� �̿�
				for(int i=0; i<room_vc.size(); i++) {
					RoomInfo r = (RoomInfo)room_vc.elementAt(i);
					if(r.Room_name.equals(message)) {
						send_message("CreatRoomFail/ok");
						RoomCheck = false;
						break;
					} 
				}
				// ���� ���� �� ���� ���
				if(RoomCheck==true) {
					// 1. Ȥ�� ���� ���� �ٸ� �濡 ���� ���̶�� �� ������
					if(this.AttendRoom!=null) {
						for(int j=0; j<room_vc.size(); j++) {
							RoomInfo leaveUser = (RoomInfo)room_vc.elementAt(j);
							if(AttendRoom.equals(leaveUser.Room_name)) {
								leaveUser.LeaveRoom(this);
							}
						}
					}
					// �� ���� �� ����
					RoomInfo r = new RoomInfo(message, this); 
					room_vc.addElement(r);
					AttendRoom = message;
					BroadCase("CreateRoom/"+message+"/"+this.NicName);
					this.send_message("CreateRoomSuccess/"+message);
				} 
				// �游��� �������� �ʱ�ȭ
				RoomCheck = true;
			}
			else if(protocol.equals("Chatting")) {
				// ���� room �� �ο������� ��ε�ĳ����
				// User1: �ȳ��ϼ��� ����
				String msg = st.nextToken(); // �޽��� ����
				
				for(int i=0; i<room_vc.size(); i++) {
					RoomInfo ri = (RoomInfo)room_vc.elementAt(i);
					if(message.equals(ri.Room_name)) {
						ri.BroadCast_Room("Chatting/"+NicName+"/"+msg);
					}
				}
			}
			else if(protocol.equals("JoinRoom")) {
				// 1. ������ �ϴ� ���� Ȯ���ϰ�,
				for(int i=0; i<room_vc.size(); i++) {
					RoomInfo r =(RoomInfo)room_vc.elementAt(i); 
					if(r.Room_name.equals(message)) { // ���� ã������,
						// 2. ���� �濡 �ҼӵǾ��ִ��� Ȯ�� �� Ư�� �濡 �ҼӵǾ� ������ �� ������,
						if(this.AttendRoom!=null) {
							for(int j=0; j<room_vc.size(); j++) {
								RoomInfo ri = (RoomInfo)room_vc.elementAt(j);
								if(AttendRoom.equals(ri.Room_name)) {
									ri.LeaveRoom(this);
								}
							}
						}
						// 3. ������ �����Ѵ�.
						AttendRoom=message;
						
						// ���ο� ����ڸ� �� ����鿡�� �˸���.
						r.BroadCast_Room("Chatting/ /**** "+NicName+"���� �����Ͽ����ϴ�. ****");
						r.JoinRoom(this);
						send_message("JoinRoom/"+message);
						break;
					}
				}
			}
			
		}
		
		//�ش� Ŭ���̾�Ʈ(this)���� �޽��� ������
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
	}	// UserInfo Ŭ���� ��
	
	// ä�ù� ���� �� ä�ó��� ���� Ŭ����
	class RoomInfo {
		String Room_name;
		// Room���� ������ vector �����ϱ� ����
		Vector Room_user_vc = new Vector();
		
		// ������ �ۼ�
		RoomInfo(String str, UserInfo user) {
			this.Room_name = str;
			this.Room_user_vc.add(user);
		}
		//���� �濡 �ִ� ����鿡�� �޽��� ����
		public void BroadCast_Room(String str) {
			for(int i=0; i<this.Room_user_vc.size(); i++) {
				UserInfo u = (UserInfo)this.Room_user_vc.elementAt(i);
				u.send_message(str);
			}
		}
		
		public void JoinRoom(UserInfo u) {
			Room_user_vc.add(u);
			System.out.println(Room_name+"����"+u.NicName+"�� ����");
		}
		public void LeaveRoom(UserInfo u) {
			for(int i=0; i<Room_user_vc.size(); i++) {
				UserInfo user = (UserInfo)Room_user_vc.elementAt(i);
				if(user.NicName.equals(u.NicName)) {
					Room_user_vc.remove(i);
					BroadCast_Room("Chatting/ /**** "+u.NicName+"���� �����Ͽ����ϴ�. ****");
					System.out.println(Room_name+"����"+u.NicName+"�� ����");
					break;
				}
			}
		}
	}
	
}
