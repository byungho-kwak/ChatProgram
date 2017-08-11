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
	
	//�α��� GUI ����
	private JFrame Login_GUI = new JFrame();
	private JPanel login_pane;
	private JTextField ip_tf;	// ip �޴� �ؽ�Ʈ�ʵ�
	private JTextField port_tf; // port �޴� �ؽ�Ʈ�ʵ�
	private JTextField id_tf; // id �޴� �ؽ�Ʈ�ʵ�
	private JButton login_btn = new JButton("�� ��");
	
	// Main GUI ����
	private JPanel main_pane;
	private JTextField message_tf;
	private JButton notesend_btn = new JButton("���� ������");
	private JButton joinroom_btn = new JButton("ä�ù� ����");
	private JButton createroom_btn = new JButton("�� �����");
	private JButton sendmessage_btn = new JButton("����");
	
	private JList user_list = new JList();	//��ü������ ����Ʈ
	private JList room_list = new JList();	// ä�ù� ��� ����Ʈ
	private JTextArea chat_area = new JTextArea(); // ä��â ����
	
	
	// ��Ʈ��ũ�� ���� �ڿ� ����
	private Socket socket;
	private String ip;
	private int port;
	private String id;
	
	// ��Ʈ�� �ڿ� ����
	private InputStream is;
	private OutputStream os;
	private DataInputStream dis;
	private DataOutputStream dos;
	
	// �� �� ������
	Vector User_List = new Vector();
	Vector Room_List = new Vector();
	// ������ ���� ���޵Ǿ�� ������ �Ľ��ؼ� � ���������� Ȯ���ϱ� ���� �뵵
	StringTokenizer st;
	
	// ������ �޼ҵ�
	Client() {	
		Login_init();
		Main_init();
		start();
	}
	
	private void start() {
		login_btn.addActionListener(this);	// �α��� ��ư ������
		notesend_btn.addActionListener(this);	// ���� ������ ������
		joinroom_btn.addActionListener(this);	// ä�ù� ���� ������
		createroom_btn.addActionListener(this);	// ä�ù� ����� ������
		sendmessage_btn.addActionListener(this);	// ä�� ���� ������
	}
	
	private void Main_init() {
		// �Ʒ� �޼ҵ���� �� �տ� this�� �����Ǿ� ���� = JFrame ��ü�� ��� �޾ұ� ������
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 679, 572);
		main_pane = new JPanel();
		main_pane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(main_pane);
		main_pane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("�� ü �� �� ��");
		lblNewLabel.setBounds(12, 26, 129, 15);
		main_pane.add(lblNewLabel);
		
		user_list.setBounds(12, 51, 129, 144);
		main_pane.add(user_list);

		notesend_btn.setBounds(12, 205, 129, 23);
		main_pane.add(notesend_btn);
		
		JLabel label = new JLabel("ä �� �� �� ��");
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
	
		// ������ �α��� ���� â�� ���ؼ��� ��ü�� ���� �����߱� ������ �� �տ� Login_GUI ��ü������ �־��ش�.
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
		
		Login_GUI.setVisible(true); // true = ȭ�鿡 ���̰�	false = ȭ�鿡 ������ �ʰ�
		
	}
	
	// ���ϻ��� �� Ŀ�ؼ� �޼ҵ�(��Ʈ��/�޽��� �۽� ������ ����) ȣ��
	private void Network() {
		try {
			socket = new Socket(ip, port);
			// ���������� ���� ���� �� ��Ʈ�� �����ϱ�
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
			
			ip = ip_tf.getText().trim();	// trim : �� �ڿ� �� ������ ������ �� ������ ����ִ� �޼ҵ�
			port = Integer.parseInt(port_tf.getText().trim());	// int�� ����ȯ
			id = id_tf.getText().trim();  // ID �� ����
			System.out.println("�α��ι�ư Ŭ��");		
			Network();	
		}
		else if(e.getSource() == notesend_btn) {
			System.out.println("���� ������ ��ư Ŭ��");
			// ����Ʈ���� ������ �� ��������
			String user = (String)user_list.getSelectedValue();
			
			// ���� ������ ���̾˷α� ����
			String note = JOptionPane.showInputDialog("�����޽���");
			
			// ���� �������� : Note/User1/�ȳ��ϼ���~
			if(note!=null)
				send_message("Note/"+user+"/"+note);
			
		}
		else if(e.getSource() == joinroom_btn)
			System.out.println("�� ���� ��ư Ŭ��");
		else if(e.getSource() == createroom_btn)
			System.out.println("�� ����� ��ư Ŭ��");
		else if(e.getSource() == sendmessage_btn) {
			send_message("�ӽ��׽�Ʈ �Դϴ�");
			System.out.println("ä�� ���� ��ư Ŭ��");
		}
	}

	private void Connection() {
		
		// �������� in/out ��Ʈ�� ����
		try {
			is = socket.getInputStream();
			os = socket.getOutputStream();
			dis = new DataInputStream(is);
			dos = new DataOutputStream(os);
		} catch(IOException e) {
			
		} // Stream ���� ��
		
		// ó�� ���� �� ID �޾Ƽ� ������ ���� send_message ���
		send_message(id);
		User_List.add(id); // Vector�� id ����
		
		// �Է�(�����κ��� �޽��� ����)�� ���� ������ ����(�͸� Ŭ����)
		Thread th = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(true) {
					try {
						String msg = dis.readUTF();
						System.out.println("�����κ��� ���ŵ� �޽���: "+msg);
						in_message(msg);
						
					} catch (IOException e) {
					}
				}
			}
		});
		th.start();
	}
	
	// �����κ��� ������ ��� �޽���
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
			System.out.println(message+"�� ���� �� ����: "+note);
			
			JOptionPane.showMessageDialog
			(null, note, message+"������ ���� ����", JOptionPane.CLOSED_OPTION);
		}
	
	}
	
	// ������ �޽��� ������ �޼ҵ�
	private void send_message(String str) {
		try {
			dos.writeUTF(str);
		} catch (IOException e) {
			
		}
	}
}
