import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;

public class Chat_Image extends JFrame {
    private JLabel sendImage, getImage;
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

        getImageArea = new JPanel();
        getImageArea.setBounds(225,5,200,300);
        getImageArea.setBackground(Color.gray);
        getImage = new JLabel("No image received");


        chooseImgBtn = new JButton("Choose image");
        chooseImgBtn.setBounds(5, 350, 200, 30);

        sendImageBtn = new JButton("Send image");
        sendImageBtn.setEnabled(false);
        sendImageBtn.setBounds(5, 390, 200, 30);

        sendImageArea.add(sendImage);
        getImageArea.add(getImage);
        this.add(chooseImgBtn);
        this.add(sendImageBtn);
        this.add(sendImageArea);
        this.add(getImageArea);
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
        new ImageReceiver(getImage, socket);
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
        Image image = imageIcon.getImage();

            new ImageSender(image, socket, sendImage, localPath);


    }


}

class ImageSender {
    private Image image;
    private Socket socket;
    private ObjectOutputStream out;
    private JLabel sendImage;
    private String localPath;

    public ImageSender(Image image, Socket socket, JLabel sendImage, String localPath){
        this.image = image;
        this.socket = socket;
        this.sendImage = sendImage;
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
            Storage message = new Storage(data, "message");
            System.out.println(data.length);
            out.writeObject(message);
            out.reset();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ImageReceiver extends Thread{
    private JLabel imageLabel;
    private Socket socket;
    private ObjectInputStream in;

    public ImageReceiver(JLabel imageLabel, Socket socket){
        this.imageLabel = imageLabel;
        this.socket = socket;
        start();
    }

    public void run() {
        try {
            while (true) {
                in = new ObjectInputStream(socket.getInputStream());
                Storage storage = (Storage)in.readObject();
                System.out.println(storage.getData().length);
                byte[] data = storage.getData();
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
                BufferedImage receivedBufferedImage = ImageIO.read(byteArrayInputStream);
                ImageIcon receivedImage = new ImageIcon(receivedBufferedImage);
                imageLabel.setIcon(receivedImage);



            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public void kill() throws IOException {
        socket.close();
    }


}
