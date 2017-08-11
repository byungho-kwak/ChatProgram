package Chat_Client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class Client extends JFrame implements ActionListener{
	
	//로그인 GUI 변수
	private JFrame Login_GUI = new JFrame();
	private JPanel login_pane;
	private JTextField ip_tf;	// ip 받는 텍스트필드
	private JTextField port_tf; // port 받는 텍스트필드
	private JTextField id_tf; // id 받는 텍스트필드
	private JButton login_btn = new JButton("접 속");
	
	// Main GUI 변수
	private JPanel main_pane;
	private JTextField message_tf;
	private JButton notesend_btn = new JButton("쪽지 보내기");
	private JButton joinroom_btn = new JButton("채팅방 참여");
	private JButton createroom_btn = new JButton("방 만들기");
	private JButton sendmessage_btn = new JButton("전송");
	
	private JList user_list = new JList();	//전체접속자 리스트
	private JList room_list = new JList();	// 채팅방 목록 리스트
	private JTextArea chat_area = new JTextArea(); // 채팅창 변수
	
	
	// 네트워크를 위한 자원 변수
	private Socket socket;
	private String ip;
	private int port;
	private String id;
	
	// 스트림 자원 생성
	private InputStream is;
	private OutputStream os;
	private DataInputStream dis;
	private DataOutputStream dos;
	
	// 그 외 변수들
	Vector User_List = new Vector();
	Vector Room_List = new Vector();
	// 서버로 부터 전달되어온 데이터 파싱해서 어떤 데이터인지 확인하기 위한 용도
	StringTokenizer st;
	
	// 생성자 메소드
	Client() {	
		Login_init();
		Main_init();
		start();
	}
	
	private void start() {
		login_btn.addActionListener(this);	// 로그인 버튼 리스너
		notesend_btn.addActionListener(this);	// 쪽지 보내기 리스너
		joinroom_btn.addActionListener(this);	// 채팅방 참여 리스너
		createroom_btn.addActionListener(this);	// 채팅방 만들기 리스너
		sendmessage_btn.addActionListener(this);	// 채팅 전송 리스너
	}
	
	private void Main_init() {
		// 아래 메소드들의 맨 앞에 this가 생략되어 있음 = JFrame 자체를 상속 받았기 떄문에
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 679, 572);
		main_pane = new JPanel();
		main_pane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(main_pane);
		main_pane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("전 체 접 속 자");
		lblNewLabel.setBounds(12, 26, 129, 15);
		main_pane.add(lblNewLabel);
		
		user_list.setBounds(12, 51, 129, 144);
		main_pane.add(user_list);

		notesend_btn.setBounds(12, 205, 129, 23);
		main_pane.add(notesend_btn);
		
		JLabel label = new JLabel("채 팅 방 목 록");
		label.setBounds(12, 260, 129, 15);
		main_pane.add(label);
		
		room_list.setBounds(12, 285, 129, 139);
		main_pane.add(room_list);
		
		joinroom_btn.setBounds(12, 434, 129, 23);
		main_pane.add(joinroom_btn);
		
		createroom_btn.setBounds(12, 467, 129, 23);
		main_pane.add(createroom_btn);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(160, 47, 487, 398);
		main_pane.add(scrollPane);
		
		scrollPane.setViewportView(chat_area);
		
		message_tf = new JTextField();
		message_tf.setBounds(160, 468, 378, 21);
		main_pane.add(message_tf);
		message_tf.setColumns(10);
		
		sendmessage_btn.setBounds(550, 467, 97, 23);
		main_pane.add(sendmessage_btn);
		
		this.setVisible(true);
	}
	private void Login_init() {
	
		// 하지만 로그인 관련 창에 대해서는 객체를 따로 생성했기 때문에 맨 앞에 Login_GUI 객체명으로 넣어준다.
		Login_GUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		Login_GUI.setBounds(100, 100, 295, 400);
		login_pane = new JPanel();
		login_pane.setBorder(new EmptyBorder(5, 5, 5, 5));
		Login_GUI.setContentPane(login_pane);
		login_pane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Server IP");
		lblNewLabel.setBounds(29, 150, 78, 15);
		login_pane.add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Server Port");
		lblNewLabel_1.setBounds(29, 201, 78, 15);
		login_pane.add(lblNewLabel_1);
		
		JLabel lblId = new JLabel("ID");
		lblId.setBounds(29, 256, 78, 15);
		login_pane.add(lblId);
		
		ip_tf = new JTextField();
		ip_tf.setBounds(119, 147, 116, 21);
		login_pane.add(ip_tf);
		ip_tf.setColumns(10);
		
		port_tf = new JTextField();
		port_tf.setBounds(119, 198, 116, 21);
		login_pane.add(port_tf);
		port_tf.setColumns(10);
		
		id_tf = new JTextField();
		id_tf.setBounds(119, 253, 116, 21);
		login_pane.add(id_tf);
		id_tf.setColumns(10);
		
		
		login_btn.setBounds(29, 304, 206, 23);
		login_pane.add(login_btn);
		
		Login_GUI.setVisible(true); // true = 화면에 보이게	false = 화면에 보이지 않게
		
	}
	
	// 소켓생성 및 커넥션 메소드(스트림/메시지 송신 스레드 생성) 호출
	private void Network() {
		try {
			socket = new Socket(ip, port);
			// 정상적으로 연결 됐을 때 스트림 설정하기
			if(socket!=null) {
				Connection();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
	
	public static void main(String[] args) {
		new Client();

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == login_btn) {
			
			ip = ip_tf.getText().trim();	// trim : 맨 뒤에 빈 공간이 생겼을 때 앞으로 댕겨주는 메소드
			port = Integer.parseInt(port_tf.getText().trim());	// int형 형변환
			id = id_tf.getText().trim();  // ID 값 저장
			System.out.println("로그인버튼 클릭");		
			Network();	
		}
		else if(e.getSource() == notesend_btn) {
			System.out.println("쪽지 보내기 버튼 클릭");
			// 리스트에서 선택한 값 가져오기
			String user = (String)user_list.getSelectedValue();
			
			// 쪽지 보내기 다이알로그 생성
			String note = JOptionPane.showInputDialog("보낼메시지");
			
			// 쪽지 프로토콜 : Note/User1/안녕하세요~
			if(note!=null)
				send_message("Note/"+user+"/"+note);
			
		}
		else if(e.getSource() == joinroom_btn)
			System.out.println("방 참여 버튼 클릭");
		else if(e.getSource() == createroom_btn)
			System.out.println("방 만들기 버튼 클릭");
		else if(e.getSource() == sendmessage_btn) {
			send_message("임시테스트 입니다");
			System.out.println("채팅 전송 버튼 클릭");
		}
	}

	private void Connection() {
		
		// 서버로의 in/out 스트림 설정
		try {
			is = socket.getInputStream();
			os = socket.getOutputStream();
			dis = new DataInputStream(is);
			dos = new DataOutputStream(os);
		} catch(IOException e) {
			
		} // Stream 설정 끝
		
		// 처음 접속 시 ID 받아서 서버로 전달 send_message 사용
		send_message(id);
		User_List.add(id); // Vector에 id 저장
		
		// 입력(서버로부터 메시지 수신)을 위한 스레드 생성(익명 클래스)
		Thread th = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(true) {
					try {
						String msg = dis.readUTF();
						System.out.println("서버로부터 수신된 메시지: "+msg);
						in_message(msg);
						
					} catch (IOException e) {
					}
				}
			}
		});
		th.start();
	}
	
	// 서버로부터 들어오는 모든 메시지
	private void in_message(String str) {
		st = new StringTokenizer(str, "/");
		
		String protocol = st.nextToken();
		String message = st.nextToken();
		
		if(protocol.equals("NewUser")) {
			User_List.add(message);
			user_list.setListData(User_List);
		}
		else if(protocol.equals("ExistingUser")) {
			if(message.equals("End"))
				user_list.setListData(User_List);
			else
				User_List.add(message);
		}
		else if(protocol.equals("Note")) {
			String note = st.nextToken();
			System.out.println(message+"로 부터 온 쪽지: "+note);
			
			JOptionPane.showMessageDialog
			(null, note, message+"님으로 부터 쪽지", JOptionPane.CLOSED_OPTION);
		}
	
	}
	
	// 서버로 메시지 보내는 메소드
	private void send_message(String str) {
		try {
			dos.writeUTF(str);
		} catch (IOException e) {
			
		}
	}
}
