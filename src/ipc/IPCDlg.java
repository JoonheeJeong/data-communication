package ipc;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

@SuppressWarnings("serial")
public class IPCDlg extends JFrame implements BaseLayer {

	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	BaseLayer UnderLayer;

	private static LayerManager m_LayerMgr = new LayerManager();

	private JTextField ChattingWrite;

	Container contentPane;

	JTextArea ChattingArea;
	JTextArea srcAddress;
	JTextArea dstAddress;

	JLabel lblsrc;
	JLabel lbldst;

	JButton Setting_Button;
	JButton Chat_send_Button;

	static JComboBox<String> NICComboBox;

	int adapterNumber = 0;

	String Text;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		m_LayerMgr.AddLayer(new SocketLayer("Socket"));
		m_LayerMgr.AddLayer(new ChatAppLayer("Chat"));
		m_LayerMgr.AddLayer(new IPCDlg("GUI"));
		
		m_LayerMgr.ConnectLayers(" Socket ( *Chat ( *GUI ) ) ");
	}

	public IPCDlg(String pName) {
		pLayerName = pName;

		setTitle("IPC");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(250, 250, 644, 425);
		contentPane = new JPanel();
		((JComponent) contentPane).setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JPanel chattingPanel = new JPanel();// chatting panel
		chattingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "chatting",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		chattingPanel.setBounds(10, 5, 360, 276);
		contentPane.add(chattingPanel);
		chattingPanel.setLayout(null);

		JPanel chattingEditorPanel = new JPanel();// chatting write panel
		chattingEditorPanel.setBounds(10, 15, 340, 210);
		chattingPanel.add(chattingEditorPanel);
		chattingEditorPanel.setLayout(null);

		ChattingArea = new JTextArea();
		ChattingArea.setEditable(false);
		ChattingArea.setBounds(0, 0, 340, 210);
		chattingEditorPanel.add(ChattingArea);// chatting edit

		JPanel chattingInputPanel = new JPanel();// chatting write panel
		chattingInputPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		chattingInputPanel.setBounds(10, 230, 250, 20);
		chattingPanel.add(chattingInputPanel);
		chattingInputPanel.setLayout(null);

		ChattingWrite = new JTextField();
		ChattingWrite.setBounds(2, 2, 250, 20);// 249
		chattingInputPanel.add(ChattingWrite);
		ChattingWrite.setColumns(10);// writing area

		JPanel settingPanel = new JPanel();
		settingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "setting",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		settingPanel.setBounds(380, 5, 236, 371);
		contentPane.add(settingPanel);
		settingPanel.setLayout(null);

		JPanel sourceAddressPanel = new JPanel();
		sourceAddressPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		sourceAddressPanel.setBounds(10, 96, 170, 20);
		settingPanel.add(sourceAddressPanel);
		sourceAddressPanel.setLayout(null);

		lblsrc = new JLabel("Source Address");
		lblsrc.setBounds(10, 75, 170, 20);
		settingPanel.add(lblsrc);

		srcAddress = new JTextArea();
		srcAddress.setBounds(2, 2, 170, 20);
		sourceAddressPanel.add(srcAddress);// src address

		JPanel destinationAddressPanel = new JPanel();
		destinationAddressPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		destinationAddressPanel.setBounds(10, 212, 170, 20);
		settingPanel.add(destinationAddressPanel);
		destinationAddressPanel.setLayout(null);

		lbldst = new JLabel("Destination Address");
		lbldst.setBounds(10, 187, 190, 20);
		settingPanel.add(lbldst);

		dstAddress = new JTextArea();
		dstAddress.setBounds(2, 2, 170, 20);
		destinationAddressPanel.add(dstAddress);// dst address

		Setting_Button = new JButton("Setting");// setting
		Setting_Button.setBounds(80, 270, 100, 20);
		Setting_Button.addActionListener(new setAddressListener());
		settingPanel.add(Setting_Button);// setting

		Chat_send_Button = new JButton("Send");
		Chat_send_Button.setBounds(270, 230, 80, 20);
		Chat_send_Button.addActionListener(new setAddressListener());
		chattingPanel.add(Chat_send_Button);// chatting send button

		setVisible(true);

	}

	class setAddressListener implements ActionListener {
		@Override
		/*
		 * Setting Button이 눌렸을 때의 동작 처리
		 * 1. SrcAddress의 text를 ChatAppLayer header에 저장
		 * 2. DstAddress의 text를 ChatAppLayer header에 저장
		 * 3. SrcAddress의 text를 SocketLayer Server port에 저장
		 * 4. DstAddress의 text를 SocketLayer Client port에 저장
		 * 5. SocketLayer의 서버를 실행시킴(SocketLayer Thread 동작)
		 * 6. “Setting” Button을 “Reset” Button으로 변경
		 * 7. SrcAddress, DstAddress의 값 변경 못하게 설정
		 * 8. “Reset” Button을 누를 시 SrcAddress, DstAddress의text를 공백으로 변경
		 * 9. “Reset” Button - > “Setting” Button
		 * 
		 * Send Button이 눌렸을 때의 농작 처리
		 * 1. Setting Button이 “Reset”인지 확인
		 * 2. ChattingWrite에 적은 Text를 ChattingArea에 보여준다.
		 * 3. ChatAppLayer에 Send()호출해서 String을 Byte형식으로변경해서 보낸다.
		 * 4. 주소 값이 없으면 “주소 설정 오류” MessageDialog를띄운다.
		 * */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == Setting_Button) {
				
				if (Setting_Button.getText() == "Reset") {
					srcAddress.setText("");
					dstAddress.setText("");
					Setting_Button.setText("Setting");
					srcAddress.setEditable(true);
					dstAddress.setEditable(true);
				} else {
					String Ssrc = srcAddress.getText();
					String Sdst = dstAddress.getText();
					
					int src = Integer.parseInt(Ssrc);
					int dst = Integer.parseInt(Sdst);
					
					((SocketLayer) m_LayerMgr.GetLayer("Socket")).setServerPort(src);
					((SocketLayer) m_LayerMgr.GetLayer("Socket")).setClientPort(dst);
					
					((ChatAppLayer) m_LayerMgr.GetLayer("Chat")).SetEnetSrcAddress(src);
					((ChatAppLayer) m_LayerMgr.GetLayer("Chat")).SetEnetDstAddress(dst);
					((SocketLayer) m_LayerMgr.GetLayer("Socket")).Receive();
					
					Setting_Button.setText("Reset");
					srcAddress.setEditable(false);
					dstAddress.setEditable(false);
				}
				
			}
			
			if (e.getSource() == Chat_send_Button) {
				if (Setting_Button.getText() != "Reset") {
					JOptionPane.showMessageDialog(null, "Port Address Setting Error.\n");
					return;
				}
				String sendingText = ChattingWrite.getText();
				ChattingWrite.setText("");
				ChattingArea.append("[SEND]:" + sendingText + "\n");
				byte[] data = sendingText.getBytes();
				GetUnderLayer().Send(data, data.length);
			}
		}
	}

	public boolean Receive(byte[] input) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < input.length; i++) {
			if (input[i] < 0) { // 한글이면 3바이트
				byte[] temp = new byte[3];
				temp[0] = input[i];
				temp[1] = input[++i];
				temp[2] = input[++i];
				buf.append( new String(temp) );
			} else { // 기타 영문, 문자, 기호이면 1바이트
				buf.append( (char)input[i] );
			}
		}
		ChattingArea.append("[RECV]:" + buf + "\n");
		return true;
	}

	@Override
	public void SetUnderLayer(BaseLayer pUnderLayer) {
		// TODO Auto-generated method stub
		if (pUnderLayer == null)
			return;
		this.p_UnderLayer = pUnderLayer;
	}

	@Override
	public void SetUpperLayer(BaseLayer pUpperLayer) {
		// TODO Auto-generated method stub
		if (pUpperLayer == null)
			return;
		this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
		// nUpperLayerCount++;
	}

	@Override
	public String GetLayerName() {
		// TODO Auto-generated method stub
		return pLayerName;
	}

	@Override
	public BaseLayer GetUnderLayer() {
		// TODO Auto-generated method stub
		if (p_UnderLayer == null)
			return null;
		return p_UnderLayer;
	}

	@Override
	public BaseLayer GetUpperLayer(int nindex) {
		// TODO Auto-generated method stub
		if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
			return null;
		return p_aUpperLayer.get(nindex);
	}

	@Override
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		this.SetUpperLayer(pUULayer);
		pUULayer.SetUnderLayer(this);

	}

}
