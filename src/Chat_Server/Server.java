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
	private JButton start_btn = new JButton("서버 실행");
	private JButton stop_btn = new JButton("서버 중지");
	
	//Network 자원 생성
	private ServerSocket server_socket;
	private Socket socket;
	private int port;
	private StringTokenizer st;
	
	// 생성된 유저 객체들 저장	
	private Vector user_vc = new Vector();
	
	// 생성된 룸 객체들 저장
	private Vector room_vc = new Vector();
	
	Server() {	// 생성자 
		init();	// 화면 생성 메소드
		start();	// 리스너 설정 메소드
	}
	
	private void start() {
		// start, stop버튼에 대한 actionListner 설정하기
		start_btn.addActionListener(this);	// 자체 클래스에서 actionListner를 상속받았기 때문에 this
		stop_btn.addActionListener(this);	
		
	}

	// 화면 구성
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
		
		JLabel lblNewLabel = new JLabel("포트 번호");
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
		
		this.setVisible(true); // true = 화면에 보이게	false = 화면에 보이지 않게
	}
	private void Server_start() {
		try {
			server_socket = new ServerSocket(port);
		} catch (IOException e) {
			JOptionPane.showMessageDialog
			(null, "해당 포트는 사용 중 입니다..","알림", JOptionPane.CLOSED_OPTION);
			e.printStackTrace();
		}
		
		// 정상적으로 포트가 열렸을 경우
		if(server_socket != null) {
			Connection();
		}

	}
	
	private void Connection() {
		// 1가지의 스레드에서는 1가지의 일만 처리할 수 있다.
		// 소켓 accept로 무한대기일 때는 다른 버튼 누를 수 없는 상태가 된다.
		// 이를 위해 accpet부분 thread 추가해서 처리
		
		// 무한루프틑 통한 다중 사용자 받기(스레드 생성)
	

		Thread th = new Thread(new Runnable() {
			@Override
			public void run() {	// 스레드에서 처리할 일을 기재
				while(true) {
					// 사용자 접속 대기 무한대
					try {
						
						textArea.append("사용자 접속 대기중\n");
						socket = server_socket.accept();
						textArea.append("사용자 접속!\n");
						
						UserInfo user = new UserInfo(socket);
						user.start();
						
					} catch (IOException e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog
						(null, "accept 에러 발생","알림", JOptionPane.CLOSED_OPTION);
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
			System.out.println("서비스 스타트 버튼 클릭");
			Server_start();	// 소켓생성 및 사용자 접속 대기
		}
		
		if(e.getSource() == stop_btn) {
			System.out.println("서비스 중지 버튼 클릭");
			
		}
	}	// 액션 이벤트 끝
	
	/*---------------------------------------------------------------*/
	
	// 접속 유저별 객체 생성 및 정보 Vector 내 저장
	// 사용자 각각의 스레드를 만들어줘야 하기 때문에, 스레드를 상속받은 내부 클래스로 만들어준다. 
	class UserInfo extends Thread {
		
		// 스트림 자원 생성
		private InputStream is;
		private OutputStream os;
		private DataInputStream dis;
		private DataOutputStream dos;
		
		private Socket user_socket;
		private String NicName;

		// UserInfo에 넣는 이유 : 어떤 사용자가 방 만들기 신청했는지 알기 위해
		private boolean RoomCheck;
		private String AttendRoom;
		
		UserInfo(Socket soc) {
			this.user_socket = soc;
			this.RoomCheck = true;
			this.AttendRoom = null;
			UserNetWork();
		}
		
		// 신규 user 객체에 대한 stream 설정
		private void UserNetWork() {
			try {
				is = user_socket.getInputStream();
				dis = new DataInputStream(is);
				os = user_socket.getOutputStream();
				dos = new DataOutputStream(os);
				
				NicName = dis.readUTF();
				textArea.append(NicName+" : 사용자 접속!\n");
				
				System.out.println("현재 접속된 사용자 수: "+user_vc.size());
				
				// 기존 유저에게 새로 접속된 유저 알리기
				BroadCase("NewUser/"+NicName);
				// 새로 추가된 user에게 기존 user 모두 전송 후(Existing/User1~N), 전송 완료 프로토콜 전달(send_message("ExistingUser/"+"Update"))
				for(int i=0; i<user_vc.size(); i++) {
					UserInfo u = (UserInfo)user_vc.elementAt(i);
					this.send_message("ExistingUser/"+u.NicName);
				}
				//클라이언트에게 vector 모두 다 보내줬음을 알려주기 위한 프로토콜(ExistingUser/End)
				send_message("ExistingUser/"+"Update");
				
				//vector에 자신 등록
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
				(null, "Server쪽 User Stream 설정 에러","알림", JOptionPane.CLOSED_OPTION);
			}
		}
		
		public void run() {
			while(true) {
				try {
					String msg = dis.readUTF();
					textArea.append(NicName+"사용자로부터 들어온 메시지 :"+msg+"\n");
					in_message(msg);
				} catch (IOException e) {
					
					// this 사용자가 접속이 끊어지면 dis.readUTF에서 오류가 발생한다.\
					// 따라서 아래 예외처리로 들어오게 됨
					e.printStackTrace();
					textArea.append(NicName+" : 사용자 접속 끊어짐\n");
					
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
		} // run 메소드 끝
		
		//서버로 들어오는 메시지 처리
		private void in_message(String str) {
			
			st = new StringTokenizer(str, "/");
			String protocol = st.nextToken();
			String message = st.nextToken();
			
			// 쪽지 프로토콜/사용자/내용 토크나이저 통한 파싱 후 해당 user에게 전달
			if(protocol.equals("Note")) {
			
				String note = st.nextToken();
				System.out.println(message+"님이 보낼 내용: "+note);

				for(int i=0; i<user_vc.size(); i++) {
					UserInfo u = (UserInfo)user_vc.elementAt(i);
					if(u.NicName.equals(message)) {
						send_message("Note/"+NicName+"/"+note);
						break;
					}
				}
			} 
			else if(protocol.equals("CreateRoom")) { // 방 만들기 요청시
				// 1. 현재 같은 방이 존재하는지 확인 for문 이용
				for(int i=0; i<room_vc.size(); i++) {
					RoomInfo r = (RoomInfo)room_vc.elementAt(i);
					if(r.Room_name.equals(message)) {
						send_message("CreatRoomFail/ok");
						RoomCheck = false;
						break;
					} 
				}
				// 방을 만들 수 있을 경우
				if(RoomCheck==true) {
					// 1. 혹시 내가 현재 다른 방에 접속 중이라면 방 나가기
					if(this.AttendRoom!=null) {
						for(int j=0; j<room_vc.size(); j++) {
							RoomInfo leaveUser = (RoomInfo)room_vc.elementAt(j);
							if(AttendRoom.equals(leaveUser.Room_name)) {
								leaveUser.LeaveRoom(this);
							}
						}
					}
					// 방 생성 및 접속
					RoomInfo r = new RoomInfo(message, this); 
					room_vc.addElement(r);
					AttendRoom = message;
					BroadCase("CreateRoom/"+message+"/"+this.NicName);
					this.send_message("CreateRoomSuccess/"+message);
				} 
				// 방만들기 가능유무 초기화
				RoomCheck = true;
			}
			else if(protocol.equals("Chatting")) {
				// 같은 room 내 인원들한테 브로드캐스팅
				// User1: 안녕하세요 형식
				String msg = st.nextToken(); // 메시지 내용
				
				for(int i=0; i<room_vc.size(); i++) {
					RoomInfo ri = (RoomInfo)room_vc.elementAt(i);
					if(message.equals(ri.Room_name)) {
						ri.BroadCast_Room("Chatting/"+NicName+"/"+msg);
					}
				}
			}
			else if(protocol.equals("JoinRoom")) {
				// 1. 들어가고자 하는 방을 확인하고,
				for(int i=0; i<room_vc.size(); i++) {
					RoomInfo r =(RoomInfo)room_vc.elementAt(i); 
					if(r.Room_name.equals(message)) { // 방을 찾았으면,
						// 2. 내가 방에 소속되어있는지 확인 후 특정 방에 소속되어 있으면 방 나가고,
						if(this.AttendRoom!=null) {
							for(int j=0; j<room_vc.size(); j++) {
								RoomInfo ri = (RoomInfo)room_vc.elementAt(j);
								if(AttendRoom.equals(ri.Room_name)) {
									ri.LeaveRoom(this);
								}
							}
						}
						// 3. 방으로 접속한다.
						AttendRoom=message;
						
						// 새로운 사용자를 방 사람들에게 알린다.
						r.BroadCast_Room("Chatting/ /**** "+NicName+"님이 입장하였습니다. ****");
						r.JoinRoom(this);
						send_message("JoinRoom/"+message);
						break;
					}
				}
			}
			
		}
		
		//해당 클라이언트(this)에게 메시지 보내기
		private void send_message(String str) {
			try {
				dos.writeUTF(str);
			} catch (IOException e) {}
		}
		
		// 전체 사용자에게 메시지 전송
		private void BroadCase(String str) {
			// 기존(현재) 사용자들에게 새로운 user 접속 알림 
			for(int i=0; i<user_vc.size(); i++) {
				UserInfo u = (UserInfo)user_vc.elementAt(i);
				u.send_message(str);
			}
		}
	}	// UserInfo 클래스 끝
	
	// 채팅방 정보 및 채팅내용 전달 클래스
	class RoomInfo {
		String Room_name;
		// Room마다 접속자 vector 관리하기 위함
		Vector Room_user_vc = new Vector();
		
		// 생성자 작성
		RoomInfo(String str, UserInfo user) {
			this.Room_name = str;
			this.Room_user_vc.add(user);
		}
		//현재 방에 있는 사람들에게 메시지 전송
		public void BroadCast_Room(String str) {
			for(int i=0; i<this.Room_user_vc.size(); i++) {
				UserInfo u = (UserInfo)this.Room_user_vc.elementAt(i);
				u.send_message(str);
			}
		}
		
		public void JoinRoom(UserInfo u) {
			Room_user_vc.add(u);
			System.out.println(Room_name+"으로"+u.NicName+"님 입장");
		}
		public void LeaveRoom(UserInfo u) {
			for(int i=0; i<Room_user_vc.size(); i++) {
				UserInfo user = (UserInfo)Room_user_vc.elementAt(i);
				if(user.NicName.equals(u.NicName)) {
					Room_user_vc.remove(i);
					BroadCast_Room("Chatting/ /**** "+u.NicName+"님이 퇴장하였습니다. ****");
					System.out.println(Room_name+"에서"+u.NicName+"님 퇴장");
					break;
				}
			}
		}
	}
	
}
