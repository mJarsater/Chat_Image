import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;

public class Chat_Image extends JFrame {
    private JLabel sendImage, getImage, sendMesssageLabel, getMessageLabel;
    private JTextField sendMessageField;
    private JTextArea getMessageTextArea;
    private JButton chooseImgBtn, sendImageBtn;
    private File selectedImage;
    private JPanel sendImageArea, getImageArea;
    private ImageIcon imageIcon;
    private int port;
    private String address, localPath;
    private Socket socket;


    public Chat_Image(String[]args){
        try {
        sendImageArea = new JPanel();
        sendImageArea.setBounds(5,5,200,300);
        sendImageArea.setBackground(Color.gray);
        sendImage = new JLabel("No image chose");
        sendMesssageLabel = new JLabel("Message: ");
        sendMesssageLabel.setBounds(5, 310, 60, 20);
        sendMessageField = new JTextField();
        sendMessageField.setBounds(65, 310, 135, 20);


        getImageArea = new JPanel();
        getImageArea.setBounds(225,5,200,300);
        getImageArea.setBackground(Color.gray);
        getImage = new JLabel("No image received");
        getMessageLabel = new JLabel("Received message: ");
        getMessageLabel.setBounds(225, 310, 200, 20);
        getMessageTextArea = new JTextArea();
        getMessageTextArea.setEditable(false);
        getMessageTextArea.setBounds(225, 340, 200, 100);
        chooseImgBtn = new JButton("Choose image");
        chooseImgBtn.setBounds(5, 350, 200, 30);

        sendImageBtn = new JButton("Send image");
        sendImageBtn.setEnabled(false);
        sendImageBtn.setBounds(5, 390, 200, 30);

        sendImageArea.add(sendImage);
        getImageArea.add(getImage);
        this.add(chooseImgBtn);
        this.add(sendImageBtn);
        this.add(sendMesssageLabel);
        this.add(sendMessageField);
        this.add(sendImageArea);
        this.add(getImageArea);
        this.add(getMessageLabel);
        this.add(getMessageTextArea);
        sendImageBtn.addActionListener(this:: sendImage);
        chooseImgBtn.addActionListener(this:: getImage);
        this.setLayout(null);
        this.setTitle("Imagetransfer");
        this.setSize(500,500);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        port = 4848;
        address = "atlas.dsv.su.se";
        socket = new Socket(address, port);
        setTitle("Connected...");
        new ImageReceiver(getImage, socket, getMessageTextArea, this);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void main(String[] args) {
        new Chat_Image(args);
    }

    private void getImage(ActionEvent actionEvent) {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if(result == JFileChooser.APPROVE_OPTION) {
            sendImageBtn.setEnabled(true);
            chooseImgBtn.setEnabled(false);
            selectedImage = fileChooser.getSelectedFile();
            try {
                imageIcon = new ImageIcon(ImageIO.read(selectedImage));
                localPath = selectedImage.getPath();
                sendImage.setIcon(imageIcon);
                sendImage.setText("");
            }catch (IOException ioe){
                System.out.println();
            }
        }
    }


    private void sendImage(ActionEvent actionEvent)  {
        System.out.println("Send clicked");
        if(sendMessageField.getText().equals("")){
            String msg = "No message";
            new ImageSender( socket,  localPath ,msg);
        } else {
            String msg = sendMessageField.getText();
            new ImageSender( socket,  localPath ,msg);
    }




}

class ImageSender {

    private Socket socket;
    private ObjectOutputStream out;
    private String msg;

    private String localPath;

    public ImageSender(Socket socket, String localPath, String msg){
        this.msg = msg;
        this.socket = socket;
        this.localPath = localPath;
        sendImage();
    }

    public void sendImage(){
        try {
            OutputStream outputStream = socket.getOutputStream();
            out = new ObjectOutputStream(outputStream);

            BufferedImage bufferedImage = ImageIO.read(new File(localPath));
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpg", byteArrayOutputStream);
            byte[] data = byteArrayOutputStream.toByteArray();
            Storage message = new Storage(data, msg);
            System.out.println(data.length);
            out.writeObject(message);
            out.reset();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ImageReceiver extends Thread {
    private JLabel imageLabel;
    private JTextArea getMessageTextArea;
    private Socket socket;
    private ObjectInputStream in;
    private Chat_Image chat;

    public ImageReceiver(JLabel imageLabel, Socket socket, JTextArea getMessageTextArea, Chat_Image chat) {
        this.getMessageTextArea = getMessageTextArea;
        this.imageLabel = imageLabel;
        this.socket = socket;
        this.chat = chat;
        start();
    }

    public void run() {
        try {
            while (true) {
                in = new ObjectInputStream(socket.getInputStream());
                Storage storage = (Storage) in.readObject();
                byte[] data = storage.getData();
                String msg = storage.getId();
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
                BufferedImage receivedBufferedImage = ImageIO.read(byteArrayInputStream);
                ImageIcon receivedImage = new ImageIcon(receivedBufferedImage);
                imageLabel.setIcon(receivedImage);
                getMessageTextArea.setText(msg);

            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void kill() throws IOException {
        socket.close();
    }


}
}
